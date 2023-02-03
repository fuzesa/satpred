package edu.utcn.ipprrg.proc;

import org.hipparchus.util.FastMath;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Position {
    private static final TimeScale utc = TimeScalesFactory.getUTC();       // UTC timescale, although it should be a global constant, not just to this class
    private static final Frame inertialFrame = FramesFactory.getEME2000(); // EME2000 inertial frame
    private static double mu = 3.986004415e+14;                            // μ - Central attraction coefficient, standard gravitational parameter for planet Earth
    private AbsoluteDate initialDate;
    private double duration = 600.;
    private double stepT = 60.;
    private double semiMajorAxis;
    private double eccentricity;
    private double inclination;
    private double perigeeArgument;
    private double rightAscensionOfAscendingNode;
    private double meanAnomaly;

    public Position() {
    }

    public Position(AbsoluteDate initialDate, double semiMajorAxis, double eccentricity, double inclination, double perigeeArgument, double rightAscensionOfAscendingNode, double meanAnomaly) {
        this.initialDate = initialDate;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.perigeeArgument = perigeeArgument;
        this.rightAscensionOfAscendingNode = rightAscensionOfAscendingNode;
        this.meanAnomaly = meanAnomaly;
    }

    public Position(AbsoluteDate initialDate, double duration, double stepT, double semiMajorAxis, double eccentricity, double inclination, double perigeeArgument, double rightAscensionOfAscendingNode, double meanAnomaly) {
        this.initialDate = initialDate;
        this.duration = duration;
        this.stepT = stepT;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.perigeeArgument = perigeeArgument;
        this.rightAscensionOfAscendingNode = rightAscensionOfAscendingNode;
        this.meanAnomaly = meanAnomaly;
    }

    public static double getMu() {
        return mu;
    }

    public static void setMu(double mu) {
        Position.mu = mu;
    }

    public AbsoluteDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(AbsoluteDate initialDate) {
        this.initialDate = initialDate;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getStepT() {
        return stepT;
    }

    public void setStepT(double stepT) {
        this.stepT = stepT;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public double getInclination() {
        return inclination;
    }

    public void setInclination(double inclination) {
        this.inclination = inclination;
    }

    public double getPerigeeArgument() {
        return perigeeArgument;
    }

    public void setPerigeeArgument(double perigeeArgument) {
        this.perigeeArgument = perigeeArgument;
    }

    public double getRightAscensionOfAscendingNode() {
        return rightAscensionOfAscendingNode;
    }

    public void setRightAscensionOfAscendingNode(double rightAscensionOfAscendingNode) {
        this.rightAscensionOfAscendingNode = rightAscensionOfAscendingNode;
    }

    public double getMeanAnomaly() {
        return meanAnomaly;
    }

    public void setMeanAnomaly(double meanAnomaly) {
        this.meanAnomaly = meanAnomaly;
    }

    public String getState() {
        final Orbit keplerianOrbit = new KeplerianOrbit(this.semiMajorAxis, this.eccentricity, this.inclination,
                this.perigeeArgument, this.rightAscensionOfAscendingNode, this.meanAnomaly, PositionAngle.MEAN,
                inertialFrame, initialDate, mu);
        final KeplerianPropagator kepler = new KeplerianPropagator(keplerianOrbit);
        final SpacecraftState currentState = kepler.propagate(initialDate);
        return String.format(Locale.US, "%s %s%n", currentState.getDate(), currentState.getOrbit());
    }

    public List<String> getStateList() {
        // Some default params
        double a = 24396159;                                    // semi major axis in meters
        double e = 0.72831215;                                  // eccentricity
        double i = FastMath.toRadians(7);                    // inclination
        double omega = FastMath.toRadians(180);              // perigee argument
        double raan = FastMath.toRadians(261);               // right ascension of ascending node
        double lM = 0;                                          // mean anomaly

        // Initial Keplerian Orbit
        final Orbit initialOrbit = new KeplerianOrbit(a, e, i, omega, raan, lM, PositionAngle.MEAN, inertialFrame, initialDate, mu);

        // Propagator for determining motion
        final KeplerianPropagator kepler = new KeplerianPropagator(initialOrbit);

        // Perform calculation for positions
        final AbsoluteDate finalDate = initialDate.shiftedBy(duration);
        int cpt = 1;
        final List<String> calculatedState = new ArrayList<>();
        for (AbsoluteDate extrapDate = initialDate; extrapDate.compareTo(finalDate) <= 0; extrapDate = extrapDate.shiftedBy(stepT)) {
            SpacecraftState currentState = kepler.propagate(extrapDate);
//            System.out.format(Locale.US, "step %2d %s %s%n", cpt++, currentState.getDate(), currentState.getOrbit());
            calculatedState.add(String.format(Locale.US, "step %2d %s %s%n", cpt++, currentState.getDate(), currentState.getOrbit()));
        }

        return calculatedState;
    }
}
