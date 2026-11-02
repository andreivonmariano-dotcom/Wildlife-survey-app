package org.phwildlife.survey;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.junit.Assert.*;

/** Run in a NEW instrumentation process after shell am force-stop. */
@RunWith(AndroidJUnit4.class)
public final class Rc92ProcessRestartInstrumentedTest {
    private static Button find(View v,String text){
        if(v instanceof Button&&text.equals(((Button)v).getText().toString()))
            return (Button)v;
        if(v instanceof ViewGroup){
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++){
                Button b=find(g.getChildAt(i),text);
                if(b!=null)return b;
            }
        }
        return null;
    }
    @Test public void forceStopThenRelaunchPreservesEventWavAndBackup()throws Exception{
        Context ctx=InstrumentationRegistry.getInstrumentation().getTargetContext();
        String event=null,filename=null;
        try(FieldDb db=new FieldDb(ctx);
            Cursor c=db.getReadableDatabase().rawQuery(
                "SELECT s.id,a.filename,s.start_gps_status,s.ended "+
                "FROM surveys s JOIN acoustic_clips a ON a.event_id=s.id "+
                "WHERE s.observer_name=?",
                new String[]{Rc92UnverifiedDeviceInstrumentedTest.OBSERVER})){
            assertTrue("Pre-stop survey and microphone WAV must survive",c.moveToFirst());
            event=c.getString(0);filename=c.getString(1);
            assertEquals(SurveyStartGpsPolicy.MISSING,c.getString(2));
            assertTrue("Survey was not accidentally completed",c.isNull(3));
            assertEquals("One survey/clip pair",1,c.getCount());
        }
        File audio=new File(AcousticEvidence.directory(ctx),filename);
        assertTrue(audio.isFile()&&audio.length()>44);
        try(ActivityScenario<MainActivity> session=ActivityScenario.launch(
                new Intent(ctx,MainActivity.class).putExtra("resume",true))){
            session.onActivity(a->{
                Button end=find(a.getWindow().getDecorView(),"End Survey");
                assertNotNull("Recorder can resume after process death",end);
                assertTrue(end.isShown()&&end.isEnabled());
            });
        }
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        String status=FieldBackupArchive.export(ctx,bytes);
        assertTrue("Backup confirms modern schema",status.toLowerCase().contains("verified"));
        boolean found=false;
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null){
                if(entry.getName().equals("media/acoustic/"+filename)){
                    found=true;
                    ByteArrayOutputStream copy=new ByteArrayOutputStream();
                    byte[] buf=new byte[8192];int n;
                    while((n=zip.read(buf))!=-1)copy.write(buf,0,n);
                    assertEquals("WAV retained byte-for-byte in backup",audio.length(),copy.size());
                }
                zip.closeEntry();
            }
        }
        assertTrue("Backup ZIP includes recorded audio",found);
    }
}
