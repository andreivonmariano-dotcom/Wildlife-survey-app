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
    @Override public void onCreate(Bundle state){super.onCreate(state);helper=new FieldDb(this);}
    @Override protected void onResume(){super.onResume();render();}
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
    private int count(String sql,String[] args) {
        try(Cursor c=helper.getReadableDatabase().rawQuery(sql,args)){return c.moveToFirst()?c.getInt(0):0;}
    }
    private void section(String heading,String value,String detail){
        LinearLayout c=vertical();c.setPadding(dp(14),dp(10),dp(14),dp(10));c.setBackground(UiKit.surface(this));
        TextView h=label(heading,13);h.setTextColor(UiKit.MUTED);
        TextView num=label(value,28);num.setTextColor(UiKit.FOREST);num.setTypeface(null,android.graphics.Typeface.BOLD);
        c.addView(h);c.addView(num);
        if(detail!=null)c.addView(label(detail,12));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);page.addView(c,lp);
    }
    private void render(){
        LinearLayout root=vertical();root.setPadding(dp(14),dp(14),dp(14),dp(12));root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Analysis","Fieldwork",
            this::finish));
        TextView note=label("Survey records",13);
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
                    "Database integrity: OK.":
                    "Database check: "+result+". Back up records before editing.")
                .setPositiveButton("Close",null).show();
        });
        LinearLayout.LayoutParams checkLp=new LinearLayout.LayoutParams(-1,dp(54));
        checkLp.bottomMargin=dp(14);page.addView(check,checkLp);
        String outcome=SurveyOutcomeSql.RESOLVED;
        int complete=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='complete'",null);
        int incomplete=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='incomplete'",null);
        int excluded=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='not_counted'",null);
        int active=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='active'",null);
        int total=complete+incomplete+excluded;
        section("Archived surveys",String.valueOf(total),
            complete+" complete · "+incomplete+" incomplete · "+excluded+" not counted");
        if(active>0)section("Active surveys",String.valueOf(active),null);
        int visits=count("SELECT COUNT(*) FROM point_counts p JOIN surveys s ON s.id=p.event_id "+
            "WHERE "+outcome+"='complete'",null);
        int otherVisits=count("SELECT COUNT(*) FROM sampling_units u JOIN surveys s ON s.id=u.event_id "+
            "WHERE "+outcome+"='complete'",null);
        section("Completed sampling units",String.valueOf(visits+otherVisits),
            visits+" bird point visits · "+otherVisits+" other protocol visits");
        int records=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete'",null);
        int distinct=count("SELECT COUNT(DISTINCT lower(trim(r.raw_identification))) "+
            "FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete' AND trim(COALESCE(r.raw_identification,''))<>''",null);
        section("Detections in complete surveys",String.valueOf(records),
            distinct+" distinct identification labels");
        int excludedRecords=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='not_counted'",null);
        section("Not-counted detections",String.valueOf(excludedRecords),null);
        int stations=count("SELECT COUNT(*) FROM sites",null);
        section("Registered stations",String.valueOf(stations),"Across all projects");
        int candidate=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' "+
            "AND r.distance_eligible=1 AND "+outcome+"='complete'",null);
        section("Bird distance candidates",String.valueOf(candidate),"Screening count");
        page.addView(UiKit.sectionTitle(this,"Detections by wildlife group"));
        try(Cursor c=helper.getReadableDatabase().rawQuery(
            "SELECT r.taxon_group,COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete' "+
            "GROUP BY r.taxon_group ORDER BY r.taxon_group",null)){
            while(c.moveToNext())section(c.getString(0),String.valueOf(c.getInt(1)),null);
        }
        page.addView(UiKit.sectionTitle(this,"Recent field activity"));
        try(Cursor c=helper.getReadableDatabase().rawQuery(
            "SELECT COALESCE(s.survey_profile,s.taxon_group,'Birds'),"+
            "COALESCE(s.station_id,'Unassigned'),s.started,"+outcome+
            " FROM surveys s ORDER BY s.started DESC LIMIT 8",null)){
            while(c.moveToNext())page.addView(label(c.getString(0)+" · "+c.getString(1)+"\n"+c.getString(2)+" · "+c.getString(3),14));
        }
        setContentView(root);
    }
}
