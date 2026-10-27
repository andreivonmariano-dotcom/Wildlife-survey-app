package org.phwildlife.survey;

/** Survey-start GPS decision. A missing location never becomes an estimated fix. */
public final class SurveyStartGpsPolicy {
    public static final String VERIFIED="verified_start_fix_under_10m";
    public static final String MISSING="missing_at_survey_start";
    private SurveyStartGpsPolicy(){}

    /**
     * Returns null only when GPS is required but not verified. A deliberate
     * observer skip is recorded separately from an accepted GNSS reading.
     */
    public static String classify(boolean observerSkipped,double latitude,double longitude,
            float accuracyMeters,long ageMs,boolean mockLocation){
        if(observerSkipped)return MISSING;
        if(GpsFixPolicy.acceptableElapsed(latitude,longitude,accuracyMeters,ageMs,mockLocation))
            return VERIFIED;
        return null;
    }
}
