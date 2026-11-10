package org.phwildlife.survey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/** Android emulator integration contracts for the fully bundled RC110 offline owner gallery. */
@RunWith(AndroidJUnit4.class)
public final class Rc110OfflineGalleryInstrumentedTest {
    private static final String TAXON="Acerodon jubatus";

    private static JSONObject readManifest(TaxonDetailActivity a) throws Exception {
        try(InputStream stream=a.getAssets().open("offline_drive_gallery_manifest_v1.json");
            ByteArrayOutputStream data=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];
            int n;
            while((n=stream.read(buf))!=-1){
                if(data.size()+n>300000)throw new AssertionError("Gallery manifest too large");
                data.write(buf,0,n);
            }
            return new JSONObject(data.toString("UTF-8"));
        }
    }

    private static ActivityScenario<TaxonDetailActivity> openSpecies(){
        return ActivityScenario.launch(
            new Intent(ApplicationProvider.getApplicationContext(),TaxonDetailActivity.class)
                .putExtra("phlibrary_scientific_name",TAXON));
    }

    @Test public void all333ManifestEntriesHaveUniqueBundledPhotoAssets() throws Exception {
        try(ActivityScenario<TaxonDetailActivity> scenario=openSpecies()){
            scenario.onActivity(a -> {
                try {
                    JSONObject manifest=readManifest(a);
                    JSONArray items=manifest.getJSONArray("items");
                    assertEquals(333,manifest.getInt("count"));
                    assertEquals(333,items.length());
                    Set<String> assetNames=new HashSet<>();
                    int birds=0,herps=0,mammals=0;
                    for(int i=0;i<items.length();i++){
                        JSONObject item=items.getJSONObject(i);
                        String name=item.getString("offlineAsset");
                        assertTrue(name.matches("offline_photos/drive_[0-9]{4}\\.jpg"));
                        assertTrue("No duplicate packaged photograph",assetNames.add(name));
                        String group=item.getString("group");
                        if("Birds".equals(group))birds++;
                        else if("Reptiles and Amphibians".equals(group))herps++;
                        else if("Mammals".equals(group))mammals++;
                        else fail("Unexpected source group: "+group);
                    }
                    assertEquals(163,birds);assertEquals(149,herps);assertEquals(21,mammals);
                    for(String sample:new String[]{
                        "offline_photos/drive_0001.jpg",
                        "offline_photos/drive_0167.jpg",
                        "offline_photos/drive_0333.jpg"}){
                        try(InputStream stream=a.getAssets().open(sample)){
                            assertEquals(0xff,stream.read());
                            assertEquals(0xd8,stream.read());
                            assertEquals(0xff,stream.read());
                        }
                    }
                }catch(Exception ex){throw new AssertionError(ex);}
            });
        }
    }

    @Test public void albumCoverDecodesLocallyWithoutDriveAccess(){
        try(ActivityScenario<TaxonDetailActivity> scenario=openSpecies()){
            scenario.onActivity(a -> {
                assertTrue(PhLibraryDriveGallery.photoCount(a,TAXON)>1);
                ImageView cover=PhLibraryDriveGallery.coverForTaxon(a,TAXON);
                assertNotNull("Bundled gallery should render a photo, not a placeholder",cover);
                assertNotNull(cover.getDrawable());
                assertTrue(cover.getDrawable() instanceof BitmapDrawable);
                Bitmap image=((BitmapDrawable)cover.getDrawable()).getBitmap();
                assertNotNull(image);
                assertTrue(image.getWidth()>100);
                assertTrue(image.getHeight()>100);
                assertTrue(Math.max(image.getWidth(),image.getHeight())<=1920);
                assertTrue(cover.getContentDescription().toString().contains(TAXON));
                assertTrue(cover.getContentDescription().toString().contains("rights"));
            });
        }
    }

    @Test public void multiPhotoReaderSwipeAndPreviousRetainProvenance(){
        try(ActivityScenario<TaxonDetailActivity> scenario=openSpecies()){
            scenario.onActivity(a -> {
                int total=PhLibraryDriveGallery.photoCount(a,TAXON);
                assertTrue(total>1);
                LinearLayout parent=new LinearLayout(a);
                parent.setOrientation(LinearLayout.VERTICAL);
                PhLibraryDriveGallery.addReaderGallery(a,parent,TAXON);
                assertEquals(1,parent.getChildCount());
                LinearLayout album=(LinearLayout)parent.getChildAt(0);
                LinearLayout heading=(LinearLayout)album.getChildAt(0);
                TextView counter=(TextView)heading.getChildAt(1);
                FrameLayout frame=(FrameLayout)album.getChildAt(1);
                ImageView viewer=(ImageView)frame.getChildAt(0);
                assertNotNull(viewer.getDrawable());
                assertEquals("1 / "+total,counter.getText().toString());

                long t=SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,260f,80f,0);
                MotionEvent up=MotionEvent.obtain(t,t+120,MotionEvent.ACTION_UP,20f,82f,0);
                try {
                    assertTrue(viewer.dispatchTouchEvent(down));
                    assertTrue(viewer.dispatchTouchEvent(up));
                }finally{down.recycle();up.recycle();}
                assertEquals("2 / "+total,counter.getText().toString());
                assertNotNull("Swipe should decode the next image",viewer.getDrawable());
                LinearLayout controls=(LinearLayout)album.getChildAt(3);
                controls.getChildAt(0).performClick();
                assertEquals("1 / "+total,counter.getText().toString());
                TextView credits=(TextView)album.getChildAt(4);
                assertTrue(credits.getText().toString().contains("rights pending verification"));
            });
        }
    }
}
