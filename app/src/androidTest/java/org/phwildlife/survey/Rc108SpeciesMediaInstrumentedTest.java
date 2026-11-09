package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.junit.Assert.*;

/** Source-safe native tests for the daily species, photo placeholder, and private import. */
@RunWith(AndroidJUnit4.class)
public final class Rc108SpeciesMediaInstrumentedTest {
    private static View tagged(View view,String tag){
        if(tag.equals(view.getTag()))return view;
        if(view instanceof ViewGroup){
            ViewGroup g=(ViewGroup)view;
            for(int i=0;i<g.getChildCount();i++){
                View found=tagged(g.getChildAt(i),tag);if(found!=null)return found;
            }
        }
        return null;
    }
    @Test public void dailyCardUsesPhotographedBookTaxonAndStableDate()throws Exception{
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),WelcomeActivity.class)
                .putExtra("show_menu",true))){
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                LocalDate today=LocalDate.now(ZoneId.of("Asia/Manila"));
                JSONObject chosen=ApoSpeciesOfDay.select(a,today);
                assertNotNull("At least one approved offline photographed taxon required",chosen);
                assertEquals(chosen.optString("s"),
                    ApoSpeciesOfDay.select(a,today).optString("s"));
                assertTrue(PhLibrarySpeciesMedia.hasApprovedPhoto(a,chosen.optString("s")));
                View daily=tagged(a.getWindow().getDecorView(),"apo_species_of_the_day");
                assertNotNull("APO Home must include a Species of the Day card",daily);
                String description=daily.getContentDescription().toString();
                assertTrue(description.contains(chosen.optString("s")));
                assertTrue(description.contains("IUCN global status"));
                assertTrue(description.contains("Endemicity"));
            });
        }
    }
    @Test public void missingPhotoHasNeutralQuestionMarkAndExplicitProvenance()throws Exception{
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),TaxonDetailActivity.class)
                .putExtra("phlibrary_scientific_name","Aviceda jerdoni"))){
            scenario.onActivity(a->{
                assertTrue(PhLibraryBook01.has(a,"Aviceda jerdoni"));
                assertFalse(PhLibrarySpeciesMedia.hasApprovedPhoto(a,"Aviceda jerdoni"));
                View blank=PhLibrarySpeciesMedia.photoOrPlaceholder(a,"Aviceda jerdoni");
                assertTrue(blank instanceof FrameLayout);
                assertTrue(blank.getContentDescription().toString().contains("No photograph"));
                assertEquals("?",((TextView)((FrameLayout)blank).getChildAt(0))
                    .getText().toString());
            });
        }
        assertEquals("Philippine endemic",ApoSpeciesOfDay.endemicity(
            new JSONObject().put("b","◆ Philippine endemic     •     ◉ IUCN LC")));
        assertEquals("Not specified",ApoSpeciesOfDay.endemicity(
            new JSONObject().put("b","↝ Migratory     •     ◉ IUCN LC")));
    }
    @Test public void userPhotoCanBeCopiedPrivatelyAndRemovedWithoutChangingSource()throws Exception{
        try(ActivityScenario<TaxonDetailActivity> scenario=ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),TaxonDetailActivity.class)
                .putExtra("phlibrary_scientific_name","Aviceda jerdoni"))){
            scenario.onActivity(a->{
                String scientific="Aviceda jerdoni";
                File origin=new File(a.getCacheDir(),"rc108_owner_photo_test.jpg");
                Bitmap photo=Bitmap.createBitmap(80,60,Bitmap.Config.RGB_565);
                try(FileOutputStream out=new FileOutputStream(origin)){
                    assertTrue(photo.compress(Bitmap.CompressFormat.JPEG,90,out));
                }catch(Exception ex){throw new AssertionError(ex);}
                finally{photo.recycle();}
                long originalSize=origin.length();
                try{
                    PhLibrarySpeciesMedia.importPrivate(a,scientific,Uri.fromFile(origin));
                    assertTrue(PhLibrarySpeciesMedia.hasPersonalPhoto(a,scientific));
                    assertTrue(PhLibrarySpeciesMedia.removePrivate(a,scientific));
                    assertFalse(PhLibrarySpeciesMedia.hasPersonalPhoto(a,scientific));
                    assertEquals("Source photograph must remain unchanged",
                        originalSize,origin.length());
                }catch(Exception ex){throw new AssertionError(ex);}
                origin.delete();
            });
        }
    }
}
