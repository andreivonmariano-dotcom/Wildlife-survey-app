package org.phwildlife.survey;

import android.content.Context;
import android.content.SharedPreferences;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Private, edition-agnostic citation bookmarks; original bibliography is untouched. */
public final class ApoReferenceBookmarks {
    private static final String PREFERENCES="apo_offline_research_saved_v1";
    private static final String SAVED="citations";
    private ApoReferenceBookmarks(){}

    static String id(String citation){
        String normal=citation==null?"":citation.trim().replaceAll("\\s+"," ")
            .toLowerCase(Locale.ROOT);
        if(normal.isEmpty())return "";
        try{
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(
                normal.getBytes(StandardCharsets.UTF_8));
            StringBuilder key=new StringBuilder(hash.length*2);
            for(byte b:hash)key.append(String.format(Locale.ROOT,"%02x",b&255));
            return key.toString();
        }catch(Exception error){
            throw new IllegalStateException("Unable to identify reference",error);
        }
    }
    private static SharedPreferences storage(Context context){
        return context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
    }
    public static boolean isSaved(Context context,String citation){
        String id=id(citation);
        return !id.isEmpty()&&storage(context).getStringSet(SAVED,
            java.util.Collections.emptySet()).contains(id);
    }
    public static void setSaved(Context context,String citation,boolean saved){
        String id=id(citation);
        if(id.isEmpty())return;
        SharedPreferences prefs=storage(context);
        // The returned set may be backed by preferences; always copy before editing.
        Set<String> values=new HashSet<>(prefs.getStringSet(SAVED,
            java.util.Collections.emptySet()));
        if(saved)values.add(id);
        else values.remove(id);
        prefs.edit().putStringSet(SAVED,values).apply();
    }
    public static int count(Context context){
        return storage(context).getStringSet(SAVED,
            java.util.Collections.emptySet()).size();
    }
}
