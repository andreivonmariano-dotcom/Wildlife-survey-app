package org.phwildlife.survey;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Installs references supplied only to a private owner build.
 *
 * The publicly built APK contains neither reference. The optional private
 * Gradle asset directory is excluded from Git and must have BOTH validated
 * owner files. No observations, historical identifications, surveys or media
 * are changed. The already-present PAIC catalog or local map is preserved.
 */
public final class BundledPrivateReferences {
    static final String CATALOG="paic714_master.csv";
    static final String MAP="paic_worldcover2020_500m.txt";
    private static final String CATALOG_SHA=
        "1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959";
    private static final String MAP_SHA=
        "e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089";
    private static final int MAX_CSV=512*1024,MAX_MAP=512*1024;
    private BundledPrivateReferences(){}

    private static boolean exists(Context context,String name){
        try(InputStream input=context.getAssets().open(name)){return input.read()>=0;}
        catch(IOException missing){return false;}
    }
    public static boolean packaged(Context context){
        return exists(context,CATALOG)||exists(context,MAP);
    }
    public static boolean hasBundledCatalog(Context context){
        return exists(context,CATALOG);
    }
    public static boolean hasBundledMap(Context context){
        return exists(context,MAP);
    }
    private static byte[] bytes(Context context,String name,int max)throws Exception{
        try(InputStream input=context.getAssets().open(name);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=input.read(b))!=-1){
                if(out.size()+n>max)throw new IllegalArgumentException("Oversized private reference "+name);
                out.write(b,0,n);
            }
            return out.toByteArray();
        }
    }
    private static void check(byte[] file,String expected)throws Exception{
        byte[] sha=MessageDigest.getInstance("SHA-256").digest(file);
        StringBuilder hex=new StringBuilder();
        for(byte b:sha)hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        if(!expected.equals(hex.toString()))
            throw new IllegalArgumentException("Bundled reference checksum mismatch");
    }
    /**
     * Quote-aware RFC4180-like parser. Handles CR/LF, double quotes and commas
     * inside names or sources. The exact SHA and importer roster validation
     * together reject altered, incomplete or mismatched owner checklists.
     */
    static List<Map<String,String>> parse(String text){
        ArrayList<List<String>> lines=new ArrayList<>();
        ArrayList<String> record=new ArrayList<>();
        StringBuilder field=new StringBuilder();
        boolean quoted=false;
        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(c=='"'){
                if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){
                    field.append('"');i++;
                }else quoted=!quoted;
            }else if(c==','&&!quoted){
                record.add(field.toString());field.setLength(0);
            }else if((c=='\r'||c=='\n')&&!quoted){
                if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;
                record.add(field.toString());field.setLength(0);
                lines.add(record);record=new ArrayList<>();
                if(lines.size()>716)throw new IllegalArgumentException("Too many catalog records");
            }else field.append(c);
        }
        if(quoted)throw new IllegalArgumentException("Incomplete quoted catalog field");
        if(field.length()>0||!record.isEmpty()){
            record.add(field.toString());lines.add(record);
        }
        if(lines.size()!=715)throw new IllegalArgumentException("Expected 714 catalog rows");
        List<String> header=lines.get(0);
        if(!header.isEmpty())header.set(0,header.get(0).replace("\ufeff",""));
        if(header.size()!=26)throw new IllegalArgumentException("Unexpected catalog header");
        ArrayList<Map<String,String>> rows=new ArrayList<>();
        for(int i=1;i<lines.size();i++){
            List<String> columns=lines.get(i);
            if(columns.size()!=header.size())throw new IllegalArgumentException(
                "Catalog column mismatch at row "+(i+1));
            Map<String,String> row=new HashMap<>();
            for(int j=0;j<header.size();j++)
                row.put(header.get(j),columns.get(j).trim());
            rows.add(row);
        }
        return rows;
    }
    private static boolean activeMaster(Context context)throws Exception{
        try(FieldDb helper=new FieldDb(context);
            Cursor c=helper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM taxon_catalog WHERE taxon_key LIKE 'LIKAS725:%' "+
                "AND source_version=?",new String[]{MindanaoMasterImporter.VERSION})){
            return c.moveToFirst()&&c.getInt(0)==714;
        }
    }
    /**
     * Called on a worker thread before LIKAS leaves the startup screen.
     * An installation is attempted only once; a failure remains visible
     * and may be retried on next launch. No re-import on ordinary app opens.
     */
    public static void ensureInstalled(Context context)throws Exception{
        boolean hasCatalog=hasBundledCatalog(context),hasMap=hasBundledMap(context);
        if(!hasCatalog&&!hasMap)return; // Standard public APK.
        if(!hasCatalog||!hasMap)
            throw new IllegalStateException("Private build is missing a required reference");
        SharedPreferences state=context.getSharedPreferences("likas_private_references",
            Context.MODE_PRIVATE);
        if(state.getBoolean("installed_v1",false))return;
        // Verify both files BEFORE changing the existing reference database.
        byte[] catalog=bytes(context,CATALOG,MAX_CSV);
        byte[] map=bytes(context,MAP,MAX_MAP);
        check(catalog,CATALOG_SHA);check(map,MAP_SHA);
        if(!activeMaster(context)){
            List<Map<String,String>> rows=parse(new String(catalog,StandardCharsets.UTF_8));
            MindanaoMasterImporter.validate(rows);
            MindanaoMasterImporter.validateApprovedRoster(rows);
            MindanaoMasterImporter.install(context,rows);
        }
        // An existing, observer-installed map is left alone.
        if(!PaicLandCoverPack.installed(context)){
            try(java.io.ByteArrayInputStream input=new java.io.ByteArrayInputStream(map)){
                PaicLandCoverPack.install(context,input);
            }
        }
        if(!state.edit().putBoolean("installed_v1",true).commit())
            throw new IOException("Unable to save private reference installation state");
    }
}
