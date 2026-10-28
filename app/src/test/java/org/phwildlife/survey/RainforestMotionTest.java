package org.phwildlife.survey;

import static org.junit.Assert.*;
import org.junit.Test;

public class RainforestMotionTest {
    @Test public void lightingTransitionsAreContinuousAndNormalized() {
        for (int minute = 0; minute < 1440; minute++) {
            float[] w = RainforestMotion.lightWeights(minute);
            float total = 0;
            for (float v : w) { assertTrue(v >= 0 && v <= 1); total += v; }
            assertEquals(1f, total, .0001f);
            if (minute > 0) {
                float[] previous = RainforestMotion.lightWeights(minute-1);
                for (int i = 0; i < 4; i++)
                    assertTrue("Lighting jump at minute " + minute,
                        Math.abs(w[i] - previous[i]) < .03f);
            }
        }
    }

    @Test public void timeOfDayUsesExpectedStates() {
        assertEquals(1f, RainforestMotion.lightWeights(0)[3], .001f);
        assertEquals(1f, RainforestMotion.lightWeights(396)[0], .001f);
        assertEquals(1f, RainforestMotion.lightWeights(720)[1], .001f);
        assertEquals(1f, RainforestMotion.lightWeights(1080)[2], .001f);
        assertEquals(1f, RainforestMotion.lightWeights(1260)[3], .001f);
    }

    @Test public void independentWavesRemainFiniteAndDifferent() {
        for (float sec = 0; sec <= 360f; sec += .25f) {
            float canopy = RainforestMotion.wave(sec, 6.7f, .4f);
            float clouds = RainforestMotion.wave(sec, 64f, .65f);
            assertFalse(Float.isNaN(canopy));
            assertTrue(Math.abs(canopy) <= 1.0001);
            assertTrue(Math.abs(clouds) <= 1.0001);
        }
        assertNotEquals(RainforestMotion.wave(3f, 6.7f, .4f),
            RainforestMotion.wave(3f, 64f, .65f), .001f);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidMinuteThrows() {
        RainforestMotion.lightWeights(1440);
    }
}