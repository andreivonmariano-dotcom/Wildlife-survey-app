package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class CallLinkPolicyTest {
    @Test public void rejectCrossSurveyOrTaxonLinks(){
        assertTrue(CallLinkPolicy.sameSurveyAndGroup("SV-1","SV-1","Amphibians","Amphibians"));
        assertFalse(CallLinkPolicy.sameSurveyAndGroup("SV-1","SV-2","Amphibians","Amphibians"));
        assertFalse(CallLinkPolicy.sameSurveyAndGroup("SV-1","SV-1","Amphibians","Birds"));
    }
    @Test public void labelMustBeWithinClip(){
        assertTrue(CallLinkPolicy.usableLabelRange(500,1250,2000));
        assertFalse(CallLinkPolicy.usableLabelRange(0,3000,2000));
        assertFalse(CallLinkPolicy.usableLabelRange(600,600,2000));
    }
}
