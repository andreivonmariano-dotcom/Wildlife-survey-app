package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Research export of observed effort and evidence; no inference from blank entries. */
public final class FieldProtocolEvidence {
    private FieldProtocolEvidence(){}
    private static String escape(String value){
        return "\""+(value==null?"":value.replace("\"","\"\""))+"\"";
    }
    private static void csv(ZipOutputStream archive,SQLiteDatabase db,
            String file,String query)throws Exception{
        try(Cursor c=db.rawQuery(query,null)){
            archive.putNextEntry(new ZipEntry(file));
            StringBuilder header=new StringBuilder();
            for(int i=0;i<c.getColumnCount();i++){
                if(i>0)header.append(',');
                header.append(escape(c.getColumnName(i)));
            }
            archive.write((header+"\n").getBytes(StandardCharsets.UTF_8));
            while(c.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int i=0;i<c.getColumnCount();i++){
                    if(i>0)line.append(',');
                    line.append(escape(c.isNull(i)?"":c.getString(i)));
                }
                archive.write((line+"\n").getBytes(StandardCharsets.UTF_8));
            }
            archive.closeEntry();
        }
    }
    public static void writeZip(ZipOutputStream archive,SQLiteDatabase db)throws Exception{
        csv(archive,db,"coverboard_units.csv",
            "SELECT id,event_id,station_id,board_id,installed_at_utc,"+
            "board_material,habitat,setup_notes "+
            "FROM coverboard_units ORDER BY event_id,board_id");
        csv(archive,db,"coverboard_checks.csv",
            "SELECT x.id,x.board_pk,b.event_id,b.station_id,b.board_id,"+
            "x.checked_at_utc,x.observer,x.board_condition,x.substrate,"+
            "x.temperature_c,x.humidity_pct,x.individuals_seen,x.taxon_label,x.notes,"+
            "x.latitude,x.longitude,x.gps_accuracy_m,x.gps_timestamp_utc,x.gps_status "+
            "FROM coverboard_checks x JOIN coverboard_units b ON b.id=x.board_pk "+
            "ORDER BY b.event_id,x.checked_at_utc");
        csv(archive,db,"area_search_passes.csv",
            "SELECT id,event_id,station_id,area_id,started_at_utc,ended_at_utc,"+
            "observer,measured_area_m2,route_length_m,habitat,weather,"+
            "repeat_pass,duplicate_control_notes,notes "+
            "FROM area_search_passes ORDER BY event_id,started_at_utc");
        csv(archive,db,"track_sign_efforts.csv",
            "SELECT id,event_id,station_id,route_id,started_at_utc,ended_at_utc,"+
            "observer,verified_length_m,substrate,weather,visibility,notes "+
            "FROM track_sign_efforts ORDER BY event_id,started_at_utc");
        csv(archive,db,"track_sign_encounters.csv",
            "SELECT id,event_id,effort_id,recorded_at_utc,sign_type,taxon_label,"+
            "age_class,identification_confidence,independent_event_key,substrate,notes,"+
            "latitude,longitude,gps_accuracy_m,gps_timestamp_utc,gps_status "+
            "FROM track_sign_encounters ORDER BY event_id,effort_id,id");
        csv(archive,db,"spotlight_passes.csv",
            "SELECT id,event_id,station_id,route_id,observer,started_at_utc,"+
            "ended_at_utc,beam_width_m,visibility_m,distance_m,beam_type,weather,"+
            "repeat_pass,duplicate_control_notes,notes "+
            "FROM spotlight_passes ORDER BY event_id,started_at_utc");
    }
}
