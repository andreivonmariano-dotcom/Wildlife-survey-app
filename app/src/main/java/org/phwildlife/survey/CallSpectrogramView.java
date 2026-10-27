package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/** Color spectrogram and time selection, works for live microphone and archived WAV. */
public final class CallSpectrogramView extends View {
    private static final int MAX_COLUMNS=120;
    private final ArrayList<float[]> columns=new ArrayList<>();
    private final Paint paint=new Paint();
    private float cursor=.0f;
    private long durationMs=0,visibleStartMs=0,visibleEndMs=0;
    private float contrast=1f;
    private boolean savedMode=false;
    public interface PositionListener {void selected(float fraction);}
    private PositionListener listener;
    public interface FrequencyListener {void measured(double hz,long timeMs);}
    private FrequencyListener frequencyListener;
    public void setFrequencyListener(FrequencyListener listener){frequencyListener=listener;}
    public void setContrast(float factor){
        contrast=Math.max(.5f,Math.min(4f,factor));invalidate();
    }
    public CallSpectrogramView(Context context){
        super(context);
        setMinimumHeight(Math.round(220*getResources().getDisplayMetrics().density));
        setContentDescription("Sound spectrogram from low frequencies at bottom to high frequencies at top");
    }
    public void setPositionListener(PositionListener callback){listener=callback;}
    public void setCursor(float fraction){
        cursor=Math.max(0f,Math.min(1f,fraction));invalidate();
    }
    public void append(short[] samples){
        if(savedMode)return;
        float[] band=SpectrogramMath.analyze(samples,0);
        if(columns.size()>=MAX_COLUMNS)columns.remove(0);
        columns.add(band);invalidate();
    }
    public void resetLive(){
        savedMode=false;durationMs=0;cursor=0;columns.clear();invalidate();
    }
    public void setSaved(float[][] frames,long duration){
        setSaved(frames,duration,0,duration);
    }
    public void setSaved(float[][] frames,long duration,long startMs,long endMs){
        savedMode=true;durationMs=duration;visibleStartMs=startMs;visibleEndMs=endMs;
        columns.clear();
        for(float[] band:frames)columns.add(band);
        invalidate();
    }
    public static float[][] readWav(File file)throws Exception{
        return readWav(file,0,-1);
    }
    public static float[][] readWav(File file,long startMs,long endMs)throws Exception{
        try(RandomAccessFile audio=new RandomAccessFile(file,"r")){
            if(audio.length()<44)throw new IllegalArgumentException("Empty WAV");
            byte[] header=new byte[44];audio.readFully(header);
            if(header[0]!='R'||header[1]!='I'||header[8]!='W'||header[9]!='A')
                throw new IllegalArgumentException("Not a WAV file");
            long samples=(audio.length()-44)/2;
            if(samples<SpectrogramMath.WINDOW)return new float[0][];
            long totalMs=samples*1000L/CallAudioRecorder.SAMPLE_RATE;
            long from=Math.max(0,Math.min(totalMs,startMs));
            long to=endMs<0?totalMs:Math.max(from+1,Math.min(totalMs,endMs));
            long firstSample=from*CallAudioRecorder.SAMPLE_RATE/1000;
            long lastSample=Math.min(samples-SpectrogramMath.WINDOW,
                to*CallAudioRecorder.SAMPLE_RATE/1000-SpectrogramMath.WINDOW);
            if(lastSample<firstSample)lastSample=firstSample;
            int count=(int)Math.min(MAX_COLUMNS,Math.max(1,
                (lastSample-firstSample)/SpectrogramMath.WINDOW));
            float[][] out=new float[count][];
            byte[] frame=new byte[SpectrogramMath.WINDOW*2];
            short[] decoded=new short[SpectrogramMath.WINDOW];
            for(int i=0;i<count;i++){
                long start=count==1?firstSample:
                    firstSample+(lastSample-firstSample)*i/(count-1);
                audio.seek(44+start*2);audio.readFully(frame);
                for(int j=0;j<decoded.length;j++){
                    int low=frame[j*2]&255,high=frame[j*2+1];
                    decoded[j]=(short)(low|(high<<8));
                }
                out[i]=SpectrogramMath.analyze(decoded,0);
            }
            return out;
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();
        canvas.drawColor(Color.rgb(12,37,37));
        int count=columns.size();
        if(count==0){
            paint.setColor(Color.rgb(183,211,196));
            paint.setTextSize(16*getResources().getDisplayMetrics().scaledDensity);
            canvas.drawText("Spectrogram appears during recording or playback",12,h/2,paint);
            return;
        }
        float cw=w/count,ch=h/SpectrogramMath.BANDS;
        for(int col=0;col<count;col++){
            float[] band=columns.get(col);
            for(int b=0;b<band.length;b++){
                float power=Math.max(0f,Math.min(1f,band[b]*contrast));
                int r=(int)(12+power*233),g=(int)(35+power*192),
                    blue=(int)(39+power*62);
                paint.setColor(Color.rgb(Math.min(255,r),Math.min(255,g),Math.min(255,blue)));
                canvas.drawRect(col*cw,h-(b+1)*ch,(col+1)*cw+1,h-b*ch+1,paint);
            }
        }
        if(savedMode){
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(2.5f*getResources().getDisplayMetrics().density);
            float span=Math.max(1,visibleEndMs-visibleStartMs);
            float x=(cursor*durationMs-visibleStartMs)/span*w;
            canvas.drawLine(x,0,x,h,paint);
        }
        paint.setColor(Color.rgb(232,245,225));
        paint.setTextSize(11*getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText("17 kHz",6,16,paint);
        canvas.drawText("1 kHz",6,h*.53f,paint);
        canvas.drawText("85 Hz",6,h-7,paint);
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(savedMode&&event.getAction()==MotionEvent.ACTION_UP){
            float fraction=Math.max(0f,Math.min(1f,event.getX()/Math.max(1f,getWidth())));
            long ms=AcousticViewport.timeAt(fraction,durationMs,visibleStartMs,visibleEndMs);
            setCursor(ms/(float)Math.max(1,durationMs));
            if(listener!=null)listener.selected(ms/(float)Math.max(1,durationMs));
            if(frequencyListener!=null)frequencyListener.measured(
                AcousticViewport.frequencyAt(event.getY()/Math.max(1f,getHeight())),ms);
            performClick();return true;
        }
        return savedMode && event.getAction()==MotionEvent.ACTION_DOWN || super.onTouchEvent(event);
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
