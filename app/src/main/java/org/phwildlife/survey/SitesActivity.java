package org.phwildlife.survey;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SitesActivity extends Activity {
    private FieldDb helper;
    private LinearLayout content;
    private Spinner projectSpinner;
    private ArrayList<String> pids=new ArrayList<>();
    private final int GREEN=Color.rgb(35,105,73);
    private static String now(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.format(new Date());}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5);}
    private TextView label(String t,int size){TextView x=new TextView(this);x.setText(t);x.setTextSize(size);x.setTextColor(Color.rgb(24,53,40));x.setPadding(dp(5),dp(8),dp(5),dp(8));return x;}
    private Button btn(String t){Button b=new Button(this);b.setText(t);b.setAllCaps(false);return b;}
    private EditText edit(String hint,String value){EditText x=new EditText(this);x.setHint(hint);x.setText(value==null?"":value);x.setSingleLine(true);x.setTextSize(15);return x;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    @Override public void onCreate(Bundle b){super.onCreate(b);helper=new FieldDb(this);render();}
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
    private String selectedProject(){int pos=projectSpinner==null?-1:projectSpinner.getSelectedItemPosition();return pos<0||pos>=pids.size()?"default":pids.get(pos);}
    private void render(){
        LinearLayout root=column();root.setPadding(dp(16),dp(16),dp(16),dp(16));root.setBackgroundColor(Color.rgb(246,249,246));
        root.addView(label("Projects & sampling stations",23));
        root.addView(label("Save stations once. Reuse their IDs for repeat visits and spatial analysis.",13));
        Button back=btn("← Home");back.setOnClickListener(v->finish());root.addView(back);
        Spinner previous=projectSpinner;String selected=previous==null?"default":selectedProject();
        projectSpinner=new Spinner(this);pids.clear();ArrayList<String> projects=new ArrayList<>();
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){pids.add(c.getString(0));projects.add(c.getString(1)+" · "+c.getString(0));}
        }
        projectSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,projects));
        int selectedIndex=pids.indexOf(selected);if(selectedIndex>=0)projectSpinner.setSelection(selectedIndex);
        root.addView(label("Active project",15));
        root.addView(projectSpinner);
        Button project=btn("＋ Add project");project.setOnClickListener(v->newProject());root.addView(project);
        Button site=btn("＋ Add sampling station");site.setOnClickListener(v->siteDialog(null));root.addView(site);
        ScrollView scroll=new ScrollView(this);content=column();scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        projectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int i,long id){renderSites();}
        });
        setContentView(root);renderSites();
    }
    private void renderSites(){
        if(content==null)return;content.removeAllViews();
        String projectId=selectedProject();
        content.addView(label("Stations in "+projectId,17));
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,habitat,latitude,longitude FROM sites WHERE project_id=? ORDER BY id",new String[]{projectId})){
            if(c.getCount()==0)content.addView(label("No stations. Add one above to start a structured survey.",13));
            while(c.moveToNext()){
                String id=c.getString(0);
                String coords=c.isNull(3)?"No coordinates":String.format(Locale.US,"%.5f, %.5f",c.getDouble(3),c.getDouble(4));
                Button edit=btn(c.getString(1)+" ("+id+")\n"+coords);
                edit.setAllCaps(false);
                edit.setOnClickListener(v->siteDialog(id));
                content.addView(edit);
            }
        }
    }
    private void newProject(){
        LinearLayout form=column();form.setPadding(dp(16),0,dp(16),0);
        EditText id=edit("Project ID (unique, e.g. EAGLE-2026)","");
        EditText name=edit("Project name","");
        form.addView(id);form.addView(name);
        new AlertDialog.Builder(this).setTitle("New project").setView(form).setNegativeButton("Cancel",null)
          .setPositiveButton("Save",(dlg,w)->{
              String i=id.getText().toString().trim(),n=name.getText().toString().trim();
              if(i.isEmpty()||n.isEmpty()){Toast.makeText(this,"Project ID and name required",Toast.LENGTH_LONG).show();return;}
              ContentValues v=new ContentValues();v.put("id",i);v.put("name",n);v.put("created_at",now());
              long res=helper.getWritableDatabase().insertWithOnConflict("projects",null,v,SQLiteDatabase.CONFLICT_IGNORE);
              if(res<0)Toast.makeText(this,"Project ID already exists",Toast.LENGTH_SHORT).show();
              render();int at=pids.indexOf(i);if(at>=0)projectSpinner.setSelection(at);
          }).show();
    }
    private static Double num(String s){try{return s.trim().isEmpty()?null:Double.valueOf(s.trim());}catch(Exception e){return null;}}
    private void siteDialog(String siteId){
        String[] original={"","","","","",""};
        String pid=selectedProject();
        if(siteId!=null){
            try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,COALESCE(habitat,''),latitude,longitude,COALESCE(notes,'') FROM sites WHERE id=?",new String[]{siteId})){
                if(c.moveToFirst())for(int i=0;i<6;i++)original[i]=c.isNull(i)?"":c.getString(i);
            }
        }
        LinearLayout form=column();form.setPadding(dp(16),0,dp(16),0);
        EditText[] entries={
            edit("Station ID (unique)",original[0]),
            edit("Site / station name",original[1]),
            edit("Habitat",original[2]),
            edit("Latitude (decimal degrees)",original[3]),
            edit("Longitude (decimal degrees)",original[4]),
            edit("Notes",original[5])};
        entries[3].setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        entries[4].setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        for(EditText e:entries)form.addView(e);
        if(siteId!=null)entries[0].setEnabled(false);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(siteId==null?"New sampling station":"Edit sampling station")
            .setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String id=entries[0].getText().toString().trim(),name=entries[1].getText().toString().trim();
            Double lat=num(entries[3].getText().toString()),lon=num(entries[4].getText().toString());
            if(id.isEmpty()||name.isEmpty()){Toast.makeText(this,"Station ID and name required",Toast.LENGTH_SHORT).show();return;}
            if((lat==null)!=(lon==null)||lat!=null&&(lat< -90||lat>90||lon< -180||lon>180)){
                Toast.makeText(this,"Enter valid latitude and longitude, or leave both blank",Toast.LENGTH_LONG).show();return;
            }
            ContentValues values=new ContentValues();
            values.put("id",id);values.put("project_id",pid);values.put("name",name);
            values.put("habitat",entries[2].getText().toString().trim());
            if(lat!=null){values.put("latitude",lat);values.put("longitude",lon);}
            else{values.putNull("latitude");values.putNull("longitude");}
            values.put("notes",entries[5].getText().toString().trim());
            values.put("updated_at",now());
            int rows=0;
            if(siteId==null){values.put("created_at",now());rows=helper.getWritableDatabase().insertWithOnConflict("sites",null,values,SQLiteDatabase.CONFLICT_IGNORE)<0?0:1;}
            else rows=helper.getWritableDatabase().update("sites",values,"id=?",new String[]{siteId});
            if(rows==0){Toast.makeText(this,"Station ID exists, choose a unique ID",Toast.LENGTH_SHORT).show();return;}
            dialog.dismiss();renderSites();
        }));
        dialog.show();
    }
}
