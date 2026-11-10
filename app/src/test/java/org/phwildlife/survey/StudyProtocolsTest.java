package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class StudyProtocolsTest {
    @Test public void untouchedSurveysKeepExistingRules(){
        assertTrue(StudyProtocols.passes(null,0,0,0,false,0));
    }
    @Test public void minimumRequirementsAreAdditive(){
        StudyProtocols.Rule p=new StudyProtocols.Rule("Forest frogs",3,30,4,true,true);
        assertFalse(StudyProtocols.passes(p,2,40*60000L,4,true,2));
        assertFalse(StudyProtocols.passes(p,3,29*60000L,4,true,2));
        assertFalse(StudyProtocols.passes(p,3,40*60000L,3,true,2));
        assertFalse(StudyProtocols.passes(p,3,40*60000L,4,false,2));
        assertFalse(StudyProtocols.passes(p,3,40*60000L,4,true,0));
        assertTrue(StudyProtocols.passes(p,3,40*60000L,4,true,2));
    }
}
