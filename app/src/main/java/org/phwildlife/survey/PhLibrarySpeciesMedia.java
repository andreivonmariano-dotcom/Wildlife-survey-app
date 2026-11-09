package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;

/** Private, source-restricted and EXIF-free PHLibrary photos from the owner's Drive. */
public final class PhLibrarySpeciesMedia {
    private static final String PREFS="phlibrary_owner_drive_gallery_v1";
    private static final String DIR="phlibrary_owner_species";
    private PhLibrarySpeciesMedia(){}
    private static int dp(Activity a,float n){return PhLibrary2Ui.dp(a,n);}
    private static String key(String scientific){
        return scientific==null?"":scientific.trim().toLowerCase(Locale.ROOT);
    }
    private static File folder(Context c){return new File(c.getFilesDir(),DIR);}
    private static boolean safeFile(String name){
        return name.matches("owner_[0-9a-f]{20}_[0-9a-f]{12}\\.jpg");
    }
    private static String hex(byte[] bytes){
        StringBuilder out=new StringBuilder();
        for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));
        return out.toString();
    }
    private static String sha(String value)throws Exception{
        return hex(MessageDigest.getInstance("SHA-256").digest(
            value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
    private static final class Photo {
        final String file,label;
        Photo(String file,String label){this.file=file;this.label=label;}
    }
    private static ArrayList<Photo> images(Context c,String scientific){
        ArrayList<Photo> result=new ArrayList<>();
        String raw=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
            .getString(key(scientific),"[]");
        try{
            JSONArray list=new JSONArray(raw);
            for(int i=0;i<list.length();i++){
                JSONObject item=list.optJSONObject(i);
                if(item==null)continue;
                String name=item.optString("file"),label=item.optString("source");
                if(!safeFile(name)||!PhLibraryBulkPhotoImport.isOwnerDriveFilename(c,label))
                    continue;
                File file=new File(folder(c),name);
                if(file.isFile()&&file.length()>100)result.add(new Photo(name,label));
            }
        }catch(Exception ignored){}
        return result;
    }
    public static boolean hasPersonalPhoto(Context c,String scientific){
        return !images(c,scientific).isEmpty();
    }
    /** Historical name retained for callers; external stock photos are never eligible. */
    public static boolean hasApprovedPhoto(Activity a,String scientific){
        return hasPersonalPhoto(a,scientific);
    }
    public static int photoCount(Context c,String scientific){
        return images(c,scientific).size();
    }
    private static Bitmap decodeForView(File photo,int requestedMax){
        BitmapFactory.Options bounds=new BitmapFactory.Options();
        bounds.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(photo.getAbsolutePath(),bounds);
        if(bounds.outWidth<1||bounds.outHeight<1)return null;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPreferredConfig=Bitmap.Config.ARGB_8888;
        int sample=1;
        while(Math.max(bounds.outWidth,bounds.outHeight)/(sample*2)>requestedMax)
            sample*=2;
        options.inSampleSize=sample;
        return BitmapFactory.decodeFile(photo.getAbsolutePath(),options);
    }
    private static ImageView image(Activity a,Photo photo,int size){
        ImageView view=new ImageView(a);
        Bitmap bitmap=decodeForView(new File(folder(a),photo.file),size);
        if(bitmap!=null)view.setImageBitmap(bitmap);
        view.setBackgroundColor(0xFFE3E6E3);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setContentDescription("Owner's Google Drive photograph "+photo.label+
            ". Filename-based identification, not independently reviewed.");
        return view;
    }
    public static View placeholder(Activity a,String scientific){
        FrameLayout frame=new FrameLayout(a);
        frame.setBackground(PhLibrary2Ui.bg(a,0xFFE3E6E3,0xFFCDD3CE,13));
        TextView question=PhLibrary2Ui.text(a,"?",38,0xFF8A948D,false,false);
        question.setGravity(Gravity.CENTER);
        question.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(question,new FrameLayout.LayoutParams(-1,-1));
        frame.setContentDescription("No owner Drive photograph available for "+scientific);
        frame.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        return frame;
    }
    public static View photoOrPlaceholder(Activity a,String scientific){
        ArrayList<Photo> list=images(a,scientific);
        if(!list.isEmpty())return image(a,list.get(0),1600);
        return placeholder(a,scientific);
    }
    /** High-resolution, uncropped account plate with optional alternate owner photographs. */
    public static void addReaderPlate(Activity a,LinearLayout parent,String scientific){
        ArrayList<Photo> list=images(a,scientific);
        if(list.isEmpty()){
            View blank=placeholder(a,scientific);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,188));
            lp.topMargin=dp(a,12);
            parent.addView(blank,lp);
        }else{
            ImageView plate=image(a,list.get(0),2000);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(a,272));
            lp.topMargin=dp(a,12);parent.addView(plate,lp);
            TextView source=PhLibrary2Ui.text(a,
                "Owner's Drive photograph · "+list.size()+
                (list.size()==1?" image":" images"),12,PhLibrary2Ui.MUTED,false,false);
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);
            sp.topMargin=dp(a,5);parent.addView(source,sp);
            if(list.size()>1){
                HorizontalScrollView scroll=new HorizontalScrollView(a);
                scroll.setHorizontalScrollBarEnabled(false);
                LinearLayout strip=new LinearLayout(a);
                strip.setOrientation(LinearLayout.HORIZONTAL);
                for(Photo photo:list){
                    ImageView thumb=image(a,photo,280);
                    thumb.setClickable(true);thumb.setFocusable(true);
                    thumb.setOnClickListener(v->{
                        Bitmap selected=decodeForView(new File(folder(a),photo.file),2000);
                        if(selected!=null)plate.setImageBitmap(selected);
                        source.setText("Owner's Drive · "+photo.label);
                    });
                    LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(dp(a,96),dp(a,78));
                    tp.rightMargin=dp(a,6);
                    strip.addView(thumb,tp);
                }
                scroll.addView(strip);
                LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(-1,dp(a,82));
                slp.topMargin=dp(a,9);parent.addView(scroll,slp);
            }
        }
        if(a instanceof TaxonDetailActivity){
            TaxonDetailActivity detail=(TaxonDetailActivity)a;
            TextView add=PhLibrary2Ui.action(a,
                "Add owner's Drive photo",()->detail.choosePrivateSpeciesPhoto(scientific),false);
            LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(a,48));
            ap.topMargin=dp(a,8);parent.addView(add,ap);
            if(!list.isEmpty()){
                TextView remove=PhLibrary2Ui.action(a,"Remove Drive photos",()->
                    detail.confirmRemovePrivateSpeciesPhoto(scientific),false);
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(a,48));
                rp.topMargin=dp(a,5);parent.addView(remove,rp);
            }
        }
    }
    public static void importPrivate(Context c,String scientific,Uri uri)throws Exception{
        if(uri==null)throw new IllegalArgumentException("No source photograph selected");
        String name="";
        try(Cursor cursor=c.getContentResolver().query(uri,
            new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(cursor!=null&&cursor.moveToFirst())name=cursor.getString(0);
        }catch(Exception ignored){}
        String matched=PhLibraryBulkPhotoImport.exactTaxon(c,name);
        if(matched==null||!matched.equalsIgnoreCase(scientific))
            throw new IllegalArgumentException("Choose a photo named in the owner's Drive inventory for this species");
        try(InputStream stream=c.getContentResolver().openInputStream(uri)){
            if(stream==null)throw new IllegalArgumentException("Source photo unavailable");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            byte[] buffer=new byte[16384];int count;
            while((count=stream.read(buffer))!=-1){
                if(bytes.size()+count>34000000)
                    throw new IllegalArgumentException("Photo is over the 34 MB import limit");
                bytes.write(buffer,0,count);
            }
            importPrivateBytes(c,scientific,name,bytes.toByteArray());
        }
    }
    /** Save a standalone, high-detail JPEG. Metadata never leaves the original Drive file. */
    public static synchronized void importPrivateBytes(
        Context c,String scientific,String sourceName,byte[] bytes)throws Exception{
        String matched=PhLibraryBulkPhotoImport.exactTaxon(c,sourceName);
        if(matched==null||!matched.equalsIgnoreCase(scientific))
            throw new IllegalArgumentException("Unrecognized owner Drive filename");
        if(bytes==null||bytes.length<128||bytes.length>34000000)
            throw new IllegalArgumentException("Invalid source photograph size");
        BitmapFactory.Options bounds=new BitmapFactory.Options();
        bounds.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);
        if(bounds.outWidth<1||bounds.outHeight<1||
            bounds.outWidth>22000||bounds.outHeight>22000||
            (long)bounds.outWidth*bounds.outHeight>150000000)
            throw new IllegalArgumentException("Invalid photograph dimensions");
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPreferredConfig=Bitmap.Config.ARGB_8888;
        int sample=1;
        while(Math.max(bounds.outWidth,bounds.outHeight)/sample>4200)
            sample*=2;
        options.inSampleSize=sample;
        Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        if(bitmap==null)throw new IllegalArgumentException("Could not decode owner photograph");
        // No upscaling, modest reduction only for very large originals.
        int longest=Math.max(bitmap.getWidth(),bitmap.getHeight());
        if(longest>2400){
            double ratio=2400d/longest;
            Bitmap scaled=Bitmap.createScaledBitmap(bitmap,
                Math.max(1,(int)Math.round(bitmap.getWidth()*ratio)),
                Math.max(1,(int)Math.round(bitmap.getHeight()*ratio)),true);
            if(scaled!=bitmap){bitmap.recycle();bitmap=scaled;}
        }
        File destination=folder(c);
        if(!destination.isDirectory()&&!destination.mkdirs()){
            bitmap.recycle();throw new IllegalStateException("Photo storage unavailable");
        }
        String filename="owner_"+sha(key(scientific)).substring(0,20)+"_"+
            sha(sourceName.toLowerCase(Locale.ROOT)).substring(0,12)+".jpg";
        File temp=new File(destination,filename+".tmp"),target=new File(destination,filename);
        boolean written;
        try(FileOutputStream out=new FileOutputStream(temp)){
            written=bitmap.compress(Bitmap.CompressFormat.JPEG,91,out);
            out.flush();
        }finally{bitmap.recycle();}
        if(!written||temp.length()<128){
            temp.delete();throw new IllegalStateException("Could not encode owner photograph");
        }
        if(!temp.renameTo(target)){
            temp.delete();throw new IllegalStateException("Photo file could not be saved");
        }
        ArrayList<Photo> current=images(c,scientific);
        boolean exists=false;
        for(Photo photo:current)if(photo.file.equals(filename))exists=true;
        if(!exists)current.add(new Photo(filename,sourceName));
        JSONArray manifest=new JSONArray();
        for(Photo photo:current){
            JSONObject entry=new JSONObject();
            entry.put("file",photo.file);entry.put("source",photo.label);
            manifest.put(entry);
        }
        if(!c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putString(key(scientific),manifest.toString()).commit())
            throw new IllegalStateException("Photo index could not be saved");
    }
    /** Removes only generated app-side Drive photos, never the original Drive files. */
    public static synchronized boolean removePrivate(Context c,String scientific){
        ArrayList<Photo> photos=images(c,scientific);
        if(!c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .remove(key(scientific)).commit())return false;
        boolean ok=true;
        for(Photo photo:photos){
            File f=new File(folder(c),photo.file);
            if(f.exists()&&!f.delete())ok=false;
        }
        return ok;
    }
}
