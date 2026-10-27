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
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.PowerManager;
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
    private final Paint glowPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix glowTransform=new Matrix();
    // Reuse radial gradients and geometry on each frame instead of allocating sprites.
    private final RadialGradient sunHalo=new RadialGradient(0,0,1,
        new int[]{Color.argb(148,255,245,169),Color.argb(52,255,230,139),
            Color.TRANSPARENT},new float[]{0f,.30f,1f},Shader.TileMode.CLAMP);
    private final RadialGradient moonHalo=new RadialGradient(0,0,1,
        new int[]{Color.argb(133,198,226,255),Color.argb(54,170,207,255),
            Color.TRANSPARENT},new float[]{0f,.30f,1f},Shader.TileMode.CLAMP);
    private final PowerManager powerManager;
    private final Bitmap day,night;
    private final long epoch = SystemClock.uptimeMillis();
    private boolean motionEnabled = true;
    private float nightMix,targetNightMix;
    private long lastClockSample,lastFrame;
    private float plateScale,plateX,plateWidth;
    private float heldSeconds;
    private float deviceLocalHour=12f;

    public LikasForestView(Context context){this(context,null);}
    public LikasForestView(Context context,AttributeSet attrs){
        super(context,attrs);
        powerManager=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
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
            Calendar deviceClock=Calendar.getInstance();
            deviceLocalHour=deviceClock.get(Calendar.HOUR_OF_DAY)+
                deviceClock.get(Calendar.MINUTE)/60f;
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
            postInvalidateDelayed(powerManager!=null&&powerManager.isPowerSaveMode()?
                66L:FRAME_INTERVAL_MS);
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
    /** Ambient animation is drawn over the approved static panorama with no new bitmaps. */
    private void drawEffects(Canvas c,float t,int w,int h){
        drawSkyLights(c,t,w,h);
        drawClouds(c,t,w,h);
        drawBirds(c,t,w,h);
        drawWaterfalls(c,t,w,h);
        drawLakeRipples(c,t,w,h);
        drawSwayingLeaves(c,t,w,h);
        drawFireflies(c,t,w,h);
    }

    private void halo(Canvas c,RadialGradient shader,float x,float y,float radius,float fade){
        if(fade<=0f||x<-radius||x>getWidth()+radius)return;
        glowTransform.reset();
        glowTransform.setScale(radius,radius);
        glowTransform.postTranslate(x,y);
        shader.setLocalMatrix(glowTransform);
        glowPaint.setShader(shader);
        glowPaint.setAlpha(Math.max(0,Math.min(255,Math.round(fade*255f))));
        c.drawCircle(x,y,radius,glowPaint);
        glowPaint.setShader(null);
        glowPaint.setAlpha(255);
    }

    /** The night artwork already paints the moon: illuminate it, don't add a second moon. */
    private void drawSkyLights(Canvas c,float t,int w,int h){
        float dayStrength=Math.max(0f,1f-nightMix);
        float nightStrength=Math.max(0f,nightMix);
        float shimmer=.92f+.08f*(float)Math.sin(t*.72f);
        // Sun arcs across the daylight sky using local device time.
        float dayProgress=Math.max(0f,Math.min(1f,(deviceLocalHour-6f)/12f));
        float sunWorld=.39f+dayProgress*.18f;
        float sunX=screenX(sunWorld)+(float)Math.sin(t*.04f)*w*.002f;
        float sunY=h*(.16f-.08f*(float)Math.sin(dayProgress*Math.PI));
        halo(c,sunHalo,sunX,sunY,w*.24f,dayStrength*shimmer*.84f);
        if(dayStrength>.12f&&sunX>0&&sunX<w){
            effectPaint.setColor(Color.argb(Math.round(70f*dayStrength),
                255,249,196));
            c.drawCircle(sunX,sunY,Math.max(3f,w*.014f),effectPaint);
        }
        // Moon is embedded near 0.65 of the approved night panorama.
        float moonX=screenX(.649f)+(float)Math.sin(t*.052f)*w*.002f;
        float moonY=h*.12f+(float)Math.sin(t*.12f)*h*.001f;
        halo(c,moonHalo,moonX,moonY,w*.22f,nightStrength*shimmer);
    }

    private void drawClouds(Canvas c,float t,int w,int h){
        float strength=Math.max(.3f,1f-nightMix*.58f);
        // A few translucent cloud banks drift independently of camera movement.
        for(int i=0;i<4;i++){
            float travel=w*1.5f;
            float x=((t*(3.2f+i*.8f)+i*w*.38f)%travel)-w*.25f;
            float y=h*(.10f+i*.034f)+(float)Math.sin(t*.06f+i)*h*.006f;
            float wide=w*(.085f+.025f*(i%2));
            effectPaint.setColor(Color.argb(Math.round(43f*strength),
                232,245,242));
            effectRect.set(x-wide,y-wide*.20f,x+wide,y+wide*.20f);
            c.drawOval(effectRect,effectPaint);
            effectRect.set(x-wide*.48f,y-wide*.36f,x+wide*.34f,y+wide*.12f);
            c.drawOval(effectRect,effectPaint);
        }
    }

    private void drawBirds(Canvas c,float t,int w,int h){
        float day=Math.max(0f,1f-nightMix*1.6f);
        if(day<=.01f)return;
        effectPaint.setColor(Color.argb(Math.round(200f*day),22,50,48));
        effectPaint.setStyle(Paint.Style.STROKE);
        effectPaint.setStrokeCap(Paint.Cap.ROUND);
        effectPaint.setStrokeWidth(Math.max(1.5f,w*.0037f));
        for(int i=0;i<5;i++){
            // Two drifting flocks with independently flapping wing silhouettes.
            float offset=i<3?0f:12f;
            float x=((t*(9f+i*.6f)+offset+w*(i%3)*.043f)%(w*1.42f))-w*.20f;
            float y=h*(.14f+.055f*(i%3))+
                (float)Math.sin(t*.32f+i*2f)*h*.013f;
            float span=w*(.015f+(i%3)*.003f);
            float flap=(float)Math.sin(t*(3.1f+i*.13f)+i*1.7f)*span*.60f;
            c.drawLine(x-span,y-flap,x,y,effectPaint);
            c.drawLine(x,y,x+span,y-flap,effectPaint);
        }
        effectPaint.setStyle(Paint.Style.FILL);
    }

    private void drawWaterfalls(Canvas c,float t,int w,int h){
        // Normalized coordinates align with waterfalls in the approved panorama.
        float[] xPositions={.536f,.627f,.744f};
        float[] origins={.565f,.553f,.555f};
        effectPaint.setStrokeCap(Paint.Cap.ROUND);
        effectPaint.setStrokeWidth(Math.max(1.2f,w*.003f));
        for(int fall=0;fall<xPositions.length;fall++){
            float center=screenX(xPositions[fall]);
            if(center<-w*.08f||center>w*1.08f)continue;
            for(int stream=0;stream<5;stream++){
                float x=center+(stream-2)*w*.007f;
                float drop=(t*(36f+stream*4f)+stream*h*.043f+fall*h*.07f)
                    %(h*.15f);
                float y=h*origins[fall]+drop;
                float end=Math.min(y+h*.036f,h*.69f);
                if(end<=y)continue;
                effectPaint.setColor(Color.argb(56+(stream%3)*16,
                    202,241,248));
                c.drawLine(x,y,x+(float)Math.sin(t*1.1f+stream)*w*.002f,
                    end,effectPaint);
            }
            float spray=(float)(.5f+.5f*Math.sin(t*2.9f+fall));
            effectPaint.setColor(Color.argb(Math.round(65f*spray),
                211,248,250));
            effectRect.set(center-w*.022f,h*.683f,center+w*.022f,h*.69f);
            c.drawOval(effectRect,effectPaint);
        }
    }

    private void drawLakeRipples(Canvas c,float t,int w,int h){
        float shimmer=1f-nightMix*.35f;
        effectPaint.setStrokeWidth(Math.max(1f,w*.003f));
        effectPaint.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<21;i++){
            float source=.47f+(i%11)*.025f;
            float drift=(float)Math.sin(t*.58f+i*1.37f)*.0045f;
            float x=screenX(source+drift);
            if(x<-w*.10f||x>w*1.1f)continue;
            float y=h*(.725f+(i/11)*.06f+(i%4)*.017f);
            float length=w*(.018f+(i%4)*.014f)*
                (.82f+.18f*(float)Math.sin(t*1.35f+i));
            int alpha=Math.round((35+(i%5)*13)*shimmer);
            effectPaint.setColor(Color.argb(alpha,180,240,247));
            c.drawLine(x-length*.5f,y,x+length*.5f,y,effectPaint);
        }
        // Expanding rings at the falls' receiving pool.
        float[] splashSources={.536f,.627f,.744f};
        for(int i=0;i<splashSources.length;i++){
            float x=screenX(splashSources[i]);
            if(x<0||x>w)continue;
            float phase=(t*.35f+i*.37f)%1f;
            float radius=w*(.01f+phase*.044f);
            effectPaint.setColor(Color.argb(Math.round(50f*(1f-phase)),
                184,239,246));
            effectPaint.setStyle(Paint.Style.STROKE);
            effectPaint.setStrokeWidth(Math.max(1f,w*.0027f));
            effectRect.set(x-radius,h*.702f-radius*.18f,
                x+radius,h*.702f+radius*.18f);
            c.drawOval(effectRect,effectPaint);
        }
        effectPaint.setStyle(Paint.Style.FILL);
    }

    private void drawSwayingLeaves(Canvas c,float t,int w,int h){
        for(int i=0;i<7;i++){
            float x=i%2==0?w*.035f:w*.965f;
            float y=h*(.11f+(i/2)*.16f);
            float angle=(float)Math.sin(t*.70f+i*1.7f)*6f;
            c.save();
            c.rotate(angle,x,y);
            effectPaint.setColor(Color.argb(85,20,73,42));
            effectRect.set(x-w*.012f,y-h*.020f,
                x+w*.012f,y+h*.020f);
            c.drawOval(effectRect,effectPaint);
            c.restore();
        }
    }

    private void drawFireflies(Canvas c,float t,int w,int h){
        float night=Math.max(0f,(nightMix-.15f)/.85f);
        if(night<=0f)return;
        for(int i=0;i<18;i++){
            float x=w*(.04f+(i*37%93)*.01f)+
                (float)Math.sin(t*.43f+i*1.8f)*w*.017f;
            float y=h*(.34f+(i*13%45)*.01f)+
                (float)Math.cos(t*.62f+i)*h*.011f;
            float pulse=(float)(.5+.5*Math.sin(t*(1.1f+i*.04f)+i*1.9f));
            float alpha=night*pulse*pulse;
            effectPaint.setColor(Color.argb(Math.round(49f*alpha),228,248,130));
            c.drawCircle(x,y,Math.max(3f,w*.012f),effectPaint);
            effectPaint.setColor(Color.argb(Math.round(210f*alpha),245,255,171));
            c.drawCircle(x,y,Math.max(1.2f,w*.0031f),effectPaint);
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
