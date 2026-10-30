package org.phwildlife.survey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import static org.junit.Assert.*;

/** Disposable-emulator only: read actual research ZIP output, never user files. */
@RunWith(AndroidJUnit4.class)
public final class Rc51ResearchZipInstrumentedTest {
    static final String MODERN="QA_MODERN", LEGACY="QA_LEGACY", ZERO="QA_ZERO",
        INCOMPLETE="QA_INCOMPLETE", EXCLUDED="QA_NOT_COUNTED", ACTIVE="QA_ACTIVE",
        ANOMALY="QA_ANOMALY";
    private static void fixture(SQLiteDatabase db,String id,String outcome,
            boolean ended,int complete,boolean detection){
        ContentValues survey=new ContentValues();
        survey.put("id",id);survey.put("method","point_count");
        survey.put("survey_profile","Birds");survey.put("taxon_group","Birds");
        survey.put("started","2026-10-20T08:00:00Z");
        if(ended)survey.put("ended","2026-10-20T08:10:00Z");
        if(outcome==null)survey.putNull("outcome");else survey.put("outcome",outcome);
        survey.put("complete",complete);survey.put("project_id","default");
        survey.put("region_label","Synthetic QA region");survey.put("area_km2",1d);
        survey.put("point_radius_m",50d);survey.put("point_target_s",600);
        db.insertOrThrow("surveys",null,survey);
        ContentValues site=new ContentValues();
        site.put("id","ST_"+id);site.put("project_id","default");
        site.put("name","Synthetic station");site.put("created_at","2026-10-20");
        db.insertOrThrow("sites",null,site);
        ContentValues point=new ContentValues();
        point.put("id","PC_"+id);point.put("event_id",id);
        point.put("station_id","ST_"+id);point.put("sequence_number",1);
        point.put("started","2026-10-20T08:00:00Z");
        point.put("ended","2026-10-20T08:10:00Z");
        point.put("target_seconds",600);point.put("completed",1);
        db.insertOrThrow("point_counts",null,point);
        if(detection){
            ContentValues record=new ContentValues();
            record.put("event_id",id);record.put("point_count_id","PC_"+id);
            record.put("detected_at","2026-10-20T08:05:00Z");
            record.put("taxon_group","Birds");
            record.put("raw_identification","Synthetic QA bird");
            record.put("individual_count",1);record.put("detection_method","visual");
            record.put("horizontal_m",30d);record.put("distance_eligible",1);
            record.put("identification_level","species_name_unverified");
            record.put("identification_confidence","Not assessed");
            db.insertOrThrow("field_records",null,record);
        }
    }
    private static Map<String,String> entries(byte[] archive)throws Exception{
        Map<String,String> result=new HashMap<>();
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(archive))){
            ZipEntry entry;byte[] chunk=new byte[8192];int n;
            while((entry=zip.getNextEntry())!=null){
                ByteArrayOutputStream value=new ByteArrayOutputStream();
                while((n=zip.read(chunk))!=-1)value.write(chunk,0,n);
                if(entry.getName().endsWith(".csv"))
                    result.put(entry.getName(),new String(value.toByteArray(),StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return result;
    }
    static boolean includes(String csv,String id){
        return csv!=null&&csv.contains("\""+id+"\"");
    }
    @Test public void actualZipKeepsRawHistoryAndExcludesOnlyIneligibleAnalyticalEvents()
            throws Exception{
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        try(FieldDb helper=new FieldDb(context)){
            SQLiteDatabase db=helper.getWritableDatabase();
            try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM surveys",null)){
                assertTrue(c.moveToFirst());
                assertEquals("Test must run on a disposable empty emulator",0,c.getInt(0));
            }
            fixture(db,MODERN,"complete",true,1,true);
            fixture(db,LEGACY,null,true,1,true);
            fixture(db,ZERO,"complete",true,1,false);
            fixture(db,INCOMPLETE,"incomplete",true,0,true);
            fixture(db,EXCLUDED,"not_counted",true,1,true);
            fixture(db,ACTIVE,null,false,0,true);
            fixture(db,ANOMALY,null,false,1,true);
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            try(ZipOutputStream zip=new ZipOutputStream(output)){
                MainActivity.writeResearchArchive(context,helper,zip);
            }
            Map<String,String> archive=entries(output.toByteArray());
            for(String name:new String[]{"surveys.csv","observations.csv","point_counts.csv",
                "occupancy_visits.csv","sdm_occurrences.csv","distance_samples.csv",
                "distance_review.csv","distance_flatfile_draft.csv","data_quality_flags.csv"})
                assertTrue("Missing research ZIP member "+name,archive.containsKey(name));
            for(String id:new String[]{MODERN,LEGACY,ZERO,INCOMPLETE,EXCLUDED,ACTIVE,ANOMALY})
                assertTrue("Missing raw survey "+id,includes(archive.get("surveys.csv"),id));
            for(String id:new String[]{MODERN,LEGACY,INCOMPLETE,EXCLUDED,ACTIVE,ANOMALY})
                assertTrue("Missing raw observation "+id,includes(archive.get("observations.csv"),id));
            for(String filename:new String[]{"occupancy_visits.csv","sdm_occurrences.csv",
                "distance_samples.csv","distance_review.csv","distance_flatfile_draft.csv"}){
                String content=archive.get(filename);
                // distance_samples.csv has a point-count ID rather than an event_id column.
                String prefix=filename.equals("distance_samples.csv")?"PC_":"";
                assertTrue(filename+" omitted modern survey",includes(content,prefix+MODERN));
                assertTrue(filename+" omitted legacy completed survey",includes(content,prefix+LEGACY));
                for(String invalid:new String[]{INCOMPLETE,EXCLUDED,ACTIVE,ANOMALY})
                    assertFalse(filename+" wrongly included "+invalid,includes(content,prefix+invalid));
            }
            assertTrue("Completed zero-detection visit should retain effort",
                includes(archive.get("occupancy_visits.csv"),ZERO));
            assertTrue("Distance sample should retain zero-detection station",
                includes(archive.get("distance_samples.csv"),"PC_"+ZERO));
            assertTrue("Zero-detection row must be labeled, not interpreted as absence",
                archive.get("distance_flatfile_draft.csv").contains("zero_detected_species"));
        }
    }
}
