package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;

public final class FieldQuickActionsTest {
    @Test public void birdsHaveDirectCallLibrary(){
        assertEquals(FieldQuickActions.Kind.CALLS,
            FieldQuickActions.forSurvey("Birds","point_count"));
        assertEquals("Record / review calls",
            FieldQuickActions.label(FieldQuickActions.Kind.CALLS));
    }
    @Test public void amphibianCallingIsDirect(){
        assertEquals(FieldQuickActions.Kind.CALLS,
            FieldQuickActions.forSurvey("Herpetofauna","amphibian_calls"));
    }
    @Test public void reptileVesHasQuickPhoto(){
        assertEquals(FieldQuickActions.Kind.PHOTO,
            FieldQuickActions.forSurvey("Herpetofauna","visual_encounter"));
    }
    @Test public void mammalTrapAndSignMethodsHaveOwnEvidence(){
        assertEquals(FieldQuickActions.Kind.DEVICES,
            FieldQuickActions.forSurvey("Mammals","camera_trap_check"));
        assertEquals(FieldQuickActions.Kind.DEVICES,
            FieldQuickActions.forSurvey("Mammals","live_trap_check"));
        assertEquals(FieldQuickActions.Kind.METHOD_EVIDENCE,
            FieldQuickActions.forSurvey("Mammals","track_sign"));
    }
    @Test public void unsupportedBasicLogDoesNotPretendToRecordCalls(){
        assertEquals(FieldQuickActions.Kind.NONE,
            FieldQuickActions.forSurvey("Mammals","bat_acoustic"));
        assertEquals(FieldQuickActions.Kind.NONE,
            FieldQuickActions.forSurvey(null,"point_count"));
    }
}