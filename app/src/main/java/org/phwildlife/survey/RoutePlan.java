package org.phwildlife.survey;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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
        if(!Double.isFinite(lengthM)||!Double.isFinite(spacingM)||lengthM<0||spacingM<=0)
            throw new IllegalArgumentException("Invalid route length or spacing");
        return (int)Math.floor((lengthM+1e-7)/spacingM)+1;
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
        return l!=null&&l.hasAccuracy()&&l.getAccuracy()>0&&l.getAccuracy()<=75f&&
            now-l.getTime()>=0&&now-l.getTime()<=30000&&
            !l.isFromMockProvider()&&l.getLatitude()>=-90&&l.getLatitude()<=90&&
            l.getLongitude()>=-180&&l.getLongitude()<=180;
    }
    public static void start(SQLiteDatabase db,String event,Next n,String pointId,int targetSeconds,long startedAt,Location gps){
        boolean hasFix=acceptable(gps,startedAt);
        String time=utc(startedAt);
        String quality=hasFix?"gps_fix_at_station_start":"missing_or_weak_gps_at_start";
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
