package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Keeps image copies attached to a specific board check or mammal sign. */
public final class ProtocolEvidencePhotos {
    private ProtocolEvidencePhotos(){}
    private static String utc(){
        SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));return fmt.format(new Date());
    }
    private static File folder(Context ctx){
        File f=new File(ctx.getFilesDir(),"record_media");
        if(!f.isDirectory()&&!f.mkdirs())throw new IllegalStateException("Media directory unavailable");
        return f;
    }
    public static void attach(Context ctx,SQLiteDatabase db,String eventId,
            String type,long targetId,Uri source)throws Exception{
        if(!"coverboard_check".equals(type)&&!"track_sign".equals(type))
            throw new IllegalArgumentException("Unknown evidence target");
        if(targetId<1||eventId==null)throw new IllegalArgumentException("Unknown target");
        String table="coverboard_check".equals(type)?
            "coverboard_checks x JOIN coverboard_units b ON b.id=x.board_pk":
            "track_sign_encounters x";
        String link="coverboard_check".equals(type)?"b.event_id":"x.event_id";
        try(Cursor c=db.rawQuery("SELECT 1 FROM "+table+
            " WHERE x.id=? AND "+link+"=?",new String[]{String.valueOf(targetId),eventId})){
            if(!c.moveToFirst())throw new SecurityException("Photo does not belong to survey");
        }
        String pending=PhotoEvidence.addPending(ctx,source);
        File prior=new File(folder(ctx),pending);
        String suffix=pending.substring(pending.lastIndexOf('.'));
        String name="protocol_"+UUID.randomUUID()+suffix;
        File stored=new File(folder(ctx),name);
        if(!prior.renameTo(stored)){
            prior.delete();throw new IllegalStateException("Cannot save selected photo");
        }
        String mime=suffix.equals(".png")?"image/png":suffix.equals(".webp")?
            "image/webp":suffix.equals(".heic")?"image/heic":"image/jpeg";
        try{
            db.execSQL("INSERT INTO protocol_media(event_id,target_type,target_id,"+
                "file_name,mime_type,added_utc,source_note) VALUES(?,?,?,?,?,?,?)",
                new Object[]{eventId,type,targetId,name,mime,utc(),
                    "original_copy_from_android_document_picker"});
        }catch(Exception err){stored.delete();throw err;}
    }
    private static String q(String value){
        return "\""+(value==null?"":value.replace("\"","\"\""))+"\"";
    }
    public static void writeZip(Context ctx,ZipOutputStream zip,SQLiteDatabase db)throws Exception{
        zip.putNextEntry(new ZipEntry("protocol_photo_index.csv"));
        zip.write(("id,event_id,target_type,target_id,file_name,mime_type,"+
            "added_utc,source_note\n").getBytes(StandardCharsets.UTF_8));
        try(Cursor c=db.rawQuery("SELECT id,event_id,target_type,target_id,"+
            "file_name,mime_type,added_utc,source_note FROM protocol_media ORDER BY id",null)){
            while(c.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int i=0;i<c.getColumnCount();i++){
                    if(i>0)line.append(',');
                    line.append(q(c.isNull(i)?"":c.getString(i)));
                }
                zip.write((line+"\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        zip.closeEntry();
        try(Cursor c=db.rawQuery("SELECT file_name FROM protocol_media ORDER BY id",null)){
            byte[] buffer=new byte[16384];
            while(c.moveToNext()){
                String name=c.getString(0);
                if(name==null||!name.matches("protocol_[0-9a-fA-F-]+\\.(jpg|png|webp|heic)"))
                    throw new SecurityException("Unsafe protocol photo path");
                File source=new File(folder(ctx),name);
                if(!source.isFile())throw new FileNotFoundException("Missing protocol photo "+name);
                zip.putNextEntry(new ZipEntry("protocol_photos/"+name));
                try(FileInputStream input=new FileInputStream(source)){
                    int n;while((n=input.read(buffer))!=-1)zip.write(buffer,0,n);
                }
                zip.closeEntry();
            }
        }
    }
}
