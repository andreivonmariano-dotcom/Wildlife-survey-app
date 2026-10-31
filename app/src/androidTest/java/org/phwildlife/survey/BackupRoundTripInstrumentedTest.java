package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.security.MessageDigest;
import static org.junit.Assert.*;

/** Runs ONLY on an isolated disposable emulator. No production user data. */
@RunWith(AndroidJUnit4.class)
public final class BackupRoundTripInstrumentedTest {
    private static byte[] digest(byte[] payload)throws Exception{
        return MessageDigest.getInstance("SHA-256").digest(payload);
    }
    private static String hex(byte[] data){
        StringBuilder s=new StringBuilder();
        for(byte b:data)s.append(String.format(java.util.Locale.US,"%02x",b&255));
        return s.toString();
    }
    private static long count(Context app,String table)throws Exception{
        try(FieldDb db=new FieldDb(app);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM "+table,null)){
            assertTrue(c.moveToFirst());return c.getLong(0);
        }
    }
    @Test public void testFullBackupRestoresOnlyIntoFreshWorkspace()throws Exception{
        Context ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        // This build runs on a newly created emulator image. Never clear app data on a real device.
        assertTrue("New emulator must begin with empty workspace",FieldBackupArchive.canRestore(ctx));
        final String id="RC7D-QA-"+System.currentTimeMillis();
        try(FieldDb helper=new FieldDb(ctx)){
            SQLiteDatabase db=helper.getWritableDatabase();
            ContentValues survey=new ContentValues();
            survey.put("id",id);
            survey.put("method","point_count");
            survey.put("taxon_group","Birds");
            survey.put("started","2026-10-28T10:00:00Z");
            survey.put("ended","2026-10-28T10:10:00Z");
            survey.put("complete",1);
            assertTrue(db.insertOrThrow("surveys",null,survey)>0);
        }
        File media=new File(new File(ctx.getFilesDir(),"record_media"),"qa_record_image.jpg");
        assertTrue(media.getParentFile().isDirectory()||media.getParentFile().mkdirs());
        byte[] bytes="LIKAS-RC7D-RECOVERY-MEDIA-FIXTURE".getBytes("UTF-8");
        try(FileOutputStream out=new FileOutputStream(media)){out.write(bytes);}
        assertFalse("Protect nonempty workspace",FieldBackupArchive.canRestore(ctx));

        ByteArrayOutputStream archive=new ByteArrayOutputStream();
        String exported=FieldBackupArchive.export(ctx,archive);
        assertTrue(exported.contains("Verified v27"));
        HashMap<String,byte[]> unzipped=new HashMap<>();
        try(ZipInputStream in=new ZipInputStream(
            new ByteArrayInputStream(archive.toByteArray()))){
            ZipEntry entry;
            while((entry=in.getNextEntry())!=null){
                ByteArrayOutputStream value=new ByteArrayOutputStream();
                byte[] buf=new byte[8192];int n;
                while((n=in.read(buf))!=-1)value.write(buf,0,n);
                unzipped.put(entry.getName(),value.toByteArray());
                in.closeEntry();
            }
        }
        assertTrue(unzipped.containsKey("db/wildlife_field_v03.db"));
        assertTrue(unzipped.containsKey("media/record_media/qa_record_image.jpg"));
        assertArrayEquals(bytes,unzipped.get("media/record_media/qa_record_image.jpg"));
        JSONObject manifest=new JSONObject(new String(unzipped.get("manifest.json"),"UTF-8"));
        assertEquals(27,manifest.getInt("sqliteSchema"));
        assertEquals(hex(digest(bytes)),manifest.getJSONObject("files")
            .getString("media/record_media/qa_record_image.jpg"));

        boolean blocked=false;
        try{FieldBackupArchive.restoreIntoEmpty(ctx,
            new ByteArrayInputStream(archive.toByteArray()));}
        catch(Exception ex){blocked=true;}
        assertTrue("Restore must never overwrite existing survey",blocked);
        assertEquals(1,count(ctx,"surveys"));

        try(FieldDb helper=new FieldDb(ctx)){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.delete("surveys","id=?",new String[]{id});
        }
        assertTrue(media.delete());
        assertTrue("Restore workspace should be empty after fixture deletion",
            FieldBackupArchive.canRestore(ctx));
        String msg=FieldBackupArchive.restoreIntoEmpty(ctx,
            new ByteArrayInputStream(archive.toByteArray()));
        assertTrue(msg.contains("Restore installed"));
        SQLiteDatabase restored=SQLiteDatabase.openDatabase(
            ctx.getDatabasePath(FieldDb.NAME).getAbsolutePath(),null,
            SQLiteDatabase.OPEN_READONLY);
        try(Cursor c=restored.rawQuery(
            "SELECT id,ended FROM surveys WHERE id=?",new String[]{id})){
            assertTrue("Archived observation restored",c.moveToFirst());
            assertEquals(id,c.getString(0));
            assertEquals("2026-10-28T10:10:00Z",c.getString(1));
        }finally{restored.close();}
        assertTrue("Media must survive recovery",media.isFile());
        java.io.FileInputStream f=new java.io.FileInputStream(media);
        byte[] actual=new byte[bytes.length];int size=f.read(actual);f.close();
        assertEquals(bytes.length,size);
        assertArrayEquals(bytes,actual);
        assertFalse("Restored workspace must refuse another in-place restore",
            FieldBackupArchive.canRestore(ctx));
    }
}
