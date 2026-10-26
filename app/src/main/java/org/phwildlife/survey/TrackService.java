package org.phwildlife.survey;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Foreground GPS tracking for an actively started transect.
 * Distance is filtered GPS path length, not verified tape-measured line effort.
 */
public final class TrackService extends Service implements LocationListener {
    public static final String ACTION_START="org.phwildlife.survey.TRACK_START";
    public static final String ACTION_STOP="org.phwildlife.survey.TRACK_STOP";
    private static final String CHANNEL="wildlife_track_v09";
    private static final int NOTIFY_ID=426;
    private LocationManager manager;
    private FieldDb db;
    private String eventId;
    private Location previous;
    private long lastElapsed=0;
    private double total=0;
    private String lastReason="Awaiting GPS";
    private static String iso(long t){SimpleDateFormat s=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);s.setTimeZone(TimeZone.getTimeZone("UTC"));return s.format(new Date(t));}
    @Override public void onCreate(){super.onCreate();manager=(LocationManager)getSystemService(LOCATION_SERVICE);db=new FieldDb(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&ACTION_STOP.equals(intent.getAction())){stopRecording();return START_NOT_STICKY;}
        String requested=intent==null?null:intent.getStringExtra("event_id");
        if(requested==null||requested.isEmpty()){stopSelf();return START_NOT_STICKY;}
        if(eventId!=null&&eventId.equals(requested))return START_STICKY;
        if(eventId!=null)manager.removeUpdates(this);
        eventId=requested;
        previous=null;lastElapsed=0;total=0;
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT COALESCE(gps_track_m,0) FROM surveys WHERE id=? AND ended IS NULL",new String[]{eventId})){
            if(!c.moveToFirst()){stopRecording();return START_NOT_STICKY;}
            total=c.getDouble(0);
        }
        // Never bridge GPS gaps or an Activity/service restart with a fictitious straight segment.
        if(Build.VERSION.SDK_INT>=26){
            NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            n.createNotificationChannel(new NotificationChannel(CHANNEL,"Wildlife transect GPS",NotificationManager.IMPORTANCE_LOW));
        }
        Notification notification=notification("Collecting transect GPS positions");
        try{
            if(Build.VERSION.SDK_INT>=29)startForeground(NOTIFY_ID,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            else startForeground(NOTIFY_ID,notification);
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                updateState("location_permission_missing");stopRecording();return START_NOT_STICKY;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,2f,this);
            updateState("recording");
        }catch(Exception ex){updateState("gps_unavailable");stopRecording();return START_NOT_STICKY;}
        return START_STICKY;
    }
    private Notification notification(String message){
        Intent open=new Intent(this,WelcomeActivity.class);
        PendingIntent pending=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        return builder.setContentTitle("Wildlife transect tracking").setContentText(message)
          .setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentIntent(pending).setOngoing(true).build();
    }
    private void updateState(String state){
        if(db==null||eventId==null)return;
        db.getWritableDatabase().execSQL("UPDATE surveys SET gps_track_status=? WHERE id=?",new Object[]{state,eventId});
    }
    @Override public void onLocationChanged(Location l){
        if(eventId==null||db==null)return;
        if(l.isFromMockProvider()){record(l,0,false,"mock_location");return;}
        if(!l.hasAccuracy()||l.getAccuracy()>35){record(l,0,false,"low_accuracy");return;}
        long when=l.getElapsedRealtimeNanos();
        if(previous==null){accept(l,0,when,"first_fix");return;}
        double seconds=(when-lastElapsed)/1e9;
        if(seconds<=0){record(l,0,false,"out_of_order");return;}
        if(seconds>120){accept(l,0,when,"gap_reset");return;}
        float step=previous.distanceTo(l);
        if(seconds>0&&step/seconds>4.0){record(l,0,false,"unrealistic_walking_speed");return;}
        if(step<3){record(l,0,false,"jitter_below_3m");return;}
        // Use accepted GPS points only, with conservative filtering of sparse/poor fixes.
        accept(l,step,when,"accepted");
    }
    private void accept(Location l,double step,long time,String reason){
        total+=step;
        previous=new Location(l);lastElapsed=time;
        record(l,step,true,reason);
    }
    private void record(Location l,double step,boolean accepted,String reason){
        try{
            SQLiteDatabase d=db.getWritableDatabase();
            d.beginTransaction();
            try{
                d.execSQL("INSERT INTO track_points(event_id,recorded_at,latitude,longitude,accuracy_m,segment_m,accepted,reason) VALUES(?,?,?,?,?,?,?,?)",
                  new Object[]{eventId,iso(System.currentTimeMillis()),l.getLatitude(),l.getLongitude(),l.hasAccuracy()?l.getAccuracy():null,step,accepted?1:0,reason});
                if(accepted)d.execSQL("UPDATE surveys SET gps_track_m=? WHERE id=? AND ended IS NULL",new Object[]{total,eventId});
                d.setTransactionSuccessful();
            }finally{d.endTransaction();}
        }catch(Exception ignored){}
    }
    private void stopRecording(){
        if(manager!=null)try{manager.removeUpdates(this);}catch(Exception ignored){}
        updateState("stopped");
        if(Build.VERSION.SDK_INT>=24)stopForeground(STOP_FOREGROUND_REMOVE);else stopForeground(true);
        stopSelf();
    }
    @Override public void onDestroy(){
        if(manager!=null)try{manager.removeUpdates(this);}catch(Exception ignored){}
        if(db!=null)db.close();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}