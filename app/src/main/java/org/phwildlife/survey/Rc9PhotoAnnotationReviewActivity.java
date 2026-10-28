package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/** Produces drafts only. A scientific reviewer must approve source and marker location. */
public final class Rc9PhotoAnnotationReviewActivity extends Activity {
    private static final String FILE="rc9_anatomical_annotation_drafts.json";
    private final ArrayList<JSONObject> photos=new ArrayList<>();
    private final ArrayList<JSONObject> claims=new ArrayList<>();
    private final java.util.HashMap<String,JSONObject> sourceProfiles=new java.util.HashMap<>();
    private Spinner choosePhoto,chooseTrait;
    private EditText reviewer,notes;
    private TextView status;
    private Rc9PhotoMarkerView marker;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);
        t.setTextColor(UiKit.INK);t.setPadding(dp(4),dp(4),dp(4),dp(5));
        t.setTextIsSelectable(true);return t;
    }
    private static JSONObject read(Activity a,String filename)throws Exception{
        try(InputStream in=a.getAssets().open(filename);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>480000)throw new IllegalArgumentException("Reference exceeds limit");
                out.write(b,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }
    }
    private static boolean owned(JSONObject photo){
        return "dreidrei".equalsIgnoreCase(photo.optString("observer"))&&
            "owner_direct_private_research_permission".equals(photo.optString("rightsBasis"))&&
            photo.optString("offlineAsset").matches("offline_photos/inat_[0-9]+\\.jpg");
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(UiKit.BG);root.setPadding(dp(14),dp(9),dp(14),dp(11));
        root.addView(UiKit.navigationHeader(this,"Anatomical annotation drafts","Species",this::finish));
        root.addView(text("Tap the exact anatomical feature in a photograph you own. "+
            "Each draft links to a cited LIKAS trait. It is never an expert-approved marker. "+
            "Review source, image position, identity and diagnostic value before publication.",13));
        try{
            JSONArray species=read(this,"rc8_evidence_comparisons_v1.json").getJSONArray("species");
            for(int i=0;i<species.length();i++){
                JSONObject p=species.optJSONObject(i);
                if(p!=null)sourceProfiles.put(p.optString("scientificName"),p);
            }
            JSONArray images=read(this,"offline_inaturalist_manifest_v1.json").getJSONArray("items");
            for(int i=0;i<images.length();i++){
                JSONObject p=images.optJSONObject(i);
                if(p!=null&&owned(p)&&sourceProfiles.containsKey(p.optString("taxon"))){
                    try(InputStream in=getAssets().open(p.optString("offlineAsset"))){
                        if(in.read()!=-1)photos.add(p);
                    }catch(Exception ignored){}
                }
            }
            if(photos.isEmpty())throw new IllegalArgumentException("No owned reference photos");
        }catch(Exception ex){
            root.addView(text("Owner-approved offline reference photos are unavailable.",15));
            setContentView(root);return;
        }
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        choosePhoto=new Spinner(this);chooseTrait=new Spinner(this);
        choosePhoto.setContentDescription("Choose owner-authored wildlife photograph");
        chooseTrait.setContentDescription("Choose cited morphological trait for image mark");
        ArrayList<String> options=new ArrayList<>();
        for(JSONObject p:photos)options.add(p.optString("taxon")+" · photo "+p.optString("photoId"));
        choosePhoto.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,options));
        body.addView(text("Owner's original reference photo",13));body.addView(choosePhoto);
        marker=new Rc9PhotoMarkerView(this);marker.allowDraftTap(true);
        body.addView(marker,new LinearLayout.LayoutParams(-1,dp(290)));
        body.addView(text("Character linked to published evidence",13));body.addView(chooseTrait);
        reviewer=new EditText(this);reviewer.setSingleLine(true);
        reviewer.setHint("Reviewer name or initials");
        reviewer.setContentDescription("Reviewer identification for pending draft");
        body.addView(reviewer);
        notes=new EditText(this);notes.setHint("Visible character notes and uncertainty");
        notes.setMinLines(2);notes.setMaxLines(4);
        body.addView(notes);
        status=text("No review marker selected. Tap directly on the photograph.",12);
        body.addView(status);
        Button save=UiKit.button(this,"Save as unreviewed annotation draft","primary");
        body.addView(save,new LinearLayout.LayoutParams(-1,dp(52)));
        save.setOnClickListener(v->saveDraft());
        Button share=UiKit.button(this,"Share drafts for specialist review","secondary");
        body.addView(share,new LinearLayout.LayoutParams(-1,dp(48)));
        share.setOnClickListener(v->shareDrafts());
        setContentView(root);
        choosePhoto.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int i,long id){photoChanged();}
            @Override public void onNothingSelected(android.widget.AdapterView<?> p){}
        });
        String requested=getIntent().getStringExtra("scientific_name");
        int photoId=getIntent().getIntExtra("photo_id",-1);
        int select=0;
        for(int i=0;i<photos.size();i++){
            JSONObject photo=photos.get(i);
            if(photoId>0&&photo.optInt("photoId")==photoId){select=i;break;}
            if(requested!=null&&requested.equalsIgnoreCase(photo.optString("taxon")))select=i;
        }
        choosePhoto.setSelection(select);
        photoChanged();
    }
    private JSONObject photo(){
        int p=choosePhoto.getSelectedItemPosition();
        return p>=0&&p<photos.size()?photos.get(p):null;
    }
    private void photoChanged(){
        JSONObject p=photo();if(p==null||marker==null)return;
        marker.useAsset(p.optString("offlineAsset"));
        claims.clear();
        JSONObject source=sourceProfiles.get(p.optString("taxon"));
        JSONArray traits=source==null?null:source.optJSONArray("traits");
        ArrayList<String> names=new ArrayList<>();
        if(traits!=null)for(int i=0;i<traits.length();i++){
            JSONObject t=traits.optJSONObject(i);
            if(t==null||!"Identification".equals(t.optString("category")))continue;
            if(!t.optString("url").startsWith("https://"))continue;
            claims.add(t);
            names.add(t.optString("label"));
        }
        if(names.isEmpty())names.add("No cited anatomical character available");
        chooseTrait.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item,names));
        status.setText(claims.isEmpty()?
            "Not assessed: source-linked anatomical characters needed before drafting.":
            "Tap the location of the listed trait. This will be an unreviewed draft.");
    }
    private JSONObject draftPack(){
        File file=new File(getFilesDir(),FILE);
        if(!file.isFile())return new JSONObject();
        try(FileInputStream in=new FileInputStream(file);
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){
                if(out.size()+n>180000)return new JSONObject();
                out.write(b,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }catch(Exception e){return new JSONObject();}
    }
    private void saveDraft(){
        JSONObject p=photo();int index=chooseTrait.getSelectedItemPosition();
        String who=reviewer.getText().toString().trim();
        if(p==null||index<0||index>=claims.size()||!marker.hasDraft()||who.isEmpty()){
            status.setText("Select a cited character, tap the photo and enter reviewer initials.");
            return;
        }
        try{
            JSONObject item=new JSONObject();
            JSONObject trait=claims.get(index);
            item.put("photoId",p.getInt("photoId"));
            item.put("photoSha256",p.optString("sha256"));
            item.put("scientificName",p.optString("taxon"));
            item.put("sourcePhoto",p.optString("sourceUrl"));
            item.put("photographer",p.optString("author"));
            item.put("traitId","literature:"+p.optString("taxon")+":"+trait.optString("label"));
            item.put("character",trait.optString("label"));
            item.put("citation",trait.optString("url"));
            item.put("sourceStatement",trait.optString("value"));
            item.put("x",Math.round(marker.markerX()*10000)/10000.0);
            item.put("y",Math.round(marker.markerY()*10000)/10000.0);
            item.put("reviewer",who.substring(0,Math.min(80,who.length())));
            String note=notes.getText().toString().trim();
            item.put("notes",note.substring(0,Math.min(800,note.length())));
            item.put("reviewStatus","pending_expert_review");
            java.text.SimpleDateFormat utc=new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
            utc.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            item.put("createdUtc",utc.format(new java.util.Date()));
            JSONObject pack=draftPack();
            JSONArray items=pack.optJSONArray("items");
            if(items==null)items=new JSONArray();
            if(items.length()>=150)throw new IllegalStateException("Review draft capacity reached; share previous drafts");
            items.put(item);
            pack.put("schemaVersion","rc9-annotation-draft-v1");
            pack.put("items",items);
            try(FileOutputStream out=openFileOutput(FILE,MODE_PRIVATE)){
                out.write(pack.toString(2).getBytes("UTF-8"));
            }
            status.setText("Draft saved locally ("+items.length()+"). NOT expert-approved. Share for review.");
        }catch(Exception ex){status.setText("Draft could not be saved. No survey records were modified.");}
    }
    private void shareDrafts(){
        JSONObject data=draftPack();
        if(data.optJSONArray("items")==null||data.optJSONArray("items").length()==0){
            status.setText("No drafts to share.");return;
        }
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("application/json");
        send.putExtra(Intent.EXTRA_SUBJECT,"LIKAS anatomical annotations requiring expert review");
        send.putExtra(Intent.EXTRA_TEXT,data.toString());
        startActivity(Intent.createChooser(send,"Send source-linked review drafts"));
    }
}
