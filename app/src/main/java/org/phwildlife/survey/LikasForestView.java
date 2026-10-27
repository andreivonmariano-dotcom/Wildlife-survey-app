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
        night=LikasUiRules.isNight(hour);
        if(width>0&&height>0){
            sky=new LinearGradient(0,0,0,height,
                night?rgb("#102B34"):rgb("#416F61"),
                night?rgb("#466759"):rgb("#A5B993"),Shader.TileMode.CLAMP);
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
    /**
     * Uneven, overlapping native tree-crown lobes. Avoid the identical oval
     * silhouettes that made the previous tree layers appear artificial.
     */
    private void crown(Canvas c,float x,float y,float rw,float rh,int color){
        fill(color);shape.reset();
        final float left=(x-rw)*width,top=(y-rh)*height;
        final float right=(x+rw)*width,bottom=(y+rh)*height;
        final float w=right-left,h=bottom-top,mid=(right+left)*.5f;
        shape.moveTo(left+w*.06f,bottom-h*.17f);
        shape.cubicTo(left-w*.10f,top+h*.69f,left+w*.10f,top+h*.44f,left+w*.19f,top+h*.48f);
        shape.cubicTo(left+w*.10f,top+h*.20f,left+w*.31f,top+h*.10f,left+w*.39f,top+h*.22f);
        shape.cubicTo(left+w*.42f,top-h*.08f,left+w*.63f,top-h*.04f,left+w*.70f,top+h*.20f);
        shape.cubicTo(left+w*.86f,top+h*.05f,right+w*.05f,top+h*.27f,right-w*.06f,top+h*.48f);
        shape.cubicTo(right+w*.14f,top+h*.66f,right-w*.02f,bottom+h*.04f,mid+w*.12f,bottom-h*.03f);
        shape.cubicTo(mid,bottom+h*.02f,left+w*.30f,bottom-h*.02f,left+w*.06f,bottom-h*.17f);
        shape.close();c.drawPath(shape,paint);
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
        // Slightly bent understory palm with tapered and asymmetric fronds.
        polygon(c,trunk,x-.006f,1f,x+.005f,1f,x+bend+.008f,y,x+bend-.006f,y);
        float head=x+bend;
        for(int i=0;i<8;i++){
            float direction=i<4?-1f:1f;
            float n=i%4;
            float length=.085f-n*.011f;
            float dx=direction*length;
            float rise=.022f+n*.018f;
            shape.reset();shape.moveTo(head*width,y*height);
            shape.cubicTo((head+dx*.32f)*width,(y-rise*.6f)*height,
                (head+dx*.80f)*width,(y-rise*.95f)*height,
                (head+dx)*width,(y-rise*.3f+n*.008f)*height);
            shape.cubicTo((head+dx*.66f)*width,(y-rise*.45f+.021f)*height,
                (head+dx*.24f)*width,(y+.015f)*height,head*width,y*height);
            shape.close();fill(leaf);c.drawPath(shape,paint);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(width<=0||height<=0)return;
        float seconds=playing?(SystemClock.uptimeMillis()-firstFrame)/1000f:0f;
        paint.setShader(sky);canvas.drawRect(0,0,width,height,paint);paint.setShader(null);
        // Soft sunrise/dusk glow. No bitmap, particle allocation or video.
        fill(night?Color.argb(29,194,219,199):Color.argb(68,245,226,178));
        canvas.drawCircle(width*.76f,height*.14f,width*.14f,paint);
        int far=night?rgb("#27483F"):rgb("#678778");
        int near=night?rgb("#163B34"):rgb("#2F6754");
        int deep=night?rgb("#0C2A28"):rgb("#184536");
        int bark=night?rgb("#15362E"):rgb("#315844");
        float sway=playing?(float)Math.sin(seconds*.34f)*width*.004f:0f;
        canvas.save();
        canvas.translate(sway,0);
        emergent(canvas,.04f,.31f,.061f,.10f,bark,far);
        emergent(canvas,.34f,.29f,.082f,.112f,bark,far);
        emergent(canvas,.69f,.33f,.073f,.11f,bark,far);
        emergent(canvas,.96f,.28f,.091f,.14f,bark,far);
        canvas.restore();
        // Thin drifting forest mist, painted behind the foreground understory.
        float drift=playing?(float)Math.sin(seconds*.17f)*width*.045f:0f;
        fill(Color.argb(night?12:22,219,237,218));
        canvas.drawOval(-width*.18f+drift,height*.63f,width*.72f+drift,height*.78f,paint);
        canvas.drawOval(width*.4f-drift,height*.57f,width*1.18f-drift,height*.72f,paint);
        emergent(canvas,-.06f,.54f,.063f,.18f,bark,near);
        emergent(canvas,.81f,.51f,.076f,.17f,bark,near);
        emergent(canvas,1.10f,.55f,.067f,.15f,bark,near);
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
