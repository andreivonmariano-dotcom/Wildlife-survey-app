package org.phwildlife.survey;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/** Device-local reading collection; independent of field surveys and taxon source data. */
public final class PhLibraryBookmarks {
    private static final String PREFS="phlibrary_reader_collection_v1";
    private static final String KEY="saved_scientific_names";
    private PhLibraryBookmarks(){}
    private static SharedPreferences store(Context c){
        return c.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }
    public static boolean saved(Context c,String name){
        return store(c).getStringSet(KEY,new HashSet<>()).contains(name);
    }
    public static Set<String> all(Context c){
        return new HashSet<>(store(c).getStringSet(KEY,new HashSet<>()));
    }
    public static boolean toggle(Context c,String name){
        if(name==null||name.trim().isEmpty())return false;
        Set<String> values=all(c);
        boolean added=values.add(name);
        if(!added)values.remove(name);
        store(c).edit().putStringSet(KEY,values).apply();
        return added;
    }
}
