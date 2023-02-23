package edu.utcn.ipprrg.satpred.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TLEValidator implements Validator<String> {
    private static final List<Character> VALID_STARTING_CHARS = new ArrayList<>(Arrays.asList('0', '1', '2'));

    @Override
    public Boolean isValid(String obj) {
        return null != obj && !obj.isEmpty() && VALID_STARTING_CHARS.contains(obj.charAt(0));
    }
}
