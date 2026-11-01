package org.phwildlife.survey;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Activates the owner's LIKAS Mindanao research checklist from a privately
 * selected CSV. Source rows are deliberately not checked into the public
 * repository or embedded in the downloadable APK before rights clearance.
 *
 * Prior catalogs, synonym/evidence records and reviewed habitat profiles are
 * archived inside the same SQLite transaction. Historic surveys, observations,
 * media, favorites and recorded identifications are never changed.
 */
public final class MindanaoMasterImporter {
    public static final String VERSION="v3.6_2026-11-01";
    private static final String PREFIX="LIKAS725:";
    private static final int COUNT=725;
    private static final String[] COLUMN_NAMES={
        "taxon_key","taxon_group","common_name","scientific_name","family",
        "source","source_version","region_codes","review_status","order_name",
        "subfamily","subgenus","authority","authority_year","distribution_text",
        "distribution_status","ph_red_list","iucn_red_list","cites",
        "synonym_text","taxonomic_note","source_url","ebird_url","sound_url"
    };
    private MindanaoMasterImporter(){}

    public static void validate(List<Map<String,String>> rows){
        if(rows==null||rows.size()!=COUNT)
            throw new IllegalArgumentException("Expected exactly 725 checklist species");
        HashSet<String> keys=new HashSet<>();
        HashSet<String> names=new HashSet<>();
        HashMap<String,Integer> groups=new HashMap<>();
        for(Map<String,String> row:rows){
            String key=field(row,"taxon_key");
            String name=field(row,"scientific_name");
            String group=field(row,"taxon_group");
            if(!key.startsWith(PREFIX)||!key.equals(PREFIX+
                    name.toLowerCase(java.util.Locale.ROOT)
                        .replaceAll("[^a-z0-9]+","_").replaceAll("^_|_$","")))
                throw new IllegalArgumentException("Invalid LIKAS taxon key: "+name);
            if(name.isEmpty()||field(row,"common_name").isEmpty()||
                    field(row,"family").isEmpty()||field(row,"order_name").isEmpty()||
                    field(row,"habitat_text").isEmpty()||
                    field(row,"elevation_text").isEmpty())
                throw new IllegalArgumentException("Incomplete species record: "+name);
            if(!VERSION.equals(field(row,"source_version"))||
                    !"unreviewed".equals(field(row,"review_status")))
                throw new IllegalArgumentException("Unsupported master edition / evidence class");
            if(!keys.add(key)||!names.add(name.toLowerCase(java.util.Locale.ROOT)))
                throw new IllegalArgumentException("Duplicate checklist species: "+name);
            groups.put(group,groups.containsKey(group)?groups.get(group)+1:1);
        }
        for(String group:Arrays.asList("Birds","Amphibians","Reptiles","Mammals")){
            int expected="Birds".equals(group)?414:"Amphibians".equals(group)?53:
                "Reptiles".equals(group)?156:102;
            if(groups.getOrDefault(group,0)!=expected)
                throw new IllegalArgumentException("Unexpected count for "+group);
        }
        if(groups.size()!=4)throw new IllegalArgumentException("Unrecognized wildlife group");
    }

    private static String field(Map<String,String> row,String key){
        String value=row.get(key);
        return value==null?"":value.trim();
    }

    public static void install(Context context,List<Map<String,String>> rows)throws Exception{
        validate(rows);
        try(FieldDb helper=new FieldDb(context)){
            SQLiteDatabase d=helper.getWritableDatabase();
            d.beginTransaction();
            try{
                // Archive the old reference rows within the same atomic operation.
                // Do not touch original detections, photos or field-survey records.
                archive(d,"taxon_catalog","likas_prior_taxon_catalog");
                archive(d,"taxon_synonyms","likas_prior_taxon_synonyms");
                archive(d,"taxon_occurrence_evidence","likas_prior_taxon_occurrence_evidence");
                archive(d,"taxon_habitat_profiles","likas_prior_taxon_habitat_profiles");
                d.execSQL("CREATE TABLE IF NOT EXISTS likas_master_attributes("+
                    "taxon_key TEXT PRIMARY KEY,habitat_text TEXT NOT NULL,"+
                    "elevation_text TEXT NOT NULL,source_version TEXT NOT NULL)");
                d.execSQL("CREATE TABLE IF NOT EXISTS likas_master_install_audit("+
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,installed_utc TEXT NOT NULL,"+
                    "source_version TEXT NOT NULL,active_species_count INTEGER NOT NULL)");
                d.execSQL("DELETE FROM taxon_synonyms");
                d.execSQL("DELETE FROM taxon_occurrence_evidence");
                d.execSQL("DELETE FROM taxon_habitat_profiles");
                d.execSQL("DELETE FROM taxon_catalog");
                d.execSQL("DELETE FROM likas_master_attributes");
                StringBuilder columns=new StringBuilder();
                StringBuilder placeholders=new StringBuilder();
                for(String name:COLUMN_NAMES){
                    if(columns.length()>0){columns.append(",");placeholders.append(",");}
                    columns.append(name);placeholders.append("?");
                }
                try(SQLiteStatement insert=d.compileStatement(
                        "INSERT INTO taxon_catalog("+columns+
                        ",created_utc) VALUES("+placeholders+",datetime('now'))");
                    SQLiteStatement detail=d.compileStatement(
                        "INSERT INTO likas_master_attributes("+
                        "taxon_key,habitat_text,elevation_text,source_version) "+
                        "VALUES(?,?,?,?)")){
                    for(Map<String,String> row:rows){
                        insert.clearBindings();
                        for(int i=0;i<COLUMN_NAMES.length;i++){
                            String value=field(row,COLUMN_NAMES[i]);
                            // Empty, unverified IUCN/DENR categories stay SQL NULL.
                            if(value.isEmpty()&&("iucn_red_list".equals(COLUMN_NAMES[i])||
                                    "ph_red_list".equals(COLUMN_NAMES[i])))
                                insert.bindNull(i+1);
                            else insert.bindString(i+1,value);
                        }
                        insert.executeInsert();
                        detail.clearBindings();
                        detail.bindString(1,field(row,"taxon_key"));
                        detail.bindString(2,field(row,"habitat_text"));
                        detail.bindString(3,field(row,"elevation_text"));
                        detail.bindString(4,VERSION);
                        detail.executeInsert();
                    }
                }
                long count=DatabaseUtils.longForQuery(d,
                    "SELECT COUNT(*) FROM taxon_catalog",null);
                long descriptions=DatabaseUtils.longForQuery(d,
                    "SELECT COUNT(*) FROM likas_master_attributes",null);
                if(count!=COUNT||descriptions!=COUNT)
                    throw new IllegalStateException("Catalog count mismatch; transaction rolled back");
                d.execSQL("INSERT INTO likas_master_install_audit("+
                    "installed_utc,source_version,active_species_count) "+
                    "VALUES(datetime('now'),?,?)",new Object[]{VERSION,COUNT});
                d.setTransactionSuccessful();
            }finally{d.endTransaction();}
        }
    }

    private static void archive(SQLiteDatabase d,String active,String archived){
        d.execSQL("CREATE TABLE IF NOT EXISTS "+archived+
            " AS SELECT * FROM "+active+" WHERE 0");
        d.execSQL("INSERT INTO "+archived+" SELECT * FROM "+active);
    }
}
