package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class FieldLayoutPolicyTest {
    @Test public void narrowPhonesUseFullWidthTiles(){
        assertTrue(FieldLayoutPolicy.stackSurveyTiles(320,1f));
        assertTrue(FieldLayoutPolicy.stackSurveyTiles(411,1.5f));
        assertFalse(FieldLayoutPolicy.stackSurveyTiles(420,1f));
    }
    @Test public void portalPairsRespondToAvailableWidthAndTextScale(){
        assertTrue(FieldLayoutPolicy.stackPortalPair(320,1f));
        assertTrue(FieldLayoutPolicy.stackPortalPair(360,1f));
        assertFalse(FieldLayoutPolicy.stackPortalPair(390,1f));
        assertTrue(FieldLayoutPolicy.stackPortalPair(412,1.3f));
        assertFalse(FieldLayoutPolicy.stackPortalPair(412,1.0f));
        assertEquals(FieldLayoutPolicy.stackSurveyTiles(360,1f),
            FieldLayoutPolicy.stackPortalPair(360,1f));
    }
    @Test public void entryStacksOnlyWhenTooNarrow(){
        assertTrue(FieldLayoutPolicy.stackSpeciesEntry(320,1f));
        assertTrue(FieldLayoutPolicy.stackSpeciesEntry(393,1.8f));
        assertFalse(FieldLayoutPolicy.stackSpeciesEntry(393,1f));
    }
    @Test public void buttonTargetsScaleWithinBounds(){
        assertEquals(50,FieldLayoutPolicy.actionHeightDp(1f));
        assertTrue(FieldLayoutPolicy.actionHeightDp(2f)>=96);
        assertEquals(96,FieldLayoutPolicy.actionHeightDp(4f));
        assertEquals(56,FieldLayoutPolicy.saveHeightDp(1f));
        assertEquals(80,FieldLayoutPolicy.saveHeightDp(3f));
        assertEquals(80,FieldLayoutPolicy.saveHeightDp(1.6f));
        assertTrue(FieldLayoutPolicy.stackRecorderDock(320,2f));
        assertFalse(FieldLayoutPolicy.stackRecorderDock(360,1.6f));
        assertTrue(FieldLayoutPolicy.stackRecorderDock(360,2f));
        assertFalse(FieldLayoutPolicy.stackRecorderDock(412,2f));
    }
}