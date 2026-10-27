package org.phwildlife.survey;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** North-up GNSS sky plot: center is zenith, outer ring is the horizon. */
public final class SatelliteSkyView extends View {
    private static final int GREEN=Color.rgb(31,113,79),GRAY=Color.rgb(125,137,145);
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Sat> sats=new ArrayList<>();
    private static final class Sat {
        float az,el,cn0; int svid,system; boolean used;
        Sat(GnssStatus g,int i){
            az=g.getAzimuthDegrees(i);el=g.getElevationDegrees(i);
            cn0=g.getCn0DbHz(i);svid=g.getSvid(i);
            system=g.getConstellationType(i);used=g.usedInFix(i);
        }
    }
    public SatelliteSkyView(Context c){super(c);setContentDescription("Satellite sky plot awaiting GNSS status");}
    public void update(GnssStatus status){
        sats.clear();
        for(int i=0;i<status.getSatelliteCount();i++)sats.add(new Sat(status,i));
        setContentDescription(sats.size()+" tracked GNSS satellites. Green: used in position fix. Gray: not used.");
        invalidate();
    }
    public void clear(){sats.clear();invalidate();}
    private void style(int color,float width,boolean filled){
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(filled?Paint.Style.FILL:Paint.Style.STROKE);
    }
    private static String prefix(int system){
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
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float scale=getResources().getDisplayMetrics().scaledDensity;
        float cx=getWidth()/2f, cy=getHeight()/2f;
        float radius=Math.max(0,Math.min(getWidth(),getHeight())/2f-26*scale);
        if(radius<10)return;
        style(Color.rgb(210,220,213),2*scale,false);
        for(int k=1;k<=3;k++)c.drawCircle(cx,cy,radius*k/3f,paint);
        c.drawLine(cx-radius,cy,cx+radius,cy,paint);
        c.drawLine(cx,cy-radius,cx,cy+radius,paint);
        style(GREEN,1,true);
        paint.setTextSize(13*scale);paint.setTextAlign(Paint.Align.CENTER);
        c.drawText("N",cx,cy-radius-9*scale,paint);
        c.drawText("S",cx,cy+radius+17*scale,paint);
        c.drawText("E",cx+radius+12*scale,cy+4*scale,paint);
        c.drawText("W",cx-radius-12*scale,cy+4*scale,paint);
        for(Sat sat:sats){
            if(!Float.isFinite(sat.az)||!Float.isFinite(sat.el)||sat.el<0)continue;
            double radians=Math.toRadians(sat.az);
            float r=radius*(90-Math.min(90,sat.el))/90f;
            float x=cx+(float)Math.sin(radians)*r;
            float y=cy-(float)Math.cos(radians)*r;
            // Border identifies in-fix satellites; C/N0 changes marker size.
            float marker=(5f+Math.max(0,Math.min(50,sat.cn0))/20f)*scale;
            style(sat.used?GREEN:GRAY,1,true);c.drawCircle(x,y,marker,paint);
            style(Color.WHITE,1,true);paint.setTextSize(9*scale);paint.setTextAlign(Paint.Align.CENTER);
            c.drawText(prefix(sat.system)+sat.svid,x,y-marker-3*scale,paint);
        }
        if(sats.isEmpty()){
            style(GRAY,1,true);paint.setTextSize(12*scale);paint.setTextAlign(Paint.Align.CENTER);
            c.drawText("Searching for satellites",cx,cy+5*scale,paint);
        }
    }
}
