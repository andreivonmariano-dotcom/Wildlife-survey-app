package org.phwildlife.survey;

/**
 * Research survey disposition independent of UI wording.
 * Cancelled surveys are retained for audit but excluded from eligible analyses.
 * Incomplete visits and unmeasured route length cannot be called complete.
 */
public final class SurveyClosePolicy {
    private SurveyClosePolicy(){}
    public static final String COMPLETE="complete";
    public static final String INCOMPLETE="incomplete";
    public static final String NOT_COUNTED="not_counted";
    public static boolean routeLengthRequired(String method){
        return "line_transect".equals(method)||"ves_transect".equals(method);
    }
    public static boolean searchedAreaRequired(String method){
        return "area_search".equals(method);
    }
    public static String resolve(boolean requestedComplete,boolean cancelled,
            boolean stationBased,int visits,int unfinishedVisits,int missedPlannedStations,
            int targetSeconds,long measuredActiveEffortMs,
            boolean routeLengthRequired,boolean plannedRouteFinished,Double measuredRouteM,
            boolean searchedAreaRequired,Double measuredAreaM2){
        if(cancelled)return NOT_COUNTED;
        if(!requestedComplete)return INCOMPLETE;
        if(stationBased&&(visits<1||unfinishedVisits>0||missedPlannedStations>0))
            return INCOMPLETE;
        if(!stationBased&&targetSeconds>0&&
            measuredActiveEffortMs<(long)targetSeconds*1000L)
            return INCOMPLETE;
        if(routeLengthRequired&&(!plannedRouteFinished||measuredRouteM==null||
            !Double.isFinite(measuredRouteM)||measuredRouteM<=0d))
            return INCOMPLETE;
        if(searchedAreaRequired&&(measuredAreaM2==null||
            !Double.isFinite(measuredAreaM2)||measuredAreaM2<=0d))
            return INCOMPLETE;
        return COMPLETE;
    }
}
