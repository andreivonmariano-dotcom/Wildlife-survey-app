package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

/** Detailed offline reference descriptions never mutate field detections or locations. */
public final class OfflineFieldLibrary {
    private OfflineFieldLibrary(){}
    private static int dp(Activity a,int n){
        return Math.round(n*a.getResources().getDisplayMetrics().density);
    }
    private static JSONObject read(Activity a,String filename){
        try(InputStream in=a.getAssets().open(filename)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buf=new byte[4096];int n;
            while((n=in.read(buf))>0){
                if(out.size()+n>250000)throw new IllegalStateException("Reference too large");
                out.write(buf,0,n);
            }
            return new JSONObject(out.toString("UTF-8"));
        }catch(Exception ignored){return new JSONObject();}
    }
    private static TextView text(Activity a,String value,boolean heading){
        TextView label=new TextView(a);
        label.setText(value);
        label.setTextSize(heading?16:15);
        label.setLineSpacing(dp(a,3),1.05f);
        label.setTextColor(heading?UiKit.FOREST:UiKit.INK);
        label.setPadding(dp(a,4),dp(a,heading?10:3),dp(a,4),dp(a,3));
        if(heading)label.setTypeface(null,Typeface.BOLD);
        label.setTextIsSelectable(true);
        return label;
    }
    private static void paragraph(Activity a,LinearLayout out,String name,String body){
        if(body==null||body.trim().isEmpty())return;
        out.addView(text(a,name,true));
        out.addView(text(a,body,false));
    }
    private static JSONObject source(JSONArray sources,String id){
        if(sources==null)return null;
        for(int i=0;i<sources.length();i++){
            JSONObject src=sources.optJSONObject(i);
            if(src!=null&&id.equals(src.optString("id")))return src;
        }
        return null;
    }
    private static void citationList(Activity a,LinearLayout target,
            JSONArray sources,JSONArray ids){
        if(ids==null||sources==null)return;
        HashSet<String> used=new HashSet<>();
        target.addView(text(a,"References and source editions",true));
        for(int i=0;i<ids.length();i++){
            String id=ids.optString(i);
            if(!used.add(id))continue;
            JSONObject src=source(sources,id);
            if(src==null)continue;
            String title=src.optString("title");
            TextView citation=text(a,src.optString("publisher")+
                " · "+title+"\nAccessed "+src.optString("accessed")+
                "\n"+src.optString("url"),false);
            citation.setTextColor(UiKit.MUTED);
            citation.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
            citation.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            target.addView(citation);
        }
    }
    public interface SpeciesChoice {
        void choose(String scientificName);
    }
    public static int[] availableCounts(Activity a){
        JSONArray species=read(a,"diagnostic_profiles_v1.json").optJSONArray("profiles");
        JSONArray habitats=read(a,"offline_habitat_profiles_v1.json").optJSONArray("profiles");
        return new int[]{species==null?0:species.length(),
            habitats==null?0:habitats.length()};
    }
    public static void addSpeciesDirectory(Activity a,LinearLayout target,
            SpeciesChoice choice){
        JSONArray species=read(a,"diagnostic_profiles_v1.json").optJSONArray("profiles");
        if(species==null||species.length()==0)return;
        target.addView(FieldVisuals.section(a,"Browse detailed field guides",
            species.length()+" introductory, source-checked species accounts"));
        LinearLayout all=new LinearLayout(a);
        all.setOrientation(LinearLayout.VERTICAL);
        all.setVisibility(View.GONE);
        java.util.HashSet<String> featured=new java.util.HashSet<>();
        for(int i=0;i<species.length();i++){
            JSONObject entry=species.optJSONObject(i);
            if(entry==null)continue;
            String group=entry.optString("group");
            String scientific=entry.optString("scientificName");
            String title=entry.optString("commonName");
            if(scientific.isEmpty())continue;
            Button action=UiKit.button(a,title+"  ·  "+group,"secondary");
            action.setOnClickListener(v->choice.choose(scientific));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,49));
            lp.bottomMargin=dp(a,6);
            if(featured.add(group))target.addView(action,lp);
            else all.addView(action,lp);
        }
        Button more=UiKit.button(a,"More detailed accounts  ▾","quiet");
        target.addView(more,new LinearLayout.LayoutParams(-1,dp(a,48)));
        target.addView(all);
        more.setOnClickListener(v->{
            boolean show=all.getVisibility()!=View.VISIBLE;
            all.setVisibility(show?View.VISIBLE:View.GONE);
            more.setText(show?"Fewer accounts  ▴":"More detailed accounts  ▾");
        });
    }
    /** Source checked only; absent account is clearly labeled, not populated by guesswork. */
    public static boolean addSpecies(Activity a,LinearLayout target,String scientific){
        JSONObject bundle=read(a,"diagnostic_profiles_v1.json");
        JSONArray list=bundle.optJSONArray("profiles");
        if(list==null)return false;
        JSONObject profile=null;
        for(int i=0;i<list.length();i++){
            JSONObject entry=list.optJSONObject(i);
            if(entry!=null&&scientific.equalsIgnoreCase(entry.optString("scientificName"))){
                profile=entry;break;
            }
        }
        if(profile==null)return false;
        JSONObject account=profile.optJSONObject("account");
        if(account==null)return false;
        target.addView(FieldVisuals.section(a,"Field identification and ecology",
            "Source-checked reference account, not a verified station observation"));
        OfflinePhotoEvidence.add(a,target,account.optString("photoId"));
        paragraph(a,target,"Description",account.optString("overview"));
        paragraph(a,target,"Habitat and microhabitat",account.optString("habitat"));
        Button more=UiKit.button(a,"Field notes & source references  ▾","quiet");
        LinearLayout.LayoutParams moreLp=new LinearLayout.LayoutParams(-1,dp(a,48));
        moreLp.topMargin=dp(a,8);target.addView(more,moreLp);
        LinearLayout extra=new LinearLayout(a);
        extra.setOrientation(LinearLayout.VERTICAL);
        extra.setVisibility(View.GONE);
        paragraph(a,extra,"Natural history and activity",account.optString("behavior"));
        paragraph(a,extra,"Useful field documentation",account.optString("fieldNotes"));
        paragraph(a,extra,"Identification cautions",account.optString("cautions"));
        citationList(a,extra,bundle.optJSONArray("sourceList"),
            account.optJSONArray("sourceIds"));
        target.addView(extra);
        more.setOnClickListener(v->{
            boolean open=extra.getVisibility()!=View.VISIBLE;
            extra.setVisibility(open?View.VISIBLE:View.GONE);
            more.setText(open?"Field notes & source references  ▴":
                "Field notes & source references  ▾");
        });
        return true;
    }
    /** Filtered, compact and lazy-loaded habitat guide: one expanded card at a time. */
    public static void addHabitats(Activity a,LinearLayout target){
        addHabitats(a,target,"All habitats");
    }
    public static void addHabitats(Activity a,LinearLayout target,String chosenGroup){
        JSONObject pack=read(a,"offline_habitat_profiles_v1.json");
        JSONArray profiles=pack.optJSONArray("profiles");
        if(profiles==null){
            target.addView(text(a,"Habitat reference pack unavailable offline.",false));
            return;
        }
        int matches=0;
        for(int n=0;n<profiles.length();n++){
            JSONObject profile=profiles.optJSONObject(n);
            if(profile!=null&&("All habitats".equals(chosenGroup)||
                chosenGroup.equals(profile.optString("group"))))matches++;
        }
        target.addView(FieldVisuals.section(a,"Philippine habitat profiles",
            matches+" of "+profiles.length()+" source-backed accounts · choose a habitat"));
        final ArrayList<LinearLayout> openDetails=new ArrayList<>();
        final ArrayList<Button> toggles=new ArrayList<>();
        final ArrayList<String> labels=new ArrayList<>();
        for(int i=0;i<profiles.length();i++){
            JSONObject profile=profiles.optJSONObject(i);
            if(profile==null||!("All habitats".equals(chosenGroup)||
                chosenGroup.equals(profile.optString("group"))))continue;
            LinearLayout card=new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(a,12),dp(a,8),dp(a,12),dp(a,10));
            card.setBackground(UiKit.surface(a));
            String name=profile.optString("name");
            Button toggle=UiKit.button(a,name+"  ▾","quiet");
            toggle.setContentDescription("Read "+name+" habitat guide");
            card.addView(toggle,new LinearLayout.LayoutParams(-1,dp(a,52)));
            TextView teaser=text(a,profile.optString("overview"),false);
            teaser.setMaxLines(2);
            teaser.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(teaser);
            LinearLayout detail=new LinearLayout(a);
            detail.setOrientation(LinearLayout.VERTICAL);
            detail.setVisibility(View.GONE);
            card.addView(detail);
            openDetails.add(detail);toggles.add(toggle);labels.add(name);
            toggle.setOnClickListener(v->{
                boolean open=detail.getVisibility()!=View.VISIBLE;
                for(int n=0;n<openDetails.size();n++){
                    openDetails.get(n).setVisibility(View.GONE);
                    toggles.get(n).setText(labels.get(n)+"  ▾");
                }
                if(open){
                    if(detail.getChildCount()==0){
                        OfflinePhotoEvidence.add(a,detail,profile.optString("photoId"));
                        paragraph(a,detail,"Habitat description",profile.optString("overview"));
                        paragraph(a,detail,"Field recognition",
                            profile.optString("fieldIndicators"));
                        paragraph(a,detail,"Water, geology and substrate",
                            profile.optString("waterAndSubstrate"));
                        paragraph(a,detail,"Vegetation and wildlife context",
                            profile.optString("vegetationAndWildlife"));
                        JSONArray metrics=profile.optJSONArray("measurements");
                        if(metrics!=null){
                            StringBuilder measurements=new StringBuilder();
                            for(int n=0;n<metrics.length();n++){
                                if(n>0)measurements.append("\\n");
                                measurements.append("• ").append(metrics.optString(n));
                            }
                            paragraph(a,detail,"What to measure",measurements.toString());
                        }
                        paragraph(a,detail,"Scientific cautions",
                            profile.optString("limitations"));
                        citationList(a,detail,pack.optJSONArray("sources"),
                            profile.optJSONArray("sourceIds"));
                    }
                    detail.setVisibility(View.VISIBLE);
                    toggle.setText(name+"  ▴");
                }
                teaser.setVisibility(open?View.GONE:View.VISIBLE);
            });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(a,10);
            target.addView(card,lp);
        }
        if(matches==0)target.addView(FieldVisuals.context(a,
            "No habitat reference matches this group."));
    }
}
