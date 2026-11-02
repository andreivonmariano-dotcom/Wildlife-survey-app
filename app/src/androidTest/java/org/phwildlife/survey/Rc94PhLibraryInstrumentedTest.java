package org.phwildlife.survey;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

/** Source-only UI acceptance: no survey, taxon or field-data records are written. */
@RunWith(AndroidJUnit4.class)
public final class Rc94PhLibraryInstrumentedTest {
    private static TextView text(View view,String title){
        if(view instanceof TextView &&
           title.equals(((TextView)view).getText().toString()))
            return (TextView)view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                TextView found=text(group.getChildAt(i),title);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static EditText entry(View view){
        if(view instanceof EditText)return (EditText)view;
        if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++){
                EditText found=entry(group.getChildAt(i));
                if(found!=null)return found;
            }
        }
        return null;
    }
    private static void saveShot(String label)throws Exception{
        Context ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Bitmap bitmap=InstrumentationRegistry.getInstrumentation()
            .getUiAutomation().takeScreenshot();
        assertNotNull("Android window screenshot",bitmap);
        File target=new File(ctx.getExternalFilesDir(null),label+".png");
        try(FileOutputStream file=new FileOutputStream(target)){
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,file));
        }finally{bitmap.recycle();}
        assertTrue(target.isFile()&&target.length()>12000);
    }
    @Test public void aHubShowsChecklistFirstAndCitedGuides()throws Exception{
        try(ActivityScenario<LikasLibraryActivity> scenario=
                ActivityScenario.launch(LikasLibraryActivity.class)){
            scenario.onActivity(activity->{
                View view=activity.getWindow().getDecorView();
                for(String target:new String[]{"PHLibrary","Search species checklist",
                        "Wildlife guides","Habitat guides","Conservation evidence",
                        "Photo comparisons","Source checklists"}){
                    TextView title=text(view,target);
                    assertNotNull("Missing reference destination: "+target,title);
                }
            });
            saveShot("rc94_phlibrary_hub");
        }
    }
    @Test public void bDiscoveryTabsKeepSeparateSearches()throws Exception{
        Context app=InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent open=new Intent(app,LikasDiscoveryActivity.class)
            .putExtra("view","species");
        try(ActivityScenario<LikasDiscoveryActivity> scenario=
                ActivityScenario.launch(open)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                EditText search=entry(root);
                assertNotNull(search);
                search.setText("Eagle");
                Button habitats=(Button)text(root,"Habitats");
                assertNotNull(habitats);
                habitats.performClick();
                assertEquals("",search.getText().toString());
                search.setText("forest");
                Button species=(Button)text(root,"Species");
                assertNotNull(species);
                species.performClick();
                assertEquals("Eagle",search.getText().toString());
                habitats.performClick();
                assertEquals("forest",search.getText().toString());
            });
            saveShot("rc94_phlibrary_guides");
        }
    }
}
