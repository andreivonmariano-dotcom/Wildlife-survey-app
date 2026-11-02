package org.phwildlife.survey;

/** Context-sensitive navigation for nested field survey records. */
public final class SurveyNavigationPolicy {
    private SurveyNavigationPolicy(){}

    /** A picker owns its selected record only if its caller supplied no event. */
    public static boolean backToSurveySelector(String launchedEventId,String selectedEventId){
        return launchedEventId==null && selectedEventId!=null;
    }

    /** Recorder review returns to the recorder; history-owned detail returns to the list. */
    public static boolean closeHistoryToCaller(boolean fromRecorder,String selectedEventId){
        return fromRecorder || selectedEventId==null;
    }
}
