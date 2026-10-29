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
        assertTrue(study.contains("Sutton, L. J."));
        assertTrue(study.contains("Bird Conservation International"));
        assertTrue(study.contains("https://doi.org/10.1017/S0959270926100586"));
    }

    @Test public void correctedFirstAuthorFromVerifiedDOI(){
        String url="https://doi.org/10.11609/jott.7261.14.3.20713-20720";
        assertEquals("(Reintar et al., 2022)",
            ResearchCitationFormatter.inText(
                "Warguez et al. (2022), Journal of Threatened Taxa",2022,url));
        String apa=ResearchCitationFormatter.sourceRecord(
            "Warguez et al. (2022), Journal of Threatened Taxa",2022,url);
        assertTrue(apa.startsWith("Reintar, A. R. T."));
        assertTrue(apa.contains("14(3), 20713–20720"));
        assertEquals(28,VerifiedApaCatalog.count());
    }

    @Test public void reviewedLiteratureDateAndSynthesisLabels(){
        assertEquals("(Ishihara et al., 2015)",ResearchCitationFormatter.inText(
            "Ishihara et al. (2015), Oryx 49(2):352–359",2015,
            "https://doi.org/10.1017/S0030605314000313"));
        assertEquals("(IUCN SSC Hornbill Specialist Group, n.d.)",
            ResearchCitationFormatter.inText(
                "IUCN SSC Hornbill Specialist Group",0,
                "https://iucnhornbills.org/rufous-headed-hornbill/"));
        assertTrue(ResearchCitationFormatter.sourceRecord(
            "Esselstyn et al. (2004)",2004,
            "https://www.biodiversitylibrary.org/part/49110")
            .contains("Esselstyn, J. A., Widmann, P., & Heaney, L. R."));
    }

    @Test public void datedInstitutionalNotices(){
        assertEquals("(DENR-PENRO Cotabato, 2025)",
            ResearchCitationFormatter.inText(
                "DENR-PENRO Cotabato (9 June 2025), Philippine crocodile rescue",2025));
    }
}
