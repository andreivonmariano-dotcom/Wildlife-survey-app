package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * RC110 visual evidence, NOT app features. Runs only on disposable Android emulator.
 * Screenshots are actual native View trees, not mockups, stored in external app files.
 */
@RunWith(AndroidJUnit4.class)
public final class Rc110VisualAuditInstrumentedTest {
    private static String preset(){
        return InstrumentationRegistry.getArguments().getString("preset","p360f100");
    }
    private static void shot(Activity activity,String slug){
        View root=activity.getWindow().getDecorView();
        assertTrue("Layout must finish before audit capture",root.getWidth()>10&&root.getHeight()>10);
        Bitmap image=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        try {
            root.draw(new Canvas(image));
            File parent=new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "RC110_UI_AUDIT");
            assertTrue(parent.isDirectory()||parent.mkdirs());
            File dest=new File(parent,preset()+"-"+slug+".png");
            try(FileOutputStream output=new FileOutputStream(dest)){
                assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,output));
            }catch(Exception e){throw new AssertionError(e);}
            assertTrue("Screenshot was empty: "+slug,dest.length()>1000);
        }finally{image.recycle();}
    }
    private static ScrollView scroll(View root){
        if(root instanceof ScrollView)return (ScrollView)root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                ScrollView child=scroll(group.getChildAt(i));
                if(child!=null)return child;
            }
        }
        return null;
    }
    private static void at(Activity a,int fraction){
        ScrollView view=scroll(a.getWindow().getDecorView());
        if(view==null)return;
        int max=Math.max(0,view.getChildAt(0).getHeight()-view.getHeight());
        view.scrollTo(0,max*fraction/100);
    }
    private static void settle(){
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(250);
    }
    private static void capture(ActivityScenario<? extends Activity> scenario,String slug,int fraction){
        scenario.onActivity(a->at(a,fraction));
        settle();
        scenario.onActivity(a->shot(a,slug));
    }
    private static void screen(ActivityScenario<WelcomeActivity> scenario,String method,
                               String name,boolean mid,boolean bottom){
        scenario.onActivity(a->{
            try {
                Method destination=a.getClass().getDeclaredMethod(method);
                destination.setAccessible(true);
                destination.invoke(a);
            }catch(Exception e){throw new AssertionError("Unable to open "+method,e);}
        });
        settle();
        capture(scenario,name+"-top",0);
        if(mid)capture(scenario,name+"-middle",50);
        if(bottom)capture(scenario,name+"-bottom",100);
    }
    private static void surveySetup(ActivityScenario<WelcomeActivity> scenario,String group,
                                    boolean extra){
        scenario.onActivity(a->{
            try {
                Method setup=a.getClass().getDeclaredMethod("setup",String.class);
                setup.setAccessible(true);
                setup.invoke(a,group);
            }catch(Exception e){throw new AssertionError("Cannot open "+group+" setup",e);}
        });
        settle();
        String slug="setup-"+group.toLowerCase(java.util.Locale.ROOT);
        capture(scenario,slug+"-top",0);
        if(extra)capture(scenario,slug+"-middle",50);
        capture(scenario,slug+"-bottom",100);
    }
    @Test public void aPortalAndSurveySetupVisualAudit(){
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),WelcomeActivity.class)
            .putExtra("show_menu",true);
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(launch)){
            settle();
            capture(scenario,"apo-home-top",0);
            capture(scenario,"apo-home-bottom",100);
            screen(scenario,"myDataHub","my-data",true,true);
            screen(scenario,"fieldworkHub","surveys",false,true);
            
            screen(scenario,"analysisHub","analysis-exports",false,false);
            screen(scenario,"fieldUtilitiesHub","field-utilities",false,true);
            
            
            surveySetup(scenario,"Birds",true);
            surveySetup(scenario,"Herpetofauna",true);
            surveySetup(scenario,"Mammals",true);
        }
    }
    @Test public void bSettingsVisualAudit(){
        try(ActivityScenario<LikasSettingsActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),LikasSettingsActivity.class))){
            settle();
            capture(scenario,"settings-top",0);
            capture(scenario,"settings-middle",50);
            capture(scenario,"settings-bottom",100);
        }
    }
    @Test public void cRecorderVisualAudit(){
        String group=InstrumentationRegistry.getArguments().getString("profile","Birds");
        assertTrue(group,group.equals("Birds")||group.equals("Herpetofauna")||
            group.equals("Mammals"));
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        intent.putExtra("create_new",true);
        intent.putExtra("survey_profile",group);
        intent.putExtra("survey_method","opportunistic");
        intent.putExtra("start_without_gps",true);
        intent.putExtra("project_id","default");
        intent.putExtra("observer_name","RC110-EMULATOR-AUDIT-ONLY");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(intent)){
            settle();
            String key="recorder-"+group.toLowerCase(java.util.Locale.ROOT);
            scenario.onActivity(a->{
                assertTrue("Recorder must contain End Survey button",
                    hasText(a.getWindow().getDecorView(),"End Survey"));
            });
            capture(scenario,key+"-top",0);
            capture(scenario,key+"-middle",50);
            capture(scenario,key+"-bottom",100);
        }
    }
    private static boolean hasText(View v,String target){
        if(v instanceof TextView&&target.equals(((TextView)v).getText().toString()))return true;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++)
                if(hasText(g.getChildAt(i),target))return true;
        }
        return false;
    }
}
