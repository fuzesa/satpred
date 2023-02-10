package edu.utcn.ipprrg.util;

import java.util.List;
import java.util.stream.Collectors;

public final class ResultUtil {
    private ResultUtil() {
    }

    public static List<String> resultsToStringList(List<double[]> results) {
        return results.stream().map(result -> result[0] + "," + result[1]).collect(Collectors.toList());
    }
}
