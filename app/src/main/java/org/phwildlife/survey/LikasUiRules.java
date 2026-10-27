package org.phwildlife.survey;

/** Deterministic display state without Android dependencies. */
public final class LikasUiRules {
    private LikasUiRules(){}
    public static boolean isNight(int localHour){
        if(localHour<0||localHour>23)throw new IllegalArgumentException("hour 0..23");
        return localHour<6||localHour>=18;
    }
    public static boolean canContinue(boolean activeSurvey){return activeSurvey;}
}
