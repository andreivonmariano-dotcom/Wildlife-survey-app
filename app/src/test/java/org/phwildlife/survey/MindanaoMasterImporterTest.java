package org.phwildlife.survey;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class MindanaoMasterImporterTest {
    private List<Map<String,String>> fixture(){
        ArrayList<Map<String,String>> entries=new ArrayList<>();
        String[] groups={"Birds","Amphibians","Reptiles","Mammals"};
        int[] counts={408,53,154,99};
        int id=0;
        for(int g=0;g<groups.length;g++){
            for(int n=0;n<counts[g];n++){
                id++;
                String name="Testus species"+id;
                Map<String,String> row=new HashMap<>();
                row.put("taxon_key","LIKAS725:testus_species"+id);
                row.put("taxon_group",groups[g]);
                row.put("scientific_name",name);
                row.put("common_name","Taxon "+id);
                row.put("family","Testidae");
                row.put("order_name","Testiformes");
                row.put("source","Private LIKAS reference");
                row.put("source_version","PAIC714_2026-11-01");
                row.put("review_status","unreviewed");
                row.put("habitat_text","Forest");
                row.put("elevation_text","0–100 m");
                entries.add(row);
            }
        }
        assertEquals(714,entries.size());
        return entries;
    }

    @Test public void acceptsCompleteEditionAndExpectedGroupCounts(){
        MindanaoMasterImporter.validate(fixture());
    }
    @Test public void rejectsUnverifiedSourceEdition(){
        List<Map<String,String>> data=fixture();
        data.get(0).put("source_version","unknown");
        rejects(data);
    }
    @Test public void rejectsDuplicatedTaxonConcept(){
        List<Map<String,String>> data=fixture();
        data.get(1).put("scientific_name",data.get(0).get("scientific_name"));
        data.get(1).put("taxon_key",data.get(0).get("taxon_key"));
        rejects(data);
    }
    @Test public void rejectsMissingHabitat(){
        List<Map<String,String>> data=fixture();
        data.get(0).put("habitat_text","");
        rejects(data);
    }
    @Test public void rejectsTruncatedChecklist(){
        List<Map<String,String>> data=fixture();
        data.remove(0);
        rejects(data);
    }
    @Test public void rejectsIncorrectWildlifeGroupCount(){
        List<Map<String,String>> data=fixture();
        data.get(0).put("taxon_group","Reptiles");
        rejects(data);
    }
    @Test public void rejectsUnapprovedRosterEvenWithCorrectGroupTotals(){
        try{
            MindanaoMasterImporter.validateApprovedRoster(fixture());
            fail("A synthetic roster cannot activate as the owner master");
        }catch(IllegalArgumentException expected){ }
    }
    @Test public void rejectsAnyOfTheElevenOmissions(){
        List<Map<String,String>> data=fixture();
        data.get(0).put("scientific_name","Apus affinis");
        data.get(0).put("taxon_key","LIKAS725:apus_affinis");
        rejects(data);
    }
    private static void rejects(List<Map<String,String>> rows){
        try{
            MindanaoMasterImporter.validate(rows);
            fail("Expected invalid master to be rejected");
        }catch(IllegalArgumentException expected){
            // Validation aborts before any SQLite write.
        }
    }
}
