package org.phwildlife.survey;

import java.util.Locale;

/** Predictable species-list ordering and screening-status filters. */
public final class SpeciesSortPolicy {
    private SpeciesSortPolicy(){}
    public static final int ALPHABETICAL=0,FAMILY=1,GENUS=2,SCORE_HIGH=3,SCORE_LOW=4;
    public static final String[] SORT_LABELS={
        "Name · A to Z","Family · A to Z","Genus · A to Z",
        "Screening score · high to low","Screening score · low to high"
    };
    public static final String[] FILTER_LABELS={
        "All scores","Likely","Unlikely","Insufficient Data"
    };
    public static String genus(String scientific){
        if(scientific==null)return "";
        String cleaned=scientific.trim();
        int index=cleaned.indexOf(' ');
        return (index<0?cleaned:cleaned.substring(0,index)).toLowerCase(Locale.ROOT);
    }
    private static String normalized(String value){
        return value==null?"":value.trim().toLowerCase(Locale.ROOT);
    }
    public static boolean keep(int filter,String label){
        switch(filter){
            case 1:return "Likely".equals(label);
            case 2:return "Unlikely".equals(label);
            case 3:return "Insufficient Data".equals(label);
            default:return true;
        }
    }
    public static int compare(int sort,String commonA,String scientificA,String familyA,
            Integer scoreA,String commonB,String scientificB,String familyB,Integer scoreB){
        int result=0;
        if(sort==SCORE_HIGH||sort==SCORE_LOW){
            // Unassessed scores always last, even in ascending order.
            if(scoreA==null&&scoreB!=null)return 1;
            if(scoreA!=null&&scoreB==null)return -1;
            if(scoreA!=null&&scoreB!=null)
                result=(sort==SCORE_HIGH?Integer.compare(scoreB,scoreA):
                    Integer.compare(scoreA,scoreB));
        }else if(sort==FAMILY){
            result=normalized(familyA).compareTo(normalized(familyB));
        }else if(sort==GENUS){
            result=genus(scientificA).compareTo(genus(scientificB));
        }
        if(result!=0)return result;
        String labelA=normalized(commonA).isEmpty()?scientificA:commonA;
        String labelB=normalized(commonB).isEmpty()?scientificB:commonB;
        result=normalized(labelA).compareTo(normalized(labelB));
        if(result!=0)return result;
        return normalized(scientificA).compareTo(normalized(scientificB));
    }
}
