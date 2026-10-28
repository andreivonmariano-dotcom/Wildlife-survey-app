package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Private, opt-in local data deletion. Does not modify the scientific catalog. */
public final class DataManagementActivity extends Activity {
    private FieldDb helper;
    private LinearLayout sections;
    private final ArrayList<String> surveyIds=new ArrayList<>();
    private Spinner surveyChoice;
    private final ExecutorService deletionWorker=Executors.newSingleThreadExecutor();
    private boolean deleting;
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        helper=new FieldDb(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(16),dp(12),dp(16),dp(12));
        root.addView(UiKit.navigationHeader(this,"Manage saved data","Workspace",this::finish));
        TextView notice=UiKit.sectionTitle(this,
            "Deletion is permanent. The research ZIP is not a complete restore backup.");
        notice.setTextSize(13);notice.setTextColor(UiKit.MUTED);root.addView(notice);
        ScrollView scroll=new ScrollView(this);
        sections=new LinearLayout(this);
        sections.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(sections);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        setContentView(root);
        show();
    }
    @Override protected void onResume(){super.onResume();if(sections!=null)show();}
    private void label(String name,String subtitle){
        sections.addView(FieldVisuals.section(this,name,subtitle));
    }
    private Button button(String name,Runnable action){
        Button b=UiKit.button(this,name,"secondary");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));
        lp.bottomMargin=dp(7);
        sections.addView(b,lp);
        b.setOnClickListener(v->action.run());
        return b;
    }
    private void show(){
        sections.removeAllViews();
        SQLiteDatabase db=helper.getReadableDatabase();
        int surveys=FieldDataDeletion.count(db,"surveys");
        int active=FieldDataDeletion.countActive(db);
        int records=FieldDataDeletion.count(db,"field_records");
        int sites=FieldDataDeletion.count(db,"sites");
        int projects=FieldDataDeletion.count(db,"projects");
        int routes=FieldDataDeletion.count(db,"route_templates");
        int studies=FieldDataDeletion.count(db,"study_protocol_templates");
        int favorites=FieldDataDeletion.count(db,"taxa_favorites");
        label("Saved surveys and evidence",
            surveys+" surveys · "+records+" observations · "+active+" active");
        button("Review survey history",()->
            startActivity(new Intent(this,HistoryActivity.class)));
        surveyIds.clear();
        ArrayList<String> labels=new ArrayList<>();
        try(Cursor c=db.rawQuery(
                "SELECT id,COALESCE(survey_profile,taxon_group,'Wildlife'),"+
                    "COALESCE(method,''),COALESCE(started,'') "+
                    "FROM surveys WHERE ended IS NOT NULL AND "+
                    "COALESCE(outcome,'')<>'active' "+
                    "ORDER BY started DESC LIMIT 150",null)){
            while(c.moveToNext()){
                surveyIds.add(c.getString(0));
                labels.add(c.getString(1)+" · "+c.getString(2)+" · "+c.getString(3));
            }
        }
        if(!surveyIds.isEmpty()){
            surveyChoice=new Spinner(this);
            surveyChoice.setContentDescription("Choose an archived survey to delete");
            surveyChoice.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,labels));
            sections.addView(surveyChoice,new LinearLayout.LayoutParams(-1,dp(52)));
            button("Delete selected archived survey",()->{
                int index=surveyChoice.getSelectedItemPosition();
                if(index>=0&&index<surveyIds.size()){
                    String id=surveyIds.get(index);
                    confirm("selected survey","DELETE",()->{
                        try(FieldDb local=new FieldDb(getApplicationContext())){
                            return resultText(FieldDataDeletion.deleteSurvey(
                                getApplicationContext(),local.getWritableDatabase(),id));
                        }
                    });
                }
            });
            if(surveys>150)sections.addView(FieldVisuals.context(this,
                "Showing the newest 150 archived surveys. Other surveys remain saved."));
        }
        Button deleteAllSurveys=button("Delete all archived surveys and their evidence",()->
            confirm("ALL surveys, observations, photos, audio and sampling effort",
                "DELETE SURVEYS",()->{
                    try(FieldDb local=new FieldDb(getApplicationContext())){
                        return resultText(FieldDataDeletion.deleteAllSurveys(
                            getApplicationContext(),local.getWritableDatabase()));
                    }
                }));
        deleteAllSurveys.setEnabled(active==0);
        label("Projects, sites and reusable plans",
            projects+" projects · "+sites+" stations · "+routes+" routes · "+
                studies+" study templates");
        button("Delete one station, project, route or study template",()->
            chooseWorkspaceRecord());
        Button deleteWorkspace=button("Delete project and station register",()->
            confirm("ALL projects, stations, routes and study templates",
                "DELETE WORKSPACE",()->{
                    try(FieldDb local=new FieldDb(getApplicationContext())){
                        FieldDataDeletion.clearWorkspace(local.getWritableDatabase());
                        return "Project and station register cleared.";
                    }
                }));
        deleteWorkspace.setEnabled(surveys==0);
        label("Species preferences",favorites+" favorite species");
        button("Clear favorite species",()->
            confirm("favorite species list","CLEAR FAVORITES",()->{
                try(FieldDb local=new FieldDb(getApplicationContext())){
                    FieldDataDeletion.clearFavorites(local.getWritableDatabase());
                    return "Species favorites cleared.";
                }
            }));
        label("Reset all my field data","Deletes surveys, media, projects, stations, "+
            "routes, study templates and favorites. Source reference catalogs remain installed.");
        Button resetAll=button("Delete all my saved field data",()->
            confirm("ALL user-created research records and attached media",
                "DELETE ALL",()->{
                    try(FieldDb local=new FieldDb(getApplicationContext())){
                        return resultText(FieldDataDeletion.clearAll(
                            getApplicationContext(),local.getWritableDatabase()));
                    }
                }));
        resetAll.setEnabled(active==0);
        if(active>0)sections.addView(FieldVisuals.context(this,
            "Active or paused surveys are protected. Use End Survey before any reset."));
        sections.addView(FieldVisuals.context(this,
            "Installed Philippine species checklists and bundled diagnostic references "+
            "are protected reference data. Deleting favorites does not remove them."));
    }
    private void chooseWorkspaceRecord(){
        final String[] categories={"Stations","Projects","Route plans","Study templates"};
        new AlertDialog.Builder(this).setTitle("Choose type of saved item")
            .setItems(categories,(dialog,index)->chooseWorkspaceItem(index))
            .setNegativeButton("Cancel",null).show();
    }
    private void chooseWorkspaceItem(int index){
        final String[] tables={"sites","projects","route_templates",
            "study_protocol_templates"};
        if(index<0||index>=tables.length)return;
        String table=tables[index];
        ArrayList<String> ids=new ArrayList<>(),names=new ArrayList<>();
        try(Cursor c=helper.getReadableDatabase().rawQuery(
                "SELECT id,name FROM "+table+" ORDER BY name COLLATE NOCASE LIMIT 150",
                null)){
            while(c.moveToNext()){
                String id=c.getString(0);
                if("projects".equals(table)&&"default".equals(id))continue;
                ids.add(id);names.add(c.getString(1)+" · "+id);
            }
        }catch(Exception error){
            showMessage("Cannot read saved items: "+error.getMessage());return;
        }
        if(ids.isEmpty()){
            showMessage("No deletable saved items in this category.");return;
        }
        new AlertDialog.Builder(this).setTitle("Choose item to delete")
            .setItems(names.toArray(new String[0]),(dialog,selected)->{
                String id=ids.get(selected);
                confirm("selected "+table+" item","DELETE",()->{
                    try(FieldDb local=new FieldDb(getApplicationContext())){
                        SQLiteDatabase db=local.getWritableDatabase();
                        switch(index){
                            case 0:FieldDataDeletion.deleteSite(db,id);break;
                            case 1:FieldDataDeletion.deleteProject(db,id);break;
                            case 2:FieldDataDeletion.deleteRoute(db,id);break;
                            case 3:FieldDataDeletion.deleteStudyTemplate(db,id);break;
                            default:return "No saved item selected.";
                        }
                        return "Selected saved item deleted.";
                    }
                });
            })
            .setNegativeButton("Cancel",null).show();
    }
    private void confirm(String scope,String phrase,Callable<String> action){
        if(deleting)return;
        EditText input=new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(18);
        input.setHint(phrase);
        input.setInputType(InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        LinearLayout wrapper=new LinearLayout(this);
        wrapper.setPadding(dp(20),dp(4),dp(20),0);
        wrapper.addView(input,new LinearLayout.LayoutParams(-1,dp(56)));
        new AlertDialog.Builder(this).setTitle("Permanently delete "+scope+"?")
            .setMessage("This cannot be undone by the app. Type "+phrase+
                " exactly to confirm. Export important research data first.")
            .setView(wrapper)
            .setNegativeButton("Keep data",null)
            .setPositiveButton("Delete",(dialog,which)->{
                if(!phrase.equals(input.getText().toString().trim())){
                    showMessage("Nothing deleted. Confirmation phrase did not match.");
                    return;
                }
                if(deleting)return;
                deleting=true;
                AlertDialog progress=new AlertDialog.Builder(this)
                    .setTitle("Deleting selected records")
                    .setMessage("Applying the confirmed change to local storage.")
                    .setCancelable(false).create();
                progress.show();
                deletionWorker.execute(()->{
                    String report;
                    try{report=action.call();}
                    catch(Exception error){report="Deletion stopped: "+error.getMessage();}
                    final String message=report;
                    runOnUiThread(()->{
                        deleting=false;
                        progress.dismiss();
                        if(isFinishing()||isDestroyed())return;
                        showMessage(message);
                        show();
                    });
                });
            }).show();
    }
    private String resultText(FieldDataDeletion.Report report){
        return report.deletedSurveys+" surveys deleted. "+
            report.deletedFiles+" linked media files removed."+
            (report.failedFiles>0?" "+report.failedFiles+
                " media files could not be removed.":"");
    }
    private void showMessage(String message){
        new AlertDialog.Builder(this).setTitle("Saved data")
            .setMessage(message).setPositiveButton("Close",null).show();
    }
    @Override protected void onDestroy(){
        deletionWorker.shutdown();
        helper.close();super.onDestroy();
    }
}