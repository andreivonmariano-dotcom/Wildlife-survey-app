package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

/** Protocol summaries explain only already-recorded method records. */
public final class MethodCompletionReviewTest {
    @Test public void fieldWithoutPassesNeedsAttention(){
        MethodCompletionReview.Result r=MethodCompletionReview.assess(
            "area_search",new int[]{0,0,0},null);
        assertEquals(MethodCompletionReview.Kind.FIELD_EFFORT,r.kind);
        assertFalse(r.methodRecordsReady);
        assertTrue(r.detail.contains("Recorded passes: 0"));
        assertEquals("Review method effort",r.actionLabel);
        assertFalse(r.headline.contains("survey complete"));
    }
    @Test public void invalidAreaSearchPassesAreVisible(){
        MethodCompletionReview.Result r=MethodCompletionReview.assess(
            "area_search",new int[]{3,0,1},null);
        assertFalse(r.methodRecordsReady);
        assertTrue(r.detail.contains("Invalid or incomplete effort entries: 1"));
    }
    @Test public void coverboardNeedsInspectionsNotInventedDetections(){
        MethodCompletionReview.Result missing=MethodCompletionReview.assess(
            "coverboard_search",new int[]{5,2,0},null);
        assertFalse(missing.methodRecordsReady);
        assertTrue(missing.detail.contains("Uninspected boards: 2"));
        MethodCompletionReview.Result ready=MethodCompletionReview.assess(
            "coverboard_search",new int[]{5,0,0},null);
        assertTrue(ready.methodRecordsReady);
        assertTrue(ready.headline.contains("final checks"));
    }
    @Test public void cameraTrapDoesNotRequireRetrievalAtEveryCheck(){
        MethodCompletionReview.Result r=MethodCompletionReview.assess(
            "camera_trap_check",null,new int[]{3,0,3,0});
        assertEquals(MethodCompletionReview.Kind.DEVICES,r.kind);
        assertTrue(r.methodRecordsReady);
        assertTrue(r.detail.contains("Still deployed: 3"));
        assertTrue(r.detail.contains("retrieval is not required"));
    }
    @Test public void missingTrapChecksAndCaptureDetailsRemainOpen(){
        MethodCompletionReview.Result r=MethodCompletionReview.assess(
            "live_trap_check",null,new int[]{4,1,2,2});
        assertFalse(r.methodRecordsReady);
        assertTrue(r.detail.contains("Without a recorded check: 1"));
        assertTrue(r.detail.contains("Missing capture details: 2"));
    }
    @Test public void otherMethodsDoNotClaimCompletion(){
        for(String method:new String[]{"point_count","line_transect","ves_transect",
            "amphibian_calls","opportunistic","visual_encounter","bat_acoustic"}){
            MethodCompletionReview.Result r=MethodCompletionReview.assess(method,null,null);
            assertEquals(method,MethodCompletionReview.Kind.NONE,r.kind);
            assertFalse(method,r.methodRecordsReady);
            assertTrue(r.detail.contains("Recorded sightings alone do not"));
        }
    }
    @Test public void corruptedMethodCountsAreNotSilentlyAccepted(){
        try{
            MethodCompletionReview.assess("area_search",null,null);
            fail("Missing method counts must not silently pass");
        }catch(IllegalArgumentException expected){}
        try{
            MethodCompletionReview.assess("camera_trap_check",null,new int[]{1,0});
            fail("Missing device counts must not silently pass");
        }catch(IllegalArgumentException expected){}
    }
}
