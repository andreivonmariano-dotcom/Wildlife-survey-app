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
        body.addView(ApoHomeUi.section(this,"Export survey data",
            "Create a ZIP with research CSV files, recorded sampling effort, " +
            "and linked field evidence. This is different from a restorable " +
            "device backup. Review sensitive locations before sharing."));
        exportButton=UiKit.button(this,"Create research ZIP","primary");
        LinearLayout.LayoutParams action=new LinearLayout.LayoutParams(-1,-2);
        action.topMargin=dp(12);
        body.addView(exportButton,action);
        exportButton.setOnClickListener(v->chooseDestination());
        status=UiKit.caption(this,"Choose a destination to save a research ZIP.");
        LinearLayout.LayoutParams note=new LinearLayout.LayoutParams(-1,-2);
        note.topMargin=dp(18);
        body.addView(status,note);
        setContentView(root);
        UiMotion.enter(this,body);
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
        if(request!=CREATE_RESEARCH_ZIP || result!=RESULT_OK || data==null)return;
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
