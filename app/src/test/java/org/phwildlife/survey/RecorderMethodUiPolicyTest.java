package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

/** The display policy must not modify biological evidence or field protocols. */
public final class RecorderMethodUiPolicyTest {
    @Test public void distanceFirstDetectionOnlyForStructuredBirdRoutes(){
        assertTrue(RecorderMethodUiPolicy.firstDetectionProminent("Birds","point_count"));
        assertTrue(RecorderMethodUiPolicy.firstDetectionProminent("Birds","line_transect"));
        assertFalse(RecorderMethodUiPolicy.firstDetectionProminent("Birds","opportunistic"));
        assertFalse(RecorderMethodUiPolicy.firstDetectionProminent("Mammals","line_transect"));
        assertFalse(RecorderMethodUiPolicy.firstDetectionProminent("Birds","area_search"));
    }
    @Test public void amphibianListeningPrioritizesCallsAndLeavesMicrohabitatAvailable(){
        assertTrue(RecorderMethodUiPolicy.listeningProminent("Herpetofauna","amphibian_calls"));
        assertFalse(RecorderMethodUiPolicy.microhabitatProminent("Herpetofauna","amphibian_calls"));
        assertTrue(RecorderMethodUiPolicy.microhabitatProminent("Herpetofauna","visual_encounter"));
        assertTrue(RecorderMethodUiPolicy.microhabitatProminent("Herpetofauna","ves_transect"));
        assertFalse(RecorderMethodUiPolicy.listeningProminent("Herpetofauna","drift_fence_check"));
        assertFalse(RecorderMethodUiPolicy.listeningProminent("Birds","amphibian_calls"));
    }
    @Test public void deviceIdentityEmphasizedForCameraAndLiveTrapOnly(){
        assertTrue(RecorderMethodUiPolicy.deviceIdProminent("Mammals","camera_trap_check"));
        assertTrue(RecorderMethodUiPolicy.deviceIdProminent("Mammals","live_trap_check"));
        assertFalse(RecorderMethodUiPolicy.deviceIdProminent("Mammals","track_sign"));
        assertFalse(RecorderMethodUiPolicy.deviceIdProminent("Herpetofauna","drift_fence_check"));
        assertEquals(RecorderMethodUiPolicy.Action.DEVICES,
            RecorderMethodUiPolicy.action("Herpetofauna","drift_fence_check"));
        assertEquals(RecorderMethodUiPolicy.Action.AUDIO,
            RecorderMethodUiPolicy.action("Herpetofauna","amphibian_calls"));
        assertEquals(RecorderMethodUiPolicy.Action.FIELD_EFFORT,
            RecorderMethodUiPolicy.action("Birds","area_search"));
        assertEquals(RecorderMethodUiPolicy.Action.NONE,
            RecorderMethodUiPolicy.action("Mammals","bat_acoustic"));
    }
    @Test public void everyRegisteredMethodHasSpecificNonEmptyFocus(){
        for(String profile:new String[]{"Birds","Herpetofauna","Mammals"})
            for(SurveyMethods.Method m:SurveyMethods.forProfile(profile)){
                String focus=RecorderMethodUiPolicy.focus(profile,m.code);
                assertTrue(profile+" "+m.code,focus.length()>18);
                assertFalse(focus.contains("null"));
                if(m.code.equals("opportunistic"))assertTrue(focus.contains("Incidental"));
                else assertFalse("No generic fallback for published methods",
                    focus.startsWith("Field observation"));
            }
        assertTrue(RecorderMethodUiPolicy.focus("Mammals","bat_acoustic")
            .contains("basic detection log only"));
        assertEquals("Record / review calls",
            RecorderMethodUiPolicy.actionLabel("Herpetofauna","amphibian_calls"));
    }
}
