package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Exports original WAV audio, clip provenance and time-coded call annotations. */
public final class AcousticEvidence {
    private AcousticEvidence(){}
    public static File directory(Context context){
        File folder=new File(context.getFilesDir(),"acoustic");
        if(!folder.exists()&&!folder.mkdirs()&&!folder.isDirectory())
            throw new IllegalStateException("Unable to create private audio folder");
        return folder;
    }
    public static boolean safeName(String name){
        return name!=null&&name.matches("clip_[0-9]+_[a-z0-9]+\\.wav");
    }
    private static String csvValue(String s){
        return "\""+(s==null?"":s.replace("\"","\"\""))+"\"";
    }
    private static void csv(ZipOutputStream zip,SQLiteDatabase db,
        String entry,String query)throws Exception{
        try(Cursor records=db.rawQuery(query,null)){
            zip.putNextEntry(new ZipEntry(entry));
            StringBuilder header=new StringBuilder();
            for(int col=0;col<records.getColumnCount();col++){
                if(col>0)header.append(',');
                header.append(csvValue(records.getColumnName(col)));
            }
            zip.write((header+"\n").getBytes(StandardCharsets.UTF_8));
            while(records.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int col=0;col<records.getColumnCount();col++){
                    if(col>0)line.append(',');
                    line.append(csvValue(records.isNull(col)?"":records.getString(col)));
                }
                zip.write((line+"\n").getBytes(StandardCharsets.UTF_8));
            }
            zip.closeEntry();
        }
    }
    public static void writeZip(Context context,ZipOutputStream zip,SQLiteDatabase db)
        throws Exception{
        csv(zip,db,"acoustic_clips.csv",
            "SELECT id,event_id,station_id,taxon_group,method,filename,"+
            "recorded_at_utc,duration_ms,sample_rate_hz,channels,format,notes,recording_status "+
            "FROM acoustic_clips ORDER BY recorded_at_utc");
        csv(zip,db,"acoustic_labels.csv",
            "SELECT id,clip_id,start_ms,end_ms,call_label,notes,reviewed_at_utc,"+
            "taxon_key,observation_id,selected_frequency_hz,COALESCE(review_status,'unverified') "+
            "FROM acoustic_labels ORDER BY clip_id,start_ms,id");
        File folder=directory(context);
        try(Cursor c=db.rawQuery(
            "SELECT filename FROM acoustic_clips ORDER BY recorded_at_utc",null)){
            byte[] buffer=new byte[16384];
            while(c.moveToNext()){
                String name=c.getString(0);
                if(!safeName(name))throw new SecurityException("Unsafe clip name in database");
                File source=new File(folder,name);
                if(!source.isFile())throw new FileNotFoundException("Missing survey WAV: "+name);
                zip.putNextEntry(new ZipEntry("audio/"+name));
                try(FileInputStream in=new FileInputStream(source)){
                    int n;while((n=in.read(buffer))!=-1)zip.write(buffer,0,n);
                }
                zip.closeEntry();
            }
        }
    }
}
