package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;

public final class SurveyClosePolicyTest {
    private String outcome(boolean request,boolean cancel,boolean stations,int visits,
            int unfinished,int missed,int target,long effort,boolean route,Double distance,
            boolean area,Double areaM2){
        return SurveyClosePolicy.resolve(request,cancel,stations,visits,unfinished,missed,
            target,effort,route,distance,area,areaM2);
    }
    @Test public void canceledAlwaysNotCounted(){
        assertEquals("not_counted",outcome(true,true,true,0,4,8,600,0,true,null,false,null));
    }
    @Test public void deliberateEarlyEndRemainsIncomplete(){
        assertEquals("incomplete",outcome(false,false,false,1,0,0,0,5000,false,null,false,null));
    }
    @Test public void cannotCompleteWithUnvisitedOrUnfinishedPoints(){
        assertEquals("incomplete",outcome(true,false,true,4,0,1,600,700000,false,null,false,null));
        assertEquals("incomplete",outcome(true,false,true,4,1,0,600,700000,false,null,false,null));
        assertEquals("incomplete",outcome(true,false,true,0,0,0,600,700000,false,null,false,null));
        assertEquals("complete",outcome(true,false,true,4,0,0,600,700000,false,null,false,null));
    }
    @Test public void earlyTransectCannotComplete(){
        assertEquals("incomplete",outcome(true,false,false,0,0,0,600,599999,true,2000d,false,null));
        assertEquals("incomplete",outcome(true,false,false,0,0,0,600,600000,true,null,false,null));
        assertEquals("complete",outcome(true,false,false,0,0,0,600,600000,true,1900d,false,null));
    }
    @Test public void areaSearchRequiresRecordedArea(){
        assertEquals("incomplete",outcome(true,false,false,0,0,0,0,10000,false,null,true,null));
        assertEquals("complete",outcome(true,false,false,0,0,0,0,10000,false,null,true,250d));
    }
    @Test public void incompleteStationOutcomeAlwaysRemainsIncomplete(){
        assertEquals("incomplete",outcome(false,false,true,1,0,0,600,600000,false,null,false,null));
    }
}
