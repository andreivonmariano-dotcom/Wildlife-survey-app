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
    private static String now(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US){{setTimeZone(TimeZone.getTimeZone("UTC"));}}.format(new Date());}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5);}
    private TextView label(String t,int size){TextView x=new TextView(this);x.setText(t);x.setTextSize(size);x.setTextColor(UiKit.INK);x.setPadding(dp(5),dp(8),dp(5),dp(8));return x;}
    private Button btn(String t){return UiKit.button(this,t,"secondary");}
    private EditText edit(String hint,String value){EditText x=new EditText(this);x.setHint(hint);x.setText(value==null?"":value);x.setSingleLine(true);x.setTextSize(15);return x;}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    @Override public void onCreate(Bundle b){super.onCreate(b);helper=new FieldDb(this);render();}
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
    private String selectedProject(){int pos=projectSpinner==null?-1:projectSpinner.getSelectedItemPosition();return pos<0||pos>=pids.size()?"default":pids.get(pos);}
    private void render(){
        LinearLayout root=column();root.setPadding(dp(16),dp(16),dp(16),dp(16));root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Projects & sites","Home",this::finish));
        TextView introduction=label("Organize your projects and repeat-visit stations.",13);
        introduction.setTextColor(UiKit.MUTED);root.addView(introduction);
        Button explanation=UiKit.button(this,"About station identities  ▾","quiet");
        root.addView(explanation);
        explanation.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Projects and station IDs")
            .setMessage("Create a project before starting a survey. Survey routes generate permanent station IDs. You may correct a station's reference coordinates. Existing visit GPS snapshots remain unchanged.")
            .setPositiveButton("Close",null).show());
        Spinner previous=projectSpinner;String selected=previous==null?"default":selectedProject();
        projectSpinner=new Spinner(this);pids.clear();ArrayList<String> projects=new ArrayList<>();
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){pids.add(c.getString(0));projects.add(c.getString(1));}
        }
        projectSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,projects));
        int selectedIndex=pids.indexOf(selected);if(selectedIndex>=0)projectSpinner.setSelection(selectedIndex);
        root.addView(label("Active project",15));
        root.addView(projectSpinner);
        Button project=UiKit.button(this,"＋ Add project","primary");project.setOnClickListener(v->newProject());root.addView(project);
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
        content.addView(UiKit.sectionTitle(this,"Sampling stations"));
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,habitat,latitude,longitude FROM sites WHERE project_id=? ORDER BY id",new String[]{projectId})){
            if(c.getCount()==0)content.addView(label("No stations yet. Start a survey to generate stations automatically.",13));
            while(c.moveToNext()){
                String id=c.getString(0);
                String coords=c.isNull(3)?"No coordinates":String.format(Locale.US,"%.5f, %.5f",c.getDouble(3),c.getDouble(4));
                Button edit=btn(c.getString(1)+"\n"+coords);
                edit.setAllCaps(false);
                edit.setOnClickListener(v->siteDialog(id));
                content.addView(edit);
            }
        }
    }
    /** Permanent device-independent identifiers, checked again by SQLite's primary key. */
    private String newId(String prefix,String table){
        SQLiteDatabase db=helper.getReadableDatabase();
        for(int tries=0;tries<12;tries++){
            String random=UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.US);
            String id=prefix+"-"+random;
            try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM "+table+" WHERE id=?",new String[]{id})){
                if(c.moveToFirst()&&c.getInt(0)==0)return id;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique identifier");
    }
    private void newProject(){
        final String proposed=newId("PRJ","projects");
        LinearLayout form=column();form.setPadding(dp(16),0,dp(16),0);
        form.addView(label("Permanent project ID · generated automatically",12));
        TextView idLabel=label(proposed,14);
        idLabel.setTextIsSelectable(true);form.addView(idLabel);
        EditText name=edit("Project name (required)","");
        form.addView(name);
        form.addView(label("Name the project. The ID stays unchanged so observations and stations keep their original links.",12));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("New project").setView(form)
            .setNegativeButton("Cancel",null).setPositiveButton("Create project",null).create();
        dialog.setOnShowListener(w->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String n=name.getText().toString().trim();
            if(n.isEmpty()){name.setError("Project name is required");name.requestFocus();return;}
            ContentValues values=new ContentValues();
            values.put("id",proposed);values.put("name",n);values.put("created_at",now());
            long result=helper.getWritableDatabase().insertWithOnConflict(
                "projects",null,values,SQLiteDatabase.CONFLICT_IGNORE);
            if(result<0){
                Toast.makeText(this,"The ID already exists. Reopen New project to generate another.",Toast.LENGTH_LONG).show();
                return;
            }
            dialog.dismiss();
            render();
            int at=pids.indexOf(proposed);if(at>=0)projectSpinner.setSelection(at);
        }));
        dialog.show();
    }
    private static Double num(String s){try{return s.trim().isEmpty()?null:Double.valueOf(s.trim());}catch(Exception e){return null;}}
    private void siteDialog(String siteId){
        if(siteId==null)return; // New stations are generated from a survey, not here.
        String[] original={"","","","","",""};
        String pid=selectedProject();
        if(siteId!=null){
            try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,COALESCE(habitat,''),latitude,longitude,COALESCE(notes,'') FROM sites WHERE id=?",new String[]{siteId})){
                if(c.moveToFirst())for(int i=0;i<6;i++)original[i]=c.isNull(i)?"":c.getString(i);
            }
        }
        LinearLayout form=column();form.setPadding(dp(16),0,dp(16),0);
        form.addView(label("Permanent sampling station ID · generated automatically",12));
        TextView generatedId=label(original[0],14);
        generatedId.setTextIsSelectable(true);
        form.addView(generatedId);
        EditText[] entries={
            edit("Station ID (permanent)",original[0]),
            edit("Site / station name",original[1]),
            edit("Habitat",original[2]),
            edit("Latitude (decimal degrees)",original[3]),
            edit("Longitude (decimal degrees)",original[4]),
            edit("Notes",original[5])};
        entries[3].setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        entries[4].setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        // A station ID is stable from creation onward. Observers never type or change it.
        entries[0].setVisibility(View.GONE);
        for(EditText e:entries)form.addView(e);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);
        form.addView(label("Coordinate edits here do not change saved point-count GPS snapshots. Manually changed coordinates lose the original GPS accuracy value.",12));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit saved sampling station")
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
            Double prevLat=num(original[3]),prevLon=num(original[4]);
            boolean changedCoords=lat==null?prevLat!=null||prevLon!=null:
                prevLat==null||prevLon==null||Double.compare(prevLat,lat)!=0||Double.compare(prevLon,lon)!=0;
            if(changedCoords)values.putNull("coordinate_uncertainty_m");
            values.put("notes",entries[5].getText().toString().trim());
            values.put("updated_at",now());
            int rows=helper.getWritableDatabase().update("sites",values,"id=?",new String[]{siteId});
            if(rows==0){Toast.makeText(this,"Station no longer exists",Toast.LENGTH_SHORT).show();return;}
            dialog.dismiss();renderSites();
        }));
        dialog.show();
    }
}
