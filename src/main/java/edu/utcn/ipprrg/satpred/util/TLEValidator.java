package edu.utcn.ipprrg.satpred.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Line-level filter for TLE files: keeps the name line ('0'), and the two
 * data lines ('1' and '2'). Full TLE format correctness (field layout,
 * checksums) is validated separately via {@link org.orekit.propagation.analytical.tle.TLE#isFormatOK}.
 */
public class TLEValidator implements Validator<String> {
    private static final Set<Character> VALID_STARTING_CHARS =
            new HashSet<>(Arrays.asList('0', '1', '2'));

    @Override
    public boolean isValid(String obj) {
        return null != obj && !obj.isEmpty() && VALID_STARTING_CHARS.contains(obj.charAt(0));
    }
}
