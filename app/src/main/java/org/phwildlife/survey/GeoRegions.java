package org.phwildlife.survey;
import java.util.*;

/**
 * Geographic preference is MANUALLY selected. This is NOT a GPS point-in-polygon
 * assignment. A province, administrative region, island and faunal region differ.
 *
 * Regional links below are sparse source-checked EXAMPLES, not complete checklists.
 * Missing links mean "not yet evaluated", never "species absent".
 */
public final class GeoRegions {
  private GeoRegions(){}
  public static final String DATASET="field-curated-region-evidence-v1-2026-10";
  public static final String BOUNDARY_METHOD="manual_region_choice_no_polygon";
  public static final String[] IDS={"PH","MINDANAO","VISAYAS","LUZON","PALAWAN","MINDORO","SULU","OTHER"};
  public static final String[] LABELS={
    "All Philippines / no regional filter",
    "Mindanao (main island and local field checklist)",
    "Visayas (island group)",
    "Luzon (main island and nearby islands)",
    "Palawan (island group)",
    "Mindoro (island)",
    "Sulu archipelago (separate from Mindanao)",
    "Outside listed groups / location uncertain"
  };
  public static int index(String id){
    for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;return 0;
  }
  public static String idAt(int pos){return IDS[Math.max(0,Math.min(IDS.length-1,pos))];}
  public static String label(String id){return LABELS[index(id)];}
  private static final Map<String,Map<String,Set<String>>> evidence=new HashMap<>();
  private static void record(String group,String area,String... names){
    Map<String,Set<String>> regions=evidence.get(group);
    if(regions==null){regions=new HashMap<>();evidence.put(group,regions);}
    Set<String> set=regions.get(area);
    if(set==null){set=new HashSet<>();regions.put(area,set);}
    for(String name:names)set.add(name.toLowerCase(Locale.US));
  }
  static{
    // Bird records from Avibase Mindanao (2026), source https://avibase.bsc-eoc.org/checklist.jsp?region=PHmn
    record("Birds","MINDANAO",
      "Pithecophaga jefferyi","Nisaetus pinskeri","Penelopides affinis",
      "Otus mirus","Goodfellowia miranda","Gallicolumba crinigera",
      "Micromacronus sordidus","Turdus nigrorum","Spilornis holospilus",
      "Macronus striaticeps","Ptilocichla mindanensis","Hypsipetes philippinus");
    // Distinct island representative examples. Full review of native Philippine
    // ranges and synonymy is required before expanding this regional set.
    record("Birds","LUZON","Penelopides manillae","Gallicolumba luzonica");
    record("Birds","VISAYAS","Penelopides panini","Orthotomus castaneiceps");
    record("Birds","PALAWAN","Anthracoceros marchei");
    record("Birds","MINDORO","Turdus mindorensis");
    // Reptile Database and 2026 Agusan Marsh survey (Hydrosaurus pustulatus).
    // https://reptile-database.reptarium.cz/Hydrosaurus/pustulatus
    // https://pmc.ncbi.nlm.nih.gov/articles/PMC12993478/
    record("Reptiles","MINDANAO","Hydrosaurus pustulatus");
    record("Reptiles","LUZON","Hydrosaurus pustulatus");
    record("Reptiles","VISAYAS","Hydrosaurus pustulatus");
    record("Reptiles","MINDORO","Hydrosaurus pustulatus");
    // IUCN SSC WPSG detailed species profiles, distribution by faunal regions.
    // https://www.iucn-wpsg.org/visayan-warty-pig-detailed
    // https://www.iucn-wpsg.org/philippine-warty-pig-detailed
    record("Mammals","VISAYAS","Sus cebifrons","Sus philippensis");
    record("Mammals","MINDANAO","Sus philippensis","Carlito syrichta");
    record("Mammals","LUZON","Sus philippensis");
    // No amphibian island list is treated as verified at present.
  }
  public static boolean hasPositiveEvidence(String group,String region,String scientificName){
    if(scientificName==null||region==null||region.equals("PH"))return false;
    Map<String,Set<String>> byGroup=evidence.get(group);
    if(byGroup==null)return false;
    Set<String> regionNames=byGroup.get(region);
    return regionNames!=null&&regionNames.contains(scientificName.trim().toLowerCase(Locale.US));
  }
  public static String evidenceSource(String group,String region,String name){
    if(!hasPositiveEvidence(group,region,name))return "";
    if(group.equals("Birds"))return "Avibase Mindanao 2026 / literature-checked starter list";
    if(group.equals("Reptiles"))return "Reptile Database and published Mindanao herpetofaunal records";
    if(group.equals("Mammals"))return "IUCN SSC pig profiles / peer-reviewed tarsier records";
    return "";
  }
}
