package edu.utcn.ipprrg.util;

import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

public final class Constants {

    private Constants() {
    }

    public static final TimeScale UTC_TIMESCALE = TimeScalesFactory.getUTC(); // UTC Timescale
    public static final double MU = 3.986004415e+14;                          // μ - Central attraction coefficient, standard gravitational parameter for planet Earth
    public static final Frame J2000_FRAME = FramesFactory.getEME2000();       // EME2000 inertial frame - https://en.wikipedia.org/wiki/Earth-centered_inertial
}
