package org.phwildlife.survey;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import java.util.List;

/** Review quality flags without changing field observations. */
public final class AuditActivity extends Activity{
    private FieldDb helper;
    private LinearLayout items;
    private int dp(int value){return Math.round(getResources().getDisplayMetrics().density*value);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);helper=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(14),dp(16),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.title(this,"Scientific data review"));
        TextView explanation=new TextView(this);
        explanation.setText("This read-only review checks database links, route templates, station effort, GPS, photographs and observation metadata. A warning is not automatic evidence of invalid data.");
        explanation.setTextSize(14);explanation.setTextColor(UiKit.INK);
        root.addView(explanation);
        Button refresh=UiKit.button(this,"Run quality checks","primary");root.addView(refresh);
        refresh.setOnClickListener(v->scan());
        Button back=UiKit.button(this,"‹ Back","secondary");root.addView(back);
        back.setOnClickListener(v->finish());
        ScrollView scroll=new ScrollView(this);
        items=new LinearLayout(this);items.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(items);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);scan();
    }
    private void scan(){
        items.removeAllViews();
        List<DataAudit.Issue> issues=DataAudit.run(this,helper.getReadableDatabase());
        TextView summary=new TextView(this);
        summary.setText(issues.isEmpty()?"No issues detected by the current checks":
            issues.size()+" issue categories need review");
        summary.setTextSize(19);summary.setTextColor(UiKit.INK);
        summary.setPadding(0,dp(12),0,dp(12));items.addView(summary);
        if(issues.isEmpty()){
            TextView note=new TextView(this);
            note.setText("Passing automated checks does not verify species identification, GPS field accuracy, equipment calibration, sampling design or adequate survey effort.");
            note.setTextSize(13);items.addView(note);
        }
        for(DataAudit.Issue issue:issues){
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12),dp(9),dp(12),dp(9));
            card.setBackground(UiKit.surface(this));
            TextView title=new TextView(this);
            title.setText(issue.level+" · "+issue.code.replace('_',' ')+" · "+issue.count);
            title.setTextSize(15);title.setTextColor(issue.level.equals("ERROR")?
                Color.rgb(174,43,43):UiKit.INK);
            card.addView(title);
            TextView description=new TextView(this);
            description.setText(issue.message+(issue.sample.isEmpty()?"":"\nExample: "+issue.sample));
            description.setTextSize(13);description.setTextColor(UiKit.MUTED);
            card.addView(description);
            LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);
            margin.topMargin=dp(9);items.addView(card,margin);
        }
        TextView explanation=new TextView(this);
        explanation.setText("Full inspection results are included in ZIP export as data_quality_flags.csv. "+
            "This screen never repairs or deletes source records automatically.");
        explanation.setTextColor(UiKit.INK);explanation.setTextSize(12);
        explanation.setPadding(0,dp(15),0,0);items.addView(explanation);
    }
    @Override protected void onDestroy(){super.onDestroy();helper.close();}
}
