package edu.utcn.ipprrg.satpred.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the exact CSV serialization of output values. The full-dataset
 * baseline fingerprint (md5) depends on this format being byte-stable.
 */
class OutputFormatTest {

    @Test
    void raDecRangeFormat() {
        RaDecRange raDec = new RaDecRange(56.831647407625006, -46.57223519698233);
        assertEquals("56.831647407625006,-46.57223519698233", raDec.toString());
    }

    @Test
    void ecfCoordFormat() {
        ECFCoord ecf = new ECFCoord(-3655112.350593745, 4595740.367160993, -3997032.957847777);
        assertEquals("-3655112.350593745,4595740.367160993,-3997032.957847777", ecf.toString());
    }

    @Test
    void raDecEcfCombinedFormat() {
        RaDecRange raDec = new RaDecRange(56.831647407625006, -46.57223519698233);
        ECFCoord ecf = new ECFCoord(-3655112.350593745, 4595740.367160993, -3997032.957847777);
        RaDecECF combined = new RaDecECF(raDec, ecf);
        assertEquals(
                "56.831647407625006,-46.57223519698233,-3655112.350593745,4595740.367160993,-3997032.957847777",
                combined.toString());
    }
}
