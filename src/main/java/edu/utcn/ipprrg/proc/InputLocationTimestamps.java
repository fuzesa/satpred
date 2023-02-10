package edu.utcn.ipprrg.proc;

import java.util.List;

public class InputLocationTimestamps {
    private final InputLocation inputLocation;

    private final List<InputTimestamp> inputTimestamps;

    public InputLocationTimestamps(InputLocation inputLocation, List<InputTimestamp> inputTimestamps) {
        this.inputLocation = inputLocation;
        this.inputTimestamps = inputTimestamps;
    }

    public InputLocation getInputLocation() {
        return inputLocation;
    }

    public List<InputTimestamp> getInputTimestamps() {
        return inputTimestamps;
    }
}
