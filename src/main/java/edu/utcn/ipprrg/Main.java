package edu.utcn.ipprrg;

import edu.utcn.ipprrg.proc.InputData;
import edu.utcn.ipprrg.util.FileUtil;
import edu.utcn.ipprrg.util.TLEUtil;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.estimation.measurements.AngularRaDec;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.generation.AngularRaDecBuilder;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedPVCoordinates;

import static edu.utcn.ipprrg.util.Constants.J2000_FRAME;
import static edu.utcn.ipprrg.util.Constants.UTC_TIMESCALE;

public class Main {
    private static final String RESOURCE_FILE_NAME = "tle.txt";

    public static void main(String[] args) {
        FileUtil.loadManagerWithLoadedData();

        /* FIRST ATTEMPT */
        /*final InputData inputData = FileUtil.parseResourceToInputData(resourceFileName);
        final TLE probaTLE = TLEUtil.getTLEFromInputData(inputData);
        final AbsoluteDate probaDate = new AbsoluteDate(2023, 1, 18, 16, 58, 51.717659, TimeScalesFactory.getUTC());

        //final List<String> states = pos.getState();
        final Position pos = new Position(probaDate, probaTLE);
        // final String state = pos.getState();
        final SpacecraftState spacecraftState = SatelliteUtil.getState(OrbitType.KEPLERIAN, pos);

        final OneAxisEllipsoid bodyShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

        double feleacuLat = 46.71040333;
        double feleacuLon = 23.593594666;
        double feleacuAlt = 787.56;

        final GeodeticPoint feleacuCoords = new GeodeticPoint(feleacuLat, feleacuLon, feleacuAlt);

        final String observatoryName = "Feleacu";

        final TopocentricFrame topocentricFrame = new TopocentricFrame(bodyShape, feleacuCoords, observatoryName);

        final GroundStation feleacuStation = new GroundStation(topocentricFrame);

        final Frame probaFrame = bodyShape.getFrame();

        final ObservableSatellite observableSatellite = new ObservableSatellite(0);

        double angs[] = {spacecraftState.getA()};

        double sigmas[] = {0};

        double baseWeights[] = {1000};

        final AngularRaDec angularRaDec = new AngularRaDec(feleacuStation, probaFrame, probaDate, angs, sigmas, baseWeights, observableSatellite);*/

        /* SECOND ATTEMPT */
        // https://forum.orekit.org/t/satellite-prediction/827
        // https://forum.orekit.org/t/problem-with-measurements-generation/1589/3

        // 1 - GET POSITION - VELOCITY VECTORS

        // Create the TLE object
        final InputData inputData = FileUtil.parseResourceToInputData(RESOURCE_FILE_NAME);
        final TLE tle = TLEUtil.getTLEFromInputData(inputData);

        // Use a TLE propagator
        final TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);
        final AbsoluteDate first = new AbsoluteDate(2023, 1, 18, 16, 58, 51.717659, UTC_TIMESCALE);
        

        double a = TLEUtil.getSemiMajorAxis(tle.getMeanMotion());

        // To get the Position / Velocity coords
        final TimeStampedPVCoordinates timeStampedPVCoordinates = tlePropagator.getPVCoordinates(first, J2000_FRAME);

        // 2 - GET RIGHT ASCENSION - DECLINATION

        // Define coordinates of the station
        double feleacuLat = 46.71040333;
        double feleacuLon = 23.593594666;
        double feleacuAlt = 787.56;

        // Coordinates must be converted to radians for the constructor
        double feleacuLatRad = FastMath.toRadians(feleacuLat);
        double feleacuLonRad = FastMath.toRadians(feleacuLon);

        final GeodeticPoint feleacuCoords = new GeodeticPoint(feleacuLatRad, feleacuLonRad, feleacuAlt);


        // Define planet Earth's shape
        final OneAxisEllipsoid bodyShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

        // https://forum.orekit.org/t/angularazel-to-radec/1665/4

        // Name for the ground station
        final String observatoryName = "Feleacu";

        final TopocentricFrame topocentricFrame = new TopocentricFrame(bodyShape, feleacuCoords, observatoryName);

        final GroundStation feleacuStation = new GroundStation(topocentricFrame);

        final ObservableSatellite observableSatellite = new ObservableSatellite(0);

        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(J2000_FRAME, timeStampedPVCoordinates);

        final SpacecraftState spacecraftState = new SpacecraftState(absolutePVCoordinates);

        double sigma[] = {0.1, 0.1};
        double baseWeight[] = {1, 1};

        final SpacecraftState spacecraftStates[] = {spacecraftState};

        final AngularRaDecBuilder angularRaDecBuilder = new AngularRaDecBuilder(null, feleacuStation, J2000_FRAME, sigma, baseWeight, observableSatellite);

        angularRaDecBuilder.init(first, first);

        final AngularRaDec angularRaDec = angularRaDecBuilder.build(spacecraftStates);

        double degrees[] = {
                FastMath.toDegrees(angularRaDec.getObservedValue()[0]),
                FastMath.toDegrees(angularRaDec.getObservedValue()[1])
        };

        System.out.println(first + " " + (degrees[0] < 0 ? 360 + degrees[0] : degrees[0]));
        System.out.println(first + " " + (degrees[1] < 0 ? 360 + degrees[1] : degrees[1]));
    }
}