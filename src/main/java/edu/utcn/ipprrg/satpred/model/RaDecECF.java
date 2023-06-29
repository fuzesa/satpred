package edu.utcn.ipprrg.satpred.model;

import java.util.Map;

public class RaDecECF {
    Map<RaDecRange, ECFCoord> entries;

    public RaDecECF(Map<RaDecRange, ECFCoord> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return entries.keySet().toArray()[0].toString() + "," + entries.values().toArray()[0].toString();
    }
}
