package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.InputLocation;
import edu.utcn.ipprrg.satpred.model.InputLocationTimestamps;
import edu.utcn.ipprrg.satpred.model.InputTimestamp;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.time.AbsoluteDate;

import java.util.List;
import java.util.stream.Collectors;

public final class InputUtil {
    private static final String DELIMITER = ",";
    private static final int NUMBER_OF_PARAMS_FOR_LOCATION = 3;
    private static final int NUMBER_OF_PARAMS_FOR_TIMESTAMP = 6;

    private InputUtil() {
    }

    public static InputLocation parseStringToIL(String input) {
        final String[] parts = split(input, NUMBER_OF_PARAMS_FOR_LOCATION, "location (latitude,longitude,altitude)");
        final InputLocation inputLocation = new InputLocation();
        inputLocation.setLatitude(parseDouble(parts[0], "latitude", input));
        inputLocation.setLongitude(parseDouble(parts[1], "longitude", input));
        inputLocation.setAltitude(parseDouble(parts[2], "altitude", input));
        return inputLocation;
    }

    public static InputTimestamp parseStringToIT(String input) {
        final String[] parts = split(input, NUMBER_OF_PARAMS_FOR_TIMESTAMP, "timestamp (year,month,day,hour,minutes,seconds)");
        final InputTimestamp inputTimestamp = new InputTimestamp();
        inputTimestamp.setYear(parseInt(parts[0], "year", input));
        inputTimestamp.setMonth(parseInt(parts[1], "month", input));
        inputTimestamp.setDay(parseInt(parts[2], "day", input));
        inputTimestamp.setHour(parseInt(parts[3], "hour", input));
        inputTimestamp.setMinutes(parseInt(parts[4], "minutes", input));
        inputTimestamp.setSeconds(parseDouble(parts[5], "seconds", input));
        return inputTimestamp;
    }

    public static InputLocationTimestamps parseInputFileToILT(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Input file is empty: expected a location line followed by timestamp lines");
        }
        final InputLocation inputLocation = parseStringToIL(lines.get(0));
        final List<InputTimestamp> inputTimestamps = lines.stream().skip(1)
                .map(InputUtil::parseStringToIT)
                .collect(Collectors.toList());
        return new InputLocationTimestamps(inputLocation, inputTimestamps);
    }

    public static GeodeticPoint ilToGP(InputLocation inputLocation) {
        final double latitude = FastMath.toRadians(inputLocation.getLatitude());
        final double longitude = FastMath.toRadians(inputLocation.getLongitude());
        final double altitude = inputLocation.getAltitude();
        return new GeodeticPoint(latitude, longitude, altitude);
    }

    public static AbsoluteDate itToAD(InputTimestamp inputTimestamp) {
        return new AbsoluteDate(
                inputTimestamp.getYear(), inputTimestamp.getMonth(), inputTimestamp.getDay(),
                inputTimestamp.getHour(), inputTimestamp.getMinutes(), inputTimestamp.getSeconds(),
                Constants.UTC_TIMESCALE);
    }

    private static String[] split(String input, int expected, String what) {
        if (input == null) {
            throw new IllegalArgumentException("Missing input line for " + what);
        }
        final String[] parts = input.split(DELIMITER, -1);
        if (parts.length != expected) {
            throw new IllegalArgumentException("Invalid " + what + ": expected " + expected
                    + " comma-separated values but got " + parts.length + " >> \"" + input + "\"");
        }
        return parts;
    }

    private static double parseDouble(String value, String field, String line) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for " + field + " (\"" + value + "\") in line: \"" + line + "\"");
        }
    }

    private static int parseInt(String value, String field, String line) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + field + " (\"" + value + "\") in line: \"" + line + "\"");
        }
    }
}
