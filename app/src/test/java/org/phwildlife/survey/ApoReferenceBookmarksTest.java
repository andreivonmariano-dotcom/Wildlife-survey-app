package org.phwildlife.survey;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ApoReferenceBookmarksTest {
    @Test public void citationKeySurvivesFormattingAndEditionRowOrder(){
        String reference="Zanaga et al. (2021). ESA WorldCover 10 m 2020 v100.";
        String same="  ZANAGA et al.  (2021). ESA WorldCover 10 m 2020 v100. ";
        assertEquals(ApoReferenceBookmarks.id(reference),
            ApoReferenceBookmarks.id(same));
        assertEquals(64,ApoReferenceBookmarks.id(reference).length());
        assertNotEquals(ApoReferenceBookmarks.id(reference),
            ApoReferenceBookmarks.id("Another paper, 2021"));
        assertEquals("",ApoReferenceBookmarks.id("   "));
    }
    @Test public void unicodeTitlesRemainStable(){
        assertEquals(ApoReferenceBookmarks.id("Davao – field survey"),
            ApoReferenceBookmarks.id("davao – FIELD SURVEY"));
    }
}
