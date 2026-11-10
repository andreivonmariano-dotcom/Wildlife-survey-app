package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** Recently saved raw identifications, offered as names only. Never replay effort or evidence. */
public final class RecentTaxa {
    private RecentTaxa(){}
    public static String[] names(SQLiteDatabase db,String group){
        ArrayList<String> names=new ArrayList<>();
        if(db==null||group==null||group.trim().isEmpty())return new String[0];
        try(Cursor c=db.rawQuery(
            "SELECT TRIM(raw_identification) FROM field_records "+
            "WHERE taxon_group=? AND TRIM(COALESCE(raw_identification,''))<>'' "+
            "GROUP BY TRIM(raw_identification) ORDER BY MAX(id) DESC LIMIT 6",
            new String[]{group})){
            while(c.moveToNext())names.add(c.getString(0));
        }catch(Exception ignored){
            // A damaged or historical record should never block field capture.
        }
        return names.toArray(new String[0]);
    }
}
