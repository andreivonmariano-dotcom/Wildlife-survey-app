package org.phwildlife.survey;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Isolated emulator-only checks. No real user database or survey is cleared. */
@RunWith(AndroidJUnit4.class)
public final class Rc10LibraryScreensInstrumentedTest {
    private static void walk(View root,ArrayList<View> found){
        found.add(root);
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                walk(group.getChildAt(i),found);
        }
    }
    private static Button named(View root,String label){
        ArrayList<View> all=new ArrayList<>();
        walk(root,all);
        for(View view:all)
            if(view instanceof Button&&((Button)view).getText().toString().contains(label))
                return (Button)view;
        return null;
    }
    @Test public void libraryResearchPanelCanExpand()throws Exception{
        try(ActivityScenario<Rc7SpeciesLibraryActivity> test=
                ActivityScenario.launch(Rc7SpeciesLibraryActivity.class)){
            test.onActivity(activity->{
                Button toggle=named(activity.getWindow().getDecorView(),
                    "Research and comparison tools");
                assertNotNull("Research disclosure on species library",toggle);
                toggle.performClick();
                assertNotNull("Cited differential tool accessible after expansion",
                    named(activity.getWindow().getDecorView(),"Cited differential characters"));
                assertNotNull("Photo comparison tool accessible",
                    named(activity.getWindow().getDecorView(),"Compare offline wildlife photos"));
            });
        }
    }
    @Test public void habitatResearchPanelCanExpand()throws Exception{
        try(ActivityScenario<HabitatRecordsActivity> test=
                ActivityScenario.launch(HabitatRecordsActivity.class)){
            test.onActivity(activity->{
                Button toggle=named(activity.getWindow().getDecorView(),
                    "Habitat research tools");
                assertNotNull("Habitat reference disclosure",toggle);
                toggle.performClick();
                assertNotNull("Published source association remains reachable",
                    named(activity.getWindow().getDecorView(),"Published species"));
            });
        }
    }
    private static void requireVisibleUnclipped(View v,String label){
        assertNotNull(label+" control exists",v);
        android.graphics.Rect visible=new android.graphics.Rect();
        assertTrue(label+" visible in screen",v.getGlobalVisibleRect(visible));
        int[] position=new int[2];v.getLocationOnScreen(position);
        assertTrue(label+" height not clipped: visible="+visible.height()+
            " of "+v.getHeight()+"; y="+position[1]+" visible="+visible+
            " rootHeight="+v.getRootView().getHeight(),
            visible.height()>=v.getHeight()-2);
    }
    private static void capture(android.app.Activity activity,String name){
        View root=activity.getWindow().getDecorView();
        android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(root.getWidth(),
            root.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
        root.draw(new android.graphics.Canvas(image));
        try(java.io.FileOutputStream stream=new java.io.FileOutputStream(
                new java.io.File(activity.getFilesDir(),name))){
            assertTrue(image.compress(android.graphics.Bitmap.CompressFormat.PNG,90,stream));
        }catch(Exception error){throw new IllegalStateException(error);}
        image.recycle();
    }
    @Test public void largeTextSpeciesAndHabitatsPreserveFieldControls()throws Exception{
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .getUiAutomation().executeShellCommand(
                "settings put system font_scale 2.0").close();
        android.os.SystemClock.sleep(800);
        try{
            try(ActivityScenario<Rc7SpeciesLibraryActivity> test=
                    ActivityScenario.launch(Rc7SpeciesLibraryActivity.class)){
                test.onActivity(activity->{
                    assertTrue("Actual large-text configuration",
                        activity.getResources().getConfiguration().fontScale>=1.60f);
                    View root=activity.getWindow().getDecorView();
                    ArrayList<View> all=new ArrayList<>();
                    walk(root,all);
                    android.widget.EditText search=null;
                    android.widget.Spinner group=null;
                    for(View v:all){
                        if(search==null&&v instanceof android.widget.EditText)
                            search=(android.widget.EditText)v;
                        if(group==null&&v instanceof android.widget.Spinner)
                            group=(android.widget.Spinner)v;
                    }
                    requireVisibleUnclipped(search,"Species search");
                    requireVisibleUnclipped(group,"Species wildlife group");
                    requireVisibleUnclipped(named(root,"Research tools"),"Species tools");
                    assertNotNull("Research panel present for disclosure",
                        named(root,"Compare traits"));
                    capture(activity,"rc25_species_library_200percent.png");
                });
            }
            try(ActivityScenario<HabitatRecordsActivity> test=
                    ActivityScenario.launch(HabitatRecordsActivity.class)){
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                test.onActivity(activity->{
                    // Capture geometry even on failure, before first viewport assertions.
                    capture(activity,"rc27b_habitats_before_assert.png");
                    assertTrue("Actual large-text configuration",
                        activity.getResources().getConfiguration().fontScale>=1.60f);
                    View root=activity.getWindow().getDecorView();
                    requireVisibleUnclipped(named(root,"Guide"),"Habitat guide tab");
                    requireVisibleUnclipped(named(root,"My records"),"Site records tab");
                    int[] guidePos=new int[2],recordPos=new int[2];
                    named(root,"Guide").getLocationOnScreen(guidePos);
                    named(root,"My records").getLocationOnScreen(recordPos);
                    assertTrue("Both Habitat tabs must share a top edge",
                        Math.abs(guidePos[1]-recordPos[1])<=2);
                    requireVisibleUnclipped(named(root,"Research tools"),"Habitat tools");
                    Button toggle=named(root,"Research tools");
                    assertTrue(toggle.performClick());
                    assertNotNull("Research comparison reachable within scroll",
                        named(root,"Compare habitat traits"));
                    assertNotNull("Site reference link retained",
                        named(root,"Published species"));
                    capture(activity,"rc25_habitats_200percent.png");
                });
            }
        }finally{
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().executeShellCommand(
                    "settings put system font_scale 1.0").close();
            android.os.SystemClock.sleep(800);
        }
    }

    @Test public void largeTextHomeSetupAndHistoryHaveAccessibleActions()throws Exception{
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .getUiAutomation().executeShellCommand(
                "settings put system font_scale 2.0").close();
        android.os.SystemClock.sleep(800);
        try{
            android.content.Intent launch=new android.content.Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                WelcomeActivity.class);
            launch.putExtra("show_menu",true);
            try(ActivityScenario<WelcomeActivity> test=ActivityScenario.launch(launch)){
                test.onActivity(activity->{
                    View root=activity.getWindow().getDecorView();
                    assertTrue(activity.getResources().getConfiguration().fontScale>=1.60f);
                    ArrayList<View> all=new ArrayList<>();walk(root,all);
                    View birds=null;
                    for(View v:all)if(v.getContentDescription()!=null&&
                            v.getContentDescription().toString().startsWith(
                                "Birds, Counts and transects")){
                        birds=v;break;
                    }
                    requireVisibleUnclipped(birds,"Home Birds tile");
                    capture(activity,"rc26_home_200percent.png");
                    assertTrue("Home opens Birds setup",birds.performClick());
                });
                // The UI must complete a layout pass after navigation before geometry checks.
                androidx.test.platform.app.InstrumentationRegistry
                    .getInstrumentation().waitForIdleSync();
                test.onActivity(activity->{
                    View setup=activity.getWindow().getDecorView();
                    Button start=named(setup,"Continue to GPS");
                    requireVisibleUnclipped(start,"Survey setup GPS continuation");
                    assertTrue(start.getContentDescription().toString()
                        .contains("GPS preflight"));
                    ArrayList<View> form=new ArrayList<>();walk(setup,form);
                    boolean methodFound=false,projectFound=false;
                    for(View v:form){
                        if(v instanceof TextView&&
                                ((TextView)v).getText().toString().contains("Survey method"))
                            methodFound=true;
                        if(v instanceof android.widget.Spinner&&
                                "Choose survey project".equals(
                                    String.valueOf(v.getContentDescription())))
                            projectFound=true;
                    }
                    assertTrue("Method selection remains available",methodFound);
                    assertTrue("Project choice accessible",projectFound);
                    capture(activity,"rc26_survey_setup_200percent.png");
                });
            }
            try(ActivityScenario<HistoryActivity> history=
                    ActivityScenario.launch(HistoryActivity.class)){
                history.onActivity(activity->{
                    assertTrue(activity.getResources().getConfiguration().fontScale>=1.60f);
                    View root=activity.getWindow().getDecorView();
                    ArrayList<View> all=new ArrayList<>();walk(root,all);
                    View outcome=null,search=null;
                    for(View v:all){
                        if("Filter saved surveys by outcome".equals(
                                String.valueOf(v.getContentDescription())))outcome=v;
                        if("Search survey group, method, station or date".equals(
                                String.valueOf(v.getContentDescription())))search=v;
                    }
                    requireVisibleUnclipped(outcome,"Survey history outcome filter");
                    requireVisibleUnclipped(search,"Survey history search field");
                    capture(activity,"rc26_history_200percent.png");
                });
            }
        }finally{
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().executeShellCommand(
                    "settings put system font_scale 1.0").close();
            android.os.SystemClock.sleep(800);
        }
    }

    @Test public void offlineResearchCardsContainCitedEvidence()throws Exception{
        try(ActivityScenario<Rc7SpeciesLibraryActivity> test=
                ActivityScenario.launch(Rc7SpeciesLibraryActivity.class)){
            test.onActivity(activity->{
                for(String name:new String[]{
                    "Ansonia mcgregori","Limnonectes magnus",
                    "Acerodon jubatus","Carlito syrichta"}){
                    LinearLayout references=new LinearLayout(activity);
                    references.setOrientation(LinearLayout.VERTICAL);
                    Rc10ResearchBatch.add(activity,references,name);
                    assertNotNull("Cited research section for "+name,
                        named(references,"Read expanded field notes"));
                    named(references,"Read expanded field notes").performClick();
                    ArrayList<View> all=new ArrayList<>();
                    walk(references,all);
                    int citedSources=0;
                    for(View v:all)
                        if(v instanceof Button&&
                            ((Button)v).getText().toString().equals("Read original reference"))
                            citedSources++;
                    assertTrue("References must exist for "+name,citedSources>=4);
                }
            });
        }
    }
}
