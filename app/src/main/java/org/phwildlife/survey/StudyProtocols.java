package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Study-level minimum requirements are snapshotted when a survey begins. */
public final class StudyProtocols {
    private StudyProtocols(){}
    public static final class Rule {
        public final String name;
        public final int visits,minutes,units;
        public final boolean gps,photo;
        Rule(String name,int visits,int minutes,int units,boolean gps,boolean photo){
            this.name=name;this.visits=visits;this.minutes=minutes;this.units=units;
            this.gps=gps;this.photo=photo;
        }
    }
    public static boolean passes(Rule rule,int visits,long effortMs,int units,
            boolean hasVerifiedGps,int photoCount){
        if(rule==null)return true;
        return visits>=rule.visits&&effortMs>=rule.minutes*60_000L&&
            units>=rule.units&&(!rule.gps||hasVerifiedGps)&&
            (!rule.photo||photoCount>0);
    }
    public static void apply(SQLiteDatabase db,String eventId,String projectId,
            String method,String templateId,String nowUtc){
        if(templateId==null||templateId.isEmpty())return;
        db.execSQL("INSERT INTO survey_study_protocols(event_id,template_id,"+
            "template_name,min_visits,min_minutes,min_units,require_verified_gps,"+
            "require_protocol_photo,applied_at_utc) "+
            "SELECT ?,id,name,min_visits,min_minutes,min_units,require_verified_gps,"+
            "require_protocol_photo,? FROM study_protocol_templates "+
            "WHERE id=? AND project_id=? AND method=?",
            new Object[]{eventId,nowUtc,templateId,projectId,method});
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM survey_study_protocols "+
            "WHERE event_id=?",new String[]{eventId})){
            if(!c.moveToFirst()||c.getInt(0)!=1)
                throw new IllegalArgumentException("Selected study template no longer matches this survey");
        }
    }
    public static Rule snapshot(SQLiteDatabase db,String eventId){
        try(Cursor c=db.rawQuery("SELECT template_name,min_visits,min_minutes,min_units,"+
            "require_verified_gps,require_protocol_photo FROM survey_study_protocols "+
            "WHERE event_id=?",new String[]{eventId})){
            if(!c.moveToFirst())return null;
            return new Rule(c.getString(0),c.getInt(1),c.getInt(2),c.getInt(3),
                c.getInt(4)!=0,c.getInt(5)!=0);
        }
    }
    public static int photoCount(SQLiteDatabase db,String eventId){
        int n=0;
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM protocol_media WHERE event_id=?",
            new String[]{eventId})){
            if(c.moveToFirst())n+=c.getInt(0);
        }
        try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM observation_media m "+
            "JOIN field_records r ON r.id=m.record_id "+
            "WHERE r.event_id=? AND r.deleted_at IS NULL",new String[]{eventId})){
            if(c.moveToFirst())n+=c.getInt(0);
        }
        return n;
    }
}
