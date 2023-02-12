package edu.utcn.ipprrg.satpred.model;

public class RaDecRange {
    // Right Ascension - Declination NORMALIZED <= 0° < value < 360°
    private Double raDec; // [degrees]
    // Range NORMALIZED <= 0° < value < 360°
    private Double range; // [degrees]

    public RaDecRange() {
    }

    public RaDecRange(Double raDec, Double range) {
        this.raDec = raDec;
        this.range = range;
    }

    public Double getRaDec() {
        return raDec;
    }

    public void setRaDec(Double raDec) {
        this.raDec = raDec;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return raDec + "," + range;
    }
}
