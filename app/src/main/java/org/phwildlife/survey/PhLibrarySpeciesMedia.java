package org.phwildlife.survey;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

/** Offline species photos. User-selected files stay in private app storage, never the APK. */
public final class PhLibrarySpeciesMedia {
    private static final String PREFS="phlibrary_personal_species_photos_v1";
    private static final String DIR="phlibrary_species_photos";
    private PhLibrarySpeciesMedia(){}
    private static int dp(Activity a,float n){return PhLibrary2Ui.dp(a,n);}
    private static String key(String scientific){
        return scientific==null?"":scientific.trim().toLowerCase(Locale.ROOT);
    }
    private static File privateFile(Context c,String scientific){
        String file=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(key(scientific),"");
        if(!file.matches("species_[a-f0-9]{32}\\.jpg"))return null;
        File folder=new File(c.getFilesDir(),DIR);
        File image=new File(folder,file);
        return image.isFile()&&image.length()>0?image:null;
    }
    public static boolean hasPersonalPhoto(Context c,String scientific){
        return privateFile(c,scientific)!=null;
    }
    public static boolean hasApprovedPhoto(Activity a,String scientific){
        return OfflinePhotoEvidence.hasOfflineTaxon(a,scientific);
    }
    private static Bitmap privateBitmap(Context c,String scientific){
        File image=privateFile(c,scientific);
        if(image==null)return null;
        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(image.getAbsolutePath(),opts);
        if(opts.outWidth<1||opts.outHeight<1)return null;
        opts.inJustDecodeBounds=false;
        opts.inPreferredConfig=Bitmap.Config.RGB_565;
        opts.inSampleSize=1;
        while(Math.max(opts.outWidth,opts.outHeight)/opts.inSampleSize>640)
            opts.inSampleSize*=2;
        return BitmapFactory.decodeFile(image.getAbsolutePath(),opts);
    }
    public static View placeholder(Activity a,String scientific){
        FrameLayout frame=new FrameLayout(a);
        frame.setBackground(PhLibrary2Ui.bg(a,0xFFE3E6E3,0xFFCDD3CE,13));
        TextView question=PhLibrary2Ui.text(a,"?",38,0xFF8A948D,false,false);
        question.setGravity(Gravity.CENTER);
        question.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(question,new FrameLayout.LayoutParams(-1,-1));
        frame.setContentDescription("No photograph available for "+scientific);
        frame.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        return frame;
    }
    public static View photoOrPlaceholder(Activity a,String scientific){
        Bitmap personal=privateBitmap(a,scientific);
        if(personal!=null){
            ImageView image=new ImageView(a);image.setImageBitmap(personal);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setContentDescription("Device-added photograph of "+scientific+
                ". Identification has not been independently verified.");
            return image;
        }
        ImageView bundled=OfflinePhotoEvidence.thumbnailForTaxon(a,scientific);
        if(bundled!=null){
            bundled.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return bundled;
        }
        return placeholder(a,scientific);
    }
    /** Reader plate, preserving approved Commons attribution and a simple missing-photo state. */
    public static void addReaderPlate(Activity a,LinearLayout parent,String scientific){
        if(hasPersonalPhoto(a,scientific)){
            View plate=photoOrPlaceholder(a,scientific);
            LinearLayout.LayoutParams image=new LinearLayout.LayoutParams(-1,dp(a,188));
            image.topMargin=dp(a,12);parent.addView(plate,image);
            TextView source=PhLibrary2Ui.text(a,"Device photo · selected by user",12,
                PhLibrary2Ui.MUTED,false,false);
            source.setPadding(dp(a,3),dp(a,5),0,dp(a,7));
            parent.addView(source);
        }else if(hasApprovedPhoto(a,scientific)){
            OfflinePhotoEvidence.addPhLibraryPlate(a,parent,scientific);
        }else{
            View missing=placeholder(a,scientific);
            LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,dp(a,158));
            mp.topMargin=dp(a,12);parent.addView(missing,mp);
        }
        if(a instanceof TaxonDetailActivity){
            TaxonDetailActivity detail=(TaxonDetailActivity)a;
            boolean personal=hasPersonalPhoto(a,scientific);
            TextView importButton=PhLibrary2Ui.action(a,
                personal?"Change device photo":"Add photo from device or Drive",
                ()->detail.choosePrivateSpeciesPhoto(scientific),false);
            LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(a,48));
            ap.topMargin=dp(a,7);parent.addView(importButton,ap);
            if(personal){
                TextView remove=PhLibrary2Ui.action(a,"Remove device photo",()->
                    detail.confirmRemovePrivateSpeciesPhoto(scientific),false);
                LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(a,48));
                rp.topMargin=dp(a,5);parent.addView(remove,rp);
            }
        }
    }
    /** Decode a selected file with a bounded pixel size. Re-encoding strips EXIF/GPS. */
    public static void importPrivate(Context c,String scientific,Uri source)throws Exception{
        if(scientific==null||scientific.trim().isEmpty()||source==null)
            throw new IllegalArgumentException("A species and photograph are required");
        BitmapFactory.Options bounds=new BitmapFactory.Options();
        bounds.inJustDecodeBounds=true;
        try(InputStream stream=c.getContentResolver().openInputStream(source)){
            if(stream==null)throw new IllegalArgumentException("Unable to open selected photograph");
            BitmapFactory.decodeStream(stream,null,bounds);
        }
        if(bounds.outWidth<1||bounds.outHeight<1||
            bounds.outWidth>22000||bounds.outHeight>22000)
            throw new IllegalArgumentException("Unsupported photograph dimensions");
        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inPreferredConfig=Bitmap.Config.RGB_565;
        opts.inSampleSize=1;
        while(Math.max(bounds.outWidth,bounds.outHeight)/opts.inSampleSize>1280)
            opts.inSampleSize*=2;
        Bitmap decoded;
        try(InputStream stream=c.getContentResolver().openInputStream(source)){
            if(stream==null)throw new IllegalArgumentException("Selected photograph is unavailable");
            decoded=BitmapFactory.decodeStream(stream,null,opts);
        }
        if(decoded==null)throw new IllegalArgumentException("Could not decode photograph");
        File directory=new File(c.getFilesDir(),DIR);
        if(!directory.isDirectory()&&!directory.mkdirs()){
            decoded.recycle();throw new IllegalStateException("Could not create private photo directory");
        }
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(
            key(scientific).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hex=new StringBuilder();
        for(int i=0;i<16;i++)hex.append(String.format(Locale.ROOT,"%02x",hash[i]&255));
        String name="species_"+hex+".jpg";
        File temporary=new File(directory,name+".tmp");
        File target=new File(directory,name);
        boolean written=false;
        try(FileOutputStream out=new FileOutputStream(temporary)){
            written=decoded.compress(Bitmap.CompressFormat.JPEG,83,out);
            out.flush();
        }finally{decoded.recycle();}
        if(!written||temporary.length()<100){
            temporary.delete();throw new IllegalStateException("Photo conversion failed");
        }
        if(!temporary.renameTo(target)){
            temporary.delete();throw new IllegalStateException("Could not store private photo");
        }
        if(!c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
            .edit().putString(key(scientific),name).commit())
            throw new IllegalStateException("Could not register private photo");
    }
    public static boolean removePrivate(Context c,String scientific){
        File original=privateFile(c,scientific);
        if(!c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .remove(key(scientific)).commit())return false;
        return original==null||original.delete();
    }
}
