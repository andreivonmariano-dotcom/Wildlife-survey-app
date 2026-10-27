package org.phwildlife.survey;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Versioned measurements linked to a survey and individual station visit. */
public final class StationEnvironmentStore{
    private StationEnvironmentStore(){}
    private static String utc(long epoch){
        SimpleDateFormat date=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        return date.format(new Date(epoch));
    }
    private static boolean ids(String event,String visit){
        return event!=null&&!event.trim().isEmpty()&&visit!=null&&!visit.trim().isEmpty();
    }
    private static void ensureRow(SQLiteDatabase d,String event,String visit,String station){
        d.execSQL("INSERT OR IGNORE INTO station_environment("+
            "visit_id,event_id,station_id,updated_utc) VALUES(?,?,?,datetime('now'))",
            new Object[]{visit,event,station==null?"":station});
    }
    /** The initial fix is a snapshot: later GNSS updates cannot rewrite it. */
    public static void recordStationFix(SQLiteDatabase d,String event,String visit,
            String station,Location fix){
        if(!ids(event,visit))return;
        ensureRow(d,event,visit,station);
        GpsElevation.Sample elevation=GpsElevation.read(fix);
        // Keep raw ellipsoid height separate, preserving datum and validity.
        d.execSQL("UPDATE station_environment SET elevation_msl_m=?,"+
            "elevation_hae_m=?,vertical_uncertainty_m=?,elevation_datum=?,"+
            "elevation_source=?,fix_time_utc=?,updated_utc=datetime('now') "+
            "WHERE visit_id=? AND elevation_source='not_available'",
            new Object[]{elevation.mslMeters,elevation.ellipsoidMeters,
                elevation.verticalUncertaintyMeters,elevation.datum,
                elevation.status,elevation.fresh?utc(elevation.utcMs):null,visit});
    }
    /**
     * A GPS fix may first report ellipsoid height, then gain an MSL
     * conversion a few seconds later. Upgrade only the missing MSL fields.
     * An already usable station-start MSL sample is immutable.
     */
    public static void recordImprovedMsl(SQLiteDatabase d,String event,String visit,
            String station,Location latest){
        if(!ids(event,visit))return;
        GpsElevation.Sample sample=GpsElevation.read(latest);
        if(!sample.usableForHabitat)return;
        ensureRow(d,event,visit,station);
        d.execSQL("UPDATE station_environment SET elevation_msl_m=?,"+
                "vertical_uncertainty_m=?,elevation_datum='MSL',"+
                "elevation_source='later_verified_msl',fix_time_utc=?,"+
                "updated_utc=datetime('now') WHERE visit_id=? "+
                "AND elevation_source IN ('not_available','stale_or_unqualified',"+
                    "'no_altitude','ellipsoid_only','unverified_vertical_accuracy')",
            new Object[]{sample.mslMeters,sample.verticalUncertaintyMeters,
                utc(sample.utcMs),visit});
    }
    /** Manual MSL measurements are kept in distinct fields from the GNSS snapshot. */
    public static void recordManualElevation(SQLiteDatabase d,String event,String visit,
            String station,Double msl,Double uncertainty){
        if(!ids(event,visit))return;
        ensureRow(d,event,visit,station);
        d.execSQL("UPDATE station_environment SET manual_elevation_msl_m=?,"+
                "manual_vertical_uncertainty_m=?,updated_utc=datetime('now') "+
                "WHERE visit_id=?",
            new Object[]{msl,uncertainty,visit});
    }
    /** Never replace an earlier station GPS fix with manually entered elevation. */
    public static void recordHabitat(SQLiteDatabase d,String event,String visit,
            String station,String habitat){
        if(!ids(event,visit))return;
        ensureRow(d,event,visit,station);
        d.execSQL("UPDATE station_environment SET habitat_code=?,habitat_source=?,"+
            "updated_utc=datetime('now') WHERE visit_id=?",
            new Object[]{habitat==null||habitat.isEmpty()?null:habitat,
                habitat==null||habitat.isEmpty()?null:"observer_selected",visit});
    }
}
