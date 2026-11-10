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
                assertNotNull(findText(root,"Required: device GPS accuracy under 10 m"));
                assertNotNull(findDescription(root,"Continue without a verified survey-start GPS fix"));
                View detail=findDescription(root,"Show coordinates, altitude, accuracy and satellite diagnostics");
                View help=findDescription(root,"Show GPS retry and Android location settings");
                assertNotNull(detail);
                assertNotNull(help);
                assertNotNull(findText(root,"Retry GPS acquisition"));
                assertFalse("Retry should start collapsed",findText(root,"Retry GPS acquisition").isShown());
                detail.performClick();
                assertNotNull(findDescription(root,"Hide coordinates and GPS diagnostics"));
                detail.performClick();
                help.performClick();
                assertNotNull(findText(root,"Retry GPS acquisition"));
                assertTrue("Retry should expand",findText(root,"Retry GPS acquisition").isShown());
                assertNotNull(findDescription(root,"Open Android device location settings"));
                assertNull("No redundant visible start button",
                    findText(root,"Start survey with verified GPS"));
            });
        }
    }
    @Test public void stationPreflightHasSeparateFlagAndNoSkyMap(){
        try(ActivityScenario<SurveyGpsActivity> scenario=ActivityScenario.launch(screen(true))){
            scenario.onActivity(a->{
                View root=a.getWindow().getDecorView();
                assertNotNull(findText(root,"Station GPS"));
                assertNotNull(findText(root,"Start station without GPS"));
                assertNotNull(findDescription(root,"Start station visit with unverified station GPS coordinates"));
                View detail=findDescription(root,"Show coordinates, altitude, accuracy and satellite diagnostics");
                assertNotNull(detail);
                detail.performClick();
                assertNull(findText(root,"Satellites: waiting for receiver"));
                assertNull(findText(root,"Start station with current GPS"));
            });
        }
    }

}
