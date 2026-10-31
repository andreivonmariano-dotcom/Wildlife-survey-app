package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;

/** Exact source bitmap aspect ratio and 0..1 image geometry, never estimated anatomy. */
public final class Rc9PhotoMarkerView extends View {
    private Bitmap bitmap;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF image=new RectF();
    private JSONArray approved=new JSONArray();
    private boolean draftEnabled=false;
    private float draftX=-1f,draftY=-1f;
    public Rc9PhotoMarkerView(Context context){super(context);
        setContentDescription("Offline photographic reference with review-only annotation points");
        setMinimumHeight(Math.round(270*getResources().getDisplayMetrics().density));
    }
    public boolean useAsset(String path){
        if(path==null||!path.matches("offline_photos/inat_[0-9]+\\.jpg"))return false;
        try(InputStream in=getContext().getAssets().open(path)){
            BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;
            Bitmap image=BitmapFactory.decodeStream(in,null,options);
            if(image==null||image.getWidth()<30||image.getHeight()<30)return false;
            if(bitmap!=null)bitmap.recycle();
            bitmap=image;
            draftX=-1f;draftY=-1f;approved=new JSONArray();invalidate();
            return true;
        }catch(Exception e){return false;}
    }
    public void allowDraftTap(boolean editable){draftEnabled=editable;}
    public void showApproved(JSONArray items){approved=items==null?new JSONArray():items;invalidate();}
    public boolean hasDraft(){return draftX>=0&&draftY>=0;}
    public float markerX(){return draftX;}
    public float markerY(){return draftY;}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int width=MeasureSpec.getSize(widthSpec);
        int h=Math.round(290*getResources().getDisplayMetrics().density);
        setMeasuredDimension(width,resolveSize(h,heightSpec));
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(0xffe8e9e3);
        if(bitmap==null)return;
        float scale=Math.min((float)getWidth()/bitmap.getWidth(),(float)getHeight()/bitmap.getHeight());
        float dw=bitmap.getWidth()*scale,dh=bitmap.getHeight()*scale;
        float x=(getWidth()-dw)*0.5f,y=(getHeight()-dh)*0.5f;
        image.set(x,y,x+dw,y+dh);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap,null,image,paint);
        for(int i=0;i<approved.length();i++){
            JSONObject m=approved.optJSONObject(i);
            if(m==null||!"expert_approved".equals(m.optString("reviewStatus")))continue;
            double ax=m.optDouble("x",-1),ay=m.optDouble("y",-1);
            if(ax<0||ax>1||ay<0||ay>1)continue;
            dot(canvas,(float)ax,(float)ay,0xff1c7352);
        }
        if(hasDraft())dot(canvas,draftX,draftY,0xffb84b2b);
    }
    private void dot(Canvas canvas,float x,float y,int color){
        float px=image.left+x*image.width(),py=image.top+y*image.height();
        float r=8*getResources().getDisplayMetrics().density;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3*getResources().getDisplayMetrics().density);
        paint.setColor(Color.WHITE);canvas.drawCircle(px,py,r+2,paint);
        paint.setColor(color);canvas.drawCircle(px,py,r,paint);
        paint.setStyle(Paint.Style.FILL);
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(!draftEnabled||bitmap==null)return super.onTouchEvent(event);
        if(event.getAction()==MotionEvent.ACTION_DOWN)return true;
        if(event.getAction()!=MotionEvent.ACTION_UP)return true;
        float x=event.getX(),y=event.getY();
        if(!image.contains(x,y))return true;
        draftX=(x-image.left)/image.width();
        draftY=(y-image.top)/image.height();
        invalidate();performClick();return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
