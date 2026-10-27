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
    private static final int PICK=891,PICK_MDD=892,MAX=25*1024*1024;
    private FieldDb db;private TextView info;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);db=new FieldDb(this);
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(22,22,22,22);layout.setBackgroundColor(UiKit.BG);
        layout.addView(UiKit.title(this,"Offline taxonomy"));
        info=new TextView(this);info.setTextSize(15);info.setTextColor(UiKit.INK);layout.addView(info);
        Button choose=UiKit.button(this,"Import attributed species CSV","primary");
        layout.addView(choose);choose.setOnClickListener(v->pick());
        Button mammals=UiKit.button(this,"Import Philippine mammals from MDD v2.5 CSV","secondary");
        layout.addView(mammals);mammals.setOnClickListener(v->pickMdd());
        Button back=UiKit.button(this,"‹ Home","secondary");layout.addView(back);back.setOnClickListener(v->finish());
        setContentView(layout);refresh();
    }
    @Override protected void onDestroy(){super.onDestroy();db.close();}
    private void refresh(){
        int total=0,added=0,mdd=0;
        try(Cursor c=db.getReadableDatabase().rawQuery(
           "SELECT COUNT(*),SUM(CASE WHEN source<>'v0.12 offline starter' THEN 1 ELSE 0 END) FROM taxon_catalog",null)){
           if(c.moveToFirst()){total=c.getInt(0);added=c.isNull(1)?0:c.getInt(1);}
        }
        try(Cursor mammal=db.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM taxon_catalog WHERE taxon_group='Mammals' AND taxon_key LIKE 'MDD:%'",null)){
            if(mammal.moveToFirst())mdd=mammal.getInt(0);
        }
        info.setText("Catalog: "+total+" names ("+added+" from non-starter sources), including "+
          mdd+" Philippine mammal candidates from MDD v2.5. The other groups still have incomplete starter lists.\n\n"+
          "For a versioned checklist, prepare UTF-8 CSV with headers:\n"+
          "taxon_key,taxon_group,common_name,scientific_name,family,source,source_version,region_codes,review_status\n\n"+
          "Taxon group: Birds, Reptiles, Amphibians or Mammals. Separate regional codes with |, for example MINDANAO|P:1102400000. A P: code is a PSGC province code. Leave region_codes blank when distribution is not documented. review_status: reviewed or unreviewed.\n\n"+
          "Importer never treats an unlisted region as a confirmed absence. Source/version are required. Verified distribution evidence must be curated separately from accepted taxonomy.\n\n"+
          "Recommended avian taxonomy: AviList v2026 (CC BY 4.0).\n\n"+
          "Mammals: select the official MDD_v2.5_6904species.csv file. The app selects species whose countryDistribution includes Philippines, excluding domestic and recently extinct taxa. "+
          "This is a national candidate species list, not proof of occurrence on any island or province. Philippine Mammal Project locality review is still required.\n"+
          "Amphibians: Emerson Y. Sy 2025-1. Reptiles: Emerson Y. Sy 2026-1. "+
          "Use the attributed checklist CSV importer for researcher-reviewed entries from those sources.\n");
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
    @Override protected void onActivityResult(int req,int outcome,Intent data){
        super.onActivityResult(req,outcome,data);
        if((req!=PICK&&req!=PICK_MDD)||outcome!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        if(req==PICK_MDD){importMdd(uri);return;}
        try{
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
            new AlertDialog.Builder(this).setTitle("Import checklist?")
                .setMessage("Upsert "+valid.size()+" entries. Source and version are stored per name. Existing observations are not modified.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Import",(d,w)->apply(valid)).show();
        }catch(Exception ex){info.setText("Checklist validation failed: "+ex.getMessage());}
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
    private void apply(List<Map<String,String>> rows){
        SQLiteDatabase d=db.getWritableDatabase();int changed=0;
        d.beginTransaction();
        try{
            android.database.sqlite.SQLiteStatement statement=d.compileStatement(
                "INSERT OR REPLACE INTO taxon_catalog(taxon_key,taxon_group,common_name,scientific_name,"+
                "family,source,source_version,region_codes,review_status,created_utc) VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))");
            for(Map<String,String> r:rows){
                statement.clearBindings();
                String[] keys={"taxon_key","taxon_group","common_name","scientific_name","family","source","source_version","region_codes","review_status"};
                for(int j=0;j<keys.length;j++)statement.bindString(j+1,r.get(keys[j]));
                statement.executeInsert();changed++;
            }
            d.setTransactionSuccessful();
            info.setText("Imported "+changed+" entries.");
        }catch(Exception ex){info.setText("Import rolled back: "+ex.getMessage());}
        finally{d.endTransaction();}
        refresh();
    }
}
