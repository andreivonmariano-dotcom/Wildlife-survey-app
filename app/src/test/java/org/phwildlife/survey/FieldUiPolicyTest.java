package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;

public final class FieldUiPolicyTest {
    @Test public void showsOnlyOneWildlifeProfile(){
        for(String selected:new String[]{"Birds","Herpetofauna","Mammals"}){
            int active=(FieldUiPolicy.birdDetails(selected)?1:0)
                +(FieldUiPolicy.herpetofaunaDetails(selected)?1:0)
                +(FieldUiPolicy.mammalDetails(selected)?1:0);
            assertEquals(selected,1,active);
        }
    }
    @Test public void reptileVisitNeverShowsAmphibianCallFields(){
        assertFalse(FieldUiPolicy.callIndex("Herpetofauna","Reptiles"));
        assertFalse(FieldUiPolicy.listeningNoise("Herpetofauna","Reptiles","amphibian_calls"));
        assertTrue(FieldUiPolicy.callIndex("Herpetofauna","Amphibians"));
        assertTrue(FieldUiPolicy.listeningNoise("Herpetofauna","Amphibians","amphibian_calls"));
        assertFalse(FieldUiPolicy.listeningNoise("Herpetofauna","Amphibians","visual_encounter"));
        assertFalse(FieldUiPolicy.callIndex("Birds","Amphibians"));
    }
    @Test public void stationRouteControlsAreMethodSpecific(){
        assertTrue(FieldUiPolicy.routeSetup("point_count"));
        assertTrue(FieldUiPolicy.pointStationSetup("point_count"));
        assertTrue(FieldUiPolicy.routeSetup("line_transect"));
        assertTrue(FieldUiPolicy.routeSetup("ves_transect"));
        assertFalse(FieldUiPolicy.pointStationSetup("line_transect"));
        assertFalse(FieldUiPolicy.routeSetup("camera_trap_check"));
        assertFalse(FieldUiPolicy.routeSetup("opportunistic"));
        assertFalse(FieldUiPolicy.pointStationSetup("visual_encounter"));
    }
}
