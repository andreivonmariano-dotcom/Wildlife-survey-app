package org.phwildlife.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Shared offline schema, versioned migrations. Never drop a user observation table. */
public final class FieldDb extends SQLiteOpenHelper {
    public static final String NAME="wildlife_field_v03.db";
    private final Context appContext;
    public FieldDb(Context context){super(context,NAME,null,21);appContext=context.getApplicationContext();}
    @Override public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE surveys(id TEXT PRIMARY KEY,method TEXT,started TEXT,ended TEXT,transect_az REAL,taxon_group TEXT DEFAULT 'Birds',point_target_s INTEGER,effort_length_m REAL,complete INTEGER,survey_profile TEXT,station_id TEXT,observer_name TEXT,protocol_status TEXT)");
        d.execSQL("CREATE TABLE sightings(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,seen_at TEXT,taxon TEXT,individual_count INTEGER,detection TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,lat REAL,lon REAL,gps_accuracy_m REAL)");
        d.execSQL("CREATE TABLE field_records(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT,detected_at TEXT,taxon_group TEXT,common_name TEXT,scientific_name TEXT,raw_identification TEXT,individual_count INTEGER,detection_method TEXT,horizontal_m REAL,bearing_magnetic REAL,pitch_deg REAL,perpendicular_m REAL,observer_lat REAL,observer_lon REAL,gps_accuracy_m REAL,habitat TEXT,behavior TEXT,notes TEXT,compass_quality INTEGER,name_source TEXT)");
        d.execSQL("CREATE INDEX record_event_idx ON field_records(event_id)");
        upgradeFour(d);
        upgradeFive(d);
        upgradeSix(d);
        upgradeSeven(d);
        upgradeEight(d);
        upgradeNine(d);
        upgradeTen(d);
        upgradeEleven(d);
        upgradeTwelve(d);
        upgradeThirteen(d);
        upgradeFourteen(d);
        upgradeFifteen(d);
        upgradeSixteen(d);
        upgradeSeventeen(d);
        upgradeEighteen(d);
        upgradeNineteen(d);
        upgradeTwenty(d);
        upgradeTwentyOne(d);
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
        if(oldVersion<5)upgradeFive(d);
        if(oldVersion<6)upgradeSix(d);
        if(oldVersion<7)upgradeSeven(d);
        if(oldVersion<8)upgradeEight(d);
        if(oldVersion<9)upgradeNine(d);
        if(oldVersion<10)upgradeTen(d);
        if(oldVersion<11)upgradeEleven(d);
        if(oldVersion<12)upgradeTwelve(d);
        if(oldVersion<13)upgradeThirteen(d);
        if(oldVersion<14)upgradeFourteen(d);
        if(oldVersion<15)upgradeFifteen(d);
        if(oldVersion<16)upgradeSixteen(d);
        if(oldVersion<17)upgradeSeventeen(d);
        if(oldVersion<18)upgradeEighteen(d);
        if(oldVersion<19)upgradeNineteen(d);
        if(oldVersion<20)upgradeTwenty(d);
        if(oldVersion<21)upgradeTwentyOne(d);
    }
    /**
     * User-reviewed habitat references are stored apart from publisher taxon
     * rows. Missing habitat/elevation evidence must not imply absence.
     */
    private void upgradeNineteen(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS taxon_habitat_profiles("+
            "taxon_key TEXT PRIMARY KEY NOT NULL,habitat_codes TEXT NOT NULL,"+
            "strict_habitat INTEGER NOT NULL DEFAULT 0,"+
            "elevation_min_m REAL,elevation_max_m REAL,"+
            "source TEXT NOT NULL,source_version TEXT NOT NULL,"+
            "review_status TEXT NOT NULL DEFAULT 'unreviewed',"+
            "updated_utc TEXT NOT NULL)");
        d.execSQL("CREATE INDEX IF NOT EXISTS habitat_profile_review "+
            "ON taxon_habitat_profiles(review_status)");
    }
    /**
     * Seed only independently checked ecological literature. Blank records
     * are NOT populated from taxonomy or checklist geographic membership.
     * INSERT OR IGNORE protects profiles already entered by the researcher.
     */
    private void upgradeTwenty(SQLiteDatabase d){
        d.execSQL("ALTER TABLE taxon_habitat_profiles ADD COLUMN source_url TEXT "+
            "NOT NULL DEFAULT ''");
        d.execSQL("ALTER TABLE taxon_habitat_profiles ADD COLUMN evidence_note TEXT "+
            "NOT NULL DEFAULT ''");
        String[][] verified={
            {"WBCP2025:Pithecophaga_jefferyi","lowland|secondary",
                "150","1450","Animal Diversity Web: Pithecophaga jefferyi",
                "accessed-2026-10-27","https://animaldiversity.org/accounts/Pithecophaga_jefferyi/",
                "Forest habitat and broad elevation range, 150–1450 m. Region-specific "+
                "published statements are not strict physiological limits."},
            {"MDD:1000915","secondary|lowland",
                "","","Neri-Arboleda et al., Journal of Zoology",
                "2002","https://doi.org/10.1017/S0952836902000997",
                "Carlito syrichta: older scientific name Tarsius syrichta. "+
                "Bohol study documents secondary lowland rainforest. "+
                "No species-wide elevation limits assigned."},
            {"SYAMP2025:001","riparian",
                "","","Aznar et al., Barbourula busuangensis field study",
                "2025","https://pmc.ncbi.nlm.nih.gov/articles/PMC11740736/",
                "Clear, well-oxygenated forest streams. No species-wide "+
                "elevation endpoints assigned."},
            {"SYAMP2025:002","riparian|montane|lowland",
                "","","IUCN, Threatened Amphibians of the World",
                "2008","https://www.iucn-amphibians.org/wp-content/uploads/"+
                    "sites/4/2018/11/TAW-Threatened-Species-Profiles_Part1.pdf",
                "Source describes cool mountain streams in lowland and "+
                "lower montane forest on Mindanao. No range endpoints assigned."}
        };
        for(String[] p:verified){
            d.execSQL("INSERT OR IGNORE INTO taxon_habitat_profiles("+
                "taxon_key,habitat_codes,strict_habitat,elevation_min_m,"+
                "elevation_max_m,source,source_version,review_status,"+
                "updated_utc,source_url,evidence_note) "+
                "VALUES(?,?,0,?,?,?,?, 'reviewed',datetime('now'),?,?)",
                new Object[]{p[0],p[1],
                    p[2].isEmpty()?null:Double.valueOf(p[2]),
                    p[3].isEmpty()?null:Double.valueOf(p[3]),
                    p[4],p[5],p[6],p[7]});
        }
    }
    /** Captures per-visit environmental measurements, not site-wide estimates. */
    private void upgradeTwentyOne(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS station_environment("+
            "visit_id TEXT PRIMARY KEY,event_id TEXT NOT NULL,station_id TEXT NOT NULL,"+
            "habitat_code TEXT,habitat_source TEXT,"+
            "elevation_msl_m REAL,elevation_hae_m REAL,"+
            "vertical_uncertainty_m REAL,elevation_datum TEXT,"+
            "elevation_source TEXT NOT NULL DEFAULT 'not_available',"+
            "fix_time_utc TEXT,updated_utc TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS station_env_event "+
            "ON station_environment(event_id,station_id)");
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
    private void upgradeFive(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS point_counts(id TEXT PRIMARY KEY,event_id TEXT NOT NULL,station_id TEXT NOT NULL,sequence_number INTEGER NOT NULL,started TEXT NOT NULL,ended TEXT,target_seconds INTEGER NOT NULL,completed INTEGER,notes TEXT,UNIQUE(event_id,sequence_number))");
        d.execSQL("CREATE INDEX IF NOT EXISTS point_event_idx ON point_counts(event_id,sequence_number)");
        d.execSQL("CREATE TABLE IF NOT EXISTS track_points(id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT NOT NULL,recorded_at TEXT NOT NULL,latitude REAL NOT NULL,longitude REAL NOT NULL,accuracy_m REAL,segment_m REAL DEFAULT 0,accepted INTEGER NOT NULL,reason TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS track_by_event ON track_points(event_id,id)");
        d.execSQL("ALTER TABLE field_records ADD COLUMN point_count_id TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN protocol_window_flag TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN air_temp_c REAL");
        d.execSQL("ALTER TABLE field_records ADD COLUMN humidity_pct REAL");
        d.execSQL("ALTER TABLE field_records ADD COLUMN substrate TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN weather_notes TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN amphibian_call_score INTEGER");
        d.execSQL("ALTER TABLE field_records ADD COLUMN microhabitat TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN planned_duration_s INTEGER");
        d.execSQL("ALTER TABLE surveys ADD COLUMN gps_track_m REAL DEFAULT 0");
        d.execSQL("ALTER TABLE surveys ADD COLUMN gps_track_status TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN searched_area_m2 REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN observer_effort_minutes REAL");
        // Represent pre-v0.9 bird point visits as legacy samples, without invented completion times.
        d.execSQL("INSERT OR IGNORE INTO point_counts(id,event_id,station_id,sequence_number,started,ended,target_seconds,completed,notes) "+
            "SELECT 'LEGACY-'+id,id,COALESCE(station_id,'UNSPECIFIED'),1,started,ended,COALESCE(point_target_s,600),complete,'Legacy record grouping only; station effort requires review' "+
            "FROM surveys WHERE method='point_count'");
        d.execSQL("UPDATE field_records SET point_count_id='LEGACY-'||event_id WHERE event_id IN (SELECT id FROM surveys WHERE method='point_count') AND point_count_id IS NULL");
    }
    private void upgradeSix(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS sampling_units("+
            "id TEXT PRIMARY KEY,event_id TEXT NOT NULL,station_id TEXT NOT NULL,"+
            "sequence_number INTEGER NOT NULL,method TEXT NOT NULL,started TEXT NOT NULL,ended TEXT,"+
            "target_seconds INTEGER,completed INTEGER,effort_minutes REAL,effort_area_m2 REAL,"+
            "effort_length_m REAL,noise_score INTEGER,wind_score INTEGER,rain_condition TEXT,"+
            "equipment_id TEXT,number_devices INTEGER,trap_effort_hours REAL,notes TEXT,"+
            "UNIQUE(event_id,sequence_number))");
        d.execSQL("CREATE INDEX IF NOT EXISTS units_by_survey ON sampling_units(event_id,sequence_number)");
        d.execSQL("ALTER TABLE field_records ADD COLUMN sampling_unit_id TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN equipment_id TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN count_basis TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN life_stage TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN evidence_type TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN perch_height_m REAL");
        d.execSQL("ALTER TABLE field_records ADD COLUMN call_noise_score INTEGER");
        d.execSQL("ALTER TABLE field_records ADD COLUMN unit_time_status TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN session_notes TEXT");
        d.execSQL("UPDATE field_records SET unit_time_status=COALESCE(protocol_window_flag,'unknown') WHERE unit_time_status IS NULL");
    }
    /** Preserve excluded records. No physical record deletion in an outcome change. */
    private void upgradeSeven(SQLiteDatabase d){
        d.execSQL("ALTER TABLE surveys ADD COLUMN outcome TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN exclusion_reason TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN outcome_changed_at TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN paused_at_utc TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN total_paused_ms INTEGER NOT NULL DEFAULT 0");
        d.execSQL("ALTER TABLE surveys ADD COLUMN effort_paused INTEGER NOT NULL DEFAULT 0");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN unit_paused_ms INTEGER NOT NULL DEFAULT 0");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN unit_paused_ms INTEGER NOT NULL DEFAULT 0");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN camera_days_verified REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN trap_nights_verified REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN recorder_hours_verified REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN deployment_started_utc TEXT");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN deployment_ended_utc TEXT");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN downtime_hours REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN effort_remarks TEXT");
        d.execSQL("CREATE TABLE IF NOT EXISTS survey_outcome_audit("+
          "id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT NOT NULL,previous_outcome TEXT,"+
          "new_outcome TEXT NOT NULL,changed_at_utc TEXT NOT NULL,reason TEXT,record_count INTEGER)");
        d.execSQL("CREATE TABLE IF NOT EXISTS survey_effort_pauses("+
          "id INTEGER PRIMARY KEY AUTOINCREMENT,event_id TEXT NOT NULL,paused_at_utc TEXT NOT NULL,"+
          "resumed_at_utc TEXT,duration_ms INTEGER,reason TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS survey_outcome_audit_event ON survey_outcome_audit(event_id)");
        d.execSQL("CREATE INDEX IF NOT EXISTS survey_pauses_event ON survey_effort_pauses(event_id)");
        d.execSQL("UPDATE surveys SET outcome=CASE WHEN ended IS NULL THEN 'active' "+
          "WHEN complete=1 THEN 'complete' ELSE 'incomplete' END WHERE outcome IS NULL");
    }
    private void upgradeEight(SQLiteDatabase d){
        d.execSQL("ALTER TABLE surveys ADD COLUMN geographic_region_id TEXT NOT NULL DEFAULT 'PH'");
        d.execSQL("ALTER TABLE surveys ADD COLUMN geographic_selection_method TEXT NOT NULL DEFAULT 'legacy_unspecified'");
        d.execSQL("ALTER TABLE surveys ADD COLUMN regional_taxa_version TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN identification_level TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN regional_evidence_status TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN geo_region_at_detection TEXT");
        d.execSQL("UPDATE surveys SET regional_taxa_version='legacy_no_region_filter' "+
             "WHERE regional_taxa_version IS NULL");
        d.execSQL("UPDATE field_records SET identification_level='legacy_not_assessed' "+
             "WHERE identification_level IS NULL");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_surveys_geo_region ON surveys(geographic_region_id)");
        d.execSQL("CREATE INDEX IF NOT EXISTS idx_field_records_identification ON field_records(identification_level)");
    }
    private void upgradeNine(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS taxon_catalog("+
          "taxon_key TEXT PRIMARY KEY,taxon_group TEXT NOT NULL,common_name TEXT,scientific_name TEXT NOT NULL,"+
          "family TEXT,source TEXT NOT NULL,source_version TEXT NOT NULL,region_codes TEXT,"+
          "review_status TEXT NOT NULL DEFAULT 'unreviewed',created_utc TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS catalog_group ON taxon_catalog(taxon_group,common_name)");
        d.execSQL("CREATE TABLE IF NOT EXISTS taxa_favorites("+
          "taxon_group TEXT NOT NULL,raw_identification TEXT NOT NULL,PRIMARY KEY(taxon_group,raw_identification))");
        d.execSQL("CREATE TABLE IF NOT EXISTS observation_media("+
          "id INTEGER PRIMARY KEY AUTOINCREMENT,record_id INTEGER NOT NULL,file_name TEXT NOT NULL,"+
          "mime_type TEXT NOT NULL,original_name TEXT,added_utc TEXT,source_note TEXT)");
        d.execSQL("CREATE INDEX IF NOT EXISTS media_record ON observation_media(record_id)");
        d.execSQL("ALTER TABLE surveys ADD COLUMN admin_province_name TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN admin_province_psgc TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN admin_boundary_version TEXT");
        d.execSQL("ALTER TABLE surveys ADD COLUMN admin_boundary_status TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN identification_confidence TEXT");
        d.execSQL("ALTER TABLE field_records ADD COLUMN identification_review_note TEXT");
        d.execSQL("UPDATE field_records SET identification_confidence='not_assessed' "+
          "WHERE identification_confidence IS NULL");
        // Initial names are explicitly unreviewed, never represented as a complete checklist.
        for(String group:Taxa.GROUPS){
            for(String item:Taxa.forGroup(group)){
                String[] parts=item.split("\\|",2);
                if(parts.length!=2)continue;
                String key="STARTER:"+group+":"+parts[1].toLowerCase(java.util.Locale.US);
                d.execSQL("INSERT OR IGNORE INTO taxon_catalog(taxon_key,taxon_group,common_name,"+
                   "scientific_name,source,source_version,region_codes,review_status) VALUES(?,?,?,?,?,?,?,?)",
                   new Object[]{key,group,parts[0],parts[1],"v0.12 offline starter","legacy_v0.12","","unreviewed"});
            }
        }
    }
    /**
     * Survey plans describe chainage, not pre-calculated geographic positions.
     * A station coordinate is observed when the count starts, not interpolated.
     */
    private void upgradeTen(SQLiteDatabase d){
        d.execSQL("ALTER TABLE surveys ADD COLUMN planned_route_m REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN planned_station_spacing_m REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN planned_station_count INTEGER");
        d.execSQL("ALTER TABLE surveys ADD COLUMN route_design TEXT");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN gps_latitude REAL");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN gps_longitude REAL");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN gps_accuracy_m REAL");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN gps_recorded_at TEXT");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN gps_status TEXT");
        d.execSQL("ALTER TABLE point_counts ADD COLUMN planned_chainage_m REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN gps_latitude REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN gps_longitude REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN gps_accuracy_m REAL");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN gps_recorded_at TEXT");
        d.execSQL("ALTER TABLE sampling_units ADD COLUMN gps_status TEXT");
        d.execSQL("CREATE TABLE IF NOT EXISTS survey_stations("+
            "event_id TEXT NOT NULL,station_id TEXT NOT NULL,sequence_number INTEGER NOT NULL,"+
            "planned_chainage_m REAL NOT NULL,visit_state TEXT NOT NULL DEFAULT 'planned',"+
            "captured_at TEXT,gps_latitude REAL,gps_longitude REAL,gps_accuracy_m REAL,"+
            "gps_status TEXT NOT NULL DEFAULT 'pending',"+
            "PRIMARY KEY(event_id,sequence_number),UNIQUE(event_id,station_id))");
        d.execSQL("CREATE INDEX IF NOT EXISTS survey_station_event ON survey_stations(event_id,visit_state)");
    }

    /**
     * First install and upgrades both install national wildlife catalogs locally.
     * The database upgrade is transactional. A corrupted embedded asset aborts
     * the upgrade rather than saving a partial species list or field record.
     */
    private void upgradeEighteen(SQLiteDatabase db){
        BundledCatalog.seed(appContext,db);
    }

    /** Taxonomic sources may link to external references; no images are bundled. */
    private void upgradeSeventeen(SQLiteDatabase database){
        for(String column:new String[]{"source_url","ebird_url","sound_url"}){
            database.execSQL("ALTER TABLE taxon_catalog ADD COLUMN "+column+" TEXT");
        }
    }
    /** Version 12: GNSS provenance, plus a version-pinned offline mammal candidate catalog. */
    /**
     * The presence of a taxon in a checklist and the evidence that a
     * taxon occurs in a particular locality are two different facts.
     * Only explicit attributed occurrence evidence belongs here.
     */
    private void upgradeSixteen(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS taxon_occurrence_evidence("+
            "taxon_key TEXT NOT NULL,area_type TEXT NOT NULL,"+
            "area_label TEXT NOT NULL,evidence_basis TEXT NOT NULL,"+
            "evidence_text TEXT NOT NULL,source TEXT NOT NULL,"+
            "source_version TEXT NOT NULL,source_url TEXT NOT NULL DEFAULT '',"+
            "review_status TEXT NOT NULL DEFAULT 'source_listed',"+
            "PRIMARY KEY(taxon_key,area_type,area_label,source,source_version))");
        d.execSQL("CREATE INDEX IF NOT EXISTS evidence_area "+
            "ON taxon_occurrence_evidence(area_type,area_label)");
        d.execSQL("CREATE INDEX IF NOT EXISTS evidence_taxon "+
            "ON taxon_occurrence_evidence(taxon_key)");
    }

    /** Detailed fields from the source's published checklist, not live IUCN claims. */
    private void upgradeFifteen(SQLiteDatabase d){
        for(String name:new String[]{"order_name","subfamily","subgenus","authority",
            "authority_year","distribution_text","distribution_status","ph_red_list",
            "iucn_red_list","cites","synonym_text","taxonomic_note"}){
            d.execSQL("ALTER TABLE taxon_catalog ADD COLUMN "+name+" TEXT");
        }
        d.execSQL("CREATE INDEX IF NOT EXISTS catalog_group_region_codes "+
            "ON taxon_catalog(taxon_group,region_codes)");
    }

    /** Version 14: attributed synonym crosswalk, independent of historic observations. */
    private void upgradeFourteen(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS taxon_synonyms("+
            "taxon_key TEXT NOT NULL,alias_name TEXT NOT NULL,"+
            "source TEXT NOT NULL,source_version TEXT NOT NULL,"+
            "PRIMARY KEY(taxon_key,alias_name,source,source_version))");
        d.execSQL("CREATE INDEX IF NOT EXISTS synonym_alias_nocase "+
            "ON taxon_synonyms(alias_name COLLATE NOCASE)");
        d.execSQL("CREATE INDEX IF NOT EXISTS synonym_taxon_key "+
            "ON taxon_synonyms(taxon_key)");
    }

    /** Version 13 improves large versioned checklist lookup without changing observations. */
    private void upgradeThirteen(SQLiteDatabase d){
        d.execSQL("CREATE INDEX IF NOT EXISTS catalog_group_scientific_nocase "+
            "ON taxon_catalog(taxon_group,scientific_name COLLATE NOCASE)");
        d.execSQL("CREATE INDEX IF NOT EXISTS catalog_group_common_nocase "+
            "ON taxon_catalog(taxon_group,common_name COLLATE NOCASE)");
        d.execSQL("CREATE INDEX IF NOT EXISTS catalog_source_version "+
            "ON taxon_catalog(source,source_version)");
    }

    private void upgradeTwelve(SQLiteDatabase d){
        d.execSQL("ALTER TABLE surveys ADD COLUMN start_gps_latitude REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN start_gps_longitude REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN start_gps_accuracy_m REAL");
        d.execSQL("ALTER TABLE surveys ADD COLUMN start_gps_recorded_at TEXT");
        // Legacy MDD imports displayed taxon keys with underscores as if they were names.
        d.execSQL("UPDATE taxon_catalog SET scientific_name=REPLACE(scientific_name,'_',' ') "+
            "WHERE taxon_key LIKE 'MDD:%' AND instr(scientific_name,'_')>0");
        seedMddMammals(d);
    }

    private void seedMddMammals(SQLiteDatabase d){
        try(InputStream input=appContext.getAssets().open("ph_mammals_mdd_v2_5.json")){
            ByteArrayOutputStream output=new ByteArrayOutputStream();
            byte[] chunk=new byte[8192];int n;
            while((n=input.read(chunk))!=-1){
                if(output.size()+n>2_000_000)throw new IllegalStateException("MDD asset unexpectedly large");
                output.write(chunk,0,n);
            }
            JSONArray mammals=new JSONArray(new String(output.toByteArray(),StandardCharsets.UTF_8));
            if(mammals.length()<20||mammals.length()>1200)
                throw new IllegalStateException("MDD candidate count out of expected range");
            SQLiteStatement stmt=d.compileStatement(
                "INSERT OR IGNORE INTO taxon_catalog(taxon_key,taxon_group,common_name,"+
                "scientific_name,family,source,source_version,region_codes,review_status,created_utc) "+
                "VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))");
            for(int i=0;i<mammals.length();i++){
                JSONObject name=mammals.getJSONObject(i);
                stmt.clearBindings();
                String[] fields={
                    name.getString("taxon_key"),"Mammals",name.optString("common_name"),
                    name.getString("scientific_name"),name.optString("family"),
                    "ASM Mammal Diversity Database, DOI 10.5281/zenodo.21654811",
                    "v2.5_2026-07-28","",name.getString("review_status")
                };
                for(int j=0;j<fields.length;j++)stmt.bindString(j+1,fields[j]);
                stmt.executeInsert();
            }
            stmt.close();
        }catch(java.io.FileNotFoundException missing){
            android.util.Log.w("WildlifeSurvey","Bundled mammal asset not present; manual MDD import remains available");
        }catch(Exception error){
            android.util.Log.e("WildlifeSurvey","Could not seed bundled MDD v2.5 mammals",error);
        }
    }

    /** Version 11: reusable point-count station identities, audit without data mutation. */
    private void upgradeEleven(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS route_templates("+
            "id TEXT PRIMARY KEY,project_id TEXT NOT NULL,name TEXT NOT NULL,"+
            "method TEXT NOT NULL,planned_length_m REAL NOT NULL,"+
            "station_spacing_m REAL NOT NULL,station_count INTEGER NOT NULL,"+
            "created_at_utc TEXT NOT NULL,notes TEXT,UNIQUE(project_id,name,method))");
        d.execSQL("CREATE TABLE IF NOT EXISTS route_template_stations("+
            "template_id TEXT NOT NULL,station_id TEXT NOT NULL,"+
            "sequence_number INTEGER NOT NULL,chainage_m REAL NOT NULL,"+
            "PRIMARY KEY(template_id,sequence_number),UNIQUE(template_id,station_id))");
        d.execSQL("CREATE INDEX IF NOT EXISTS route_templates_project_method ON route_templates(project_id,method)");
        d.execSQL("ALTER TABLE surveys ADD COLUMN route_template_id TEXT");
    }
}
