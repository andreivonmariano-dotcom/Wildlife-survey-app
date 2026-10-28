package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Scoped, explicit deletion of owner-created data; bundled scientific references are preserved. */
public final class FieldDataDeletion {
    private FieldDataDeletion(){}

    private static final String[] EVENT_TABLES={
        "acoustic_labels", "trap_captures", "trap_checks", "coverboard_checks",
        "acoustic_clips", "trap_deployments", "track_sign_encounters",
        "track_sign_efforts", "coverboard_units", "observation_media",
        "protocol_media", "record_edits", "field_records", "sightings",
        "point_counts", "sampling_units", "survey_stations",
        "survey_outcome_audit", "survey_effort_pauses", "station_environment",
        "track_points", "area_search_passes", "spotlight_passes",
        "survey_study_protocols", "surveys"
    };
    private static final String[] WORKSPACE_TABLES={
        "route_template_stations", "route_templates", "study_protocol_templates",
        "sites", "projects"
    };
    public static final class Report {
        public final int deletedSurveys;
        public final int deletedFiles;
        public final int failedFiles;
        Report(int surveys,int files,int failed){
            deletedSurveys=surveys;deletedFiles=files;failedFiles=failed;
        }
    }
    public static int count(SQLiteDatabase db,String table){
        if(!"surveys".equals(table)&&!"field_records".equals(table)&&
           !"projects".equals(table)&&!"sites".equals(table)&&
           !"taxa_favorites".equals(table)&&!"route_templates".equals(table)&&
           !"study_protocol_templates".equals(table)&&!"observation_media".equals(table)&&
           !"protocol_media".equals(table)&&!"acoustic_clips".equals(table))
            throw new IllegalArgumentException("Unsupported count");
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+table,null)){
            return c.moveToFirst()?c.getInt(0):0;
        }
    }
    public static int countActive(SQLiteDatabase db){
        try(Cursor c=db.rawQuery(
            "SELECT COUNT(*) FROM surveys WHERE ended IS NULL OR outcome='active'",null)){
            return c.moveToFirst()?c.getInt(0):0;
        }
    }
    private static ArrayList<String> names(SQLiteDatabase db,String sql,String[] args){
        ArrayList<String> result=new ArrayList<>();
        try(Cursor c=db.rawQuery(sql,args)){
            while(c.moveToNext())if(!c.isNull(0))result.add(c.getString(0));
        }
        return result;
    }
    private static void collectMedia(SQLiteDatabase db,String survey,
            List<String> photos,List<String> calls){
        String[] args=survey==null?null:new String[]{survey};
        photos.addAll(names(db,
            "SELECT m.file_name FROM observation_media m JOIN field_records r "+
                "ON r.id=m.record_id"+(survey==null?"":" WHERE r.event_id=?"),args));
        photos.addAll(names(db,"SELECT file_name FROM protocol_media"+
            (survey==null?"":" WHERE event_id=?"),args));
        calls.addAll(names(db,"SELECT filename FROM acoustic_clips"+
            (survey==null?"":" WHERE event_id=?"),args));
    }
    private static void deleteWhere(SQLiteDatabase db,String table,String selector,
            String[] args){
        db.delete(table,selector,args);
    }
    private static void deleteEventRows(SQLiteDatabase db,String id){
        String[] args={id};
        deleteWhere(db,"acoustic_labels","clip_id IN "+
            "(SELECT id FROM acoustic_clips WHERE event_id=?)",args);
        deleteWhere(db,"trap_captures","check_id IN (SELECT c.id FROM trap_checks c "+
            "JOIN trap_deployments t ON t.id=c.deployment_id WHERE t.event_id=?)",args);
        deleteWhere(db,"trap_checks","deployment_id IN "+
            "(SELECT id FROM trap_deployments WHERE event_id=?)",args);
        deleteWhere(db,"coverboard_checks","board_pk IN "+
            "(SELECT id FROM coverboard_units WHERE event_id=?)",args);
        deleteWhere(db,"observation_media","record_id IN "+
            "(SELECT id FROM field_records WHERE event_id=?)",args);
        deleteWhere(db,"record_edits","record_id IN "+
            "(SELECT id FROM field_records WHERE event_id=?)",args);
        for(String table:new String[]{
            "acoustic_clips","trap_deployments","track_sign_encounters",
            "track_sign_efforts","coverboard_units","protocol_media",
            "field_records","sightings","point_counts","sampling_units",
            "survey_stations","survey_outcome_audit","survey_effort_pauses",
            "station_environment","track_points","area_search_passes",
            "spotlight_passes","survey_study_protocols","surveys"
        })db.delete(table,table.equals("surveys")?"id=?":"event_id=?",args);
    }
    private static int[] removeFiles(Context ctx,List<String> photos,List<String> calls){
        int removed=0,failed=0;
        HashSet<String> photoNames=new HashSet<>(photos);
        HashSet<String> callNames=new HashSet<>(calls);
        File photoDir=new File(ctx.getFilesDir(),"record_media");
        File callDir=new File(ctx.getFilesDir(),"acoustic");
        for(String name:photoNames){
            // Never accept path traversal, absolute paths, or arbitrary files.
            if(name==null||!name.matches("(observation_[0-9]+_[0-9a-fA-F-]+|protocol_[0-9a-fA-F-]+)\\.(jpg|png|webp|heic)")){
                failed++;continue;
            }
            File file=new File(photoDir,name);
            if(!file.exists())continue;
            if(file.delete())removed++;else failed++;
        }
        for(String name:callNames){
            if(!AcousticEvidence.safeName(name)){failed++;continue;}
            File file=new File(callDir,name);
            if(!file.exists())continue;
            if(file.delete())removed++;else failed++;
        }
        return new int[]{removed,failed};
    }
    /** Archive survey only. Nothing active, including a paused session, is removable. */
    public static Report deleteSurvey(Context context,SQLiteDatabase db,String surveyId){
        if(surveyId==null||surveyId.isEmpty())throw new IllegalArgumentException("Survey required");
        ArrayList<String> photos=new ArrayList<>(),calls=new ArrayList<>();
        db.beginTransaction();
        try{
            try(Cursor c=db.rawQuery("SELECT ended,outcome FROM surveys WHERE id=?",
                new String[]{surveyId})){
                if(!c.moveToFirst())throw new IllegalArgumentException("Survey not found");
                if(c.isNull(0)||"active".equals(c.getString(1)))
                    throw new IllegalStateException("End the active survey first");
            }
            collectMedia(db,surveyId,photos,calls);
            deleteEventRows(db,surveyId);
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        int[] count=removeFiles(context,photos,calls);
        return new Report(1,count[0],count[1]);
    }
    public static Report deleteAllSurveys(Context context,SQLiteDatabase db){
        ArrayList<String> photos=new ArrayList<>(),calls=new ArrayList<>();
        int surveys;
        db.beginTransaction();
        try{
            if(countActive(db)>0)throw new IllegalStateException(
                "End or cancel every active survey before deletion");
            surveys=count(db,"surveys");
            collectMedia(db,null,photos,calls);
            ArrayList<String> ids=names(db,"SELECT id FROM surveys",null);
            for(String id:ids)deleteEventRows(db,id);
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        int[] count=removeFiles(context,photos,calls);
        return new Report(surveys,count[0],count[1]);
    }
    public static void clearWorkspace(SQLiteDatabase db){
        db.beginTransaction();
        try{
            if(count(db,"surveys")!=0)throw new IllegalStateException(
                "Delete dependent surveys before removing the project register");
            for(String table:WORKSPACE_TABLES)db.delete(table,null,null);
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }
    public static void clearFavorites(SQLiteDatabase db){
        db.delete("taxa_favorites",null,null);
    }
    /** All user-created data. Reference catalog rows never erased by this option. */
    public static Report clearAll(Context context,SQLiteDatabase db){
        ArrayList<String> photos=new ArrayList<>(),calls=new ArrayList<>();
        int surveys;
        db.beginTransaction();
        try{
            if(countActive(db)>0)throw new IllegalStateException(
                "End every active survey before resetting the workspace");
            surveys=count(db,"surveys");
            collectMedia(db,null,photos,calls);
            // Delete any rows of user-generated event tables in dependency order.
            db.execSQL("DELETE FROM acoustic_labels");
            db.execSQL("DELETE FROM trap_captures");
            db.execSQL("DELETE FROM trap_checks");
            db.execSQL("DELETE FROM coverboard_checks");
            for(String table:EVENT_TABLES){
                if("acoustic_labels".equals(table)||"trap_captures".equals(table)||
                   "trap_checks".equals(table)||"coverboard_checks".equals(table))continue;
                db.delete(table,null,null);
            }
            for(String table:WORKSPACE_TABLES)db.delete(table,null,null);
            db.delete("taxa_favorites",null,null);
            db.setTransactionSuccessful();
        }finally{db.endTransaction();}
        int[] count=removeFiles(context,photos,calls);
        return new Report(surveys,count[0],count[1]);
    }
}