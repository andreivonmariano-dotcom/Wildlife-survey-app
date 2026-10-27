package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Offline day/night forest with continuously moving, subpixel camera.
 * Prefer the original-resolution files. Bundled 240px files remain a fallback.
 * Reuses two bitmaps, one matrix and paints without frame-sized allocations.
 */
public final class LikasForestView extends View {
    private static final long FRAME_INTERVAL_MS = 33L;
    private static final String DAY_HD = "likas_forest/forest_day_hd.webp";
    private static final String NIGHT_HD = "likas_forest/forest_night_hd.webp";
    private static final String DAY_FALLBACK = "likas_forest/forest_day.webp";
    private static final String NIGHT_FALLBACK = "likas_forest/forest_night.webp";
    private final Paint imagePaint = new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix imageMatrix = new Matrix();
    private final RectF effectRect = new RectF();
    private final Bitmap day,night;
    private final long epoch = SystemClock.uptimeMillis();
    private boolean motionEnabled = true;
    private float nightMix,targetNightMix;
    private long lastClockSample,lastFrame;
    private float plateScale,plateX,plateWidth;
    private float heldSeconds;

    public LikasForestView(Context context){this(context,null);}
    public LikasForestView(Context context,AttributeSet attrs){
        super(context,attrs);
        day = loadPreferred(context,DAY_HD,DAY_FALLBACK);
        night = loadPreferred(context,NIGHT_HD,NIGHT_FALLBACK);
        nightMix=targetNightMix=clockNightFraction();
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setContentDescription("Philippine tropical forest background changing with the time of day");
    }
    private static Bitmap loadPreferred(Context context,String hd,String fallback){
        Bitmap result=load(context,hd);
        return result!=null?result:load(context,fallback);
    }
    private static Bitmap load(Context context,String path){
        try(InputStream stream=context.getAssets().open(path)){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;
            options.inPreferredConfig=Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(stream,null,options);
        }catch(IOException ignored){return null;}
    }
    public void setMotionEnabled(boolean enabled){
        if(motionEnabled==enabled)return;
        if(!enabled)heldSeconds=(SystemClock.uptimeMillis()-epoch)/1000f;
        motionEnabled=enabled;
        invalidate();
    }
    static float ease(float x){
        x=Math.max(0f,Math.min(1f,x));
        return x*x*(3f-2f*x);
    }
    private static float clockNightFraction(){
        Calendar clock=Calendar.getInstance();
        float hour=clock.get(Calendar.HOUR_OF_DAY)+clock.get(Calendar.MINUTE)/60f;
        if(hour<5.25f||hour>=19.25f)return 1f;
        if(hour<6.5f)return 1f-ease((hour-5.25f)/1.25f);
        if(hour<17.75f)return 0f;
        return ease((hour-17.75f)/1.5f);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        final int w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        long now=SystemClock.uptimeMillis();
        if(lastClockSample==0||now-lastClockSample>=5000L){
            targetNightMix=clockNightFraction();
            lastClockSample=now;
        }
        float delta=lastFrame==0?0f:Math.min(100f,now-lastFrame);
        lastFrame=now;
        nightMix+=(targetNightMix-nightMix)*Math.min(1f,delta/2000f);
        float seconds=motionEnabled?(now-epoch)/1000f:heldSeconds;

        canvas.drawColor(Color.rgb(8,39,30));
        Bitmap first=day!=null?day:night;
        if(first!=null){
            float progress=ForestMotionPolicy.pan(seconds);
            drawPlate(canvas,first,255,progress);
            if(day!=null&&night!=null)
                drawPlate(canvas,night,Math.round(255f*nightMix),progress);
            if(motionEnabled&&ValueAnimator.areAnimatorsEnabled())
                drawEffects(canvas,seconds,w,h);
        }
        if(motionEnabled&&isShown()&&ValueAnimator.areAnimatorsEnabled())
            postInvalidateDelayed(FRAME_INTERVAL_MS);
    }
    private void drawPlate(Canvas canvas,Bitmap bitmap,int alpha,float progress){
        float scale=Math.max(getWidth()/(float)bitmap.getWidth(),
                getHeight()/(float)bitmap.getHeight());
        float scaledWidth=bitmap.getWidth()*scale;
        float maxOffset=Math.max(0f,scaledWidth-getWidth());
        float x=-maxOffset*progress;
        imageMatrix.reset();
        imageMatrix.setScale(scale,scale);
        imageMatrix.postTranslate(x,(getHeight()-bitmap.getHeight()*scale)*.5f);
        imagePaint.setAlpha(alpha);
        imagePaint.setFilterBitmap(true);
        canvas.save();
        canvas.clipRect(0,0,getWidth(),getHeight());
        canvas.drawBitmap(bitmap,imageMatrix,imagePaint);
        canvas.restore();
        plateScale=scale;
        plateX=x;
        plateWidth=scaledWidth;
    }
    private float screenX(float normalized){
        return plateX+normalized*plateWidth;
    }
    private void drawEffects(Canvas c,float t,int w,int h){
        for(int i=0;i<4;i++){
            float x=i%2==0?w*.02f:w*.98f;
            float y=h*(.14f+.17f*(i/2));
            float angle=(float)Math.sin(t*.65f+i)*4f;
            c.save();c.rotate(angle,x,y);
            effectPaint.setColor(Color.argb(75,22,80,45));
            effectRect.set(x-w*.012f,y-h*.018f,x+w*.012f,y+h*.018f);
            c.drawOval(effectRect,effectPaint);c.restore();
        }
        for(int i=0;i<3;i++){
            float x=(w*(.16f+i*.39f)+t*(2.2f+i))%(w*1.3f);
            float y=h*(.12f+i*.065f);
            effectPaint.setColor(Color.argb(Math.round(22f*(1f-nightMix)),
                    224,248,231));
            effectRect.set(x,y,x+w*.075f,y+Math.max(2f,h*.003f));
            c.drawRect(effectRect,effectPaint);
        }
        effectPaint.setStrokeWidth(Math.max(1.5f,w*.004f));
        for(int i=0;i<2;i++){
            float x=screenX(i==0?129f/240f:151f/240f);
            if(x>0&&x<w){
                float offset=(t*18f+i*23f)%(h*.12f);
                effectPaint.setColor(Color.argb(84,204,245,238));
                c.drawLine(x,h*.49f+offset,x,h*.505f+offset,effectPaint);
            }
        }
        for(int i=0;i<7;i++){
            float x=screenX((109f+i*8f)/240f)+(float)Math.sin(t*.7f+i)*w*.003f;
            if(x>0&&x<w){
                float y=h*(.67f+i%3*.018f);
                effectPaint.setColor(Color.argb(48+i%3*16,183,245,232));
                c.drawLine(x,y,x+w*.025f,y,effectPaint);
            }
        }
        for(int i=0;i<10;i++){
            float baseX=(i%2==0?.08f:.90f)+(i/2)*(i%2==0?.025f:-.02f);
            float x=w*baseX+(float)Math.sin(t*.3f+i*2f)*w*.018f;
            float y=h*(.19f+(i%5)*.135f)+(float)Math.cos(t*.48f+i)*h*.009f;
            float pulse=.5f+.5f*(float)Math.sin(t*1.25f+i*1.7f);
            int alpha=Math.round((20f+108f*nightMix)*pulse);
            effectPaint.setColor(Color.argb(alpha,233,250,159));
            float side=Math.max(2f,w*.006f);
            effectRect.set(x,y,x+side,y+side);
            c.drawRect(effectRect,effectPaint);
        }
    }
    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        if(visibility==VISIBLE&&motionEnabled&&ValueAnimator.areAnimatorsEnabled()){
            lastFrame=0L;
            postInvalidateDelayed(FRAME_INTERVAL_MS);
        }
    }
}
