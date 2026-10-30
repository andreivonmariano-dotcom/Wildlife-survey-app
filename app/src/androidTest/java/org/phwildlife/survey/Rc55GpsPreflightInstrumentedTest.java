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

/** Read-only GPS preflight screen contracts on the disposable emulator. */
@RunWith(AndroidJUnit4.class)
public final class Rc55GpsPreflightInstrumentedTest {
    private static View findDescription(View v,String starts){
        if(String.valueOf(v.getContentDescription()).startsWith(starts))return v;
        if(v instanceof ViewGroup){
            ViewGroup group=(ViewGroup)v;
            for(int i=0;i<group.getChildCount();i++){
                View hit=findDescription(group.getChildAt(i),starts);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static TextView findText(View v,String contains){
        if(v instanceof TextView &&
            ((TextView)v).getText().toString().contains(contains))return (TextView)v;
        if(v instanceof ViewGroup){
            ViewGroup group=(ViewGroup)v;
            for(int i=0;i<group.getChildCount();i++){
                TextView hit=findText(group.getChildAt(i),contains);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private Intent screen(boolean station){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
            SurveyGpsActivity.class);
        i.putExtra("station_gps_mode",station);
        return i;
    }
    @Test public void surveyPreflightKeepsSafeActionAndReadableStatus(){
        try(ActivityScenario<SurveyGpsActivity> scenario=ActivityScenario.launch(screen(false))){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(findText(root,"Survey GPS"));
                assertNotNull(findText(root,"Position status"));
                assertNotNull(findText(root,"GPS controls"));
                assertNotNull(findText(root,"Check current GPS again"));
                assertNotNull(findDescription(root,"Open Android device location settings"));
                View missing=findDescription(root,"Start survey with its starting GPS position missing");
                assertNotNull(missing);
                assertTrue(missing.isClickable());
                assertNotNull(findDescription(root,"Cancel GPS acquisition and return to survey setup"));
                View disclosure=findDescription(root,"Show satellite signal and sky details");
                assertNotNull(disclosure);disclosure.performClick();
                assertNotNull(findDescription(root,"Hide satellite signal and sky details"));
                disclosure.performClick();
                assertNotNull(findDescription(root,"Show satellite signal and sky details"));
                assertNull("A second start button must not appear",findText(root,"Start survey with verified GPS"));
            });
        }
    }
    @Test public void stationPreflightHasSeparateMissingFixReturnAndNoSkyMap(){
        try(ActivityScenario<SurveyGpsActivity> scenario=ActivityScenario.launch(screen(true))){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(findText(root,"Station GPS"));
                assertNotNull(findText(root,"Position status"));
                assertNotNull(findText(root,"Start without GPS · flag missing"));
                assertNotNull(findDescription(root,"Return to the active survey without starting a station"));
                assertNull(findDescription(root,"Show satellite signal and sky details"));
                assertNull(findText(root,"Start station with current GPS"));
            });
        }
    }
}
