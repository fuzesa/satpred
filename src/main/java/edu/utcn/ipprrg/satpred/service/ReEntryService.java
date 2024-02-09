package edu.utcn.ipprrg.satpred.service;

import edu.utcn.ipprrg.satpred.util.Constants;
import edu.utcn.ipprrg.satpred.util.FileUtil;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.frames.FramesFactory;
import org.orekit.models.earth.atmosphere.NRLMSISE00;
import org.orekit.models.earth.atmosphere.data.CssiSpaceWeatherData;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.DSSTPropagatorBuilder;
import org.orekit.propagation.conversion.DormandPrince853IntegratorBuilder;
import org.orekit.propagation.conversion.FiniteDifferencePropagatorConverter;
import org.orekit.propagation.events.AltitudeDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.propagation.semianalytical.dsst.DSSTPropagator;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTAtmosphericDrag;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ReEntryService {

    private static final String DEFAULT_TEST_RESOURCES_PATH = "src/test/resources/";
    private static final String DEFAULT_DATA_DIR = DEFAULT_TEST_RESOURCES_PATH + "orekit-data";
    private static final String SAMPLE_TLE_PATH = DEFAULT_TEST_RESOURCES_PATH + "tle.txt";
    private static final String SPACE_WEATHER_SUPPORTED_NAMES = "SpaceWeather-All-v1.2.txt";
    private static final double MIN_TIME_STEP = 360;
    private static final double MAX_TIME_STEP = 7200;
    private static final double POS_TOLERANCE = 10;
    private static final double CD = 2.2;
    private static final double MASS_IN_KG = 3007.93;
    private static final double AREA = 11.3162;
    private static final double FITTING_DURATION = 60 * 60 * 24;
    private static final double FITTING_STEP = 60 * 5;
    private static final double FITTING_THRESHOLD = 0.001;
    private static final int FITTING_MAX_ITERS = 1000;
    private static final int HANDLER_TIME = 60 * 60;
    private static final double ALTITUDE_DETECTOR_MAX_CHECK = 60;
    private static final double ALTITUDE_DETECTOR_THRESHOLD = 0.001;
    private static final double ALTITUDE_DETECTOR_ALTITUDE = 100;
    private static final int EARTH_RADIUS_METERS = 6378137;


    public void init() {
        FileUtil.loadManagerWithLoadedData(DEFAULT_DATA_DIR);

        final List<String> tleStrings = FileUtil.readTextFileToList(SAMPLE_TLE_PATH);
        final TLE probaTLE = new TLE(tleStrings.get(1), tleStrings.get(2), Constants.UTC_TIMESCALE);
        final Propagator tlePropagator = TLEPropagator.selectExtrapolator(probaTLE);

        final Orbit refOrbit = tlePropagator.getInitialState().getOrbit();
        DormandPrince853IntegratorBuilder dormandPrince853IntegratorBuilder = new DormandPrince853IntegratorBuilder(MIN_TIME_STEP, MAX_TIME_STEP, POS_TOLERANCE);
        DSSTPropagatorBuilder dsstPropagatorBuilder = new DSSTPropagatorBuilder(refOrbit, dormandPrince853IntegratorBuilder, POS_TOLERANCE, PropagationType.MEAN, PropagationType.MEAN);
        dsstPropagatorBuilder.setMass(MASS_IN_KG);

        final CssiSpaceWeatherData spaceWeatherData = new CssiSpaceWeatherData(SPACE_WEATHER_SUPPORTED_NAMES);

        final NRLMSISE00 atmosphere = new NRLMSISE00(spaceWeatherData, CelestialBodyFactory.getSun(), Constants.PLANET_EARTH_BODY_SHAPE);

        dsstPropagatorBuilder.addForceModel(new DSSTAtmosphericDrag(atmosphere, CD, AREA, Constants.MU));

        final FiniteDifferencePropagatorConverter dsstFitter = new FiniteDifferencePropagatorConverter(dsstPropagatorBuilder, FITTING_THRESHOLD, FITTING_MAX_ITERS);
        dsstFitter.convert(tlePropagator, FITTING_DURATION, (int) (FITTING_DURATION / FITTING_STEP));
        final DSSTPropagator dsstPropagator = (DSSTPropagator) dsstFitter.getAdaptedPropagator();
        System.out.println(dsstPropagator.getInitialState().getOrbit());

        final TimeStampedPVCoordinates tspvc1 = dsstPropagator.getInitialState().getPVCoordinates(FramesFactory.getEME2000());
        final CartesianOrbit cartesianOrbit = new CartesianOrbit(tspvc1, FramesFactory.getEME2000(), dsstPropagator.getMu());
        final SpacecraftState newState = new SpacecraftState(cartesianOrbit, MASS_IN_KG);
        dsstPropagator.resetInitialState(newState);

        final EventDetector altdet = new AltitudeDetector(ALTITUDE_DETECTOR_MAX_CHECK, ALTITUDE_DETECTOR_THRESHOLD, ALTITUDE_DETECTOR_ALTITUDE, Constants.PLANET_EARTH_BODY_SHAPE).withHandler(((s, detector, decreasing) -> {
            System.out.println("Altitude is " + FastMath.min(((s.getA() * (1 + s.getE())) - EARTH_RADIUS_METERS) / 1000, ((s.getA() * (1 - s.getE())) - EARTH_RADIUS_METERS) / 1000) + " km and the spacecraft will re-enter soon");
            System.out.println("Re-entry event occurs on " + s.getDate().toString());
            return Action.STOP;
        }));

        dsstPropagator.addEventDetector(altdet);

        final SpacecraftStatesHandler spacecraftStatesHandler = new SpacecraftStatesHandler();

        dsstPropagator.getMultiplexer().add(HANDLER_TIME, spacecraftStatesHandler);

        final List<SpacecraftState> states = spacecraftStatesHandler.getStates();

        // Semi Major Axis
        // Eccentricity
        // Inclination
        // Right Ascension of Ascending Node
        // Angle of Polarization
        // Mean Anomaly

        final File output = new File("re-entry.txt");
        try (final PrintStream stream = new PrintStream(output)) {
            for (SpacecraftState state : states) {
                final KeplerianOrbit Orb = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(state.getOrbit());
                String text = "%s,%.4f,%.7f,%.5f,%.5f,%.5f,%.5f";
                text = String.format(text, Orb.getDate(), Orb.getA() / 1000, Orb.getE(), FastMath.toDegrees(Orb.getI()), FastMath.toDegrees(Orb.getRightAscensionOfAscendingNode()), FastMath.toDegrees(Orb.getPerigeeArgument()), FastMath.toDegrees(Orb.getMeanAnomaly()));
                stream.println(text);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SpacecraftStatesHandler implements OrekitFixedStepHandler {
        private final List<SpacecraftState> states = new ArrayList<>();

        public void handleStep(final SpacecraftState currentState) {
            states.add(currentState);
        }

        public List<SpacecraftState> getStates() {
            return states;
        }
    }

}
