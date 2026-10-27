package org.phwildlife.survey;

/** Pure Java route arithmetic shared by setup, database planning and unit tests. */
public final class SurveyMath{
    private SurveyMath(){}
    public static int stations(double lengthM,double spacingM){
        if(!Double.isFinite(lengthM)||!Double.isFinite(spacingM)||
            lengthM<0||spacingM<=0)
            throw new IllegalArgumentException("Invalid route length or spacing");
        double count=Math.floor((lengthM+1e-7)/spacingM)+1;
        if(count>Integer.MAX_VALUE)throw new IllegalArgumentException("Too many stations");
        return (int)count;
    }
    public static double chainage(int zeroBasedIndex,double spacingM){
        if(zeroBasedIndex<0||!Double.isFinite(spacingM)||spacingM<=0)
            throw new IllegalArgumentException("Invalid point index or spacing");
        return zeroBasedIndex*spacingM;
    }
}
