package org.phwildlife.survey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Canonical habitat vocabulary from the owner's 714-species PAIC display checklist. */
public final class PaicHabitatPolicy {
    private PaicHabitatPolicy() {}
    public static final String[] HABITATS={
        "Forest","Freshwater wetlands","Agricultural areas",
        "Coastal and intertidal areas","Rivers and streams",
        "Grassland and open country","Shrubland","Urban and disturbed areas",
        "Caves and karst","Marine waters","Riverbanks and shorelines",
        "Montane habitats","Forest edge","Woodland","Cliffs and rocky areas",
        "Trees and roost sites"
    };
    private static final Pattern RANGE=Pattern.compile(
        "^([0-9][0-9,]*)\\s*[–-]\\s*([0-9][0-9,]*)\\s*m$",Pattern.CASE_INSENSITIVE);
    private static final Pattern POINT=Pattern.compile(
        "^([0-9][0-9,]*)\\s*m$",Pattern.CASE_INSENSITIVE);
    public static boolean isKnown(String name){
        for(String habitat:HABITATS)if(habitat.equals(name))return true;
        return false;
    }
    public static String encode(Set<String> chosen){
        StringBuilder out=new StringBuilder();
        for(String name:HABITATS){
            if(chosen.contains(name)){
                if(out.length()>0)out.append("|");
                out.append(name);
            }
        }
        return out.toString();
    }
    public static Set<String> decode(String serialized){
        HashSet<String> answer=new HashSet<>();
        if(serialized==null||serialized.trim().isEmpty())return answer;
        for(String item:serialized.split("\\|"))if(isKnown(item))answer.add(item);
        return answer;
    }
    public static int habitatMatches(String sourceText,Set<String> chosen){
        if(sourceText==null||chosen==null||chosen.isEmpty())return 0;
        HashSet<String> reference=new HashSet<>();
        for(String part:sourceText.split(",\\s*"))
            reference.add(part.trim().toLowerCase(Locale.ROOT));
        int matches=0;
        for(String item:chosen)
            if(reference.contains(item.toLowerCase(Locale.ROOT)))matches++;
        return matches;
    }
    /** Unknown ranges return null. Coordinates are not used to invent an elevation. */
    public static Boolean elevationWithin(String guide,Double mslMeters){
        if(guide==null||mslMeters==null||!Double.isFinite(mslMeters))return null;
        String value=guide.trim();
        Matcher range=RANGE.matcher(value);
        try{
            if(range.matches()){
                double low=Double.parseDouble(range.group(1).replace(",",""));
                double high=Double.parseDouble(range.group(2).replace(",",""));
                return mslMeters>=low&&mslMeters<=high;
            }
            // A single elevation (especially marine "0 m") is not a
            // species range. Do not manufacture a +/-100 m ecological band.
            Matcher point=POINT.matcher(value);
            if(point.matches())return null;
        }catch(NumberFormatException ignored){}
        return null;
    }
    /** Advisory rank only. Never a probability, occurrence or exclusion criterion. */
    public static int ranking(String habitatGuide,String elevationGuide,
            Set<String> chosen,Double mslMeters){
        int matched=habitatMatches(habitatGuide,chosen);
        Boolean elevation=elevationWithin(elevationGuide,mslMeters);
        return matched*10+(Boolean.TRUE.equals(elevation)?1:0);
    }
    /**
     * Use only the owner's named PAIC habitat guide categories. This is a
     * source-label shortlist, not an absence claim or ecological prediction.
     * 2: directly named habitat, 1: broader compatible guide category,
     * 0: different named category, -1: no usable habitat entry.
     */
    public static int siteHabitatRelevance(String habitatGuide,String observedCode){
        if(observedCode==null||observedCode.isEmpty()||
                habitatGuide==null||habitatGuide.trim().isEmpty()||
                habitatGuide.trim().equals("—"))return -1;
        Set<String> entries=new HashSet<>();
        for(String part:habitatGuide.split(",\\s*"))
            entries.add(part.trim().toLowerCase(Locale.ROOT));
        String exact="";
        String broad="";
        switch(observedCode){
            case "montane": exact="Montane habitats";broad="Forest";break;
            case "mossy": broad="Montane habitats|Forest";break;
            case "lowland": broad="Forest";break;
            case "secondary": broad="Forest|Forest edge";break;
            case "grassland": exact="Grassland and open country";break;
            case "agriculture": exact="Agricultural areas";break;
            case "wetland": exact="Freshwater wetlands";break;
            case "riparian": exact="Rivers and streams";broad="Riverbanks and shorelines";break;
            case "mangrove": broad="Coastal and intertidal areas";break;
            case "urban": exact="Urban and disturbed areas";break;
            default:return -1;
        }
        if(!exact.isEmpty()&&entries.contains(exact.toLowerCase(Locale.ROOT)))return 2;
        if(!broad.isEmpty())for(String candidate:broad.split("\\|"))
            if(entries.contains(candidate.toLowerCase(Locale.ROOT)))return 1;
        return 0;
    }

    /** WorldCover classes are land cover, not ground-verified habitat. */
    public static String landCoverSuggestion(int code){
        switch(code){
            case 10:return "Forest"; // Tree cover, which may include plantation trees
            case 20:return "Shrubland";
            case 30:return "Grassland and open country";
            case 40:return "Agricultural areas";
            case 50:return "Urban and disturbed areas";
            case 90:return "Freshwater wetlands"; // candidate only; salinity unverified
            case 95:return "Coastal and intertidal areas"; // mapped mangroves
            default:return ""; // open water, bare ground, etc. cannot map reliably
        }
    }
}
