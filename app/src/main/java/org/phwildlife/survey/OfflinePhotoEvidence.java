package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/** Reads only already-bundled CC thumbnails; never fetches wildlife images during fieldwork. */
public final class OfflinePhotoEvidence {
    private OfflinePhotoEvidence(){}
    private static JSONObject manifest;
    private static JSONObject manifest(Activity a){
        if(manifest==null){
            try(InputStream in=a.getAssets().open("offline_photo_manifest_v1.json")){
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                byte[] buf=new byte[4096];int n;
                while((n=in.read(buf))>0){
                    if(out.size()+n>48000)throw new IllegalStateException("Manifest too large");
                    out.write(buf,0,n);
                }
                manifest=new JSONObject(out.toString("UTF-8"));
            }catch(Exception ignored){manifest=new JSONObject();}
        }
        return manifest;
    }
    private static JSONObject inatManifest;
    private static JSONObject inatManifest(Activity a){
        if(inatManifest==null){
            try(InputStream in=a.getAssets().open("offline_inaturalist_manifest_v1.json");
                ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] buf=new byte[4096];int n;
                while((n=in.read(buf))!=-1){
                    if(out.size()+n>180000)throw new IllegalArgumentException("Photo manifest too large");
                    out.write(buf,0,n);
                }
                inatManifest=new JSONObject(out.toString("UTF-8"));
            }catch(Exception ignored){inatManifest=new JSONObject();}
        }
        return inatManifest;
    }
    private static JSONObject findForTaxon(Activity a,String scientific){
        if(scientific==null||scientific.isEmpty())return null;
        for(JSONArray list:new JSONArray[]{manifest(a).optJSONArray("items"),
                inatManifest(a).optJSONArray("items")}){
            if(list==null)continue;
            for(int i=0;i<list.length();i++){
                JSONObject item=list.optJSONObject(i);
                if(item==null||!scientific.equalsIgnoreCase(item.optString("taxon")))continue;
                String file=item.optString("offlineAsset");
                if(!file.matches("offline_photos/(species|habitat|inat)_[a-z0-9_]+\\.jpg"))continue;
                try(InputStream stream=a.getAssets().open(file)){
                    if(stream.read()!=-1)return item;
                }catch(Exception ignored){}
            }
        }
        return null;
    }
    /** Only returns true for a genuinely installed, attribution-linked JPEG. */
    public static boolean hasOfflineTaxon(Activity a,String scientific){
        return findForTaxon(a,scientific)!=null;
    }
    /** iNaturalist references are rendered only if a verified JPEG was bundled. */
    public static void addForTaxon(Activity a,LinearLayout parent,String scientific){
        JSONArray items=inatManifest(a).optJSONArray("items");
        if(items==null)return;
        for(int i=0;i<items.length();i++){
            JSONObject photo=items.optJSONObject(i);
            if(photo!=null&&scientific.equalsIgnoreCase(photo.optString("taxon"))){
                add(a,parent,photo.optString("id"));
                return;
            }
        }
    }
    private static int dp(Activity a,int x){
        return Math.round(x*a.getResources().getDisplayMetrics().density);
    }
    public static void add(Activity a,LinearLayout parent,String id){
        if(id==null||id.trim().isEmpty())return;
        JSONObject found=null;
        for(JSONArray items:new JSONArray[]{manifest(a).optJSONArray("items"),
                inatManifest(a).optJSONArray("items")}){
            if(items==null)continue;
            for(int i=0;i<items.length();i++){
                JSONObject item=items.optJSONObject(i);
                if(item!=null&&id.equals(item.optString("id"))){found=item;break;}
            }
            if(found!=null)break;
        }
        if(found==null)return;
        String file=found.optString("offlineAsset");
        if(!file.matches("offline_photos/(species|habitat|inat)_[a-z0-9_]+\\.jpg"))return;
        boolean loaded=false;
        try(InputStream stream=a.getAssets().open(file)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buffer=new byte[8192];int n;
            while((n=stream.read(buffer))>0){
                if(out.size()+n>900000)throw new IllegalArgumentException("Photo too large");
                out.write(buffer,0,n);
            }
            Bitmap bitmap=BitmapFactory.decodeByteArray(out.toByteArray(),0,out.size());
            if(bitmap!=null){
                ImageView image=new ImageView(a);
                image.setImageBitmap(bitmap);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setBackgroundColor(UiKit.BG);
                image.setContentDescription(found.optString("caption",
                    "Photographic wildlife reference"));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,210));
                lp.topMargin=dp(a,9);lp.bottomMargin=dp(a,6);
                parent.addView(image,lp);
                loaded=true;
            }
        }catch(Exception ignored){}
        TextView credit=new TextView(a);
        credit.setText(found.optString("caption")+"\n"+
            "Photo: "+found.optString("author")+" · "+found.optString("license"));
        credit.setTextSize(13);
        credit.setTextColor(UiKit.MUTED);
        credit.setPadding(dp(a,5),0,dp(a,5),dp(a,5));
        parent.addView(credit);
        android.widget.Button details=UiKit.button(a,
            "Photo source & license  ▾","quiet");
        LinearLayout.LayoutParams detailsLp=
            new LinearLayout.LayoutParams(-1,dp(a,44));
        parent.addView(details,detailsLp);
        TextView attribution=new TextView(a);
        attribution.setText("Photographer: "+found.optString("author")+
            "\nLicense: "+found.optString("license")+
            "\nFile source: "+found.optString("sourceUrl")+
            "\nLicense terms: "+found.optString("licenseUrl"));
        attribution.setTextSize(12);
        attribution.setTextColor(UiKit.MUTED);
        attribution.setTextIsSelectable(true);
        attribution.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        attribution.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        attribution.setPadding(dp(a,5),dp(a,7),dp(a,5),dp(a,8));
        attribution.setVisibility(View.GONE);
        parent.addView(attribution);
        details.setOnClickListener(v->{
            boolean open=attribution.getVisibility()!=View.VISIBLE;
            attribution.setVisibility(open?View.VISIBLE:View.GONE);
            details.setText(open?"Photo source & license  ▴":
                "Photo source & license  ▾");
        });
    }
}
