/**
 * Format taken from NORAD specification
 * Source: https://celestrak.org/NORAD/documentation/tle-fmt.php
 *
 * AAAAAAAAAAAAAAAAAAAAAAAA
 * 1 NNNNNU NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
 * 2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNNNNNNNN
 *
 * Line 0 is a twenty-four character name (to be consistent with the name length in the NORAD SATCAT).
 * Lines 1 and 2 are the standard Two-Line Orbital Element Set Format identical to that used by NORAD and NASA.
 *
 * Example:
 *
 * NOAA 14
 * 1 23455U 94089A   97320.90946019  .00000140  00000-0  10191-3 0  2621
 * 2 23455  99.0090 272.6745 0008546 223.1686 136.8816 14.11711747148495
 */
package edu.utcn.ipprrg.util;

import org.orekit.propagation.analytical.tle.TLE;

import java.util.List;

import static edu.utcn.ipprrg.util.Constants.MU;

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
            throw new RuntimeException();
        }
    }

    // https://space.stackexchange.com/questions/18289/how-to-get-semi-major-axis-from-tle
    public static double getSemiMajorAxis(double meanMotion) {
        return Math.pow(MU, (double) 1 / 3) / Math.pow(2 * meanMotion * Math.PI / NUMBER_OF_SECONDS_IN_A_DAY, (double) 2 / 3);
    }
}
