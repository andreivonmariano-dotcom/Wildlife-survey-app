package org.phwildlife.survey;

/** Selection rules for GPS-assisted checklist scopes. No inferred species occurrence. */
public final class GeoChecklistPolicy {
    private GeoChecklistPolicy(){}
    /** Only one enclosing, unambiguous province polygon can choose an island scope. */
    public static boolean canAutoSelect(String boundaryStatus,String region){
        if(!"suggestion_only".equals(boundaryStatus))return false;
        if(region==null||region.equals("PH")||region.equals("OTHER"))return false;
        for(String available:GeoRegions.IDS)
            if(region.equals(available))return true;
        return false;
    }
    public static boolean acceptableLocation(double lat,double lon,float accuracy,long ageMs,
            boolean mock){
        return GpsFixPolicy.acceptableElapsed(lat,lon,accuracy,ageMs,mock);
    }
}
