package org.phwildlife.survey;

/** Completeness of recorded field protocols, independent of zero detections. */
public final class FieldProtocolPolicy {
    private FieldProtocolPolicy(){}
    public static boolean supported(String method){
        return "coverboard_search".equals(method)||"area_search".equals(method)||
            "track_sign".equals(method);
    }
    public static boolean canComplete(String method,int units,int missingChecks,int invalidEfforts){
        if(!supported(method))return true;
        if("coverboard_search".equals(method))
            return units>0&&missingChecks==0;
        return units>0&&invalidEfforts==0;
    }
    public static boolean validInterval(long startMs,long endMs){
        return startMs>0&&endMs>startMs;
    }
    public static boolean validArea(double squareMeters){
        return Double.isFinite(squareMeters)&&squareMeters>0;
    }
    public static boolean validDistance(double meters){
        return Double.isFinite(meters)&&meters>0;
    }
}
