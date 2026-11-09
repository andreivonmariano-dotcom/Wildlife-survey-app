package org.phwildlife.survey;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import android.util.Base64InputStream;
import android.util.Base64;

/** Four owner-supplied Mindanao photographs, rotated only while the home hero is visible. */
public final class PhLibraryPhotoHero {
    private static final String ASSET="phlibrary_mindanao_forest_strip.webp.b64";
    private PhLibraryPhotoHero(){}
    public static View create(Activity a,String title,String subtitle){
        Bitmap sheet;
        try(InputStream input=a.getAssets().open(ASSET);
            Base64InputStream decoded=new Base64InputStream(input,Base64.DEFAULT)){
            sheet=BitmapFactory.decodeStream(decoded);
            if(sheet==null || sheet.getHeight()<4 || sheet.getWidth()<100 ||
                sheet.getHeight()%4!=0)
                throw new IllegalStateException("Invalid bundled PHLibrary photo strip");
        }catch(Exception ex){
            // Safe fallback if the optional art asset is damaged or unavailable.
            return PhLibrary2Ui.hero(a,"PHLIBRARY",title,subtitle,false);
        }
        final boolean large=a.getResources().getConfiguration().fontScale>=1.6f;
        FrameLayout frame=new FrameLayout(a);
        frame.setMinimumHeight(PhLibrary2Ui.dp(a,large?240:194));
        frame.setBackground(PhLibrary2Ui.bg(a,PhLibrary2Ui.DEEP,
            android.graphics.Color.TRANSPARENT,17));
        frame.setClipToOutline(true);
        frame.addView(new PhotoLayer(a,sheet),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout labels=new LinearLayout(a);labels.setOrientation(LinearLayout.VERTICAL);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.setPadding(PhLibrary2Ui.dp(a,20),PhLibrary2Ui.dp(a,26),
            PhLibrary2Ui.dp(a,20),PhLibrary2Ui.dp(a,23));
        TextView heading=PhLibrary2Ui.text(a,title,large?24:30,
            android.graphics.Color.WHITE,true,false);
        heading.setShadowLayer(3,0,2,0xCC000000);
        labels.addView(heading);
        TextView info=PhLibrary2Ui.text(a,subtitle,large?12:14,
            0xFFF1F5EB,false,true);
        info.setShadowLayer(2,0,1,0x99000000);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);
        ip.topMargin=PhLibrary2Ui.dp(a,12);labels.addView(info,ip);
        frame.addView(labels,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER_VERTICAL));
        return frame;
    }
    private static final class PhotoLayer extends View {
        private static final long SLOT=600000L,FADE=950L;
        private final Bitmap sheet;
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        private final Runnable tick=new Runnable(){public void run(){
            int next=slot();
            if(next!=current){previous=current;current=next;fadeStart=SystemClock.uptimeMillis();}
            invalidate(); schedule();
        }};
        private int current,previous;private long fadeStart=0;
        private final Paint overlay=new Paint(Paint.ANTI_ALIAS_FLAG);
        PhotoLayer(Activity a,Bitmap b){
            super(a);sheet=b;current=slot();previous=current;
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        private int slot(){return (int)((System.currentTimeMillis()/SLOT)%4);}
        private void schedule(){removeCallbacks(tick);postDelayed(tick,Math.max(1000L,SLOT-System.currentTimeMillis()%SLOT));}
        @Override protected void onAttachedToWindow(){super.onAttachedToWindow();schedule();}
        @Override protected void onDetachedFromWindow(){removeCallbacks(tick);super.onDetachedFromWindow();}
        private void paintImage(Canvas canvas,int number,int alpha){
            if(alpha<=0)return;
            int unit=sheet.getHeight()/4,width=sheet.getWidth();
            // Crop to the view ratio without distorting or repeating the original photograph.
            float viewAspect=getWidth()/(float)Math.max(1,getHeight());
            float sourceAspect=width/(float)Math.max(1,unit);
            Rect src;
            if(sourceAspect>viewAspect){
                int cropped=Math.max(1,Math.round(unit*viewAspect));
                int x=(width-cropped)/2;src=new Rect(x,number*unit,x+cropped,(number+1)*unit);
            }else{
                int cropped=Math.max(1,Math.round(width/viewAspect));
                int y=(unit-cropped)/2;src=new Rect(0,number*unit+y,width,number*unit+y+cropped);
            }
            p.setAlpha(alpha);
            canvas.drawBitmap(sheet,src,new Rect(0,0,getWidth(),getHeight()),p);
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            if(sheet.isRecycled())return;
            if(current==previous||fadeStart==0)paintImage(c,current,255);
            else {
                float t=Math.min(1,(SystemClock.uptimeMillis()-fadeStart)/(float)FADE);
                paintImage(c,previous,255);
                paintImage(c,current,Math.round(t*255));
                if(t<1)postInvalidateDelayed(32);
                else {previous=current;fadeStart=0;}
            }
            overlay.setShader(new LinearGradient(0,0,0,getHeight(),
                0xA909221A,0x4E0B281D,Shader.TileMode.CLAMP));
            c.drawRect(0,0,getWidth(),getHeight(),overlay);
        }
    }
}
