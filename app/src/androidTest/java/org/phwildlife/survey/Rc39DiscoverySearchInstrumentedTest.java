package org.phwildlife.survey;

import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Android smoke: gallery queries change the actual rendered cards without network or DB writes. */
@RunWith(AndroidJUnit4.class)
public final class Rc39DiscoverySearchInstrumentedTest {
    private static View byDescription(View view,String label){
        if(label.equals(String.valueOf(view.getContentDescription())))return view;
        if(view instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)view;
            for(int i=0;i<parent.getChildCount();i++){
                View found=byDescription(parent.getChildAt(i),label);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static boolean hasText(View view,String text){
        if(view instanceof TextView &&
                ((TextView)view).getText().toString().contains(text))return true;
        if(view instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)view;
            for(int i=0;i<parent.getChildCount();i++)
                if(hasText(parent.getChildAt(i),text))return true;
        }
        return false;
    }
    @Test public void searchAndClearFindRealSpeciesAndKeepTheFullCatalog(){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        intent.putExtra("view","species");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                EditText search=(EditText)byDescription(root,
                    "Search illustrated offline species and habitat accounts");
                assertNotNull(search);
                search.setText("unlikely_species_match_9zz");
            });
            SystemClock.sleep(350);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue(hasText(root,"No matches"));
                assertTrue(hasText(root,"Full Philippine species checklist"));
                View clear=byDescription(root,"Clear library search");
                assertNotNull(clear);
                clear.performClick();
            });
            SystemClock.sleep(350);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertFalse(hasText(root,"No matches"));
                assertTrue(hasText(root,"Next Birds card") ||
                    byDescription(root,"Next Birds card")!=null);
            });
        }
    }
    @Test public void habitatSearchHasIndependentEmptyState(){
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        intent.putExtra("view","habitats");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                EditText search=(EditText)byDescription(root,
                    "Search illustrated offline species and habitat accounts");
                assertNotNull(search);
                search.setText("impossible_habitat_99z");
            });
            SystemClock.sleep(350);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue(hasText(root,"No matches"));
                assertTrue(hasText(root,"All habitats"));
                assertFalse(hasText(root,"IUCN CR"));
            });
        }
    }
}
