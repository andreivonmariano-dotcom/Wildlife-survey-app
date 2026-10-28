package org.phwildlife.survey;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.text.InputType;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Private offline library for bird songs and amphibian calls.
 * Audio and labels outlive the survey; recording requires an active survey.
 */
public final class CallLibraryActivity extends Activity {
    private static final int REQUEST_AUDIO=321;
    private FieldDb database;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private LinearLayout body;
    private String eventId,group="Birds",surveyProfile="Birds",method="",station="",selectedClip=null;
    private long selectedMs=0,clipDuration=0;
    private int spectrogramZoom=1,analysisGeneration=0;
    private float spectrogramContrast=1f;
    private double lastTappedHz=Double.NaN;
    private TextView frequencyInfo;
    private CallAudioRecorder recorder;
    private CallSpectrogramView spectrum;
    private MediaPlayer player;
    private SeekBar seek;
    private TextView playbackTime;
    private Button recordButton;
    private String pendingFilename,pendingId,recordedAt;
    private final Runnable updateCursor=new Runnable(){
        @Override public void run(){
            if(player!=null&&player.isPlaying()){
                int pos=player.getCurrentPosition();
                selectedMs=pos;
                if(seek!=null)seek.setProgress(pos);
                if(spectrum!=null)spectrum.setCursor(pos/(float)Math.max(1,clipDuration));
                if(playbackTime!=null)playbackTime.setText(format(pos)+" / "+format(clipDuration));
                ui.postDelayed(this,250);
            }
        }
    };
    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
    private TextView label(String value,int size,boolean strong){
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(UiKit.INK);
        if(strong)t.setTypeface(null,android.graphics.Typeface.BOLD);
        t.setPadding(dp(4),dp(7),dp(4),dp(7));return t;
    }
    private LinearLayout column(){
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c;
    }
    private static String utc(long ms){
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date(ms));
    }
    private static String format(long ms){
        return String.format(Locale.US,"%02d:%02d",ms/60000,ms/1000%60);
    }
    private void notice(String text){
        android.widget.Toast.makeText(this,text,android.widget.Toast.LENGTH_LONG).show();
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        database=new FieldDb(this);
        eventId=getIntent().getStringExtra("event_id");
        String selectedGroup=getIntent().getStringExtra("taxon_group");
        if(selectedGroup!=null)group=selectedGroup;
        method=getIntent().getStringExtra("method");
        if(method==null)method="";
        station=getIntent().getStringExtra("station_id");
        if(station==null)station="";
        if(eventId!=null){
            try(Cursor c=database.getReadableDatabase().rawQuery(
                "SELECT COALESCE(survey_profile,taxon_group,'Birds'),method "+
                "FROM surveys WHERE id=?",new String[]{eventId})){
                if(c.moveToFirst()){
                    surveyProfile=c.getString(0);
                    if(selectedGroup==null)group=surveyProfile.equals("Herpetofauna")?
                        "Amphibians":surveyProfile;
                    method=c.getString(1);
                }
            }
        }
        draw();
    }
    private boolean canRecord(){
        if(eventId==null)return false;
        try(Cursor c=database.getReadableDatabase().rawQuery(
            "SELECT ended FROM surveys WHERE id=?",new String[]{eventId})){
            return c.moveToFirst()&&c.isNull(0);
        }catch(Exception e){return false;}
    }
    private void layout(String title){
        LinearLayout root=column();root.setBackgroundColor(UiKit.BG);
        root.setPadding(dp(14),dp(8),dp(14),dp(12));
        root.addView(UiKit.navigationHeader(this,title,selectedClip!=null?"Library":"Back",()->{
            if(recorder!=null){notice("Stop recording and finish saving before leaving");return;}
            if(selectedClip!=null){releasePlayer();selectedClip=null;draw();}
            else finish();
        }));
        ScrollView scroll=new ScrollView(this);
        body=column();body.setPadding(0,dp(8),0,dp(24));
        scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private void draw(){
        if(selectedClip!=null){showClip();return;}
        releasePlayer();layout("Sound recordings");
        TextView desc=label(eventId==null?
            "Saved bird songs and amphibian calls · all surveys":
            group+" · "+SurveyMethods.displayLabel(surveyProfile,method),13,false);
        desc.setTextColor(UiKit.MUTED);body.addView(desc);
        if(canRecord()){
            recordButton=UiKit.button(this,"● Record sound","primary");
            body.addView(recordButton,new LinearLayout.LayoutParams(-1,dp(55)));
            recordButton.setOnClickListener(v->{
                if(recorder!=null&&recorder.isCapturing())stopRecord();
                else checkMicrophone();
            });
            TextView help=label("44.1 kHz mono WAV · 10-minute clip limit · stored offline",12,false);
            help.setTextColor(UiKit.MUTED);body.addView(help);
            spectrum=new CallSpectrogramView(this);
            spectrum.resetLive();body.addView(spectrum,new LinearLayout.LayoutParams(-1,dp(204)));
        }else{
            spectrum=null;recordButton=null;
        }
        TextView heading=label("Recording library",17,true);
        heading.setPadding(dp(4),dp(14),dp(4),dp(8));body.addView(heading);
        String sql="SELECT c.id,c.event_id,c.taxon_group,c.recorded_at_utc,c.duration_ms,"+
            "COALESCE(s.outcome,'active'),"+
            "(SELECT COUNT(*) FROM acoustic_labels l WHERE l.clip_id=c.id) "+
            "FROM acoustic_clips c LEFT JOIN surveys s ON s.id=c.event_id "+
            (eventId==null?"":"WHERE c.event_id=? ")+
            "ORDER BY c.recorded_at_utc DESC LIMIT 250";
        try(Cursor c=database.getReadableDatabase().rawQuery(
                sql,eventId==null?null:new String[]{eventId})){
            if(c.getCount()==0)body.addView(label("No saved sound clips yet.",14,false));
            while(c.moveToNext()){
                String id=c.getString(0),survey=c.getString(1),taxon=c.getString(2),
                    date=c.getString(3),outcome=c.getString(5);
                long duration=c.getLong(4);int labels=c.getInt(6);
                Button item=UiKit.button(this,"▶  "+taxon+"  ·  "+format(duration)+
                    "\n"+date+"  ·  "+labels+" call labels  ·  "+outcome,"secondary");
                item.setAllCaps(false);
                item.setMinHeight(dp(78));
                LinearLayout.LayoutParams card=new LinearLayout.LayoutParams(-1,dp(84));
                card.bottomMargin=dp(9);body.addView(item,card);
                item.setOnClickListener(v->{selectedClip=id;selectedMs=0;showClip();});
            }
        }catch(Exception e){body.addView(label("Cannot load recordings: "+e.getMessage(),13,false));}
    }
    private void checkMicrophone(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO);return;
        }
        startRecord();
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] result){
        super.onRequestPermissionsResult(code,permissions,result);
        if(code==REQUEST_AUDIO){
            if(result.length>0&&result[0]==PackageManager.PERMISSION_GRANTED)startRecord();
            else notice("Microphone permission is required to record sound");
        }
    }
    private void startRecord(){
        if(!canRecord()){
            notice("An active survey is required for recording");return;
        }
        if(recorder!=null){
            notice("Previous audio is still being saved");return;
        }
        String key=UUID.randomUUID().toString().substring(0,8);
        pendingFilename="clip_"+System.currentTimeMillis()+"_"+key+".wav";
        pendingId="AUDIO-"+UUID.randomUUID().toString();
        recordedAt=utc(System.currentTimeMillis());
        File file=new File(AcousticEvidence.directory(this),pendingFilename);
        recorder=new CallAudioRecorder();
        if(spectrum!=null)spectrum.resetLive();
        try{
            recorder.start(file,new CallAudioRecorder.Listener(){
                @Override public void onSamples(short[] audio){
                    ui.post(()->{
                        if(spectrum!=null&&!isFinishing()&&!isDestroyed())
                            spectrum.append(audio);
                    });
                }
                @Override public void onFinished(File wav,long duration,String error){
                    if(error==null&&duration>0){
                        try(FieldDb store=new FieldDb(getApplicationContext())){
                            ContentValues values=new ContentValues();
                            values.put("id",pendingId);values.put("event_id",eventId);
                            values.put("station_id",station);
                            values.put("taxon_group",group);
                            values.put("method",method);values.put("filename",wav.getName());
                            values.put("recorded_at_utc",recordedAt);
                            values.put("duration_ms",duration);
                            values.put("sample_rate_hz",CallAudioRecorder.SAMPLE_RATE);
                            values.put("channels",1);values.put("format","WAV_PCM16");
                            store.getWritableDatabase().insertOrThrow("acoustic_clips",null,values);
                        }catch(Exception fail){error="Could not save clip metadata: "+fail.getMessage();}
                    }
                    final String result=error;
                    ui.post(()->{
                        if(isFinishing()||isDestroyed())return;
                        if(result!=null)notice(result);
                        else notice("Sound saved · "+format(duration));
                        recorder=null;draw();
                    });
                }
            });
            if(recordButton!=null)recordButton.setText("■ Stop and save recording");
        }catch(Exception error){
            recorder=null;file.delete();
            notice("Recorder unavailable: "+error.getMessage());
        }
    }
    private void stopRecord(){
        if(recorder!=null)recorder.stop();
        if(recordButton!=null){
            recordButton.setText("Saving sound…");
            recordButton.setEnabled(false);
        }
    }
    private void showClip(){
        releasePlayer();layout("Review recording");
        final String clip=selectedClip;
        String filename=null,profile=null,date=null,outcome=null;
        try(Cursor c=database.getReadableDatabase().rawQuery(
            "SELECT c.filename,c.taxon_group,c.recorded_at_utc,c.duration_ms,"+
            "COALESCE(s.outcome,'active') FROM acoustic_clips c "+
            "LEFT JOIN surveys s ON s.id=c.event_id WHERE c.id=?",
            new String[]{clip})){
            if(c.moveToFirst()){
                filename=c.getString(0);profile=c.getString(1);
                date=c.getString(2);clipDuration=c.getLong(3);outcome=c.getString(4);
            }
        }
        if(!AcousticEvidence.safeName(filename)){
            body.addView(label("Clip metadata missing or invalid.",15,true));return;
        }
        final File file=new File(AcousticEvidence.directory(this),filename);
        body.addView(label(profile+" · "+date+"\nSurvey: "+outcome,14,true));
        spectrum=new CallSpectrogramView(this);
        spectrum.setContrast(spectrogramContrast);
        body.addView(spectrum,new LinearLayout.LayoutParams(-1,dp(260)));
        frequencyInfo=label("Tap the spectrogram: time and frequency appear here",12,true);
        frequencyInfo.setTextColor(UiKit.POND);
        body.addView(frequencyInfo);
        spectrum.setFrequencyListener((hz,ms)->{
            lastTappedHz=hz;
            frequencyInfo.setText(String.format(
                Locale.US,"Cursor %s · %.0f Hz (approximate)",format(ms),hz));
        });
        LinearLayout tools=new LinearLayout(this);
        Button less=UiKit.button(this,"− Zoom","secondary");
        Button more=UiKit.button(this,"+ Zoom","secondary");
        tools.addView(less,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout.LayoutParams zoomLp=new LinearLayout.LayoutParams(0,dp(48),1);
        zoomLp.leftMargin=dp(8);tools.addView(more,zoomLp);
        body.addView(tools);
        Button backward=UiKit.button(this,"‹ Earlier","secondary");
        Button forward=UiKit.button(this,"Later ›","secondary");
        LinearLayout pages=new LinearLayout(this);
        pages.addView(backward,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout.LayoutParams nextLp=new LinearLayout.LayoutParams(0,dp(48),1);
        nextLp.leftMargin=dp(8);pages.addView(forward,nextLp);body.addView(pages);
        TextView contrastTitle=label("Spectrogram contrast",12,true);
        body.addView(contrastTitle);
        SeekBar contrastBar=new SeekBar(this);
        contrastBar.setMax(350);contrastBar.setProgress(Math.round((spectrogramContrast-.5f)*100));
        body.addView(contrastBar);
        less.setOnClickListener(v->{
            spectrogramZoom=Math.max(1,spectrogramZoom/2);
            loadSpectrum(file,clip);
        });
        more.setOnClickListener(v->{
            spectrogramZoom=Math.min(16,spectrogramZoom*2);
            loadSpectrum(file,clip);
        });
        backward.setOnClickListener(v->{
            long[] w=AcousticViewport.window(clipDuration,selectedMs,spectrogramZoom);
            selectedMs=Math.max(0,w[0]-(w[1]-w[0])/2);
            loadSpectrum(file,clip);
        });
        forward.setOnClickListener(v->{
            long[] w=AcousticViewport.window(clipDuration,selectedMs,spectrogramZoom);
            selectedMs=Math.min(clipDuration,w[1]+(w[1]-w[0])/2);
            loadSpectrum(file,clip);
        });
        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar bar){}
            public void onStopTrackingTouch(SeekBar bar){}
            public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
                if(fromUser){
                    spectrogramContrast=.5f+progress/100f;
                    spectrum.setContrast(spectrogramContrast);
                }
            }
        });
        playbackTime=label("00:00 / "+format(clipDuration),14,true);
        body.addView(playbackTime);
        seek=new SeekBar(this);seek.setMax((int)Math.min(Integer.MAX_VALUE,clipDuration));
        body.addView(seek);
        spectrum.setPositionListener(fraction->{
            selectedMs=(long)(fraction*clipDuration);
            seek.setProgress((int)selectedMs);
            if(player!=null)player.seekTo((int)selectedMs);
            playbackTime.setText(format(selectedMs)+" / "+format(clipDuration));
        });
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar b){}
            public void onStopTrackingTouch(SeekBar b){}
            public void onProgressChanged(SeekBar b,int progress,boolean fromUser){
                if(fromUser){
                    selectedMs=progress;
                    spectrum.setCursor(progress/(float)Math.max(1,clipDuration));
                    if(player!=null)player.seekTo(progress);
                    playbackTime.setText(format(progress)+" / "+format(clipDuration));
                }
            }
        });
        LinearLayout actions=new LinearLayout(this);
        Button play=UiKit.button(this,"▶ Play / Pause","primary");
        actions.addView(play,new LinearLayout.LayoutParams(0,dp(52),1));
        Button label=UiKit.button(this,"Label call","secondary");
        LinearLayout.LayoutParams labelSize=new LinearLayout.LayoutParams(0,dp(52),1);
        labelSize.leftMargin=dp(8);actions.addView(label,labelSize);
        body.addView(actions);
        play.setOnClickListener(v->{
            try{
                if(player==null){
                    player=new MediaPlayer();player.setDataSource(file.getAbsolutePath());
                    player.prepare();
                    player.setOnCompletionListener(mp->{
                        spectrum.setCursor(1f);
                        seek.setProgress((int)clipDuration);
                    });
                }
                if(player.isPlaying()){
                    player.pause();
                    ui.removeCallbacks(updateCursor);
                }else{
                    player.start();
                    ui.removeCallbacks(updateCursor);
                    ui.post(updateCursor);
                }
            }catch(Exception e){notice("Playback failed: "+e.getMessage());releasePlayer();}
        });
        label.setOnClickListener(v->newLabel(clip));
        TextView labelHeader=label("Call annotations",17,true);
        body.addView(labelHeader);
        try(Cursor rows=database.getReadableDatabase().rawQuery(
            "SELECT start_ms,end_ms,call_label,COALESCE(notes,''),id,"+
            "COALESCE(taxon_key,''),observation_id,selected_frequency_hz "+
            "FROM acoustic_labels WHERE clip_id=? ORDER BY start_ms,id",new String[]{clip})){
            if(rows.getCount()==0)body.addView(label("No call labels yet.",13,false));
            while(rows.moveToNext()){
                long time=rows.getLong(0),until=rows.getLong(1);
                String species=rows.getString(2),notes=rows.getString(3);
                long annotationId=rows.getLong(4);
                String linkedTaxon=rows.getString(5);
                String linkedRecord=rows.isNull(6)?"":String.valueOf(rows.getLong(6));
                String frequency=rows.isNull(7)?"":String.format(Locale.US,
                    " · ~%.0f Hz",rows.getDouble(7));
                Button annotation=UiKit.button(this,format(time)+"–"+format(until)+
                    "  "+species+frequency+(linkedTaxon.isEmpty()?"":" · checklist linked")+
                    (linkedRecord.isEmpty()?"":" · observation #"+linkedRecord)+
                    "\nTap to play · hold to link evidence","secondary");
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(69));
                lp.bottomMargin=dp(7);body.addView(annotation,lp);
                annotation.setOnClickListener(v->{
                    selectedMs=time;seek.setProgress((int)time);
                    spectrum.setCursor(time/(float)Math.max(1,clipDuration));
                    if(player!=null)player.seekTo((int)time);
                });
                annotation.setOnLongClickListener(v->{
                    openLabelLinks(annotationId);return true;
                });
            }
        }
        // Analysis runs away from UI; archived clips remain playable without an active survey.
        loadSpectrum(file,clip);
    }
    /** Analyze the currently visible time window, preserving the WAV unchanged. */
    private void loadSpectrum(File file,String clip){
        final long[] bounds=AcousticViewport.window(clipDuration,selectedMs,spectrogramZoom);
        final int generation=++analysisGeneration;
        final CallSpectrogramView destination=spectrum;
        new Thread(()->{
            try{
                float[][] bands=CallSpectrogramView.readWav(file,bounds[0],bounds[1]);
                ui.post(()->{
                    if(generation!=analysisGeneration||!clip.equals(selectedClip)||
                        isDestroyed()||spectrum!=destination)return;
                    destination.setSaved(bands,clipDuration,bounds[0],bounds[1]);
                    destination.setCursor(selectedMs/(float)Math.max(1,clipDuration));
                    if(frequencyInfo!=null)
                        frequencyInfo.setText("Window "+format(bounds[0])+" to "+
                            format(bounds[1])+" · "+spectrogramZoom+"× zoom");
                });
            }catch(Exception e){ui.post(()->{
                if(generation==analysisGeneration)notice("Spectrogram: "+e.getMessage());
            });}
        },"LIKAS-Spectrogram-Window").start();
    }
    private void newLabel(String clip){
        LinearLayout form=column();form.setPadding(dp(16),dp(5),dp(16),dp(5));
        EditText name=new EditText(this);name.setHint("Call ID / species (or unidentified)");
        name.setSingleLine(true);form.addView(name);
        EditText start=new EditText(this);start.setInputType(InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL);
        start.setHint("Start in seconds");start.setText(String.format(Locale.US,"%.2f",selectedMs/1000d));
        form.addView(start);
        EditText end=new EditText(this);end.setInputType(InputType.TYPE_CLASS_NUMBER|
            InputType.TYPE_NUMBER_FLAG_DECIMAL);
        end.setHint("End in seconds");
        end.setText(String.format(Locale.US,"%.2f",
            Math.min(clipDuration,selectedMs+1000)/1000d));form.addView(end);
        EditText frequency=new EditText(this);
        frequency.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        frequency.setHint("Frequency Hz · estimate from tapped spectrogram");
        if(Double.isFinite(lastTappedHz))
            frequency.setText(String.format(Locale.US,"%.0f",lastTappedHz));
        form.addView(frequency);
        EditText notes=new EditText(this);notes.setHint("Call pattern or ID evidence (optional)");
        form.addView(notes);
        new AlertDialog.Builder(this).setTitle("Label recorded call")
            .setView(form).setNegativeButton("Cancel",null)
            .setPositiveButton("Save label",(dialog,which)->{
                try{
                    String species=name.getText().toString().trim();
                    if(species.isEmpty())throw new IllegalArgumentException("Call ID required");
                    long from=Math.round(Double.parseDouble(start.getText().toString())*1000),
                        to=Math.round(Double.parseDouble(end.getText().toString())*1000);
                    if(!CallLinkPolicy.usableLabelRange(from,to,clipDuration))
                        throw new IllegalArgumentException("Select a valid time interval");
                    ContentValues v=new ContentValues();
                    v.put("clip_id",clip);v.put("start_ms",from);v.put("end_ms",to);
                    v.put("call_label",species);
                    String hzText=frequency.getText().toString().trim();
                    if(!hzText.isEmpty()){
                        double hz=Double.parseDouble(hzText);
                        if(!Double.isFinite(hz)||hz<AcousticViewport.MIN_HZ||
                            hz>AcousticViewport.MAX_HZ)
                            throw new IllegalArgumentException("Select 85–17,000 Hz");
                        v.put("selected_frequency_hz",hz);
                    }
                    v.put("notes",notes.getText().toString().trim());
                    v.put("reviewed_at_utc",utc(System.currentTimeMillis()));
                    database.getWritableDatabase().insertOrThrow("acoustic_labels",null,v);
                    selectedMs=from;showClip();
                }catch(Exception e){notice("Label not saved: "+e.getMessage());}
            }).show();
    }
    /** Link a reviewed time range to the checklist and/or saved survey detection. */
    private void openLabelLinks(long id){
        new AlertDialog.Builder(this).setTitle("Call evidence links")
            .setItems(new String[]{"Choose checklist taxon","Link saved observation",
                "Clear checklist link","Clear observation link"},(dialog,which)->{
                if(which==0)lookupChecklist(id);
                else if(which==1)chooseObservation(id);
                else{
                    ContentValues v=new ContentValues();
                    v.putNull(which==2?"taxon_key":"observation_id");
                    v.put("review_status","unverified");
                    database.getWritableDatabase().update("acoustic_labels",v,
                        "id=?",new String[]{String.valueOf(id)});
                    showClip();
                }
            }).setNegativeButton("Close",null).show();
    }
    private String[] annotationContext(long labelId){
        try(Cursor c=database.getReadableDatabase().rawQuery(
            "SELECT c.event_id,c.taxon_group FROM acoustic_labels a "+
            "JOIN acoustic_clips c ON c.id=a.clip_id WHERE a.id=?",
            new String[]{String.valueOf(labelId)})){
            if(c.moveToFirst())return new String[]{c.getString(0),c.getString(1)};
        }
        throw new IllegalStateException("Saved call label not found");
    }
    private void lookupChecklist(long id){
        EditText query=new EditText(this);query.setSingleLine(true);
        query.setHint("Search scientific or common name");
        new AlertDialog.Builder(this).setTitle("Find checklist species").setView(query)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Find",(d,w)->{
                String value=query.getText().toString().trim();
                if(value.length()<2){notice("Enter at least two characters");return;}
                String[] context=annotationContext(id);
                java.util.ArrayList<String> keys=new java.util.ArrayList<>();
                java.util.ArrayList<String> names=new java.util.ArrayList<>();
                try(Cursor c=database.getReadableDatabase().rawQuery(
                    "SELECT taxon_key,scientific_name,COALESCE(common_name,'') "+
                    "FROM taxon_catalog WHERE taxon_group=? AND "+
                    "(scientific_name LIKE ? OR common_name LIKE ?) "+
                    "ORDER BY scientific_name LIMIT 50",
                    new String[]{context[1],"%"+value+"%","%"+value+"%"})){
                    while(c.moveToNext()){
                        keys.add(c.getString(0));
                        names.add(c.getString(1)+(c.getString(2).isEmpty()?"":" · "+c.getString(2)));
                    }
                }
                if(names.isEmpty()){notice("No matching checklist taxon. Keep a manual label.");return;}
                new AlertDialog.Builder(this).setTitle("Assign checklist identity")
                    .setItems(names.toArray(new String[0]),(dlg,index)->{
                        ContentValues v=new ContentValues();
                        v.put("taxon_key",keys.get(index));
                        v.put("review_status","linked_unverified");
                        database.getWritableDatabase().update("acoustic_labels",v,
                            "id=?",new String[]{String.valueOf(id)});
                        showClip();
                    }).setNegativeButton("Cancel",null).show();
            }).show();
    }
    private void chooseObservation(long labelId){
        String[] context=annotationContext(labelId);
        java.util.ArrayList<Long> recordIds=new java.util.ArrayList<>();
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        try(Cursor c=database.getReadableDatabase().rawQuery(
            "SELECT id,COALESCE(raw_identification,''),detected_at "+
            "FROM field_records WHERE event_id=? AND taxon_group=? AND deleted_at IS NULL "+
            "ORDER BY detected_at DESC LIMIT 100",context)){
            while(c.moveToNext()){
                recordIds.add(c.getLong(0));
                names.add("#"+c.getLong(0)+" · "+c.getString(1)+" · "+c.getString(2));
            }
        }
        if(names.isEmpty()){notice("No saved "+context[1]+" observations in this survey");return;}
        new AlertDialog.Builder(this).setTitle("Link saved survey observation")
            .setItems(names.toArray(new String[0]),(d,index)->{
                long record=recordIds.get(index);
                try(Cursor check=database.getReadableDatabase().rawQuery(
                    "SELECT event_id,taxon_group FROM field_records WHERE id=? "+
                    "AND deleted_at IS NULL",new String[]{String.valueOf(record)})){
                    if(!check.moveToFirst()||!CallLinkPolicy.sameSurveyAndGroup(
                        context[0],check.getString(0),context[1],check.getString(1))){
                        notice("Observation does not match this survey and taxon group");return;
                    }
                }
                ContentValues v=new ContentValues();
                v.put("observation_id",record);
                v.put("review_status","linked_unverified");
                database.getWritableDatabase().update("acoustic_labels",v,
                    "id=?",new String[]{String.valueOf(labelId)});
                showClip();
            }).setNegativeButton("Cancel",null).show();
    }
    private void releasePlayer(){
        if(player!=null){
            try{player.release();}catch(Exception ignored){}
            player=null;
        }
        seek=null;playbackTime=null;
    }
    @Override public void onBackPressed(){
        if(recorder!=null&&recorder.isCapturing()){stopRecord();return;}
        if(selectedClip!=null){selectedClip=null;draw();return;}
        super.onBackPressed();
    }
    @Override protected void onResume(){
        super.onResume();
    }
    @Override protected void onPause(){
        ui.removeCallbacks(updateCursor);
        if(recorder!=null&&recorder.isCapturing())recorder.stop();
        if(player!=null&&player.isPlaying())player.pause();
        super.onPause();
    }
    @Override protected void onDestroy(){
        if(recorder!=null)recorder.stop();
        releasePlayer();
        ui.removeCallbacksAndMessages(null);
        database.close();
        super.onDestroy();
    }
}
