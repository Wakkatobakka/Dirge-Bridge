package com.nttdocomo.ui;
/** Small deterministic mono PCM16 resampler used to feed Android's native-rate audio path. */
public final class PcmResampler {
 public static final int TARGET_RATE=48000;
 private PcmResampler(){}
 public static short[] to48k(short[] input,int sourceRate){
  if(input==null||input.length==0)return new short[0];if(sourceRate<=0)throw new IllegalArgumentException("sourceRate");
  if(sourceRate==TARGET_RATE)return input.clone();
  if(sourceRate==8000){short[] out=new short[input.length*6];int o=0;for(int i=0;i<input.length;i++){int a=input[i],b=input[Math.min(i+1,input.length-1)];for(int k=0;k<6;k++)out[o++]=(short)((a*(6-k)+b*k)/6);}return out;}
  int n=Math.max(1,(int)Math.round(input.length*(TARGET_RATE/(double)sourceRate)));short[] out=new short[n];
  double step=sourceRate/(double)TARGET_RATE;for(int j=0;j<n;j++){double p=j*step;int i=(int)p;double f=p-i;if(i>=input.length-1)out[j]=input[input.length-1];else out[j]=(short)Math.round(input[i]*(1.0-f)+input[i+1]*f);}return out;
 }
}
