package edu.utcn.ipprrg.satpred.util;

import edu.utcn.ipprrg.satpred.model.InputLocation;
import edu.utcn.ipprrg.satpred.model.InputTimestamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputUtilTest {

    @Test
    void parsesValidLocation() {
        InputLocation loc = InputUtil.parseStringToIL("46.710545,23.593554,752.799988");
        assertEquals(46.710545, loc.getLatitude());
        assertEquals(23.593554, loc.getLongitude());
        assertEquals(752.799988, loc.getAltitude());
    }

    @Test
    void parsesValidTimestamp() {
        InputTimestamp ts = InputUtil.parseStringToIT("2025,9,4,18,13,30.5");
        assertEquals(2025, ts.getYear());
        assertEquals(9, ts.getMonth());
        assertEquals(4, ts.getDay());
        assertEquals(18, ts.getHour());
        assertEquals(13, ts.getMinutes());
        assertEquals(30.5, ts.getSeconds());
    }

    @Test
    void rejectsLocationWithWrongFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> InputUtil.parseStringToIL("46.71,23.59"));
    }

    @Test
    void rejectsTimestampWithWrongFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> InputUtil.parseStringToIT("2025,9,4,18,13"));
    }

    @Test
    void rejectsNonNumericLocation() {
        assertThrows(IllegalArgumentException.class, () -> InputUtil.parseStringToIL("north,23.59,752.8"));
    }

    @Test
    void rejectsNonIntegerTimestampField() {
        assertThrows(IllegalArgumentException.class, () -> InputUtil.parseStringToIT("2025,Sep,4,18,13,0.0"));
    }
}
