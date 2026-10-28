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
