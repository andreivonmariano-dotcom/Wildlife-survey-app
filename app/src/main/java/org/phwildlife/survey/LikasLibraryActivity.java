package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ClipData;
import android.os.Bundle;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.net.Uri;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A short, image-led entry to the complete offline 715-account Book 01 atlas. */
public final class LikasLibraryActivity extends Activity {
    private int dp(float n){return PhLibrary2Ui.dp(this,n);}

    /** Return from Living Atlas to the one canonical workspace without duplicating its state. */
    private void workspace(String target){
        Intent intent=new Intent(this,WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("workspace_destination",target);
        startActivity(intent);
        finish();
    }
    private void openAtlas(String extra,String filter){
        Intent i=new Intent(this,PhLibraryDirectoryActivity.class);
        if(extra!=null)i.putExtra(extra,filter);
        startActivity(i);
    }
    private void tile(LinearLayout body,String label,Runnable open){
        TextView v=PhLibrary2Ui.action(this,label,open,false);
        v.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        v.setTextSize(18);
        v.setPadding(dp(18),dp(10),dp(18),dp(10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));
        p.bottomMargin=dp(8);body.addView(v,p);
    }

    private static final int PHOTO_BULK_PICKER=8414;
    private static final int MAX_BULK_SELECTION=400;
    private static final class PhotoMatch{
        final Uri uri;final String species;
        PhotoMatch(Uri uri,String species){this.uri=uri;this.species=species;}
    }
    private void selectSpeciesPhotos(){
        new AlertDialog.Builder(this).setTitle("Import species photographs")
            .setMessage("Choose photos from this device or a connected Drive account. "+
                "Only filenames matching a full scientific name in this offline book "+
                "(for example, Staurois natator.jpg) will be offered for import. "+
                "Check the identification and permission for every photograph. "+
                "The originals remain unchanged and no photos are uploaded to APO.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Choose photos",(dialog,which)->{
                Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.setType("image/*");
                picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try{startActivityForResult(picker,PHOTO_BULK_PICKER);}
                catch(Exception e){new AlertDialog.Builder(this)
                    .setMessage("No photo document picker is available.")
                    .setPositiveButton("Close",null).show();}
            }).show();
    }
    private String displayName(Uri selected){
        try(Cursor cursor=getContentResolver().query(selected,
            new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(cursor!=null&&cursor.moveToFirst()){
                String name=cursor.getString(0);
                return name==null?"":name;
            }
        }catch(Exception ignored){}
        return "";
    }
    private void reviewBulkSelection(Intent chosen){
        ArrayList<Uri> chosenFiles=new ArrayList<>();
        ClipData clips=chosen.getClipData();
        if(clips!=null){
            for(int i=0;i<clips.getItemCount()&&i<=MAX_BULK_SELECTION;i++)
                chosenFiles.add(clips.getItemAt(i).getUri());
        }else if(chosen.getData()!=null)chosenFiles.add(chosen.getData());
        if(chosenFiles.isEmpty())return;
        if(chosenFiles.size()>MAX_BULK_SELECTION){
            new AlertDialog.Builder(this).setMessage("Select up to "+
                MAX_BULK_SELECTION+" photographs at a time.")
                .setPositiveButton("Close",null).show();return;
        }
        LinkedHashMap<String,PhotoMatch> approved=new LinkedHashMap<>();
        int unknown=0,alternates=0,alreadyImported=0;
        for(Uri uri:chosenFiles){
            String scientific=PhLibraryBulkPhotoImport.exactTaxon(this,displayName(uri));
            if(scientific==null){unknown++;continue;}
            if(PhLibrarySpeciesMedia.hasPersonalPhoto(this,scientific)){
                alreadyImported++;continue;
            }
            if(approved.putIfAbsent(scientific,new PhotoMatch(uri,scientific))!=null)
                alternates++;
        }
        if(approved.isEmpty()){
            new AlertDialog.Builder(this).setTitle("No photographs matched")
                .setMessage("Unmatched or already-imported files: "+chosenFiles.size()+
                    ". To use camera-numbered photos, open the corresponding species "+
                    "account and choose Add photo.")
                .setPositiveButton("Close",null).show();return;
        }
        StringBuilder species=new StringBuilder();
        int shown=0;
        for(String latin:approved.keySet()){
            if(shown++==15){species.append("\\n…");break;}
            species.append("\\n").append(latin);
        }
        int ignored=unknown,duplicates=alternates,prior=alreadyImported;
        new AlertDialog.Builder(this).setTitle("Review "+approved.size()+" species matches")
            .setMessage("The match uses only the photo filename, not image identification. "+
                "Confirm that the selected photographs actually show these species and "+
                "that you can use them in this private app.\\n"+species+
                "\\n\\nUnmatched: "+ignored+" · Repeat species: "+duplicates+
                " · Already attached: "+prior)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Import privately",(dialog,which)->{
                ArrayList<PhotoMatch> imports=new ArrayList<>(approved.values());
                new Thread(()->{
                    int done=0,failed=0;
                    for(PhotoMatch match:imports){
                        try{PhLibrarySpeciesMedia.importPrivate(this,match.species,match.uri);
                            done++;
                        }catch(Exception e){failed++;}
                    }
                    final int success=done,errors=failed;
                    runOnUiThread(()->{
                        if(isFinishing()||isDestroyed())return;
                        new AlertDialog.Builder(this).setTitle("Photo import finished")
                            .setMessage(success+" species photos added to this device. "+
                                errors+" files could not be imported. "+
                                "Original files in Drive/device were not changed.")
                            .setPositiveButton("Close",null).show();
                    });
                },"phlibrary-private-bulk-media").start();
            }).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==PHOTO_BULK_PICKER&&result==RESULT_OK&&data!=null)
            reviewBulkSelection(data);
    }
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(PhLibrary2Ui.PAPER);
        getWindow().setNavigationBarColor(PhLibrary2Ui.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PhLibrary2Ui.PAPER);
        root.setPadding(dp(16),dp(8),dp(16),0);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        boolean largeText=getResources().getConfiguration().fontScale>=1.6f;
        TextView back=PhLibrary2Ui.action(this,"Back",this::finish,false);
        if(largeText){
            back.setTextSize(12);back.setPadding(dp(7),dp(5),dp(7),dp(5));
        }
        nav.addView(back,new LinearLayout.LayoutParams(dp(
            getResources().getConfiguration().fontScale>=1.6f?100:74),dp(48)));
        TextView title=PhLibrary2Ui.text(this,"Living Atlas",15,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);
        if(getResources().getConfiguration().fontScale>=1.6f)title.setTextSize(12);
        nav.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(9),0,dp(26));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(ApoShellBar.create(this,"atlas",
            ()->workspace("home"),()->workspace("fieldwork"),
            ()->scroll.smoothScrollTo(0,0),()->workspace("data")));
        setContentView(root);

        body.addView(PhLibraryPhotoHero.create(this,"Wildlife of\nGreater Mindanao","715 species accounts"));
        View searchGap=new View(this);
        body.addView(searchGap,new LinearLayout.LayoutParams(-1,dp(17)));
        LinearLayout bar=new LinearLayout(this);
        bar.setOrientation(largeText?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(16);
        search.setHint("Search species");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setContentDescription("Search the 715-account species atlas");
        UiKit.fieldInput(this,search);
        bar.addView(search,largeText?
            new LinearLayout.LayoutParams(-1,dp(58)):
            new LinearLayout.LayoutParams(0,dp(54),1));
        TextView go=PhLibrary2Ui.action(this,"Search",()->
            openAtlas("phlibrary_query",search.getText().toString().trim()),true);
        if(largeText){go.setTextSize(12);go.setPadding(dp(9),dp(5),dp(9),dp(5));}
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(
            largeText?-1:-2,dp(largeText?56:54));
        if(largeText)gp.topMargin=dp(7);
        else gp.leftMargin=dp(8);
        bar.addView(go,gp);body.addView(bar);
        search.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_SEARCH){
                openAtlas("phlibrary_query",search.getText().toString().trim());return true;
            }
            return false;
        });

        View tileGap=new View(this);
        body.addView(tileGap,new LinearLayout.LayoutParams(-1,dp(16)));
        body.addView(PhLibrary2Ui.panel(this,"Explore connections",
            "Species, taxonomy, habitats and references",()->
                startActivity(new Intent(this,LivingAtlasExploreActivity.class))),
            new LinearLayout.LayoutParams(-1,-2));
        View connectionsGap=new View(this);
        body.addView(connectionsGap,new LinearLayout.LayoutParams(-1,dp(10)));
        tile(body,"Species Atlas",()->openAtlas(null,null));
        tile(body,"Habitat Atlas",()->startActivity(
            new Intent(this,HabitatSpeciesEvidenceActivity.class)));
        tile(body,"References",()->startActivity(
            new Intent(this,PhLibraryBibliographyActivity.class)));
        tile(body,"Saved species",()->openAtlas("phlibrary_saved_only","true"));
        tile(body,"Offline Map",()->
            startActivity(new Intent(this,ApoOfflineMapActivity.class)));
        // Native photos ship inside the signed APK; no photo import is needed.

    }
}
