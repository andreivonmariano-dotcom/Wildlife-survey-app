package org.phwildlife.survey;

import android.location.Location;
import android.os.Build;

/**
 * An elevation snapshot from the same fresh GPS fix used for survey coordinates.
 * GPS ellipsoid height is not interchangeable with mean-sea-level elevation.
 */
public final class ElevationFix {
    private ElevationFix(){}

    // A very poor vertical estimate must not be presented as measured elevation.
    public static final float MAX_VERTICAL_ACCURACY_M=100f;

    public static final class Reading {
        public final Double meters;
        public final Double verticalAccuracyM;
        public final String datum;
        public final String source;
        public final String status;
        public final Long fixTimeMs;

        private Reading(Double meters,Double verticalAccuracyM,String datum,
                        String source,String status,Long fixTimeMs){
            this.meters=meters;
            this.verticalAccuracyM=verticalAccuracyM;
            this.datum=datum;
            this.source=source;
            this.status=status;
            this.fixTimeMs=fixTimeMs;
        }
    }

    public static Reading fromGps(Location fix,long now){
        if(!RoutePlan.acceptable(fix,now))
            return new Reading(null,null,null,"gps","no_fresh_gps_fix",null);

        Double height=null,accuracy=null;
        String datum=null;
        if(Build.VERSION.SDK_INT>=34&&fix.hasMslAltitude()){
            height=fix.getMslAltitudeMeters();
            datum="mean_sea_level";
            if(fix.hasMslAltitudeAccuracy())
                accuracy=(double)fix.getMslAltitudeAccuracyMeters();
        }else if(fix.hasAltitude()){
            height=fix.getAltitude();
            datum="wgs84_ellipsoid";
            if(Build.VERSION.SDK_INT>=26&&fix.hasVerticalAccuracy())
                accuracy=(double)fix.getVerticalAccuracyMeters();
        }

        if(height==null||!Double.isFinite(height))
            return new Reading(null,accuracy,datum,"gps","altitude_unavailable",fix.getTime());
        if(accuracy!=null&&(!Double.isFinite(accuracy)||accuracy<0))
            accuracy=null;
        if(accuracy!=null&&accuracy>MAX_VERTICAL_ACCURACY_M)
            return new Reading(null,accuracy,datum,"gps","poor_vertical_accuracy",fix.getTime());

        return new Reading(height,accuracy,datum,"gps",
            accuracy==null?"vertical_accuracy_unknown":"accepted",fix.getTime());
    }

    /** Manual edits apply to the next observation, not the station GPS snapshot. */
    public static Reading manual(double meters){
        if(!Double.isFinite(meters)||meters< -500||meters>9000)
            throw new IllegalArgumentException("Check manual elevation (-500 to 9000 m)");
        return new Reading(meters,null,"unspecified","manual","manual_override",null);
    }
}
