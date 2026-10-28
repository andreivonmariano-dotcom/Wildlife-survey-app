package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Offline LIKAS scene with native, independent 5fps transparent sprites.
 * Native landing controls and the permanent launcher icon are separate.
 * Does not access GPS, database, remote services or research exports.
 * This class stays unconnected until sprite assets ship inside the APK.
 */
public final class LikasSpriteForestView extends View {
    private static final int W=432,H=756;
    private static final long FRAME_MS=200L;
    private static final String ARCHIVE="likas_sprite_assets.zip";
    private static final String[] ORDER={"cloud","river","waterfall","wildlife","rocks","canopy","fern"};
    private final Object lock=new Object();
    private final Paint painter=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final HashMap<String,Bitmap> fixed=new HashMap<>();
    private final ArrayList<Sheet> sheets=new ArrayList<>();
    private final PowerManager powerManager;
    private HandlerThread thread;
    private Handler worker;
    private final AtomicBoolean tickQueued=new AtomicBoolean(false);
    private final Runnable spriteTick=()->{tickQueued.set(false);tick();};
    private boolean ready=false,started=false,loadFailed=false;
    private volatile boolean alive=false;
    private volatile float nightBlend=0f;
    private long lastClockRead=0L;
    private static final class Sheet {
        String mode,kind;
        int x,y,w,h,cols,count,index,currentIndex=-1;
        BitmapRegionDecoder decoder;
        Bitmap current;
        void clear() {
            if(current!=null){current.recycle();current=null;}
            if(decoder!=null){decoder.recycle();decoder=null;}
        }
    }
    public LikasSpriteForestView(Context c){this(c,null);}
    public LikasSpriteForestView(Context c,AttributeSet attrs){
        super(c,attrs);
        powerManager=(PowerManager)c.getSystemService(Context.POWER_SERVICE);
        painter.setFilterBitmap(false);
        painter.setDither(false);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    private static byte[] allBytes(InputStream stream) throws Exception{
        byte[] buf=new byte[16384];
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        int n;
        while((n=stream.read(buf))!=-1)out.write(buf,0,n);
        return out.toByteArray();
    }
    private void load() throws Exception {
        HashMap<String,byte[]> entries=new HashMap<>();
        try(ZipInputStream zip=new ZipInputStream(getContext().getAssets().open(ARCHIVE))){
            ZipEntry e;
            while((e=zip.getNextEntry())!=null){
                if(!e.isDirectory())entries.put(e.getName(),allBytes(zip));
                zip.closeEntry();
            }
        }
        byte[] data=entries.get("manifest.json");
        if(data==null)throw new IllegalStateException("Missing sprite manifest");
        JSONObject manifest=new JSONObject(new String(data,"UTF-8"));
        JSONObject components=manifest.getJSONObject("components");
        ArrayList<Sheet> loaded=new ArrayList<>();
        HashMap<String,Bitmap> scenes=new HashMap<>();
        for(String mode:new String[]{"day","night"}){
            for(String kind:new String[]{"fixed","rocks"}){
                byte[] bytes=entries.get("scene/"+kind+"_"+mode+".webp");
                if(bytes==null)throw new IllegalStateException("Missing background");
                BitmapFactory.Options opt=new BitmapFactory.Options();
                opt.inPreferredConfig=kind.equals("fixed")?Bitmap.Config.RGB_565:Bitmap.Config.ARGB_8888;
                Bitmap image=BitmapFactory.decodeByteArray(bytes,0,bytes.length,opt);
                if(image==null)throw new IllegalStateException("Bad background image");
                scenes.put(kind+"_"+mode,image);
            }
        }
        java.util.Iterator<String> keys=components.keys();
        while(keys.hasNext()){
            String key=keys.next();
            JSONObject entry=components.getJSONObject(key);
            Sheet s=new Sheet();
            s.kind=entry.getString("kind");
            s.mode=entry.getString("lighting");
            s.x=entry.getJSONArray("origin").getInt(0);
            s.y=entry.getJSONArray("origin").getInt(1);
            s.w=entry.getJSONArray("size").getInt(0);
            s.h=entry.getJSONArray("size").getInt(1);
            s.count=entry.getInt("frame_count");
            s.index=entry.getInt("index");
            JSONObject atlas=entry.getJSONObject("spritesheet");
            s.cols=atlas.getInt("columns");
            byte[] bytes=entries.get(atlas.getString("file"));
            if(bytes==null)throw new IllegalStateException("Missing sprite atlas "+key);
            s.decoder=BitmapRegionDecoder.newInstance(bytes,0,bytes.length,false);
            if(s.decoder==null)throw new IllegalStateException("Bad sprite atlas "+key);
            loaded.add(s);
        }
        synchronized(lock){
            if(!alive){
                for(Sheet sheet:loaded)sheet.clear();
                for(Bitmap image:scenes.values())image.recycle();
                return;
            }
            fixed.putAll(scenes);
            sheets.addAll(loaded);
            ready=true;
        }
    }
    /** Local day 06:30-17:30; one-hour dawn and dusk blends. */
    public static float nightFraction(int localMinute){
        int minute=Math.max(0,Math.min(1439,localMinute));
        if(minute<330||minute>=1110)return 1f;
        if(minute<390)return 1f-(minute-330)/60f;
        if(minute<1050)return 0f;
        return (minute-1050)/60f;
    }
    private void sampleClock(){
        Calendar t=Calendar.getInstance();
        nightBlend=nightFraction(t.get(Calendar.HOUR_OF_DAY)*60+t.get(Calendar.MINUTE));
    }
    private boolean shouldAnimateSprites(){
        return ValueAnimator.areAnimatorsEnabled()&&
            !(powerManager!=null&&powerManager.isPowerSaveMode());
    }
    private void scheduleTick(Handler activeWorker,long delayMs){
        if(!alive||activeWorker==null)return;
        if(tickQueued.compareAndSet(false,true)){
            if(!activeWorker.postDelayed(spriteTick,delayMs))tickQueued.set(false);
        }
    }

    private void tick(){
        // Capture the handler before detachment clears the worker field.
        Handler activeWorker=worker;
        if(!alive||activeWorker==null)return;
        if(getWindowVisibility()!=VISIBLE||!isShown()){
            scheduleTick(activeWorker,1500L);
            return;
        }
        long now=SystemClock.uptimeMillis();
        if(now-lastClockRead>15000L){sampleClock();lastClockRead=now;}
        boolean motion=shouldAnimateSprites();
        long frame=motion?now/FRAME_MS:0L;
        if(!ready&&!loadFailed){
            try{load();}
            catch(Exception err){
                loadFailed=true;
                Log.e("LIKAS_SPRITES","Unable to initialize reviewed sprite archive",err);
            }
        }
        if(ready){
            for(Sheet s:sheets){
                float alpha=s.mode.equals("night")?nightBlend:1f-nightBlend;
                if(alpha<=.001f)continue;
                int desired=(int)((frame+s.index*3L)%s.count);
                if(desired==s.currentIndex)continue;
                Rect region=new Rect((desired%s.cols)*s.w,(desired/s.cols)*s.h,
                    (desired%s.cols+1)*s.w,(desired/s.cols+1)*s.h);
                Bitmap next;
                try{
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inPreferredConfig=Bitmap.Config.ARGB_8888;
                    next=s.decoder.decodeRegion(region,options);
                }catch(RuntimeException err){continue;}
                if(next!=null)synchronized(lock){
                    Bitmap old=s.current;
                    s.current=next;
                    s.currentIndex=desired;
                    if(old!=null)old.recycle();
                }
            }
        }
        if(alive){
            postInvalidate();
            scheduleTick(activeWorker,motion?FRAME_MS:15000L);
        }
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        c.drawColor(Color.rgb(8,39,30));
        synchronized(lock){
            if(!ready)return;
            float scale=Math.max(getWidth()/(float)W,getHeight()/(float)H);
            c.save();
            c.translate((getWidth()-scale*W)*.5f,(getHeight()-scale*H)*.5f);
            c.scale(scale,scale);
            if(nightBlend<.999f)drawMode(c,"day",1f);
            if(nightBlend>.001f)drawMode(c,"night",nightBlend);
            c.restore();
        }
    }
    private void drawMode(Canvas c,String mode,float opacity){
        painter.setAlpha(Math.max(0,Math.min(255,Math.round(opacity*255))));
        Bitmap background=fixed.get("fixed_"+mode);
        if(background!=null)c.drawBitmap(background,0,0,painter);
        for(String kind:ORDER){
            if(kind.equals("rocks")){
                Bitmap rock=fixed.get("rocks_"+mode);
                if(rock!=null)c.drawBitmap(rock,0,0,painter);
            }else for(Sheet s:sheets){
                if(s.mode.equals(mode)&&s.kind.equals(kind)&&s.current!=null)
                    c.drawBitmap(s.current,(float)s.x,(float)s.y,painter);
            }
        }
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(!started){
            started=true;alive=true;
            thread=new HandlerThread("LikasSpriteClock");
            thread.start();
            worker=new Handler(thread.getLooper());
            scheduleTick(worker,0L);
        }
    }
    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        if(visibility==VISIBLE&&worker!=null){
            worker.removeCallbacks(spriteTick);
            tickQueued.set(false);
            scheduleTick(worker,0L);
        }
    }
    @Override protected void onDetachedFromWindow(){
        alive=false;
        if(worker!=null)worker.removeCallbacksAndMessages(null);
        tickQueued.set(false);
        if(thread!=null)thread.quitSafely();
        synchronized(lock){
            for(Sheet s:sheets)s.clear();
            sheets.clear();
            for(Bitmap image:fixed.values())image.recycle();
            fixed.clear();
            ready=false;
        }
        worker=null;thread=null;started=false;
        loadFailed=false;
        super.onDetachedFromWindow();
    }
}
