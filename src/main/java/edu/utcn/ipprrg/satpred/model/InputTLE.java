/**
 * Format taken from NORAD specification
 * Source: https://celestrak.org/NORAD/documentation/tle-fmt.php
 * <p>
 * AAAAAAAAAAAAAAAAAAAAAAAA
 * 1 NNNNNU NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
 * 2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNNNNNNNN
 * <p>
 * Line 0 is a twenty-four character name (to be consistent with the name length in the NORAD SATCAT).
 * Lines 1 and 2 are the standard Two-Line Orbital Element Set Format identical to that used by NORAD and NASA.
 * <p>
 * Example:
 * <p>
 * NOAA 14
 * 1 23455U 94089A   97320.90946019  .00000140  00000-0  10191-3 0  2621
 * 2 23455  99.0090 272.6745 0008546 223.1686 136.8816 14.11711747148495
 */
package edu.utcn.ipprrg.satpred.model;

import java.util.List;

public class InputTLE {
    private int index;
    private List<String> lines;

    public InputTLE(int index, List<String> lines) {
        this.index = index;
        this.lines = lines;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
