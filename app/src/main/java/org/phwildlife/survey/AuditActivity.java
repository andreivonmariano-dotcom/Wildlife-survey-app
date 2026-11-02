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
        root.addView(UiKit.navigationHeader(this,"Data quality review","Analysis & Exports",this::finish));
        TextView explanation=new TextView(this);
        explanation.setText("Read-only checks of recorded effort, location metadata and evidence files. The review does not alter records.");
        explanation.setTextSize(14);explanation.setTextColor(UiKit.MUTED);
        explanation.setPadding(0,dp(4),0,dp(12));root.addView(explanation);
        refreshButton=UiKit.button(this,"Run quality checks","primary");
        root.addView(refreshButton);
        refreshButton.setOnClickListener(v->scan());
        Button integrity=UiKit.button(this,"Check local database integrity","secondary");
        LinearLayout.LayoutParams integrityLp=new LinearLayout.LayoutParams(-1,-2);
        integrityLp.topMargin=dp(9);
        root.addView(integrity,integrityLp);
        integrity.setOnClickListener(v->{
            integrity.setEnabled(false);
            auditWorker.execute(()->{
                String result;
                try(FieldDb local=new FieldDb(getApplicationContext());
                    android.database.Cursor cursor=local.getReadableDatabase()
                        .rawQuery("PRAGMA quick_check",null)){
                    result=cursor.moveToFirst()?cursor.getString(0):"No response";
                }catch(Exception error){
                    result="Check could not finish: "+error.getClass().getSimpleName();
                }
                final String message="ok".equalsIgnoreCase(result)?
                    "SQLite quick_check returned OK. This does not verify field accuracy or taxon identifications.":
                    "Database integrity result: "+result+
                    ". Back up field data and review before further edits.";
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    integrity.setEnabled(true);
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Database integrity")
                        .setMessage(message)
                        .setPositiveButton("Close",null).show();
                });
            });
        });
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
        int errors=0,warnings=0;
        for(DataAudit.Issue finding:issues){
            if("ERROR".equals(finding.level))errors++;
            else warnings++;
        }
        summary.setText(issues.isEmpty()?"No issues detected by the current checks":
            errors+" error categories · "+warnings+" other flag categories");
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
        explanation.setText("Issue counts are categories, not necessarily unique observations. " +
            "Full inspection results appear in research ZIP data_quality_flags.csv. " +
            "A passing check never verifies sampling design or species identification.");
        explanation.setTextColor(UiKit.INK);explanation.setTextSize(12);
        explanation.setPadding(0,dp(15),0,0);items.addView(explanation);
    }
    @Override protected void onDestroy(){
        auditWorker.shutdownNow();
        super.onDestroy();
    }
}
