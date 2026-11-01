package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

/** Source-guide matching never supplies inferred or confirmed occurrence. */
public final class PaicHabitatPolicyTest {
    @Test public void supportsAllSixteenChecklistHabitats(){
        assertEquals(16,PaicHabitatPolicy.HABITATS.length);
        assertTrue(PaicHabitatPolicy.isKnown("Rivers and streams"));
        assertFalse(PaicHabitatPolicy.isKnown("Primary forest"));
    }
    @Test public void multiHabitatMatchingIgnoresDisplayCase(){
        Set<String> chosen=new HashSet<>();
        chosen.add("Forest");chosen.add("Rivers and streams");
        assertEquals(2,PaicHabitatPolicy.habitatMatches(
            "Forest, rivers and streams",chosen));
        assertEquals(0,PaicHabitatPolicy.habitatMatches(
            "Coastal and intertidal areas",chosen));
        assertTrue(PaicHabitatPolicy.decode(PaicHabitatPolicy.encode(chosen))
            .containsAll(chosen));
    }
    @Test public void unknownElevationNeverBecomesZero(){
        assertNull(PaicHabitatPolicy.elevationWithin("",0d));
        assertNull(PaicHabitatPolicy.elevationWithin("Unknown",500d));
        assertNull(PaicHabitatPolicy.elevationWithin("0–2,000 m",null));
        assertEquals(Boolean.TRUE,PaicHabitatPolicy.elevationWithin("0–2,000 m",900d));
        assertEquals(Boolean.FALSE,PaicHabitatPolicy.elevationWithin("0–2,000 m",3000d));
    }
    @Test public void bareLandAndOpenWaterDoNotBecomeHabitats(){
        assertEquals("",PaicHabitatPolicy.landCoverSuggestion(60));
        assertEquals("",PaicHabitatPolicy.landCoverSuggestion(80));
        assertEquals("Coastal and intertidal areas",
            PaicHabitatPolicy.landCoverSuggestion(95));
    }
    @Test public void advisoryRankHasNoAbsenceOrProbability(){
        Set<String> chosen=new HashSet<>();
        chosen.add("Forest");chosen.add("Rivers and streams");
        assertEquals(21,PaicHabitatPolicy.ranking("Forest, rivers and streams",
            "0–1,000 m",chosen,500d));
        assertEquals(0,PaicHabitatPolicy.ranking("Agricultural areas",
            "1,500–2,000 m",chosen,500d));
        assertEquals(0,PaicHabitatPolicy.ranking(null,null,chosen,null));
    }
}
