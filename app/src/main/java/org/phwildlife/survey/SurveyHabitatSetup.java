package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.Set;

/**
 * Survey-level preparation, not a station-level habitat observation. The confirmed
 * habitat labels and map suggestions have distinct columns and provenance.
 */
public final class SurveyHabitatSetup {
    private SurveyHabitatSetup(){}
    public static void create(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS survey_habitat_setup("+
            "event_id TEXT PRIMARY KEY NOT NULL,confirmed_habitats TEXT NOT NULL,"+
            "checklist_mode TEXT NOT NULL,map_suggestions TEXT NOT NULL,"+
            "landcover_source TEXT,latitude REAL,longitude REAL,"+
            "gps_accuracy_m REAL,gps_fix_time_ms INTEGER,"+
            "elevation_msl_m REAL,vertical_uncertainty_m REAL,"+
            "prepared_utc TEXT NOT NULL)");
    }
    public static void save(SQLiteDatabase db,String id,String chosen,String mode,
            String mapSuggestions,String source,Double lat,Double lon,
            Double accuracy,long gpsTime,Double altitude,Double uncertainty){
        if(id==null||id.isEmpty())throw new IllegalArgumentException("Survey ID required");
        if(!"suggested".equals(mode))mode="all";
        create(db);
        db.execSQL("INSERT OR REPLACE INTO survey_habitat_setup("+
            "event_id,confirmed_habitats,checklist_mode,map_suggestions,"+
            "landcover_source,latitude,longitude,gps_accuracy_m,gps_fix_time_ms,"+
            "elevation_msl_m,vertical_uncertainty_m,prepared_utc)"+
            " VALUES(?,?,?,?,?,?,?,?,?,?,?,datetime('now'))",
            new Object[]{id,PaicHabitatPolicy.encode(
                PaicHabitatPolicy.decode(chosen)),mode,
                mapSuggestions==null?"":mapSuggestions,source,
                lat,lon,accuracy,gpsTime<=0?null:gpsTime,altitude,uncertainty});
    }
    public static final class State{
        public final String habitats,mode,mapSource;
        public final Double mslMeters,uncertaintyMeters;
        State(String habitats,String mode,String source,Double msl,Double accuracy){
            this.habitats=habitats;this.mode=mode;mapSource=source;
            mslMeters=msl;uncertaintyMeters=accuracy;
        }
    }
    public static State load(SQLiteDatabase db,String id){
        if(id==null||id.isEmpty())return null;
        try(Cursor c=db.rawQuery("SELECT confirmed_habitats,checklist_mode,"+
                "landcover_source,elevation_msl_m,vertical_uncertainty_m "+
                "FROM survey_habitat_setup WHERE event_id=? LIMIT 1",
                new String[]{id})){
            if(!c.moveToFirst())return null;
            return new State(c.getString(0),c.getString(1),c.getString(2),
                c.isNull(3)?null:c.getDouble(3),
                c.isNull(4)?null:c.getDouble(4));
        }
    }
}
