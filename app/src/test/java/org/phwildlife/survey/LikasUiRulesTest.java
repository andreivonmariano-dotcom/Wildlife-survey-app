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
    @Test public void cannotContinueWithoutActiveSurvey(){
        assertFalse(LikasUiRules.canContinue(false));
        assertTrue(LikasUiRules.canContinue(true));
    }
}
