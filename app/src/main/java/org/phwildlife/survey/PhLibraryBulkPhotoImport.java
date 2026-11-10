package org.phwildlife.survey;

import android.content.Context;
import org.json.JSONObject;
import java.util.Locale;

/** Strict filename crosswalk for user-selected bulk photos, not an ID engine. */
public final class PhLibraryBulkPhotoImport {
    private PhLibraryBulkPhotoImport(){}
    public static String exactTaxon(Context c,String filename){
        if(filename==null)return null;
        String source=filename.trim();
        if(!source.matches("(?i).+\\.(jpg|jpeg|png|webp)"))return null;
        int suffix=source.lastIndexOf('.');
        String stem=source.substring(0,suffix).trim();
        if(stem.isEmpty()||stem.toLowerCase(Locale.ROOT).contains(" cf. ")||
            stem.toLowerCase(Locale.ROOT).matches(".*\\bsp\\.?\\b.*"))return null;
        for(JSONObject row:PhLibraryBook01.entries(c)){
            String latin=row.optString("s");
            if(stem.equalsIgnoreCase(latin))return latin;
        }
        return null;
    }
}
