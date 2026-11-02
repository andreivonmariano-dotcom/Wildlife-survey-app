package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import java.io.File;
import java.io.RandomAccessFile;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/** Android 14 emulator, isolated empty user data. Actual UI, GPS skip, and WAV bytes. */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class Rc92UnverifiedDeviceInstrumentedTest {
    static final String OBSERVER="QA_RC92_GPS_SKIP_AND_AUDIO";
    static final String CANCEL="QA_RC92_GPS_CANCEL";
    static final Context APP=InstrumentationRegistry.getInstrumentation().getTargetContext();

    private static Button button(View v,String title){
        if(v instanceof Button&&title.equals(((Button)v).getText().toString()))return (Button)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                Button result=button(g.getChildAt(i),title);
                if(result!=null)return result;
            }
        }
        return null;
    }
    private static int count(String observer){
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM surveys WHERE observer_name=?",new String[]{observer})){
            assertTrue(c.moveToFirst());return c.getInt(0);
        }
    }
    static String survey(){
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT id FROM surveys WHERE observer_name=?",
                new String[]{OBSERVER})){
            assertTrue("No survey recorded by explicit no-GPS action",c.moveToFirst());
            assertEquals("Exactly one started survey",1,c.getCount());
            return c.getString(0);
        }
    }
    private static Intent gps(String observer){
        return new Intent(APP,SurveyGpsActivity.class)
            .putExtra("create_new",true)
            .putExtra("observer_name",observer)
            .putExtra("survey_profile","Birds")
            .putExtra("survey_method","opportunistic");
    }
    @Test public void aCancelWithoutFixDoesNotCreateSurvey(){
        assertEquals(0,count(CANCEL));
        try(ActivityScenario<SurveyGpsActivity> test=ActivityScenario.launch(gps(CANCEL))){
            test.onActivity(a->{
                Button skip=button(a.getWindow().getDecorView(),"Continue without GPS");
                assertNotNull(skip);assertTrue(skip.isShown());
                skip.performClick();
            });
            onView(withText("Keep checking GPS")).perform(click());
            test.onActivity(Activity::onBackPressed);
        }
        assertEquals("Back and dismissed confirmation cannot create an event",0,count(CANCEL));
    }
    @Test public void bExplicitGpsSkipPersistsMissingCoordinates(){
        assertEquals(0,count(OBSERVER));
        try(ActivityScenario<SurveyGpsActivity> test=ActivityScenario.launch(gps(OBSERVER))){
            test.onActivity(a->{
                Button skip=button(a.getWindow().getDecorView(),"Continue without GPS");
                assertNotNull(skip);skip.performClick();
            });
            onView(withText("Continue, flag missing GPS")).perform(click());
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
        String id=survey();
        try(FieldDb db=new FieldDb(APP);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT start_gps_status,start_gps_latitude,start_gps_longitude,ended "+
                "FROM surveys WHERE id=?",new String[]{id})){
            assertTrue(c.moveToFirst());
            assertEquals(SurveyStartGpsPolicy.MISSING,c.getString(0));
            assertTrue(c.isNull(1)&&c.isNull(2));
            assertTrue("Survey must remain active",c.isNull(3));
        }
    }
    @Test public void cActualMicrophoneStoresValidatedRiffAndMetadata() throws Exception {
        String id=survey();
        Intent launch=new Intent(APP,CallLibraryActivity.class)
            .putExtra("event_id",id).putExtra("taxon_group","Birds");
        try(ActivityScenario<CallLibraryActivity> screen=ActivityScenario.launch(launch)){
            screen.onActivity(a->{
                Button record=button(a.getWindow().getDecorView(),"● Record sound");
                assertNotNull("Recording control visible",record);
                record.performClick();
            });
            SystemClock.sleep(1800);
            screen.onActivity(a->{
                Button stop=button(a.getWindow().getDecorView(),"■ Stop and save recording");
                assertNotNull("AudioRecord must initialize and begin capture",stop);
                stop.performClick();
            });
            boolean stored=false;
            for(int attempt=0;attempt<100;attempt++){
                try(FieldDb db=new FieldDb(APP);
                    Cursor c=db.getReadableDatabase().rawQuery(
                        "SELECT filename,duration_ms,sample_rate_hz,channels "+
                        "FROM acoustic_clips WHERE event_id=? ORDER BY recorded_at_utc DESC LIMIT 1",
                        new String[]{id})){
                    if(c.moveToFirst()){
                        long ms=c.getLong(1);
                        assertTrue("WAV duration recorded",ms>300);
                        assertEquals(44100,c.getInt(2));assertEquals(1,c.getInt(3));
                        File wav=new File(AcousticEvidence.directory(APP),c.getString(0));
                        assertTrue("Valid WAV data exists",wav.isFile()&&wav.length()>44);
                        try(RandomAccessFile f=new RandomAccessFile(wav,"r")){
                            byte[] head=new byte[12];f.readFully(head);
                            assertEquals("RIFF",new String(head,0,4,"US-ASCII"));
                            assertEquals("WAVE",new String(head,8,4,"US-ASCII"));
                            long data=(head[4]&255L)|((head[5]&255L)<<8)|
                                ((head[6]&255L)<<16)|((head[7]&255L)<<24);
                            assertEquals("WAV chunk length matches bytes",wav.length()-8,data);
                        }
                        stored=true;break;
                    }
                }
                SystemClock.sleep(150);
            }
            assertTrue("App must persist captured audio metadata and bytes",stored);
        }
    }
}
