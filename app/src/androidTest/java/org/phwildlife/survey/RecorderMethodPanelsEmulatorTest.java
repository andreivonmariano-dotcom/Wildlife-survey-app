package org.phwildlife.survey;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Disposable emulator only: confirm real responsive panels without creating detections. */
@RunWith(AndroidJUnit4.class)
public final class RecorderMethodPanelsEmulatorTest {
    private static View text(View root,String value){
        if(root instanceof TextView&&
            ((TextView)root).getText().toString().contains(value))return root;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++){
                View found=text(parent.getChildAt(i),value);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static int visibleExactLabels(View root,String value){
        int count=0;
        if(root instanceof TextView&&root.isShown()&&
            ((TextView)root).getText().toString().equals(value))count++;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                count+=visibleExactLabels(group.getChildAt(i),value);
        }
        return count;
    }
    private static View hint(View root,String value){
        if(root instanceof EditText&&
            ((EditText)root).getHint()!=null&&
            ((EditText)root).getHint().toString().contains(value))return root;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++){
                View found=hint(parent.getChildAt(i),value);
                if(found!=null)return found;
            }
        }
        return null;
    }
    /** Close only disposable emulator setup sessions, never used in production code. */
    private static void isolateEmulatorSession(){
        Context context=ApplicationProvider.getApplicationContext();
        try(FieldDb db=new FieldDb(context)){
            db.getWritableDatabase().execSQL(
                "UPDATE surveys SET ended=datetime('now'),complete=0 "+
                "WHERE ended IS NULL");
        }
    }
    private static Intent survey(String profile,String method){
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launch.putExtra("create_new",true);
        launch.putExtra("start_without_gps",true);
        launch.putExtra("survey_profile",profile);
        launch.putExtra("survey_method",method);
        launch.putExtra("project_id","default");
        return launch;
    }
    @Test public void amphibianListeningPromotesActualScoreAndNoiseFields(){
        isolateEmulatorSession();
        try(ActivityScenario<MainActivity> scene=ActivityScenario.launch(
            survey("Herpetofauna","amphibian_calls"))){
            scene.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                assertNotNull(text(root,"Amphibian listening · call index"));
                View call=text(root,"Amphibian call index (0–3)");
                View noise=text(root,"Background noise during amphibian listening");
                View micro=hint(root,"Microhabitat (stream edge");
                assertNotNull(call);assertNotNull(noise);assertNotNull(micro);
                assertTrue("Listening score must be presented by default",call.isShown());
                assertTrue("Noise must be presented by default",noise.isShown());
                assertFalse("Other microhabitat measurements remain in disclosure",
                    micro.isShown());
                View audio=text(root,"Record / review calls");
                assertNotNull(audio);assertTrue(audio.isShown());
                assertEquals("Only one visible call library action",1,
                    visibleExactLabels(root,"Record / review calls"));
                assertTrue(text(root,"Save Observation").isShown());
                assertTrue(text(root,"End Survey").isShown());
            });
        }
    }
    @Test public void vesPromotesMicrohabitatWithoutFalseCallMeasurement(){
        isolateEmulatorSession();
        try(ActivityScenario<MainActivity> scene=ActivityScenario.launch(
            survey("Herpetofauna","visual_encounter"))){
            scene.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                assertNotNull(text(root,"Visual encounter · microhabitat"));
                View micro=hint(root,"Microhabitat (stream edge");
                assertNotNull(micro);assertTrue(micro.isShown());
                View listening=text(root,"Background noise during amphibian listening");
                assertNotNull(listening);assertFalse(listening.isShown());
                assertTrue(text(root,"End Survey").isShown());
            });
        }
    }
    @Test public void cameraTrapEmphasizesEquipmentButLeavesOptionalHeight(){
        isolateEmulatorSession();
        try(ActivityScenario<MainActivity> scene=ActivityScenario.launch(
            survey("Mammals","camera_trap_check"))){
            scene.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                assertNotNull(text(root,"Camera trap · device ID"));
                View device=hint(root,"Equipment or trap ID");
                View height=hint(root,"Height above ground");
                assertNotNull(device);assertNotNull(height);
                assertTrue("Equipment ID is a primary device-specific field",device.isShown());
                assertFalse("Optional measurements must start collapsed",height.isShown());
                View checks=text(root,"Device checks & captures");
                assertNotNull(checks);assertTrue(checks.isShown());
                assertEquals("Only one visible device checks action",1,
                    visibleExactLabels(root,"Device checks & captures"));
                assertTrue(text(root,"End Survey").isShown());
            });
        }
    }
}
