package com.nttdocomo.ui;
import java.nio.file.*;import java.security.*;
public final class TestMldDecoder {
 static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}
 public static void main(String[] a)throws Exception{
  if(a.length!=1)throw new IllegalArgumentException("Pass titlemusic.mld path");byte[] m=Files.readAllBytes(Paths.get(a[0]));MldDecoder.Decoded d=MldDecoder.decode(m);
  if(d.sampleRate!=8000||d.bitDepth!=2||d.channels!=1)throw new AssertionError("unexpected format");
  if(d.pcm.length!=663160)throw new AssertionError("unexpected title sample count "+d.pcm.length);
  int min=32767,max=-32768;long sum=0;MessageDigest md=MessageDigest.getInstance("SHA-256");byte[] pcm=new byte[d.pcm.length*2];
  for(int i=0;i<d.pcm.length;i++){int v=d.pcm[i];if(v<min)min=v;if(v>max)max=v;sum+=Math.abs(v);pcm[i*2]=(byte)v;pcm[i*2+1]=(byte)(v>>8);}String sha=hex(md.digest(pcm));
  System.out.println("PASS: MLD adat/adpm parsed and G.726 2-bit decoded");System.out.println("rate="+d.sampleRate+" samples="+d.pcm.length+" min="+min+" max="+max+" meanAbs="+(sum/(double)d.pcm.length)+" pcmSha256="+sha);
 }
}
