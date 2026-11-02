package org.phwildlife.survey;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** True Android SQLite + recorder UI evidence, no assumed wild sightings. */
@RunWith(AndroidJUnit4.class)
public final class ObservationTimelineEmulatorTest {
    private static View find(View node,String contains){
        if(node instanceof TextView &&
            ((TextView)node).getText().toString().contains(contains))return node;
        if(node instanceof ViewGroup){
            ViewGroup group=(ViewGroup)node;
            for(int i=0;i<group.getChildCount();i++){
                View match=find(group.getChildAt(i),contains);
                if(match!=null)return match;
            }
        }
        return null;
    }
    @Test public void timelineIsEventScopedAndExcludesDeletedAndDrafts(){
        try(SQLiteDatabase db=SQLiteDatabase.create(null)){
            db.execSQL("CREATE TABLE field_records(id INTEGER PRIMARY KEY,"+
                "event_id TEXT,detected_at TEXT,raw_identification TEXT,"+
                "scientific_name TEXT,taxon_group TEXT,individual_count INTEGER,deleted_at TEXT)");
            db.execSQL("CREATE TABLE record_edits(record_id INTEGER,action TEXT)");
            db.execSQL("INSERT INTO field_records VALUES(1,'survey-a',"+
                "'2026-11-02T09:00:00Z','Species A',NULL,'Birds',2,NULL)");
            db.execSQL("INSERT INTO field_records VALUES(2,'survey-b',"+
                "'2026-11-02T11:00:00Z','Species B',NULL,'Birds',3,NULL)");
            db.execSQL("INSERT INTO field_records VALUES(3,'survey-a',"+
                "'2026-11-02T12:00:00Z','Removed',NULL,'Birds',1,'2026-11-02')");
            db.execSQL("INSERT INTO field_records VALUES(4,'survey-a',"+
                "'2026-11-02T10:00:00Z','Corrected A',NULL,'Birds',4,NULL)");
            db.execSQL("INSERT INTO record_edits VALUES(4,'edit')");
            ObservationTimeline.Snapshot a=ObservationTimeline.load(db,"survey-a",4);
            assertEquals("Only saved, nondeleted records of this event",2,a.savedCount);
            assertEquals(2,a.recent.size());
            assertEquals(4L,a.recent.get(0).id);
            assertTrue(a.recent.get(0).corrected);
            assertEquals("Corrected A",a.recent.get(0).label);
            assertEquals(1L,a.recent.get(1).id);
            assertFalse(a.recent.get(1).corrected);
            ObservationTimeline.Snapshot b=ObservationTimeline.load(db,"survey-b",4);
            assertEquals(1,b.savedCount);
            assertEquals(2L,b.recent.get(0).id);
        }
    }
    @Test public void recorderHasExpandableTimelineAndExplicitDraftState(){
        Intent launch=new Intent(ApplicationProvider.getApplicationContext(),MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launch.putExtra("create_new",true);
        launch.putExtra("start_without_gps",true);
        launch.putExtra("survey_profile","Birds");
        launch.putExtra("survey_method","opportunistic");
        launch.putExtra("project_id","default");
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(launch)){
            scenario.onActivity(activity->{
                View root=activity.getWindow().getDecorView();
                View toggle=find(root,"Saved timeline · ");
                assertNotNull("Survey timeline should be discoverable",toggle);
                View draft=find(root,"Draft · No unsaved observation");
                assertNotNull("Draft needs a distinct state",draft);
                assertTrue("Collapsed timeline should not conceal count",toggle.isShown());
                toggle.performClick();
                assertNotNull("Empty saved-timeline state required",
                    find(root,"No saved observations yet"));
                toggle.performClick();
                View save=find(root,"Save Observation");
                View end=find(root,"End Survey");
                assertNotNull(save);assertTrue(save.isShown());
                assertNotNull(end);assertTrue(end.isShown());
            });
        }
    }
}
