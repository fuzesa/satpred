package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.RaDecECF;

import java.util.ArrayList;
import java.util.List;

public final class RaDecECFUtil {
    private RaDecECFUtil() {
    }

    /**
     * Formats one CSV row per non-null result:
     * {@code tleIndex,timestampIndex,RA,Dec,ecfX,ecfY,ecfZ}.
     * The timestamp index is the position in the list, so skipped (null)
     * entries leave a gap rather than renumbering the surviving rows.
     */
    public static List<String> raDecECFListToStringList(int index, List<RaDecECF> results) {
        final List<String> out = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            final RaDecECF result = results.get(i);
            if (result == null) {
                continue;
            }
            out.add(index + "," + i + "," + result.toString());
        }
        return out;
    }
}
