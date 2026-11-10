package org.phwildlife.survey;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;
import java.util.Map;

/** Private reference bootstrap parser and packaging contract tests. */
public final class BundledPrivateReferencesTest {
    @Test public void parsesExactNumberOfQuotedRows(){
        String[] cols={"taxon_key","taxon_group","common_name","scientific_name",
            "family","source","source_version","region_codes","review_status",
            "order_name","subfamily","subgenus","authority","authority_year",
            "distribution_text","distribution_status","ph_red_list",
            "iucn_red_list","cites","synonym_text","taxonomic_note",
            "source_url","ebird_url","sound_url","habitat_text","elevation_text"};
        StringBuilder csv=new StringBuilder(String.join(",",cols)).append("\n");
        for(int i=0;i<714;i++){
            String[] row=new String[cols.length];
            java.util.Arrays.fill(row,"");
            row[0]="LIKAS725:test_"+i;
            row[1]="Birds";
            row[2]="\"Bird, \"\"test\"\"\"";
            row[3]="Test name "+i;
            row[4]="Testidae";
            row[5]="Source";
            row[6]="PAIC714_2026-11-01";
            row[8]="unreviewed";
            row[9]="Passeriformes";
            row[24]="Forest";
            row[25]="\"0–2,000 m\"";
            csv.append(String.join(",",row)).append("\r\n");
        }
        List<Map<String,String>> records=BundledPrivateReferences.parse(csv.toString());
        assertEquals(714,records.size());
        assertEquals("Bird, \"test\"",records.get(0).get("common_name"));
        assertEquals("Test name 713",records.get(713).get("scientific_name"));
    }
    @Test public void rejectsTruncatedAndMalformedSources(){
        try{
            BundledPrivateReferences.parse("taxon_key,scientific_name\nA,B\n");
            fail("Accepted missing records");
        }catch(IllegalArgumentException expected){}
        try{
            BundledPrivateReferences.parse("\"unclosed");
            fail("Accepted unclosed quote");
        }catch(IllegalArgumentException expected){}
    }
}