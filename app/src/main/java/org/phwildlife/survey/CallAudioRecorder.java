package org.phwildlife.survey;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import java.io.File;
import java.io.RandomAccessFile;

/** App-private, lossless mono WAV capture. Recording never overwrites survey data. */
public final class CallAudioRecorder {
    public static final int SAMPLE_RATE=44100;
    public static final long MAX_SECONDS=600;
    public interface Listener {
        void onSamples(short[] samples);
        void onFinished(File file,long durationMs,String error);
    }
    private volatile boolean capturing=false;
    private AudioRecord recorder;
    private Thread writer;
    public boolean isCapturing(){return capturing;}

    public synchronized void start(File destination,Listener listener) throws Exception{
        if(capturing)throw new IllegalStateException("Already recording");
        int min=AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if(min<=0)throw new IllegalStateException("Microphone does not support 44.1 kHz PCM");
        recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,
            Math.max(min*2,8192));
        if(recorder.getState()!=AudioRecord.STATE_INITIALIZED){
            recorder.release();recorder=null;
            throw new IllegalStateException("Microphone cannot initialize");
        }
        try{recorder.startRecording();}
        catch(Exception e){recorder.release();recorder=null;throw e;}
        capturing=true;
        writer=new Thread(()->capture(destination,listener),"LIKAS-Acoustic-WAV");
        writer.start();
    }
    public synchronized void stop(){
        // Allow the active read to finish. Calling stop() across threads can
        // return ERROR_INVALID_OPERATION and discard otherwise valid audio.
        capturing=false;
    }
    private void capture(File destination,Listener listener){
        long frames=0;String error=null;
        AudioRecord active=recorder;
        try(RandomAccessFile wav=new RandomAccessFile(destination,"rw")){
            wav.setLength(0);
            wav.write(new byte[44]);
            short[] pcm=new short[2048];
            byte[] bytes=new byte[pcm.length*2];
            while(capturing&&frames<SAMPLE_RATE*MAX_SECONDS){
                int n=active.read(pcm,0,pcm.length,AudioRecord.READ_BLOCKING);
                if(n==AudioRecord.ERROR_INVALID_OPERATION||n==AudioRecord.ERROR_BAD_VALUE){
                    error="Audio receiver stopped";break;
                }
                if(n<=0)continue;
                for(int i=0;i<n;i++){
                    bytes[i*2]=(byte)(pcm[i]&255);
                    bytes[i*2+1]=(byte)((pcm[i]>>8)&255);
                }
                wav.write(bytes,0,n*2);frames+=n;
                if(n>=SpectrogramMath.WINDOW){
                    short[] window=new short[SpectrogramMath.WINDOW];
                    System.arraycopy(pcm,n-window.length,window,0,window.length);
                    listener.onSamples(window);
                }
            }
            if(frames>0){
                wav.seek(0);writeHeader(wav,frames*2);
            }
        }catch(Exception e){error=e.getClass().getSimpleName()+": "+e.getMessage();}
        finally{
            capturing=false;
            try{active.release();}catch(Exception ignored){}
            recorder=null;
            if(frames==0||error!=null){destination.delete();}
            listener.onFinished(destination,frames*1000L/SAMPLE_RATE,error);
        }
    }
    private static void ascii(RandomAccessFile f,String s)throws Exception{f.write(s.getBytes("US-ASCII"));}
    private static void le16(RandomAccessFile f,int value)throws Exception{
        f.write(value&255);f.write((value>>8)&255);
    }
    private static void le32(RandomAccessFile f,long value)throws Exception{
        for(int i=0;i<4;i++)f.write((int)(value>>(8*i))&255);
    }
    private static void writeHeader(RandomAccessFile f,long dataBytes)throws Exception{
        ascii(f,"RIFF");le32(f,36+dataBytes);ascii(f,"WAVE");
        ascii(f,"fmt ");le32(f,16);le16(f,1);le16(f,1);
        le32(f,SAMPLE_RATE);le32(f,SAMPLE_RATE*2);le16(f,2);le16(f,16);
        ascii(f,"data");le32(f,dataBytes);
    }
}
