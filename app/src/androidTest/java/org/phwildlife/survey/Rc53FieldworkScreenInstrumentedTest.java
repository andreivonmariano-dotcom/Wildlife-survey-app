package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Only runs against an empty disposable AVD; no real owner records may be edited. */
@RunWith(AndroidJUnit4.class)
public final class Rc53FieldworkScreenInstrumentedTest {
    private static View findDescription(View root,String prefix){
        String desc=String.valueOf(root.getContentDescription());
        if(desc.startsWith(prefix))return root;
        if(root instanceof ViewGroup){
            ViewGroup children=(ViewGroup)root;
            for(int i=0;i<children.getChildCount();i++){
                View hit=findDescription(children.getChildAt(i),prefix);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static View findLabel(View root,String label){
        if(root instanceof TextView&&label.contentEquals(((TextView)root).getText()))
            return root;
        if(root instanceof ViewGroup){
            ViewGroup children=(ViewGroup)root;
            for(int i=0;i<children.getChildCount();i++){
                View hit=findLabel(children.getChildAt(i),label);
                if(hit!=null)return hit;
            }
        }
        return null;
    }
    private static Intent home(){
        Intent i=new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                WelcomeActivity.class);
        i.putExtra("show_menu",true);
        return i;
    }
    @Test public void fieldworkPresentsUniformDestinationsAndExpandableTools(){
        try(FieldDb helper=new FieldDb(
                InstrumentationRegistry.getInstrumentation().getTargetContext())){
            SQLiteDatabase db=helper.getReadableDatabase();
            try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM surveys",null)){
                assertTrue(c.moveToFirst());
                assertEquals("Only use clean disposable AVD",0,c.getInt(0));
            }
        }
        try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(home())){
            scenario.onActivity(a->{
                View decor=a.getWindow().getDecorView();
                View open=findDescription(decor,"Open Fieldwork.");
                assertNotNull("Home's Fieldwork destination",open);
                open.performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(a->{
                View decor=a.getWindow().getDecorView();
                for(String s:new String[]{"Open Birds.","Open Herpetofauna.","Open Mammals.",
                    "Open Projects & stations","Open History.","Open Analysis."}){
                    View choice=findDescription(decor,s);
                    assertNotNull(s,choice);
                    assertTrue(s,choice.isClickable());
                    assertTrue(s,choice.getHeight()>0);
                }
                View birds=findDescription(decor,"Open Birds.");
                View herps=findDescription(decor,"Open Herpetofauna.");
                View mammals=findDescription(decor,"Open Mammals.");
                assertEquals(birds.getWidth(),herps.getWidth());
                assertEquals(herps.getWidth(),mammals.getWidth());
                assertNotNull(findLabel(decor,"Records & projects"));
                View toggle=findDescription(decor,"Show additional field tools");
                if(toggle==null){
                    // The screenshot pass may leave this persisted preference expanded.
                    toggle=findDescription(decor,"Hide additional field tools");
                    assertNotNull("Field tools toggle must remain available",toggle);
                    toggle.performClick();
                }
                assertNotNull(toggle);
                assertEquals("Show additional field tools",String.valueOf(toggle.getContentDescription()));
                toggle.performClick();
                assertNotNull(findDescription(decor,"Open Site habitats"));
                assertNotNull(findDescription(decor,"Open Camera traps"));
                assertNotNull(findDescription(decor,"Open Study plans"));
                assertEquals("Hide additional field tools",String.valueOf(toggle.getContentDescription()));
                toggle.performClick();
                assertEquals("Show additional field tools",String.valueOf(toggle.getContentDescription()));
            });
        }
    }
    @Test public void activeSurveyKeepsResumeBeforeRecordsWithoutOfferingANewSurvey(){
        android.content.Context ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String id="QA_RC53_ACTIVE_LAYOUT";
        try(FieldDb helper=new FieldDb(ctx)){
            SQLiteDatabase db=helper.getWritableDatabase();
            try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM surveys",null)){
                assertTrue(c.moveToFirst());
                assertEquals("Do not seed over existing fieldwork",0,c.getInt(0));
            }
            ContentValues s=new ContentValues();
            s.put("id",id);s.put("method","point_count");
            s.put("survey_profile","Birds");s.put("taxon_group","Birds");
            s.put("project_id","default");s.put("started","2026-10-30T08:00:00Z");
            s.put("complete",0);s.put("outcome","active");
            db.insertOrThrow("surveys",null,s);
            try {
                try(ActivityScenario<WelcomeActivity> scenario=ActivityScenario.launch(home())){
                    scenario.onActivity(a->{
                        View root=a.getWindow().getDecorView();
                        View field=findDescription(root,"Open Fieldwork.");
                        assertNotNull(field);
                        field.performClick();
                        View resume=findLabel(root,"Resume Survey");
                        assertNotNull("Active survey must have prominent Resume",resume);
                        assertNotNull(findLabel(root,"Current survey"));
                        assertNotNull(findLabel(root,"In progress"));
                        assertNull("One live survey at a time: no New survey choices",
                            findDescription(root,"Open Birds."));
                        assertNotNull(findDescription(root,"Open History."));
                        assertNotNull(findDescription(root,"Open Analysis."));
                    });
                }
            }finally{
                db.delete("surveys","id=?",new String[]{id});
            }
        }
    }
}
