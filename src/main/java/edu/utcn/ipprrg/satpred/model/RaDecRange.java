package edu.utcn.ipprrg.satpred.model;

/**
 * A single Right Ascension / Declination observation, in degrees.
 * Right Ascension is normalized to [0, 360); declination is in [-90, 90].
 */
public class RaDecRange {
    private final double rightAscension; // [degrees], normalized to [0, 360)
    private final double declination;    // [degrees], in [-90, 90]

    public RaDecRange(double rightAscension, double declination) {
        this.rightAscension = rightAscension;
        this.declination = declination;
    }

    public double getRightAscension() {
        return rightAscension;
    }

    public double getDeclination() {
        return declination;
    }

    @Override
    public String toString() {
        return rightAscension + "," + declination;
    }
}
