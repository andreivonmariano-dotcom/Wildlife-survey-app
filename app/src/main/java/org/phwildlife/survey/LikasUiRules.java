package org.phwildlife.survey;

/** Deterministic display state without Android dependencies. */
public final class LikasUiRules {
    private LikasUiRules(){}
    public static boolean isNight(int localHour){
        if(localHour<0||localHour>23)throw new IllegalArgumentException("hour 0..23");
        return localHour<6||localHour>=18;
    }
    public static boolean canContinue(boolean activeSurvey){return activeSurvey;}
    /** Clock time is local. Dawn and dusk gradually shift pixel forest colors. */
    public static float daylightFactor(int minute){
        if(minute<0||minute>=1440)throw new IllegalArgumentException("minute 0..1439");
        if(minute<300||minute>=1140)return 0f;
        if(minute<420)return smooth((minute-300)/120f);
        if(minute<=1050)return 1f;
        return 1f-smooth((minute-1050)/90f);
    }
    private static float smooth(float x){return x*x*(3f-2f*x);}

}
