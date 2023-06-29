package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.ECFCoord;
import edu.utcn.ipprrg.satpred.model.RaDecECF;
import edu.utcn.ipprrg.satpred.model.RaDecRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RaDecECFUtil {
    private RaDecECFUtil() {
    }

    public static List<String> raDecECFListToStringList(int index, List<RaDecECF> results) {
        AtomicInteger resultIndex = new AtomicInteger();
        return results.stream().map(result -> index + "," + resultIndex.getAndIncrement() + "," + result.toString()).collect(Collectors.toList());
    }
}
