package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileOutputStream;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Disposable Android 14 emulator only. Tests the actual lifecycle dialogs,
 * stored zero-detection events, GPS provenance and closure audit trail.
 * Does not clear app data or inject fictitious biological observations.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class BioToolsLifecycleInstrumentedTest {
    private static final Context APP=InstrumentationRegistry.getInstrumentation().getTargetContext();

    private static Button button(View root,String title){
        if(root instanceof Button && title.equals(((Button)root).getText().toString()))
            return (Button)root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                Button candidate=button(group.getChildAt(i),title);
                if(candidate!=null)return candidate;
            }
        }
        return null;
    }
    private static String uniqueObserver(String name){
        return "BIOTOOLS-LIFE-"+name+"-"+SystemClock.elapsedRealtimeNanos();
    }
    private static Intent launch(String observer,String method,int plannedSeconds){
        Intent intent=new Intent(APP,MainActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("survey_profile","Birds");
        intent.putExtra("survey_method",method);
        intent.putExtra("observer_name",observer);
        intent.putExtra("start_without_gps",true);
        intent.putExtra("planned_duration_s",plannedSeconds);
        return intent;
    }
    private static void end(Activity a){
        Button end=button(a.getWindow().getDecorView(),"End Survey");
        assertNotNull("End Survey pinned in active recorder",end);
        assertTrue(end.isEnabled()&&end.isShown());
        end.performClick();
    }
    private static void capture(String file){
        Bitmap bmp=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull("Full system screen captured",bmp);
        File dir=new File(APP.getExternalFilesDir(null),"biotools_qa_lifecycle");
        assertTrue(dir.isDirectory()||dir.mkdirs());
        try(FileOutputStream stream=new FileOutputStream(new File(dir,file+".png"))){
            assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,95,stream));
        }catch(Exception e){throw new IllegalStateException(e);}
        finally{bmp.recycle();}
    }
    private static String id(String observer){
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT id FROM surveys WHERE observer_name=? ORDER BY started DESC LIMIT 1",
                new String[]{observer})){
            assertTrue("Real survey row saved",c.moveToFirst());return c.getString(0);
        }
    }
    private static void assertState(String id,String state,String reason){
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT outcome,complete,ended,COALESCE(exclusion_reason,''),"+
                "start_gps_latitude,start_gps_longitude,start_gps_accuracy_m,"+
                "COALESCE(start_gps_status,'') FROM surveys WHERE id=?",
                new String[]{id})){
            assertTrue("Survey retained after end",c.moveToFirst());
            assertEquals("Correct survey outcome",state,c.getString(0));
            assertEquals("Complete flag consistent", "complete".equals(state)?1:0,c.getInt(1));
            assertFalse("Archive has closure timestamp",c.isNull(2));
            assertEquals("Exclusion reason only for Not Counted",reason,c.getString(3));
            assertTrue("No synthetic start latitude",c.isNull(4));
            assertTrue("No synthetic start longitude",c.isNull(5));
            assertTrue("No synthetic start GPS accuracy",c.isNull(6));
            assertEquals("Explicit missing GPS flag",SurveyStartGpsPolicy.MISSING,c.getString(7));
        }
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT previous_outcome,new_outcome,COALESCE(reason,''),record_count "+
                "FROM survey_outcome_audit WHERE event_id=? ORDER BY id DESC LIMIT 1",
                new String[]{id})){
            assertTrue("Survey status change audited",c.moveToFirst());
            assertEquals("active",c.getString(0));
            assertEquals(state,c.getString(1));
            assertEquals("No fabricated detections",0,c.getInt(3));
            if("not_counted".equals(state))assertEquals(reason,c.getString(2));
            if("incomplete".equals(state))assertFalse("Incomplete reason recorded",
                c.getString(2).isEmpty());
        }
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM field_records WHERE event_id=? AND deleted_at IS NULL",
                new String[]{id})){
            assertTrue(c.moveToFirst());
            assertEquals("Zero detections not fabricated",0,c.getInt(0));
        }
    }

    @Test public void aEndIncompleteArchivesZeroDetectionAndAudit(){
        String observer=uniqueObserver("incomplete");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(
                launch(observer,"opportunistic",0))){
            scenario.onActivity(BioToolsLifecycleInstrumentedTest::end);
            onView(withText("End Incomplete")).perform(click());
            capture("end_incomplete_review");
            onView(withText("Save incomplete")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        assertState(id(observer),"incomplete","");
    }

    @Test public void bNotCountedRequiresExplanationAndRetainsAudit(){
        String observer=uniqueObserver("excluded");
        String reason="Rain stopped safe sampling";
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(
                launch(observer,"opportunistic",0))){
            scenario.onActivity(BioToolsLifecycleInstrumentedTest::end);
            onView(withText("Cancel Survey · Not Counted")).perform(click());
            capture("not_counted_reason");
            onView(withText("Mark NOT COUNTED")).perform(click());
            String event=id(observer);
            try(FieldDb db=new FieldDb(APP);
                Cursor c=db.getReadableDatabase().rawQuery(
                    "SELECT ended FROM surveys WHERE id=?",new String[]{event})){
                assertTrue(c.moveToFirst());
                assertTrue("Blank reason must not close survey",c.isNull(0));
            }
            onView(withHint("Why should this survey not be counted? (required)"))
                .perform(replaceText(reason),closeSoftKeyboard());
            onView(withText("Mark NOT COUNTED")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        String event=id(observer);
        assertState(event,"not_counted",reason);
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM surveys s WHERE s.id=? AND "+
                SurveyOutcomeSql.RESOLVED+"='complete'",new String[]{event})){
            assertTrue(c.moveToFirst());
            assertEquals("Excluded from complete-only analytical survey denominator",0,c.getInt(0));
        }
    }

    @Test public void cRequestedCompleteWithoutRequiredPointVisitBecomesIncomplete(){
        String observer=uniqueObserver("downgraded");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(
                launch(observer,"point_count",0))){
            scenario.onActivity(BioToolsLifecycleInstrumentedTest::end);
            onView(withText("Finish Complete")).perform(click());
            capture("point_count_completion_checklist");
            onView(withText("Confirm finish")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        assertState(id(observer),"incomplete","");
    }

    @Test public void dOpportunisticZeroDetectionCanCompleteWithoutInventingSpecies(){
        String observer=uniqueObserver("complete");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(
                launch(observer,"opportunistic",0))){
            scenario.onActivity(BioToolsLifecycleInstrumentedTest::end);
            onView(withText("Finish Complete")).perform(click());
            onView(withText("Confirm finish")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        assertState(id(observer),"complete","");
    }

    @Test public void eBackToHomeLeavesSurveyActiveAndResumable(){
        String observer=uniqueObserver("resumed");
        String before;
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(
                launch(observer,"opportunistic",0))){
            before=id(observer);
            scenario.onActivity(a->a.onBackPressed());
            onView(withText("Go Home")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT ended FROM surveys WHERE id=?",new String[]{before})){
            assertTrue(c.moveToFirst());assertTrue("Home leaves survey active",c.isNull(0));
        }
        try(ActivityScenario<MainActivity> resumed=ActivityScenario.launch(
                new Intent(APP,MainActivity.class).putExtra("resume",true))){
            resumed.onActivity(a->{
                Button end=button(a.getWindow().getDecorView(),"End Survey");
                assertNotNull("Resume kept End Survey",end);
                assertTrue(end.isShown()&&end.isEnabled());
                end.performClick();
            });
            onView(withText("End Incomplete")).perform(click());
            onView(withText("Save incomplete")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        assertEquals("Resume did not create a new survey",before,id(observer));
        assertState(before,"incomplete","");
    }
}
