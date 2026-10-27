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
import android.location.altitude.AltitudeConverter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static final String ACTION_ACQUIRE="org.phwildlife.survey.GNSS_ACQUIRE";
    public static final String ACTION_END="org.phwildlife.survey.GNSS_END";
    // Latest live phone GPS from one receiver subscription, shared across survey screens.
    private static volatile Location latestFix;
    private static volatile boolean receiverRunning=false;
    public static Location currentFix(){
        Location fix=latestFix;
        return fix==null?null:new Location(fix);
    }
    public static boolean isRunning(){return receiverRunning;}
    public static boolean wasStopped(android.content.Context context){
        return context.getSharedPreferences("likas_gps_control",MODE_PRIVATE)
            .getBoolean("user_stopped",false);
    }
    public static void startGnss(android.content.Context context){
        // A visible user action explicitly clears a prior notification stop.
        context.getSharedPreferences("likas_gps_control",MODE_PRIVATE).edit()
            .putBoolean("user_stopped",false).apply();
        Intent request=new Intent(context,TrackService.class).setAction(ACTION_ACQUIRE);
        if(Build.VERSION.SDK_INT>=26)context.startForegroundService(request);
        else context.startService(request);
    }
    public static void stopGnss(android.content.Context context){
        Intent request=new Intent(context,TrackService.class).setAction(ACTION_END);
        context.startService(request);
    }
    private static final String CHANNEL="wildlife_track_v09";
    private static final int NOTIFY_ID=426;
    private LocationManager manager;
    private FieldDb db;
    private String eventId;
    private Location previous;
    private long lastElapsed=0;
    private double total=0;
    private String lastReason="Awaiting GPS";
    private long lastNotificationElapsed=0;
    private boolean foreground=false;
    private boolean subscribed=false;
    private final ExecutorService altitudeWorker=Executors.newSingleThreadExecutor();
    private AltitudeConverter altitudeConverter;
    private long lastConversionRequest=0;
    private static String iso(long t){SimpleDateFormat s=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);s.setTimeZone(TimeZone.getTimeZone("UTC"));return s.format(new Date(t));}
    @Override public void onCreate(){super.onCreate();manager=(LocationManager)getSystemService(LOCATION_SERVICE);db=new FieldDb(this);}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String action=intent==null?ACTION_ACQUIRE:intent.getAction();
        if(ACTION_END.equals(action)){
            getSharedPreferences("likas_gps_control",MODE_PRIVATE)
                .edit().putBoolean("user_stopped",true).apply();
            stopRecording();return START_NOT_STICKY;
        }
        if(ACTION_ACQUIRE.equals(action)){
            getSharedPreferences("likas_gps_control",MODE_PRIVATE)
                .edit().putBoolean("user_stopped",false).apply();
        }else if(wasStopped(this)){
            // A notification Stop GPS choice must survive process restarts.
            stopRecording();return START_NOT_STICKY;
        }
        if(ACTION_STOP.equals(action)){
            if(eventId!=null)updateState("stopped");
            eventId=null;previous=null;lastElapsed=0;
            showNotification("GNSS active · background positioning");
            return START_STICKY;
        }
        // The active foreground service survives screen switching, keeping the receiver warm.
        try{
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                stopRecording();return START_NOT_STICKY;
            }
            if(manager==null||!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                stopRecording();return START_NOT_STICKY;
            }
            if(!foreground)startLocationForeground();
            if(!subscribed){
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,this);
                subscribed=true;
                receiverRunning=true;
            }
            if(ACTION_START.equals(action)){
                String requested=intent.getStringExtra("event_id");
                if(requested!=null&&!requested.isEmpty()&&!requested.equals(eventId)){
                    double restored;
                    try(Cursor c=db.getReadableDatabase().rawQuery(
                        "SELECT COALESCE(gps_track_m,0) FROM surveys WHERE id=? AND ended IS NULL",
                        new String[]{requested})){
                        if(!c.moveToFirst())return START_STICKY;
                        restored=c.getDouble(0);
                    }
                    eventId=requested;
                    total=restored;previous=null;lastElapsed=0;
                    updateState("recording");
                }
            }
            return START_STICKY;
        }catch(Exception ex){
            android.util.Log.w("WildlifeGNSS","GPS service cannot start",ex);
            stopRecording();return START_NOT_STICKY;
        }
    }
    private void startLocationForeground(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            n.createNotificationChannel(new NotificationChannel(CHANNEL,
                "Wildlife Survey continuous GPS",NotificationManager.IMPORTANCE_LOW));
        }
        Notification initial=notification("Searching for a fresh phone GPS fix");
        if(Build.VERSION.SDK_INT>=29)
            startForeground(NOTIFY_ID,initial,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else startForeground(NOTIFY_ID,initial);
        foreground=true;
    }
    private void showNotification(String message){
        if(!foreground)return;
        try{
            NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(n!=null)n.notify(NOTIFY_ID,notification(message));
        }catch(SecurityException ignored){
            // Android 13+ can hide notifications when POST_NOTIFICATIONS is denied.
        }
    }
    private Notification notification(String message){
        Intent open=new Intent(this,WelcomeActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending=PendingIntent.getActivity(this,0,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent stop=new Intent(this,TrackService.class).setAction(ACTION_END);
        PendingIntent stopPending=PendingIntent.getService(this,1,stop,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?
            new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        return builder.setContentTitle("Wildlife Survey · GPS active").setContentText(message)
          .setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentIntent(pending)
          .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Stop GPS",stopPending)
          .setOngoing(true).build();
    }
    private void updateState(String state){
        if(db==null||eventId==null)return;
        db.getWritableDatabase().execSQL("UPDATE surveys SET gps_track_status=? WHERE id=?",new Object[]{state,eventId});
    }
    @Override public void onLocationChanged(Location l){
        latestFix=new Location(l);
        // Android 14+ can convert native GPS WGS84 height to mean-sea-level
        // height. The conversion may load local geoid assets and must run
        // off the UI thread. Older phones retain the WGS84 height only.
        if(Build.VERSION.SDK_INT>=34&&l.hasAltitude()&&!l.hasMslAltitude()&&
                l.hasVerticalAccuracy()&&
                SystemClock.elapsedRealtime()-lastConversionRequest>=5000L){
            lastConversionRequest=SystemClock.elapsedRealtime();
            Location candidate=new Location(l);
            altitudeWorker.execute(()->{
                try{
                    if(altitudeConverter==null)altitudeConverter=new AltitudeConverter();
                    altitudeConverter.addMslAltitudeToLocation(
                        getApplicationContext(),candidate);
                    Location current=latestFix;
                    if(current!=null&&current.getElapsedRealtimeNanos()==
                            candidate.getElapsedRealtimeNanos()&&
                            candidate.hasMslAltitude()){
                        latestFix=new Location(candidate);
                    }
                }catch(Exception ignored){
                    // A geoid conversion failure never fabricates MSL height.
                }
            });
        }
        long uptime=SystemClock.elapsedRealtime();
        if(uptime-lastNotificationElapsed>=5000L){
            lastNotificationElapsed=uptime;
            showNotification(l.hasAccuracy()?
                String.format(Locale.US,"Continuous fix · reported ±%.1f m · %s",
                    l.getAccuracy(),eventId==null?"ready for surveys":"survey active"):
                "Acquiring GPS · accuracy not reported");
        }
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
        subscribed=false;receiverRunning=false;latestFix=null;
        updateState("stopped");
        if(foreground){
            if(Build.VERSION.SDK_INT>=24)stopForeground(STOP_FOREGROUND_REMOVE);
            else stopForeground(true);
            foreground=false;
        }
        stopSelf();
    }
    @Override public void onDestroy(){
        if(manager!=null)try{manager.removeUpdates(this);}catch(Exception ignored){}
        receiverRunning=false;latestFix=null;subscribed=false;
        if(db!=null)db.close();
        altitudeWorker.shutdownNow();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}