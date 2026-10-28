package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

/** Portable, local data snapshot. Import never merges records into existing fieldwork. */
public final class FieldBackupArchive {
    private FieldBackupArchive() {}
    private static final String FORMAT="likas-field-backup-v1";
    private static final String DATABASE="db/wildlife_field_v03.db";
    private static final long MAX_FILE=256L*1024*1024;
    private static final long MAX_ARCHIVE=768L*1024*1024;
    private static final int MAX_ENTRIES=8000;

    private static void require(boolean yes,String message)throws IOException {
        if(!yes)throw new IOException(message);
    }
    private static String hex(byte[] bytes){
        StringBuilder s=new StringBuilder();
        for(byte b:bytes)s.append(String.format(java.util.Locale.US,"%02x",b&255));
        return s.toString();
    }
    private static MessageDigest digest()throws Exception{
        return MessageDigest.getInstance("SHA-256");
    }
    private static String checksum(File f)throws Exception{
        MessageDigest md=digest();
        try(InputStream in=new BufferedInputStream(new FileInputStream(f))){
            byte[] buf=new byte[16384];int n;
            while((n=in.read(buf))!=-1)md.update(buf,0,n);
        }
        return hex(md.digest());
    }
    private static File stage(Context ctx,String prefix)throws IOException{
        File base=ctx.getCacheDir();
        File dir=new File(base,prefix+"-"+UUID.randomUUID().toString());
        require(dir.mkdirs(),"Cannot create local backup staging directory");
        return dir;
    }
    private static void clear(File f){
        if(f==null||!f.exists())return;
        if(f.isDirectory()){
            File[] children=f.listFiles();
            if(children!=null)for(File sub:children)clear(sub);
        }
        f.delete();
    }
    private static void copy(File from,File to)throws IOException{
        File parent=to.getParentFile();
        require(parent!=null&&(parent.isDirectory()||parent.mkdirs()),
            "Cannot create backup destination folder");
        try(InputStream in=new BufferedInputStream(new FileInputStream(from));
            OutputStream out=new BufferedOutputStream(new FileOutputStream(to))){
            byte[] bytes=new byte[16384];int n;
            while((n=in.read(bytes))!=-1)out.write(bytes,0,n);
        }
    }
    private static int sqliteVersion(SQLiteDatabase db){
        try(Cursor c=db.rawQuery("SELECT sqlite_version()",null)){
            require(c.moveToFirst(),"SQLite version unavailable");
            String[] parts=c.getString(0).split("\\.");
            return Integer.parseInt(parts[0])*1000000+
                Integer.parseInt(parts[1])*1000+Integer.parseInt(parts[2]);
        }catch(Exception e){return 0;}
    }
    private static void checkDatabase(File file)throws Exception{
        SQLiteDatabase db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),null,
            SQLiteDatabase.OPEN_READONLY);
        try{
            int version=db.getVersion();
            require(version==27,"Backup database schema is not LIKAS v27: "+version);
            try(Cursor c=db.rawQuery("PRAGMA quick_check",null)){
                require(c.moveToFirst()&&"ok".equalsIgnoreCase(c.getString(0)),
                    "Backup SQLite integrity check failed");
            }
            for(String table:new String[]{"surveys","field_records","projects","sites"}){
                try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM \""+table+"\"",null)){
                    require(c.moveToFirst(),"Backup missing table "+table);
                }
            }
        }finally{db.close();}
    }
    public static boolean hasActiveSurvey(Context ctx){
        try(FieldDb f=new FieldDb(ctx);
            Cursor c=f.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM surveys WHERE ended IS NULL",null)){
            return !c.moveToFirst()||c.getInt(0)>0;
        }catch(Exception e){return true;}
    }
    /** A restore is allowed only into an empty workspace, never over field records. */
    public static boolean canRestore(Context ctx){
        // Never replace a database with an uncheckpointed WAL or existing
        // orphan media. In-place recovery is deliberately unsupported.
        File base=ctx.getDatabasePath(FieldDb.NAME);
        File wal=new File(base.getAbsolutePath()+"-wal");
        if(wal.exists()&&wal.length()>0)return false;
        for(String folder:new String[]{"record_media","acoustic"}){
            File dir=new File(ctx.getFilesDir(),folder);
            File[] existing=dir.listFiles();
            if(existing!=null&&existing.length>0)return false;
        }
        try(FieldDb f=new FieldDb(ctx)){
            SQLiteDatabase db=f.getReadableDatabase();
            for(String t:new String[]{"surveys","field_records","sightings",
                    "sites","route_templates","study_protocol_templates","taxa_favorites"}){
                try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM \""+t+"\"",null)){
                    if(!c.moveToFirst()||c.getLong(0)!=0)return false;
                }
            }
            try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM projects WHERE id<>'default'",null)){
                if(!c.moveToFirst()||c.getLong(0)!=0)return false;
            }
            return !hasActiveSurvey(ctx);
        }catch(Exception e){return false;}
    }
    private static boolean safeName(String name){
        if(name==null||name.length()>230||name.contains("..")||name.contains("\\")
                ||name.startsWith("/")||name.indexOf('\0')>=0)return false;
        if(DATABASE.equals(name))return true;
        if(name.startsWith("media/record_media/")||name.startsWith("media/acoustic/")){
            String item=name.substring(name.lastIndexOf('/')+1);
            return item.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]{0,150}")&&!item.startsWith(".");
        }
        if(name.startsWith("prefs/")){
            String item=name.substring(6);
            return item.matches("[A-Za-z0-9_.-]{1,150}\\.xml")&&!item.startsWith(".");
        }
        return false;
    }
    private static void mediaEntries(Context ctx,Map<String,File> entries){
        for(String folder:new String[]{"record_media","acoustic"}){
            File dir=new File(ctx.getFilesDir(),folder);
            File[] files=dir.listFiles();
            if(files==null)continue;
            for(File f:files){
                String name="media/"+folder+"/"+f.getName();
                if(f.isFile()&&safeName(name))entries.put(name,f);
            }
        }
        File prefDir=new File(ctx.getApplicationInfo().dataDir,"shared_prefs");
        File[] prefs=prefDir.listFiles();
        if(prefs!=null)for(File f:prefs){
            String name="prefs/"+f.getName();
            if(f.isFile()&&safeName(name))entries.put(name,f);
        }
    }
    /** Uses SQLite's online VACUUM INTO snapshot, never raw-copies an active WAL. */
    public static String export(Context ctx,OutputStream stream)throws Exception{
        require(!hasActiveSurvey(ctx),
            "Finish or end the active survey before creating a recoverable snapshot.");
        File dir=stage(ctx,"likas-export");
        try{
            File snapshot=new File(dir,"snapshot.sqlite");
            try(FieldDb helper=new FieldDb(ctx)){
                SQLiteDatabase db=helper.getWritableDatabase();
                require(sqliteVersion(db)>=3027000,
                    "This Android SQLite build lacks safe online backup (VACUUM INTO).");
                try(Cursor c=db.rawQuery("PRAGMA quick_check",null)){
                    require(c.moveToFirst()&&"ok".equalsIgnoreCase(c.getString(0)),
                        "Local database integrity check failed");
                }
                db.execSQL("VACUUM INTO '"+snapshot.getAbsolutePath().replace("'","''")+"'");
            }
            checkDatabase(snapshot);
            LinkedHashMap<String,File> entries=new LinkedHashMap<>();
            entries.put(DATABASE,snapshot);
            mediaEntries(ctx,entries);
            require(entries.size()<=MAX_ENTRIES,"Too many backup attachments");
            JSONObject hashes=new JSONObject();
            long total=0;
            for(Map.Entry<String,File> item:entries.entrySet()){
                File file=item.getValue();
                long length=file.length();
                require(length<=MAX_FILE,"Oversized backup attachment: "+item.getKey());
                total+=length;
                require(total<=MAX_ARCHIVE,"Local field backup exceeds safety size limit");
                hashes.put(item.getKey(),checksum(file));
            }
            JSONObject manifest=new JSONObject();
            manifest.put("format",FORMAT);
            manifest.put("sqliteSchema",27);
            manifest.put("createdUtc",new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",java.util.Locale.US) {{
                  setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }}.format(new Date()));
            manifest.put("files",hashes);
            try(ZipOutputStream zip=new ZipOutputStream(new BufferedOutputStream(stream))){
                byte[] buf=new byte[16384];
                for(Map.Entry<String,File> item:entries.entrySet()){
                    zip.putNextEntry(new ZipEntry(item.getKey()));
                    try(InputStream in=new BufferedInputStream(new FileInputStream(item.getValue()))){
                        int n;
                        while((n=in.read(buf))!=-1)zip.write(buf,0,n);
                    }
                    zip.closeEntry();
                }
                zip.putNextEntry(new ZipEntry("manifest.json"));
                zip.write(manifest.toString().getBytes("UTF-8"));
                zip.closeEntry();
                zip.finish();
            }
            return "Verified v27 snapshot with "+(entries.size()-1)+" attachments and preferences.";
        }finally{clear(dir);}
    }
    private static File unpack(Context ctx,InputStream input)throws Exception{
        File dir=stage(ctx,"likas-restore");
        try{
            HashSet<String> found=new HashSet<>();
            JSONObject manifest=null;
            long total=0;
            int count=0;
            try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(input))){
                ZipEntry item;
                byte[] buf=new byte[16384];
                while((item=zip.getNextEntry())!=null){
                    String name=item.getName();
                    require(!item.isDirectory()&&(safeName(name)||"manifest.json".equals(name)),
                        "Unsafe or unexpected backup entry");
                    require(found.add(name),"Duplicate backup entry");
                    require(++count<=MAX_ENTRIES,"Too many ZIP entries");
                    File target=new File(dir,name);
                    require(target.getCanonicalPath().startsWith(dir.getCanonicalPath()+File.separator),
                        "Invalid archive path");
                    require(target.getParentFile().isDirectory()||target.getParentFile().mkdirs(),
                        "Cannot stage backup entry");
                    long size=0;
                    try(OutputStream out=new BufferedOutputStream(new FileOutputStream(target))){
                        int n;
                        while((n=zip.read(buf))!=-1){
                            size+=n;total+=n;
                            require(size<=MAX_FILE&&total<=MAX_ARCHIVE,
                                "Archive is larger than backup safety limits");
                            out.write(buf,0,n);
                        }
                    }
                    zip.closeEntry();
                    if("manifest.json".equals(name)){
                        require(size<250000,"Backup manifest is too large");
                        try(InputStream in=new FileInputStream(target)){
                            byte[] raw=new byte[(int)size];
                            int off=0,n;
                            while(off<raw.length&&(n=in.read(raw,off,raw.length-off))>0)off+=n;
                            require(off==raw.length,"Truncated backup manifest");
                            manifest=new JSONObject(new String(raw,"UTF-8"));
                        }
                    }
                }
            }
            require(manifest!=null&&FORMAT.equals(manifest.optString("format")),
                "Not a LIKAS current-version recovery archive");
            require(manifest.optInt("sqliteSchema")==27,"Incompatible backup schema");
            JSONObject hashes=manifest.getJSONObject("files");
            require(hashes.length()==found.size()-1&&found.contains(DATABASE),
                "Missing or undeclared backup data");
            Iterator<String> names=hashes.keys();
            while(names.hasNext()){
                String name=names.next();
                require(found.contains(name)&&safeName(name),"Unknown manifest entry");
                require(checksum(new File(dir,name)).equalsIgnoreCase(hashes.getString(name)),
                    "Backup checksum mismatch: "+name);
            }
            checkDatabase(new File(dir,DATABASE));
            return dir;
        }catch(Exception e){clear(dir);throw e;}
    }
    /** Only for a fresh, empty LIKAS workspace, then exit the process to clear caches. */
    public static String restoreIntoEmpty(Context ctx,InputStream input)throws Exception{
        require(canRestore(ctx),"Restore is blocked: this workspace has saved field data.");
        File dir=unpack(ctx,input);
        try{
            require(canRestore(ctx),"Workspace changed during backup validation.");
            File db=ctx.getDatabasePath(FieldDb.NAME);
            File databaseDir=db.getParentFile();
            require(databaseDir!=null&&(databaseDir.exists()||databaseDir.mkdirs()),
                "Cannot prepare database directory");
            File old=new File(databaseDir,FieldDb.NAME+".before-restore");
            require(!old.exists(),"A prior recovery safety copy exists. Do not overwrite it.");
            // Database helper connections are closed by the empty-workspace check.
            File incoming=new File(databaseDir,FieldDb.NAME+".incoming");
            copy(new File(dir,DATABASE),incoming);
            checkDatabase(incoming);
            if(db.exists())require(db.renameTo(old),
                "Cannot preserve original empty database before recovery");
            boolean restored=incoming.renameTo(db);
            if(!restored){
                if(old.exists())old.renameTo(db);
                throw new IOException("Cannot install the validated database snapshot.");
            }
            // Restore only app-owned field media and saved settings, no arbitrary paths.
            File root=new File(ctx.getApplicationInfo().dataDir);
            JSONObject hashes=new JSONObject(new String(
                Files.readAllBytes(new File(dir,"manifest.json").toPath()),"UTF-8"))
                .getJSONObject("files");
            Iterator<String> names=hashes.keys();
            while(names.hasNext()){
                String name=names.next();
                if(DATABASE.equals(name))continue;
                File dest;
                if(name.startsWith("prefs/"))dest=new File(root,"shared_prefs/"+name.substring(6));
                else dest=new File(ctx.getFilesDir(),name.substring("media/".length()));
                copy(new File(dir,name),dest);
            }
            // Retain before-restore database as an explicit local rollback artifact.
            return "Restore installed. Close and restart LIKAS before viewing fieldwork.";
        }finally{clear(dir);}
    }
}
