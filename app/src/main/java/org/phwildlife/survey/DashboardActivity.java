package org.phwildlife.survey;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.Locale;

public class DashboardActivity extends Activity {
    private FieldDb helper;
    private LinearLayout page;
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+0.5);}
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private TextView label(String s,int font){TextView t=new TextView(this);t.setText(s);t.setTextColor(UiKit.INK);t.setTextSize(font);t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;}
    @Override public void onCreate(Bundle state){super.onCreate(state);helper=new FieldDb(this);render();}
    @Override protected void onResume(){super.onResume();if(page!=null)render();}
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
    private int count(String sql,String[] args) {
        try(Cursor c=helper.getReadableDatabase().rawQuery(sql,args)){return c.moveToFirst()?c.getInt(0):0;}
    }
    private void section(String heading,String value,String detail){
        LinearLayout c=vertical();c.setPadding(dp(9),dp(7),dp(9),dp(7));c.setBackground(UiKit.surface(this));
        TextView h=label(heading,13);h.setTextColor(UiKit.MUTED);
        TextView num=label(value,27);num.setTypeface(null,android.graphics.Typeface.BOLD);
        c.addView(h);c.addView(num);
        if(detail!=null)c.addView(label(detail,12));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);page.addView(c,lp);
    }
    private void render(){
        LinearLayout root=vertical();root.setPadding(dp(14),dp(14),dp(14),dp(12));root.setBackgroundColor(UiKit.BG);
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);
        Button back=UiKit.button(this,"← Home","secondary");bar.addView(back);back.setOnClickListener(v->finish());
        root.addView(bar);root.addView(UiKit.title(this,"Fieldwork dashboard"));
        TextView note=label("Offline summaries of saved activity. These are descriptive counts, not ecological density estimates.",13);
        note.setTextColor(UiKit.MUTED);root.addView(note);
        ScrollView scroll=new ScrollView(this);page=vertical();scroll.addView(page);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button check=UiKit.button(this,"Check database integrity","secondary");
        check.setOnClickListener(v->{
            String result="Unknown";
            try(Cursor c=helper.getReadableDatabase().rawQuery("PRAGMA quick_check",null)){
                if(c.moveToFirst())result=c.getString(0);
            }catch(Exception e){result="Cannot complete check: "+e.getMessage();}
            new android.app.AlertDialog.Builder(this).setTitle("Local database integrity")
                .setMessage(result.equalsIgnoreCase("ok")?
                    "SQLite reports OK. This checks internal database structure, not the scientific accuracy of observations.":
                    "SQLite check: "+result+". Export your records as a backup before further edits.")
                .setPositiveButton("Close",null).show();
        });
        LinearLayout.LayoutParams checkLp=new LinearLayout.LayoutParams(-1,dp(54));
        checkLp.bottomMargin=dp(14);page.addView(check,checkLp);
        int total=count("SELECT COUNT(*) FROM surveys",null);
        int complete=count("SELECT COUNT(*) FROM surveys WHERE complete=1",null);
        int active=count("SELECT COUNT(*) FROM surveys WHERE ended IS NULL",null);
        int records=count("SELECT COUNT(*) FROM field_records WHERE deleted_at IS NULL",null);
        int locations=count("SELECT COUNT(*) FROM sites",null);
        int distinct=count("SELECT COUNT(DISTINCT lower(trim(raw_identification))) FROM field_records WHERE deleted_at IS NULL AND trim(COALESCE(raw_identification,''))<>''",null);
        section("SURVEYS",String.valueOf(total),complete+" completed · "+active+" active");
        int pointVisits=count("SELECT COUNT(*) FROM point_counts",null);
        int completePoints=count("SELECT COUNT(*) FROM point_counts WHERE completed=1",null);
        section("POINT COUNT VISITS",String.valueOf(pointVisits),completePoints+" completed · each station has its own count ID");
        int otherVisits=count("SELECT COUNT(*) FROM sampling_units",null);
        int otherFinished=count("SELECT COUNT(*) FROM sampling_units WHERE completed=1",null);
        section("HERP / MAMMAL / OTHER VISITS",String.valueOf(otherVisits),
            otherFinished+" completed · includes repeated sampling stations");
        try(Cursor paths=helper.getReadableDatabase().rawQuery(
          "SELECT COALESCE(SUM(gps_track_m),0),COUNT(*) FROM surveys WHERE method IN ('line_transect','ves_transect')",null)){
          if(paths.moveToFirst())section("GPS WALKED PATH",String.format(Locale.US,"%.0f m",paths.getDouble(0)),
            "Across "+paths.getInt(1)+" route surveys · GPS estimate, not certified transect effort");
        }
        section("DETECTIONS",String.valueOf(records),distinct+" distinct identification labels (not verified species)");
        section("REGISTERED STATIONS",String.valueOf(locations),"Repeated visits remain separate events");
        int candidate=count("SELECT COUNT(*) FROM field_records WHERE deleted_at IS NULL AND taxon_group='Birds' AND distance_eligible=1",null);
        section("BIRD DISTANCE CANDIDATES",String.valueOf(candidate),"Screening flag only · not model-validated");
        page.addView(label("OBSERVATIONS BY TAXON",17));
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT taxon_group,COUNT(*) FROM field_records WHERE deleted_at IS NULL GROUP BY taxon_group ORDER BY taxon_group",null)){
            if(c.getCount()==0)page.addView(label("No recorded observations yet.",13));
            while(c.moveToNext())section(c.getString(0),String.valueOf(c.getInt(1)),null);
        }
        page.addView(label("RECENT FIELD ACTIVITY",17));
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT COALESCE(survey_profile,taxon_group,'Birds'),COALESCE(station_id,'Unassigned'),started,CASE WHEN ended IS NULL THEN 'Active' WHEN complete=1 THEN 'Completed' ELSE 'Incomplete' END FROM surveys ORDER BY started DESC LIMIT 8",null)){
            while(c.moveToNext())page.addView(label(c.getString(0)+" · "+c.getString(1)+"\n"+c.getString(2)+" · "+c.getString(3),14));
        }
        page.addView(label("Scientific model eligibility must be checked using detection assumptions, valid effort, sampling frames and species-level datasets.",12));
        setContentView(root);
    }
}
