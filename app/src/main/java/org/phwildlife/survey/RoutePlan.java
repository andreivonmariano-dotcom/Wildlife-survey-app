package org.phwildlife.survey;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Design offsets are route CHAINAGES in meters. No route geometry is fabricated.
 * Coordinates are assigned from a real GPS fix only at visit start.
 */
public final class RoutePlan {
    private RoutePlan(){}
    private static String utc(long time){
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date(time));
    }
    public static int count(double lengthM,double spacingM){
        return SurveyMath.stations(lengthM,spacingM);
    }
    public static String newSiteId(){
        return "STN-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.US);
    }
    /** Caller must hold a transaction. The surrounding survey inserts atomically. */
    public static int create(SQLiteDatabase d,String event,String project,double lengthM,double spacingM,long time){
        int n=count(lengthM,spacingM);
        if(n<2||n>201)throw new IllegalArgumentException("Survey design requires 2–201 points");
        String date=utc(time);
        for(int i=0;i<n;i++){
            double offset=i*spacingM;
            String id=newSiteId();
            String name=String.format(Locale.US,"Point %02d · %.0f m",i+1,offset);
            d.execSQL("INSERT INTO sites(id,project_id,name,notes,created_at,updated_at) VALUES(?,?,?,?,?,?)",
                new Object[]{id,project,name,"Auto-generated planned station. Coordinate recorded on field arrival. Survey: "+event,date,date});
            d.execSQL("INSERT INTO survey_stations(event_id,station_id,sequence_number,planned_chainage_m,visit_state) VALUES(?,?,?,?,?)",
                new Object[]{event,id,i+1,offset,"planned"});
        }
        return n;
    }
    /** Create a durable route template using the survey's existing, immutable station IDs. */
    public static String saveTemplate(SQLiteDatabase d,String event,String project,
            String name,double lengthM,double spacingM,long now){
        if(name==null||name.trim().length()<3||name.trim().length()>100)
            throw new IllegalArgumentException("Route template name must be 3–100 characters");
        String id="RTE-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.US);
        int stations=plannedCount(d,event);
        if(stations<2||stations!=count(lengthM,spacingM))
            throw new IllegalStateException("Route station count does not match design");
        d.execSQL("INSERT INTO route_templates(id,project_id,name,method,planned_length_m,"+
            "station_spacing_m,station_count,created_at_utc) VALUES(?, ?, ?, 'point_count', ?, ?, ?, ?)",
            new Object[]{id,project,name.trim(),lengthM,spacingM,stations,utc(now)});
        d.execSQL("INSERT INTO route_template_stations(template_id,station_id,sequence_number,chainage_m) "+
            "SELECT ?,station_id,sequence_number,planned_chainage_m FROM survey_stations "+
            "WHERE event_id=? ORDER BY sequence_number",new Object[]{id,event});
        d.execSQL("UPDATE surveys SET route_template_id=? WHERE id=?",new Object[]{id,event});
        return id;
    }
    /**
     * Revisit a saved route using the exact station IDs from prior fieldwork.
     * Fresh GPS is sampled independently during each visit, and the site coordinate
     * remains available for comparison. This method never duplicates site rows.
     */
    public static int reuseTemplate(SQLiteDatabase d,String template,String project,String event,
            double expectedLength,double expectedSpacing){
        int count=0;
        try(Cursor c=d.rawQuery(
            "SELECT station_id,sequence_number,chainage_m,planned_length_m,station_spacing_m,"+
            "station_count,project_id FROM route_template_stations ts "+
            "JOIN route_templates rt ON rt.id=ts.template_id "+
            "WHERE ts.template_id=? ORDER BY sequence_number",
            new String[]{template})){
            while(c.moveToNext()){
                if(!project.equals(c.getString(6))||
                   Math.abs(expectedLength-c.getDouble(3))>0.001||
                   Math.abs(expectedSpacing-c.getDouble(4))>0.001||
                   count(c.getDouble(3),c.getDouble(4))!=c.getInt(5))
                    throw new IllegalArgumentException("Saved route does not match the survey design");
                String stationId=c.getString(0);
                try(Cursor site=d.rawQuery("SELECT 1 FROM sites WHERE id=? AND project_id=?",
                    new String[]{stationId,project})){
                    if(!site.moveToFirst())throw new IllegalStateException("Template station is missing or belongs to another project");
                }
                int seq=c.getInt(1);
                if(seq!=count+1)throw new IllegalStateException("Template station ordering is inconsistent");
                d.execSQL("INSERT INTO survey_stations(event_id,station_id,sequence_number,"+
                    "planned_chainage_m,visit_state) VALUES(?,?,?,?,'planned')",
                    new Object[]{event,stationId,seq,c.getDouble(2)});
                count++;
            }
        }
        if(count<2||count!=count(expectedLength,expectedSpacing))
            throw new IllegalStateException("Saved route contains an unexpected number of stations");
        d.execSQL("UPDATE surveys SET route_template_id=? WHERE id=?",new Object[]{template,event});
        return count;
    }
    public static final class Next {
        public final String stationId;
        public final int sequence;
        public final double chainage;
        Next(String stationId,int sequence,double chainage){
            this.stationId=stationId;this.sequence=sequence;this.chainage=chainage;
        }
    }
    public static Next next(SQLiteDatabase d,String event){
        try(Cursor c=d.rawQuery("SELECT station_id,sequence_number,planned_chainage_m "+
            "FROM survey_stations WHERE event_id=? AND visit_state='planned' "+
            "ORDER BY sequence_number LIMIT 1",new String[]{event})){
            return c.moveToFirst()?new Next(c.getString(0),c.getInt(1),c.getDouble(2)):null;
        }
    }
    public static int plannedCount(SQLiteDatabase d,String event){
        try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM survey_stations WHERE event_id=?",
            new String[]{event})){return c.moveToFirst()?c.getInt(0):0;}
    }
    public static int recordedCount(SQLiteDatabase d,String event){
        try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM survey_stations WHERE event_id=? AND visit_state<>'planned'",
            new String[]{event})){return c.moveToFirst()?c.getInt(0):0;}
    }
    public static boolean acceptable(Location l,long now){
        if(l==null||!LocationManager.GPS_PROVIDER.equals(l.getProvider())||!l.hasAccuracy())return false;
        long age=(android.os.SystemClock.elapsedRealtimeNanos()-l.getElapsedRealtimeNanos())/1000000L;
        return GpsFixPolicy.acceptableElapsed(l.getLatitude(),l.getLongitude(),
            l.getAccuracy(),age,l.isFromMockProvider());
    }
    public static void start(SQLiteDatabase db,String event,Next n,String pointId,int targetSeconds,long startedAt,Location gps){
        boolean hasFix=acceptable(gps,startedAt);
        String time=utc(startedAt);
        String quality=hasFix?"verified_station_fix_under_10m":"missing_or_weak_gps_at_start";
        Object lat=hasFix?gps.getLatitude():null,lon=hasFix?gps.getLongitude():null,
            acc=hasFix?gps.getAccuracy():null;
        db.beginTransaction();
        try{
            db.execSQL("INSERT INTO point_counts(id,event_id,station_id,sequence_number,started,target_seconds,"+
                "planned_chainage_m,gps_latitude,gps_longitude,gps_accuracy_m,gps_recorded_at,gps_status) "+
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{pointId,event,n.stationId,n.sequence,time,targetSeconds,n.chainage,
                    lat,lon,acc,hasFix?utc(gps.getTime()):null,quality});
            db.execSQL("UPDATE survey_stations SET visit_state='started',captured_at=?,gps_latitude=?,"+
                "gps_longitude=?,gps_accuracy_m=?,gps_status=? WHERE event_id=? AND sequence_number=?",
                new Object[]{time,lat,lon,acc,quality,event,n.sequence});
            if(hasFix){
                db.execSQL("UPDATE sites SET latitude=?,longitude=?,coordinate_uncertainty_m=?,updated_at=? "+
                    "WHERE id=? AND latitude IS NULL AND longitude IS NULL",
                    new Object[]{lat,lon,acc,time,n.stationId});
            }
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public static void close(SQLiteDatabase d,String event,String stationId,boolean completed){
        d.execSQL("UPDATE survey_stations SET visit_state=? WHERE event_id=? AND station_id=?",
            new Object[]{completed?"completed":"incomplete",event,stationId});
    }
}
