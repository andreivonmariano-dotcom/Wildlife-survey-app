package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SurveyNavigationPolicyTest {
    @Test public void fieldworkPickerReturnsToSurveyList(){
        assertTrue(SurveyNavigationPolicy.backToSurveySelector(null,"SV-123"));
        assertFalse(SurveyNavigationPolicy.backToSurveySelector(null,null));
    }

    @Test public void recorderOwnedProtocolReturnsDirectlyToRecorder(){
        assertFalse(SurveyNavigationPolicy.backToSurveySelector("SV-123","SV-123"));
        assertFalse(SurveyNavigationPolicy.backToSurveySelector("SV-123",null));
    }

    @Test public void historyDetailReturnsToOwner(){
        assertTrue(SurveyNavigationPolicy.closeHistoryToCaller(true,"SV-123"));
        assertFalse(SurveyNavigationPolicy.closeHistoryToCaller(false,"SV-123"));
        assertTrue(SurveyNavigationPolicy.closeHistoryToCaller(false,null));
    }
}
