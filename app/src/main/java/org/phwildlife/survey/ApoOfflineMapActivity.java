package org.phwildlife.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Entirely offline reference map. The outlines are low-resolution 2026-Q3
 * administrative province polygons, NOT PAIC or species range boundaries.
 * User coordinates are loaded read-only from their original SQLite29 records.
 */
public final class ApoOfflineMapActivity extends Activity {
    private static final double[] PH={115.0,130.0,4.0,22.0};
    private static final double[] MINDANAO={120.0,128.0,4.5,11.3};
    private final ArrayList<Pin> pins=new ArrayList<>();
    private JSONArray provinceFeatures;
    private MapSurface surface;
    private TextView subtitle;
    private boolean mindanao=false;
    private boolean showLocalities=true;

    private static final class Pin {
        final String name,kind,provenance;
        final double lat,lon;
        Pin(String name,String kind,String provenance,double lat,double lon){
            this.name=name;this.kind=kind;this.provenance=provenance;
            this.lat=lat;this.lon=lon;
        }
    }
    private static boolean valid(double lat,double lon){
        return Double.isFinite(lat)&&Double.isFinite(lon)&&
            lat>=-90&&lat<=90&&lon>=-180&&lon<=180;
    }
    private static String fmt(double value){
        return String.format(Locale.US,"%.5f",value);
    }
    private ArrayList<Pin> readLocalities() {
        ArrayList<Pin> result=new ArrayList<>();
        File file=getDatabasePath(FieldDb.NAME);
        if(!file.isFile())return result;
        // OPEN_READONLY deliberately bypasses FieldDb constructors and upgrades.
        try(SQLiteDatabase db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),
                null,SQLiteDatabase.OPEN_READONLY)){
            try(Cursor rows=db.rawQuery(
                "SELECT COALESCE(name,id),latitude,longitude,"+
                "COALESCE(coordinate_uncertainty_m,-1) FROM sites "+
                "WHERE latitude IS NOT NULL AND longitude IS NOT NULL",null)){
                while(rows.moveToNext()){
                    double lat=rows.getDouble(1),lon=rows.getDouble(2);
                    if(!valid(lat,lon))continue;
                    double accuracy=rows.getDouble(3);
                    String detail="Saved site reference coordinate"+
                        (accuracy>=0?" · recorded uncertainty "+Math.round(accuracy)+" m":
                            " · coordinate uncertainty unrecorded");
                    result.add(new Pin(rows.getString(0),"site",detail,lat,lon));
                }
            }
            try(Cursor rows=db.rawQuery(
                "SELECT COALESCE(survey_profile,taxon_group,'Survey'),"+
                "COALESCE(started,''),start_gps_latitude,start_gps_longitude,"+
                "COALESCE(start_gps_status,'unrecorded') FROM surveys "+
                "WHERE start_gps_latitude IS NOT NULL AND start_gps_longitude IS NOT NULL",null)){
                while(rows.moveToNext()){
                    double lat=rows.getDouble(2),lon=rows.getDouble(3);
                    if(!valid(lat,lon))continue;
                    String label=rows.getString(0)+" survey";
                    String date=rows.getString(1);
                    String status=rows.getString(4);
                    result.add(new Pin(label,"survey",
                        "Survey-start GPS fix · "+(date==null?"":date)+
                        " · status "+status,lat,lon));
                }
            }
        }catch(Exception ex){
            // Keep the reference map usable if a historical table is unavailable.
            // Corrupt or inaccessible field rows leave the reference map usable.
            result.clear();
        }
        return result;
    }

    private void showPin(Pin pin){
        String message=pin.provenance+"\n"+
            fmt(pin.lat)+"° N, "+fmt(pin.lon)+"° E"+
            "\nStored only on this device.";
        new AlertDialog.Builder(this).setTitle(pin.name)
            .setMessage(message).setPositiveButton("Close",null).show();
    }
    private void updateSubtitle(){
        if(subtitle==null)return;
        int visible=0;
        double[] extent=mindanao?MINDANAO:PH;
        for(Pin p:pins)if(inside(p.lat,p.lon,extent))visible++;
        subtitle.setText((mindanao?"Mindanao view":"Philippines view")+
            " · "+visible+" saved coordinates in view");
    }
    private static boolean inside(double lat,double lon,double[] b){
        return valid(lat,lon)&&lon>=b[0]&&lon<=b[1]&&lat>=b[2]&&lat<=b[3];
    }
    private TextView action(String title,Runnable run){
        TextView v=new TextView(this);
        v.setText(title);v.setTextColor(ApoVisualSystem.FOREST);
        v.setTextSize(14);v.setGravity(Gravity.CENTER);
        v.setMinHeight(ApoVisualSystem.dp(this,48));
        v.setPadding(ApoVisualSystem.dp(this,9),ApoVisualSystem.dp(this,8),
            ApoVisualSystem.dp(this,9),ApoVisualSystem.dp(this,8));
        v.setBackground(ApoVisualSystem.surface(this,ApoVisualSystem.SURFACE,
            ApoVisualSystem.BORDER,12));
        v.setContentDescription(title);v.setFocusable(true);v.setClickable(true);
        v.setOnClickListener(w->run.run());return v;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(ApoVisualSystem.FIELD_BACKGROUND);
        getWindow().setNavigationBarColor(ApoVisualSystem.FIELD_BACKGROUND);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ApoVisualSystem.FIELD_BACKGROUND);
        int gutter=ApoVisualSystem.dp(this,14);
        root.setPadding(gutter,gutter,gutter,gutter);
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=action("Back",this::finish);
        header.addView(back,new LinearLayout.LayoutParams(ApoVisualSystem.dp(this,78),-2));
        TextView heading=new TextView(this);
        heading.setText("Offline Map");heading.setTextSize(22);
        heading.setTextColor(ApoVisualSystem.INK);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(gutter,0,0,0);
        header.addView(heading,new LinearLayout.LayoutParams(0,-2,1f));
        root.addView(header);
        subtitle=new TextView(this);
        subtitle.setTextSize(13);subtitle.setTextColor(ApoVisualSystem.MUTED);
        subtitle.setPadding(0,ApoVisualSystem.dp(this,9),0,ApoVisualSystem.dp(this,10));
        root.addView(subtitle);
        surface=new MapSurface();
        surface.setContentDescription("Offline provincial reference map with saved survey and site coordinates");
        LinearLayout.LayoutParams mapSize=new LinearLayout.LayoutParams(-1,0,1f);
        root.addView(surface,mapSize);
        LinearLayout controls=new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(0,ApoVisualSystem.dp(this,10),0,0);
        TextView scope=action("Mindanao",()->{});
        scope.setOnClickListener(v->{
            mindanao=!mindanao;
            scope.setText(mindanao?"Philippines":"Mindanao");
            scope.setContentDescription(mindanao?
                "Show all Philippines":"Show Mindanao region");
            surface.resetShapes();surface.invalidate();updateSubtitle();
        });
        controls.addView(scope,new LinearLayout.LayoutParams(0,-2,1f));
        TextView locality=action("Hide sites",()->{});
        locality.setOnClickListener(v->{
            showLocalities=!showLocalities;
            locality.setText(showLocalities?"Hide sites":"Show sites");
            locality.setContentDescription(showLocalities?
                "Hide recorded coordinates":"Show recorded coordinates");
            surface.invalidate();
        });
        LinearLayout.LayoutParams locParams=new LinearLayout.LayoutParams(0,-2,1f);
        locParams.leftMargin=ApoVisualSystem.dp(this,7);
        controls.addView(locality,locParams);
        if(ApoVisualSystem.largeText(this)){
            scope.setTextSize(12);
            locality.setTextSize(12);
        }
        root.addView(controls);
        TextView legend=new TextView(this);
        legend.setText("● Survey GPS   ◆ Saved site\n"+
            "2026 Q3 provinces · schematic view");
        legend.setTextColor(ApoVisualSystem.MUTED);
        legend.setTextSize(ApoVisualSystem.largeText(this)?10:12);
        legend.setPadding(0,ApoVisualSystem.dp(this,8),0,0);
        legend.setContentDescription("Map legend and sources. Tap to read details");
        legend.setFocusable(true);legend.setClickable(true);
        legend.setMinHeight(ApoVisualSystem.dp(this,48));
        legend.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Map sources and coordinates")
            .setMessage("Boundaries: Philippine administrative provinces, PSGC Q3 2026, "+
                "low-resolution simplified polygons. Source: faeldon/philippines-json-maps "+
                "(MIT).\n\nCoordinates: WGS84 (EPSG:4326). The map is a schematic "+
                "longitude-latitude view without a distance scale.\n\n"+
                "● Survey-start GPS: coordinates recorded at survey start.\n"+
                "◆ Saved site: field project reference coordinates. "+
                "Original accuracy and uncertainty remain in the saved record.")
            .setPositiveButton("Close",null).show());
        root.addView(legend);
        setContentView(root);
        updateSubtitle();
        // Boundary geometry parsing and SQLite reads stay off the UI thread.
        new Thread(()->{
            JSONArray features=null;
            try{features=BoundaryLookup.mapFeatures(getApplicationContext());}
            catch(Exception ignored){}
            final JSONArray ready=features;
            // Database reads never mutate a list currently used by the UI.
            final ArrayList<Pin> loaded=readLocalities();
            runOnUiThread(()->{
                if(isFinishing()||isDestroyed())return;
                pins.clear();pins.addAll(loaded);
                provinceFeatures=ready;
                surface.resetShapes();
                surface.invalidate();
                updateSubtitle();
                if(ready==null)subtitle.setText("Provincial map unavailable. Saved coordinates shown.");
            });
        },"apo-offline-map-loader").start();
    }

    private final class MapSurface extends View {
        private final Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG);
        private Path[] shapes;
        private int shapedWidth=-1,shapedHeight=-1;
        MapSurface(){super(ApoOfflineMapActivity.this);setMinimumHeight(ApoVisualSystem.dp(ApoOfflineMapActivity.this,230));}
        void resetShapes(){shapes=null;shapedWidth=-1;shapedHeight=-1;}
        private double[] bounds(){return mindanao?MINDANAO:PH;}
        private float x(double lon,int width){
            double[] b=bounds();
            return (float)(16+(lon-b[0])/(b[1]-b[0])*(width-32));
        }
        private float y(double lat,int height){
            double[] b=bounds();
            return (float)(15+(b[3]-lat)/(b[3]-b[2])*(height-30));
        }
        private void ring(Path path,JSONArray ring,int w,int h)throws Exception{
            for(int k=0;k<ring.length();k++){
                JSONArray pt=ring.getJSONArray(k);
                float px=x(pt.getDouble(0),w),py=y(pt.getDouble(1),h);
                if(k==0)path.moveTo(px,py);
                else path.lineTo(px,py);
            }
            path.close();
        }
        private void polygon(Path p,JSONArray rings,int w,int h)throws Exception{
            for(int i=0;i<rings.length();i++)ring(p,rings.getJSONArray(i),w,h);
        }
        private void rebuild(int w,int h){
            shapedWidth=w;shapedHeight=h;
            if(provinceFeatures==null){shapes=new Path[0];return;}
            Path[] next=new Path[provinceFeatures.length()];
            for(int i=0;i<next.length;i++)try{
                JSONObject entry=provinceFeatures.getJSONObject(i);
                Path path=new Path();path.setFillType(Path.FillType.EVEN_ODD);
                JSONArray g=entry.getJSONArray("g");
                if("MultiPolygon".equals(entry.optString("t"))){
                    for(int j=0;j<g.length();j++)polygon(path,g.getJSONArray(j),w,h);
                }else polygon(path,g,w,h);
                next[i]=path;
            }catch(Exception ignored){}
            shapes=next;
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            int w=getWidth(),h=getHeight();
            if(w<60||h<70)return;
            canvas.drawColor(Color.rgb(236,244,248));
            double[] b=bounds();
            brush.setStrokeWidth(ApoVisualSystem.dp(ApoOfflineMapActivity.this,.7f));
            brush.setStyle(Paint.Style.STROKE);
            brush.setColor(0xFFCCDEE0);
            int latStep=mindanao?2:4,lonStep=mindanao?2:5;
            for(int lat=(int)Math.ceil(b[2]);lat<=b[3];lat++)
                if(lat%latStep==0)canvas.drawLine(15,y(lat,h),w-15,y(lat,h),brush);
            for(int lon=(int)Math.ceil(b[0]);lon<=b[1];lon++)
                if(lon%lonStep==0)canvas.drawLine(x(lon,w),15,x(lon,w),h-15,brush);
            if(shapes==null||shapedWidth!=w||shapedHeight!=h)rebuild(w,h);
            brush.setColor(ApoVisualSystem.LEAF_MID);
            brush.setStyle(Paint.Style.FILL);
            for(Path p:shapes)if(p!=null)canvas.drawPath(p,brush);
            brush.setColor(0xFF9AB8A3);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeWidth(ApoVisualSystem.dp(ApoOfflineMapActivity.this,.55f));
            for(Path p:shapes)if(p!=null)canvas.drawPath(p,brush);
            brush.setStyle(Paint.Style.FILL);
            brush.setTextSize(ApoVisualSystem.dp(ApoOfflineMapActivity.this,11));
            brush.setColor(ApoVisualSystem.MUTED);
            canvas.drawText("N ↑",14,22,brush);
            if(showLocalities)for(Pin p:pins){
                if(!inside(p.lat,p.lon,b))continue;
                float px=x(p.lon,w),py=y(p.lat,h);
                boolean survey="survey".equals(p.kind);
                brush.setColor(survey?ApoVisualSystem.CAMERA:ApoVisualSystem.FOREST);
                if(survey)canvas.drawCircle(px,py,ApoVisualSystem.dp(ApoOfflineMapActivity.this,4),brush);
                else{
                    float d=ApoVisualSystem.dp(ApoOfflineMapActivity.this,5);
                    Path diamond=new Path();
                    diamond.moveTo(px,py-d);diamond.lineTo(px+d,py);
                    diamond.lineTo(px,py+d);diamond.lineTo(px-d,py);
                    diamond.close();canvas.drawPath(diamond,brush);
                }
            }
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getAction()!=MotionEvent.ACTION_UP)return true;
            if(!showLocalities)return true;
            Pin closest=null;
            double best=ApoVisualSystem.dp(ApoOfflineMapActivity.this,22);
            for(Pin p:pins){
                if(!inside(p.lat,p.lon,bounds()))continue;
                double distance=Math.hypot(event.getX()-x(p.lon,getWidth()),
                    event.getY()-y(p.lat,getHeight()));
                if(distance<best){best=distance;closest=p;}
            }
            if(closest!=null)showPin(closest);
            performClick();
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
