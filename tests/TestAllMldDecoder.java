package com.nttdocomo.ui;
import java.nio.file.*;import java.util.*;
public final class TestAllMldDecoder{
 public static void main(String[]a)throws Exception{Path root=Paths.get(a[0]);int files=0;long samples=0;List<String> failures=new ArrayList<String>();
  try(java.util.stream.Stream<Path>s=Files.walk(root)){for(Path p:(Iterable<Path>)s.filter(Files::isRegularFile)::iterator){if(!p.getFileName().toString().toLowerCase().endsWith(".mld"))continue;files++;try{MldDecoder.Decoded d=MldDecoder.decode(Files.readAllBytes(p));if(d.sampleRate!=8000||d.channels!=1||d.bitDepth!=2)throw new Exception("format");samples+=d.pcm.length;}catch(Exception e){failures.add(root.relativize(p)+": "+e);}}}
  if(!failures.isEmpty())throw new AssertionError(failures.toString());System.out.println("PASS: decoded "+files+" Lost Episode MLD assets; PCM samples="+samples+"; all 8000 Hz / 2-bit / mono");
 }
}
