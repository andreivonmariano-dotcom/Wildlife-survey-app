package org.phwildlife.survey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Local, user-selected research source pack. The copyrighted source tables
 * are never built into the APK or written to the public source repository.
 * All four source CSVs must validate before an all-or-nothing database import.
 */
public final class ChecklistPackImporter {
    public static final String VERSION="WBCP2025-SY2025-2026-private-1";
    private ChecklistPackImporter(){}
    public static final class Result {
        public final int birds,amphibians,reptiles,evidence,synonyms;
        Result(int b,int a,int r,int e,int s){
            birds=b;amphibians=a;reptiles=r;evidence=e;synonyms=s;
        }
    }
    private static final String[] SPECIES_COLUMNS={
        "taxon_key","taxon_group","common_name","scientific_name","family",
        "source","source_version","region_codes","review_status",
        "order_name","subfamily","subgenus","authority","authority_year",
        "distribution_text","distribution_status","ph_red_list","iucn_red_list",
        "cites","synonym_text","taxonomic_note","source_url","ebird_url","sound_url"
    };
    private static final String[] EVIDENCE_COLUMNS={
        "taxon_key","area_type","area_label","evidence_basis","evidence_text",
        "source","source_version","source_url","review_status"
    };
    private static final String[] NAMES={
        "birds.csv","herpetofauna.csv","bird_evidence.csv",
        "herpetofauna_evidence.csv"
    };
    private static final String[] HASHES={
        "4c9c95878033604ae931096d2a50682ba9ed75d0e3c0f7ec11b1565f7f26209e",
        "c96057cfa6ec129f14142634a5a1d608df4a960e953d7ff9e0e6482a42a12b4b",
        "a699c657b92789c9931f57aa4b6171f12159ed7c527e1039817a0cd257d5bf28",
        "e52bf1d3c39d2c85b2e891c08a9caa91adae6116390b8b913205e63b0296be3d"
    };
    private static final int[] EXPECTED={761,496,179,184};

    public static Result importZip(Context context,InputStream stream)throws Exception{
        if(stream==null)throw new IllegalArgumentException("No checklist archive selected");
        Map<String,byte[]> files=new HashMap<>();
        int totalBytes=0;
        try(ZipInputStream zipped=new ZipInputStream(stream)){
            ZipEntry entry;
            while((entry=zipped.getNextEntry())!=null){
                if(entry.isDirectory())continue;
                int index=Arrays.asList(NAMES).indexOf(entry.getName());
                if(index<0)continue;
                if(files.containsKey(entry.getName()))
                    throw new IllegalArgumentException("Duplicate checklist file: "+entry.getName());
                ByteArrayOutputStream data=new ByteArrayOutputStream();
                byte[] chunk=new byte[8192];int size;
                while((size=zipped.read(chunk))!=-1){
                    data.write(chunk,0,size);
                    totalBytes+=size;
                    if(data.size()>5_000_000||totalBytes>12_000_000)
                        throw new IllegalArgumentException("Checklist file too large");
                }
                byte[] raw=data.toByteArray();
                if(!HASHES[index].equalsIgnoreCase(sha256(raw)))
                    throw new IllegalArgumentException("Unexpected version or changed data in "+entry.getName());
                files.put(entry.getName(),raw);
            }
        }
        List<List<Map<String,String>>> parsed=new ArrayList<>();
        for(int i=0;i<NAMES.length;i++){
            byte[] bytes=files.get(NAMES[i]);
            if(bytes==null)throw new IllegalArgumentException("Checklist package missing "+NAMES[i]);
            List<Map<String,String>> rows=parse(bytes,i<2?SPECIES_COLUMNS:EVIDENCE_COLUMNS);
            if(rows.size()!=EXPECTED[i])
                throw new IllegalArgumentException("Count mismatch for "+NAMES[i]+": "+rows.size());
            parsed.add(rows);
        }
        HashSet<String> ids=new HashSet<>();
        HashSet<String> named=new HashSet<>();
        int birds=0,amphibians=0,reptiles=0;
        for(int k=0;k<2;k++){
            for(Map<String,String> row:parsed.get(k)){
                String key=row.get("taxon_key"),group=row.get("taxon_group");
                String scientific=row.get("scientific_name");
                if(key.isEmpty()||scientific.isEmpty()||!ids.add(key))
                    throw new IllegalArgumentException("Duplicate/missing taxon identifier");
                if(!named.add(group+"|"+scientific.toLowerCase(java.util.Locale.ROOT)))
                    throw new IllegalArgumentException("Duplicate accepted scientific name");
                if(k==0){
                    if(!group.equals("Birds")||
                        !row.get("source").startsWith("Wild Bird Club of the Philippines")||
                        !row.get("source_version").equals("2025-04-24_IOC15.1"))
                        throw new IllegalArgumentException("WBCP source/version mismatch");
                    birds++;
                }else{
                    if(!row.get("source").startsWith("Emerson Y. Sy")||
                        !(row.get("source_version").equals("2025-1")&&group.equals("Amphibians")||
                          row.get("source_version").equals("2026-1")&&group.equals("Reptiles")))
                        throw new IllegalArgumentException("Sy source/version mismatch");
                    if(group.equals("Amphibians"))amphibians++;
                    else if(group.equals("Reptiles"))reptiles++;
                    else throw new IllegalArgumentException("Wrong herpetofauna group");
                }
            }
        }
        if(birds!=761||amphibians!=116||reptiles!=380)
            throw new IllegalArgumentException("Wrong bird/herpetofauna group counts");
        HashSet<String> evidenceIds=new HashSet<>();
        for(int k=2;k<4;k++){
            for(Map<String,String> row:parsed.get(k)){
                if(!ids.contains(row.get("taxon_key")))
                    throw new IllegalArgumentException("Regional evidence points to unknown taxon");
                if(!row.get("review_status").equals("source_listed"))
                    throw new IllegalArgumentException("Invalid regional review status");
                if(!row.get("area_type").equals("island")&&!row.get("area_type").equals("province"))
                    throw new IllegalArgumentException("Invalid regional area type");
                String id=row.get("taxon_key")+"|"+row.get("area_type")+"|"+
                    row.get("area_label")+"|"+row.get("source")+"|"+row.get("source_version");
                if(!evidenceIds.add(id))
                    throw new IllegalArgumentException("Duplicate occurrence record");
            }
        }

        int synonymCount=0;
        // DB and historical user observations stay untouched on failed source validation.
        try(FieldDb helper=new FieldDb(context.getApplicationContext())){
            SQLiteDatabase db=helper.getWritableDatabase();
            db.beginTransaction();
            try{
                StringBuilder columns=new StringBuilder(),values=new StringBuilder();
                for(String column:SPECIES_COLUMNS){
                    if(columns.length()>0){columns.append(",");values.append(",");}
                    columns.append(column);values.append("?");
                }
                try(SQLiteStatement taxon=db.compileStatement(
                        "INSERT OR REPLACE INTO taxon_catalog("+columns+
                        ",created_utc) VALUES("+values+",datetime('now'))");
                    SQLiteStatement alias=db.compileStatement(
                        "INSERT OR REPLACE INTO taxon_synonyms("+
                        "taxon_key,alias_name,source,source_version) VALUES(?,?,?,?)");
                    SQLiteStatement evidence=db.compileStatement(
                        "INSERT OR REPLACE INTO taxon_occurrence_evidence("+
                        "taxon_key,area_type,area_label,evidence_basis,evidence_text,"+
                        "source,source_version,source_url,review_status) VALUES(?,?,?,?,?,?,?,?,?)")){
                    for(int k=0;k<2;k++)for(Map<String,String> row:parsed.get(k)){
                        taxon.clearBindings();
                        for(int i=0;i<SPECIES_COLUMNS.length;i++)
                            taxon.bindString(i+1,row.get(SPECIES_COLUMNS[i]));
                        taxon.executeInsert();
                        String olderNames=row.get("synonym_text");
                        if(!olderNames.isEmpty())for(String part:olderNames.split(";")){
                            String older=part.trim();
                            if(older.isEmpty()||older.equalsIgnoreCase(row.get("scientific_name")))continue;
                            alias.clearBindings();
                            alias.bindString(1,row.get("taxon_key"));
                            alias.bindString(2,older);
                            alias.bindString(3,row.get("source"));
                            alias.bindString(4,row.get("source_version"));
                            alias.executeInsert();
                            synonymCount++;
                        }
                    }
                    for(int k=2;k<4;k++)for(Map<String,String> row:parsed.get(k)){
                        evidence.clearBindings();
                        for(int i=0;i<EVIDENCE_COLUMNS.length;i++)
                            evidence.bindString(i+1,row.get(EVIDENCE_COLUMNS[i]));
                        evidence.executeInsert();
                    }
                }
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }
        context.getSharedPreferences("likas_catalog_install",Context.MODE_PRIVATE)
            .edit().putString("source_pack",VERSION).apply();
        return new Result(birds,amphibians,reptiles,evidenceIds.size(),synonymCount);
    }
    private static String sha256(byte[] raw)throws Exception{
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(raw);
        StringBuilder result=new StringBuilder();
        for(byte b:digest)result.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        return result.toString();
    }
    private static List<Map<String,String>> parse(byte[] bytes,String[] required)throws Exception{
        String text=new String(bytes,StandardCharsets.UTF_8);
        if(!text.isEmpty()&&text.charAt(0)=='\uFEFF')text=text.substring(1);
        ArrayList<List<String>> table=new ArrayList<>();
        List<String> row=new ArrayList<>();
        StringBuilder field=new StringBuilder();
        boolean quoted=false;
        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(c=='"'){
                if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){
                    field.append('"');i++;
                }else quoted=!quoted;
            }else if(c==','&&!quoted){row.add(field.toString());field.setLength(0);}
            else if((c=='\n'||c=='\r')&&!quoted){
                if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;
                row.add(field.toString());field.setLength(0);
                table.add(row);row=new ArrayList<>();
            }else field.append(c);
        }
        if(quoted)throw new IllegalArgumentException("Unclosed CSV quoted field");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());table.add(row);}
        if(table.isEmpty())throw new IllegalArgumentException("Empty checklist CSV");
        List<String> head=table.get(0);
        for(String column:required)if(!head.contains(column)&&!column.equals("source_url")&&
                !column.equals("ebird_url")&&!column.equals("sound_url"))
            throw new IllegalArgumentException("Missing required catalog column: "+column);
        ArrayList<Map<String,String>> output=new ArrayList<>();
        for(int r=1;r<table.size();r++){
            List<String> values=table.get(r);
            if(values.size()==1&&values.get(0).trim().isEmpty())continue;
            if(values.size()!=head.size())
                throw new IllegalArgumentException("Malformed checklist CSV row "+(r+1));
            Map<String,String> parsed=new HashMap<>();
            for(int i=0;i<head.size();i++)parsed.put(head.get(i),values.get(i).trim());
            for(String column:required)parsed.putIfAbsent(column,"");
            output.add(parsed);
            if(output.size()>2000)throw new IllegalArgumentException("Checklist CSV exceeds limit");
        }
        return output;
    }
}
