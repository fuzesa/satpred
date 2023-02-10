package edu.utcn.ipprrg.util;

import edu.utcn.ipprrg.proc.Position;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;

import static edu.utcn.ipprrg.util.Constants.J2000_FRAME;
import static edu.utcn.ipprrg.util.Constants.MU;

public final class SatelliteUtil {

    private SatelliteUtil() {
    }

    public static SpacecraftState getState(OrbitType orbitType, Position position) {
        Orbit orbit;
        SpacecraftState currentState = null;
        if (orbitType == OrbitType.KEPLERIAN) {
            final AbsoluteDate absoluteDate = position.getInitialDate();
            orbit = new KeplerianOrbit(position.getSemiMajorAxis(), position.getEccentricity(), position.getInclination(), position.getPerigeeArgument(), position.getRightAscensionOfAscendingNode(), position.getMeanAnomaly(), PositionAngle.MEAN, J2000_FRAME, absoluteDate, MU);
            final KeplerianPropagator kepler = new KeplerianPropagator(orbit);
            currentState = kepler.propagate(absoluteDate);
        }
        return currentState;
    }

}
