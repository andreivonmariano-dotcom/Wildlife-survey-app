package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Read-only RC7 priority research cards. Catalog data and raw observations remain unchanged. */
public final class Rc7FieldAccounts {
    private Rc7FieldAccounts(){}
    private static JSONArray accounts;
    private static int dp(Activity a,int p){
        return Math.round(p*a.getResources().getDisplayMetrics().density);
    }
    public static JSONArray read(Activity a){
        if(accounts!=null)return accounts;
        try(InputStream in=a.getAssets().open("rc7a_field_cards_v1.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>240000)throw new IllegalArgumentException("RC7 pack too large");
                out.write(buffer,0,n);
            }
            JSONObject pack=new JSONObject(out.toString("UTF-8"));
            if(!"rc7a-priority-cards-1".equals(pack.optString("schemaVersion")))
                throw new IllegalArgumentException("Unsupported priority pack");
            accounts=pack.getJSONArray("accounts");
        }catch(Exception e){accounts=new JSONArray();}
        return accounts;
    }
    public static JSONObject find(Activity a,String scientific){
        JSONArray all=read(a);
        if(scientific==null)return null;
        for(int i=0;i<all.length();i++){
            JSONObject entry=all.optJSONObject(i);
            if(entry!=null&&scientific.equalsIgnoreCase(entry.optString("scientificName")))
                return entry;
        }
        return null;
    }
    public static boolean priority(Activity a,String scientific){
        return find(a,scientific)!=null;
    }
    private static void line(Activity a,LinearLayout parent,String label,String value){
        if(value==null||value.trim().isEmpty())return;
        TextView heading=new TextView(a);
        heading.setText(label);heading.setTextSize(13);heading.setTypeface(null,Typeface.BOLD);
        heading.setTextColor(UiKit.FOREST);
        heading.setPadding(dp(a,4),dp(a,11),dp(a,3),dp(a,3));
        parent.addView(heading);
        TextView body=new TextView(a);
        body.setText(value);body.setTextSize(15);body.setTextColor(UiKit.INK);
        body.setLineSpacing(dp(a,2),1.07f);body.setTextIsSelectable(true);
        body.setPadding(dp(a,4),0,dp(a,3),dp(a,3));
        parent.addView(body);
    }
    public static boolean add(Activity a,LinearLayout parent,String scientific){
        JSONObject p=find(a,scientific);
        if(p==null)return false;
        boolean detailed="existing_source_linked_account".equals(p.optString("accountTier"));
        parent.addView(FieldVisuals.section(a,"RC7A priority research card",
            detailed?"Existing source-linked field account plus priority checklist record":
                "Taxonomy verified against checklist · species biology review pending"));
        OfflinePhotoEvidence.addForTaxon(a,parent,scientific);
        line(a,parent,"Identity and source family",
            p.optString("commonName")+" · "+scientific+" · "+p.optString("family"));
        if(!detailed)
            line(a,parent,"Identification review status",p.optString("identification"));
        line(a,parent,"Record in the field",p.optString("fieldDocumentation"));
        line(a,parent,"Species determination caution",p.optString("identificationCaution"));
        JSONArray peers=p.optJSONArray("sameGenusCandidates");
        if(peers!=null&&peers.length()>0){
            StringBuilder genus=new StringBuilder();
            for(int i=0;i<peers.length();i++){
                if(genus.length()>0)genus.append(", ");
                genus.append(peers.optString(i));
            }
            line(a,parent,"Same-genus research candidates (not confirmed lookalikes)",
                genus.toString());
        }
        Button compare=UiKit.button(a,"Compare cited identification characters","secondary");
        parent.addView(compare);
        compare.setOnClickListener(v->a.startActivity(
            new Intent(a,DiagnosticGuideActivity.class)
                .putExtra("scientific_name",scientific)));
        line(a,parent,"Source citation",p.optString("sourceCitation")+
            "\n"+p.optString("sourceUrl"));
        line(a,parent,"Scientific review limits",p.optString("scientificLimits"));
        Button observe=UiKit.button(a,"Search dreidrei iNaturalist observations","quiet");
        observe.setContentDescription("Search iNaturalist records from dreidrei for this taxon; external site");
        parent.addView(observe);
        observe.setOnClickListener(v->{
            String url="https://www.inaturalist.org/observations?user_id=dreidrei&taxon_name="+
                Uri.encode(scientific)+"&photos=true";
            a.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
        });
        return true;
    }
}
