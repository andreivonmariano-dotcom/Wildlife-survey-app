package org.phwildlife.survey;
import org.junit.Test;
import static org.junit.Assert.*;
public final class TaxonSearchPolicyTest {
    @Test public void specialSqlCharactersAreLiteral(){
        assertEquals("platymantis!_x!%!!",TaxonSearchPolicy.escapeLike("platymantis_x%!"));
        assertEquals("!%test%",TaxonSearchPolicy.prefix("%TEST"));
        assertEquals("%!_frog%",TaxonSearchPolicy.contains("_FROG"));
    }
    @Test public void textInputTrimmedAndNormalized(){
        assertEquals("platymantis",TaxonSearchPolicy.trim(" Platymantis "));
        assertEquals("bird%",TaxonSearchPolicy.prefix(" BIRD "));
    }
    @Test public void geographicFilterRequiresActualRegion(){
        assertFalse(TaxonSearchPolicy.hasDocumentedRegion(true,"PH"));
        assertFalse(TaxonSearchPolicy.hasDocumentedRegion(true,"OTHER"));
        assertFalse(TaxonSearchPolicy.hasDocumentedRegion(false,"MINDANAO"));
        assertTrue(TaxonSearchPolicy.hasDocumentedRegion(true,"MINDANAO"));
    }
    @Test public void boundedResults(){
        assertEquals(60,TaxonSearchPolicy.MAX_SUGGESTIONS);
    }
}