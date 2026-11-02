package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only recent field observations. The draft is deliberately excluded:
 * the only countable records are persisted, non-deleted field_records.
 * Corrections are shown from the existing append-only record_edits audit.
 */
public final class ObservationTimeline {
    private ObservationTimeline(){}

    public static final class Item {
        public final long id;
        public final String label,group,detectedAt;
        public final int individuals;
        public final boolean corrected;
        Item(long id,String label,String group,String detectedAt,int individuals,boolean corrected){
            this.id=id;this.label=label;this.group=group;
            this.detectedAt=detectedAt;this.individuals=individuals;
            this.corrected=corrected;
        }
    }
    public static final class Snapshot {
        public final int savedCount;
        public final List<Item> recent;
        Snapshot(int count,List<Item> items){savedCount=count;recent=items;}
    }
    public static Snapshot load(SQLiteDatabase database,String eventId,int limit){
        if(database==null||eventId==null||eventId.trim().isEmpty())
            throw new IllegalArgumentException("Survey event is required");
        int count=0;
        try(Cursor c=database.rawQuery(
            "SELECT COUNT(*) FROM field_records WHERE event_id=? AND deleted_at IS NULL",
            new String[]{eventId})){
            if(c.moveToFirst())count=c.getInt(0);
        }
        ArrayList<Item> results=new ArrayList<>();
        String sql="SELECT r.id,"+
            "COALESCE(NULLIF(TRIM(r.raw_identification),''),"+
            "NULLIF(TRIM(r.scientific_name),''),'Unidentified record'),"+
            "COALESCE(r.taxon_group,''),COALESCE(r.detected_at,''),"+
            "COALESCE(r.individual_count,0),"+
            "EXISTS(SELECT 1 FROM record_edits e WHERE e.record_id=r.id AND e.action='edit') "+
            "FROM field_records r WHERE r.event_id=? AND r.deleted_at IS NULL "+
            "ORDER BY r.detected_at DESC,r.id DESC LIMIT ?";
        try(Cursor c=database.rawQuery(sql,new String[]{
            eventId,String.valueOf(Math.max(0,Math.min(limit,8)))})){
            while(c.moveToNext())results.add(new Item(
                c.getLong(0),c.getString(1),c.getString(2),
                c.getString(3),c.getInt(4),c.getInt(5)!=0));
        }
        return new Snapshot(count,results);
    }
}
