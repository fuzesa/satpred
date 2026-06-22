package edu.utcn.ipprrg.satpred.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TLEValidatorTest {

    private final TLEValidator validator = new TLEValidator();

    @Test
    void acceptsTleLineStarts() {
        assertTrue(validator.isValid("0 VANGUARD 1"));
        assertTrue(validator.isValid("1 00005U 58002B   25246.94746652 ..."));
        assertTrue(validator.isValid("2 00005  34.2570 117.7759 ..."));
    }

    @Test
    void rejectsNullEmptyAndOtherStarts() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("# comment"));
        assertFalse(validator.isValid("3 something"));
    }
}
