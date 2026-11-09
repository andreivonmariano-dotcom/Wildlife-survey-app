package org.phwildlife.survey;

import android.content.Context;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** One-time offline import of the vetted filename inventory from the owner's Drive ZIP. */
public final class PhLibraryDrivePackImport {
    private PhLibraryDrivePackImport(){}
    public static final class Result {
        public int entries,imported,unmatched,failed;
        public final Set<String> taxa=new HashSet<>();
        public String summary(){
            return imported+" owner photographs added across "+taxa.size()+
                " species accounts. "+unmatched+" lacked an exact Book 01 match; "+
                failed+" could not be decoded or saved. Original Drive photos unchanged.";
        }
    }
    public static Result importZip(Context c,Uri uri)throws Exception {
        if(uri==null)throw new IllegalArgumentException("No ZIP file selected");
        try(InputStream stream=c.getContentResolver().openInputStream(uri)){
            if(stream==null)throw new IllegalArgumentException("Cannot open photo pack");
            return importStream(c,stream);
        }
    }
    public static Result importStream(Context c,InputStream stream)throws Exception {
        Result result=new Result();
        long cumulative=0;
        try(ZipInputStream zip=new ZipInputStream(stream)){
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null){
                if(++result.entries>400)
                    throw new IllegalArgumentException("ZIP exceeds 400 file entries");
                if(entry.isDirectory()){zip.closeEntry();continue;}
                String name=entry.getName();
                if(name.contains("/")||name.contains("\\")||
                    !PhLibraryBulkPhotoImport.isOwnerDriveFilename(c,name)){
                    result.unmatched++;zip.closeEntry();continue;
                }
                String taxon=PhLibraryBulkPhotoImport.exactTaxon(c,name);
                if(taxon==null){
                    result.unmatched++;zip.closeEntry();continue;
                }
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                byte[] buffer=new byte[16384];int count;boolean oversize=false;
                while((count=zip.read(buffer))!=-1){
                    if(bytes.size()+count>34000000){oversize=true;break;}
                    bytes.write(buffer,0,count);
                    cumulative+=count;
                    if(cumulative>1250000000L)
                        throw new IllegalArgumentException("ZIP expands beyond safe total size");
                }
                if(oversize){
                    result.failed++;
                    // Drain remainder of the rejected entry without retaining its bytes.
                    while(zip.read(buffer)!=-1){}
                    zip.closeEntry();continue;
                }
                try{
                    PhLibrarySpeciesMedia.importPrivateBytes(c,taxon,name,bytes.toByteArray());
                    result.imported++;result.taxa.add(taxon);
                }catch(Exception ignored){result.failed++;}
                zip.closeEntry();
            }
        }
        return result;
    }
}
