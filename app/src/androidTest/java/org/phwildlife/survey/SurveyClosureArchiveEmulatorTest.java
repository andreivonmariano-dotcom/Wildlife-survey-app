package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Uses real app SQLite29 and full research ZIP; all rows are synthetic and test-only. */
@RunWith(AndroidJUnit4.class)
public final class SurveyClosureArchiveEmulatorTest {
    private static final String TIME="2026-11-02T08:00:00Z";
    private static String testId(String prefix){
        return "QA_RC92_"+prefix+"_"+UUID.randomUUID().toString().replace("-","");
    }
    private static void survey(SQLiteDatabase db,String id,String method,
            String profile,String outcome,int complete){
        db.execSQL("INSERT INTO surveys(id,method,started,ended,complete,"+
            "survey_profile,project_id,outcome,observer_effort_minutes) "+
            "VALUES(?,?,?,?,?,?,?,?,?)",
            new Object[]{id,method,TIME,"2026-11-02T08:15:00Z",
                complete,profile,"default",outcome,15d});
    }
    private static void detection(SQLiteDatabase db,String id,String label){
        db.execSQL("INSERT INTO field_records(event_id,detected_at,taxon_group,"+
            "raw_identification,individual_count,detection_method,identification_level) "+
            "VALUES(?,?,?,?,?,?,?)",
            new Object[]{id,"2026-11-02T08:07:00Z","Birds",label,1,
                "visual","unidentified_or_unverified"});
    }
    private static Map<String,String> archive(Context context,FieldDb helper)throws Exception{
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(buffer)){
            MainActivity.writeResearchArchive(context,helper,zip);
        }
        Map<String,String> entries=new HashMap<>();
        try(ZipInputStream zip=new ZipInputStream(
                new ByteArrayInputStream(buffer.toByteArray()))){
            ZipEntry entry;
            byte[] tmp=new byte[8192];
            while((entry=zip.getNextEntry())!=null){
                ByteArrayOutputStream data=new ByteArrayOutputStream();
                int n;
                while((n=zip.read(tmp))!=-1)data.write(tmp,0,n);
                assertFalse("Duplicate research entry: "+entry.getName(),
                    entries.containsKey(entry.getName()));
                entries.put(entry.getName(),
                    new String(data.toByteArray(),StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return entries;
    }
    @Test public void realSqliteAndResearchZipPreserveZeroDetectionVisitAndOutcomes()throws Exception{
        Context app=ApplicationProvider.getApplicationContext();
        String zero=testId("ZERO");
        String partial=testId("INCOMPLETE");
        String excluded=testId("NOTCOUNTED");
        String partialTaxon=testId("UNVERIFIED_PARTIAL");
        String excludedTaxon=testId("UNVERIFIED_EXCLUDED");
        try(FieldDb helper=new FieldDb(app)){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.beginTransaction();
            try{
                survey(db,zero,"point_count","Birds","complete",1);
                db.execSQL("INSERT INTO point_counts(id,event_id,station_id,sequence_number,"+
                    "started,ended,target_seconds,completed) VALUES(?,?,?,?,?,?,?,?)",
                    new Object[]{testId("POINT"),zero,"QA_TEST_STATION",1,TIME,
                        "2026-11-02T08:10:00Z",600,1});
                survey(db,partial,"point_count","Birds","incomplete",0);
                detection(db,partial,partialTaxon);
                survey(db,excluded,"opportunistic","Birds","not_counted",0);
                detection(db,excluded,excludedTaxon);
                db.execSQL("INSERT INTO survey_outcome_audit(event_id,previous_outcome,"+
                    "new_outcome,changed_at_utc,reason,record_count) VALUES(?,?,?,?,?,?)",
                    new Object[]{excluded,"active","not_counted",TIME,
                        "Test-only excluded from analyses",1});
                db.execSQL("INSERT INTO survey_effort_pauses(event_id,paused_at_utc,"+
                    "resumed_at_utc,duration_ms,reason) VALUES(?,?,?,?,?)",
                    new Object[]{partial,TIME,"2026-11-02T08:02:00Z",120000,
                        "Test-only interrupted effort"});
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
            Map<String,String> files=archive(app,helper);
            assertTrue(files.containsKey("surveys.csv"));
            assertTrue(files.containsKey("observations.csv"));
            assertTrue(files.containsKey("occupancy_visits.csv"));
            assertTrue(files.containsKey("sdm_occurrences.csv"));
            assertTrue(files.containsKey("survey_outcome_audit.csv"));
            assertTrue(files.containsKey("effort_pauses.csv"));
            String raw=files.get("surveys.csv");
            assertTrue(raw.contains(zero));
            assertTrue(raw.contains(partial));
            assertTrue(raw.contains(excluded));
            assertTrue("Raw archive retains excluded observations",
                files.get("observations.csv").contains(excludedTaxon));
            assertTrue("Raw archive retains incomplete observations",
                files.get("observations.csv").contains(partialTaxon));
            assertTrue("Zero-detection completed effort has its denominator",
                files.get("occupancy_visits.csv").contains(zero));
            assertFalse("Excluded surveys must not enter eligible visit analyses",
                files.get("occupancy_visits.csv").contains(excluded));
            assertFalse("Incomplete surveys must not enter complete-visit analyses",
                files.get("occupancy_visits.csv").contains(partial));
            assertFalse("No invented presence for a zero-detection visit",
                files.get("sdm_occurrences.csv").contains(zero));
            assertFalse("Excluded test detections cannot become SDM occurrences",
                files.get("sdm_occurrences.csv").contains(excludedTaxon));
            assertTrue(files.get("survey_outcome_audit.csv").contains(excluded));
            assertTrue(files.get("effort_pauses.csv").contains(partial));
            assertTrue(files.get("effort_pauses.csv").contains("120000"));
        }
    }
    @Test public void correctionAuditSurvivesExactResearchZip()throws Exception{
        Context app=ApplicationProvider.getApplicationContext();
        String surveyId=testId("CORRECTION");
        String label=testId("CORRECTED_IDENTIFICATION");
        String reason="Field note corrected following evidence review";
        try(FieldDb helper=new FieldDb(app)){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.beginTransaction();
            try{
                survey(db,surveyId,"opportunistic","Birds","complete",1);
                detection(db,surveyId,label);
                long record;
                try(Cursor c=db.rawQuery(
                    "SELECT id FROM field_records WHERE event_id=?",
                    new String[]{surveyId})){
                    assertTrue(c.moveToFirst());record=c.getLong(0);
                }
                db.execSQL("INSERT INTO record_edits(record_id,action,edited_at,"+
                    "previous_json,current_json,reason) VALUES(?,?,?,?,?,?)",
                    new Object[]{record,"edit",TIME,
                        "{\"raw_identification\":\"Undetermined\"}",
                        "{\"raw_identification\":\""+label+"\"}",reason});
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
            Map<String,String> zip=archive(app,helper);
            assertTrue(zip.get("observations.csv").contains(label));
            assertTrue(zip.get("edit_history.csv").contains(reason));
            assertTrue(zip.get("edit_history.csv").contains("Undetermined"));
            assertTrue(zip.get("edit_history.csv").contains(label));
        }
    }
    @Test public void existingClosurePolicyRetainsMeasuredEffortAndSeparateOutcomes(){
        assertEquals(SurveyClosePolicy.COMPLETE,SurveyClosePolicy.resolve(
            true,false,true,1,0,0,600,600000,
            false,false,null,false,null));
        assertEquals(SurveyClosePolicy.INCOMPLETE,SurveyClosePolicy.resolve(
            true,false,true,1,1,0,600,600000,
            false,false,null,false,null));
        assertEquals(SurveyClosePolicy.INCOMPLETE,SurveyClosePolicy.resolve(
            true,false,false,0,0,0,600,300000,
            false,false,null,false,null));
        assertEquals(SurveyClosePolicy.NOT_COUNTED,SurveyClosePolicy.resolve(
            true,true,false,0,0,0,600,300000,
            false,false,null,false,null));
        assertEquals(SurveyClosePolicy.INCOMPLETE,SurveyClosePolicy.resolve(
            true,false,false,0,0,0,0,80000,
            true,false,null,false,null));
        assertEquals(SurveyClosePolicy.COMPLETE,SurveyClosePolicy.resolve(
            true,false,false,0,0,0,0,80000,
            false,false,null,false,null));
        assertFalse(TrapProtocolPolicy.canComplete(2,1,2,0));
        assertTrue("Open camera traps can pass check criteria if checked",
            TrapProtocolPolicy.canComplete(2,0,2,0));
        assertFalse(TrapProtocolPolicy.canComplete(2,0,2,1));
    }
}
