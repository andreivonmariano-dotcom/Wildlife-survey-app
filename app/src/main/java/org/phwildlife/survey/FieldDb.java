package org.phwildlife.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Shared offline schema, versioned migrations. Never drop a user observation table. */
public final class FieldDb extends SQLiteOpenHelper {
    public static final String NAME="wildlife_field_v03.db";
    public FieldDb(Context context){super(context,NAME,null,4);}
    @Override public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE surveys(id TEXT PRIMARY KEY,method TEXT,started TEXT,ended TEXT,transect_az REAL,taxon_group TEXT DEFAULT 'Birds',point_target_s INTEGER,effort_length_m REAL,complete INTEGER,survey_profile TEXT,station_id TEXT,observer_name TEXT,protocol_status TEXT)");
        d.execSQL("CREATE TABLE sightings(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,seen_at TEXT,taxon TEXT,individual_count INTEGER,detection TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,lat REAL,lon REAL,gps_accuracy_m REAL)");
        d.execSQL("CREATE TABLE field_records(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,detected_at TEXT,taxon_group TEXT,common_name TEXT,scientific_name TEXT,raw_identification TEXT,individual_count INTEGER,detection_method TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,observer_lat REAL,observer_lon REAL,gps_accuracy_m REAL,habitat TEXT,behavior TEXT,notes TEXT,compass_quality INTEGER,name_source TEXT)");
        d.execSQL("CREATE INDEX record_event_idx ON field_records(event_id)");
        upgradeFour(d);
    }
    @Override public void onUpgrade(SQLiteDatabase d,int oldVersion,int newVersion){
        if(oldVersion<2){
            d.execSQL("ALTER TABLE surveys ADD COLUMN taxon_group TEXT DEFAULT 'Birds'");
            d.execSQL("ALTER TABLE surveys ADD COLUMN point_target_s INTEGER");
            d.execSQL("ALTER TABLE surveys ADD COLUMN effort_length_m REAL");
            d.execSQL("ALTER TABLE surveys ADD COLUMN complete INTEGER");
            d.execSQL("CREATE TABLE IF NOT EXISTS field_records(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,detected_at TEXT,taxon_group TEXT,common_name TEXT,scientific_name TEXT,raw_identification TEXT,individual_count INTEGER,detection_method TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,observer_lat REAL,observer_lon REAL,gps_accuracy_m REAL,habitat TEXT,behavior TEXT,notes TEXT,compass_quality INTEGER,name_source TEXT)");
            d.execSQL("CREATE INDEX IF NOT EXISTS record_event_idx ON field_records(event_id)");
        }
        if(oldVersion<3){
            d.execSQL("ALTER TABLE surveys ADD COLUMN survey_profile TEXT");
            d.execSQL("ALTER TABLE surveys ADD COLUMN station_id TEXT");
            d.execSQL("ALTER TABLE surveys ADD COLUMN observer_name TEXT");
            d.execSQL("ALTER TABLE surveys ADD COLUMN protocol_status TEXT");
            d.execSQL("UPDATE surveys SET survey_profile=COALESCE(taxon_group,'Birds'),protocol_status='legacy' WHERE survey_profile IS NULL");
        }
        if(oldVersion<4)upgradeFour(d);
    }
    private void upgradeFour(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS projects(id TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT, created_at TEXT NOT NULL)");
        d.execSQL("CREATE TABLE IF NOT EXISTS sites(id TEXT PRIMARY KEY, project_id TEXT NOT NULL DEFAULT 'default', name TEXT NOT NULL, habitat TEXT, latitude REAL, longitude REAL, coordinate_uncertainty_m REAL, notes TEXT, created_at TEXT, updated_at TEXT)");
        d.execSQL("CREATE TABLE IF NOT EXISTS record_edits(id INTEGER PRIMARY KEY AUTOINCREMENT,record_id INTEGER NOT NULL,action TEXT NOT NULL,edited_at TEXT NOT NULL,previous_json TEXT,current_json TEXT,reason TEXT)");
        d.execSQL("ALTER TABLE surveys ADD COLUMN project_id TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN region_label TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN area_km2 REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN point_radius_m REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN weather TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN distance_method TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN initial_position TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN movement_status TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN distance_eligible INTEGER NOT NULL DEFAULT 0");
        d.execSQL("ALTER TABLE field_records ADD COLUMN updated_at TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN deleted_at TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN legacy_source_id INTEGER");
        d.execSQL("CREATE INDEX IF NOT EXISTS records_active_event ON field_records(event_id,deleted_at)");
        d.execSQL("INSERT OR IGNORE INTO projects(id,name,description,created_at) VALUES('default','General field project','Default offline project',datetime('now'))");
        d.execSQL("UPDATE surveys SET project_id='default' WHERE project_id IS NULL");
        d.execSQL("INSERT OR IGNORE INTO sites(id,project_id,name,created_at,updated_at) SELECT DISTINCT station_id,'default',station_id,datetime('now'),datetime('now') FROM surveys WHERE station_id IS NOT NULL AND trim(station_id)<>''");
        d.execSQL("INSERT INTO field_records(event_id,detected_at,taxon_group,raw_identification,individual_count,detection_method,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,observer_lat,observer_lon,gps_accuracy_m,name_source,legacy_source_id,updated_at) SELECT event_id,seen_at,'Birds',taxon,individual_count,detection,horizontal_m,bearing_magnetic,pitch_deg,perpendicular_m,lat,lon,gps_accuracy_m,'legacy_v0.3',id,datetime('now') FROM sightings");
        d.execSQL("UPDATE field_records SET distance_eligible=0 WHERE distance_eligible IS NULL");
    }
}
