package org.phwildlife.survey;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Launch regression: 90 species have no independently dated status yet. */
@RunWith(AndroidJUnit4.class)
public final class Rc35DiscoveryNullStatusInstrumentedTest {
    private boolean contains(View root,String expected) {
        if(root instanceof TextView &&
            ((TextView)root).getText().toString().contains(expected))return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(contains(group.getChildAt(i),expected))return true;
        }
        return false;
    }
    @Test public void openingSpeciesDeckWithUnassessedTaxaDoesNotCrash() {
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        intent.putExtra("view","species");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue("Species cards rendered",contains(root,"Birds"));
                assertTrue("Conservation shelf rendered",contains(root,"Conservation"));
                assertTrue("Rarity is never inferred",contains(root,"Rarity unassessed"));
                assertTrue("All species remain discoverable",contains(root,"All species"));
                assertTrue("Screen has measurable layout",
                    root.getWidth()>0 && root.getHeight()>0);
            });
        }
    }
    @Test public void openingHabitatDeckRemainsIndependentOfThreatStatus() {
        Intent intent=new Intent(InstrumentationRegistry.getInstrumentation()
            .getTargetContext(),LikasDiscoveryActivity.class);
        intent.putExtra("view","habitats");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=ActivityScenario.launch(intent)){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue("Habitat shelf rendered",contains(root,"Terrestrial"));
                assertTrue("Full habitat directory accessible",contains(root,"All habitats"));
                assertFalse("No species threat tiers in habitat list",contains(root,"IUCN CR"));
            });
        }
    }
}
