package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.RaDecRange;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class RaDecUtil {
    private RaDecUtil() {
    }

    public static List<String> raDecListToStringList(List<RaDecRange> results) {
        return results.stream().map(RaDecRange::toString).collect(Collectors.toList());
    }

    public static List<String> raDecListToStringList(int index, List<RaDecRange> results) {
        AtomicInteger resultIndex = new AtomicInteger();
        return results.stream().map(result -> index + "," + resultIndex.getAndIncrement() + "," + result.toString()).collect(Collectors.toList());
    }
}
