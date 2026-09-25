package com.nttdocomo.ui;
/**
 * Pads short PCM streams with silence so Android MODE_STREAM has enough frames
 * to actually drain and advance its playback head. The decoded game audio is
 * untouched; only the device-side stream tail is extended with zero samples.
 */
final class PcmStreamPad {
 private PcmStreamPad(){}
 static short[] ensureMinimum(short[] pcm,int minimumFrames){
  if(pcm==null)throw new IllegalArgumentException("pcm");
  if(minimumFrames<=0||pcm.length>=minimumFrames)return pcm;
  short[] out=new short[minimumFrames];
  System.arraycopy(pcm,0,out,0,pcm.length);
  return out;
 }
}
