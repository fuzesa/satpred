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
     * Computes the Right Ascension / Declination and ECF position for a single
     * timestamp. Returns {@code null} (logged) if OREKIT cannot produce a value,
     * so the caller can skip the row rather than emit fabricated zeros.
     */
    private RaDecECF getRaDecRange(AbsoluteDate absoluteDate, TLEPropagator tlePropagator,
                                   Frame itrf, AngularRaDecBuilder angularRaDecBuilder) {
        try {
            // Position / Velocity in the inertial frame
            final TimeStampedPVCoordinates timeStampedPVCoordinates =
                    tlePropagator.getPVCoordinates(absoluteDate, Constants.J2000_FRAME);

            // ECF position (date-dependent transform must stay per-timestamp)
            final Transform eciToEcf = Constants.J2000_FRAME.getTransformTo(itrf, absoluteDate);
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
        } catch (OrekitException e) {
            LOG.warning("Skipping timestamp " + absoluteDate + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Computes one result per timestamp (in order). Entries may be {@code null}
     * when a particular timestamp failed; callers skip those.
     * Per-TLE OREKIT objects (frame, ground station, measurement builder) are
     * created once here and reused across all timestamps for this TLE.
     */
    public List<RaDecECF> getRaDecList(List<String> tleLines, InputLocationTimestamps inputLocationTimestamps,
                                       String observatoryName) {
        final TLE tle = TLEUtil.getTLE(tleLines);
        final TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);
        final GeodeticPoint geodeticPoint = InputUtil.ilToGP(inputLocationTimestamps.getInputLocation());

        final Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        final TopocentricFrame topocentricFrame =
                new TopocentricFrame(Constants.PLANET_EARTH_BODY_SHAPE, geodeticPoint, observatoryName);
        final GroundStation groundStation = new GroundStation(topocentricFrame);
        final ObservableSatellite observableSatellite = new ObservableSatellite(0);
        final AngularRaDecBuilder angularRaDecBuilder = new AngularRaDecBuilder(
                null, groundStation, Constants.J2000_FRAME, SIGMA, BASE_WEIGHT, observableSatellite);

        final List<InputTimestamp> timestamps = inputLocationTimestamps.getInputTimestamps();
        final List<RaDecECF> results = new ArrayList<>(timestamps.size());
        for (final InputTimestamp timestamp : timestamps) {
            results.add(getRaDecRange(InputUtil.itToAD(timestamp), tlePropagator, itrf, angularRaDecBuilder));
        }
        return results;
    }
}
