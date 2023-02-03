package edu.utcn.ipprrg;

import edu.utcn.ipprrg.proc.CustomTLE;
import edu.utcn.ipprrg.proc.Position;
import edu.utcn.ipprrg.util.FileUtil;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.estimation.measurements.AngularRaDec;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            FileUtil.getManagerWithLoadedData();
            final String proba = FileUtil.getResourceFileAsString("tle.txt");
            final List<String> probaList = Arrays.asList(proba.split("\n"));
            final CustomTLE customTLE = new CustomTLE(probaList);
            final TLE probaTLE = customTLE.getTLE().get();

//            final AbsoluteDate probaAbsDateTime = new AbsoluteDate(2023, 2, 3, 12, 30, 00, TimeScalesFactory.getUTC());
            final AbsoluteDate utcnAbsDateTime = new AbsoluteDate(2023, 1, 18, 16, 58, 51.717659, TimeScalesFactory.getUTC());
//            final Position pos = new Position(probaAbsDateTime);
            //final List<String> states = pos.getState();
            double semiMajorAxis = Math.pow(3.986004415e+14, (double) 1 / 3) / Math.pow(2 * probaTLE.getMeanMotion() * Math.PI / 86400, (double) 2 / 3);
            final Position pos = new Position(utcnAbsDateTime, semiMajorAxis, probaTLE.getE(), probaTLE.getI(), probaTLE.getPerigeeArgument(), probaTLE.getRaan(), probaTLE.getMeanAnomaly());
            final String state =  pos.getState();

            final OneAxisEllipsoid bodyShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));

            double utcnLat = 46.71040333;
            double utcnLon = 23.593594666;
            double utcnAlt = 787.56;

            final GeodeticPoint geodeticPoint = new GeodeticPoint(utcnLat, utcnLon, utcnAlt);

            final String observatoryName = "Feleacu";

            final TopocentricFrame topocentricFrame = new TopocentricFrame(bodyShape, geodeticPoint, observatoryName);

            final GroundStation groundStation = new GroundStation(topocentricFrame);

            final Frame probaFrame = bodyShape.getFrame();

            final ObservableSatellite observableSatellite = new ObservableSatellite(0);





            System.out.println(proba);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException();
        }
    }
}