package org.phwildlife.survey;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Real Android 14 screen traversal. Only use a disposable, initially empty emulator.
 * No database reset, no deletion of user projects, no mock GPS fixes.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class BioToolsNavigationInstrumentedTest {
    private static final Instrumentation INSTR =
        InstrumentationRegistry.getInstrumentation();
    private static final Context APP = INSTR.getTargetContext();

    private static void walk(View node,ArrayList<View> out) {
        out.add(node);
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++)walk(group.getChildAt(i),out);
        }
    }
    private static Button button(Activity a,String label){
        ArrayList<View> nodes=new ArrayList<>();
        walk(a.getWindow().getDecorView(),nodes);
        for(View v:nodes)if(v instanceof Button
                &&label.equals(((Button)v).getText().toString()))return (Button)v;
        throw new AssertionError("Button not found: "+label);
    }
    private static View described(Activity a,String label){
        ArrayList<View> nodes=new ArrayList<>();
        walk(a.getWindow().getDecorView(),nodes);
        for(View v:nodes)if(label.equals(String.valueOf(v.getContentDescription())))return v;
        throw new AssertionError("Accessible control not found: "+label);
    }
    private static boolean contains(Activity a,String text){
        ArrayList<View> nodes=new ArrayList<>();
        walk(a.getWindow().getDecorView(),nodes);
        for(View v:nodes)if(v instanceof TextView
                &&((TextView)v).getText().toString().contains(text))return true;
        return false;
    }
    private static void snapshot(Activity a,String label){
        View root=a.getWindow().getDecorView();
        assertTrue("screenshot view rendered",root.getWidth()>0&&root.getHeight()>0);
        File directory=new File(a.getExternalFilesDir(null),"biotools_qa");
        assertTrue("capture directory",directory.isDirectory()||directory.mkdirs());
        Bitmap b=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
                Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(b));
        try(FileOutputStream stream=new FileOutputStream(new File(directory,label+".png"))){
            assertTrue("PNG saved",b.compress(Bitmap.CompressFormat.PNG,95,stream));
        }catch(Exception e){throw new IllegalStateException(e);}
        finally{b.recycle();}
    }
    private static String fixture(String method,String profile,String outcome){
        String id="BIOTOOLS-QA-"+method+"-"+SystemClock.elapsedRealtimeNanos();
        try(FieldDb db=new FieldDb(APP)){
            ContentValues row=new ContentValues();
            row.put("id",id);row.put("method",method);
            row.put("taxon_group",profile.equals("Herpetofauna")?"Reptiles":profile);
            row.put("survey_profile",profile);
            row.put("project_id","default");
            row.put("started","2026-11-02T04:00:00Z");
            row.put("observer_name","BIOTOOLS-DISPOSABLE-EMULATOR");
            if(!"active".equals(outcome)){
                row.put("ended","2026-11-02T04:10:00Z");
                row.put("complete","complete".equals(outcome)?1:0);
                row.put("outcome",outcome);
            }
            db.getWritableDatabase().insertOrThrow("surveys",null,row);
        }
        return id;
    }
    private static String outcome(String event){
        try(FieldDb db=new FieldDb(APP);Cursor c=db.getReadableDatabase()
            .rawQuery("SELECT outcome FROM surveys WHERE id=?",new String[]{event})){
            assertTrue("survey retained",c.moveToFirst());
            return c.getString(0);
        }
    }
    private static boolean exists(String id){
        try(FieldDb db=new FieldDb(APP);Cursor c=db.getReadableDatabase()
            .rawQuery("SELECT COUNT(*) FROM surveys WHERE id=?",new String[]{id})){
            assertTrue(c.moveToFirst());return c.getInt(0)==1;
        }
    }


    @Test public void aWelcomeFieldworkSetupAndBackStayInCorrectHierarchy(){
        Intent home=new Intent(APP,WelcomeActivity.class).putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(home)){
            scenario.onActivity(a->{
                described(a,"Open Fieldwork. Surveys · Data · History").performClick();
                assertTrue(contains(a,"New survey"));
                described(a,"Open Birds. Counts · Transects").performClick();
                assertTrue("Bird setup opened",contains(a,"Point count duration (minutes)"));
                assertTrue(contains(a,"Continue to GPS"));
            });
            INSTR.waitForIdleSync();
            scenario.onActivity(a->{
                snapshot(a,"survey_setup_360dp");
                a.onBackPressed();
                assertTrue("Setup Back returns to Fieldwork",contains(a,"New survey"));
            });
            INSTR.waitForIdleSync();
            scenario.onActivity(a->{
                snapshot(a,"fieldwork_hub");
                a.onBackPressed();
                assertTrue("Fieldwork Back returns to Home",contains(a,"Fieldwork"));
            });
        }
    }

    @Test public void bAudioLibraryHeaderAndAndroidBackBothExitToCaller(){
        String id=fixture("amphibian_calls","Herpetofauna","incomplete");
        Intent launch=new Intent(APP,CallLibraryActivity.class).putExtra("event_id",id);
        try(ActivityScenario<CallLibraryActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Sound recordings"));
                snapshot(a,"audio_library");
                described(a,"Back to previous screen").performClick();
                assertTrue("header Back exits read-only audio library",a.isFinishing());
            });
        }
        try(ActivityScenario<CallLibraryActivity> scenario=ActivityScenario.launch(launch)){
            INSTR.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            INSTR.waitForIdleSync();
        }
        assertEquals("incomplete",outcome(id));
    }

    @Test public void historyHeaderAndAndroidBackReturnToSameSurveyList() {
        String id=fixture("point_count","Birds","incomplete");
        Intent launch=new Intent(APP,HistoryActivity.class).putExtra("event_id",id);
        try(ActivityScenario<HistoryActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Observations"));
                assertTrue(contains(a,"Incomplete"));
                snapshot(a,"history_observations");
                described(a,"Back to All surveys").performClick();
                assertTrue("header Back reached survey list",contains(a,"Survey history"));
                assertTrue("existing fixture still listed",contains(a,"Point count"));
                // Android needs a drawing frame after the list redraw.
            });
            INSTR.waitForIdleSync();
            scenario.onActivity(a->snapshot(a,"history_survey_list"));
        }
        try(ActivityScenario<HistoryActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->assertTrue(contains(a,"Observations")));
            INSTR.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            INSTR.waitForIdleSync();
            scenario.onActivity(a->assertTrue(
                "Android Back reached all surveys",contains(a,"Survey history")));
        }
        assertTrue(exists(id));
    }

    @Test public void recorderOwnedHistoryClosesToCallerInsteadOfAllSurveys(){
        String id=fixture("opportunistic","Birds","incomplete");
        Intent intent=new Intent(APP,HistoryActivity.class)
            .putExtra("event_id",id).putExtra("from_recorder",true);
        try(ActivityScenario<HistoryActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Observations"));
                described(a,"Back to Survey").performClick();
                assertTrue("header Back finishes recorder-owned detail",a.isFinishing());
            });
        }
        try(ActivityScenario<HistoryActivity> scenario=ActivityScenario.launch(intent)){
            INSTR.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            INSTR.waitForIdleSync();
            // Avoid dereferencing a destroyed ActivityScenario after Back.
        }
        assertEquals("incomplete",outcome(id));
    }

    @Test public void devicePickerShowsUndeployedSurveyAndBothBackRoutes(){
        String id=fixture("camera_trap_check","Mammals","incomplete");
        Intent launch=new Intent(APP,TrapProtocolActivity.class);
        try(ActivityScenario<TrapProtocolActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Device-method surveys"));
                assertTrue("survey with zero deployments visible",contains(a,"Camera-trap check"));
                snapshot(a,"device_survey_picker");
                Button candidate=null;
                ArrayList<View> nodes=new ArrayList<>();
                walk(a.getWindow().getDecorView(),nodes);
                for(View v:nodes)if(v instanceof Button&&
                   ((Button)v).getText().toString().contains("Camera-trap check")){
                   candidate=(Button)v;break;
                }
                assertNotNull("undeployed method selectable",candidate);
                candidate.performClick();
                assertTrue(contains(a,"No devices registered yet."));
                assertTrue(contains(a,id));
            });
            INSTR.waitForIdleSync();
            scenario.onActivity(a->{
                snapshot(a,"device_no_deployments");
                described(a,"Back to All device surveys").performClick();
                assertTrue(contains(a,"Device-method surveys"));
            });
        }
        try(ActivityScenario<TrapProtocolActivity> scenario=ActivityScenario.launch(
                new Intent(APP,TrapProtocolActivity.class).putExtra("event_id",id))){
            scenario.onActivity(a->{
                assertTrue(contains(a,"No devices registered yet."));
                described(a,"Back to previous screen").performClick();
                assertTrue("recorder-owned device screen finishes",a.isFinishing());
            });
        }
        try(ActivityScenario<TrapProtocolActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(a->{
                ArrayList<View> nodes=new ArrayList<>();
                walk(a.getWindow().getDecorView(),nodes);
                for(View v:nodes)if(v instanceof Button&&
                   ((Button)v).getText().toString().contains("Camera-trap check")){
                   v.performClick();return;
                }
                fail("No device survey found");
            });
            INSTR.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            INSTR.waitForIdleSync();
            scenario.onActivity(a->assertTrue("system Back returns to picker",
                contains(a,"Device-method surveys")));
        }
        assertEquals("incomplete",outcome(id));
    }

    @Test public void protocolSurveyPickerBackReturnsToPicker(){
        String id=fixture("coverboard_search","Herpetofauna","incomplete");
        try(ActivityScenario<FieldProtocolsActivity> scenario=
                ActivityScenario.launch(FieldProtocolsActivity.class)){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Open a completed or active survey"));
                ArrayList<View> nodes=new ArrayList<>();
                walk(a.getWindow().getDecorView(),nodes);
                Button choice=null;
                for(View v:nodes)if(v instanceof Button&&
                    ((Button)v).getText().toString().contains("coverboard")){
                    choice=(Button)v;break;
                }
                assertNotNull("fixture in method list",choice);
                choice.performClick();
                assertTrue(contains(a,"Coverboards"));
            });
            INSTR.waitForIdleSync();
            scenario.onActivity(a->{
                snapshot(a,"protocol_detail");
                described(a,"Back to All protocol surveys").performClick();
                assertTrue(contains(a,"Open a completed or active survey"));
            });
        }
        try(ActivityScenario<FieldProtocolsActivity> scenario=ActivityScenario.launch(
                new Intent(APP,FieldProtocolsActivity.class).putExtra("event_id",id))){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Coverboards"));
                described(a,"Back to previous screen").performClick();
                assertTrue(a.isFinishing());
            });
        }
        assertEquals("incomplete",outcome(id));
    }

    @Test public void stationProgressShowsSurveyVisitsWithoutProjectInventory(){
        String id=fixture("visual_encounter","Herpetofauna","incomplete");
        try(ActivityScenario<StationProgressActivity> scenario=ActivityScenario.launch(
                new Intent(APP,StationProgressActivity.class).putExtra("event_id",id))){
            scenario.onActivity(a->{
                assertTrue(contains(a,"Stations visited in this survey"));
                assertTrue(contains(a,"No station visits recorded in this survey yet."));
                assertFalse("unrelated stations omitted",
                    contains(a,"Other registered project stations"));
                snapshot(a,"station_progress_empty");
                described(a,"Back to previous screen").performClick();
                assertTrue(a.isFinishing());
            });
        }
        assertEquals("incomplete",outcome(id));
    }

    @Test public void zRecorderActionsAnd200PercentFontDoNotClipEndSurvey(){
        Intent intent=new Intent(APP,MainActivity.class)
            .putExtra("create_new",true).putExtra("survey_profile","Birds")
            .putExtra("survey_method","opportunistic")
            .putExtra("start_without_gps",true)
            .putExtra("observer_name","BIOTOOLS-NAV-RECORDER");
        try(android.os.ParcelFileDescriptor fd=INSTR.getUiAutomation()
                .executeShellCommand("settings put system font_scale 2.0")){}
        SystemClock.sleep(750);
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                Button end=button(a,"End Survey");
                assertTrue(end.isShown());
                assertTrue(end.isEnabled());
                assertNotNull(button(a,"Save Observation"));
                assertNotNull(button(a,"Review Records"));
                Rect bounds=new Rect();
                assertTrue(end.getGlobalVisibleRect(bounds));
                assertTrue("End Survey not clipped",bounds.height()>=end.getHeight()-2);
                snapshot(a,"recorder_360dp_200percent");
            });
        }finally{
            try(android.os.ParcelFileDescriptor fd=INSTR.getUiAutomation()
                    .executeShellCommand("settings put system font_scale 1.0")){}
        }
        // No synthetic observations deleted; leave any active fixture on the disposable image
        // for emulator log inspection, not as a production survey.
    }
}
