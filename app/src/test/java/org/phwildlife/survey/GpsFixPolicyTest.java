package org.phwildlife.survey;

import static org.junit.Assert.*;
import org.junit.Test;

public final class GpsFixPolicyTest {
    private static final long NOW = 1_800_000_000_000L;

    @Test public void acceptsFreshFixUnderTenMeters() {
        assertTrue(GpsFixPolicy.acceptable(7.1, 125.5, 9.99f, NOW - 1000, NOW, false));
    }
    @Test public void rejectsBoundaryAndWorseAccuracy() {
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 10f, NOW, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 75f, NOW, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, Float.NaN, NOW, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 0f, NOW, NOW, false));
    }
    @Test public void rejectsStaleFutureAndMockLocations() {
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 2f, NOW - 10001, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 2f, NOW + 1, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, 125.5, 2f, NOW, NOW, true));
    }
    @Test public void rejectsInvalidCoordinates() {
        assertFalse(GpsFixPolicy.acceptable(91, 125.5, 2f, NOW, NOW, false));
        assertFalse(GpsFixPolicy.acceptable(7.1, Double.NaN, 2f, NOW, NOW, false));
    }
}
