package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

/** End-to-end state scenarios evaluated against production field policies. */
public final class FieldWorkflowScenarioTest {
    private String close(boolean requestComplete,boolean cancel,
            boolean stationBased,int visits,int unfinished,int missed,
            int targetSeconds,long recordedEffortMs,
            boolean transect,boolean routeFinished,Double routeMeters,
            boolean area,Double searchedSquareMeters){
        return SurveyClosePolicy.resolve(requestComplete,cancel,stationBased,
            visits,unfinished,missed,targetSeconds,recordedEffortMs,
            transect,routeFinished,routeMeters,area,searchedSquareMeters);
    }

    @Test public void homeStartGpsBirdPointCountsAndComplete(){
        assertFalse(LikasUiRules.canContinue(false));
        assertTrue(GpsFixPolicy.acceptableElapsed(7.186,125.458,6.1f,2500L,false));
        assertTrue(LikasUiRules.canContinue(true));
        assertTrue(FieldUiPolicy.pointStationSetup("point_count"));
        // No species detection is needed to complete a valid zero-detection count.
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(true,false,true,
            2,0,1,600,1200000L,false,true,null,false,null));
        assertEquals(SurveyClosePolicy.COMPLETE,close(true,false,true,
            3,0,0,600,1800000L,false,true,null,false,null));
    }

    @Test public void staleAndMockGpsNeverPassSurveyStart(){
        assertFalse(GpsFixPolicy.acceptableElapsed(7.186,125.458,6f,10001L,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.186,125.458,6f,2000L,true));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.186,125.458,10f,2000L,false));
        assertFalse(GpsFixPolicy.acceptableElapsed(7.186,125.458,Float.NaN,2000L,false));
    }

    @Test public void incompleteMammalVisitStaysArchived(){
        assertTrue(FieldUiPolicy.mammalDetails("Mammals"));
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(true,false,true,
            1,1,0,0,360000L,false,true,null,false,null));
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(false,false,true,
            1,0,0,0,360000L,false,true,null,false,null));
        assertEquals(SurveyClosePolicy.COMPLETE,close(true,false,true,
            1,0,0,0,360000L,false,true,null,false,null));
    }

    @Test public void canceledReptileSurveyIsAlwaysNotCounted(){
        assertTrue(FieldUiPolicy.herpetofaunaDetails("Herpetofauna"));
        assertFalse(FieldUiPolicy.callIndex("Herpetofauna","Reptiles"));
        assertEquals(SurveyClosePolicy.NOT_COUNTED,close(true,true,true,
            0,2,5,600,0L,false,false,null,false,null));
    }

    @Test public void transectCompletionRequiresLengthAndConfirmation(){
        assertTrue(FieldUiPolicy.routeSetup("line_transect"));
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(true,false,false,
            0,0,0,900,900000L,true,false,1200d,false,null));
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(true,false,false,
            0,0,0,900,900000L,true,true,null,false,null));
        assertEquals(SurveyClosePolicy.COMPLETE,close(true,false,false,
            0,0,0,900,900000L,true,true,1200d,false,null));
    }

    @Test public void searchAreaCompletionNeedsActualArea(){
        assertEquals(SurveyClosePolicy.INCOMPLETE,close(true,false,false,
            0,0,0,0,0L,false,true,null,true,null));
        assertEquals(SurveyClosePolicy.COMPLETE,close(true,false,false,
            0,0,0,0,0L,false,true,null,true,125d));
    }
}
