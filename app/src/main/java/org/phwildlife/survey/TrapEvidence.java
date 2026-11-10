package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Full deployment, check, and capture audit exports, including canceled surveys. */
public final class TrapEvidence {
    private TrapEvidence(){}
    private static String quote(String s){
        return "\""+(s==null?"":s.replace("\"","\"\""))+"\"";
    }
    private static void write(ZipOutputStream zip,SQLiteDatabase db,
            String name,String sql)throws Exception{
        try(Cursor c=db.rawQuery(sql,null)){
            zip.putNextEntry(new ZipEntry(name));
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
        write(zip,db,"trap_deployments.csv",
            "SELECT id,event_id,station_id,method,device_id,deployed_at_utc,"+
            "retrieved_at_utc,status,fence_length_m,setup_notes "+
            "FROM trap_deployments ORDER BY event_id,device_id");
        write(zip,db,"trap_checks.csv",
            "SELECT x.id,x.deployment_id,d.event_id,d.method,d.device_id,"+
            "x.checked_at_utc,x.operational,x.outage_minutes,x.device_condition,"+
            "x.observations,x.observer_notes FROM trap_checks x "+
            "JOIN trap_deployments d ON d.id=x.deployment_id "+
            "ORDER BY d.event_id,x.checked_at_utc");
        write(zip,db,"trap_captures.csv",
            "SELECT z.id,z.check_id,d.event_id,d.method,d.device_id,z.taxon_label,"+
            "z.individual_mark,z.logged_at_utc,z.released_at_utc,z.disposition,z.notes "+
            "FROM trap_captures z JOIN trap_checks x ON x.id=z.check_id "+
            "JOIN trap_deployments d ON d.id=x.deployment_id "+
            "ORDER BY d.event_id,z.logged_at_utc");
    }
}
