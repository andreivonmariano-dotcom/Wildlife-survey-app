package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * Standalone research export for saved field records. Reuses the recorder's
 * established archive writer rather than creating a second export format.
 * Export must not require an active survey or modify survey outcomes.
 */
public final class ResearchExportActivity extends Activity {
    private static final int CREATE_RESEARCH_ZIP=811;
    private Button exportButton;
    private TextView status;
    private boolean exporting=false;

    private int dp(int n){
        return Math.round(n*getResources().getDisplayMetrics().density);
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout body=new LinearLayout(this);
        LinearLayout root=UiKit.page(this,"Research export","Analysis & Exports",
            this::finish,body);
        body.addView(ApoHomeUi.section(this,"Research archive",
            "Export project records, survey effort, observations and linked " +
            "evidence in the established research ZIP format."));
        body.addView(UiKit.sectionTitle(this,"Records on this device"));
        body.addView(ApoHomeUi.note(this,recordSummary()));
        body.addView(UiKit.sectionTitle(this,"What will be included"));
        body.addView(ApoHomeUi.note(this,
            "Surveys of all outcomes, including incomplete and not-counted " +
            "records, are included for auditability. Some analysis tables " +
            "are restricted to completed surveys."));
        body.addView(ApoHomeUi.note(this,
            "The archive may contain precise coordinates, timestamps, observer " +
            "names and sensitive species locations. Review access before sharing."));
        body.addView(UiKit.sectionTitle(this,"Save research ZIP"));
        exportButton=UiKit.button(this,"Choose ZIP destination","primary");
        LinearLayout.LayoutParams action=new LinearLayout.LayoutParams(-1,-2);
        action.topMargin=dp(12);
        body.addView(exportButton,action);
        exportButton.setOnClickListener(v->chooseDestination());
        status=UiKit.caption(this,"No file has been exported in this session.");
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.setMinHeight(dp(48));
        LinearLayout.LayoutParams note=new LinearLayout.LayoutParams(-1,-2);
        note.topMargin=dp(18);
        body.addView(status,note);
        body.addView(ApoHomeUi.note(this,
            "Research ZIP exports are not restorable device backups. " +
            "Use Settings > Backup for workspace recovery."));
        setContentView(root);
        UiMotion.enter(this,body);
    }

    private String recordSummary(){
        try(FieldDb db=new FieldDb(this)){
            int surveys=count(db,"SELECT COUNT(*) FROM surveys");
            int observations=count(db,
                "SELECT COUNT(*) FROM field_records WHERE deleted_at IS NULL");
            int projects=count(db,"SELECT COUNT(*) FROM projects");
            int stations=count(db,"SELECT COUNT(*) FROM sites");
            return surveys+" surveys (all outcomes) · "+observations+
                " retained observations\n"+projects+" projects · "+
                stations+" sampling stations";
        }catch(Exception error){
            return "The local record summary is unavailable. Export is still accessible.";
        }
    }
    private int count(FieldDb db,String statement){
        try(android.database.Cursor c=db.getReadableDatabase().rawQuery(statement,null)){
            return c.moveToFirst()?c.getInt(0):0;
        }
    }

    private void chooseDestination(){
        if(exporting)return;
        try{
            Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_TITLE,"apo-research-export.zip");
            startActivityForResult(intent,CREATE_RESEARCH_ZIP);
        }catch(Exception e){
            status.setText("Unable to open the system file picker: "+e.getMessage());
        }
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=CREATE_RESEARCH_ZIP)return;
        if(result!=RESULT_OK || data==null){
            status.setText("Export cancelled. No research ZIP was created by APO.");
            return;
        }
        Uri destination=data.getData();
        if(destination==null){status.setText("No export destination selected.");return;}
        if(exporting)return;
        exporting=true;
        exportButton.setEnabled(false);
        status.setText("Preparing research ZIP…");
        new Thread(()->{
            String outcome;
            try(FieldDb db=new FieldDb(getApplicationContext());
                OutputStream output=getContentResolver().openOutputStream(destination)){
                if(output==null)throw new java.io.IOException("Destination cannot be opened");
                try(ZipOutputStream zip=new ZipOutputStream(output)){
                    MainActivity.writeResearchArchive(getApplicationContext(),db,zip);
                    zip.finish();
                }
                outcome="Research ZIP saved. Verify the contents before sharing.";
            }catch(Exception error){
                outcome="Export failed: "+error.getMessage()+
                    ". The selected destination may contain a partial ZIP.";
            }
            final String resultText=outcome;
            runOnUiThread(()->{
                exporting=false;
                if(isFinishing()||isDestroyed())return;
                exportButton.setEnabled(true);
                status.setText(resultText);
            });
        },"APO-Research-Export").start();
    }
}
