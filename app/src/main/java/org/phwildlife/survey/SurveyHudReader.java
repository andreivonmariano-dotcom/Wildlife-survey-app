package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Serial, bounded background polling of survey progress and recorded track distance. */
public final class SurveyHudReader implements AutoCloseable {
    public interface Callback { void onHud(Result result); }
    public static final class Result {
        public final String eventId;
        public final String progress;
        public final String track;
        Result(String eventId,String progress,String track){
            this.eventId=eventId;this.progress=progress;this.track=track;
        }
    }
    private final FieldDb helper;
    private final ExecutorService executor=Executors.newSingleThreadExecutor(r->{
        Thread t=new Thread(r,"likas-survey-hud");t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final AtomicBoolean pending=new AtomicBoolean();
    private volatile boolean closed;

    public SurveyHudReader(Context context){helper=new FieldDb(context.getApplicationContext());}
    public void request(String event,String method,int plannedStations,
            double plannedRouteMeters,boolean unitMethod,Callback callback){
        if(closed||event==null||pending.getAndSet(true))return;
        executor.execute(()->{
            Result result=null;
            try{
                SQLiteDatabase db=helper.getReadableDatabase();
                String progress="";
                if("point_count".equals(method)&&plannedStations>0){
                    int visited=RoutePlan.recordedCount(db,event);
                    int completed=unitCounts(db,"point_counts",event)[1];
                    progress=visited+"/"+plannedStations+" stations · "+completed+" complete";
                }else if(unitMethod){
                    int[] counts=unitCounts(db,
                        "point_count".equals(method)?"point_counts":"sampling_units",event);
                    progress=counts[1]+"/"+counts[0]+" visits complete";
                }
                String track="";
                if("line_transect".equals(method)||"ves_transect".equals(method)){
                    try(Cursor c=db.rawQuery(
                        "SELECT COALESCE(gps_track_m,0),COALESCE(gps_track_status,'waiting') "+
                        "FROM surveys WHERE id=?",new String[]{event})){
                        if(c.moveToFirst())
                            track=String.format(Locale.US,"GPS path ~%.0f m / %.0f m · %s",
                                c.getDouble(0),plannedRouteMeters,c.getString(1));
                    }
                }
                result=new Result(event,progress,track);
            }catch(Exception ignored){
                // Keep the last visible progress instead of reporting a false zero.
            }finally{
                pending.set(false);
            }
            Result ready=result;
            if(ready!=null&&!closed)ui.post(()->{
                if(!closed)callback.onHud(ready);
            });
        });
    }
    private static int[] unitCounts(SQLiteDatabase db,String table,String event){
        try(Cursor c=db.rawQuery(
            "SELECT COUNT(*),COALESCE(SUM(CASE WHEN completed=1 THEN 1 ELSE 0 END),0) "+
            "FROM "+table+" WHERE event_id=?",new String[]{event})){
            if(c.moveToFirst())return new int[]{c.getInt(0),c.getInt(1)};
        }
        return new int[]{0,0};
    }
    @Override public void close(){
        closed=true;ui.removeCallbacksAndMessages(null);
        executor.execute(helper::close);
        executor.shutdown();
    }
}
