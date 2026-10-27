package org.phwildlife.survey;

import static org.junit.Assert.*;
import org.junit.Test;

public final class SurveyMethodsDisplayTest {
    @Test public void unknownMethodIsNotRebrandedAsFirstOption(){
        assertEquals("old nighttime census",SurveyMethods.displayLabel("Birds",
            "old_nighttime_census"));
        assertEquals("Method not recorded",SurveyMethods.displayLabel("Mammals",""));
    }
    @Test public void currentMethodUsesReadableLabel(){
        assertEquals("Amphibian calling",
            SurveyMethods.displayLabel("Herpetofauna","amphibian_calls"));
        assertEquals("Spotlight survey",
            SurveyMethods.displayLabel("Mammals","spotlight"));
    }
}
