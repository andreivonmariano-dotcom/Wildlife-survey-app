package org.phwildlife.survey;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import org.json.JSONArray;

/** Raw ecological data exports, not fitted or cleaned model datasets. */
public final class ResearchExports {
    private ResearchExports(){}
    private static String esc(String x){return x==null?"":"\""+x.replace("\"","\"\"")+"\"";}
    private static String num(Cursor c,int n){return c.isNull(n)?"":c.getString(n);}
    private static void row(ZipOutputStream z,String... c)throws Exception{
        StringBuilder s=new StringBuilder();for(int i=0;i<c.length;i++){if(i>0)s.append(',');s.append(esc(c[i]));}
        z.write((s+"\n").getBytes(StandardCharsets.UTF_8));
    }
    public static void write(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        pointUnits(z,d);surveyVisits(z,d);sdm(z,d);herps(z,d);mammals(z,d);geojson(z,d);
    }
    private static void pointUnits(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("point_counts.csv"));
        row(z,"point_count_id","event_id","station_id","sequence","start_utc","end_utc","target_seconds","completed","notes");
        try(Cursor c=d.rawQuery("SELECT id,event_id,station_id,sequence_number,started,ended,target_seconds,completed,notes FROM point_counts ORDER BY event_id,sequence_number",null)){
            while(c.moveToNext()){String[] a=new String[9];for(int i=0;i<9;i++)a[i]=num(c,i);row(z,a);}
        }
        z.closeEntry();
    }
    private static void surveyVisits(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("occupancy_visits.csv"));
        row(z,"sampling_unit_id","session_event_id","station_id","start_utc","end_utc","taxon_profile","method","completed","observed_identification","detected","notes");
        // Presence-only detections and actual visit denominators are exported separately.
        // Blank detection is UNKNOWN, not non-detection for an arbitrary species.
        try(Cursor c=d.rawQuery("SELECT p.id,p.event_id,p.station_id,p.started,p.ended,'Birds','point_count',p.completed,p.notes FROM point_counts p ORDER BY p.started",null)){
            while(c.moveToNext())row(z,num(c,0),num(c,1),num(c,2),num(c,3),num(c,4),"Birds","point_count",num(c,7),"","","Sample unit; no species-level absence inferred");
        }
        try(Cursor c=d.rawQuery("SELECT u.id,u.event_id,u.station_id,u.started,u.ended,s.survey_profile,u.method,u.completed "+
            "FROM sampling_units u JOIN surveys s ON s.id=u.event_id ORDER BY u.started",null)){
            while(c.moveToNext())row(z,num(c,0),num(c,1),num(c,2),num(c,3),num(c,4),num(c,5),num(c,6),num(c,7),"","","Unit visit; no species-level absence inferred");
        }
        try(Cursor c=d.rawQuery("SELECT id,COALESCE(station_id,''),started,ended,COALESCE(survey_profile,''),method,complete "+
            "FROM surveys WHERE method<>'point_count' AND id NOT IN (SELECT event_id FROM sampling_units) ORDER BY started",null)){
            while(c.moveToNext())row(z,num(c,0),num(c,0),num(c,1),num(c,2),num(c,3),num(c,4),num(c,5),num(c,6),"","","Event denominator; no species-level absence inferred");
        }
        try(Cursor c=d.rawQuery("SELECT r.event_id,COALESCE(r.point_count_id,r.sampling_unit_id,r.event_id),COALESCE(p.station_id,u.station_id,s.station_id,''),r.detected_at,r.taxon_group,s.method,r.raw_identification "+
            "FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id LEFT JOIN point_counts p ON p.id=r.point_count_id "+
            "LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id "+
            "WHERE r.deleted_at IS NULL ORDER BY r.detected_at",null)){
            while(c.moveToNext())row(z,num(c,1),num(c,0),num(c,2),num(c,3),"",num(c,4),num(c,5),"",num(c,6),"1","Positive record, identification unverified");
        }
        z.closeEntry();
    }
    private static void sdm(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("sdm_occurrences.csv"));
        row(z,"record_id","event_id","point_count_id","sampling_unit_id","taxon_group","identification","latitude","longitude","coordinate_accuracy_m","coordinate_role","detected_at_utc","detection_method","survey_method","station_id","candidate_presence");
        try(Cursor c=d.rawQuery("SELECT r.id,r.event_id,r.point_count_id,r.sampling_unit_id,r.taxon_group,r.raw_identification,r.observer_lat,r.observer_lon,r.gps_accuracy_m,r.detected_at,r.detection_method,s.method,COALESCE(p.station_id,u.station_id,s.station_id,'') "+
            "FROM field_records r JOIN surveys s ON s.id=r.event_id LEFT JOIN point_counts p ON p.id=r.point_count_id "+
            "LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id "+
            "WHERE r.deleted_at IS NULL ORDER BY r.id",null)){
            while(c.moveToNext()){
                row(z,num(c,0),num(c,1),num(c,2),num(c,3),num(c,4),num(c,5),num(c,6),num(c,7),num(c,8),
                    "observer_GPS_not_exact_animal_location",num(c,9),num(c,10),num(c,11),num(c,12),
                    c.isNull(6)||c.isNull(7)?"missing_geocoordinates":"candidate_presence_only");
            }
        }
        z.closeEntry();
    }
    private static void herps(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("herpetofauna_observations.csv"));
        row(z,"record_id","event_id","sampling_unit_id","taxon_group","taxon","number","detection_method","substrate","microhabitat","air_temp_c","humidity_percent","weather","calling_index_0_3","noise_index_0_4","life_stage","time_utc","station_id","method","time_status");
        try(Cursor c=d.rawQuery("SELECT r.id,r.event_id,r.sampling_unit_id,r.taxon_group,r.raw_identification,r.individual_count,r.detection_method,r.substrate,r.microhabitat,r.air_temp_c,r.humidity_pct,r.weather_notes,r.amphibian_call_score,r.call_noise_score,r.life_stage,r.detected_at,COALESCE(u.station_id,s.station_id),s.method,r.unit_time_status FROM field_records r "+
            "JOIN surveys s ON s.id=r.event_id LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id "+
            "WHERE r.deleted_at IS NULL AND s.survey_profile='Herpetofauna' ORDER BY r.id",null)){
            while(c.moveToNext()){String[] a=new String[19];for(int i=0;i<19;i++)a[i]=num(c,i);row(z,a);}
        }
        z.closeEntry();
    }
    private static void mammals(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("mammal_observations.csv"));
        row(z,"record_id","event_id","sampling_unit_id","station_id","identification","count",
            "evidence_type","detection_method","equipment_id","height_m","timestamp_utc","method","time_status");
        try(Cursor c=d.rawQuery(
            "SELECT r.id,r.event_id,r.sampling_unit_id,COALESCE(u.station_id,s.station_id),"+
            "r.raw_identification,r.individual_count,r.evidence_type,r.detection_method,"+
            "r.equipment_id,r.perch_height_m,r.detected_at,s.method,r.unit_time_status "+
            "FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id "+
            "WHERE r.deleted_at IS NULL AND s.survey_profile='Mammals' ORDER BY r.id",null)){
            while(c.moveToNext()){
                String[] values=new String[13];
                for(int i=0;i<13;i++)values[i]=num(c,i);
                row(z,values);
            }
        }
        z.closeEntry();
    }
    private static void geojson(ZipOutputStream z,SQLiteDatabase d)throws Exception{
        z.putNextEntry(new ZipEntry("transect_tracks.geojson"));
        JSONObject fc=new JSONObject();fc.put("type","FeatureCollection");
        JSONArray features=new JSONArray();
        try(Cursor events=d.rawQuery("SELECT id,COALESCE(gps_track_m,0),COALESCE(gps_track_status,'unknown'),method FROM surveys "+
            "WHERE method IN ('line_transect','ves_transect') ORDER BY started",null)){
            while(events.moveToNext()){
                JSONArray coords=new JSONArray();String id=events.getString(0);
                try(Cursor points=d.rawQuery("SELECT longitude,latitude FROM track_points WHERE event_id=? AND accepted=1 ORDER BY id",new String[]{id})){
                    while(points.moveToNext()){
                        JSONArray point=new JSONArray();point.put(points.getDouble(0));point.put(points.getDouble(1));coords.put(point);
                    }
                }
                if(coords.length()<2)continue;
                JSONObject f=new JSONObject(),geom=new JSONObject(),props=new JSONObject();
                geom.put("type","LineString");geom.put("coordinates",coords);
                props.put("event_id",id);props.put("gps_path_m_estimate",events.getDouble(1));
                props.put("track_state",events.getString(2));props.put("survey_method",events.getString(3));
                props.put("note","Filtered observer GPS path; not a surveyed-transect effort measurement");
                f.put("type","Feature");f.put("geometry",geom);f.put("properties",props);features.put(f);
            }
        }
        fc.put("features",features);
        z.write(fc.toString().getBytes(StandardCharsets.UTF_8));z.closeEntry();
    }
}
