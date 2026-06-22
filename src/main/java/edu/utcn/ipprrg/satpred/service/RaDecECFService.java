package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.model.ECFCoord;
import edu.utcn.ipprrg.satpred.model.InputLocationTimestamps;
import edu.utcn.ipprrg.satpred.model.InputTimestamp;
import edu.utcn.ipprrg.satpred.model.RaDecECF;
import edu.utcn.ipprrg.satpred.model.RaDecRange;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.InputUtil;
import edu.utcn.ipprrg.satpred.util.LogUtil;
import edu.utcn.ipprrg.satpred.util.TLEUtil;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.measurements.AngularRaDec;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.generation.AngularRaDecBuilder;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RaDecECFService {
    private static final Logger LOG = LogUtil.getLogger(RaDecECFService.class);
    private static final double[] SIGMA = {0.1, 0.1};
    private static final double[] BASE_WEIGHT = {1, 1};

    /**
     * Satellite-independent geometry computed once per run and shared (read-only,
     * thread-safe) across all TLEs: the observation {@link AbsoluteDate}s, the
     * matching EME2000-&gt;ITRF transforms (which depend only on the date), and
     * the observer location. Precomputing these avoids recomputing the expensive
     * frame transforms once per TLE x timestamp.
     */
    public static final class ObservationContext {
        private final List<AbsoluteDate> dates;
        private final List<Transform> eciToEcf;
        private final GeodeticPoint geodeticPoint;
        private final String observatoryName;

        private ObservationContext(List<AbsoluteDate> dates, List<Transform> eciToEcf,
                                   GeodeticPoint geodeticPoint, String observatoryName) {
            this.dates = dates;
            this.eciToEcf = eciToEcf;
            this.geodeticPoint = geodeticPoint;
            this.observatoryName = observatoryName;
        }

        public int size() {
            return dates.size();
        }
    }

    /**
     * Precomputes the per-run, satellite-independent geometry once. The
     * EME2000-&gt;ITRF transform at a given instant is identical for every
     * satellite, so it is computed here (N times) rather than per TLE (N x M).
     */
    public ObservationContext buildContext(InputLocationTimestamps inputLocationTimestamps, String observatoryName) {
        final Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final List<InputTimestamp> timestamps = inputLocationTimestamps.getInputTimestamps();
        final List<AbsoluteDate> dates = new ArrayList<>(timestamps.size());
        final List<Transform> eciToEcf = new ArrayList<>(timestamps.size());
        for (final InputTimestamp timestamp : timestamps) {
            final AbsoluteDate date = InputUtil.itToAD(timestamp);
            dates.add(date);
            eciToEcf.add(Constants.J2000_FRAME.getTransformTo(itrf, date));
        }
        final GeodeticPoint geodeticPoint = InputUtil.ilToGP(inputLocationTimestamps.getInputLocation());
        return new ObservationContext(dates, eciToEcf, geodeticPoint, observatoryName);
    }

    /**
     * Computes the Right Ascension / Declination and ECF position for a single
     * timestamp, using the precomputed (satellite-independent) ECI-&gt;ECF
     * transform. Throws {@link OrekitException} if OREKIT cannot produce a value
     * (e.g. an unpropagatable, decayed orbit); the caller decides how to react.
     */
    private RaDecECF getRaDecRange(AbsoluteDate absoluteDate, Transform eciToEcf,
                                   TLEPropagator tlePropagator, AngularRaDecBuilder angularRaDecBuilder) {
        // Position / Velocity in the inertial frame
        final TimeStampedPVCoordinates timeStampedPVCoordinates =
                tlePropagator.getPVCoordinates(absoluteDate, Constants.J2000_FRAME);

        // ECF position (transform is shared across TLEs for this date)
        final PVCoordinates pvECF = eciToEcf.transformPVCoordinates(timeStampedPVCoordinates);
        final Vector3D position = pvECF.getPosition();
        final ECFCoord ecfCoord = new ECFCoord(position.getX(), position.getY(), position.getZ());

        // Right Ascension - Declination
        final AbsolutePVCoordinates absolutePVCoordinates =
                new AbsolutePVCoordinates(Constants.J2000_FRAME, timeStampedPVCoordinates);
        final SpacecraftState spacecraftState = new SpacecraftState(absolutePVCoordinates);

        angularRaDecBuilder.init(absoluteDate, absoluteDate);
        final AngularRaDec angularRaDec = angularRaDecBuilder.build(absoluteDate, new SpacecraftState[]{spacecraftState});

        final double rightAscension = FastMath.toDegrees(angularRaDec.getObservedValue()[0]);
        final double declination = FastMath.toDegrees(angularRaDec.getObservedValue()[1]);

        final RaDecRange raDecRange = new RaDecRange(
                rightAscension < 0 ? 360 + rightAscension : rightAscension,
                declination);
        return new RaDecECF(raDecRange, ecfCoord);
    }

    /**
     * Computes one result per timestamp (in order) for a single TLE, reusing the
     * shared {@link ObservationContext}. If a timestamp fails to propagate (e.g.
     * a decayed orbit that has gone hyperbolic), the remaining timestamps for
     * this TLE are skipped and the failure is logged once — such failures are a
     * property of the orbit, so they would recur for every timestamp anyway.
     * The returned list therefore holds 0..N results. The TLE propagator and the
     * (stateful) measurement builder are created per TLE for thread safety.
     */
    public List<RaDecECF> getRaDecList(List<String> tleLines, ObservationContext context) {
        final TLE tle = TLEUtil.getTLE(tleLines);
        final TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);

        final TopocentricFrame topocentricFrame =
                new TopocentricFrame(Constants.PLANET_EARTH_BODY_SHAPE, context.geodeticPoint, context.observatoryName);
        final GroundStation groundStation = new GroundStation(topocentricFrame);
        final ObservableSatellite observableSatellite = new ObservableSatellite(0);
        final AngularRaDecBuilder angularRaDecBuilder = new AngularRaDecBuilder(
                null, groundStation, Constants.J2000_FRAME, SIGMA, BASE_WEIGHT, observableSatellite);

        final int n = context.size();
        final List<RaDecECF> results = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            final AbsoluteDate date = context.dates.get(i);
            try {
                results.add(getRaDecRange(date, context.eciToEcf.get(i), tlePropagator, angularRaDecBuilder));
            } catch (OrekitException e) {
                LOG.warning("Skipping satellite " + tle.getSatelliteNumber() + " after "
                        + results.size() + "/" + n + " timestamp(s) (remaining skipped): " + e.getMessage());
                break;
            }
        }
        return results;
    }
}
