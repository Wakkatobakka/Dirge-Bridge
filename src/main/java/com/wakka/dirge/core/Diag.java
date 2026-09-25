package com.wakka.dirge.core;
import java.io.*;import java.util.*;
/** Runtime diagnostics retained across the additive bridge releases. */
public final class Diag {
 public static final long started=System.currentTimeMillis();
 public static volatile long frames,drawCalls,textures,resourceReads,glErrors,gcHints,exceptions,lookupCatches,frameClears,menuProjectionFixes;
 public static volatile String stage="Not started",lastResource="",lastError="";
 private static final ArrayDeque<String> lines=new ArrayDeque<String>();
 private static final HashSet<String> seen=new HashSet<String>();
 private static PrintWriter writer;private static long lastGC;
 public static synchronized void open(File f)throws IOException{writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"),true);}
 public static synchronized void event(String kind,String message){
  String s=String.format(java.util.Locale.ROOT,"%8.3f  %-15s %s",(System.currentTimeMillis()-started)/1000.0,kind,message);
  if(lines.size()>=600)lines.removeFirst();lines.addLast(s);if(writer!=null)writer.println(s);
  if(Host.platform!=null)Host.platform.log(s);else System.out.println(s);
 }
 public static synchronized void once(String kind,String s){if(seen.size()<10000&&seen.add(kind+":"+s))event(kind,s);}
 public static void stage(String s){stage=s;once("GAME CODE",s);}
 public static void resource(String s){resourceReads++;lastResource=s;once("RESOURCE",s);}
 public static void caught(Throwable t,String where){
  if(t instanceof Host.ResourceLookupMiss&&where.startsWith("an.")){lookupCatches++;once("LOOKUP CATCH",where+": "+t.getMessage());return;}
  exceptions++;String message=where+": "+t.getClass().getName()+": "+t.getMessage();lastError=message;
  synchronized(Diag.class){if(seen.add("EXCEPTION:"+message)&&seen.size()<10000){StringWriter sw=new StringWriter();t.printStackTrace(new PrintWriter(sw));event("CAUGHT",message+"\n"+sw);}}
 }
 public static void gcHint(Runtime r){gcHints++;long now=System.currentTimeMillis();if(now-lastGC>15000){lastGC=now;r.gc();}}
 public static synchronized String report(){StringBuilder b=new StringBuilder();
  b.append("DIRGE BRIDGE 0.2.4 — runtime report\n").append("Build type: experimental DoJa subset; platform is recorded below\n")
   .append("Audio: Lost Episode MLD ADPM 2-bit decoder -> 48 kHz PCM -> Android streaming AudioTrack; short-stream completion repair + Host-synchronized pause/resume; v0.2.4 identity-shell pass leaves runtime behavior unchanged\n")
   .append("English patch: English 1.0 (TurquoiseHammer / Yuvi) applied; resource retains japanese.dat filename\nSaves: original checkpoints, not save states; local fresh-process Continue verified. Owner v0.1.2 log is consistent with Continue (room loaded without opening), but exact close/reopen steps were not recorded.\n")
   .append("Latest instrumented game code: ").append(stage).append("\nFrames submitted: ").append(frames)
   .append("\nDraw calls: ").append(drawCalls).append("\nTextures generated: ").append(textures)
   .append("\nResource opens: ").append(resourceReads).append("\nGL errors: ").append(glErrors)
   .append("\nOther caught game exceptions: ").append(exceptions).append("\nCaught JAR lookup misses (engine may fall back to SD/Lua): ").append(lookupCatches).append("\nWhole-color-buffer initializations: ").append(frameClears).append("\nMenu projection corrections: ").append(menuProjectionFixes).append("\nGC hints: ").append(gcHints)
   .append("\nLatest resource: ").append(lastResource).append("\nLatest exception: ").append(lastError).append("\n\n");
  for(String s:lines)b.append(s).append('\n');return b.toString();
 }
}
