package edu.utcn.ipprrg.satpred.util;

import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.propagation.analytical.tle.TLE;

import java.util.List;

public final class TLEUtil {

    private static final double NUMBER_OF_SECONDS_IN_A_DAY = 86400;

    private TLEUtil() {
    }

    public static TLE getTLE(List<String> input) {
        // Assumption inputData is valid (has line1 & line2)
        final String line1 = input.get(1);
        final String line2 = input.get(2);
        if (TLE.isFormatOK(line1, line2)) {
            return new TLE(line1, line2);
        } else {
            throw new OrekitException(OrekitMessages.TLE_INVALID_PARAMETER);
        }
    }

    // https://space.stackexchange.com/questions/18289/how-to-get-semi-major-axis-from-tle
    public static double getSemiMajorAxis(double meanMotion) {
        return Math.pow(Constants.MU, (double) 1 / 3) / Math.pow(2 * meanMotion * Math.PI / NUMBER_OF_SECONDS_IN_A_DAY, (double) 2 / 3);
    }
}
