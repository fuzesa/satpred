package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.RaDecRange;

import java.util.List;
import java.util.stream.Collectors;

public final class RaDecUtil {
    private RaDecUtil() {
    }

    public static List<String> raDecListToStringList(List<RaDecRange> results) {
        return results.stream().map(RaDecRange::toString).collect(Collectors.toList());
    }
}
