package org.phwildlife.survey;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.*;
import java.util.*;
public final class StationProgressActivity extends Activity {
 private FieldDb store;private String event;
 private LinearLayout list;
 private int dp(int x){return (int)(getResources().getDisplayMetrics().density*x+0.5f);}
 private TextView label(String text,int size){
   TextView v=new TextView(this);v.setText(text);v.setTextSize(size);
   v.setTextColor(UiKit.INK);v.setPadding(dp(9),dp(8),dp(9),dp(8));return v;
 }
 private LinearLayout column(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;}
 @Override public void onCreate(Bundle b){super.onCreate(b);store=new FieldDb(this);
   event=getIntent().getStringExtra("event_id");if(event==null){finish();return;}render();}
 @Override protected void onResume(){super.onResume();if(event!=null)render();}
 @Override protected void onDestroy(){super.onDestroy();store.close();}
 private void render(){
   String project="default",method="",outcome="active";
   try(Cursor c=store.getReadableDatabase().rawQuery(
     "SELECT COALESCE(project_id,'default'),method,COALESCE(outcome,'active') FROM surveys WHERE id=?",
     new String[]{event})){
     if(!c.moveToFirst()){finish();return;}
     project=c.getString(0);method=c.getString(1);outcome=c.getString(2);
   }
   LinearLayout root=column();root.setPadding(dp(14),dp(12),dp(14),dp(12));
   root.setBackgroundColor(UiKit.BG);
   Button back=UiKit.button(this,"‹ Back","secondary");back.setOnClickListener(v->finish());
   root.addView(back);root.addView(UiKit.title(this,"Station progress"));
   root.addView(label("Survey method: "+method.replace('_',' '),13));
   if(outcome.equals("not_counted")){
     TextView note=label("NOT COUNTED. Visits remain archived but excluded from analysis.",13);
     note.setTextColor(Color.rgb(188,40,38));root.addView(note);
   }
   ScrollView scroll=new ScrollView(this);list=column();scroll.addView(list);
   root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
   if(method.equals("line_transect")||method.equals("ves_transect")||method.equals("opportunistic")){
     list.addView(label("Continuous method: sampling time and location belong to one survey event.",14));return;
   }
   String table=method.equals("point_count")?"point_counts":"sampling_units";
   Map<String,List<String>> stations=new LinkedHashMap<>();
   int finished=0,unfinished=0,running=0;
   try(Cursor c=store.getReadableDatabase().rawQuery(
     "SELECT station_id,sequence_number,started,ended,completed FROM "+table+
     " WHERE event_id=? ORDER BY sequence_number",new String[]{event})){
     while(c.moveToNext()){
       String station=c.getString(0);
       if(!stations.containsKey(station))stations.put(station,new ArrayList<String>());
       String flag=c.isNull(3)?"ACTIVE":c.getInt(4)==1?"COMPLETE":"INCOMPLETE";
       if(flag.equals("ACTIVE"))running++;else if(flag.equals("COMPLETE"))finished++;else unfinished++;
       stations.get(station).add("Visit "+c.getInt(1)+" · "+flag+" · "+c.getString(2));
     }
   }
   list.addView(label("Completed "+finished+" · incomplete "+unfinished+" · active "+running,17));
   list.addView(label("Not yet visited stations",15));
   int missing=0;
   try(Cursor c=store.getReadableDatabase().rawQuery(
     "SELECT id,name FROM sites WHERE project_id=? ORDER BY id",new String[]{project})){
     while(c.moveToNext()){
       String id=c.getString(0);
       if(!stations.containsKey(id)){missing++;card(id,c.getString(1),null);}
     }
   }
   if(missing==0)list.addView(label("All registered stations have been visited.",12));
   list.addView(label("Stations with recorded visits",15));
   for(Map.Entry<String,List<String>> e:stations.entrySet())card(e.getKey(),e.getKey(),e.getValue());
 }
 private void card(String id,String name,List<String> visits){
   LinearLayout item=column();item.setBackground(UiKit.surface(this));
   item.setPadding(dp(8),dp(7),dp(8),dp(7));
   item.addView(label(id+" · "+name,16));
   if(visits==null)item.addView(label("NOT VISITED",12));
   else for(String row:visits)item.addView(label(row,12));
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);
   list.addView(item,p);
 }
}
