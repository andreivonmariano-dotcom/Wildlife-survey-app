package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class FieldProtocolPolicyTest {
    @Test public void zeroDetectionsDoNotBlockCompletion(){
        assertTrue(FieldProtocolPolicy.canComplete("coverboard_search",3,0,0));
        assertTrue(FieldProtocolPolicy.canComplete("area_search",1,0,0));
        assertTrue(FieldProtocolPolicy.canComplete("track_sign",2,0,0));
    }
    @Test public void missingEffortIsIncomplete(){
        assertFalse(FieldProtocolPolicy.canComplete("coverboard_search",2,1,0));
        assertFalse(FieldProtocolPolicy.canComplete("coverboard_search",0,0,0));
        assertFalse(FieldProtocolPolicy.canComplete("area_search",0,0,0));
        assertFalse(FieldProtocolPolicy.canComplete("track_sign",1,0,1));
        assertFalse(FieldProtocolPolicy.canComplete("spotlight",0,0,0));
        assertTrue(FieldProtocolPolicy.canComplete("spotlight",2,0,0));
    }
    @Test public void timeAndMeasuredEffortArePositive(){
        assertTrue(FieldProtocolPolicy.validInterval(1000,2000));
        assertFalse(FieldProtocolPolicy.validInterval(1000,1000));
        assertTrue(FieldProtocolPolicy.validArea(120));
        assertFalse(FieldProtocolPolicy.validArea(Double.NaN));
        assertFalse(FieldProtocolPolicy.validDistance(-3));
    }
}
