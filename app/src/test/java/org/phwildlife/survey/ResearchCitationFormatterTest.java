package org.phwildlife.survey;

import static org.junit.Assert.*;
import org.junit.Test;

/** Readable author-date citations must not invent missing research metadata. */
public final class ResearchCitationFormatterTest {
    @Test public void reviewedAuthorYearAndOrganization(){
        assertEquals("(Sutton et al., 2026)",
            ResearchCitationFormatter.inText(
                "Sutton et al. (2026), Bird Conservation International 36:e33",2026));
        assertEquals("(Miñarro et al., 2025)",
            ResearchCitationFormatter.inText(
                "Miñarro et al. 2025, PeerJ e18694",0));
        assertEquals("(Brown et al., 1997)",
            ResearchCitationFormatter.inText(
                "Brown, Brown & Alcala (1997), Proc. California Acad. Sci.",0));
        assertEquals("(Gaulke & Curio, 2001)",
            ResearchCitationFormatter.inText(
                "Gaulke & Curio (2001), Spixiana",0));
        assertEquals("(Bat Conservation International, n.d.)",
            ResearchCitationFormatter.inText(
                "Bat Conservation International, Acerodon jubatus species account",0));
    }

    @Test public void noInventedYearAndOnlyOneEndingPeriod(){
        String fact=ResearchCitationFormatter.citeClaim(
            "Roosts in trees.","Bat Conservation International",0);
        assertEquals("Roosts in trees (Bat Conservation International, n.d.).",fact);
        assertEquals("n.d.",ResearchCitationFormatter.yearLabel(
            0,"BirdForum Opus, field reference"));
    }

    @Test public void missingMetadataRemainExplicit(){
        String bci=ResearchCitationFormatter.sourceRecord(
            "Bat Conservation International, Acerodon jubatus species account",
            0,"https://www.batcon.org/");
        assertTrue(bci.contains("Bat Conservation International. (n.d.)."));
        assertTrue(bci.contains("Acerodon jubatus species account"));
        assertFalse(bci.contains("2026"));
        assertTrue(bci.contains("https://www.batcon.org/"));
        String study=ResearchCitationFormatter.sourceRecord(
            "Sutton et al. (2026), Bird Conservation International 36:e33",
            2026,"https://doi.org/10.1017/S0959270926100586");
        assertFalse(study.contains("et al.."));
        assertTrue(study.contains("Sutton et al. (2026)."));
    }

    @Test public void datedInstitutionalNotices(){
        assertEquals("(DENR-PENRO Cotabato, 2025)",
            ResearchCitationFormatter.inText(
                "DENR-PENRO Cotabato (9 June 2025), Philippine crocodile rescue",2025));
    }
}
