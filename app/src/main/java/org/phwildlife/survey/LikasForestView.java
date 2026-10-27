package org.phwildlife.survey;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import java.util.Calendar;

/**
 * Offline, deliberately low-resolution pixel-art Philippine rainforest.
 * Fixed 216x384 scene, nearest-neighbor upscale, 10 fps maximum.
 * No network, decoded animation frames, GPS, storage, or animation workers.
 */
public final class LikasForestView extends View {
    static final int PIXELS_W=216, PIXELS_H=384, FRAME_DELAY_MS=100;
    private final Paint ink=new Paint();
    private final Bitmap scene=Bitmap.createBitmap(PIXELS_W,PIXELS_H,Bitmap.Config.ARGB_8888);
    private final Canvas pixels=new Canvas(scene);
    private final Rect destination=new Rect();
    private long animationStartedMs=0;
    private boolean playing=false,started=false;
    private float light=1f;
    private long lastClockCheck=0;

    public LikasForestView(Context context){
        super(context);
        ink.setAntiAlias(false);
        ink.setFilterBitmap(false);
        setContentDescription("Animated pixel-art Philippine tropical forest; forest changes between day and night.");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    static int mix(int a,int b,float t){
        t=Math.max(0f,Math.min(1f,t));
        return Color.rgb(
            Math.round(Color.red(a)+(Color.red(b)-Color.red(a))*t),
            Math.round(Color.green(a)+(Color.green(b)-Color.green(a))*t),
            Math.round(Color.blue(a)+(Color.blue(b)-Color.blue(a))*t));
    }
    private static final java.util.Map<String,Integer> COLORS=new java.util.HashMap<>();
    private static int color(String value){
        Integer cached=COLORS.get(value);
        if(cached!=null)return cached;
        int parsed=Color.parseColor(value);
        COLORS.put(value,parsed);
        return parsed;
    }
    private void rect(int x,int y,int w,int h,int shade){
        if(w<=0||h<=0)return;
        ink.setColor(shade);
        pixels.drawRect(x,y,x+w,y+h,ink);
    }
    private int between(String night,String day){
        return mix(color(night),color(day),light);
    }
    private void sky(float second){
        int upper=between("#101C34","#78B5AC");
        int lower=between("#435A70","#D4DBAD");
        for(int y=0;y<PIXELS_H;y+=8)
            rect(0,y,PIXELS_W,8,mix(upper,lower,y/(float)PIXELS_H));
        int orbX=162,orbY=62;
        rect(orbX-10,orbY-10,20,20,between("#9EB9D2","#FBE8A4"));
        rect(orbX-7,orbY-7,14,14,between("#DCE8D4","#FFF4C7"));
        if(light<.45f){
            for(int i=0;i<19;i++){
                int x=(i*61+13)%PIXELS_W,y=10+(i*37)%125;
                int star=Color.argb(Math.round((.45f-light)*260f),230,244,213);
                rect(x,y,i%4==0?2:1,1,star);
            }
        }
        int cloud=between("#6E8593","#E7E8D0");
        for(int n=0;n<3;n++){
            int x=((n*99+55+(int)(second*(n%2==0?1.6f:-1.1f)))%310+310)%310-48;
            int y=31+n*21;
            for(int shift=-310;shift<=310;shift+=310){
                rect(x+shift,y+7,54,6,cloud);
                rect(x+shift+7,y+3,31,12,cloud);
                rect(x+shift+17,y,18,10,cloud);
            }
        }
    }
    private void mountains(float seconds){
        int distant=between("#253B53","#88A99B");
        int ridge=between("#193648","#648E78");
        int dx=(int)(seconds*.55f)%PIXELS_W;
        for(int x=-PIXELS_W;x<PIXELS_W*2;x+=4){
            int cx=x+dx;
            int peak=142+Math.abs(((cx/4)*13)%51-26)/2;
            rect(x,peak,4,130,distant);
        }
        for(int x=0;x<PIXELS_W;x+=5){
            int peak=173+Math.abs((x*7+30)%79-40)/3;
            rect(x,peak,5,112,ridge);
        }
    }
    private void tree(int x,int crownTop,int size,int bark,int foliage,int highlight,float sway){
        // Rectangular, uneven crowns and buttressed trunks in coarse pixel clusters.
        int shift=Math.round(sway);
        int trunkW=Math.max(4,size/9);
        rect(x-trunkW/2,crownTop+size/4,trunkW,PIXELS_H-crownTop,bark);
        rect(x-trunkW-4,290,5,35,bark);
        rect(x+trunkW-1,303,6,28,bark);
        int leafW=size;
        rect(x-leafW/2+shift,crownTop+11,leafW,28,foliage);
        rect(x-leafW/2+7+shift,crownTop+3,leafW-16,35,foliage);
        rect(x-leafW/2+16+shift,crownTop-4,leafW-30,18,foliage);
        rect(x-leafW/2-10+shift,crownTop+23,24,23,foliage);
        rect(x+leafW/2-10+shift,crownTop+15,19,25,foliage);
        rect(x-leafW/2+12+shift,crownTop+7,18,8,highlight);
        rect(x+leafW/5+shift,crownTop+16,12,6,highlight);
    }
    private void layer(float sec,boolean rear){
        int foliage=rear?between("#254951","#5D9474"):between("#16453F","#2B6C4B");
        int leaves=rear?between("#3B6661","#87AD7D"):between("#255B43","#488B5B");
        int wood=rear?between("#1A313B","#416750"):between("#102E2D","#315B43");
        float speed=rear?1.1f:2.4f;
        int travel=((int)(sec*speed))%270;
        for(int k=-1;k<3;k++){
            int base=k*270-travel;
            if(rear){
                tree(base+47,130,69,wood,foliage,leaves,(float)Math.sin(sec*.63f+k)*2);
                tree(base+189,116,83,wood,foliage,leaves,(float)Math.sin(sec*.48f+k+1)*3);
            }else{
                tree(base+19,192,74,wood,foliage,leaves,(float)Math.sin(sec*.68f+k)*3);
                tree(base+177,205,88,wood,foliage,leaves,(float)Math.sin(sec*.53f+k+2)*3);
            }
        }
    }
    private void waterfall(float sec){
        int stone=between("#234044","#5A7C65");
        int foam=between("#8AAEB1","#E0F6DC");
        int water=between("#376E7D","#70C8BA");
        rect(121,206,60,126,stone);
        rect(128,206,45,118,between("#2B4B55","#54826B"));
        rect(143,208,22,117,water);
        for(int j=0;j<15;j++){
            int y=209+((j*11+(int)(sec*20))%112);
            rect(145+j%3*5,y,3,9,foam);
        }
        rect(136,316,38,6,foam);
        rect(122,323,60,4,water);
        for(int i=0;i<13;i++){
            int x=99+i*8,y=328+(i%3)*3;
            rect(x,y,6,2,foam);
        }
    }
    private void foreground(float sec){
        int ground=between("#0D282D","#244B38");
        int green=between("#1B4438","#397748");
        int greenLit=between("#34654A","#5F9655");
        rect(0,339,PIXELS_W,45,ground);
        for(int k=0;k<30;k++){
            int x=(k*37+17)%PIXELS_W;
            int y=327+(k*11)%32;
            int bend=(int)Math.sin(sec*.8f+k)*2;
            rect(x+bend,y,4,22,green);
            rect(x-5+bend,y+4,10,5,greenLit);
            rect(x+3+bend,y-4,7,7,green);
        }
        // Bottom shade supports readable launch buttons without a raster overlay.
        rect(0,364,PIXELS_W,20,between("#0B222B","#1C3F31"));
    }
    private void fireflies(float sec){
        if(light>.72f)return;
        int alpha=Math.round((.72f-light)/.72f*235f);
        for(int n=0;n<17;n++){
            float pulse=(float)Math.sin(sec*1.7f+n*2.1f);
            int x=(n*41+29+(int)(sec*(n%2==0?1f:-1f)))%PIXELS_W;
            if(x<0)x+=PIXELS_W;
            int y=170+(n*43)%167+(int)Math.sin(sec*.7f+n)*3;
            rect(x,y,n%3==0?3:2,2,Color.argb(Math.max(0,(int)(alpha*(.58f+.42f*pulse))),227,247,154));
        }
    }
    private void render(float sec){
        sky(sec);
        mountains(sec);
        layer(sec,true);
        waterfall(sec);
        layer(sec,false);
        foreground(sec);
        fireflies(sec);
    }
    @Override protected void onDraw(Canvas target){
        super.onDraw(target);
        if(getWidth()<=0||getHeight()<=0)return;
        long now=SystemClock.uptimeMillis();
        if(!started){animationStartedMs=now;started=true;updateDaylight(true);}
        else if(now-lastClockCheck>=5000L)updateDaylight(false);
        render(playing?(now-animationStartedMs)*.001f:0f);
        destination.set(0,0,getWidth(),getHeight());
        ink.setFilterBitmap(false);
        ink.setAlpha(255);
        target.drawBitmap(scene,null,destination,ink);
        if(playing&&ValueAnimator.areAnimatorsEnabled())
            postInvalidateDelayed(FRAME_DELAY_MS);
    }
    private void updateDaylight(boolean initial){
        Calendar clock=Calendar.getInstance();
        int minute=clock.get(Calendar.HOUR_OF_DAY)*60+clock.get(Calendar.MINUTE);
        float desired=LikasUiRules.daylightFactor(minute);
        if(initial)light=desired;
        else light+=Math.max(-.025f,Math.min(.025f,desired-light));
        lastClockCheck=SystemClock.uptimeMillis();
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        playing=getVisibility()==VISIBLE&&getWindowVisibility()==VISIBLE;
        animationStartedMs=SystemClock.uptimeMillis();
        started=false;invalidate();
    }
    @Override protected void onWindowVisibilityChanged(int visibility){
        super.onWindowVisibilityChanged(visibility);
        playing=visibility==VISIBLE&&getVisibility()==VISIBLE;
        if(playing){animationStartedMs=SystemClock.uptimeMillis();invalidate();}
    }
    @Override protected void onDetachedFromWindow(){
        playing=false;super.onDetachedFromWindow();
    }
}
