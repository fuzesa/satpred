package edu.utcn.ipprrg.proc;

import edu.utcn.ipprrg.util.InputUtil;
import edu.utcn.ipprrg.util.TLEUtil;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.estimation.measurements.AngularRaDec;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.generation.AngularRaDecBuilder;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.List;
import java.util.stream.Collectors;

import static edu.utcn.ipprrg.util.Constants.J2000_FRAME;
import static edu.utcn.ipprrg.util.Constants.PLANET_EARTH_BODY_SHAPE;

public class Process {

    private final List<String> tleLines;

    private final List<String> locTSLines;

    private String observatoryName = "";

    public Process(List<String> tleLines, List<String> locTSLines) {
        this.tleLines = tleLines;
        this.locTSLines = locTSLines;
    }

    public Process(List<String> tleLines, List<String> locTSLines, String observatoryName) {
        this.tleLines = tleLines;
        this.locTSLines = locTSLines;
        this.observatoryName = observatoryName;
    }

    private double[] processOne(AbsoluteDate absoluteDate, TLEPropagator tlePropagator, GeodeticPoint geodeticPoint, String observatoryName) {

        // To get the Position / Velocity coords
        final TimeStampedPVCoordinates timeStampedPVCoordinates = tlePropagator.getPVCoordinates(absoluteDate, J2000_FRAME);

        // 2 - GET RIGHT ASCENSION - DECLINATION

        final TopocentricFrame topocentricFrame = new TopocentricFrame(PLANET_EARTH_BODY_SHAPE, geodeticPoint, observatoryName);

        final GroundStation groundStation = new GroundStation(topocentricFrame);

        final ObservableSatellite observableSatellite = new ObservableSatellite(0);

        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(J2000_FRAME, timeStampedPVCoordinates);

        final SpacecraftState spacecraftState = new SpacecraftState(absolutePVCoordinates);

        double[] sigma = {0.1, 0.1};
        double[] baseWeight = {1, 1};

        final SpacecraftState[] spacecraftStates = {spacecraftState};

        final AngularRaDecBuilder angularRaDecBuilder = new AngularRaDecBuilder(null, groundStation, J2000_FRAME, sigma, baseWeight, observableSatellite);

        angularRaDecBuilder.init(absoluteDate, absoluteDate);

        final AngularRaDec angularRaDec = angularRaDecBuilder.build(spacecraftStates);

        double[] degrees = {
                FastMath.toDegrees(angularRaDec.getObservedValue()[0]),
                FastMath.toDegrees(angularRaDec.getObservedValue()[1])
        };

        return new double[]{
                (degrees[0] < 0 ? 360 + degrees[0] : degrees[0]),
                (degrees[1] < 0 ? 360 + degrees[1] : degrees[1])
        };
    }

    public List<double[]> getAnglesInNormalizedDegrees() {
        final TLE tle = TLEUtil.getTLE(tleLines);
        final InputLocationTimestamps inputLocationTimestamps = InputUtil.parseInputFileToILT(locTSLines);


        // Use a TLE propagator
        final TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);
        final GeodeticPoint geodeticPoint = InputUtil.ilToGP(inputLocationTimestamps.getInputLocation());

        return inputLocationTimestamps.getInputTimestamps().stream().map(it -> processOne(InputUtil.itToAD(it), tlePropagator, geodeticPoint, this.observatoryName)).collect(Collectors.toList());
    }
}
