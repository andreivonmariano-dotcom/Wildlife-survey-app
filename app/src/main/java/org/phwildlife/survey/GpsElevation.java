package org.phwildlife.survey;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import java.util.Locale;

/**
 * Native GNSS altitude with a recorded vertical datum. WGS84 ellipsoid
 * height is NEVER substituted for orthometric / mean sea level elevation.
 */
public final class GpsElevation {
    private GpsElevation(){}
    public static final long MAX_FIX_AGE_MS=10000L;
    public static final float MAX_VERTICAL_UNCERTAINTY_M=50f;
    public static final class Sample{
        public final Double mslMeters,ellipsoidMeters,verticalUncertaintyMeters;
        public final long ageMs,utcMs;
        public final boolean fresh,usableForHabitat;
        public final String datum,status;
        Sample(Double msl,Double hae,Double uncertainty,long age,long utc,
                boolean fresh,boolean usable,String datum,String status){
            mslMeters=msl;ellipsoidMeters=hae;verticalUncertaintyMeters=uncertainty;
            ageMs=age;utcMs=utc;this.fresh=fresh;
            usableForHabitat=usable;this.datum=datum;this.status=status;
        }
        public String label(){
            if(!fresh)return "GPS elevation: waiting for fresh fix";
            if(mslMeters!=null)
                return String.format(Locale.US,"GPS elevation: %.0f m MSL%s%s",
                    mslMeters,verticalUncertaintyMeters==null?"":String.format(
                        Locale.US," ±%.0f m",verticalUncertaintyMeters),
                    usableForHabitat?"":" · not scored");
            if(ellipsoidMeters!=null)
                return String.format(Locale.US,
                    "GPS height: %.0f m above WGS84 ellipsoid · not scored",
                    ellipsoidMeters);
            return "GPS altitude unavailable · not scored";
        }
    }
    /** Pure policy used by the Android reader and regression tests. */
    public static boolean validForHabitat(boolean fresh,boolean mslAvailable,
            Double verticalUncertainty){
        return fresh&&mslAvailable&&verticalUncertainty!=null&&
            Double.isFinite(verticalUncertainty)&&verticalUncertainty>=0.0&&
            verticalUncertainty<=MAX_VERTICAL_UNCERTAINTY_M;
    }
    public static Sample read(Location fix){
        if(fix==null)return new Sample(null,null,null,-1,0,false,false,"none","missing");
        long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
        boolean mocked=Build.VERSION.SDK_INT>=31?fix.isMock():fix.isFromMockProvider();
        boolean fresh=LocationManager.GPS_PROVIDER.equals(fix.getProvider())&&!mocked&&
            fix.hasAccuracy()&&fix.getAccuracy()<10f&&
            age>=0&&age<=MAX_FIX_AGE_MS;
        if(!fresh)return new Sample(null,null,null,age,fix.getTime(),false,false,
            "none","stale_or_unqualified");
        Double hae=fix.hasAltitude()&&Double.isFinite(fix.getAltitude())?
            fix.getAltitude():null;
        Double vertical=null;
        if(Build.VERSION.SDK_INT>=26&&fix.hasVerticalAccuracy()&&
                Float.isFinite(fix.getVerticalAccuracyMeters())){
            vertical=(double)fix.getVerticalAccuracyMeters();
        }
        Double msl=null;
        if(Build.VERSION.SDK_INT>=34&&fix.hasMslAltitude()&&
                Double.isFinite(fix.getMslAltitudeMeters())){
            msl=fix.getMslAltitudeMeters();
            // The vertical uncertainty for MSL is distinct from horizontal accuracy.
            vertical=fix.hasMslAltitudeAccuracy()?
                (double)fix.getMslAltitudeAccuracyMeters():null;
        }
        boolean canScore=validForHabitat(true,msl!=null,vertical);
        return new Sample(msl,hae,vertical,age,fix.getTime(),true,canScore,
            msl!=null?"MSL":hae!=null?"WGS84_ELLIPSOID":"none",
            canScore?"usable_msl":msl!=null?"unverified_vertical_accuracy":
                hae!=null?"ellipsoid_only":"no_altitude");
    }
}
