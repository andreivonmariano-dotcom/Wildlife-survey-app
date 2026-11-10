package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SurveyStartGpsPolicyTest {
    @Test public void verifiedFixRemainsDefault(){
        assertEquals(SurveyStartGpsPolicy.VERIFIED,
            SurveyStartGpsPolicy.classify(false,7.19,125.47,6f,1500,false));
    }
    @Test public void noPermissionOrNoFixRequiresExplicitSkip(){
        assertNull(SurveyStartGpsPolicy.classify(false,Double.NaN,Double.NaN,
            Float.NaN,Long.MAX_VALUE,false));
        assertEquals(SurveyStartGpsPolicy.MISSING,
            SurveyStartGpsPolicy.classify(true,Double.NaN,Double.NaN,
                Float.NaN,Long.MAX_VALUE,false));
    }
    @Test public void poorOldOrMockFixNeverCountsAsVerified(){
        assertNull(SurveyStartGpsPolicy.classify(false,7.19,125.47,12f,1200,false));
        assertNull(SurveyStartGpsPolicy.classify(false,7.19,125.47,4f,13000,false));
        assertNull(SurveyStartGpsPolicy.classify(false,7.19,125.47,4f,1000,true));
    }
    @Test public void explicitSkipCannotBecomeAccidentalVerifiedStart(){
        assertEquals(SurveyStartGpsPolicy.MISSING,
            SurveyStartGpsPolicy.classify(true,7.19,125.47,3f,1000,false));
    }
}
