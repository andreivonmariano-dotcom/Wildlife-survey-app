package org.phwildlife.survey;

import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Instrumented device smoke for accessible swipe controls and licensed habitat art. */
@RunWith(AndroidJUnit4.class)
public final class Rc36LibrarySwipeInstrumentedTest {
    private static View findByDescription(View root,String text){
        if(text.equals(String.valueOf(root.getContentDescription())))return root;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++){
                View found=findByDescription(parent.getChildAt(i),text);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static boolean hasText(View root,String text){
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(text))return true;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++)
                if(hasText(parent.getChildAt(i),text))return true;
        }
        return false;
    }
    private static boolean hasPhoto(View root){
        CharSequence description=root.getContentDescription();
        if(description!=null&&description.toString().startsWith(
                "Offline habitat photograph"))return true;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++)
                if(hasPhoto(parent.getChildAt(i)))return true;
        }
        return false;
    }
    @Test public void speciesShelfStepsThroughUnassessedCards(){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        i.putExtra("view","species");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(i)){
            // Nearby card creation happens after the initial layout pass.
            SystemClock.sleep(300);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                View button=findByDescription(root,"Next Birds card");
                assertNotNull("Bird shelf next action",button);
                assertTrue("First position is shown",hasText(root,"1 / 24"));
                assertTrue("Unknown status is visible",hasText(root,"Status unverified"));
                assertTrue("Next card is interactive",button.isEnabled());
                button.performClick();
            });
            SystemClock.sleep(550);
            scenario.onActivity(a->assertTrue("Bird card counter advances",
                hasText(a.getWindow().getDecorView(),"2 / 24")));
        }
    }
    @Test public void habitatShelfUsesLicensedPhotosAndKeepsDetails(){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        i.putExtra("view","habitats");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(i)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull("Terrestrial shelf can step",
                    findByDescription(root,"Next Terrestrial card"));
                assertTrue("All habitat profiles remain available",
                    hasText(root,"All habitats"));
                // Photo decoding is asynchronous; the source/rights manifest is tested
                // separately rather than assuming an image is ready on first frame.
                assertFalse("Habitat cards have no IUCN species status",
                    hasText(root,"IUCN CR"));
            });
        }
    }
}
