package org.phwildlife.survey;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Low-resolution PSGC 2026 Q3 province polygons.
 * Source: faeldon/philippines-json-maps (MIT), original PSGC source.
 * Rounded to 4 decimal places (~11m), simplified at source (~0.1%).
 * Boundary uncertainty uses a >= 700m caution band and GPS accuracy.
 * This is an administrative suggestion, NOT a faunal distribution claim.
 */
public final class BoundaryLookup {
    public static final String SOURCE="faeldon/philippines-json-maps";
    public static final String VERSION="PSGC-2026-Q3-lowres-v1";
    private BoundaryLookup(){}
    public static final class Match {
        public String province="",psgc="",regionCode="",checklistRegion="OTHER",status="outside_coverage";
        public double edgeMeters=Double.NaN;
    }
    private static JSONArray provinces;
    public static synchronized void load(Context context)throws Exception{
        if(provinces!=null)return;
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try(InputStream in=context.getAssets().open("ph_provinces_2026q3.json")){
            byte[] buf=new byte[16384];int n;
            while((n=in.read(buf))>=0)out.write(buf,0,n);
        }
        provinces=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8)).getJSONArray("features");
    }
    private static String region(String code,String name){
        if(code.equals("1700000000")){
            if(name.equals("Palawan"))return "PALAWAN";
            if(name.contains("Mindoro"))return "MINDORO";
            return "OTHER";
        }
        if(code.equals("900000000")&&name.equals("Sulu"))return "SULU";
        if(code.equals("1900000000")&&(name.equals("Basilan")||name.equals("Tawi-Tawi")))return "SULU";
        if(code.equals("600000000")||code.equals("700000000")||code.equals("800000000")||code.equals("1800000000"))
            return "VISAYAS";
        if(code.equals("900000000")||code.equals("1000000000")||code.equals("1100000000")
            ||code.equals("1200000000")||code.equals("1600000000")||code.equals("1900000000"))
            return "MINDANAO";
        return "LUZON";
    }
    public static Match find(Context ctx,double lat,double lon,float uncertaintyMeters){
        Match result=new Match();
        if(Double.isNaN(lat)||Double.isNaN(lon)||lat<4||lat>22||lon<115||lon>130){
            result.status="outside_coverage";return result;
        }
        if(Float.isNaN(uncertaintyMeters)||uncertaintyMeters<=0||uncertaintyMeters>200){
            result.status="poor_accuracy";return result;
        }
        try{
            load(ctx);
            int contained=0;
            double closest=Double.POSITIVE_INFINITY;
            for(int i=0;i<provinces.length();i++){
                JSONObject f=provinces.getJSONObject(i);
                JSONArray shape=f.getJSONArray("g");
                boolean multi=f.getString("t").equals("MultiPolygon");
                // Consider all exterior rings and holes; odd-even parity per polygon.
                boolean inside=false;double edge=Double.POSITIVE_INFINITY;
                if(multi){
                    for(int m=0;m<shape.length();m++){
                        JSONArray polygon=shape.getJSONArray(m);
                        inside|=insidePolygon(polygon,lon,lat);
                        edge=Math.min(edge,polygonEdge(polygon,lon,lat));
                    }
                }else{
                    inside=insidePolygon(shape,lon,lat);
                    edge=polygonEdge(shape,lon,lat);
                }
                closest=Math.min(closest,edge);
                if(inside){
                    contained++;
                    if(result.province.isEmpty()||edge>result.edgeMeters||Double.isNaN(result.edgeMeters)){
                        result.province=f.getString("n");
                        result.psgc=f.getString("p");result.regionCode=f.getString("r");
                        result.checklistRegion=region(result.regionCode,result.province);
                        result.edgeMeters=edge;
                    }
                }
            }
            if(contained==0){result.status="outside_admin_polygon";result.edgeMeters=closest;}
            else if(contained>1)result.status="overlapping_provinces";
            else if(result.edgeMeters<Math.max(700,uncertaintyMeters*2))result.status="near_boundary_uncertain";
            else if(result.checklistRegion.equals("OTHER"))result.status="island_assignment_uncertain";
            else result.status="suggestion_only";
        }catch(Exception ex){result.status="boundary_file_error";}
        return result;
    }
    private static boolean insidePolygon(JSONArray rings,double x,double y)throws Exception{
        boolean inside=false;
        for(int j=0;j<rings.length();j++)if(inRing(rings.getJSONArray(j),x,y))inside=!inside;
        return inside;
    }
    private static boolean inRing(JSONArray ring,double x,double y)throws Exception{
        boolean in=false;int n=ring.length();
        for(int i=0,j=n-1;i<n;j=i++){
            JSONArray a=ring.getJSONArray(i),b=ring.getJSONArray(j);
            double ax=a.getDouble(0),ay=a.getDouble(1),bx=b.getDouble(0),by=b.getDouble(1);
            if(((ay>y)!=(by>y))&&(x<(bx-ax)*(y-ay)/(by-ay)+ax))in=!in;
        }
        return in;
    }
    private static double polygonEdge(JSONArray rings,double x,double y)throws Exception{
        double best=Double.POSITIVE_INFINITY;
        for(int j=0;j<rings.length();j++){
            JSONArray ring=rings.getJSONArray(j);
            for(int i=0,n=ring.length();i<n;i++){
                JSONArray a=ring.getJSONArray(i),b=ring.getJSONArray((i+1)%n);
                double x1=(a.getDouble(0)-x)*111320*Math.cos(y*Math.PI/180);
                double y1=(a.getDouble(1)-y)*110574;
                double x2=(b.getDouble(0)-x)*111320*Math.cos(y*Math.PI/180);
                double y2=(b.getDouble(1)-y)*110574;
                double dx=x2-x1,dy=y2-y1;
                double t=(dx*dx+dy*dy)==0?0:Math.max(0,Math.min(1,-(x1*dx+y1*dy)/(dx*dx+dy*dy)));
                best=Math.min(best,Math.hypot(x1+t*dx,y1+t*dy));
            }
        }
        return best;
    }
}
