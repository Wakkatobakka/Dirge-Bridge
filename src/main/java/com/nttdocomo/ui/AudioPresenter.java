package com.nttdocomo.ui;
import android.media.*;import java.io.IOException;import com.wakka.dirge.core.Diag;import com.wakka.dirge.core.Host;
/** Android-backed DoJa audio presenter. v0.2.1 keeps long cutscene audio aligned with Host pause/resume. */
public final class AudioPresenter extends MediaPresenter {
 private MediaSound sound;private MediaListener listener;private AudioTrack track;private int volume=100,generation,streamBufferFrames;private boolean paused,sessionPaused,completed,stopped=true,writerActive,playAnnounced;
 public static AudioPresenter getAudioPresenter(int n){return new AudioPresenter();}
 public synchronized void setSound(MediaSound s){if(sound==s)return;releaseTrack();sound=s;}
 public synchronized void setMediaListener(MediaListener l){listener=l;}
 public synchronized void setAttribute(int a,int b){if(a==4){volume=Math.max(0,Math.min(100,b));applyVolume();}}
 private void applyVolume(){if(track!=null)try{track.setVolume(volume/100f);}catch(Throwable ignored){}}
 private void notifyEvent(int event){final MediaListener l=listener;if(l==null)return;try{l.mediaAction(this,event,0);}catch(Throwable t){Diag.event("AUDIO LISTENER","event "+event+" failed: "+t.getClass().getSimpleName()+": "+t.getMessage());}}
 private synchronized void releaseTrack(){generation++;writerActive=false;if(track!=null)try{track.release();}catch(Throwable ignored){}track=null;streamBufferFrames=0;paused=false;sessionPaused=false;completed=false;stopped=true;playAnnounced=false;}
 private AudioTrack buildTrack()throws IOException{
  int min=AudioTrack.getMinBufferSize(PcmResampler.TARGET_RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);if(min<=0)throw new IOException("AudioTrack min buffer failed: "+min);
  int buffer=Math.max(16384,min*2);streamBufferFrames=Math.max(1,buffer/2);
  AudioAttributes aa=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
  AudioFormat af=new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(PcmResampler.TARGET_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();AudioTrack at;
  try{at=new AudioTrack.Builder().setAudioAttributes(aa).setAudioFormat(af).setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(buffer).build();}
  catch(Throwable first){try{at=new AudioTrack(aa,af,buffer,AudioTrack.MODE_STREAM,AudioManager.AUDIO_SESSION_ID_GENERATE);}catch(Throwable second){throw new IOException("48 kHz stream creation failed: "+first.getClass().getSimpleName()+" / "+second.getClass().getSimpleName(),second);}}
  if(at.getState()!=AudioTrack.STATE_INITIALIZED){try{at.release();}catch(Throwable ignored){}throw new IOException("48 kHz streaming AudioTrack initialization failed; min="+min+" buffer="+buffer);}
  Diag.once("AUDIO TRACK","48 kHz mono streaming AudioTrack initialized; minBuffer="+min+" bytes; buffer="+buffer+" bytes; shortFloor="+streamBufferFrames+" frames");return at;
 }
 private synchronized AudioTrack ensureTrack()throws IOException{if(track!=null)return track;if(!(sound instanceof MldSound))throw new IOException("Unsupported MediaSound implementation");track=buildTrack();applyVolume();stopped=true;paused=false;sessionPaused=false;completed=false;playAnnounced=false;return track;}
 private boolean hostPaused(){return Host.paused;}
 private boolean syncSessionPause(final AudioTrack at,final int gen,final boolean primed,final int audibleFrames,final int streamFrames){
  while(hostPaused()){
   synchronized(this){if(gen!=generation||track!=at)return false;if(!sessionPaused){sessionPaused=true;if(primed&&!paused){try{at.pause();}catch(Throwable ignored){}}Diag.event("AUDIO SESSION PAUSE","Host paused; playbackHead="+(at.getPlaybackHeadPosition()&0xffffffffL)+" audibleFrames="+audibleFrames+" streamFrames="+streamFrames);}}
   try{Thread.sleep(15);}catch(InterruptedException ignored){}
  }
  synchronized(this){if(gen!=generation||track!=at)return false;if(sessionPaused){sessionPaused=false;if(primed&&!paused&&!stopped){try{at.play();if(!playAnnounced){playAnnounced=true;Diag.event("AUDIO PLAY","48 kHz streamed MLD playback started after session resume; audibleFrames="+audibleFrames+" streamFrames="+streamFrames+" volume="+volume);notifyEvent(1);}else Diag.event("AUDIO SESSION RESUME","Host resumed; playbackHead="+(at.getPlaybackHeadPosition()&0xffffffffL));}catch(Throwable t){Diag.event("AUDIO ERROR","session resume failed: "+t.getClass().getSimpleName()+": "+t.getMessage());return false;}}else Diag.event("AUDIO SESSION RESUME","Host resumed while media remained paused/stopped");}}
  return true;
 }
 private void writer(final AudioTrack at,final short[] streamPcm,final int audibleFrames,final int gen){new Thread(new Runnable(){public void run(){try{
   int pos=0;boolean primed=false;while(pos<streamPcm.length){if(!syncSessionPause(at,gen,primed,audibleFrames,streamPcm.length))return;synchronized(AudioPresenter.this){if(gen!=generation||track!=at)return;}int count=Math.min(4096,streamPcm.length-pos),w=at.write(streamPcm,pos,count,AudioTrack.WRITE_BLOCKING);if(w<=0)throw new IOException("AudioTrack write returned "+w);pos+=w;if(!primed){primed=true;synchronized(AudioPresenter.this){if(gen!=generation||track!=at)return;if(hostPaused()){sessionPaused=true;}else{at.play();paused=false;stopped=false;playAnnounced=true;}}if(!hostPaused()){Diag.event("AUDIO PLAY","48 kHz streamed MLD playback started; audibleFrames="+audibleFrames+" streamFrames="+streamPcm.length+" volume="+volume);notifyEvent(1);}else Diag.event("AUDIO SESSION PAUSE","Stream primed while Host was paused; playback deferred");}}
   while(true){if(!syncSessionPause(at,gen,primed,audibleFrames,streamPcm.length))return;synchronized(AudioPresenter.this){if(gen!=generation||track!=at)return;if(paused){try{AudioPresenter.this.wait(50);}catch(InterruptedException ignored){}continue;}}long head=at.getPlaybackHeadPosition()&0xffffffffL;if(head>=streamPcm.length)break;try{Thread.sleep(15);}catch(InterruptedException ignored){}}
   synchronized(AudioPresenter.this){if(gen!=generation||track!=at)return;completed=true;paused=false;sessionPaused=false;stopped=true;writerActive=false;}Diag.event("AUDIO COMPLETE","48 kHz streamed MLD playback reached end; audibleFrames="+audibleFrames+" streamFrames="+streamPcm.length);notifyEvent(3);
  }catch(Throwable t){synchronized(AudioPresenter.this){if(gen!=generation||track!=at)return;writerActive=false;stopped=true;}Diag.event("AUDIO ERROR","stream failed: "+t.getClass().getSimpleName()+": "+t.getMessage());notifyEvent(2);}}},"DirgeAudio").start();}
 public void play(){try{final AudioTrack at;final short[] streamPcm;final int audibleFrames;final int gen; synchronized(this){
   if(paused&&track!=null){paused=false;stopped=false;if(!hostPaused()&&!sessionPaused){track.play();if(!playAnnounced)playAnnounced=true;Diag.event("AUDIO PLAY","48 kHz stream resumed");notifyEvent(1);}else{sessionPaused=hostPaused()||sessionPaused;Diag.event("AUDIO SESSION PAUSE","Media resume deferred because Host is paused");}notifyAll();return;}
   if(writerActive&&!stopped)return;if(completed||stopped)releaseTrack();at=ensureTrack();MldDecoder.Decoded d=((MldSound)sound).decoded();short[] pcm=PcmResampler.to48k(d.pcm,d.sampleRate);audibleFrames=pcm.length;streamPcm=PcmStreamPad.ensureMinimum(pcm,streamBufferFrames);if(streamPcm.length!=pcm.length)Diag.event("AUDIO PAD","Short MLD padded with silence for Android stream completion; audibleFrames="+pcm.length+" streamFrames="+streamPcm.length);gen=++generation;writerActive=true;completed=false;stopped=false;paused=false;sessionPaused=hostPaused();
  }Diag.event("AUDIO QUEUE","Resampled MLD "+((MldSound)sound).decoded().sampleRate+" -> 48000 Hz; audibleFrames="+audibleFrames+" streamFrames="+streamPcm.length);writer(at,streamPcm,audibleFrames,gen);
  }catch(Throwable t){Diag.event("AUDIO ERROR","play failed: "+t.getClass().getSimpleName()+": "+t.getMessage());notifyEvent(2);}}
 public synchronized void pause(){if(track==null||paused||stopped)return;try{track.pause();paused=true;notifyAll();}catch(Throwable t){Diag.event("AUDIO ERROR","pause failed: "+t.getMessage());return;}notifyEvent(5);}
 public void restart(){synchronized(this){if(paused&&track!=null){paused=false;stopped=false;if(!hostPaused()&&!sessionPaused){try{track.play();if(!playAnnounced)playAnnounced=true;}catch(Throwable t){Diag.event("AUDIO ERROR","restart failed: "+t.getMessage());return;}}else sessionPaused=true;notifyAll();notifyEvent(6);return;}}play();}
 public void stop(){synchronized(this){releaseTrack();notifyAll();}Diag.event("AUDIO STOP","Presenter stopped and stream released");notifyEvent(2);}
}
