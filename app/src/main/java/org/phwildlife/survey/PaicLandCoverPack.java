package org.phwildlife.survey;

import android.content.Context;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.InflaterInputStream;

/**
 * Native bundled land-cover index with a legacy private-file override.
 * Derived from ESA WorldCover v100 (2020) 10m pixels, aggregated to 0.005 degree
 * modal grid cells (~500m). No current habitat or specific ecological community
 * can be inferred from the map. Never auto-confirm a habitat.
 */
public final class PaicLandCoverPack {
    private PaicLandCoverPack(){}
    public static final String FORMAT="LIKAS_WORLDCOVER_2020_500M_V1";
    private static final String NAME="paic_worldcover_2020_500m_v1.txt";
    private static final String BUNDLED="paic_worldcover2020_500m.txt";
    private static final int SIDE=600, CELLS=SIDE*SIDE, MAX_BYTES=1024*1024;
    public static boolean installed(Context context){
        if(new File(context.getFilesDir(),NAME).isFile())return true;
        try(InputStream bundled=context.getAssets().open(BUNDLED)){
            return bundled.read()>=0;
        }catch(Exception ignored){return false;}
    }
    private static String idAt(double latitude,double longitude){
        if(latitude<0||longitude<0||latitude>=90||longitude>=180)return "";
        int lat=(int)Math.floor(latitude/3d)*3,lon=(int)Math.floor(longitude/3d)*3;
        return String.format(Locale.US,"N%02dE%03d",lat,lon);
    }
    private static byte[] decode(String encoded)throws Exception{
        byte[] compressed=Base64.decode(encoded,Base64.DEFAULT);
        try(InflaterInputStream inflater=new InflaterInputStream(
                    new ByteArrayInputStream(compressed));
            ByteArrayOutputStream result=new ByteArrayOutputStream(CELLS)){
            byte[] chunk=new byte[8192];int size;
            while((size=inflater.read(chunk))!=-1){
                if(result.size()+size>CELLS)throw new IllegalArgumentException("Map tile exceeds grid size");
                result.write(chunk,0,size);
            }
            byte[] output=result.toByteArray();
            if(output.length!=CELLS)throw new IllegalArgumentException("Incomplete map tile");
            return output;
        }
    }
    private static Map<String,String> read(InputStream stream)throws Exception{
        Map<String,String> lines=new HashMap<>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(
            stream,StandardCharsets.US_ASCII));
        String header=reader.readLine();
        if(!FORMAT.equals(header))throw new IllegalArgumentException("Unsupported map edition");
        String line;int size=FORMAT.length()+1;
        while((line=reader.readLine())!=null){
            size+=line.length()+1;
            if(size>MAX_BYTES)throw new IllegalArgumentException("Map pack exceeds 1 MB");
            if(line.startsWith("#")||line.isEmpty())continue;
            int divider=line.indexOf('|');
            if(divider!=7||!line.substring(0,7).matches("N[0-9]{2}E[0-9]{3}"))
                throw new IllegalArgumentException("Map tile identifier invalid");
            String id=line.substring(0,divider);
            if(lines.put(id,line.substring(divider+1))!=null)
                throw new IllegalArgumentException("Duplicate map tile "+id);
        }
        if(lines.size()!=16)throw new IllegalArgumentException("Expected 16 map tiles");
        return lines;
    }
    /** Validate every tile before replacing the previous local map file. */
    public static void install(Context context,InputStream stream)throws Exception{
        Map<String,String> data=read(stream);
        for(String line:data.values())decode(line);
        File home=context.getFilesDir(),candidate=new File(home,NAME+".pending");
        if(candidate.exists()&&!candidate.delete())
            throw new IllegalStateException("Cannot replace partial map");
        try(FileOutputStream out=new FileOutputStream(candidate)){
            out.write((FORMAT+"\n").getBytes(StandardCharsets.US_ASCII));
            for(String id:new java.util.TreeSet<>(data.keySet()))
                out.write((id+"|"+data.get(id)+"\n").getBytes(StandardCharsets.US_ASCII));
            out.getFD().sync();
        }
        File target=new File(home,NAME);
        if(!candidate.renameTo(target)){
            candidate.delete();
            throw new IllegalStateException("Could not activate offline map");
        }
    }
    /**
     * Suggest up to four mapped categories from a 5x5 neighborhood (about
     * 2.5 km across). This is not the footprint of a planned transect.
     */
    public static List<String> suggest(Context context,double latitude,
            double longitude)throws Exception{
        if(!Double.isFinite(latitude)||!Double.isFinite(longitude)||
                latitude < -90 ||latitude >90||longitude < -180||longitude >180)
            return Collections.emptyList();
        File source=new File(context.getFilesDir(),NAME);
        Map<String,String> data;
        // Existing owner files remain readable for upgrade compatibility;
        // the packaged derivative is the default on a clean offline install.
        try(InputStream in=source.isFile()?
                new FileInputStream(source):context.getAssets().open(BUNDLED)){
            data=read(in);
        }
        Map<String,byte[]> cached=new HashMap<>();
        Map<String,Integer> counts=new HashMap<>();
        // Sample grid cells symmetrically around the initial, verified GPS fix.
        for(int y=-2;y<=2;y++)for(int x=-2;x<=2;x++){
            double sampleLat=latitude+y*0.005,sampleLon=longitude+x*0.005;
            String id=idAt(sampleLat,sampleLon),encoded=data.get(id);
            if(encoded==null)continue;
            byte[] tile=cached.get(id);
            if(tile==null){tile=decode(encoded);cached.put(id,tile);}
            int startLat=Integer.parseInt(id.substring(1,3));
            int startLon=Integer.parseInt(id.substring(4,7));
            int row=(int)Math.floor((startLat+3-sampleLat)/0.005);
            int col=(int)Math.floor((sampleLon-startLon)/0.005);
            if(row<0||row>=SIDE||col<0||col>=SIDE)continue;
            String match=PaicHabitatPolicy.landCoverSuggestion(tile[row*SIDE+col]&255);
            if(!match.isEmpty())counts.put(match,counts.getOrDefault(match,0)+1);
        }
        ArrayList<String> ordered=new ArrayList<>(counts.keySet());
        ordered.sort((a,b)->{
            int difference=Integer.compare(counts.get(b),counts.get(a));
            return difference!=0?difference:a.compareTo(b);
        });
        if(ordered.size()>4)return new ArrayList<>(ordered.subList(0,4));
        return ordered;
    }
}
