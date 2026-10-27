package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/** Configurable standards are project/method scoped and copied into new surveys. */
public final class StudyProtocolsActivity extends Activity {
    private FieldDb store;
    private LinearLayout screen,templates;
    private final ArrayList<String> projectIds=new ArrayList<>();
    private final ArrayList<String> methods=new ArrayList<>();
    private Spinner project,method;
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView label(String value){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(14);
        t.setTextColor(UiKit.INK);t.setPadding(dp(4),dp(6),dp(4),dp(6));return t;
    }
    private EditText entry(String hint,boolean number){
        EditText e=new EditText(this);e.setTextSize(15);e.setHint(hint);
        if(number)e.setInputType(InputType.TYPE_CLASS_NUMBER);
        return e;
    }
    private static String utc(){
        SimpleDateFormat d=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        d.setTimeZone(TimeZone.getTimeZone("UTC"));return d.format(new Date());
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);store=new FieldDb(this);draw();
    }
    private void draw(){
        ScrollView scroll=new ScrollView(this);
        screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(14),dp(12),dp(14),dp(18));screen.setBackgroundColor(UiKit.BG);
        scroll.addView(screen);setContentView(scroll);
        Button back=UiKit.button(this,"‹ Back","secondary");
        back.setOnClickListener(v->finish());screen.addView(back);
        screen.addView(label("Study protocol templates"));
        screen.addView(label("Rules are copied into each new survey. Past surveys keep their original rules."));
        project=new Spinner(this);
        ArrayList<String> projects=new ArrayList<>();projectIds.clear();
        try(Cursor c=store.getReadableDatabase().rawQuery(
            "SELECT id,name FROM projects ORDER BY name",null)){
            while(c.moveToNext()){projectIds.add(c.getString(0));projects.add(c.getString(1));}
        }
        if(projects.isEmpty()){projectIds.add("default");projects.add("General field project");}
        project.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,projects));
        screen.addView(label("Project"));screen.addView(project);
        method=new Spinner(this);methods.clear();
        ArrayList<String> labels=new ArrayList<>();
        for(String group:new String[]{"Birds","Herpetofauna","Mammals"}){
            for(SurveyMethods.Method m:SurveyMethods.forProfile(group)){
                if(methods.contains(m.code))continue;
                methods.add(m.code);labels.add(group+" · "+m.label);
            }
        }
        method.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,labels));
        screen.addView(label("Survey method"));screen.addView(method);
        EditText name=entry("Study template name",false);
        EditText visits=entry("Minimum completed visits (0 = default)",true);visits.setText("0");
        EditText minutes=entry("Minimum active sampling minutes (0 = default)",true);minutes.setText("0");
        EditText units=entry("Minimum evidence units/passes/devices (0 = default)",true);units.setText("0");
        CheckBox gps=new CheckBox(this);gps.setText("Require a verified survey-start GPS fix for Complete");
        CheckBox photo=new CheckBox(this);photo.setText("Require at least one archived evidence photo");
        for(EditText e:new EditText[]{name,visits,minutes,units})screen.addView(e);
        screen.addView(gps);screen.addView(photo);
        Button save=UiKit.button(this,"Save study template","primary");screen.addView(save);
        save.setOnClickListener(v->{
            String title=name.getText().toString().trim();
            if(title.isEmpty()){name.setError("Name required");return;}
            Integer minVisit=parse(visits),minTime=parse(minutes),minUnit=parse(units);
            if(minVisit==null||minTime==null||minUnit==null||minVisit>2000||
                minTime>43200||minUnit>100000){
                android.widget.Toast.makeText(this,"Enter nonnegative reasonable effort thresholds",
                    android.widget.Toast.LENGTH_LONG).show();return;
            }
            String selectedMethod=methods.get(method.getSelectedItemPosition());
            boolean hasUnits=!"line_transect".equals(selectedMethod)&&
                !"ves_transect".equals(selectedMethod)&&
                !"opportunistic".equals(selectedMethod);
            if(!hasUnits&&(minVisit>0||minUnit>0)){
                visits.setError("This method has no recorded sampling visits or method units");
                android.widget.Toast.makeText(this,
                    "Use minimum effort minutes for an unsegmented transect or incidental log",
                    android.widget.Toast.LENGTH_LONG).show();return;
            }
            ContentValues row=new ContentValues();
            row.put("id",UUID.randomUUID().toString());
            row.put("project_id",projectIds.get(project.getSelectedItemPosition()));
            row.put("method",methods.get(method.getSelectedItemPosition()));
            row.put("name",title);row.put("min_visits",minVisit);
            row.put("min_minutes",minTime);row.put("min_units",minUnit);
            row.put("require_verified_gps",gps.isChecked()?1:0);
            row.put("require_protocol_photo",photo.isChecked()?1:0);
            row.put("created_at_utc",utc());
            store.getWritableDatabase().insertOrThrow("study_protocol_templates",null,row);
            name.setText("");reloadTemplates();
        });
        screen.addView(label("Saved templates"));
        templates=new LinearLayout(this);templates.setOrientation(LinearLayout.VERTICAL);
        screen.addView(templates);reloadTemplates();
    }
    private Integer parse(EditText e){
        try{
            int n=Integer.parseInt(e.getText().toString().trim());
            return n>=0?n:null;
        }catch(Exception err){return null;}
    }
    private void reloadTemplates(){
        templates.removeAllViews();
        try(Cursor c=store.getReadableDatabase().rawQuery(
            "SELECT id,project_id,method,name,min_visits,min_minutes,min_units,"+
            "require_verified_gps,require_protocol_photo "+
            "FROM study_protocol_templates ORDER BY created_at_utc DESC LIMIT 150",null)){
            if(c.getCount()==0)templates.addView(label("No custom templates yet."));
            while(c.moveToNext()){
                String id=c.getString(0);
                String detail=c.getString(3)+" · "+c.getString(1)+" · "+c.getString(2)+
                    "\nVisits "+c.getInt(4)+" · minutes "+c.getInt(5)+" · units "+c.getInt(6)+
                    " · start GPS "+(c.getInt(7)>0?"required":"optional")+
                    " · photo "+(c.getInt(8)>0?"required":"optional");
                Button item=UiKit.button(this,detail,"secondary");
                item.setAllCaps(false);item.setMinHeight(dp(80));templates.addView(item);
                String methodCode=c.getString(2),name=c.getString(3);
                int oldVisits=c.getInt(4),oldMinutes=c.getInt(5),oldUnits=c.getInt(6);
                boolean oldGps=c.getInt(7)!=0,oldPhoto=c.getInt(8)!=0;
                item.setOnClickListener(v->new AlertDialog.Builder(this)
                    .setTitle(name)
                    .setItems(new String[]{"Edit template","Delete template"},(dialog,which)->{
                        if(which==0)editTemplate(id,methodCode,name,oldVisits,
                            oldMinutes,oldUnits,oldGps,oldPhoto);
                        else new AlertDialog.Builder(this)
                            .setTitle("Delete this template?")
                            .setMessage("Past survey requirements stay unchanged.")
                            .setNegativeButton("Keep",null)
                            .setPositiveButton("Delete",(d,w)->{
                                store.getWritableDatabase().delete(
                                    "study_protocol_templates","id=?",new String[]{id});
                                reloadTemplates();
                            }).show();
                    }).setNegativeButton("Close",null).show());
            }
        }
    }
    private void editTemplate(String id,String code,String existingName,
            int oldVisits,int oldMinutes,int oldUnits,boolean oldGps,boolean oldPhoto){
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18),dp(7),dp(18),dp(8));
        EditText title=entry("Template name",false);title.setText(existingName);
        EditText visits=entry("Minimum visits",true);visits.setText(String.valueOf(oldVisits));
        EditText minutes=entry("Minimum active effort minutes",true);
        minutes.setText(String.valueOf(oldMinutes));
        EditText units=entry("Minimum documented effort units",true);
        units.setText(String.valueOf(oldUnits));
        CheckBox gps=new CheckBox(this);gps.setText("Verified survey-start GPS required");
        gps.setChecked(oldGps);
        CheckBox photo=new CheckBox(this);photo.setText("At least one archived photo");
        photo.setChecked(oldPhoto);
        panel.addView(title);panel.addView(visits);panel.addView(minutes);
        panel.addView(units);panel.addView(gps);panel.addView(photo);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit study requirements")
            .setView(panel).setNegativeButton("Cancel",null)
            .setPositiveButton("Save changes",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String label=title.getText().toString().trim();
                Integer a=parse(visits),b=parse(minutes),c=parse(units);
                if(label.isEmpty()){title.setError("Name required");return;}
                if(a==null||b==null||c==null||a>2000||b>43200||c>100000){
                    units.setError("Check effort thresholds");return;
                }
                if(("line_transect".equals(code)||"ves_transect".equals(code)||
                    "opportunistic".equals(code))&&(a>0||c>0)){
                    visits.setError("This method has no station or unit visit count");return;
                }
                ContentValues update=new ContentValues();
                update.put("name",label);update.put("min_visits",a);
                update.put("min_minutes",b);update.put("min_units",c);
                update.put("require_verified_gps",gps.isChecked()?1:0);
                update.put("require_protocol_photo",photo.isChecked()?1:0);
                store.getWritableDatabase().update("study_protocol_templates",
                    update,"id=?",new String[]{id});
                dialog.dismiss();reloadTemplates();
            }));
        dialog.show();
    }
    @Override protected void onDestroy(){store.close();super.onDestroy();}
}
