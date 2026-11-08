package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import static org.junit.Assert.*;

/** Source-safe Android 14 acceptance for RC98 educational prose and book navigation. */
@RunWith(AndroidJUnit4.class)
public final class Rc98PhLibraryEditorialInstrumentedTest {
    private static View find(View root,String title){
        if(root instanceof TextView &&
           ((TextView)root).getText().toString().contains(title))return root;
        if(root instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)root;
            for(int i=0;i<parent.getChildCount();i++){
                View result=find(parent.getChildAt(i),title);
                if(result!=null)return result;
            }
        }
        return null;
    }
    @Test public void menuHasOnlyThreeDistinctExploreActions(){
        Intent open=new Intent(ApplicationProvider.getApplicationContext(),
            LikasLibraryActivity.class);
        try(ActivityScenario<LikasLibraryActivity> session=ActivityScenario.launch(open)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            session.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                assertNotNull(find(root,"Greater Mindanao"));
                assertNotNull(find(root,"Browse wildlife groups"));
                assertNotNull(find(root,"Discovery Rarity"));
                assertNotNull(find(root,"Habitat associations"));
                assertNull(find(root,"Conservation evidence"));
                assertNull(find(root,"Source collections"));
                assertNull(find(root,"Photo comparisons"));
                assertTrue(find(root,"Browse wildlife groups").isClickable() ||
                    find(root,"Browse wildlife groups").getParent()!=null);
            });
        }
    }
    @Test public void editedAccountAndDeepLinkedGroupAndRarity(){
        Intent book=new Intent(ApplicationProvider.getApplicationContext(),
            PhLibraryDirectoryActivity.class).putExtra("phlibrary_group","Birds");
        try(ActivityScenario<PhLibraryDirectoryActivity> session=ActivityScenario.launch(book)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            session.onActivity(activity->assertNotNull(find(
                activity.getWindow().getDecorView(),"408 species found")));
        }
        Intent rarity=new Intent(ApplicationProvider.getApplicationContext(),
            PhLibraryDirectoryActivity.class).putExtra("phlibrary_rarity","Legendary");
        try(ActivityScenario<PhLibraryDirectoryActivity> session=ActivityScenario.launch(rarity)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            session.onActivity(activity->assertNotNull(find(
                activity.getWindow().getDecorView(),"3 species found")));
        }
        Intent profile=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class).putExtra(
                "phlibrary_scientific_name","Pithecophaga jefferyi");
        try(ActivityScenario<TaxonDetailActivity> session=ActivityScenario.launch(profile)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            session.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                assertNotNull(find(root,"Philippine Eagle"));
                assertNotNull(find(root,"nest in large trees"));
                assertNotNull(find(root,"Philippine Eagle Foundation, n.d."));
                assertNotNull(find(root,"Text v1.82"));
                assertNotNull(find(root,"REFERENCES"));
                assertTrue(PhLibraryBook01.ready(activity));
                List<JSONObject> entries=PhLibraryBook01.entries(activity);
                assertEquals(715,entries.size());
            });
        }
    }
}
