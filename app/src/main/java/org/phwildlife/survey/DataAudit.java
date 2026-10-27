package org.phwildlife.survey;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Read-only quality checks. Flags do not edit, exclude, or fabricate observations. */
public final class DataAudit {
    private DataAudit(){}
    public static final class Issue {
        public final String level,code,message,sample;
        public final int count;
        Issue(String level,String code,int count,String sample,String message){
            this.level=level;this.code=code;this.count=count;this.sample=sample;this.message=message;
        }
    }
    private static void check(List<Issue> issues,SQLiteDatabase d,String level,String code,
            String sql,String message){
        try(Cursor c=d.rawQuery(sql,null)){
            if(c.moveToFirst()){
                int count=c.getInt(0);
                if(count>0)issues.add(new Issue(level,code,count,
                    c.isNull(1)?"":c.getString(1),message));
            }
        }catch(Exception err){
            issues.add(new Issue("ERROR","AUDIT_QUERY_"+code,1,"",err.getMessage()));
        }
    }
    public static List<Issue> run(Context context,SQLiteDatabase d){
        ArrayList<Issue> issues=new ArrayList<>();
        try(Cursor integrity=d.rawQuery("PRAGMA quick_check",null)){
            while(integrity.moveToNext()){
                String answer=integrity.getString(0);
                if(!"ok".equalsIgnoreCase(answer))
                    issues.add(new Issue("ERROR","SQLITE_INTEGRITY",1,"",answer));
            }
        }catch(Exception ex){
            issues.add(new Issue("ERROR","SQLITE_QUICK_CHECK",1,"",ex.getMessage()));
        }
        check(issues,d,"ERROR","ORPHAN_RECORDS",
            "SELECT COUNT(*),MIN(r.event_id) FROM field_records r LEFT JOIN surveys s ON s.id=r.event_id WHERE s.id IS NULL",
            "Biological observations exist without their parent survey.");
        check(issues,d,"ERROR","ORPHAN_POINT_LINKS",
            "SELECT COUNT(*),MIN(CAST(r.id AS TEXT)) FROM field_records r LEFT JOIN point_counts p ON p.id=r.point_count_id "+
                "WHERE r.point_count_id IS NOT NULL AND p.id IS NULL",
            "Observation references a missing point-count visit.");
        check(issues,d,"ERROR","ORPHAN_SAMPLING_UNIT_LINKS",
            "SELECT COUNT(*),MIN(CAST(r.id AS TEXT)) FROM field_records r LEFT JOIN sampling_units u ON u.id=r.sampling_unit_id "+
                "WHERE r.sampling_unit_id IS NOT NULL AND u.id IS NULL",
            "Observation references a missing sampling visit.");
        check(issues,d,"ERROR","MISSING_STATION_RECORD",
            "SELECT COUNT(*),MIN(pc.station_id) FROM point_counts pc LEFT JOIN sites st ON st.id=pc.station_id "+
                "WHERE st.id IS NULL AND pc.id NOT LIKE 'LEGACY-%'",
            "A point-count visit has no permanent station identity in the site registry.");
        check(issues,d,"ERROR","TEMPLATE_ORPHAN_STATION",
            "SELECT COUNT(*),MIN(t.station_id) FROM route_template_stations t LEFT JOIN sites s ON s.id=t.station_id "+
                "WHERE s.id IS NULL",
            "Saved route references a station that no longer exists.");
        check(issues,d,"ERROR","TEMPLATE_PROJECT_MISMATCH",
            "SELECT COUNT(*),MIN(t.station_id) FROM route_template_stations t "+
                "JOIN route_templates r ON r.id=t.template_id "+
                "JOIN sites s ON s.id=t.station_id WHERE s.project_id<>r.project_id",
            "A saved route includes a station belonging to a different project.");
        check(issues,d,"ERROR","TEMPLATE_INCOMPLETE",
            "SELECT COUNT(*),MIN(id) FROM route_templates WHERE station_count<>("+
                "SELECT COUNT(*) FROM route_template_stations WHERE template_id=route_templates.id)",
            "The number of saved template stations differs from the route plan.");
        check(issues,d,"ERROR","SURVEY_PLAN_MISMATCH",
            "SELECT COUNT(*),MIN(s.id) FROM surveys s WHERE COALESCE(s.planned_station_count,0)>0 "+
                "AND s.planned_station_count<>(SELECT COUNT(*) FROM survey_stations x WHERE x.event_id=s.id)",
            "A survey contains an incorrect number of planned stations.");
        check(issues,d,"ERROR","SURVEY_TEMPLATE_MISSING",
            "SELECT COUNT(*),MIN(s.id) FROM surveys s LEFT JOIN route_templates rt ON rt.id=s.route_template_id "+
                "WHERE s.route_template_id IS NOT NULL AND rt.id IS NULL",
            "Survey refers to an unavailable saved route.");
        check(issues,d,"ERROR","STATION_COORDINATE_PAIR",
            "SELECT COUNT(*),MIN(id) FROM sites WHERE (latitude IS NULL AND longitude IS NOT NULL) "+
                "OR (latitude IS NOT NULL AND longitude IS NULL)",
            "Station latitude and longitude must be recorded together.");
        check(issues,d,"REVIEW","STATION_GPS_NOT_CAPTURED",
            "SELECT COUNT(*),MIN(id) FROM point_counts WHERE planned_chainage_m IS NOT NULL AND "+
                "(gps_latitude IS NULL OR gps_longitude IS NULL)",
            "One or more planned point counts lack a verified GPS position. Review any flagged GPS-missing status.");
        check(issues,d,"REVIEW","SITE_GPS_UNCERTAINTY_UNKNOWN",
            "SELECT COUNT(*),MIN(id) FROM sites WHERE latitude IS NOT NULL AND "+
                "longitude IS NOT NULL AND coordinate_uncertainty_m IS NULL",
            "Station position exists without a recorded accuracy estimate.");
        check(issues,d,"ERROR","BAD_DETECTION_COUNT",
            "SELECT COUNT(*),MIN(CAST(id AS TEXT)) FROM field_records "+
                "WHERE deleted_at IS NULL AND (individual_count IS NULL OR individual_count<=0)",
            "Active detection count is missing or zero.");
        check(issues,d,"REVIEW","NAME_ID_CONTRADICTION",
            "SELECT COUNT(*),MIN(CAST(id AS TEXT)) FROM field_records "+
                "WHERE deleted_at IS NULL AND identification_level='unidentified' AND "+
                "TRIM(COALESCE(scientific_name,''))<>''",
            "Unidentified record carries a species-level scientific name.");
        check(issues,d,"REVIEW","COMPLETED_COUNT_TOO_SHORT",
            "SELECT COUNT(*),MIN(id) FROM point_counts WHERE completed=1 AND ended IS NOT NULL "+
                "AND ((julianday(ended)-julianday(started))*86400000.0-COALESCE(unit_paused_ms,0)) "+
                "< (target_seconds*1000.0)-2000",
            "Point-count marked complete despite insufficient active station time.");
        check(issues,d,"REVIEW","COMPLETED_WITH_UNVISITED_POINTS",
            "SELECT COUNT(*),MIN(s.id) FROM surveys s WHERE s.outcome='complete' "+
                "AND EXISTS(SELECT 1 FROM survey_stations x WHERE x.event_id=s.id AND x.visit_state='planned')",
            "Completed survey still contains unvisited planned stations.");
        check(issues,d,"REVIEW","SURVEY_EFFORT_MISSING",
            "SELECT COUNT(*),MIN(id) FROM surveys WHERE outcome='complete' AND method='point_count' "+
                "AND COALESCE(observer_effort_minutes,0)<=0",
            "Completed point-count survey has no recorded sampling effort.");
        check(issues,d,"ERROR","INVALID_GPS_COORDINATE",
            "SELECT COUNT(*),MIN(CAST(id AS TEXT)) FROM field_records WHERE deleted_at IS NULL "+
                "AND ((observer_lat IS NOT NULL AND (observer_lat < -90 OR observer_lat > 90)) "+
                "OR (observer_lon IS NOT NULL AND (observer_lon < -180 OR observer_lon > 180)))",
            "Detection has a coordinate outside valid latitude or longitude ranges.");
        // Repeat-visit station coordinates remain independent of the saved baseline.
        // A large difference is a review prompt, not automatic invalidation.
        int changedStations=0;String firstChanged="";
        try(Cursor c=d.rawQuery(
            "SELECT p.id,p.gps_latitude,p.gps_longitude,p.gps_accuracy_m,"+
            "s.latitude,s.longitude,s.coordinate_uncertainty_m FROM point_counts p "+
            "JOIN sites s ON s.id=p.station_id WHERE p.planned_chainage_m IS NOT NULL "+
            "AND p.gps_latitude IS NOT NULL AND p.gps_longitude IS NOT NULL "+
            "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL",null)){
            float[] distance=new float[1];
            while(c.moveToNext()){
                android.location.Location.distanceBetween(
                    c.getDouble(1),c.getDouble(2),c.getDouble(4),c.getDouble(5),distance);
                double uncertainty=(c.isNull(3)?75:c.getDouble(3))+
                    (c.isNull(6)?75:c.getDouble(6));
                if(distance[0]>Math.max(150.0,2.0*uncertainty)){
                    changedStations++;
                    if(firstChanged.isEmpty())firstChanged=c.getString(0);
                }
            }
        }catch(Exception ex){
            issues.add(new Issue("ERROR","STATION_REVISIT_CHECK",1,"",ex.getMessage()));
        }
        if(changedStations>0)issues.add(new Issue("REVIEW","STATION_POSITION_DRIFT",
            changedStations,firstChanged,
            "A revisit GPS position lies far from its saved station baseline. Check physical station relocation, GPS error or altered coordinates."));
        int absent=0;String first="";
        try(Cursor c=d.rawQuery("SELECT file_name FROM observation_media",null)){
            File dir=new File(context.getFilesDir(),"record_media");
            while(c.moveToNext()){
                String name=c.getString(0);
                if(name==null||name.contains("/")||name.contains("..")||!new File(dir,name).isFile()){
                    absent++;if(first.isEmpty())first=String.valueOf(name);
                }
            }
        }catch(Exception ex){issues.add(new Issue("ERROR","PHOTO_AUDIT_ERROR",1,"",ex.getMessage()));}
        if(absent>0)issues.add(new Issue("ERROR","MISSING_EVIDENCE_PHOTOS",absent,first,
            "Archived photo index references missing media files."));
        return issues;
    }
    private static String csv(String value){
        if(value==null)return "";
        return "\""+value.replace("\"","\"\"").replace("\r"," ").replace("\n"," ")+"\"";
    }
    public static void writeZip(Context ctx,ZipOutputStream zip,SQLiteDatabase d)throws Exception{
        zip.putNextEntry(new ZipEntry("data_quality_flags.csv"));
        zip.write("severity,code,affected_record_count,example_id,explanation\n".getBytes(StandardCharsets.UTF_8));
        for(Issue issue:run(ctx,d)){
            String line=csv(issue.level)+","+csv(issue.code)+","+issue.count+","+
                csv(issue.sample)+","+csv(issue.message)+"\n";
            zip.write(line.getBytes(StandardCharsets.UTF_8));
        }
        zip.closeEntry();
    }
}
