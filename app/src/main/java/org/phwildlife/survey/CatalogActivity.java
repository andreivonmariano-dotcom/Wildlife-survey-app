package org.phwildlife.survey;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Import a versioned, explicitly attributed checklist supplied by the researcher. */
public final class CatalogActivity extends Activity {
    private static final int PICK=891,PICK_MDD=892,PICK_SYNONYMS=893,MAX=25*1024*1024;
    private FieldDb db;private TextView info;
    private volatile boolean importing=false;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);db=new FieldDb(this);
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(22,22,22,22);layout.setBackgroundColor(UiKit.BG);
        layout.addView(UiKit.title(this,"Offline taxonomy"));
        info=new TextView(this);info.setTextSize(15);info.setTextColor(UiKit.INK);layout.addView(info);
        Button species=UiKit.button(this,"Find species information","primary");
        layout.addView(species);
        species.setOnClickListener(v->startActivity(
            new Intent(this,TaxonDetailActivity.class)));
        Button choose=UiKit.button(this,"Import species checklist CSV","secondary");
        layout.addView(choose);choose.setOnClickListener(v->pick());
        Button synonyms=UiKit.button(this,"Import name synonyms CSV","secondary");
        layout.addView(synonyms);synonyms.setOnClickListener(v->pickSynonyms());
        Button mammals=UiKit.button(this,"Import Philippine mammals from MDD v2.5 CSV","secondary");
        layout.addView(mammals);mammals.setOnClickListener(v->pickMdd());
        Button sources=UiKit.button(this,"Catalog sources and versions","secondary");
        layout.addView(sources);sources.setOnClickListener(v->showCatalogSources());
        Button help=UiKit.button(this,"Checklist import guide  ⓘ","quiet");
        layout.addView(help);
        help.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Checklist CSV format")
            .setMessage("UTF-8 CSV columns:\n"+
                "taxon_key, taxon_group, common_name, scientific_name, family, source, "+
                "source_version, region_codes, review_status\n\n"+
                "Taxon group: Birds, Reptiles, Amphibians or Mammals. "+
                "Use reviewed or unreviewed. Regional occurrence needs separate evidence. "+
                "National taxonomy does not verify presence at a survey station. "+
                "For mammals use the official MDD v2.5 source file.")
            .setPositiveButton("Close",null).show());
        Button back=UiKit.button(this,"‹ Home","secondary");layout.addView(back);back.setOnClickListener(v->finish());
        setContentView(layout);refresh();
    }
    @Override protected void onDestroy(){super.onDestroy();db.close();}
    private void refresh(){
        java.util.HashMap<String,int[]> counts=new java.util.HashMap<>();
        int total=0,starter=0;
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT taxon_group,COUNT(*),SUM(CASE WHEN taxon_key LIKE 'STARTER:%' "+
            "THEN 1 ELSE 0 END) FROM taxon_catalog GROUP BY taxon_group",null)){
            while(c.moveToNext()){
                counts.put(c.getString(0),new int[]{c.getInt(1),c.getInt(2)});
                total+=c.getInt(1);starter+=c.getInt(2);
            }
        }
        StringBuilder result=new StringBuilder("Offline catalog · "+total+" names");
        for(String category:new String[]{"Birds","Amphibians","Reptiles","Mammals"}){
            int[] row=counts.get(category);
            int all=row==null?0:row[0],legacy=row==null?0:row[1];
            result.append("\n").append(category).append(": ").append(all);
            if(legacy>0)result.append(" (").append(legacy).append(" starter)");
        }
        result.append("\nNational candidates are not verified local sightings.");
        info.setText(result.toString());
    }
    private void showCatalogSources(){
        StringBuilder sources=new StringBuilder();
        try(Cursor c=db.getReadableDatabase().rawQuery(
            "SELECT taxon_group,source,source_version,COUNT(*) FROM taxon_catalog "+
            "WHERE taxon_key NOT LIKE 'STARTER:%' "+
            "GROUP BY taxon_group,source,source_version "+
            "ORDER BY taxon_group,source LIMIT 40",null)){
            while(c.moveToNext()){
                if(sources.length()>0)sources.append("\n\n");
                sources.append(c.getString(0)).append(" · ")
                    .append(c.getInt(3)).append(" names\n")
                    .append(c.getString(1)).append(" · ")
                    .append(c.getString(2));
            }
        }
        new AlertDialog.Builder(this).setTitle("Catalog sources and versions")
            .setMessage(sources.length()==0?
                "Only legacy starter names are installed.":
                sources.toString()+"\n\nSource membership is not proof of local occurrence.")
            .setPositiveButton("Close",null).show();
    }
    private void pickMdd(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"text/csv","text/plain","application/octet-stream"});
        startActivityForResult(i,PICK_MDD);
    }
    private void importMdd(Uri uri){
        // Verify the exact source version through its official release filename.
        String name="";
        try(Cursor c=getContentResolver().query(uri,new String[]{
            android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(c!=null&&c.moveToFirst())name=c.getString(0);
        }catch(Exception ignored){}
        if(name==null||!name.matches("(?i).*MDD_v2[.]5_6904species[.]csv")){
            info.setText("Select the official MDD_v2.5_6904species.csv from Zenodo release 21654811.");
            return;
        }
        info.setText("Reading source and verifying country distribution…");
        new Thread(()->{
            try{
                List<List<String>> all=parse(new String(read(uri),StandardCharsets.UTF_8));
                if(all.isEmpty())throw new Exception("Empty MDD file");
                List<String> h=all.get(0);if(!h.isEmpty())h.set(0,h.get(0).replace("\ufeff",""));
                String[] required={"id","sciName","mainCommonName","family","countryDistribution","extinct","domestic","flagged"};
                for(String nameField:required)if(!h.contains(nameField))
                    throw new Exception("MDD CSV header missing "+nameField);
                ArrayList<Map<String,String>> selected=new ArrayList<>();
                for(int i=1;i<all.size();i++){
                    List<String> row=all.get(i);
                    if(row.size()!=h.size())continue; // Header-aligned data only.
                    HashMap<String,String> fields=new HashMap<>();
                    for(String key:required)fields.put(key,row.get(h.indexOf(key)).trim());
                    if(!countryHasPhilippines(fields.get("countryDistribution")))continue;
                    if(isTrue(fields.get("extinct"))||isTrue(fields.get("domestic")))continue;
                    if(fields.get("id").isEmpty()||fields.get("sciName").isEmpty())continue;
                    selected.add(fields);
                }
                if(selected.size()<20)throw new Exception("Too few Philippine mammals. Check source CSV and its countryDistribution field.");
                runOnUiThread(()->new AlertDialog.Builder(this)
                    .setTitle("Import Philippine mammal candidates?")
                    .setMessage(selected.size()+" living, non-domestic taxa have Philippines in the MDD v2.5 country list. "+
                        "Their island/province distribution is NOT verified. MDD flagged taxa stay unreviewed. Replace matching MDD IDs only.")
                    .setNegativeButton("Cancel",(d,w)->refresh())
                    .setPositiveButton("Import",(d,w)->{
                        new Thread(()->{
                            try{
                                SQLiteDatabase database=db.getWritableDatabase();
                                database.beginTransaction();
                                try{
                                    android.database.sqlite.SQLiteStatement statement=database.compileStatement(
                                        "INSERT OR REPLACE INTO taxon_catalog(taxon_key,taxon_group,common_name,"+
                                        "scientific_name,family,source,source_version,region_codes,"+
                                        "review_status,created_utc) VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))");
                                    for(Map<String,String> entry:selected){
                                        statement.clearBindings();
                                        String[] values={
                                            "MDD:"+entry.get("id"),"Mammals",entry.get("mainCommonName"),
                                            entry.get("sciName").replace('_',' '),entry.get("family"),
                                            "ASM Mammal Diversity Database, DOI 10.5281/zenodo.21654811",
                                            "v2.5_2026-07-28","",(isTrue(entry.get("flagged"))||
                                             countryUncertainPhilippines(entry.get("countryDistribution")))?"unreviewed":"reviewed"
                                        };
                                        for(int j=0;j<values.length;j++)statement.bindString(j+1,
                                            values[j]==null?"":values[j]);
                                        statement.executeInsert();
                                    }
                                    database.setTransactionSuccessful();
                                }finally{database.endTransaction();}
                                runOnUiThread(()->{
                                    refresh();
                                    info.append("\n\nImported "+selected.size()+" Philippine mammal candidate taxa from MDD v2.5.");
                                });
                            }catch(Exception failure){
                                runOnUiThread(()->info.setText("MDD import error: "+failure.getMessage()));
                            }
                        }).start();
                    }).show());
            }catch(Exception ex){
                runOnUiThread(()->info.setText("MDD validation failed: "+ex.getMessage()));
            }
        }).start();
    }
    private static boolean countryHasPhilippines(String names){
        if(names==null)return false;
        for(String item:names.split("[|;,]"))
            if(item.trim().replace("?","").trim().equalsIgnoreCase("Philippines"))return true;
        return false;
    }
    private static boolean countryUncertainPhilippines(String names){
        if(names==null)return false;
        for(String item:names.split("[|;,]"))
            if(item.contains("?")&&item.trim().replace("?","").trim().equalsIgnoreCase("Philippines"))
                return true;
        return false;
    }
    private static boolean isTrue(String value){
        if(value==null)return false;
        return value.equals("1")||value.equalsIgnoreCase("TRUE")||value.equalsIgnoreCase("yes");
    }
    private void pick(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"text/csv","text/comma-separated-values","text/plain","application/octet-stream"});
        startActivityForResult(i,PICK);
    }
    private void pickSynonyms(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{
            "text/csv","text/comma-separated-values","text/plain","application/octet-stream"});
        startActivityForResult(i,PICK_SYNONYMS);
    }
    private void importSynonyms(Uri uri){
        if(importing){info.setText("A checklist import is already running");return;}
        importing=true;info.setText("Checking scientific-name synonyms…");
        new Thread(()->{
            try{
                List<Map<String,String>> rows=validateSynonyms(uri);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()){importing=false;return;}
                    new AlertDialog.Builder(this).setTitle("Import old scientific names?")
                        .setMessage(rows.size()+" synonym links will be matched to installed "+
                            "taxon IDs. Historic observations stay unchanged.")
                        .setNegativeButton("Cancel",(dialog,which)->importing=false)
                        .setOnCancelListener(dialog->importing=false)
                        .setPositiveButton("Import",(dialog,which)->applySynonyms(rows)).show();
                });
            }catch(Exception e){
                importing=false;
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())
                    info.setText("Synonym CSV invalid: "+e.getMessage());});
            }
        },"LIKAS-Synonym-Validation").start();
    }
    private List<Map<String,String>> validateSynonyms(Uri uri)throws Exception{
        List<List<String>> lines=parse(new String(read(uri),StandardCharsets.UTF_8));
        if(lines.size()<2)throw new Exception("Empty synonym CSV");
        List<String> headers=lines.get(0);
        if(!headers.isEmpty())headers.set(0,headers.get(0).replace("\uFEFF",""));
        String[] required={"accepted_taxon_key","alias_name","source","source_version"};
        for(String key:required)if(!headers.contains(key))
            throw new Exception("Missing CSV column: "+key);
        ArrayList<Map<String,String>> rows=new ArrayList<>();
        java.util.HashSet<String> seen=new java.util.HashSet<>();
        for(int i=1;i<lines.size();i++){
            List<String> values=lines.get(i);
            if(values.size()==1&&values.get(0).trim().isEmpty())continue;
            if(values.size()!=headers.size())throw new Exception("Malformed row "+(i+1));
            Map<String,String> row=new HashMap<>();
            for(int n=0;n<headers.size();n++)
                row.put(headers.get(n),values.get(n).trim());
            for(String key:required)if(row.get(key).isEmpty())
                throw new Exception("Missing "+key+" at row "+(i+1));
            String unique=row.get("accepted_taxon_key")+"|"+
                row.get("alias_name").toLowerCase(Locale.ROOT)+"|"+
                row.get("source")+"|"+row.get("source_version");
            if(!seen.add(unique))throw new Exception("Duplicate synonym link at row "+(i+1));
            rows.add(row);
            if(rows.size()>45000)throw new Exception("Maximum 45,000 synonym names");
        }
        if(rows.isEmpty())throw new Exception("No valid synonyms");
        return rows;
    }
    private void applySynonyms(List<Map<String,String>> rows){
        info.setText("Importing verified synonyms…");
        new Thread(()->{
            try(FieldDb helper=new FieldDb(getApplicationContext())){
                SQLiteDatabase database=helper.getWritableDatabase();
                int[] inserted={0};
                database.beginTransaction();
                try{
                    try(android.database.sqlite.SQLiteStatement exists=database.compileStatement(
                        "SELECT COUNT(*) FROM taxon_catalog WHERE taxon_key=?");
                        android.database.sqlite.SQLiteStatement insert=database.compileStatement(
                        "INSERT OR REPLACE INTO taxon_synonyms("+
                        "taxon_key,alias_name,source,source_version) VALUES(?,?,?,?)")){
                        for(Map<String,String> row:rows){
                            exists.clearBindings();
                            exists.bindString(1,row.get("accepted_taxon_key"));
                            if(exists.simpleQueryForLong()!=1L)
                                throw new IllegalArgumentException("Unknown accepted taxon key: "+
                                    row.get("accepted_taxon_key"));
                            insert.clearBindings();
                            String[] values={row.get("accepted_taxon_key"),
                                row.get("alias_name"),row.get("source"),
                                row.get("source_version")};
                            for(int i=0;i<values.length;i++)
                                insert.bindString(i+1,values[i]);
                            insert.executeInsert();inserted[0]++;
                        }
                    }
                    database.setTransactionSuccessful();
                }finally{database.endTransaction();}
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())
                    info.setText("Imported "+inserted[0]+
                        " synonyms. Original survey names remain intact.");});
            }catch(Exception e){
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())
                    info.setText("Synonyms not imported: "+e.getMessage());});
            }finally{importing=false;}
        },"LIKAS-Synonym-Import").start();
    }
    @Override protected void onActivityResult(int req,int outcome,Intent data){
        super.onActivityResult(req,outcome,data);
        if((req!=PICK&&req!=PICK_MDD&&req!=PICK_SYNONYMS)||
            outcome!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        if(req==PICK_MDD){importMdd(uri);return;}
        if(req==PICK_SYNONYMS){importSynonyms(uri);return;}
        if(importing){info.setText("Another checklist import is running");return;}
        importing=true;
        info.setText("Checking checklist file…");
        new Thread(()->{
            try{
                List<Map<String,String>> valid=validateChecklist(uri);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()){importing=false;return;}
                    new AlertDialog.Builder(this).setTitle("Import checklist?")
                        .setMessage("Import "+valid.size()+" names? Existing observations stay unchanged.")
                        .setNegativeButton("Cancel",(dialog,which)->importing=false)
                        .setPositiveButton("Import",(dialog,which)->applyInBackground(valid))
                        .setOnCancelListener(dialog->importing=false).show();
                });
            }catch(Exception ex){
                importing=false;
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())
                    info.setText("Checklist validation failed: "+ex.getMessage());});
            }
        },"LIKAS-Checklist-Validation").start();
    }
    private List<Map<String,String>> validateChecklist(Uri uri)throws Exception{
            byte[] csv=read(uri);
            List<List<String>> lines=parse(new String(csv,StandardCharsets.UTF_8));
            if(lines.size()<2)throw new Exception("Checklist CSV has no species rows");
            List<String> h=lines.get(0);
            if(!h.isEmpty())h.set(0,h.get(0).replace("\ufeff",""));
            String[] required={"taxon_key","taxon_group","common_name","scientific_name","family","source","source_version","region_codes","review_status"};
            for(String key:required)if(!h.contains(key))throw new Exception("Required header missing: "+key);
            ArrayList<Map<String,String>> valid=new ArrayList<>();
            for(int i=1;i<lines.size();i++){
                List<String> row=lines.get(i);
                if(row.size()==1&&row.get(0).isEmpty())continue;
                if(row.size()!=h.size())throw new Exception("Column mismatch on row "+(i+1));
                Map<String,String> map=new HashMap<>();
                for(int n=0;n<h.size();n++)map.put(h.get(n),row.get(n).trim());
                for(String key:new String[]{"taxon_key","taxon_group","scientific_name","source","source_version"}){
                    if(map.get(key).isEmpty())throw new Exception("Missing "+key+" at row "+(i+1));
                }
                if(!Arrays.asList(Taxa.GROUPS).contains(map.get("taxon_group")))
                    throw new Exception("Unknown taxon group at row "+(i+1));
                String regions=map.get("region_codes");
                if(!regions.isEmpty())for(String code:regions.split("\\|")){
                    if(!Arrays.asList(GeoRegions.IDS).contains(code)&&!code.matches("P:[0-9]{9,10}"))
                        throw new Exception("Invalid region code '"+code+"' at row "+(i+1));
                }
                String status=map.get("review_status");
                if(!status.equals("reviewed")&&!status.equals("unreviewed"))
                    throw new Exception("Use reviewed or unreviewed at row "+(i+1));
                valid.add(map);
                if(valid.size()>45000)throw new Exception("Maximum 45,000 species names per import");
            }
        return valid;
    }
    private void applyInBackground(List<Map<String,String>> rows){
        info.setText("Importing "+rows.size()+" names…");
        new Thread(()->{
            int changed=0;
            try(FieldDb helper=new FieldDb(getApplicationContext())){
                SQLiteDatabase writable=helper.getWritableDatabase();
                writable.beginTransaction();
                String[] keys={"taxon_key","taxon_group","common_name","scientific_name",
                    "family","source","source_version","region_codes","review_status",
                    "order_name","subfamily","subgenus","authority","authority_year",
                    "distribution_text","distribution_status","ph_red_list","iucn_red_list",
                    "cites","synonym_text","taxonomic_note"};
                StringBuilder columns=new StringBuilder(),placeholders=new StringBuilder();
                for(String key:keys){
                    if(columns.length()>0){columns.append(",");placeholders.append(",");}
                    columns.append(key);placeholders.append("?");
                }
                try(android.database.sqlite.SQLiteStatement statement=writable.compileStatement(
                    "INSERT OR REPLACE INTO taxon_catalog("+columns+",created_utc) VALUES("+
                        placeholders+",datetime('now'))");
                    android.database.sqlite.SQLiteStatement alias=writable.compileStatement(
                        "INSERT OR REPLACE INTO taxon_synonyms("+
                        "taxon_key,alias_name,source,source_version) VALUES(?,?,?,?)")){
                    for(Map<String,String> row:rows){
                        statement.clearBindings();
                        for(int i=0;i<keys.length;i++)
                            statement.bindString(i+1,row.getOrDefault(keys[i],""));
                        statement.executeInsert();
                        // Sy's original amphibian spreadsheet includes multiple
                        // prior scientific names in some synonym cells.
                        String synonyms=row.getOrDefault("synonym_text","");
                        if(!synonyms.isEmpty()){
                            for(String item:synonyms.split(";")){
                                String older=item.trim();
                                if(older.isEmpty()||
                                    older.equalsIgnoreCase(row.get("scientific_name")))continue;
                                alias.clearBindings();
                                alias.bindString(1,row.get("taxon_key"));
                                alias.bindString(2,older);
                                alias.bindString(3,row.get("source"));
                                alias.bindString(4,row.get("source_version"));
                                alias.executeInsert();
                            }
                        }
                        changed++;
                    }
                    writable.setTransactionSuccessful();
                }finally{writable.endTransaction();}
                final int count=changed;
                runOnUiThread(()->{
                    if(!isFinishing()&&!isDestroyed()){
                        refresh();
                        info.append("\nImported "+count+" checklist entries.");
                    }
                });
            }catch(Exception ex){
                runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())
                    info.setText("Import rolled back: "+ex.getMessage());});
            }finally{importing=false;}
        },"LIKAS-Checklist-Import").start();
    }
    private byte[] read(Uri uri)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try(InputStream in=getContentResolver().openInputStream(uri)){
            byte[] bytes=new byte[8192];int n;
            while((n=in.read(bytes))!=-1){
                if(out.size()+n>MAX)throw new Exception("25 MB file size limit");
                out.write(bytes,0,n);
            }
        }
        return out.toByteArray();
    }
    private static List<List<String>> parse(String text)throws Exception{
        ArrayList<List<String>> all=new ArrayList<>();ArrayList<String> row=new ArrayList<>();
        StringBuilder field=new StringBuilder();boolean quotes=false;
        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(c=='"'){
                if(quotes&&i+1<text.length()&&text.charAt(i+1)=='"'){field.append('"');i++;}
                else quotes=!quotes;
            }else if(c==','&&!quotes){row.add(field.toString());field.setLength(0);}
            else if((c=='\n'||c=='\r')&&!quotes){
                if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;
                row.add(field.toString());field.setLength(0);
                all.add(row);row=new ArrayList<>();
            }else field.append(c);
        }
        if(quotes)throw new Exception("Unclosed CSV quote");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());all.add(row);}
        return all;
    }
}
