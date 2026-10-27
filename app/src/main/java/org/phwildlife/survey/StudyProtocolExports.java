package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Export both editable study templates and frozen survey rule snapshots. */
public final class StudyProtocolExports {
    private StudyProtocolExports(){}
    private static String quote(String raw){
        return "\""+(raw==null?"":raw.replace("\"","\"\""))+"\"";
    }
    private static void write(ZipOutputStream zip,SQLiteDatabase db,
            String path,String query)throws Exception{
        try(Cursor c=db.rawQuery(query,null)){
            zip.putNextEntry(new ZipEntry(path));
            StringBuilder header=new StringBuilder();
            for(int i=0;i<c.getColumnCount();i++){
                if(i>0)header.append(',');
                header.append(quote(c.getColumnName(i)));
            }
            zip.write((header+"\n").getBytes(StandardCharsets.UTF_8));
            while(c.moveToNext()){
                StringBuilder row=new StringBuilder();
                for(int i=0;i<c.getColumnCount();i++){
                    if(i>0)row.append(',');
                    row.append(quote(c.isNull(i)?"":c.getString(i)));
                }
                zip.write((row+"\n").getBytes(StandardCharsets.UTF_8));
            }
            zip.closeEntry();
        }
    }
    public static void writeZip(ZipOutputStream zip,SQLiteDatabase db)throws Exception{
        write(zip,db,"study_protocol_templates.csv",
            "SELECT id,project_id,method,name,min_visits,min_minutes,min_units,"+
            "require_verified_gps,require_protocol_photo,created_at_utc "+
            "FROM study_protocol_templates ORDER BY project_id,method,created_at_utc");
        write(zip,db,"survey_study_requirements.csv",
            "SELECT event_id,template_id,template_name,min_visits,min_minutes,min_units,"+
            "require_verified_gps,require_protocol_photo,applied_at_utc "+
            "FROM survey_study_protocols ORDER BY event_id");
    }
}
