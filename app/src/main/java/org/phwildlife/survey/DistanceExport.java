package org.phwildlife.survey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Draft Distance flatfile by METHOD + observed species.
 * Distances exported in km to match Area (km^2) and line Effort (km).
 * 1 point visit = 1 unit of Effort. Assumption screening remains external.
 */
public final class DistanceExport {
    private DistanceExport(){}
    private static String csv(String x){return x==null?"":"\""+x.replace("\"","\"\"")+"\"";}
    private static String num(Double n){return n==null?"":String.format(Locale.US,"%.8f",n);}
    private static void row(ZipOutputStream z,String... values)throws Exception{
        StringBuilder line=new StringBuilder();
        for(int i=0;i<values.length;i++){if(i>0)line.append(',');line.append(csv(values[i]));}
        z.write((line.toString()+"\n").getBytes(StandardCharsets.UTF_8));
    }
    static class Sample {
        String id,eventId,method,region,station,notes;
        Double area,effort;
        boolean metadataReady,legacy;
    }
    public static void write(ZipOutputStream z,SQLiteDatabase database)throws Exception{
        z.putNextEntry(new ZipEntry("distance_flatfile_draft.csv"));
        row(z,"species","method","Region.Label","Area","Sample.Label","Effort","object","distance","size",
            "station_id","event_id","distance_m","distance_units","metadata_ready","distance_review_status");
        ArrayList<Sample> samples=new ArrayList<>();
        try(Cursor c=database.rawQuery(
            "SELECT p.id,p.event_id,p.station_id,COALESCE(s.region_label,''),s.area_km2,COALESCE(p.notes,''),s.point_radius_m "+
            "FROM point_counts p JOIN surveys s ON s.id=p.event_id "+
            "WHERE s.survey_profile='Birds' AND p.completed=1 AND s.complete=1 ORDER BY p.started",null)){
            while(c.moveToNext()){
                Sample v=new Sample();v.id=c.getString(0);v.eventId=c.getString(1);v.station=c.getString(2);
                v.method="point_count";v.region=c.getString(3);v.area=c.isNull(4)?null:c.getDouble(4);v.effort=1.0;
                v.notes=c.getString(5);v.legacy=v.notes.startsWith("Legacy");
                v.metadataReady=!v.legacy&&v.area!=null&&v.area>0&&!v.region.isEmpty()&&!v.station.isEmpty();
                samples.add(v);
            }
        }
        try(Cursor c=database.rawQuery(
            "SELECT id,COALESCE(station_id,''),COALESCE(region_label,''),area_km2,effort_length_m "+
            "FROM surveys WHERE survey_profile='Birds' AND method='line_transect' AND complete=1 ORDER BY started",null)){
            while(c.moveToNext()){
                Sample v=new Sample();v.id=c.getString(0);v.eventId=v.id;v.method="line_transect";
                v.station=c.getString(1);v.region=c.getString(2);v.area=c.isNull(3)?null:c.getDouble(3);
                v.effort=c.isNull(4)?null:c.getDouble(4)/1000.0;
                v.metadataReady=v.effort!=null&&v.effort>0&&v.area!=null&&v.area>0&&
                    !v.region.isEmpty()&&!v.station.isEmpty();
                samples.add(v);
            }
        }
        LinkedHashSet<String> observedPairs=new LinkedHashSet<>();
        try(Cursor c=database.rawQuery(
            "SELECT DISTINCT r.raw_identification,s.method FROM field_records r "+
            "JOIN surveys s ON s.id=r.event_id WHERE r.taxon_group='Birds' AND r.deleted_at IS NULL "+
            "AND r.distance_eligible=1 AND s.complete=1 AND s.survey_profile='Birds' "+
            "AND s.method IN ('point_count','line_transect') "+
            "AND trim(COALESCE(r.raw_identification,''))<>'' ORDER BY r.raw_identification",null)){
            while(c.moveToNext())observedPairs.add(c.getString(0)+"\u001f"+c.getString(1));
        }
        for(String key:observedPairs){
            int pivot=key.lastIndexOf('\u001f');
            String species=key.substring(0,pivot),method=key.substring(pivot+1);
            for(Sample sample:samples){
                if(!sample.method.equals(method))continue;
                boolean anyEligible=false;
                try(Cursor observations=database.rawQuery(
                    "SELECT id,individual_count,horizontal_m,perpendicular_m FROM field_records "+
                    "WHERE event_id=? AND raw_identification=? AND taxon_group='Birds' "+
                    "AND deleted_at IS NULL AND distance_eligible=1 "+
                    (method.equals("point_count")?"AND point_count_id=? ":"")+"ORDER BY id",
                    method.equals("point_count")?new String[]{sample.eventId,species,sample.id}:new String[]{sample.eventId,species})){
                    while(observations.moveToNext()){
                        int col=method.equals("line_transect")?3:2;
                        if(observations.isNull(col))continue;
                        double distanceM=observations.getDouble(col);
                        if(distanceM<0)continue;
                        anyEligible=true;
                        row(z,species,method,sample.region,num(sample.area),sample.id,num(sample.effort),
                            String.valueOf(observations.getLong(0)),num(distanceM/1000.0),
                            String.valueOf(observations.getInt(1)),sample.station,sample.eventId,num(distanceM),
                            "km",sample.metadataReady?"1":"0",
                            sample.metadataReady?"candidate_only":"missing_or_legacy_sampling_metadata");
                    }
                }
                if(!anyEligible){
                    int observed=0;
                    try(Cursor all=database.rawQuery(
                        "SELECT COUNT(*) FROM field_records WHERE event_id=? AND raw_identification=? "+
                        "AND taxon_group='Birds' AND deleted_at IS NULL "+
                        (method.equals("point_count")?"AND point_count_id=?":""),
                        method.equals("point_count")?new String[]{sample.eventId,species,sample.id}:new String[]{sample.eventId,species})){
                        if(all.moveToFirst())observed=all.getInt(0);
                    }
                    if(observed==0)row(z,species,method,sample.region,num(sample.area),sample.id,num(sample.effort),
                        "","","",sample.station,sample.eventId,"","km",sample.metadataReady?"1":"0",
                        sample.metadataReady?"zero_detected_species":"missing_or_legacy_sampling_metadata");
                }
            }
        }
        z.closeEntry();
    }
}
