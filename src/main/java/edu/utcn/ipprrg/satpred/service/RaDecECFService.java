package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.model.ECFCoord;
import edu.utcn.ipprrg.satpred.model.InputLocationTimestamps;
import edu.utcn.ipprrg.satpred.model.RaDecECF;
import edu.utcn.ipprrg.satpred.model.RaDecRange;
import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.InputUtil;
import edu.utcn.ipprrg.satpred.util.TLEUtil;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.measurements.AngularRaDec;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.generation.AngularRaDecBuilder;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RaDecECFService {
    private RaDecECF getRaDecRange(AbsoluteDate absoluteDate, TLEPropagator tlePropagator, GeodeticPoint geodeticPoint, String observatoryName) {
        Map<RaDecRange, ECFCoord> retMap = new HashMap<>();

        // TODO: Add error handling for individual internal errors
        try {
            // Get the Position / Velocity coords
            final TimeStampedPVCoordinates timeStampedPVCoordinates = tlePropagator.getPVCoordinates(absoluteDate, Constants.J2000_FRAME);

            //final TimeStampedPVCoordinates probaPVCoords = tlePropagator.getPVCoordinates(absoluteDate, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

            final Transform eciToEcf = Constants.J2000_FRAME.getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), absoluteDate);
            final PVCoordinates pvECF = eciToEcf.transformPVCoordinates(timeStampedPVCoordinates);
            final Vector3D position = pvECF.getPosition();
            final ECFCoord ecfCoord = new ECFCoord(position.getX(), position.getY(), position.getZ());

            // Get Right Ascension - Declination + Range

            final TopocentricFrame topocentricFrame = new TopocentricFrame(Constants.PLANET_EARTH_BODY_SHAPE, geodeticPoint, observatoryName);

            final GroundStation groundStation = new GroundStation(topocentricFrame);

            final ObservableSatellite observableSatellite = new ObservableSatellite(0);

            final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(Constants.J2000_FRAME, timeStampedPVCoordinates);

            final SpacecraftState spacecraftState = new SpacecraftState(absolutePVCoordinates);

            double[] sigma = {0.1, 0.1};
            double[] baseWeight = {1, 1};

            final SpacecraftState[] spacecraftStates = {spacecraftState};

            final AngularRaDecBuilder angularRaDecBuilder = new AngularRaDecBuilder(null, groundStation, Constants.J2000_FRAME, sigma, baseWeight, observableSatellite);

            angularRaDecBuilder.init(absoluteDate, absoluteDate);

            final AngularRaDec angularRaDec = angularRaDecBuilder.build(spacecraftStates);

            // convert radians to degrees
            double[] degrees = {
                    FastMath.toDegrees(angularRaDec.getObservedValue()[0]),
                    FastMath.toDegrees(angularRaDec.getObservedValue()[1])
            };

            // return proper object with normalized value for Right Ascension
            final RaDecRange raDecRange = new RaDecRange(
                    (degrees[0] < 0 ? 360 + degrees[0] : degrees[0]),
                    degrees[1]
            );
            retMap.put(raDecRange, ecfCoord);
        } catch (OrekitException e) {
            System.err.println("OREKIT ERROR: " + absoluteDate.toString() + " >> " + e.getMessage() + " || Resulting RaDec will be [0°, 0°]");
            final RaDecRange raDecRange = new RaDecRange(0d, 0d);
            final ECFCoord ecfCoord = new ECFCoord(0, 0, 0);
            retMap.put(raDecRange, ecfCoord);
        }
        return new RaDecECF(retMap);
    }

    public List<RaDecECF> getRaDecList(List<String> tleLines, List<String> locTSLines, String observatoryName) {
        final TLE tle = TLEUtil.getTLE(tleLines);
        final InputLocationTimestamps inputLocationTimestamps = InputUtil.parseInputFileToILT(locTSLines);


        // Use a TLE propagator
        final TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);
        final GeodeticPoint geodeticPoint = InputUtil.ilToGP(inputLocationTimestamps.getInputLocation());

        return inputLocationTimestamps.getInputTimestamps().stream().map(it -> getRaDecRange(InputUtil.itToAD(it), tlePropagator, geodeticPoint, observatoryName)).collect(Collectors.toList());
    }
}
