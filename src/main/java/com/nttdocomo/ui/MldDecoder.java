package com.nttdocomo.ui;
import java.io.IOException;
/** Minimal MFi/MLD ADPM extractor for Lost Episode's 8 kHz, 2-bit, mono assets. */
final class MldDecoder {
 static final class Decoded {final int sampleRate,channels,bitDepth;final short[] pcm;Decoded(int r,int c,int b,short[] p){sampleRate=r;channels=c;bitDepth=b;pcm=p;}}
 private MldDecoder(){}
 private static int find(byte[] a,byte[] needle,int from,int to){outer:for(int i=Math.max(0,from);i<=Math.min(a.length-needle.length,to-needle.length);i++){for(int j=0;j<needle.length;j++)if(a[i+j]!=needle[j])continue outer;return i;}return -1;}
 private static int u16(byte[] a,int p){return ((a[p]&255)<<8)|(a[p+1]&255);}
 private static long u32(byte[] a,int p){return ((long)(a[p]&255)<<24)|((long)(a[p+1]&255)<<16)|((long)(a[p+2]&255)<<8)|(a[p+3]&255);}
 static Decoded decode(byte[] mld)throws IOException{
  if(mld==null||mld.length<16)throw new IOException("MLD is truncated");
  byte[] ADAT={'a','d','a','t'},ADPM={'a','d','p','m'};int adat=find(mld,ADAT,0,mld.length);
  if(adat<0||adat+8>mld.length)throw new IOException("MLD has no adat chunk");long body=u32(mld,adat+4);long e=(long)adat+8+body;if(body<=0||e>mld.length)throw new IOException("Bad adat length");int end=(int)e;
  int adpm=find(mld,ADPM,adat+8,Math.min(end,adat+8+256));if(adpm<0||adpm+9>end)throw new IOException("MLD has no adpm header");
  int header=u16(mld,adpm+4);if(header<3||adpm+6+header>end)throw new IOException("Bad adpm header length");
  int rate=(mld[adpm+6]&255)*1000,bits=mld[adpm+7]&255,channels=mld[adpm+8]&255,start=adpm+6+header;
  if(rate<4000||rate>48000)throw new IOException("Unsupported ADPM rate: "+rate);
  if(bits!=2||channels!=1)throw new IOException("Unsupported ADPM layout: "+bits+"-bit, channels="+channels);
  if(start>=end)throw new IOException("Empty ADPM payload");return new Decoded(rate,channels,bits,G72616.decodeLsb(mld,start,end-start));
 }
}
