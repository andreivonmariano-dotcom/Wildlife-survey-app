package org.phwildlife.survey;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Only owner-authorized Drive filenames may be mapped to a Book 01 taxon. */
public final class PhLibraryBulkPhotoImport {
    private static final String SOURCE="apo_owner_drive_photo_inventory_v1.json";
    private static Set<String> approvedNames;
    private PhLibraryBulkPhotoImport(){}

    private static synchronized Set<String> approved(Context context){
        if(approvedNames!=null)return approvedNames;
        Set<String> names=new HashSet<>();
        try(InputStream in=context.getAssets().open(SOURCE)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] bytes=new byte[4096];int count;
            while((count=in.read(bytes))!=-1){
                if(out.size()+count>160000)throw new IllegalStateException("Photo inventory is too large");
                out.write(bytes,0,count);
            }
            JSONArray items=new JSONObject(out.toString("UTF-8")).getJSONArray("items");
            for(int i=0;i<items.length();i++){
                String label=items.getJSONObject(i).getString("name");
                names.add(label.toLowerCase(Locale.ROOT));
            }
            if(names.size()!=333)throw new IllegalStateException("Owner photo inventory incomplete");
        }catch(Exception ex){names.clear();}
        approvedNames=names;
        return approvedNames;
    }
    public static String exactTaxon(Context context,String filename){
        if(filename==null)return null;
        String source=filename.trim();
        if(source.indexOf('/')>=0||source.indexOf('\\')>=0||
            !source.matches("(?i)[a-z][a-z0-9_]+\\.(jpg|jpeg|png|webp)"))
            return null;
        if(!approved(context).contains(source.toLowerCase(Locale.ROOT)))return null;
        int dot=source.lastIndexOf('.');
        String stem=source.substring(0,dot).replaceAll("__[0-9]{2,3}$","");
        String[] tokens=stem.split("_");
        if(tokens.length<2||tokens[0].isEmpty()||tokens[1].isEmpty())return null;
        String name=tokens[0]+" "+tokens[1];
        for(JSONObject row:PhLibraryBook01.entries(context)){
            String latin=row.optString("s");
            // Infraspecific epithets retain their source label, with no new
            // species-level identity invented by a fuzzy or genus-only match.
            if(name.equalsIgnoreCase(latin))return latin;
        }
        return null;
    }
    public static boolean isOwnerDriveFilename(Context context,String filename){
        return filename!=null&&approved(context).contains(
            filename.trim().toLowerCase(Locale.ROOT));
    }
}
