package org.phwildlife.survey;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Offline app-private photograph archive. The chosen image is copied immediately,
 * so edits and device restarts do not depend on a temporary URI grant.
 */
public final class PhotoEvidence {
    private PhotoEvidence(){}
    public static final int MAX_BYTES=15*1024*1024;
    private static File dir(Context ctx){File f=new File(ctx.getFilesDir(),"record_media");if(!f.exists())f.mkdirs();return f;}
    private static String now(){SimpleDateFormat d=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);d.setTimeZone(TimeZone.getTimeZone("UTC"));return d.format(new Date());}
    private static String mimeSuffix(String type){
        if(type==null)return ".jpg";
        if(type.equals("image/png"))return ".png";
        if(type.equals("image/webp"))return ".webp";
        if(type.equals("image/heic")||type.equals("image/heif"))return ".heic";
        return ".jpg";
    }
    public static String addPending(Context ctx,Uri uri)throws Exception{
        String type=ctx.getContentResolver().getType(uri);
        if(type==null||!type.startsWith("image/"))throw new Exception("Choose an image file");
        String filename="pending_"+UUID.randomUUID()+mimeSuffix(type);
        File output=new File(dir(ctx),filename);
        boolean success=false;int bytes=0;
        try(InputStream in=ctx.getContentResolver().openInputStream(uri);
            OutputStream out=new FileOutputStream(output)){
            if(in==null)throw new IOException("Image file unavailable");
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                bytes+=n;if(bytes>MAX_BYTES)throw new Exception("Photo must be smaller than 15 MB");
                out.write(buffer,0,n);
            }
            success=bytes>0;
        }finally{if(!success)output.delete();}
        if(!success)throw new Exception("Empty image");
        return filename;
    }
    public static void preview(Context ctx,String filename,ImageView view){
        File f=new File(dir(ctx),filename==null?"":filename);
        if(!f.exists()){view.setImageDrawable(null);return;}
        BitmapFactory.Options bounds=new BitmapFactory.Options();
        bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getAbsolutePath(),bounds);
        int scale=1;while(bounds.outWidth/scale>640||bounds.outHeight/scale>640)scale*=2;
        BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=scale;
        try{Bitmap image=BitmapFactory.decodeFile(f.getAbsolutePath(),o);view.setImageBitmap(image);}
        catch(OutOfMemoryError err){view.setImageDrawable(null);}
    }
    public static void attach(Context ctx,SQLiteDatabase db,long recordId,String pending)throws Exception{
        if(pending==null||!pending.startsWith("pending_"))return;
        File old=new File(dir(ctx),pending);
        if(!old.exists())throw new IOException("Pending image unavailable");
        String suffix=pending.substring(pending.lastIndexOf('.'));
        String targetName="observation_"+recordId+"_"+UUID.randomUUID()+suffix;
        File target=new File(dir(ctx),targetName);
        if(!old.renameTo(target))throw new IOException("Cannot move image into record archive");
        String mime=suffix.equals(".png")?"image/png":suffix.equals(".webp")?"image/webp":
           suffix.equals(".heic")?"image/heic":"image/jpeg";
        db.execSQL("INSERT INTO observation_media(record_id,file_name,mime_type,original_name,added_utc,source_note) VALUES(?,?,?,?,?,?)",
          new Object[]{recordId,targetName,mime,pending,now(),"device_local_copy_from_document_picker"});
    }
    private static String q(String v){return v==null?"":"\""+v.replace("\"","\"\"")+"\"";}
    public static void writeZip(Context ctx,ZipOutputStream zip,SQLiteDatabase db)throws Exception{
        zip.putNextEntry(new ZipEntry("photo_evidence_index.csv"));
        zip.write("media_id,record_id,relative_filename,mime_type,original_name,added_utc,source_note\n".getBytes(StandardCharsets.UTF_8));
        try(Cursor c=db.rawQuery("SELECT id,record_id,file_name,mime_type,original_name,added_utc,source_note FROM observation_media ORDER BY id",null)){
            while(c.moveToNext()){
                StringBuilder line=new StringBuilder();
                for(int i=0;i<7;i++){if(i>0)line.append(',');line.append(q(c.isNull(i)?"":c.getString(i)));}
                line.append('\n');zip.write(line.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        zip.closeEntry();
        try(Cursor c=db.rawQuery("SELECT DISTINCT file_name FROM observation_media ORDER BY file_name",null)){
            byte[] buffer=new byte[8192];
            while(c.moveToNext()){
                String name=c.getString(0);
                if(name.contains("/")||name.contains(".."))continue;
                File f=new File(dir(ctx),name);if(!f.isFile())continue;
                zip.putNextEntry(new ZipEntry("photos/"+name));
                try(InputStream in=new FileInputStream(f)){int n;while((n=in.read(buffer))!=-1)zip.write(buffer,0,n);}
                zip.closeEntry();
            }
        }
    }
}
