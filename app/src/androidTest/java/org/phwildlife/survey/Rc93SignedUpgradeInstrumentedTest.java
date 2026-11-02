package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import static org.junit.Assert.*;

/** Two separate instrumentations around a real, signed v152 -> v153 adb install -r. */
@RunWith(AndroidJUnit4.class)
public final class Rc93SignedUpgradeInstrumentedTest {
    static final String EXCLUDED="QA_RC92_UPGRADE_EXCLUDED";
    static final String ZERO="QA_RC92_UPGRADE_ZERO";
    static final String ACTIVE="QA_RC92_UPGRADE_ACTIVE";
    static final String LABEL="Unidentified bird, original field note";
    static final String AUDIO="clip_20261102_qa92.wav";
    static final String TIMESTAMP="2026-11-02T08:00:00Z";

    private static Context app(){
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    private static int version()throws Exception{
        Context ctx=app();
        return (int)ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),0)
            .getLongVersionCode();
    }
    private static String sha(byte[] bytes)throws Exception{
        byte[] d=MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder sb=new StringBuilder();
        for(byte b:d)sb.append(String.format(java.util.Locale.US,"%02x",b&255));
        return sb.toString();
    }
    private static byte[] tinyTestWav(){
        byte[] b=new byte[244];
        byte[] riff="RIFF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] wave="WAVEfmt ".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        System.arraycopy(riff,0,b,0,4);b[4]=(byte)236;
        System.arraycopy(wave,0,b,8,8);
        b[16]=16;b[20]=1;b[22]=1;
        b[24]=0x44;b[25]=(byte)0xac;
        b[28]=(byte)0x88;b[29]=0x58;b[30]=1;
        b[32]=2;b[34]=16;
        byte[] data="data".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        System.arraycopy(data,0,b,36,4);b[40]=(byte)200;
        return b;
    }
    private static void insert(SQLiteDatabase db,String id,String method,String outcome,Integer complete){
        ContentValues v=new ContentValues();
        v.put("id",id);v.put("method",method);v.put("started",TIMESTAMP);
        v.put("project_id","default");v.put("taxon_group","Birds");
        v.put("survey_profile","Birds");
        if(outcome!=null)v.put("outcome",outcome);
        if(complete!=null)v.put("complete",complete);
        if(outcome!=null)v.put("ended","2026-11-02T08:10:00Z");
        db.insertOrThrow("surveys",null,v);
    }
    private static int count(SQLiteDatabase db,String table,String where,String id){
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+table+" WHERE "+where+"=?",
                new String[]{id})){
            assertTrue(c.moveToFirst());return c.getInt(0);
        }
    }
    @Test public void aSeedSignedRc92Workspace() throws Exception{
        assertEquals("Seed must execute under original signed RC92",152,version());
        try(FieldDb helper=new FieldDb(app())){
            SQLiteDatabase db=helper.getWritableDatabase();
            assertEquals(29,db.getVersion());
            assertEquals(0,count(db,"surveys","id",EXCLUDED));
            db.beginTransaction();
            try{
                insert(db,EXCLUDED,"opportunistic","not_counted",0);
                insert(db,ZERO,"point_count","complete",1);
                insert(db,ACTIVE,"opportunistic",null,0);
                db.execSQL("INSERT INTO point_counts(id,event_id,station_id,sequence_number,"+
                    "started,ended,target_seconds,completed) VALUES(?,?,?,?,?,?,?,?)",
                    new Object[]{"QA_UPGRADE_POINT",ZERO,"QA_UPGRADE_STATION",1,TIMESTAMP,
                        "2026-11-02T08:10:00Z",600,1});
                db.execSQL("INSERT INTO field_records(event_id,detected_at,taxon_group,"+
                    "raw_identification,individual_count,detection_method,identification_level)"+
                    " VALUES(?,?,?,?,?,?,?)",
                    new Object[]{EXCLUDED,TIMESTAMP,"Birds",LABEL,1,
                        "visual","unidentified_or_unverified"});
                db.execSQL("INSERT INTO survey_outcome_audit(event_id,previous_outcome,"+
                    "new_outcome,changed_at_utc,reason,record_count) VALUES(?,?,?,?,?,?)",
                    new Object[]{EXCLUDED,"active","not_counted",TIMESTAMP,
                        "Excluded before upgrade for QA",1});
                db.execSQL("INSERT INTO acoustic_clips(id,event_id,taxon_group,method,"+
                    "filename,recorded_at_utc,duration_ms,sample_rate_hz,channels,format)"+
                    " VALUES(?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{"QA_UPGRADE_SOUND",EXCLUDED,"Birds","opportunistic",
                        AUDIO,TIMESTAMP,2,44100,1,"WAV_PCM16"});
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }
        File file=new File(AcousticEvidence.directory(app()),AUDIO);
        try(FileOutputStream out=new FileOutputStream(file)){out.write(tinyTestWav());}
        byte[] original=java.nio.file.Files.readAllBytes(file.toPath());
        app().getSharedPreferences("qa_upgrade_checks",0).edit()
            .putString("audio_sha",sha(original)).commit();
        assertEquals(244,file.length());
    }
    @Test public void bSignedRc93UpgradePreservesRowsMediaAndExport()throws Exception{
        assertEquals("Upgrade must install signed RC93 over RC92, no data reset",153,version());
        try(FieldDb helper=new FieldDb(app())){
            SQLiteDatabase db=helper.getReadableDatabase();
            assertEquals(29,db.getVersion());
            for(String id:new String[]{EXCLUDED,ZERO,ACTIVE}){
                assertEquals("One preserved survey "+id,1,count(db,"surveys","id",id));
            }
            try(Cursor c=db.rawQuery("SELECT outcome,ended FROM surveys WHERE id=?",
                    new String[]{ACTIVE})){
                assertTrue(c.moveToFirst());assertTrue(c.isNull(0)&&c.isNull(1));
            }
            assertEquals(1,count(db,"field_records","event_id",EXCLUDED));
            assertEquals(0,count(db,"field_records","event_id",ZERO));
            assertEquals(1,count(db,"point_counts","event_id",ZERO));
            assertEquals(1,count(db,"survey_outcome_audit","event_id",EXCLUDED));
            assertEquals(1,count(db,"acoustic_clips","event_id",EXCLUDED));
            try(Cursor c=db.rawQuery(
                "SELECT raw_identification FROM field_records WHERE event_id=?",
                new String[]{EXCLUDED})){
                assertTrue(c.moveToFirst());assertEquals(LABEL,c.getString(0));
            }
            File f=new File(AcousticEvidence.directory(app()),AUDIO);
            assertTrue(f.isFile());
            byte[] content=java.nio.file.Files.readAllBytes(f.toPath());
            assertEquals("Media bytes identical across signed upgrade",
                app().getSharedPreferences("qa_upgrade_checks",0)
                    .getString("audio_sha",""),sha(content));
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            try(ZipOutputStream zip=new ZipOutputStream(stream)){
                MainActivity.writeResearchArchive(app(),helper,zip);
            }
            Map<String,String> csv=new HashMap<>();
            try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(
                    stream.toByteArray()))){
                ZipEntry e;byte[] tmp=new byte[8192];int n;
                while((e=zip.getNextEntry())!=null){
                    ByteArrayOutputStream out=new ByteArrayOutputStream();
                    while((n=zip.read(tmp))!=-1)out.write(tmp,0,n);
                    if(e.getName().endsWith(".csv"))
                        csv.put(e.getName(),out.toString("UTF-8"));
                    zip.closeEntry();
                }
            }
            assertTrue(csv.get("surveys.csv").contains(EXCLUDED));
            assertTrue(csv.get("observations.csv").contains(LABEL));
            assertTrue(csv.get("survey_outcome_audit.csv").contains(EXCLUDED));
            assertTrue("Completed zero-detection effort exported",
                csv.get("occupancy_visits.csv").contains(ZERO));
            assertFalse("Excluded observations absent from SDM occurrences",
                csv.get("sdm_occurrences.csv").contains(LABEL));
        }
    }
}
