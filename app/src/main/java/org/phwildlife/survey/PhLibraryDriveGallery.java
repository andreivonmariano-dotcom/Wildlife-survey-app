package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Offline, multi-image photo plates sourced from the owner-directed Drive
 * collection. No Drive access is required by the installed APK. Taxon labels
 * are owner-provided and photo-level identification / rights need review.
 */
public final class PhLibraryDriveGallery {
    private PhLibraryDriveGallery(){}
    private static Map<String,List<JSONObject>> byTaxon;
    private static final String MANIFEST="offline_drive_gallery_manifest_v1.json";
    private static int dp(Activity a,float value){return PhLibrary2Ui.dp(a,value);}
    private static String key(String value){
        return value==null?"":value.trim().toLowerCase(Locale.ROOT);
    }
    private static synchronized void initialize(Activity a){
        if(byTaxon!=null)return;
        Map<String,List<JSONObject>> result=new HashMap<>();
        try(InputStream input=a.getAssets().open(MANIFEST);
                ByteArrayOutputStream stream=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int bytes;
            while((bytes=input.read(buffer))>=0){
                if(stream.size()+bytes>300000)
                    throw new IllegalArgumentException("Photo manifest exceeds bound");
                stream.write(buffer,0,bytes);
            }
            JSONArray records=new JSONObject(stream.toString("UTF-8")).getJSONArray("items");
            for(int n=0;n<records.length();n++){
                JSONObject photo=records.optJSONObject(n);
                if(photo==null)continue;
                String taxon=key(photo.optString("taxon"));
                String asset=photo.optString("offlineAsset");
                if(taxon.isEmpty()||!asset.matches("offline_photos/drive_[0-9]{4}\\.jpg"))
                    continue;
                result.computeIfAbsent(taxon,k->new ArrayList<>()).add(photo);
            }
        }catch(Exception ignored){
            // Existing verified Commons photos continue to work without this pack.
        }
        for(List<JSONObject> images:result.values()){
            images.sort((a1,a2)->a1.optString("fileName")
                .compareToIgnoreCase(a2.optString("fileName")));
        }
        byTaxon=result;
    }
    public static boolean hasForTaxon(Activity a,String scientific){
        initialize(a);
        List<JSONObject> items=byTaxon.get(key(scientific));
        return items!=null&&!items.isEmpty();
    }
    public static int photoCount(Activity a,String scientific){
        initialize(a);
        List<JSONObject> items=byTaxon.get(key(scientific));
        return items==null?0:items.size();
    }
    private static List<JSONObject> photos(Activity a,String scientific){
        initialize(a);
        List<JSONObject> items=byTaxon.get(key(scientific));
        return items==null?Collections.emptyList():items;
    }
    /** Keep adjacent full-resolution frames decoded for smooth gallery swipes. */
    private static final android.util.LruCache<String,Bitmap> frameCache=
        new android.util.LruCache<String,Bitmap>(24*1024*1024){
            @Override protected int sizeOf(String key,Bitmap bitmap){
                return bitmap.getByteCount();
            }
        };
    private static final java.util.concurrent.ExecutorService prefetchIo=
        java.util.concurrent.Executors.newSingleThreadExecutor(r->{
            Thread thread=new Thread(r,"apo-gallery-prefetch");
            thread.setDaemon(true);
            return thread;
        });
    private static Bitmap decode(Activity a,JSONObject item){return decode(a,item,2);}
    private static Bitmap decode(Activity a,JSONObject item,int sample){
        if(item==null)return null;
        String asset=item.optString("offlineAsset");
        if(!asset.matches("offline_photos/drive_[0-9]{4}\\.jpg"))return null;
        String cacheKey=asset+":sample"+sample;
        synchronized(frameCache){
            Bitmap cached=frameCache.get(cacheKey);
            if(cached!=null&&!cached.isRecycled())return cached;
        }
        try(InputStream stream=a.getAssets().open(asset)){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;
            options.inSampleSize=sample; // 960px for reader and covers; 1920px on enlarge
            Bitmap bitmap=BitmapFactory.decodeStream(stream,null,options);
            if(bitmap!=null){
                synchronized(frameCache){frameCache.put(cacheKey,bitmap);}
            }
            return bitmap;
        }catch(Exception ignored){return null;}
    }
    private static void prefetchAdjacent(Activity a,List<JSONObject> items,int index){
        if(items.size()<2)return;
        final JSONObject next=items.get((index+1)%items.size());
        final JSONObject previous=items.get((index-1+items.size())%items.size());
        prefetchIo.execute(()->{
            decode(a,next,2);
            if(previous!=next)decode(a,previous,2);
        });
    }
    public static ImageView coverForTaxon(Activity a,String scientific){
        List<JSONObject> items=photos(a,scientific);
        if(items.isEmpty())return null;
        Bitmap bitmap=decode(a,items.get(0));
        if(bitmap==null)return null;
        ImageView cover=new ImageView(a);
        cover.setImageBitmap(bitmap);
        cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cover.setBackgroundColor(PhLibrary2Ui.DEEP);
        cover.setContentDescription("Owner-supplied offline photograph of "+scientific+
            ". Identification and photographer rights await independent review.");
        return cover;
    }
    private static void openFullScreen(Activity a,String scientific,JSONObject photo){
        Bitmap full=decode(a,photo,1);
        if(full==null)return;
        LinearLayout panel=new LinearLayout(a);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(a,8),dp(a,12),dp(a,8),dp(a,6));
        panel.setBackgroundColor(PhLibrary2Ui.DEEP);
        ImageView view=new ImageView(a);
        view.setImageBitmap(full);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setContentDescription("Enlarged photograph of "+scientific);
        int height=Math.min(dp(a,610),(int)(a.getResources()
            .getDisplayMetrics().heightPixels*0.70f));
        panel.addView(view,new LinearLayout.LayoutParams(-1,height));
        TextView caption=PhLibrary2Ui.text(a,
            photo.optString("fileName")+"  ·  Owner collection",12,Color.WHITE,false,false);
        caption.setPadding(dp(a,8),dp(a,9),dp(a,8),dp(a,7));
        panel.addView(caption);
        new AlertDialog.Builder(a).setTitle(scientific).setView(panel)
            .setPositiveButton("Close",null).show();
    }
    public static void addReaderGallery(Activity a,LinearLayout parent,String scientific){
        List<JSONObject> items=photos(a,scientific);
        if(items.isEmpty())return;
        final int[] position={0};
        LinearLayout container=new LinearLayout(a);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0,dp(a,12),0,dp(a,4));
        LinearLayout heading=new LinearLayout(a);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=PhLibrary2Ui.text(a,"PHOTOGRAPHS",12,PhLibrary2Ui.FOREST,false,true);
        title.setLetterSpacing(.08f);
        heading.addView(title,new LinearLayout.LayoutParams(0,-2,1f));
        TextView counter=PhLibrary2Ui.text(a,"",13,PhLibrary2Ui.FOREST,false,true);
        heading.addView(counter);
        container.addView(heading);
        FrameLayout frame=new FrameLayout(a);
        frame.setBackground(PhLibrary2Ui.bg(a,PhLibrary2Ui.DEEP,Color.TRANSPARENT,14));
        ImageView viewer=new ImageView(a);
        viewer.setScaleType(ImageView.ScaleType.FIT_CENTER);
        viewer.setBackgroundColor(PhLibrary2Ui.DEEP);
        frame.addView(viewer,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams photoLp=new LinearLayout.LayoutParams(-1,dp(a,268));
        photoLp.topMargin=dp(a,9);
        container.addView(frame,photoLp);
        TextView hint=PhLibrary2Ui.text(a,
            items.size()>1?"Swipe photos · Tap to enlarge":"Tap photograph to enlarge",
            12,PhLibrary2Ui.MUTED,false,false);
        hint.setPadding(dp(a,2),dp(a,6),0,dp(a,3));
        container.addView(hint);
        final Runnable[] refresh=new Runnable[1];
        refresh[0]=()->{
            JSONObject photo=items.get(position[0]);
            viewer.setImageBitmap(decode(a,photo));
            counter.setText((position[0]+1)+" / "+items.size());
            prefetchAdjacent(a,items,position[0]);
            viewer.setContentDescription("Photo "+(position[0]+1)+" of "+items.size()+
                " for "+scientific+". Swipe horizontally to change. Tap to enlarge.");
        };
        final float[] start={0f,0f};
        viewer.setOnTouchListener((v,event)->{
            int action=event.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN){
                start[0]=event.getX();start[1]=event.getY();
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if(action==MotionEvent.ACTION_MOVE){
                float dx=event.getX()-start[0],dy=event.getY()-start[1];
                if(Math.abs(dy)>dp(a,18)&&Math.abs(dy)>Math.abs(dx)*1.2f)
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            if(action==MotionEvent.ACTION_UP){
                float dx=event.getX()-start[0],dy=event.getY()-start[1];
                if(Math.abs(dx)>dp(a,40)&&Math.abs(dx)>Math.abs(dy)*1.2f){
                    int target=position[0]+(dx<0?1:-1);
                    position[0]=Math.max(0,Math.min(items.size()-1,target));
                    refresh[0].run();
                }else if(Math.abs(dx)<dp(a,16)&&Math.abs(dy)<dp(a,16)){
                    openFullScreen(a,scientific,items.get(position[0]));
                }
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            if(action==MotionEvent.ACTION_CANCEL){
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return true;
        });
        if(items.size()>1){
            LinearLayout controls=new LinearLayout(a);
            controls.setGravity(Gravity.CENTER_VERTICAL);
            TextView previous=PhLibrary2Ui.action(a,"Previous",()->{
                position[0]=(position[0]-1+items.size())%items.size();
                refresh[0].run();
            },false);
            TextView next=PhLibrary2Ui.action(a,"Next",()->{
                position[0]=(position[0]+1)%items.size();
                refresh[0].run();
            },false);
            controls.addView(previous,new LinearLayout.LayoutParams(0,dp(a,48),1));
            LinearLayout.LayoutParams nextLp=new LinearLayout.LayoutParams(0,dp(a,48),1);
            nextLp.leftMargin=dp(a,8);
            controls.addView(next,nextLp);
            container.addView(controls);
        }
        TextView credits=PhLibrary2Ui.text(a,
            "Owner photo collection · Photographer / rights pending verification",
            12,PhLibrary2Ui.MUTED,false,false);
        credits.setPadding(dp(a,2),dp(a,6),dp(a,2),dp(a,5));
        credits.setOnClickListener(v->{
            JSONObject photo=items.get(position[0]);
            new AlertDialog.Builder(a).setTitle("Photograph provenance")
                .setMessage("Source filename: "+photo.optString("fileName")+
                    "\nSpecies label: "+photo.optString("originalTaxon",scientific)+
                    "\nPhoto attribution: Not recorded in the supplied filename."+
                    "\nRedistribution permission: Not independently verified."+
                    "\nIdentity: Provided by the project owner; not visually reviewed.")
                .setPositiveButton("Close",null).show();
        });
        credits.setClickable(true);credits.setFocusable(true);
        credits.setContentDescription("Photograph source and rights details");
        container.addView(credits);
        refresh[0].run();
        parent.addView(container,new LinearLayout.LayoutParams(-1,-2));
    }
}
