package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.GnssStatus;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** North-up, true-elevation satellite sky map. GNSS positions are receiver readings, not a map of observer locations. */
public final class SatelliteSkyView extends View {
    private static final int INK=Color.rgb(26,54,45),SUBTLE=Color.rgb(97,119,111);
    private static final int OUTLINE=Color.rgb(20,115,73),SURFACE=Color.rgb(248,251,249);
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Sat> satellites=new ArrayList<>();
    private static final class Sat{
        final float azimuth,elevation,cn0;
        final int constellation,svid;
        final boolean used;
        Sat(GnssStatus g,int i){
            azimuth=g.getAzimuthDegrees(i);elevation=g.getElevationDegrees(i);
            cn0=g.getCn0DbHz(i);constellation=g.getConstellationType(i);
            svid=g.getSvid(i);used=g.usedInFix(i);
        }
    }
    public SatelliteSkyView(Context context){
        super(context);
        setMinimumHeight(Math.round(dp(290)));
        setContentDescription("Waiting for live GNSS satellite positions");
    }
    private float dp(float units){return units*getResources().getDisplayMetrics().density;}
    public void update(GnssStatus gnss){
        satellites.clear();
        int used=0;
        for(int i=0;i<gnss.getSatelliteCount();i++){
            Sat s=new Sat(gnss,i);
            satellites.add(s);
            if(s.used)used++;
        }
        setContentDescription(satellites.size()+" satellites tracked, "+used+" used in the fix. North is up; edge is horizon.");
        invalidate();
    }
    public void clear(){
        satellites.clear();
        setContentDescription("Waiting for live GNSS satellite positions");
        invalidate();
    }
    private void line(int color,float stroke){
        p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(stroke));
    }
    private void fill(int color){p.setColor(color);p.setStyle(Paint.Style.FILL);}
    private void text(float size,boolean bold,Paint.Align align,int color){
        fill(color);p.setTextSize(dp(size));p.setTextAlign(align);
        p.setTypeface(bold?android.graphics.Typeface.create("sans-serif-medium",0):
            android.graphics.Typeface.create("sans-serif",0));
    }
    private static String code(int system){
        switch(system){
            case GnssStatus.CONSTELLATION_GPS:return "G";
            case GnssStatus.CONSTELLATION_GLONASS:return "R";
            case GnssStatus.CONSTELLATION_GALILEO:return "E";
            case GnssStatus.CONSTELLATION_BEIDOU:return "C";
            case GnssStatus.CONSTELLATION_QZSS:return "J";
            case GnssStatus.CONSTELLATION_SBAS:return "S";
            case GnssStatus.CONSTELLATION_IRNSS:return "I";
            default:return "?";
        }
    }
    private static int satelliteColor(int constellation){
        switch(constellation){
            case GnssStatus.CONSTELLATION_GPS:return Color.rgb(26,113,79);
            case GnssStatus.CONSTELLATION_GALILEO:return Color.rgb(44,104,176);
            case GnssStatus.CONSTELLATION_GLONASS:return Color.rgb(131,72,165);
            case GnssStatus.CONSTELLATION_BEIDOU:return Color.rgb(188,108,34);
            default:return Color.rgb(91,115,122);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        final float centerX=getWidth()/2f,centerY=getHeight()/2f;
        final float radius=Math.min(getWidth()/2f-dp(33),getHeight()/2f-dp(34));
        if(radius<=dp(35))return;
        fill(SURFACE);canvas.drawCircle(centerX,centerY,radius,p);
        line(Color.rgb(199,215,205),1.2f);
        canvas.drawCircle(centerX,centerY,radius,p);
        canvas.drawCircle(centerX,centerY,radius/3f,p);
        canvas.drawCircle(centerX,centerY,2f*radius/3f,p);
        line(Color.rgb(219,227,220),0.9f);
        canvas.drawLine(centerX-radius,centerY,centerX+radius,centerY,p);
        canvas.drawLine(centerX,centerY-radius,centerX,centerY+radius,p);
        text(10,false,Paint.Align.LEFT,SUBTLE);
        canvas.drawText("60°",centerX+radius/3f+dp(4),centerY-dp(4),p);
        canvas.drawText("30°",centerX+2*radius/3f+dp(4),centerY-dp(4),p);
        text(14,true,Paint.Align.CENTER,INK);
        canvas.drawText("N",centerX,centerY-radius-dp(13),p);
        canvas.drawText("S",centerX,centerY+radius+dp(18),p);
        canvas.drawText("E",centerX+radius+dp(16),centerY+dp(4),p);
        canvas.drawText("W",centerX-radius-dp(16),centerY+dp(4),p);
        for(Sat sat:satellites){
            if(!Float.isFinite(sat.azimuth)||!Float.isFinite(sat.elevation)||sat.elevation<0)continue;
            double angle=Math.toRadians(sat.azimuth);
            float norm=(90f-Math.min(90f,sat.elevation))/90f;
            float x=centerX+(float)Math.sin(angle)*radius*norm;
            float y=centerY-(float)Math.cos(angle)*radius*norm;
            // Size reflects C/N0, border reflects participation in the actual fix.
            float strength=Float.isFinite(sat.cn0)?Math.max(0,Math.min(50,sat.cn0)):0;
            float marker=dp(10f+strength/14f);
            int base=satelliteColor(sat.constellation);
            if(sat.used){
                fill(Color.WHITE);canvas.drawCircle(x,y,marker+dp(3),p);
                line(OUTLINE,2.5f);canvas.drawCircle(x,y,marker+dp(2),p);
            }
            fill(base);canvas.drawCircle(x,y,marker,p);
            text(9,true,Paint.Align.CENTER,Color.WHITE);
            canvas.drawText(code(sat.constellation)+sat.svid,x,y+dp(3),p);
        }
        if(satellites.isEmpty()){
            text(12,false,Paint.Align.CENTER,SUBTLE);
            canvas.drawText("Waiting for satellite signals",centerX,centerY+dp(4),p);
        }
    }
}
