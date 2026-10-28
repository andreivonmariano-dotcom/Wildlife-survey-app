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
    private static java.util.HashSet<String> names;
    private static int dp(Activity a,int p){
        return Math.round(p*a.getResources().getDisplayMetrics().density);
    }
    /** Both private source batches are stable and read-only, with unique keys. */
    public static synchronized JSONArray read(Activity a){
        if(accounts!=null)return accounts;
        JSONArray combined=new JSONArray();
        java.util.HashSet<String> seen=new java.util.HashSet<>();
        for(String name:new String[]{"rc7a_field_cards_v1.json","rc7b_field_cards_v1.json"}){
            try(InputStream in=a.getAssets().open(name);
                ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1){
                    if(out.size()+n>240000)throw new IllegalArgumentException("RC7 pack too large");
                    out.write(buffer,0,n);
                }
                JSONObject pack=new JSONObject(out.toString("UTF-8"));
                if(!"rc7a-priority-cards-1".equals(pack.optString("schemaVersion")))
                    continue;
                JSONArray listed=pack.getJSONArray("accounts");
                for(int i=0;i<listed.length();i++){
                    JSONObject item=listed.optJSONObject(i);
                    if(item==null)continue;
                    String key=item.optString("sourceTaxonKey");
                    if(!key.isEmpty()&&seen.add(key))combined.put(item);
                }
            }catch(Exception ignored){}
        }
        accounts=combined;
        names=new java.util.HashSet<>();
        for(int i=0;i<accounts.length();i++){
            JSONObject p=accounts.optJSONObject(i);
            if(p!=null)names.add(p.optString("scientificName")
                .toLowerCase(java.util.Locale.ROOT));
        }
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
        read(a);
        return scientific!=null&&names!=null&&names.contains(
            scientific.toLowerCase(java.util.Locale.ROOT));
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
        parent.addView(FieldVisuals.section(a,"RC7 priority research card",
            detailed?"Existing source-linked field account plus priority checklist record":
                "Taxonomy verified against checklist · species biology review pending"));
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
        return true;
    }
}
