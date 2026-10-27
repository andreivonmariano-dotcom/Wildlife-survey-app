package org.phwildlife.survey;

/** Fast 1024-point Hann-window FFT, 64 log-frequency bands for field spectrograms. */
public final class SpectrogramMath {
    public static final int WINDOW=1024, BANDS=64;
    private SpectrogramMath(){}
    public static float[] analyze(short[] input,int offset){
        if(input==null||offset<0||offset+WINDOW>input.length)
            throw new IllegalArgumentException("1024 audio samples required");
        double[] re=new double[WINDOW],im=new double[WINDOW];
        for(int i=0;i<WINDOW;i++){
            re[i]=input[offset+i]/32768d*(.5d-.5d*Math.cos(2d*Math.PI*i/(WINDOW-1)));
        }
        for(int i=1,j=0;i<WINDOW;i++){
            int bit=WINDOW>>1;
            for(; (j&bit)!=0; bit>>=1)j^=bit;
            j^=bit;
            if(i<j){
                double swap=re[i];re[i]=re[j];re[j]=swap;
            }
        }
        for(int n=2;n<=WINDOW;n<<=1){
            double angle=-2d*Math.PI/n,wr=Math.cos(angle),wi=Math.sin(angle);
            for(int k=0;k<WINDOW;k+=n){
                double cr=1d,ci=0d;
                for(int j=0;j<n/2;j++){
                    int a=k+j,b=a+n/2;
                    double vr=re[b]*cr-im[b]*ci,vi=re[b]*ci+im[b]*cr;
                    re[b]=re[a]-vr;im[b]=im[a]-vi;
                    re[a]+=vr;im[a]+=vi;
                    double next=cr*wr-ci*wi;ci=cr*wi+ci*wr;cr=next;
                }
            }
        }
        float[] bands=new float[BANDS];
        for(int b=0;b<BANDS;b++){
            // 44.1 kHz input, 85 Hz to ~17 kHz; higher frequencies remain visible.
            double f0=85d*Math.pow(200d,b/(double)BANDS);
            double f1=85d*Math.pow(200d,(b+1d)/BANDS);
            int lo=Math.max(1,(int)Math.floor(f0*WINDOW/44100d));
            int hi=Math.min(WINDOW/2,(int)Math.ceil(f1*WINDOW/44100d));
            double peak=0d;
            for(int i=lo;i<=hi;i++){
                double mag=Math.hypot(re[i],im[i]);
                if(mag>peak)peak=mag;
            }
            // Fixed magnitude reference, not sample-relative: silence remains dark.
            bands[b]=(float)Math.max(0d,Math.min(1d,(20d*Math.log10(peak+1e-6d)+35d)/45d));
        }
        return bands;
    }
}
