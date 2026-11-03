package org.phwildlife.survey;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Disposable Android 14 route acceptance; no occurrence, GPS or survey outcome is inferred. */
@RunWith(AndroidJUnit4.class)
public final class Rc95BioToolsNavigationInstrumentedTest {
    private static Context app(){
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    private static String fixture(String method,String group){
        String id="RC95-NAV-"+method+"-"+SystemClock.elapsedRealtimeNanos();
        try(FieldDb db=new FieldDb(app())){
            ContentValues row=new ContentValues();
            row.put("id",id);
            row.put("method",method);
            row.put("taxon_group",group);
            row.put("survey_profile",group);
            row.put("project_id","default");
            row.put("started","2026-11-03T00:00:00Z");
            row.put("ended","2026-11-03T00:15:00Z");
            row.put("complete",0);
            row.put("outcome","incomplete");
            db.getWritableDatabase().insertOrThrow("surveys",null,row);
        }
        return id;
    }
    private static void cleanup(String id){
        try(FieldDb db=new FieldDb(app())){
            db.getWritableDatabase().delete("surveys","id=?",new String[]{id});
        }
    }
    private static boolean hasText(View root,String target){
        if(root instanceof TextView&&
             ((TextView)root).getText().toString().contains(target))return true;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++)
                if(hasText(g.getChildAt(i),target))return true;
        }
        return false;
    }
    private static View described(View root,String text){
        if(text.equals(String.valueOf(root.getContentDescription())))return root;
        if(root instanceof ViewGroup){
            ViewGroup g=(ViewGroup)root;
            for(int i=0;i<g.getChildCount();i++){
                View found=described(g.getChildAt(i),text);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void tapBack(Activity activity,String destination){
        View back=described(activity.getWindow().getDecorView(),"Back to "+destination);
        assertNotNull("Expected Back to "+destination,back);
        back.performClick();
    }
    @Test public void aRecorderReviewBackClosesToCaller() {
        String event=fixture("opportunistic","Birds");
        try{
            Intent intent=new Intent(app(),HistoryActivity.class)
                .putExtra("event_id",event).putExtra("navigation_origin","recorder");
            try(ActivityScenario<HistoryActivity> screen=ActivityScenario.launch(intent)){
                screen.onActivity(a->{
                    assertTrue(hasText(a.getWindow().getDecorView(),"Observations"));
                    tapBack(a,"Live survey");
                    assertTrue("Recorder-owned review should finish",a.isFinishing());
                });
            }
        }finally{cleanup(event);}
    }
    @Test public void bArchivedHistoryBackReturnsToAllSurveys(){
        String event=fixture("point_count","Birds");
        try{
            Intent intent=new Intent(app(),HistoryActivity.class).putExtra("event_id",event);
            try(ActivityScenario<HistoryActivity> screen=ActivityScenario.launch(intent)){
                screen.onActivity(a->{
                    tapBack(a,"All surveys");
                    assertFalse("Archived detail remains in History",a.isFinishing());
                    View root=a.getWindow().getDecorView();
                    assertTrue("Back restored list title",
                        hasText(root,"Survey history")||hasText(root,"History"));
                });
            }
        }finally{cleanup(event);}
    }
    @Test public void cEmptyDeviceSurveyStaysDiscoverable(){
        String event=fixture("camera_trap_check","Mammals");
        try{
            try(ActivityScenario<TrapProtocolActivity> screen=
                    ActivityScenario.launch(TrapProtocolActivity.class)){
                screen.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    assertTrue("Device-method event exists before deployment",
                        hasText(root,"Survey "+event));
                    assertTrue(hasText(root,"Camera")||hasText(root,"camera"));
                    tapBack(a,"Field Utilities");
                    assertTrue(a.isFinishing());
                });
            }
        }finally{cleanup(event);}
    }
    @Test public void dProgressExcludesUnrelatedProjectSites(){
        String event=fixture("point_count","Birds");
        try{
            Intent intent=new Intent(app(),StationProgressActivity.class)
                .putExtra("event_id",event).putExtra("navigation_origin","history");
            try(ActivityScenario<StationProgressActivity> screen=
                    ActivityScenario.launch(intent)){
                screen.onActivity(a->{
                    View root=a.getWindow().getDecorView();
                    assertTrue(hasText(root,"No visits recorded yet"));
                    assertFalse(hasText(root,"Project stations without a recorded visit"));
                    tapBack(a,"Observations");
                    assertTrue(a.isFinishing());
                });
            }
        }finally{cleanup(event);}
    }
    @Test public void eProtocolAndAudioBackFollowHistoryCaller(){
        String event=fixture("area_search","Mammals");
        try{
            Intent protocol=new Intent(app(),FieldProtocolsActivity.class)
                .putExtra("event_id",event).putExtra("navigation_origin","history");
            try(ActivityScenario<FieldProtocolsActivity> screen=
                    ActivityScenario.launch(protocol)){
                screen.onActivity(a->{
                    tapBack(a,"Observations");
                    assertTrue(a.isFinishing());
                });
            }
            Intent sound=new Intent(app(),CallLibraryActivity.class)
                .putExtra("event_id",event).putExtra("navigation_origin","history");
            try(ActivityScenario<CallLibraryActivity> screen=
                    ActivityScenario.launch(sound)){
                screen.onActivity(a->{
                    tapBack(a,"Observations");
                    assertTrue(a.isFinishing());
                });
            }
        }finally{cleanup(event);}
    }
}
