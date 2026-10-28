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
    private static int dp(Activity a,int x){
        return Math.round(x*a.getResources().getDisplayMetrics().density);
    }
    public static void add(Activity a,LinearLayout parent,String id){
        if(id==null||id.trim().isEmpty())return;
        JSONArray items=manifest(a).optJSONArray("items");
        if(items==null)return;
        JSONObject found=null;
        for(int i=0;i<items.length();i++){
            JSONObject item=items.optJSONObject(i);
            if(item!=null&&id.equals(item.optString("id"))){found=item;break;}
        }
        if(found==null)return;
        String file=found.optString("offlineAsset");
        if(!file.matches("offline_photos/(species|habitat)_[a-z0-9_]+\\.jpg"))return;
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
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setContentDescription(found.optString("caption",
                    "Photographic wildlife reference"));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,210));
                lp.topMargin=dp(a,9);lp.bottomMargin=dp(a,6);
                parent.addView(image,lp);
                loaded=true;
            }
        }catch(Exception ignored){}
        TextView attribution=new TextView(a);
        attribution.setText((loaded?"Reference photo: ":"Reference photo not yet bundled. Credit: ")+
            found.optString("author")+" · "+found.optString("license")+
            "\n"+found.optString("caption")+
            "\nPhoto source and license: "+found.optString("sourceUrl")+
            " · "+found.optString("licenseUrl"));
        attribution.setTextSize(11);
        attribution.setTextColor(UiKit.MUTED);
        attribution.setTextIsSelectable(true);
        attribution.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        attribution.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        attribution.setPadding(dp(a,5),0,dp(a,5),dp(a,7));
        parent.addView(attribution);
    }
}
