package org.phwildlife.survey;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Provenance-preserving, airplane-mode-compatible map acceptance. */
@RunWith(AndroidJUnit4.class)
public final class ApoOfflineMapInstrumentedTest {
    private static final String SITE="APO-MAP-TEST-SITE";
    private static final String EVENT="APO-MAP-TEST-SURVEY";

    private static boolean hasText(View root,String needle){
        if(root instanceof TextView&&
            ((TextView)root).getText().toString().contains(needle))return true;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                if(hasText(group.getChildAt(i),needle))return true;
        }
        return false;
    }
    private static View labelled(View root,String needle){
        if(root instanceof TextView&&needle.equals(
            ((TextView)root).getText().toString()))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View v=labelled(group.getChildAt(i),needle);
                if(v!=null)return v;
            }
        }
        return null;
    }
    private static View described(View root,String label){
        if(label.equals(String.valueOf(root.getContentDescription())))return root;
        if(root instanceof ViewGroup){
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++){
                View child=described(group.getChildAt(i),label);
                if(child!=null)return child;
            }
        }
        return null;
    }
    private static void seed(){
        try(FieldDb helper=new FieldDb(ApplicationProvider.getApplicationContext())){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.delete("sites","id=?",new String[]{SITE});
            db.delete("surveys","id=?",new String[]{EVENT});
            ContentValues site=new ContentValues();
            site.put("id",SITE);site.put("project_id","default");
            site.put("name","Apo map fixture site");
            site.put("latitude",7.204);site.put("longitude",125.470);
            site.put("created_at","2026-11-14T10:00:00Z");
            db.insertOrThrow("sites",null,site);
            ContentValues survey=new ContentValues();
            survey.put("id",EVENT);
            survey.put("survey_profile","Birds");
            survey.put("taxon_group","Birds");
            survey.put("method","opportunistic");
            survey.put("started","2026-11-14T10:05:00Z");
            survey.put("ended","2026-11-14T11:05:00Z");
            survey.put("start_gps_latitude",7.205);
            survey.put("start_gps_longitude",125.471);
            survey.put("start_gps_status","gps_fix");
            db.insertOrThrow("surveys",null,survey);
        }
    }
    private static void cleanup(){
        try(FieldDb helper=new FieldDb(ApplicationProvider.getApplicationContext())){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.delete("surveys","id=?",new String[]{EVENT});
            db.delete("sites","id=?",new String[]{SITE});
        }
    }
    private static void snapshot(ApoOfflineMapActivity a,String page){
        View root=a.getWindow().getDecorView();
        Bitmap b=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);
        try{
            root.draw(new Canvas(b));
            File dir=new File(a.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "APO_SHELL_REVIEW");
            assertTrue(dir.isDirectory()||dir.mkdirs());
            String preset=InstrumentationRegistry.getArguments().getString(
                "preset","p360f100");
            File file=new File(dir,preset+"-"+page+".png");
            try(FileOutputStream output=new FileOutputStream(file)){
                assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,output));
            }catch(Exception e){throw new AssertionError(e);}
            assertTrue(file.length()>2500);
        }finally{b.recycle();}
    }
    private static void awaitText(ActivityScenario<ApoOfflineMapActivity> screen,
            String value){
        for(int tries=0;tries<65;tries++){
            AtomicBoolean found=new AtomicBoolean();
            screen.onActivity(a->found.set(hasText(a.getWindow().getDecorView(),value)));
            if(found.get())return;
            SystemClock.sleep(150);
        }
        fail("Offline map never displayed "+value);
    }
    @Test public void bundledProvinceMapAndStoredFixes(){
        seed();
        try(ActivityScenario<ApoOfflineMapActivity> screen=ActivityScenario.launch(
            ApoOfflineMapActivity.class)){
            awaitText(screen,"2 saved coordinates in view");
            screen.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),"2026 Q3"));
                assertTrue(hasText(a.getWindow().getDecorView(),
                    "2026 Q3 provinces"));
                assertTrue(hasText(a.getWindow().getDecorView(),"Offline Map"));
                assertNotNull(labelled(a.getWindow().getDecorView(),"Mindanao"));
                snapshot(a,"offline-map-philippines");
                View button=labelled(a.getWindow().getDecorView(),"Mindanao");
                assertNotNull(button);button.performClick();
            });
            awaitText(screen,"Mindanao view");
            screen.onActivity(a->{
                View map=described(a.getWindow().getDecorView(),
                    "Offline provincial reference map with saved survey and site coordinates");
                assertNotNull(map);
                float x=(float)(16+(125.470-120.0)/8.0*(map.getWidth()-32));
                float y=(float)(15+(11.3-7.204)/(11.3-4.5)*(map.getHeight()-30));
                long now=SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(now,now,
                    MotionEvent.ACTION_DOWN,x,y,0);
                MotionEvent up=MotionEvent.obtain(now,now+20,
                    MotionEvent.ACTION_UP,x,y,0);
                map.dispatchTouchEvent(down);
                map.dispatchTouchEvent(up);
                down.recycle();up.recycle();
            });
            screen.onActivity(a->{
                assertTrue("Map must list both overlapping source records",
                    a.chooserShowsRecords(2));
                a.closeNearbyChooser();
            });
            screen.onActivity(a->{
                assertTrue(hasText(a.getWindow().getDecorView(),
                    "2 saved coordinates in view"));
                snapshot(a,"offline-map-mindanao");
                View toggle=labelled(a.getWindow().getDecorView(),"Hide sites");
                assertNotNull(toggle);toggle.performClick();
                assertNotNull(labelled(a.getWindow().getDecorView(),"Show sites"));
                View all=labelled(a.getWindow().getDecorView(),"Philippines");
                assertNotNull(all);all.performClick();
                assertTrue(hasText(a.getWindow().getDecorView(),"Philippines view"));
            });
        }finally{cleanup();}
    }
}
