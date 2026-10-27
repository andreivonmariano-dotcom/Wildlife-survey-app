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
    @Test public void elapsedClockAcceptsAccurateFixWhenWallClockIsAhead(){
        // The phone can report -0.2 s wall-clock age for a legitimate GNSS fix.
        assertTrue(GpsFixPolicy.acceptableElapsed(7.1,125.5,3.8f,200,false));
        assertTrue(GpsFixPolicy.acceptableElapsed(7.1,125.5,3.8f,-200,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.1,125.5,3.8f,-1001,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.1,125.5,3.8f,10001,false));
    }
    @Test public void elapsedClockStillRequiresSubTenMetersAndRealCoordinates(){
        assertTrue(GpsFixPolicy.acceptableElapsed(7.1,125.5,9.9f,200,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.1,125.5,10f,200,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.1,125.5,3f,200,true));
        assertFalse(GpsFixPolicy.acceptableElapsed(95,125.5,3f,200,false));
    }
}
