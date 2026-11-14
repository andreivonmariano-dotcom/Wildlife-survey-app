package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** One directory and one source-scoped taxonomic exploration route. */
@RunWith(AndroidJUnit4.class)
public final class ApoAtlasConnectionsInstrumentedTest {
    private static final String SPECIES="Pithecophaga jefferyi";

    private static boolean hasText(View root,String value) {
        if(root instanceof TextView&&
                ((TextView)root).getText().toString().contains(value))return true;
        if(root instanceof ViewGroup){
            ViewGroup container=(ViewGroup)root;
            for(int i=0;i<container.getChildCount();i++)
                if(hasText(container.getChildAt(i),value))return true;
        }
        return false;
    }
    private static void capture(Activity a,String name){
        View root=a.getWindow().getDecorView();
        assertTrue(root.getWidth()>50&&root.getHeight()>50);
        Bitmap b=Bitmap.createBitmap(root.getWidth(),root.getHeight(),
            Bitmap.Config.ARGB_8888);
        try {
            root.draw(new Canvas(b));
            File directory=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "APO_SHELL_REVIEW");
            assertTrue(directory.isDirectory()||directory.mkdirs());
            String preset=InstrumentationRegistry.getArguments().getString("preset","p360f100");
            File output=new File(directory,preset+"-"+name+".png");
            try(FileOutputStream file=new FileOutputStream(output)){
                assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,file));
            }catch(Exception exception){throw new AssertionError(exception);}
            assertTrue("Empty Atlas screenshot",output.length()>2500);
        }finally{b.recycle();}
    }

    @Test public void canonicalBookAccountHasContextualTaxonomyRoute(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            TaxonDetailActivity.class);
        intent.putExtra("phlibrary_scientific_name",SPECIES);
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(150);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue("Book 01 detail unavailable",hasText(root,SPECIES));
                assertTrue("Missing contextual taxonomy link",
                    hasText(root,"Taxonomic connections"));
                capture(a,"canonical-species-account");
            });
        }
    }

    @Test public void contextualExplorerShowsSelectedSourceFamily(){
        Intent intent=new Intent(ApplicationProvider.getApplicationContext(),
            LivingAtlasExploreActivity.class);
        intent.putExtra(LivingAtlasExploreActivity.EXTRA_SCIENTIFIC_NAME,SPECIES);
        try(ActivityScenario<LivingAtlasExploreActivity> scenario=ActivityScenario.launch(intent)){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            SystemClock.sleep(150);
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertTrue("Wrong selected taxon",hasText(root,SPECIES));
                assertTrue("No family connections",hasText(root,"Taxonomic connections"));
                assertTrue("No evidence-scoped habitat context",hasText(root,"Habitat context"));
                assertTrue("Reviewed relation metadata missing",
                    hasText(root,"Reviewed habitat links:"));
                capture(a,"species-taxonomic-context");
            });
        }
    }
}
