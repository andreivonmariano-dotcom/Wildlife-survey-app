package org.phwildlife.survey;

import org.json.JSONObject;
import java.util.List;
import java.util.Locale;

/**
 * A navigation index over literal words in the unchanged Book 01 narratives.
 * Matches are retrieval aids, NOT verified habitat occupancy, habitat selection,
 * local occurrence, or reviewed species-habitat edges.
 */
public final class PhLibraryHabitatFilter {
    private PhLibraryHabitatFilter(){}

    public static final String[] IDS={
        "", "lowland-forest", "montane-forest", "mossy-forest", "limestone-karst",
        "ultramafic-forest", "mangrove", "freshwater-wetland", "riparian",
        "disturbed-secondary", "tropical-subalpine", "beach-forest",
        "freshwater-swamp-forest", "peat-swamp-forest",
        "semi-evergreen-rainforest", "moist-deciduous-forest"
    };
    public static final String[] NAMES={
        "Any habitat", "Lowland tropical forest", "Montane forest",
        "Mossy / upper montane forest", "Limestone and karst forest",
        "Ultramafic / ultrabasic forest", "Mangrove forest and tidal woodland",
        "Freshwater marsh and wetland", "Riparian forest and stream margin",
        "Secondary forest and disturbed mosaic", "Tropical subalpine rainforest",
        "Beach forest / coastal strand woodland", "Freshwater swamp forest",
        "Peat swamp forest", "Tropical semi-evergreen rainforest",
        "Tropical moist deciduous forest"
    };
    public static final String[] SHORT_NAMES={
        "", "Lowland", "Montane", "Mossy forest", "Karst", "Ultramafic",
        "Mangrove", "Wetland", "Riparian", "Secondary forest", "Subalpine",
        "Beach forest", "Swamp forest", "Peat swamp", "Semi-evergreen",
        "Deciduous"
    };
    private static final String[][] TERMS={
        {}, {"lowland","lower-elevation forest","low-elevation forest"},
        {"montane","mountain forest","upland forest"},
        {"mossy","cloud forest","cloud-forest","upper montane"},
        {"limestone","karst"},
        {"ultramafic","ultrabasic","serpentine soil","serpentine substrate"},
        {"mangrove","tidal forest","tidal woodland"},
        {"wetland","marsh","marshland"},
        {"riparian","streamside","stream-side","riverine forest","riverbank forest","stream bank"},
        {"secondary forest","secondary growth","second-growth","regenerating forest",
            "disturbed forest","disturbed woodland"},
        {"subalpine"},
        {"beach forest","coastal forest","strand forest","coastal strand","backshore woodland"},
        {"freshwater swamp","swamp forest","forested swamp","waterlogged forest"},
        {"peat swamp","peatland forest","peat-swamp"},
        {"semi-evergreen","semievergreen"},
        {"deciduous forest","moist deciduous","seasonally deciduous forest"}
    };
    public static int indexOfId(String id){
        if(id==null)return 0;
        for(int i=1;i<IDS.length;i++)if(IDS[i].equals(id))return i;
        return 0;
    }
    public static boolean mentions(String accountText,int index){
        if(index<=0)return true;
        if(index>=TERMS.length||accountText==null)return false;
        String text=accountText.toLowerCase(Locale.ROOT);
        for(String term:TERMS[index])if(text.contains(term))return true;
        // Educationally related vegetation and elevation descriptions can
        // suggest species for formations rarely named word-for-word in Book 01.
        // These suggestions exist only in browsing, never in survey observations.
        if(index==10)
            return text.contains("mossy")||text.contains("upper montane")||
                text.contains("high-elevation")||text.contains("summit");
        if(index==14)
            return (text.contains("lowland")||text.contains("evergreen"))&&
                (text.contains("forest")||text.contains("woodland"))&&
                (text.contains("seasonal")||text.contains("evergreen"));
        if(index==15)
            return (text.contains("woodland")||text.contains("lowland"))&&
                (text.contains("seasonal")||text.contains("dry season")||
                 text.contains("dry woodland"));
        return false;
    }
    public static boolean mentions(JSONObject record,int index){
        return record!=null && mentions(record.optString("p"),index);
    }
    public static int count(List<JSONObject> records,int index){
        int n=0;
        if(records!=null)for(JSONObject record:records)
            if(mentions(record,index))n++;
        return n;
    }
}
