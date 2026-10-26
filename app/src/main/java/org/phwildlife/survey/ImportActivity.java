package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.net.Uri;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Imports the v0.7 CSV export into an EMPTY separate v0.8 Beta database. */
public class ImportActivity extends Activity {
    private static final int PICK_ZIP=41,MAX_UNCOMPRESSED=16*1024*1024,MAX_ROWS=100000;
    private FieldDb helper;private TextView status;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);helper=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25,25,25,25);root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,"Import v0.7 backup"));
        status=new TextView(this);status.setTextSize(15);status.setTextColor(UiKit.INK);
        status.setText("Import a ZIP exported by Wildlife Survey Beta v0.7. The import runs only on an empty v0.8 database and is all-or-nothing. Keep the previous app and its export until you have reviewed the imported survey history.");
        status.setPadding(10,20,10,28);root.addView(status);
        Button select=UiKit.button(this,"Choose backup ZIP","primary");
        root.addView(select);select.setOnClickListener(v->choose());
        Button back=UiKit.button(this,"‹ Home","secondary");root.addView(back);back.setOnClickListener(v->finish());
        setContentView(root);
    }
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
    private void choose(){
        if(!emptyDatabase()){status.setText("Existing observations or surveys detected. Import requires a new, empty database to prevent ID conflicts.");return;}
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");
        startActivityForResult(i,PICK_ZIP);
    }
    private boolean emptyDatabase(){
        try(Cursor c=helper.getReadableDatabase().rawQuery(
            "SELECT (SELECT COUNT(*) FROM surveys)+(SELECT COUNT(*) FROM field_records)",null)){
            return c.moveToFirst()&&c.getInt(0)==0;
        }
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req!=PICK_ZIP||result!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        try{
            Map<String,List<Map<String,String>>> contents=readArchive(uri);
            for(String name:new String[]{"projects.csv","sites.csv","surveys.csv","observations.csv","edit_history.csv"})
                if(!contents.containsKey(name))throw new Exception("Missing required export: "+name);
            int events=contents.get("surveys.csv").size(),records=contents.get("observations.csv").size();
            if(events==0)throw new Exception("The selected export does not contain any surveys.");
            new AlertDialog.Builder(this).setTitle("Confirm backup import")
                .setMessage("Load "+events+" survey events and "+records+" observation records, along with projects, stations and edit history? Existing v0.7 data will not be changed.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Import",(d,w)->apply(contents)).show();
        }catch(Exception e){status.setText("Import file could not be validated: "+e.getMessage());}
    }
    private Map<String,List<Map<String,String>>> readArchive(Uri uri)throws Exception{
        Map<String,List<Map<String,String>>> result=new HashMap<>();
        int used=0;
        try(InputStream stream=getContentResolver().openInputStream(uri);
            ZipInputStream zip=new ZipInputStream(stream)){
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null){
                String name=entry.getName();
                if(!Arrays.asList("projects.csv","sites.csv","surveys.csv","observations.csv","edit_history.csv").contains(name)){
                    zip.closeEntry();continue;
                }
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                byte[] buf=new byte[4096];int count;
                while((count=zip.read(buf))!=-1){
                    used+=count;
                    if(used>MAX_UNCOMPRESSED)throw new Exception("Export exceeds 16 MB safety limit");
                    out.write(buf,0,count);
                }
                List<List<String>> rows=parseCsv(new String(out.toByteArray(),StandardCharsets.UTF_8));
                if(rows.isEmpty())throw new Exception("Empty table: "+name);
                List<String> headers=rows.get(0);
                if(headers.isEmpty())throw new Exception("Missing CSV header");
                List<Map<String,String>> table=new ArrayList<>();
                for(int i=1;i<rows.size();i++){
                    List<String> cells=rows.get(i);
                    if(cells.size()==1&&cells.get(0).trim().isEmpty())continue;
                    if(cells.size()!=headers.size())throw new Exception("Invalid CSV columns in "+name);
                    Map<String,String> row=new HashMap<>();
                    for(int j=0;j<headers.size();j++)row.put(headers.get(j),cells.get(j));
                    table.add(row);
                    if(table.size()>MAX_ROWS)throw new Exception("Too many export rows");
                }
                result.put(name,table);zip.closeEntry();
            }
        }
        return result;
    }
    private List<List<String>> parseCsv(String content)throws Exception{
        List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();
        StringBuilder field=new StringBuilder();boolean quoted=false;
        for(int i=0;i<content.length();i++){
            char c=content.charAt(i);
            if(c=='"'){
                if(quoted&&i+1<content.length()&&content.charAt(i+1)=='"'){field.append('"');i++;}
                else quoted=!quoted;
            }else if(c==','&&!quoted){row.add(field.toString());field.setLength(0);}
            else if((c=='\n'||c=='\r')&&!quoted){
                if(c=='\r'&&i+1<content.length()&&content.charAt(i+1)=='\n')i++;
                row.add(field.toString());field.setLength(0);rows.add(row);row=new ArrayList<>();
            }else field.append(c);
        }
        if(quoted)throw new Exception("Unclosed CSV quote");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());rows.add(row);}
        return rows;
    }
    private static String val(Map<String,String> row,String key){return row.getOrDefault(key,"");}
    private static void str(ContentValues v,String col,Map<String,String> row,String key){
        String value=val(row,key);if(!value.isEmpty())v.put(col,value);
    }
    private static void num(ContentValues v,String col,Map<String,String> row,String key){
        String value=val(row,key);if(!value.isEmpty())v.put(col,Double.parseDouble(value));
    }
    private static void integer(ContentValues v,String col,Map<String,String> row,String key){
        String value=val(row,key);if(!value.isEmpty())v.put(col,Long.parseLong(value));
    }
    private static void insert(SQLiteDatabase d,String table,ContentValues v){
        d.insertOrThrow(table,null,v);
    }
    private void apply(Map<String,List<Map<String,String>>> files){
        if(!emptyDatabase()){status.setText("Import stopped. A survey or observation already exists.");return;}
        SQLiteDatabase d=helper.getWritableDatabase();d.beginTransaction();
        try{
            for(Map<String,String> r:files.get("projects.csv")){
                ContentValues v=new ContentValues();
                str(v,"id",r,"project_id");str(v,"name",r,"name");
                str(v,"description",r,"description");str(v,"created_at",r,"created_at");
                d.insertWithOnConflict("projects",null,v,SQLiteDatabase.CONFLICT_IGNORE);
            }
            for(Map<String,String> r:files.get("sites.csv")){
                ContentValues v=new ContentValues();
                str(v,"id",r,"station_id");str(v,"project_id",r,"project_id");
                str(v,"name",r,"name");str(v,"habitat",r,"habitat");
                num(v,"latitude",r,"latitude");num(v,"longitude",r,"longitude");
                num(v,"coordinate_uncertainty_m",r,"coordinate_uncertainty_m");
                str(v,"notes",r,"notes");str(v,"created_at",r,"created_at");str(v,"updated_at",r,"updated_at");
                insert(d,"sites",v);
            }
            for(Map<String,String> r:files.get("surveys.csv")){
                ContentValues v=new ContentValues();
                str(v,"id",r,"event_id");str(v,"method",r,"method");
                str(v,"started",r,"start_utc");str(v,"ended",r,"end_utc");
                num(v,"transect_az",r,"transect_magnetic_bearing_deg");
                str(v,"taxon_group",r,"initial_taxon_group");
                integer(v,"point_target_s",r,"point_target_seconds");
                num(v,"effort_length_m",r,"actual_route_length_m");
                integer(v,"complete",r,"completed");
                str(v,"survey_profile",r,"survey_profile");str(v,"station_id",r,"station_id");
                str(v,"observer_name",r,"observer_name");str(v,"protocol_status",r,"protocol_status");
                str(v,"project_id",r,"project_id");str(v,"region_label",r,"region_label");
                num(v,"area_km2",r,"stratum_area_km2");num(v,"point_radius_m",r,"point_radius_m");
                str(v,"weather",r,"weather");
                insert(d,"surveys",v);
            }
            for(Map<String,String> r:files.get("observations.csv")){
                ContentValues v=new ContentValues();
                integer(v,"id",r,"record_id");str(v,"event_id",r,"event_id");
                str(v,"detected_at",r,"detected_at_utc");str(v,"taxon_group",r,"taxon_group");
                str(v,"common_name",r,"common_name");str(v,"scientific_name",r,"scientific_name");
                str(v,"raw_identification",r,"raw_identification");
                integer(v,"individual_count",r,"individual_count");str(v,"detection_method",r,"detection_method");
                num(v,"horizontal_m",r,"horizontal_distance_m");num(v,"bearing_magnetic",r,"magnetic_bearing_deg");
                num(v,"pitch_deg",r,"pitch_deg");num(v,"perpendicular_m",r,"perpendicular_distance_m");
                num(v,"observer_lat",r,"observer_lat");num(v,"observer_lon",r,"observer_lon");
                num(v,"gps_accuracy_m",r,"gps_accuracy_m");
                str(v,"habitat",r,"habitat");str(v,"behavior",r,"behavior");str(v,"notes",r,"notes");
                integer(v,"compass_quality",r,"compass_quality");str(v,"name_source",r,"name_source");
                str(v,"distance_method",r,"distance_method");str(v,"initial_position",r,"initial_position");
                str(v,"movement_status",r,"movement_status");
                integer(v,"distance_eligible",r,"distance_candidate_flag");
                str(v,"updated_at",r,"updated_at");str(v,"deleted_at",r,"deleted_at");
                integer(v,"legacy_source_id",r,"legacy_source_id");
                insert(d,"field_records",v);
            }
            for(Map<String,String> r:files.get("edit_history.csv")){
                ContentValues v=new ContentValues();
                integer(v,"id",r,"audit_id");integer(v,"record_id",r,"record_id");
                str(v,"action",r,"action");str(v,"edited_at",r,"edited_at");
                str(v,"previous_json",r,"previous_json");
                str(v,"current_json",r,"current_json");str(v,"reason",r,"reason");
                insert(d,"record_edits",v);
            }
            // Imported v0.7 point count events predate station-level point-count IDs.
            d.execSQL("INSERT OR IGNORE INTO point_counts(id,event_id,station_id,sequence_number,started,ended,target_seconds,completed,notes) "+
                "SELECT 'LEGACY-'+id,id,COALESCE(station_id,'UNSPECIFIED'),1,started,ended,COALESCE(point_target_s,600),complete,'Imported legacy point count; verify effort' "+
                "FROM surveys WHERE method='point_count'");
            d.execSQL("UPDATE field_records SET point_count_id='LEGACY-'||event_id "+
                "WHERE point_count_id IS NULL AND event_id IN (SELECT id FROM surveys WHERE method='point_count')");
            d.setTransactionSuccessful();
            status.setText("Import successful: "+files.get("surveys.csv").size()+" survey events and "+
                files.get("observations.csv").size()+" observations restored. Check Survey History and export again for verification.");
        }catch(Exception e){status.setText("Import rolled back. No partial survey data were written: "+e.getMessage());}
        finally{d.endTransaction();}
    }
}
