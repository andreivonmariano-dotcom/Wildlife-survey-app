package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import static org.junit.Assert.*;

/** Read-only check after install -r replaces a versionCode122 app with code123. */
@RunWith(AndroidJUnit4.class)
public final class Rc63UpgradePreservationInstrumentedTest {
    private static String value(SQLiteDatabase db,String id,String col){
        try(Cursor c=db.rawQuery("SELECT "+col+" FROM surveys WHERE id=?",new String[]{id})){
            assertTrue("Original test event survives: "+id,c.moveToFirst());
            return c.isNull(0)?null:c.getString(0);
        }
    }
    private static int count(SQLiteDatabase db,String table,String where,String arg){
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+table+" WHERE "+where,
                new String[]{arg})){
            assertTrue(c.moveToFirst());
            return c.getInt(0);
        }
    }
    @Test public void originalHistoricalSurveysAndRecordsSurviveSignedUpgrade()
            throws Exception{
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        try(FieldDb helper=new FieldDb(context)){
            SQLiteDatabase db=helper.getReadableDatabase();
            assertEquals("SQLite schema must not be reset",27,db.getVersion());
            for(String id:new String[]{"QA_MODERN","QA_LEGACY","QA_ZERO",
                    "QA_INCOMPLETE","QA_NOT_COUNTED","QA_ACTIVE","QA_ANOMALY"}){
                assertEquals("Each original event preserved once: "+id,1,
                    count(db,"surveys","id=?",id));
            }
            assertEquals("Original complete outcome retained","complete",
                value(db,"QA_MODERN","outcome"));
            assertNull("Legacy missing outcome stays null",
                value(db,"QA_LEGACY","outcome"));
            assertEquals("Explicit incomplete retained","incomplete",
                value(db,"QA_INCOMPLETE","outcome"));
            assertEquals("Not Counted outcome must not be normalized",
                "not_counted",value(db,"QA_NOT_COUNTED","outcome"));
            assertNull("Active event remains unended",value(db,"QA_ACTIVE","ended"));
            assertEquals("Zero-detection effort retains zero raw records",0,
                count(db,"field_records","event_id=?","QA_ZERO"));
            assertEquals("Original detection retained after update",1,
                count(db,"field_records","event_id=?","QA_MODERN"));
            assertEquals("Excluded event raw observation retained",1,
                count(db,"field_records","event_id=?","QA_NOT_COUNTED"));
            assertEquals("Point-count effort preserved",1,
                count(db,"point_counts","event_id=?","QA_ZERO"));
            assertEquals("No field observations silently replaced",1,
                count(db,"field_records","event_id=?","QA_INCOMPLETE"));
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(ZipOutputStream archive=new ZipOutputStream(bytes)){
                MainActivity.writeResearchArchive(context,helper,archive);
            }
            boolean raw=false,excludedFromAnalysis=true;
            try(ZipInputStream archive=new ZipInputStream(
                    new java.io.ByteArrayInputStream(bytes.toByteArray()))){
                ZipEntry entry;byte[] buf=new byte[8192];int n;
                while((entry=archive.getNextEntry())!=null){
                    ByteArrayOutputStream value=new ByteArrayOutputStream();
                    while((n=archive.read(buf))!=-1)value.write(buf,0,n);
                    String csv=value.toString("UTF-8");
                    if("surveys.csv".equals(entry.getName()))
                        raw=csv.contains("\"QA_NOT_COUNTED\"");
                    if("sdm_occurrences.csv".equals(entry.getName()) &&
                            csv.contains("\"QA_NOT_COUNTED\""))
                        excludedFromAnalysis=false;
                    archive.closeEntry();
                }
            }
            assertTrue("Raw excluded history remains downloadable",raw);
            assertTrue("Not Counted observations excluded from model-ready SDM",
                excludedFromAnalysis);
        }
    }
}
