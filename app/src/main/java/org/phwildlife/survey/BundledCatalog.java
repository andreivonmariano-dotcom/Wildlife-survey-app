package org.phwildlife.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;
import org.json.JSONArray;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.InflaterInputStream;

/**
 * Built-in, source-attributed wildlife checklists for a private research build.
 *
 * WBCP 2025 (IOC 15.1) and Emerson Y. Sy 2025-1/2026-1 were supplied by the
 * researcher. These assets must NOT be shipped in a public or commercial
 * application without confirming permission from the checklist owners.
 *
 * This class seeds catalog rows on database creation and migration.
 * Existing species and historic observations are never overwritten.
 */
public final class BundledCatalog {
    private BundledCatalog() {}
    public static final String ASSET="likas_ph_checklists_v2026_private.b64";
    public static final String ASSET_SHA256=
        "7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6";
    public static final int EXPECTED_BIRDS=761;
    public static final int EXPECTED_AMPHIBIANS=116;
    public static final int EXPECTED_REPTILES=380;
    public static final int EXPECTED_EVIDENCE=363;

    private static final String[] FIELDS={
        "taxon_key","taxon_group","common_name","scientific_name","family",
        "distribution_status","ph_red_list","iucn_red_list","region_codes",
        "synonym_text","distribution_text"
    };

    private static String source(String group){
        switch(group){
            case "Birds": return "Wild Bird Club of the Philippines (Brinkman et al. 2025)";
            case "Amphibians": return "Emerson Y. Sy, Checklist of Amphibians of the Philippines";
            case "Reptiles": return "Emerson Y. Sy, Checklist of Reptiles of the Philippines";
            default: throw new IllegalArgumentException("Unexpected taxon group");
        }
    }
    private static String sourceVersion(String group){
        switch(group){
            case "Birds": return "2025-04-24_IOC15.1";
            case "Amphibians": return "2025-1";
            case "Reptiles": return "2026-1";
            default: throw new IllegalArgumentException("Unexpected taxon group");
        }
    }
    private static String sourceUrl(String group){
        switch(group){
            case "Birds": return "https://birdwatch.ph/philippine-checklist/";
            case "Amphibians": return
                "https://www.researchgate.net/publication/360034142_Checklist_of_amphibians_of_the_Philippines_version_2025-1";
            case "Reptiles": return
                "https://www.researchgate.net/publication/362324825_Checklist_of_reptiles_of_the_Philippines_version_2026-1";
            default: return "";
        }
    }
    private static String hexDigest(byte[] bytes)throws Exception{
        StringBuilder result=new StringBuilder();
        for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))
            result.append(String.format(Locale.ROOT,"%02x",value&255));
        return result.toString();
    }
    private static JSONArray payload(Context context)throws Exception{
        ByteArrayOutputStream encoded=new ByteArrayOutputStream(85000);
        try(InputStream file=context.getAssets().open(ASSET)){
            byte[] buffer=new byte[8192];
            int length;
            while((length=file.read(buffer))!=-1){
                if(encoded.size()+length>100_000)
                    throw new IllegalArgumentException("Embedded catalog too large");
                encoded.write(buffer,0,length);
            }
        }
        byte[] compressed=Base64.decode(encoded.toByteArray(),Base64.DEFAULT);
        if(!hexDigest(compressed).equalsIgnoreCase(ASSET_SHA256))
            throw new IllegalArgumentException("Bundled catalog integrity mismatch");
        ByteArrayOutputStream decoded=new ByteArrayOutputStream(640_000);
        try(InflaterInputStream zip=new InflaterInputStream(
                new ByteArrayInputStream(compressed))){
            byte[] buffer=new byte[8192];
            int length;
            while((length=zip.read(buffer))!=-1){
                if(decoded.size()+length>900_000)
                    throw new IllegalArgumentException("Bundled catalog uncompressed size invalid");
                decoded.write(buffer,0,length);
            }
        }
        return new JSONArray(new String(decoded.toByteArray(),StandardCharsets.UTF_8));
    }
    static void seed(Context context,SQLiteDatabase db){
        try{
            JSONArray data=payload(context);
            if(data.length()!=2)throw new IllegalArgumentException("Catalog payload format");
            JSONArray taxa=data.getJSONArray(0),evidence=data.getJSONArray(1);
            if(taxa.length()!=EXPECTED_BIRDS+EXPECTED_AMPHIBIANS+EXPECTED_REPTILES||
                    evidence.length()!=EXPECTED_EVIDENCE)
                throw new IllegalArgumentException("Catalog count mismatch");
            int birds=0,amphibians=0,reptiles=0;
            HashMap<String,String> groups=new HashMap<>();
            HashMap<String,String> distribution=new HashMap<>();
            HashSet<String> used=new HashSet<>();
            // Pre-validate the complete bundle before making any SQL changes.
            for(int i=0;i<taxa.length();i++){
                JSONArray row=taxa.getJSONArray(i);
                if(row.length()!=FIELDS.length)throw new IllegalArgumentException("Catalog row shape");
                String key=row.getString(0),group=row.getString(1);
                if(key.isEmpty()||row.getString(3).isEmpty()||!used.add(key))
                    throw new IllegalArgumentException("Duplicate or incomplete catalog key");
                if(group.equals("Birds"))birds++;
                else if(group.equals("Amphibians"))amphibians++;
                else if(group.equals("Reptiles"))reptiles++;
                else throw new IllegalArgumentException("Unknown catalog group");
                groups.put(key,group);
                distribution.put(key,row.optString(10,""));
            }
            if(birds!=EXPECTED_BIRDS||amphibians!=EXPECTED_AMPHIBIANS||
                    reptiles!=EXPECTED_REPTILES)
                throw new IllegalArgumentException("Catalog group counts mismatch");
            for(int i=0;i<evidence.length();i++){
                JSONArray row=evidence.getJSONArray(i);
                if(row.length()!=3||!groups.containsKey(row.getString(0)))
                    throw new IllegalArgumentException("Orphaned regional evidence");
            }

            StringBuilder names=new StringBuilder(),holders=new StringBuilder();
            for(String field:FIELDS){
                if(names.length()>0){names.append(",");holders.append(",");}
                names.append(field);holders.append("?");
            }
            try(SQLiteStatement add=db.compileStatement(
                    "INSERT OR IGNORE INTO taxon_catalog("+names+
                    ",source,source_version,source_url,review_status,created_utc) VALUES("+
                    holders+",?,?,?,?,datetime('now'))");
                SQLiteStatement synonym=db.compileStatement(
                    "INSERT OR IGNORE INTO taxon_synonyms("+
                    "taxon_key,alias_name,source,source_version) VALUES(?,?,?,?)");
                SQLiteStatement region=db.compileStatement(
                    "INSERT OR IGNORE INTO taxon_occurrence_evidence("+
                    "taxon_key,area_type,area_label,evidence_basis,evidence_text,"+
                    "source,source_version,source_url,review_status) "+
                    "VALUES(?,?,?,?,?,?,?,?,?)")){
                for(int i=0;i<taxa.length();i++){
                    JSONArray row=taxa.getJSONArray(i);
                    String group=row.getString(1),key=row.getString(0);
                    add.clearBindings();
                    for(int n=0;n<FIELDS.length;n++)add.bindString(n+1,row.optString(n,""));
                    add.bindString(FIELDS.length+1,source(group));
                    add.bindString(FIELDS.length+2,sourceVersion(group));
                    add.bindString(FIELDS.length+3,sourceUrl(group));
                    add.bindString(FIELDS.length+4,group.equals("Birds")?"unreviewed":"reviewed");
                    add.executeInsert();
                    String earlier=row.optString(9,"");
                    if(earlier.isEmpty())continue;
                    for(String part:earlier.split(";")){
                        String old=part.trim();
                        if(old.isEmpty()||old.equalsIgnoreCase(row.getString(3)))continue;
                        synonym.clearBindings();
                        synonym.bindString(1,key);
                        synonym.bindString(2,old);
                        synonym.bindString(3,source(group));
                        synonym.bindString(4,sourceVersion(group));
                        synonym.executeInsert();
                    }
                }
                for(int i=0;i<evidence.length();i++){
                    JSONArray row=evidence.getJSONArray(i);
                    String key=row.getString(0),group=groups.get(key);
                    String place=row.getString(2),areaType=row.getString(1);
                    if(!areaType.equals("island")&&!areaType.equals("province"))
                        throw new IllegalArgumentException("Unknown area scope");
                    region.clearBindings();
                    region.bindString(1,key);
                    region.bindString(2,areaType);
                    region.bindString(3,place);
                    region.bindString(4,group.equals("Birds")?
                        "WBCP 2025 published range field":"Sy checklist distribution field");
                    String description=distribution.get(key);
                    region.bindString(5,description.isEmpty()?
                        "Named by the published checklist for "+place:description);
                    region.bindString(6,source(group));
                    region.bindString(7,sourceVersion(group));
                    region.bindString(8,sourceUrl(group));
                    region.bindString(9,"source_listed");
                    region.executeInsert();
                }
            }
            android.util.Log.i("LIKAS", "Bundled Philippine taxa seeded: "+taxa.length()+
                " species entries and "+evidence.length()+" source location references");
        }catch(Exception error){
            throw new IllegalStateException("Bundled source checklist installation failed",error);
        }
    }
}
