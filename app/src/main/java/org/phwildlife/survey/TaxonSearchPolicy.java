package org.phwildlife.survey;

import java.util.Locale;

/**
 * Safely constructs literal SQLite LIKE queries for offline taxon names.
 * Prepared statements always bind the pattern. Escaping wildcards keeps
 * a researcher's underscore and percent signs from expanding the search.
 */
public final class TaxonSearchPolicy {
    private TaxonSearchPolicy(){}
    public static final int MAX_SUGGESTIONS=60;
    public static final int MIN_CONTAINS_LENGTH=2;
    public static String escapeLike(String term){
        if(term==null)return "";
        return term.replace("!","!!").replace("%","!%").replace("_","!_");
    }
    public static String prefix(String term){
        return escapeLike(trim(term))+"%";
    }
    public static String contains(String term){
        return "%"+escapeLike(trim(term))+"%";
    }
    public static String trim(String term){
        return term==null?"":term.trim().toLowerCase(Locale.ROOT);
    }
    public static boolean hasDocumentedRegion(boolean requested,String geographicRegion){
        return requested&&geographicRegion!=null&&
            !geographicRegion.equals("PH")&&!geographicRegion.equals("OTHER");
    }
}
