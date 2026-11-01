package org.phwildlife.survey;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Review quality flags without changing field observations. */
public final class AuditActivity extends Activity{
    private final ExecutorService auditWorker=Executors.newSingleThreadExecutor();
    private LinearLayout items;
    private Button refreshButton;
    private boolean auditRunning;
    private int dp(int value){return Math.round(getResources().getDisplayMetrics().density*value);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(14),dp(16),dp(12));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Scientific data review","Home",this::finish));
        TextView explanation=new TextView(this);
        explanation.setText("Read-only checks for records, GPS, survey effort and evidence files.");
        explanation.setTextSize(14);explanation.setTextColor(UiKit.MUTED);
        explanation.setPadding(0,dp(4),0,dp(12));root.addView(explanation);
        refreshButton=UiKit.button(this,"Run quality checks","primary");
        root.addView(refreshButton);
        refreshButton.setOnClickListener(v->scan());
        ScrollView scroll=new ScrollView(this);
        items=new LinearLayout(this);items.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(items);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);scan();
    }
    private void scan(){
        if(auditRunning)return;
        auditRunning=true;
        refreshButton.setEnabled(false);
        items.removeAllViews();
        TextView pending=new TextView(this);
        pending.setText("Checking saved research records…");
        pending.setTextColor(UiKit.MUTED);
        pending.setTextSize(14);
        items.addView(pending);
        auditWorker.execute(()->{
            List<DataAudit.Issue> findings;
            try(FieldDb local=new FieldDb(getApplicationContext())){
                findings=DataAudit.run(getApplicationContext(),local.getReadableDatabase());
            }catch(Exception error){
                findings=Collections.singletonList(new DataAudit.Issue(
                    "ERROR","AUDIT_UNAVAILABLE",1,"",error.getClass().getSimpleName()));
            }
            final List<DataAudit.Issue> result=findings;
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed())return;
                auditRunning=false;
                refreshButton.setEnabled(true);
                items.removeAllViews();
                display(result);
            });
        });
    }
    private void display(List<DataAudit.Issue> issues){
        TextView summary=new TextView(this);
        summary.setText(issues.isEmpty()?"No issues detected by the current checks":
            issues.size()+" issue categories need review");
        summary.setTextSize(19);summary.setTextColor(UiKit.FOREST);
        summary.setPadding(0,dp(12),0,dp(12));items.addView(summary);
        if(issues.isEmpty()){
            TextView note=new TextView(this);
            note.setText("Passing automated checks does not verify species identification, GPS field accuracy, equipment calibration, sampling design or adequate survey effort.");
            note.setTextSize(13);note.setTextColor(UiKit.MUTED);items.addView(note);
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
        explanation.setText("Full inspection results are included in the research ZIP as data_quality_flags.csv. "+
            "Warnings require review. This screen never changes source records.");
        explanation.setTextColor(UiKit.INK);explanation.setTextSize(12);
        explanation.setPadding(0,dp(15),0,0);items.addView(explanation);
    }
    @Override protected void onDestroy(){
        auditWorker.shutdownNow();
        super.onDestroy();
    }
}
