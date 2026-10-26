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
    private static final int PICK=891,MAX=25*1024*1024;
    private FieldDb db;private TextView info;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);db=new FieldDb(this);
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(22,22,22,22);layout.setBackgroundColor(UiKit.BG);
        layout.addView(UiKit.title(this,"Offline taxonomy"));
        info=new TextView(this);info.setTextSize(15);info.setTextColor(UiKit.INK);layout.addView(info);
        Button choose=UiKit.button(this,"Import attributed species CSV","primary");
        layout.addView(choose);choose.setOnClickListener(v->pick());
        Button back=UiKit.button(this,"‹ Home","secondary");layout.addView(back);back.setOnClickListener(v->finish());
        setContentView(layout);refresh();
    }
    @Override protected void onDestroy(){super.onDestroy();db.close();}
    private void refresh(){
        int total=0,added=0;
        try(Cursor c=db.getReadableDatabase().rawQuery(
           "SELECT COUNT(*),SUM(CASE WHEN source<>'v0.12 offline starter' THEN 1 ELSE 0 END) FROM taxon_catalog",null)){
           if(c.moveToFirst()){total=c.getInt(0);added=c.isNull(1)?0:c.getInt(1);}
        }
        info.setText("Catalog: "+total+" names ("+added+" imported). The built-in starter is incomplete and unreviewed.\n\n"+
          "For a versioned checklist, prepare UTF-8 CSV with headers:\n"+
          "taxon_key,taxon_group,common_name,scientific_name,family,source,source_version,region_codes,review_status\n\n"+
          "Taxon group: Birds, Reptiles, Amphibians or Mammals. Separate regional codes with |, for example MINDANAO|P:1102400000. A P: code is a PSGC province code. Leave region_codes blank when distribution is not documented. review_status: reviewed or unreviewed.\n\n"+
          "Importer never treats an unlisted region as a confirmed absence. Source/version are required. Verified distribution evidence must be curated separately from accepted taxonomy.\n\n"+
          "Recommended avian taxonomy: AviList v2026 (CC BY 4.0). Distribution information needs an independent source.\n");
    }
    private void pick(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"text/csv","text/comma-separated-values","text/plain","application/octet-stream"});
        startActivityForResult(i,PICK);
    }
    @Override protected void onActivityResult(int req,int outcome,Intent data){
        super.onActivityResult(req,outcome,data);
        if(req!=PICK||outcome!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
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
