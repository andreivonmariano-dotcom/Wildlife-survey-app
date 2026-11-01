package org.phwildlife.survey;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

/** Offline 96-taxon and 15-habitat comparison UI test on isolated emulator. */
@RunWith(AndroidJUnit4.class)
public final class TraitComparisonInstrumentedTest {
    private static void walk(View root,ArrayList<View> found){
        found.add(root);
        if(root instanceof ViewGroup){
            ViewGroup v=(ViewGroup)root;
            for(int i=0;i<v.getChildCount();i++)walk(v.getChildAt(i),found);
        }
    }
    @Test public void testComparisonSwitchesAndSourceControls()throws Exception{
        try(ActivityScenario<Rc8TraitComparisonActivity> scenario=
                ActivityScenario.launch(Rc8TraitComparisonActivity.class)){
            scenario.onActivity(activity->{
                ArrayList<View> found=new ArrayList<>();
                walk(activity.getWindow().getDecorView(),found);
                ArrayList<Spinner> selectors=new ArrayList<>();
                Button habitat=null;
                for(View v:found){
                    if(v instanceof Spinner){
                        assertTrue("Every evidence filter needs accessible label",
                            v.getContentDescription()!=null&&v.getContentDescription().length()>0);
                        selectors.add((Spinner)v);
                    }
                    if(v instanceof Button){
                        Button button=(Button)v;
                        if(button.getText().toString().startsWith("Habitats (15)"))habitat=button;
                    }
                }
                assertEquals("Group, A, B, category",4,selectors.size());
                assertNotNull("Habitat comparison action",habitat);
                assertEquals("Four species wildlife groups",4,selectors.get(0).getAdapter().getCount());
                assertEquals("24 birds compared offline",24,selectors.get(1).getAdapter().getCount());
                habitat.performClick();
                assertEquals("Six habitat group filters",6,selectors.get(0).getAdapter().getCount());
                assertEquals("Fifteen offline habitat accounts",15,selectors.get(1).getAdapter().getCount());
                assertEquals("Six habitat comparison categories",6,selectors.get(3).getAdapter().getCount());
            });
        }
    }
}
