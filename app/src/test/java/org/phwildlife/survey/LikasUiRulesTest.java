package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class LikasUiRulesTest {
    @Test public void daytimeAndNightBoundaries(){
        assertTrue(LikasUiRules.isNight(0));
        assertTrue(LikasUiRules.isNight(5));
        assertFalse(LikasUiRules.isNight(6));
        assertFalse(LikasUiRules.isNight(10));
        assertFalse(LikasUiRules.isNight(17));
        assertTrue(LikasUiRules.isNight(18));
        assertTrue(LikasUiRules.isNight(23));
    }
    @Test public void nightAndDawnDaytimeFade(){
        assertEquals(0f,LikasUiRules.daylightFactor(0),0.0001f);
        assertEquals(0f,LikasUiRules.daylightFactor(300),0.0001f);
        assertEquals(.5f,LikasUiRules.daylightFactor(360),0.0001f);
        assertEquals(1f,LikasUiRules.daylightFactor(420),0.0001f);
        assertEquals(1f,LikasUiRules.daylightFactor(1050),0.0001f);
        assertEquals(.5f,LikasUiRules.daylightFactor(1095),0.0001f);
        assertEquals(0f,LikasUiRules.daylightFactor(1140),0.0001f);
        assertEquals(0f,LikasUiRules.daylightFactor(1439),0.0001f);
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidClockMinuteRejected(){LikasUiRules.daylightFactor(1440);}
    @Test public void cannotContinueWithoutActiveSurvey(){
        assertFalse(LikasUiRules.canContinue(false));
        assertTrue(LikasUiRules.canContinue(true));
    }
}
