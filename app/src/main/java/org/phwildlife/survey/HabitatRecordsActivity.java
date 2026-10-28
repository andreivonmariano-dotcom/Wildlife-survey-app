package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/** Offline habitat reference separated from real station measurements. Read-only. */
public final class HabitatRecordsActivity extends Activity {
    private FieldDb helper;
    private LinearLayout items;
    private Button guideTab,recordsTab;
    private int selectedTab=0;
    private String habitatGroup="All habitats";
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView label(String content,int size){
        TextView value=new TextView(this);
        value.setText(content);
        value.setTextSize(size);
        value.setTextColor(UiKit.INK);
        value.setLineSpacing(dp(3),1.04f);
        value.setPadding(dp(3),dp(5),dp(3),dp(5));
        return value;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        helper=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.addView(UiKit.title(this,"Habitat & Ecology"));
        TextView note=label("Offline reference guide and separately recorded field evidence.",13);
        note.setTextColor(UiKit.MUTED);
        root.addView(note);
        LinearLayout tabs=new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        guideTab=UiKit.button(this,"Habitat guide","primary");
        recordsTab=UiKit.button(this,"My site records","secondary");
        tabs.addView(guideTab,new LinearLayout.LayoutParams(0,dp(50),1));
        LinearLayout.LayoutParams other=new LinearLayout.LayoutParams(0,dp(50),1);
        other.leftMargin=dp(8);
        tabs.addView(recordsTab,other);
        root.addView(tabs);
        guideTab.setOnClickListener(v->{selectedTab=0;render();});
        recordsTab.setOnClickListener(v->{selectedTab=1;render();});
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        items=new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setPadding(0,dp(8),0,dp(20));
        scroll.addView(items);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button back=UiKit.button(this,"Back to workspace","quiet");
        back.setOnClickListener(v->finish());
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
        setContentView(root);
        render();
    }
    @Override protected void onResume(){super.onResume();if(items!=null)render();}
    private String value(Cursor c,int index,String fallback){
        return c.isNull(index)||c.getString(index).trim().isEmpty()?
            fallback:c.getString(index);
    }
    private void render(){
        if(items==null)return;
        items.removeAllViews();
        guideTab.setText(selectedTab==0?"Habitat guide  ●":"Habitat guide");
        recordsTab.setText(selectedTab==1?"My site records  ●":"My site records");
        if(selectedTab==0){
            Button filter=UiKit.button(this,"Filter: "+habitatGroup+"  ▾","quiet");
            LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,dp(48));
            fp.topMargin=dp(5);
            items.addView(filter,fp);
            final String[] groups={"All habitats","Terrestrial","Edaphic",
                "Coastal","Aquatic","Human-modified"};
            filter.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("Filter habitat guides")
                .setItems(groups,(dialog,which)->{
                    habitatGroup=groups[which];render();
                }).setNegativeButton("Cancel",null).show());
            if("All habitats".equals(habitatGroup))
                OfflineFieldLibrary.addHabitats(this,items);
            else
                OfflineFieldLibrary.addHabitats(this,items,habitatGroup);
            items.addView(FieldVisuals.context(this,
                "These habitat descriptions are references. Actual field "+
                "measurements belong to your sampling stations."));
            return;
        }
        Button stations=UiKit.button(this,"Manage projects & sampling stations","secondary");
        stations.setOnClickListener(v->startActivity(new Intent(this,SitesActivity.class)));
        items.addView(stations,new LinearLayout.LayoutParams(-1,dp(52)));
        SQLiteDatabase db=helper.getReadableDatabase();
        items.addView(FieldVisuals.section(this,"Site habitat register",
            "Human-entered descriptions, not inferred vegetation types"));
        try(Cursor c=db.rawQuery("SELECT id,name,COALESCE(habitat,''),"+
                "COALESCE(project_id,'') FROM sites ORDER BY updated_at DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                items.addView(FieldVisuals.context(this,value(c,1,c.getString(0))+
                    " · "+value(c,2,"Habitat not recorded")+
                    "\nProject: "+value(c,3,"Unassigned")));
                seen++;
            }
            if(seen==0)items.addView(label("No sampling stations recorded yet.",15));
        }catch(Exception ignored){items.addView(label("Site register unavailable.",15));}
        items.addView(FieldVisuals.section(this,"Recorded environmental visits",
            "Up to 35 recent sampling units; missing is never zero"));
        try(Cursor c=db.rawQuery(
                "SELECT COALESCE(station_id,''),COALESCE(habitat_code,''),"+
                "COALESCE(habitat_source,''),manual_elevation_msl_m,"+
                "elevation_msl_m,COALESCE(elevation_source,''),"+
                "vertical_uncertainty_m,COALESCE(updated_utc,'') "+
                "FROM station_environment ORDER BY updated_utc DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                String elevation="Elevation not available";
                if(!c.isNull(4))
                    elevation=String.format(Locale.US,"%.1f m MSL",c.getDouble(4));
                else if(!c.isNull(3))
                    elevation=String.format(Locale.US,
                        "%.1f m MSL (manual)",c.getDouble(3));
                String uncertainty=c.isNull(6)?"Accuracy not recorded":
                    String.format(Locale.US,"Vertical uncertainty ±%.1f m",c.getDouble(6));
                items.addView(FieldVisuals.context(this,
                    "Station "+value(c,0,"Unassigned")+
                    "\nHabitat: "+value(c,1,"Not assessed")+
                    "\nReference: "+value(c,2,"Habitat source not recorded")+
                    "\n"+elevation+
                    "\n"+uncertainty+
                    "\nRecorded "+value(c,7,"time unavailable")));
                seen++;
            }
            if(seen==0)items.addView(label(
                "No environmental sampling visits recorded yet.",15));
        }catch(Exception ignored){items.addView(label(
            "Environmental visit register unavailable.",15));}
        TextView foot=label("A habitat label or elevation is not proof "+
            "that a species occurs at this station.",13);
        foot.setTextColor(UiKit.MUTED);
        items.addView(foot);
    }
    @Override protected void onDestroy(){
        if(helper!=null)helper.close();
        super.onDestroy();
    }
}
