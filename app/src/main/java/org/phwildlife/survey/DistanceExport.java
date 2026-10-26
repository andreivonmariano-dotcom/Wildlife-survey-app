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
 * Species-specific candidate Distance flatfile. Filter by a single species AND
 * method before fitting models. Empty rows represent completed samples with no
 * eligible detections of that species, not a claim of true species absence.
 */
public final class DistanceExport {
    private DistanceExport(){}
    private static String csv(String x){
        if(x==null)return "";
        return "\""+x.replace("\"","\"\"")+"\"";
    }
    private static String num(Double n){return n==null?"":String.format(Locale.US,"%.7f",n);}
    private static void row(ZipOutputStream z,String... fields)throws Exception {
        StringBuilder s=new StringBuilder();
        for(int i=0;i<fields.length;i++){if(i>0)s.append(',');s.append(csv(fields[i]));}
        z.write((s.toString()+"\n").getBytes(StandardCharsets.UTF_8));
    }
    static final class Sample {
        String id,method,region,station;Double area,effort;
        boolean metadataReady;
    }
    public static void write(ZipOutputStream z,SQLiteDatabase database)throws Exception {
        z.putNextEntry(new ZipEntry("distance_flatfile_draft.csv"));
        row(z,"species","method","Region.Label","Area","Sample.Label","Effort","object","distance","size","station_id","metadata_ready","distance_review_status");
        ArrayList<Sample> samples=new ArrayList<>();
        try(Cursor c=database.rawQuery(
            "SELECT id,method,COALESCE(region_label,''),area_km2,COALESCE(station_id,''),effort_length_m FROM surveys "+
            "WHERE survey_profile='Birds' AND method IN ('point_count','line_transect') AND complete=1",null)){
            while(c.moveToNext()){
                Sample s=new Sample();s.id=c.getString(0);s.method=c.getString(1);s.region=c.getString(2);s.station=c.getString(4);
                s.area=c.isNull(3)?null:c.getDouble(3);
                s.effort=s.method.equals("point_count")?1.0:c.isNull(5)?null:c.getDouble(5)/1000.0;
                s.metadataReady=s.area!=null&&s.area>0&&!s.region.trim().isEmpty()&&!s.station.trim().isEmpty()&&s.effort!=null&&s.effort>0;
                samples.add(s);
            }
        }
        LinkedHashSet<String> species=new LinkedHashSet<>();
        try(Cursor c=database.rawQuery(
            "SELECT DISTINCT r.raw_identification FROM field_records r JOIN surveys s ON s.id=r.event_id "+
            "WHERE r.taxon_group='Birds' AND r.deleted_at IS NULL AND r.distance_eligible=1 "+
            "AND s.complete=1 AND s.survey_profile='Birds' AND s.method IN ('point_count','line_transect') "+
            "AND trim(COALESCE(r.raw_identification,''))<>'' ORDER BY r.raw_identification",null)){
            while(c.moveToNext())species.add(c.getString(0));
        }
        for(String taxon:species)for(Sample sample:samples){
            int found=0;
            try(Cursor obs=database.rawQuery(
                "SELECT r.id,r.individual_count,r.horizontal_m,r.perpendicular_m FROM field_records r "+
                "WHERE r.event_id=? AND r.raw_identification=? AND r.taxon_group='Birds' "+
                "AND r.deleted_at IS NULL AND r.distance_eligible=1 ORDER BY r.id",
                new String[]{sample.id,taxon})){
                while(obs.moveToNext()){
                    Double distance=obs.isNull(sample.method.equals("line_transect")?3:2)?null:
                        obs.getDouble(sample.method.equals("line_transect")?3:2);
                    if(distance==null)continue;
                    found++;
                    row(z,taxon,sample.method,sample.region,num(sample.area),sample.id,num(sample.effort),
                        String.valueOf(obs.getLong(0)),num(distance),String.valueOf(obs.getInt(1)),sample.station,
                        sample.metadataReady?"1":"0",sample.metadataReady?"candidate_only":"missing_effort_or_stratum_metadata");
                }
            }
            // A detection lacking distance is NOT a nondetection. Do not code it as zero.
            if(found==0){
                boolean speciesObserved=false;
                try(Cursor all=database.rawQuery(
                    "SELECT COUNT(*) FROM field_records WHERE event_id=? AND raw_identification=? "+
                    "AND taxon_group='Birds' AND deleted_at IS NULL",
                    new String[]{sample.id,taxon})){
                    if(all.moveToFirst())speciesObserved=all.getInt(0)>0;
                }
                if(!speciesObserved)row(z,taxon,sample.method,sample.region,num(sample.area),sample.id,num(sample.effort),
                    "","","",sample.station,sample.metadataReady?"1":"0",
                    sample.metadataReady?"zero_detections":"missing_effort_or_stratum_metadata");
            }
        }
        z.closeEntry();
    }
}
