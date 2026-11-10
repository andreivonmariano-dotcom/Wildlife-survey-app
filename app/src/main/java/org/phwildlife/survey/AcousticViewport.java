package org.phwildlife.survey;

/** Calibrated screen geometry, not a species-classification result. */
public final class AcousticViewport {
    public static final double MIN_HZ=85d,MAX_HZ=17000d;
    private AcousticViewport(){}
    public static double frequencyAt(float yFraction){
        double normalized=Math.max(0d,Math.min(1d,yFraction));
        return MAX_HZ*Math.pow(MIN_HZ/MAX_HZ,normalized);
    }
    public static long timeAt(float xFraction,long durationMs,long startMs,long endMs){
        float x=Math.max(0f,Math.min(1f,xFraction));
        return Math.min(durationMs,Math.max(0L,startMs+Math.round((endMs-startMs)*x)));
    }
    public static long[] window(long durationMs,long centerMs,int zoom){
        long duration=Math.max(1,durationMs);
        int level=Math.max(1,Math.min(16,zoom));
        long width=Math.max(1000,duration/level);
        width=Math.min(duration,width);
        long start=Math.max(0,Math.min(duration-width,centerMs-width/2));
        return new long[]{start,start+width};
    }
}
