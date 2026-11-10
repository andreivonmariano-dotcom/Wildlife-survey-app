package org.phwildlife.survey;
import static org.junit.Assert.*;
import org.junit.Test;
public final class GeoChecklistPolicyTest {
    @Test public void autoSelectOnlyUnambiguousKnownPolygon(){
        assertTrue(GeoChecklistPolicy.canAutoSelect("suggestion_only","MINDANAO"));
        assertTrue(GeoChecklistPolicy.canAutoSelect("suggestion_only","PALAWAN"));
        for(String uncertain:new String[]{"near_boundary_uncertain","overlapping_provinces",
            "outside_coverage","outside_admin_polygon","boundary_file_error",
            "island_assignment_uncertain"}){
            assertFalse(GeoChecklistPolicy.canAutoSelect(uncertain,"MINDANAO"));
        }
        assertFalse(GeoChecklistPolicy.canAutoSelect("suggestion_only","OTHER"));
        assertFalse(GeoChecklistPolicy.canAutoSelect("suggestion_only","PH"));
        assertFalse(GeoChecklistPolicy.canAutoSelect("suggestion_only","not-a-region"));
    }
    @Test public void autoLocationRequiresGenuineFreshSubTenFix(){
        assertTrue(GeoChecklistPolicy.acceptableLocation(6.74,125.36,4.7f,1200,false));
        assertFalse(GeoChecklistPolicy.acceptableLocation(6.74,125.36,10.0f,1200,false));
        assertFalse(GeoChecklistPolicy.acceptableLocation(6.74,125.36,4.7f,10001,false));
        assertFalse(GeoChecklistPolicy.acceptableLocation(6.74,125.36,4.7f,1200,true));
    }
}
