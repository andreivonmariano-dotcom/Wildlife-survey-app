package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;
import java.util.Calendar;

/**
 * Small, entirely offline forest scene. Inspired by Philippine lowland
 * dipterocarp forests: layered emergent crowns, buttressed trunks and palms.
 * No video, network, bitmap decode or animation worker threads.
 */
public final class LikasForestView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shape=new Path();
    private LinearGradient sky;
    private long firstFrame;
    private boolean playing=false,night=false;
    private int width,height;

    public LikasForestView(Context context){super(context);
        setContentDescription("Illustrated Philippine lowland rainforest background");}
    private int rgb(String hex){return Color.parseColor(hex);}
    private void palette(){
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        night=hour<6||hour>=18;
        if(width>0&&height>0){
            sky=new LinearGradient(0,0,0,height,
                night?rgb("#112B35"):rgb("#416F65"),
                night?rgb("#30544F"):rgb("#96AA7C"),Shader.TileMode.CLAMP);
        }
    }
    @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){
        super.onSizeChanged(w,h,oldW,oldH);width=w;height=h;palette();
    }
    private void fill(int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setShader(null);}
    private void polygon(Canvas c,int color,float... coords){
        shape.reset();shape.moveTo(coords[0]*width,coords[1]*height);
        for(int i=2;i<coords.length;i+=2)shape.lineTo(coords[i]*width,coords[i+1]*height);
        shape.close();fill(color);c.drawPath(shape,paint);
    }
    private void crown(Canvas c,float x,float y,float rw,float rh,int color){
        fill(color);
        c.drawOval((x-rw)*width,(y-rh)*height,(x+rw)*width,(y+rh)*height,paint);
        c.drawOval((x-rw*.72f)*width,(y-rh*1.45f)*height,
            (x+rw*.7f)*width,(y+rh*.85f)*height,paint);
    }
    private void emergent(Canvas c,float x,float top,float heightFrac,float canopy,int trunk,int leaf){
        float foot=.98f,half=.010f;
        polygon(c,trunk,x-half,top+.08f,x+half,top+.08f,
            x+half*.9f,foot-.12f,x+.055f,foot,
            x+.015f,foot,x,foot-.075f,x-.016f,foot,
            x-.06f,foot,x-half*.9f,foot-.12f);
        crown(c,x,top,canopy,heightFrac,leaf);
        crown(c,x-canopy*.47f,top+.028f,canopy*.66f,heightFrac*.85f,leaf);
        crown(c,x+canopy*.50f,top+.016f,canopy*.65f,heightFrac*.83f,leaf);
    }
    private void palm(Canvas c,float x,float y,int trunk,int leaf,float bend){
        polygon(c,trunk,x-.005f,1f,x+.006f,1f,x+bend+.008f,y,x+bend-.008f,y);
        float head=x+bend;
        for(int i=0;i<7;i++){
            double angle=Math.PI*(.11+i*.13);
            float dx=(float)Math.cos(angle)*.090f;
            float dy=(float)Math.sin(angle)*.071f;
            polygon(c,leaf,head,y,head+dx*.42f,y-dy*.78f,
                head+dx,y-dy*.38f,head+dx*.7f,y+dy*.2f);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(width<=0||height<=0)return;
        float seconds=playing?(SystemClock.uptimeMillis()-firstFrame)/1000f:0f;
        paint.setShader(sky);canvas.drawRect(0,0,width,height,paint);paint.setShader(null);
        // The glow is intentionally restrained so white title text stays legible.
        fill(night?Color.argb(36,202,221,189):Color.argb(61,224,216,153));
        canvas.drawCircle(width*.74f,height*.18f,width*.11f,paint);
        int far=night?rgb("#27483F"):rgb("#678778");
        int near=night?rgb("#163B34"):rgb("#2F6754");
        int deep=night?rgb("#0C2A28"):rgb("#184536");
        int bark=night?rgb("#15362E"):rgb("#315844");
        float sway=playing?(float)Math.sin(seconds*.34f)*width*.004f:0f;
        canvas.save();
        canvas.translate(sway,0);
        emergent(canvas,.09f,.35f,.055f,.065f,bark,far);
        emergent(canvas,.35f,.31f,.058f,.078f,bark,far);
        emergent(canvas,.69f,.34f,.045f,.078f,bark,far);
        emergent(canvas,.95f,.27f,.066f,.090f,bark,far);
        canvas.restore();
        // Thin drifting forest mist, painted behind the foreground understory.
        float drift=playing?(float)Math.sin(seconds*.17f)*width*.045f:0f;
        fill(Color.argb(night?12:22,219,237,218));
        canvas.drawOval(-width*.18f+drift,height*.63f,width*.72f+drift,height*.78f,paint);
        canvas.drawOval(width*.4f-drift,height*.57f,width*1.18f-drift,height*.72f,paint);
        emergent(canvas,-.04f,.53f,.045f,.13f,bark,near);
        emergent(canvas,.76f,.48f,.070f,.14f,bark,near);
        emergent(canvas,1.07f,.55f,.055f,.12f,bark,near);
        fill(deep);canvas.drawOval(-width*.3f,height*.83f,width*1.3f,height*1.22f,paint);
        palm(canvas,.12f,.70f,bark,deep,.06f);
        palm(canvas,.91f,.73f,bark,deep,-.045f);
        // Broad, dark foreground creates a natural readable button area.
        fill(Color.argb(night?85:60,8,27,22));
        canvas.drawRect(0,height*.76f,width,height,paint);
        if(playing&&ValueAnimator.areAnimatorsEnabled())postInvalidateDelayed(85L);
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        firstFrame=SystemClock.uptimeMillis();
        playing=getWindowVisibility()==VISIBLE&&getVisibility()==VISIBLE;
        palette();invalidate();
    }
    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        playing=visibility==VISIBLE;
        if(playing){firstFrame=SystemClock.uptimeMillis();palette();invalidate();}
    }
    @Override protected void onDetachedFromWindow(){
        playing=false;super.onDetachedFromWindow();
    }
}
