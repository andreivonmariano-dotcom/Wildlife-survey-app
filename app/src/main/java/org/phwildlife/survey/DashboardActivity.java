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
        LinearLayout root=vertical();
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Survey analysis","Analysis & Exports",
            this::finish));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        page=vertical();
        page.setPadding(0,0,0,dp(20));
        scroll.addView(page);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        page.addView(ApoHomeUi.section(this,"Survey record summary",
            "Descriptive counts from locally saved surveys and evidence."));
        String outcome=SurveyOutcomeSql.RESOLVED;
        page.addView(UiKit.sectionTitle(this,"Survey outcomes"));
        int complete=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='complete'",null);
        int incomplete=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='incomplete'",null);
        int excluded=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='not_counted'",null);
        int active=count("SELECT COUNT(*) FROM surveys s WHERE "+outcome+"='active'",null);
        int total=complete+incomplete+excluded;
        section("Archived surveys",String.valueOf(total),
            complete+" complete · "+incomplete+" incomplete · "+excluded+" not counted");
        if(active>0)section("Active surveys",String.valueOf(active),
            "In progress; excluded from completed-survey summaries");
        page.addView(UiKit.sectionTitle(this,"Sampling effort records"));
        int visits=count("SELECT COUNT(*) FROM point_counts p JOIN surveys s ON s.id=p.event_id "+
            "WHERE "+outcome+"='complete'",null);
        int otherVisits=count("SELECT COUNT(*) FROM sampling_units u JOIN surveys s ON s.id=u.event_id "+
            "WHERE "+outcome+"='complete'",null);
        section("Bird point-count visits",String.valueOf(visits),
            "Recorded visit rows from complete surveys");
        section("Other sampling-unit records",String.valueOf(otherVisits),
            "Protocol unit rows from complete surveys; counts shown separately");
        page.addView(UiKit.sectionTitle(this,"Detection records"));
        int records=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete'",null);
        int distinct=count("SELECT COUNT(DISTINCT lower(trim(r.raw_identification))) "+
            "FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete' "+
            "AND trim(COALESCE(r.raw_identification,''))<>''",null);
        section("Detections in complete surveys",String.valueOf(records),
            distinct+" distinct recorded identification labels, not verified taxa");
        int excludedRecords=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='not_counted'",null);
        section("Not-counted detections",String.valueOf(excludedRecords),
            "Retained in records; excluded from the complete-survey summary");
        int candidate=count("SELECT COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND r.taxon_group='Birds' "+
            "AND r.distance_eligible=1 AND "+outcome+"='complete'",null);
        section("Bird distance candidates",String.valueOf(candidate),
            "Eligibility screening only; not a density estimate");
        page.addView(UiKit.sectionTitle(this,"Detections by wildlife group"));
        try(Cursor c=helper.getReadableDatabase().rawQuery(
            "SELECT r.taxon_group,COUNT(*) FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.deleted_at IS NULL AND "+outcome+"='complete' "+
            "GROUP BY r.taxon_group ORDER BY r.taxon_group",null)){
            if(c.getCount()==0)page.addView(ApoHomeUi.note(this,
                "No detections from completed surveys. Zero-detection effort can still be valid."));
            while(c.moveToNext())section(c.isNull(0)?"Group not specified":c.getString(0),
                String.valueOf(c.getInt(1)),"Recorded detections");
        }
        page.addView(UiKit.sectionTitle(this,"Station records"));
        int stations=count("SELECT COUNT(*) FROM sites",null);
        section("Registered stations",String.valueOf(stations),
            "Station records across all projects; not unique visits");
        page.addView(UiKit.sectionTitle(this,"Recent field activity"));
        try(Cursor c=helper.getReadableDatabase().rawQuery(
            "SELECT COALESCE(s.survey_profile,s.taxon_group,'Birds'),"+
            "COALESCE(s.station_id,'Unassigned'),s.started,"+outcome+
            " FROM surveys s ORDER BY s.started DESC LIMIT 8",null)){
            if(c.getCount()==0)page.addView(ApoHomeUi.note(this,"No surveys recorded yet."));
            while(c.moveToNext()){
                String timestamp=c.isNull(2)?"Date not recorded":c.getString(2);
                page.addView(ApoHomeUi.note(this,c.getString(0)+" · "+c.getString(1)+
                    "\n"+timestamp+" · "+c.getString(3)));
            }
        }
        page.addView(ApoHomeUi.note(this,
            "These are descriptive database summaries. They do not establish population " +
            "abundance, occupancy, habitat selection, or sampling adequacy."));
        setContentView(root);
    }

}
