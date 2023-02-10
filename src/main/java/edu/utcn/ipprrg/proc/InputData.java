package edu.utcn.ipprrg.proc;

import java.util.List;
import java.util.Objects;

/**
 * Format taken from NORAD specification
 * Source: https://celestrak.org/NORAD/documentation/tle-fmt.php
 *
 * AAAAAAAAAAAAAAAAAAAAAAAA
 * 1 NNNNNU NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
 * 2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNNNNNNNN
 *
 * Line 0 is a twenty-four character name (to be consistent with the name length in the NORAD SATCAT).
 * Lines 1 and 2 are the standard Two-Line Orbital Element Set Format identical to that used by NORAD and NASA.
 *
 * Example:
 *
 * NOAA 14
 * 1 23455U 94089A   97320.90946019  .00000140  00000-0  10191-3 0  2621
 * 2 23455  99.0090 272.6745 0008546 223.1686 136.8816 14.11711747148495 
 */
public class InputData {
    private String line0;
    private String line1;
    private String line2;

    public InputData() {
    }

    public InputData(String line0, String line1, String line2) {
        this.line0 = line0;
        this.line1 = line1;
        this.line2 = line2;
    }

    public InputData(List<String> input) {
        setValsFromList(input);
    }

    public String getLine0() {
        return line0;
    }

    public void setLine0(String line0) {
        this.line0 = line0;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public void setValsFromList(List<String> input) {
        if (3 == input.size()) {
            this.line0 = input.get(0);
            this.line1 = input.get(1);
            this.line2 = input.get(2);
        }
        if (2 == input.size()) {
            this.line1 = input.get(0);
            this.line2 = input.get(1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputData inputData = (InputData) o;
        return Objects.equals(line0, inputData.line0) && Objects.equals(line1, inputData.line1) && Objects.equals(line2, inputData.line2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line0, line1, line2);
    }
}
