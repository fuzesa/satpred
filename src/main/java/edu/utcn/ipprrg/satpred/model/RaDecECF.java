package edu.utcn.ipprrg.satpred.model;

/**
 * Pairs a Right Ascension / Declination observation with the satellite's
 * Earth-Centered-Fixed (ECF) position for the same instant.
 */
public class RaDecECF {
    private final RaDecRange raDec;
    private final ECFCoord ecf;

    public RaDecECF(RaDecRange raDec, ECFCoord ecf) {
        this.raDec = raDec;
        this.ecf = ecf;
    }

    public RaDecRange getRaDec() {
        return raDec;
    }

    public ECFCoord getEcf() {
        return ecf;
    }

    @Override
    public String toString() {
        return raDec.toString() + "," + ecf.toString();
    }
}
