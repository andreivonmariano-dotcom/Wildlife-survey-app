package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

/** Honest, offline view of existing habitat and environmental visit records. */
public final class HabitatRecordsActivity extends Activity {
    private FieldDb helper;
    private LinearLayout items;
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView label(String text,int size){
        TextView value=new TextView(this);value.setText(text);
        value.setTextColor(UiKit.INK);value.setTextSize(size);
        value.setPadding(dp(3),dp(5),dp(3),dp(5));
        return value;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);helper=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,"Habitat & Ecology"));
        TextView note=label("Offline site and visit records, not a mapped habitat "+
            "classification or a species-distribution prediction.",13);
        note.setTextColor(UiKit.MUTED);root.addView(note);
        Button stations=UiKit.button(this,"Projects & station habitats","secondary");
        stations.setOnClickListener(v->startActivity(new Intent(this,SitesActivity.class)));
        root.addView(stations,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView scroll=new ScrollView(this);
        items=new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(items);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button back=UiKit.button(this,"Back to workspace","quiet");
        back.setOnClickListener(v->finish());
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
        setContentView(root);
    }
    @Override protected void onResume(){super.onResume();render();}
    private String value(Cursor c,int col,String fallback){
        return c.isNull(col)||c.getString(col).trim().isEmpty()?fallback:c.getString(col);
    }
    private void render(){
        if(items==null)return;
        items.removeAllViews();
        SQLiteDatabase db=helper.getReadableDatabase();
        items.addView(FieldVisuals.section(this,"Site habitat register",
            "Human-entered descriptions, not inferred vegetation types"));
        try(Cursor c=db.rawQuery("SELECT id,name,COALESCE(habitat,''),"+
                "COALESCE(project_id,'') FROM sites ORDER BY updated_at DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                String habitat=value(c,2,"Habitat not recorded");
                items.addView(FieldVisuals.context(this,value(c,1,c.getString(0))+
                    " · "+habitat+"\nProject: "+value(c,3,"Unassigned")));
                seen++;
            }
            if(seen==0)items.addView(label("No registered stations yet.",14));
        }catch(Exception err){items.addView(label("Site records unavailable.",14));}
        items.addView(FieldVisuals.section(this,"Recorded environmental visits",
            "Up to 35 recent sampling units; missing is not zero"));
        try(Cursor c=db.rawQuery(
                "SELECT COALESCE(station_id,''),COALESCE(habitat_code,''),"+
                "COALESCE(habitat_source,''),manual_elevation_msl_m,"+
                "elevation_msl_m,COALESCE(elevation_source,''),"+
                "vertical_uncertainty_m,COALESCE(updated_utc,'') "+
                "FROM station_environment ORDER BY updated_utc DESC LIMIT 35",null)){
            int seen=0;
            while(c.moveToNext()){
                String habitat=value(c,1,"Not assessed");
                String source=value(c,2,"Habitat source not recorded");
                String elevation="Elevation not available";
                if(!c.isNull(4)){
                    elevation=String.format(Locale.US,"%.1f m MSL",c.getDouble(4));
                }else if(!c.isNull(3)){
                    elevation=String.format(Locale.US,
                        "%.1f m MSL (manual)",c.getDouble(3));
                }
                String uncertainty=c.isNull(6)?"Accuracy not recorded":
                    String.format(Locale.US,"Vertical uncertainty ±%.1f m",c.getDouble(6));
                items.addView(FieldVisuals.context(this,
                    "Station "+value(c,0,"Unassigned")+"\nHabitat: "+habitat+
                    "\nReference: "+source+"\n"+elevation+
                    "\n"+uncertainty+"\nRecorded "+value(c,7,"time unavailable")));
                seen++;
            }
            if(seen==0)items.addView(label(
                "No per-visit environmental measurements are saved yet.",14));
        }catch(Exception err){items.addView(label(
            "Environmental visit records unavailable.",14));}
        TextView disclaimer=label("A habitat label or elevation is not evidence "+
            "that a species occurs at this station. More environmental "+
            "measurements and validated offline map layers are planned.",12);
        disclaimer.setTextColor(UiKit.MUTED);items.addView(disclaimer);
    }
    @Override protected void onDestroy(){helper.close();super.onDestroy();}
}