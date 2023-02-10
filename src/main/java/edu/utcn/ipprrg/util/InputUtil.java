package edu.utcn.ipprrg.util;

import edu.utcn.ipprrg.proc.InputLocation;
import edu.utcn.ipprrg.proc.InputLocationTimestamps;
import edu.utcn.ipprrg.proc.InputTimestamp;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.time.AbsoluteDate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static edu.utcn.ipprrg.util.Constants.UTC_TIMESCALE;

public final class InputUtil {
    private static final String DELIMITER = ",";
    private static final int NUMBER_OF_PARAMS_FOR_LOCATION = 3;
    private static final int NUMBER_OF_PARAMS_FOR_TIMESTAMP = 6;

    private InputUtil() {
    }

    public static InputLocation parseStringToIL(String input) {
        final InputLocation inputLocation = new InputLocation();
        final List<String> splitUp = Arrays.asList(input.split(DELIMITER));
        if (splitUp.size() == NUMBER_OF_PARAMS_FOR_LOCATION) {
            inputLocation.setLatitude(Double.parseDouble(splitUp.get(0)));
            inputLocation.setLongitude(Double.parseDouble(splitUp.get(1)));
            inputLocation.setAltitude(Double.parseDouble(splitUp.get(2)));
        }
        return inputLocation;
    }

    public static InputTimestamp parseStringToIT(String input) {
        final InputTimestamp inputTimestamp = new InputTimestamp();
        final List<String> splitUp = Arrays.asList(input.split(DELIMITER));
        if (splitUp.size() == NUMBER_OF_PARAMS_FOR_TIMESTAMP) {
            inputTimestamp.setYear(Integer.parseInt(splitUp.get(0)));
            inputTimestamp.setMonth(Integer.parseInt(splitUp.get(1)));
            inputTimestamp.setDay(Integer.parseInt(splitUp.get(2)));
            inputTimestamp.setHour(Integer.parseInt(splitUp.get(3)));
            inputTimestamp.setMinutes(Integer.parseInt(splitUp.get(4)));
            inputTimestamp.setSeconds(Double.parseDouble(splitUp.get(5)));
        }
        return inputTimestamp;
    }

    public static InputLocationTimestamps parseInputFileToILT(List<String> lines) {
        final InputLocation inputLocation = parseStringToIL(lines.get(0));
        final List<InputTimestamp> inputTimestamps = lines.stream().skip(1).map(InputUtil::parseStringToIT).collect(Collectors.toList());
        return new InputLocationTimestamps(inputLocation, inputTimestamps);
    }

    public static GeodeticPoint ilToGP(InputLocation inputLocation) {
        double latitude = FastMath.toRadians(inputLocation.getLatitude());
        double longitude = FastMath.toRadians(inputLocation.getLongitude());
        double altitude = inputLocation.getAltitude();

        return new GeodeticPoint(latitude, longitude, altitude);
    }

    public static AbsoluteDate itToAD(InputTimestamp inputTimestamp) {
        return new AbsoluteDate(
                inputTimestamp.getYear(), inputTimestamp.getMonth(), inputTimestamp.getDay(),
                inputTimestamp.getHour(), inputTimestamp.getMinutes(), inputTimestamp.getSeconds(),
                UTC_TIMESCALE);
    }
}
