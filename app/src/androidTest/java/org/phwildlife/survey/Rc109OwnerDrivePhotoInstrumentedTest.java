package org.phwildlife.survey;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.Assert.*;

/** Native owner-only media, quality, and no-clear-data upgrade coverage. */
@RunWith(AndroidJUnit4.class)
public final class Rc109OwnerDrivePhotoInstrumentedTest {
    private static final String ROW="QA_RC109_DRIVE_UPGRADE_EVENT";
    private Context app(){return ApplicationProvider.getApplicationContext();}
    private int version()throws Exception{
        Context c=app();
        return (int)c.getPackageManager().getPackageInfo(c.getPackageName(),0)
            .getLongVersionCode();
    }
    private byte[] jpeg(){
        Bitmap bitmap=Bitmap.createBitmap(800,600,Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xff41644b);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,95,out));
        bitmap.recycle();
        return out.toByteArray();
    }
    @Test public void aOwnerFilenameInventoryIsStrict(){
        assertEquals("Acerodon jubatus",
            PhLibraryBulkPhotoImport.exactTaxon(app(),"Acerodon_jubatus__01.jpg"));
        assertEquals("Acerodon jubatus",
            PhLibraryBulkPhotoImport.exactTaxon(app(),"Acerodon_jubatus__04.jpg"));
        assertEquals("Ahaetulla prasina",
            PhLibraryBulkPhotoImport.exactTaxon(app(),"Ahaetulla_prasina_preocularis.jpg"));
        assertNull(PhLibraryBulkPhotoImport.exactTaxon(app(),"other_photo.jpg"));
        assertNull(PhLibraryBulkPhotoImport.exactTaxon(app(),"species_eagle.jpg"));
        assertNull(PhLibraryBulkPhotoImport.exactTaxon(app(),"Acerodon_jubatus__05.jpg"));
        assertNull(PhLibraryBulkPhotoImport.exactTaxon(app(),"../../Acerodon_jubatus__01.jpg"));
    }
    @Test public void bOwnerArchiveImportsAllAlternatesAndStripsUnapprovedImages()throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(data)){
            for(String name:new String[]{"Acerodon_jubatus__01.jpg",
                "Acerodon_jubatus__02.jpg","species_eagle.jpg"}){
                zip.putNextEntry(new ZipEntry(name));zip.write(jpeg());zip.closeEntry();
            }
        }
        try{
            PhLibraryDrivePackImport.Result result=PhLibraryDrivePackImport.importStream(
                app(),new ByteArrayInputStream(data.toByteArray()));
            assertEquals(2,result.imported);
            assertEquals(1,result.unmatched);
            assertEquals(0,result.failed);
            assertEquals(2,PhLibrarySpeciesMedia.photoCount(app(),"Acerodon jubatus"));
            assertTrue(PhLibrarySpeciesMedia.hasPersonalPhoto(app(),"Acerodon jubatus"));
            assertFalse(PhLibrarySpeciesMedia.hasPersonalPhoto(app(),"Aviceda jerdoni"));
        }finally{
            assertTrue(PhLibrarySpeciesMedia.removePrivate(app(),"Acerodon jubatus"));
        }
    }
    @Test public void cDailyTaxonUsesOnlyOwnedPhotographs()throws Exception{
        android.app.Activity activity=new android.app.Activity();
        // The daily selector requires the full Activity asset/Book context.
        try(androidx.test.core.app.ActivityScenario<WelcomeActivity> scenario=
            androidx.test.core.app.ActivityScenario.launch(WelcomeActivity.class)){
            scenario.onActivity(a->{
                try{
                    PhLibrarySpeciesMedia.importPrivateBytes(a,"Acerodon jubatus",
                        "Acerodon_jubatus__01.jpg",jpeg());
                    org.json.JSONObject daily=ApoSpeciesOfDay.select(a,
                        LocalDate.now(ZoneId.of("Asia/Manila")));
                    assertNotNull(daily);
                    assertTrue(PhLibrarySpeciesMedia.hasPersonalPhoto(a,daily.optString("s")));
                    View card=ApoSpeciesOfDay.create(a,name->{});
                    assertNotNull(card);
                    assertTrue(card.getContentDescription().toString()
                        .contains("IUCN global status"));
                }catch(Exception ex){throw new AssertionError(ex);}
                finally{PhLibrarySpeciesMedia.removePrivate(a,"Acerodon jubatus");}
            });
        }
    }
    @Test public void dThirdPartyPhotosAreAbsentFromApk(){
        assertFalse(OfflinePhotoEvidence.hasOfflineTaxon(
            new android.app.Activity(),"Pithecophaga jefferyi"));
    }
    @Test public void eSeedSignedRc107Workspace()throws Exception{
        assertEquals(167,version());
        try(FieldDb helper=new FieldDb(app())){
            SQLiteDatabase db=helper.getWritableDatabase();
            assertEquals(29,db.getVersion());
            ContentValues row=new ContentValues();
            row.put("id",ROW);row.put("method","opportunistic");
            row.put("started","2026-11-10T08:00:00Z");
            row.put("project_id","default");row.put("taxon_group","Birds");
            row.put("survey_profile","Birds");row.put("outcome","incomplete");
            row.put("complete",0);row.put("ended","2026-11-10T08:05:00Z");
            db.insertWithOnConflict("surveys",null,row,SQLiteDatabase.CONFLICT_IGNORE);
        }
    }
    @Test public void fSignedRc109UpgradePreservesWorkspace()throws Exception{
        assertEquals(169,version());
        try(FieldDb helper=new FieldDb(app())){
            SQLiteDatabase db=helper.getReadableDatabase();
            assertEquals(29,db.getVersion());
            try(Cursor c=db.rawQuery("SELECT outcome,complete FROM surveys WHERE id=?",
                new String[]{ROW})){
                assertTrue("RC107 record disappeared during upgrade",c.moveToFirst());
                assertEquals("incomplete",c.getString(0));
                assertEquals(0,c.getInt(1));
            }
        }
    }
}
