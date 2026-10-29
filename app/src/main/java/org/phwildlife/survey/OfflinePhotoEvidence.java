package org.phwildlife.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/** Displays locally approved CC or directly owner-authorized photographs; no field-time fetches. */
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
        for(JSONArray list:new JSONArray[]{inatManifest(a).optJSONArray("items"),
                manifest(a).optJSONArray("items")}){
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
    private static java.util.HashSet<String> availablePhotoTaxa;
    /** Cache actual APK media presence to keep offline 1,500-taxon filtering fast. */
    public static synchronized boolean hasOfflineTaxon(Activity a,String scientific){
        if(scientific==null)return false;
        if(availablePhotoTaxa==null){
            java.util.HashSet<String> names=new java.util.HashSet<>();
            for(JSONArray list:new JSONArray[]{inatManifest(a).optJSONArray("items"),
                    manifest(a).optJSONArray("items")}){
                if(list==null)continue;
                for(int i=0;i<list.length();i++){
                    JSONObject image=list.optJSONObject(i);
                    if(image==null||!image.has("taxon"))continue;
                    String file=image.optString("offlineAsset");
                    if(!file.matches("offline_photos/(species|habitat|inat)_[a-z0-9_]+\\.jpg"))continue;
                    try(InputStream in=a.getAssets().open(file)){
                        if(in.read()!=-1)names.add(image.optString("taxon")
                            .toLowerCase(java.util.Locale.ROOT));
                    }catch(Exception ignored){}
                }
            }
            availablePhotoTaxa=names;
        }
        return availablePhotoTaxa.contains(scientific.toLowerCase(java.util.Locale.ROOT));
    }
    /** iNaturalist references are rendered only if a verified JPEG was bundled. */
    /** Small, downsampled thumbnail for offline catalog rows. Attribution is in species detail. */
    public static ImageView thumbnailForTaxon(Activity a,String scientific){
        JSONObject photo=findForTaxon(a,scientific);
        if(photo==null)return null;
        try(InputStream in=a.getAssets().open(photo.optString("offlineAsset"))){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=4;
            Bitmap bitmap=BitmapFactory.decodeStream(in,null,options);
            if(bitmap==null)return null;
            ImageView thumb=new ImageView(a);
            thumb.setImageBitmap(bitmap);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setContentDescription("Offline photographic identification reference of "+scientific+
                ", photographer "+photo.optString("author"));
            return thumb;
        }catch(Exception ignored){return null;}
    }
    /**
     * Compact licensed-photo preview for the priority field cards.
     * The original file, caption and full rights detail stay accessible on expansion.
     */
    /** A full-width offline plate places the organism before supporting research cards. */
    public static void addCompactForTaxon(Activity a,LinearLayout parent,String scientific){
        JSONObject photo=findForTaxon(a,scientific);
        if(photo==null)return;
        ImageView plate=null;
        String asset=photo.optString("offlineAsset");
        try(InputStream in=a.getAssets().open(asset)){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=2;
            Bitmap bitmap=BitmapFactory.decodeStream(in,null,options);
            if(bitmap!=null){
                plate=new ImageView(a);
                plate.setImageBitmap(bitmap);
            }
        }catch(Exception ignored){}
        if(plate==null)return;
        LinearLayout wrapper=new LinearLayout(a);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackground(UiKit.surface(a));
        wrapper.setPadding(dp(a,10),dp(a,10),dp(a,10),dp(a,12));
        plate.setScaleType(ImageView.ScaleType.FIT_CENTER);
        plate.setBackgroundColor(UiKit.BG);
        plate.setContentDescription(photo.optString("caption",
            "Offline licensed photo of "+scientific));
        wrapper.addView(plate,new LinearLayout.LayoutParams(-1,dp(a,192)));

        String caption=photo.optString("caption").trim();
        if(!caption.isEmpty()){
            TextView captionView=new TextView(a);
            captionView.setText(caption);
            captionView.setTextColor(UiKit.INK);
            captionView.setTextSize(13);
            captionView.setPadding(dp(a,4),dp(a,7),dp(a,4),0);
            wrapper.addView(captionView);
        }
        String creditName=photo.optString("author").trim();
        TextView credit=new TextView(a);
        credit.setText("Photo: "+(creditName.isEmpty()?"Photographer not recorded":creditName)+
            " · "+photo.optString("license"));
        credit.setTextColor(UiKit.MUTED);
        credit.setTextSize(12);
        credit.setPadding(dp(a,4),dp(a,5),dp(a,4),dp(a,4));
        wrapper.addView(credit);

        Button details=UiKit.button(a,"Photo credits & original  ▾","quiet");
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(a,48));
        ep.topMargin=dp(a,5);
        wrapper.addView(details,ep);
        LinearLayout full=new LinearLayout(a);
        full.setOrientation(LinearLayout.VERTICAL);
        full.setVisibility(View.GONE);
        wrapper.addView(full);
        final boolean[] rendered={false};
        details.setOnClickListener(v->{
            boolean show=full.getVisibility()!=View.VISIBLE;
            if(show&&!rendered[0]){
                add(a,full,photo.optString("id"));
                rendered[0]=true;
            }
            if(show)UiMotion.reveal(a,full);
            else UiMotion.hide(full);
            details.setText(show?"Hide photo credits  ▴":"Photo credits & original  ▾");
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(a,6);
        lp.bottomMargin=dp(a,12);
        parent.addView(wrapper,lp);
    }
    public static void addForTaxon(Activity a,LinearLayout parent,String scientific){
        JSONArray items=inatManifest(a).optJSONArray("items");
        if(items==null||scientific==null)return;
        java.util.ArrayList<String> ids=new java.util.ArrayList<>();
        for(int i=0;i<items.length();i++){
            JSONObject photo=items.optJSONObject(i);
            if(photo==null||!scientific.equalsIgnoreCase(photo.optString("taxon")))continue;
            String path=photo.optString("offlineAsset");
            if(!path.matches("offline_photos/inat_[a-z0-9_]+\\.jpg"))continue;
            try(InputStream in=a.getAssets().open(path)){
                if(in.read()!=-1)ids.add(photo.optString("id"));
            }catch(Exception ignored){}
            if(ids.size()==3)break;
        }
        if(ids.isEmpty())return;
        add(a,parent,ids.get(0));
        if(ids.size()==1)return;
        Button expand=UiKit.button(a,"View "+(ids.size()-1)+" additional research photos  ▾","quiet");
        parent.addView(expand,new LinearLayout.LayoutParams(-1,dp(a,48)));
        LinearLayout more=new LinearLayout(a);
        more.setOrientation(LinearLayout.VERTICAL);
        more.setVisibility(View.GONE);
        parent.addView(more);
        final boolean[] rendered={false};
        expand.setOnClickListener(v->{
            boolean visible=more.getVisibility()!=View.VISIBLE;
            if(visible&&!rendered[0]){
                for(int n=1;n<ids.size();n++)add(a,more,ids.get(n));
                rendered[0]=true;
            }
            more.setVisibility(visible?View.VISIBLE:View.GONE);
            expand.setText(visible?"Hide extra research photos  ▴":
                "View "+(ids.size()-1)+" additional research photos  ▾");
        });
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
        boolean ownerGrant="owner_direct_private_research_permission".equals(
            found.optString("rightsBasis"));
        attribution.setText("Photographer: "+found.optString("author")+
            "\nRights status: "+found.optString("license")+
            (ownerGrant?"\nDirect copyright-holder grant for LIKAS private, noncommercial research.":"")+
            "\nOriginal image: "+found.optString("sourceUrl")+
            "\nRights information: "+found.optString("licenseUrl"));
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
