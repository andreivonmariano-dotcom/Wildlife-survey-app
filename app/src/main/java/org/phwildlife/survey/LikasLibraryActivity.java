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
    private static final int PHOTO_PACK_PICKER=8415;
    private static final int MAX_BULK_SELECTION=400;
    private static final class PhotoMatch{
        final Uri uri;final String species;
        PhotoMatch(Uri uri,String species){this.uri=uri;this.species=species;}
    }
    private void selectSpeciesPhotos(){
        new AlertDialog.Builder(this).setTitle("Owner's Drive photographs")
            .setItems(new String[]{"Import LIKAS Drive photo ZIP",
                "Select individual owner Drive photos"},(d,choice)->{
                Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if(choice==0){
                    picker.setType("*/*");
                    picker.putExtra(Intent.EXTRA_MIME_TYPES,
                        new String[]{"application/zip","application/x-zip-compressed"});
                }else{
                    picker.setType("image/*");
                    picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                }
                try{startActivityForResult(picker,
                    choice==0?PHOTO_PACK_PICKER:PHOTO_BULK_PICKER);}
                catch(Exception e){new AlertDialog.Builder(this)
                    .setMessage("No document picker is available.")
                    .setPositiveButton("Close",null).show();}
            }).setNegativeButton("Close",null).show();
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
    private void importOwnerPack(Uri uri){
        new AlertDialog.Builder(this).setTitle("Import owner's Drive photo pack?")
            .setMessage("Import the LIKAS ZIP prepared from your 333 Google Drive photos. "+
                "Every matched species gets its original set of photographs, stored "+
                "offline at account-view resolution without GPS metadata. "+
                "The source files remain unchanged. Only entries in the verified "+
                "Drive filename inventory are accepted.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Import",(d,w)->new Thread(()->{
                String message;
                try{message=PhLibraryDrivePackImport.importZip(this,uri).summary();}
                catch(Exception e){message="Photo pack could not be imported: "+e.getMessage();}
                final String finalMessage=message;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    new AlertDialog.Builder(this).setTitle("Drive photo import")
                        .setMessage(finalMessage).setPositiveButton("Close",null).show();
                });
            },"phlibrary-drive-pack-import").start()).show();
    }
    private void reviewBulkSelection(Intent chosen){
        ArrayList<Uri> selected=new ArrayList<>();
        ClipData clips=chosen.getClipData();
        if(clips!=null){
            for(int i=0;i<clips.getItemCount()&&i<=MAX_BULK_SELECTION;i++)
                selected.add(clips.getItemAt(i).getUri());
        }else if(chosen.getData()!=null)selected.add(chosen.getData());
        if(selected.isEmpty())return;
        if(selected.size()>MAX_BULK_SELECTION){
            new AlertDialog.Builder(this).setMessage("Select up to 400 photos.")
                .setPositiveButton("Close",null).show();return;
        }
        ArrayList<PhotoMatch> approved=new ArrayList<>();
        int ignored=0;
        java.util.HashSet<String> species=new java.util.HashSet<>();
        for(Uri uri:selected){
            String name=displayName(uri);
            String match=PhLibraryBulkPhotoImport.exactTaxon(this,name);
            if(match==null){ignored++;continue;}
            approved.add(new PhotoMatch(uri,match));
            species.add(match);
        }
        if(approved.isEmpty()){
            new AlertDialog.Builder(this).setTitle("No owner Drive files matched")
                .setMessage("Choose photographs with exact filenames from your "+
                    "333-file LIKAS Drive collection. No files were changed.")
                .setPositiveButton("Close",null).show();return;
        }
        final int rejected=ignored;
        new AlertDialog.Builder(this).setTitle("Add "+approved.size()+" Drive photos?")
            .setMessage("Files match "+species.size()+" Book 01 species accounts by filename. "+
                "This does not independently verify the identifications. "+
                rejected+" files were excluded. Other existing photos are preserved.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Import",(d,w)->new Thread(()->{
                int done=0,failed=0;
                for(PhotoMatch item:approved){
                    try{PhLibrarySpeciesMedia.importPrivate(this,item.species,item.uri);done++;}
                    catch(Exception e){failed++;}
                }
                final int ok=done,bad=failed;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    new AlertDialog.Builder(this).setTitle("Drive photographs added")
                        .setMessage(ok+" owner photographs saved to species accounts. "+
                            bad+" files failed. No source files were modified.")
                        .setPositiveButton("Close",null).show();
                });
            },"phlibrary-private-bulk-media").start()).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null)return;
        if(request==PHOTO_BULK_PICKER)reviewBulkSelection(data);
        else if(request==PHOTO_PACK_PICKER&&data.getData()!=null)
            importOwnerPack(data.getData());
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
        TextView title=PhLibrary2Ui.text(this,"PHLibrary",15,PhLibrary2Ui.FOREST,false,true);
        title.setGravity(Gravity.CENTER);
        if(getResources().getConfiguration().fontScale>=1.6f)title.setTextSize(12);
        nav.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(nav);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,dp(9),0,dp(26));scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
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
        tile(body,"Species Atlas",()->openAtlas(null,null));
        tile(body,"Habitat Atlas",()->startActivity(
            new Intent(this,HabitatSpeciesEvidenceActivity.class)));
        tile(body,"References",()->startActivity(
            new Intent(this,PhLibraryBibliographyActivity.class)));
        tile(body,"Saved species",()->openAtlas("phlibrary_saved_only","true"));
        tile(body,"Import Drive photos",this::selectSpeciesPhotos);

    }
}
