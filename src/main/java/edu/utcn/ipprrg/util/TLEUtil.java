package edu.utcn.ipprrg.util;

import edu.utcn.ipprrg.proc.InputData;
import org.orekit.propagation.analytical.tle.TLE;

import static edu.utcn.ipprrg.util.Constants.MU;

public final class TLEUtil {

    private static final double NUMBER_OF_SECONDS_IN_A_DAY = 86400;

    private TLEUtil() {
    }

    public static TLE getTLEFromInputData(InputData inputData) {
        // Assumption inputData is valid (has line1 & line2)
        final String line1 = inputData.getLine1();
        final String line2 = inputData.getLine2();
        if (TLE.isFormatOK(line1, line2)) {
            return new TLE(line1, line2);
        } else {
            throw new RuntimeException();
        }
    }

    // https://space.stackexchange.com/questions/18289/how-to-get-semi-major-axis-from-tle
    public static double getSemiMajorAxis(double meanMotion) {
        return Math.pow(MU, (double) 1 / 3) / Math.pow(2 * meanMotion * Math.PI / NUMBER_OF_SECONDS_IN_A_DAY, (double) 2 / 3);
    }
}
