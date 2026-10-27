package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.location.Location;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener {
    private final int DARK=Color.rgb(16,41,31),GREEN=Color.rgb(34,110,75);
    private SurfaceView preview; private Reticle reticle; private FrameLayout stage;
    private TextView bearingText,infoText;
    private Camera camera;
    private SensorManager sensors;
    private float heading=Float.NaN,inclination=Float.NaN;
    private int quality=-1;
    private float previewAspect=4f/3f;
    private int camId=0;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}

    private TextView text(String s,int size) {
        TextView v=new TextView(this);
        v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }
    private Button button(String title,int color){
        Button v=new Button(this);v.setAllCaps(false);v.setText(title);v.setTextColor(Color.WHITE);
        v.setBackgroundTintList(ColorStateList.valueOf(color));v.setMinHeight(dp(52));
        return v;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(DARK);root.setPadding(dp(10),dp(8),dp(10),dp(12));
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        Button back=button("← Close",Color.rgb(80,104,91));
        bar.addView(back,new LinearLayout.LayoutParams(dp(112),dp(52)));
        back.setOnClickListener(v->finish());
        TextView title=text("Camera compass",19);title.setGravity(Gravity.CENTER);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));
        root.addView(bar);
        TextView instruction=text("Aim the center crosshair at the bird's first detection position.",12);
        instruction.setTextColor(Color.rgb(208,226,212));
        root.addView(instruction);
        stage=new FrameLayout(this);stage.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams stageParams=new LinearLayout.LayoutParams(-1,0,1f);
        stageParams.topMargin=dp(10);stageParams.bottomMargin=dp(10);
        root.addView(stage,stageParams);
        preview=new SurfaceView(this);
        stage.addView(preview,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
        reticle=new Reticle();
        stage.addView(reticle,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
        preview.getHolder().addCallback(this);
        stage.addOnLayoutChangeListener((v,left,top,right,bottom,ol,ot,or_,ob)->sizePreview());
        bearingText=text("Magnetic bearing: --°     Pitch: --°",20);
        bearingText.setGravity(Gravity.CENTER);
        root.addView(bearingText,new LinearLayout.LayoutParams(-1,dp(44)));
        infoText=text("Magnetic north · No distance is estimated from the camera",12);
        infoText.setTextColor(Color.rgb(206,224,211));
        infoText.setGravity(Gravity.CENTER);
        root.addView(infoText);
        Button capture=button("Capture bearing",GREEN);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(62));cp.topMargin=dp(10);
        root.addView(capture,cp);
        capture.setOnClickListener(v->capture());
        setContentView(root);
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},61);
    }
    private void capture(){
        if(Float.isNaN(heading)){Toast.makeText(this,"Bearing unavailable. Aim phone nearer the horizon.",Toast.LENGTH_SHORT).show();return;}
        Intent result=new Intent();
        result.putExtra("bearing_deg",heading);
        result.putExtra("pitch_deg",inclination);
        result.putExtra("captured_at",System.currentTimeMillis());
        Location fix=TrackService.currentFix();
        if(fix!=null&&LocationManager.GPS_PROVIDER.equals(fix.getProvider())&&fix.hasAccuracy()){
            long age=(SystemClock.elapsedRealtimeNanos()-fix.getElapsedRealtimeNanos())/1000000L;
            if(GpsFixPolicy.acceptableElapsed(fix.getLatitude(),fix.getLongitude(),
                fix.getAccuracy(),age,fix.isFromMockProvider()))
                result.putExtra("capture_observer_fix",new Location(fix));
        }
        result.putExtra("sensor_quality",quality);
        setResult(RESULT_OK,result);
        finish();
    }
    private void sizePreview(){
        if(stage==null||preview==null||stage.getWidth()<=0||stage.getHeight()<=0)return;
        int maxW=stage.getWidth(),maxH=stage.getHeight();
        int fittedW=Math.min(maxW,Math.max(1,Math.round(maxH/previewAspect)));
        int fittedH=Math.max(1,Math.round(fittedW*previewAspect));
        if(fittedH>maxH){fittedH=maxH;fittedW=Math.max(1,Math.round(fittedH/previewAspect));}
        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(fittedW,fittedH,Gravity.CENTER);
        FrameLayout.LayoutParams grid=new FrameLayout.LayoutParams(fittedW,fittedH,Gravity.CENTER);
        preview.setLayoutParams(params);
        reticle.setLayoutParams(grid);
    }
    private Camera.Size choosePreview(Camera.Parameters params) {
        List<Camera.Size> sizes=params.getSupportedPreviewSizes();
        Camera.Size preferred=params.getPreviewSize();
        if(sizes==null||sizes.isEmpty())return preferred;
        double best=Double.POSITIVE_INFINITY;
        for(Camera.Size candidate:sizes){
            double aspect=(double)candidate.width/candidate.height;
            double score=Math.abs(aspect-4.0/3.0)*15.0+
                Math.abs(candidate.width-1280)/2500.0;
            if(score<best){best=score;preferred=candidate;}
        }
        return preferred;
    }
    private void startCamera(){
        if(camera!=null||preview==null||!preview.getHolder().getSurface().isValid())return;
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try {
            Camera.CameraInfo info=new Camera.CameraInfo();
            int found=-1;
            for(int i=0;i<Camera.getNumberOfCameras();i++){
                Camera.getCameraInfo(i,info);
                if(info.facing==Camera.CameraInfo.CAMERA_FACING_BACK){found=i;break;}
            }
            camId=found>=0?found:0;
            camera=Camera.open(camId);
            Camera.getCameraInfo(camId,info);
            Camera.Parameters p=camera.getParameters();
            Camera.Size chosen=choosePreview(p);
            if(chosen!=null) {
                p.setPreviewSize(chosen.width,chosen.height);
                camera.setParameters(p);
                // The camera's native preview dimensions are landscape; portrait view is rotated.
                previewAspect=(float)chosen.width/(float)chosen.height;
            }
            camera.setDisplayOrientation(info.orientation);
            camera.setPreviewDisplay(preview.getHolder());
            sizePreview();
            camera.startPreview();
        }catch(Exception e){
            stopCamera();
            infoText.setText("Camera preview unavailable. Close to record without bearing.");
        }
    }
    private void stopCamera(){
        if(camera!=null){
            try{camera.stopPreview();}catch(Exception ignored){}
            camera.release();camera=null;
        }
    }
    @Override protected void onResume(){
        super.onResume();
        Sensor sensor=sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(sensor!=null)sensors.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);
        startCamera();
    }
    @Override protected void onPause(){super.onPause();sensors.unregisterListener(this);stopCamera();}
    @Override public void surfaceCreated(SurfaceHolder holder){startCamera();}
    @Override public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){sizePreview();}
    @Override public void surfaceDestroyed(SurfaceHolder holder){stopCamera();}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==61 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED)startCamera();
        else infoText.setText("Camera permission denied. Use Mark now on the survey screen.");
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){
        if(sensor!=null&&sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
            quality=accuracy;
            if(accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE)
                infoText.setText("Low compass accuracy · move away from metal and electronics");
            else infoText.setText("Magnetic north · distance must be entered manually");
        }
    }
    @Override public void onSensorChanged(SensorEvent ev){
        if(ev.sensor.getType()!=Sensor.TYPE_ROTATION_VECTOR)return;
        float[] rotation=new float[9];
        SensorManager.getRotationMatrixFromVector(rotation,ev.values);
        float[] direction=Geometry.cameraAxisHeadingAndPitch(rotation);
        heading=direction[0];inclination=direction[1];
        bearingText.setText(Float.isNaN(heading)?"Bearing unavailable · aim nearer horizon":
            String.format(Locale.US,"Magnetic %.0f°      Pitch %+.0f°",heading,inclination));
    }
    private class Reticle extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Reticle(){super(CameraActivity.this);setClickable(false);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float x=getWidth()/2f,y=getHeight()/2f,d=dp(20),gap=dp(9);
            p.setStrokeWidth(dp(2));p.setColor(Color.BLACK);
            c.drawLine(x-d-dp(1),y,x-gap+dp(1),y,p);
            c.drawLine(x+gap-dp(1),y,x+d+dp(1),y,p);
            c.drawLine(x,y-d-dp(1),x,y-gap+dp(1),p);
            c.drawLine(x,y+gap-dp(1),x,y+d+dp(1),p);
            p.setColor(Color.WHITE);p.setStrokeWidth(dp(1.5f));
            c.drawLine(x-d,y,x-gap,y,p);c.drawLine(x+gap,y,x+d,y,p);
            c.drawLine(x,y-d,x,y-gap,p);c.drawLine(x,y+gap,x,y+d,p);
            c.drawCircle(x,y,dp(2.5f),p);
        }
    }
}
