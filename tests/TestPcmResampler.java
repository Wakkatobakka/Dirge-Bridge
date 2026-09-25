import com.nttdocomo.ui.PcmResampler;
public class TestPcmResampler{
 static int n;static void ok(boolean b,String m){n++;if(!b)throw new AssertionError(m);}public static void main(String[]a){
  short[] in={0,6000,12000};short[] out=PcmResampler.to48k(in,8000);ok(out.length==18,"8k->48k length x6");ok(out[0]==0,"first sample");ok(out[6]==6000,"second source lands exactly at frame 6");ok(out[12]==12000,"third source lands exactly at frame 12");for(int i=1;i<12;i++)ok(out[i]>=out[i-1],"linear interpolation monotonic");
  short[] same=PcmResampler.to48k(in,48000);ok(same.length==in.length&&same!=in,"48k clone");ok(PcmResampler.to48k(new short[0],8000).length==0,"empty input");
  System.out.println("PASS: deterministic 8 kHz -> 48 kHz mono PCM16 interpolation; assertions="+n);
 }}
