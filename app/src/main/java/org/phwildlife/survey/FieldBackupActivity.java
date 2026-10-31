package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Offline recovery tool. Separate from the old v0.7 CSV importer. */
public final class FieldBackupActivity extends Activity {
    private static final int CREATE=360,OPEN=361;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private TextView status;
    private Button export,restore;
    private boolean busy;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(12),dp(16),dp(16));
        root.setBackgroundColor(UiKit.BG);
        root.addView(UiKit.navigationHeader(this,"Backup & recovery","Settings",this::finish));
        ScrollView scroller=new ScrollView(this);
        LinearLayout contents=new LinearLayout(this);
        contents.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(contents);
        root.addView(scroller,new LinearLayout.LayoutParams(-1,0,1f));
        TextView intro=UiKit.sectionTitle(this,"Full local fieldwork snapshot");
        contents.addView(intro);
        TextView explanation=new TextView(this);
        explanation.setText("Create a recovery ZIP containing a consistent SQLite database snapshot, "
            +"field photographs, audio, and local settings. This is not the research CSV export. "
            +"Keep the ZIP private: it contains field locations and may contain personal information. "
            +"Only use it from a trusted storage location.");
        explanation.setTextColor(UiKit.INK);explanation.setTextSize(15);
        explanation.setPadding(dp(4),dp(7),dp(4),dp(12));
        contents.addView(explanation);
        export=UiKit.button(this,"Create recoverable ZIP","primary");
        contents.addView(export,new LinearLayout.LayoutParams(-1,dp(56)));
        export.setOnClickListener(v->{
            if(busy)return;
            if(FieldBackupArchive.hasActiveSurvey(this)){
                status.setText("End or finish the active survey before making a snapshot.");
                return;
            }
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/zip");
            i.putExtra(Intent.EXTRA_TITLE,"LIKAS-field-backup-v27.zip");
            startActivityForResult(i,CREATE);
        });
        contents.addView(UiKit.sectionTitle(this,"Restore on a fresh workspace"));
        TextView note=new TextView(this);
        note.setText("Restore only into an empty LIKAS workspace with no surveys, stations, "
            +"custom projects, routes, study templates, or favorites. This operation never merges "
            +"data or overwrites existing fieldwork. Use a ZIP created by this version's backup tool. "
            +"The app must restart after restoration.");
        note.setTextColor(UiKit.MUTED);note.setTextSize(14);
        note.setPadding(dp(4),dp(4),dp(4),dp(10));contents.addView(note);
        restore=UiKit.button(this,"Check and restore LIKAS ZIP","secondary");
        contents.addView(restore,new LinearLayout.LayoutParams(-1,dp(56)));
        restore.setOnClickListener(v->{
            if(busy)return;
            if(!FieldBackupArchive.canRestore(this)){
                status.setText("Restore blocked: local records or saved workspace items exist. "
                    +"No data was changed.");
                return;
            }
            new AlertDialog.Builder(this)
                .setTitle("Restore a fieldwork archive?")
                .setMessage("The current workspace must be empty. The ZIP will be verified "
                    +"before any local replacement. A private copy of the original empty "
                    +"database is kept for recovery. Only proceed with a trusted LIKAS ZIP.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Choose ZIP",(dialog,which)->{
                    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("application/zip");
                    startActivityForResult(i,OPEN);
                }).show();
        });
        status=new TextView(this);
        status.setText("No backup or restore has been run on this device.");
        status.setTextSize(14);status.setTextColor(UiKit.INK);
        status.setPadding(dp(5),dp(18),dp(5),dp(8));contents.addView(status);
        setContentView(root);
    }
    private void work(int request,Uri uri){
        if(uri==null||busy)return;
        busy=true;export.setEnabled(false);restore.setEnabled(false);
        status.setText(request==CREATE?"Preparing verified snapshot…":"Verifying backup archive…");
        worker.execute(()->{
            String outcome;boolean recovered=false;
            try{
                if(request==CREATE){
                    try(OutputStream out=getContentResolver().openOutputStream(uri,"w")){
                        if(out==null)throw new java.io.IOException("Cannot write chosen destination");
                        outcome=FieldBackupArchive.export(getApplicationContext(),out);
                    }
                }else{
                    try(InputStream in=getContentResolver().openInputStream(uri)){
                        if(in==null)throw new java.io.IOException("Cannot read chosen backup");
                        outcome=FieldBackupArchive.restoreIntoEmpty(getApplicationContext(),in);
                        recovered=true;
                    }
                }
            }catch(Exception e){outcome="Operation stopped without successful verification: "+e.getMessage();}
            final String message=outcome;final boolean didRestore=recovered;
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed())return;
                status.setText(message);
                busy=false;export.setEnabled(true);restore.setEnabled(true);
                if(didRestore){
                    new AlertDialog.Builder(this)
                        .setTitle("Recovery installed")
                        .setMessage("Close and reopen LIKAS to reload restored records and settings.")
                        .setCancelable(false)
                        .setPositiveButton("Close LIKAS",(dialog,which)->{
                            finishAffinity();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }).show();
                }
            });
        });
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null)return;
        if(request==CREATE||request==OPEN)work(request,data.getData());
    }
    @Override protected void onDestroy(){
        worker.shutdown();
        super.onDestroy();
    }
}
