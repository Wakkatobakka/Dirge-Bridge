package com.wakka.dirge.core;
import java.lang.reflect.*;
/**
 * Context-aware second-thumb bridge.
 *
 * v0.1.9 keeps the frozen touch baseline: the left thumb is always the original
 * compact four-way handset direction channel. The right thumb is horizontal-only.
 * In third person it turns the field camera. In first-person CCameraBattle it no
 * longer manipulates the camera object directly; instead it injects the exact
 * original handset Left/Right D-pad bits that the game already uses correctly.
 * Native Up/Down from the left D-pad are preserved, and right-thumb vertical drift
 * remains ignored.
 */
public final class CameraAssist {
 public static final int PAD_LEFT=24,PAD_UP=25,PAD_RIGHT=26,PAD_DOWN=27;
 public static final int PAD_MASK=(1<<PAD_LEFT)|(1<<PAD_UP)|(1<<PAD_RIGHT)|(1<<PAD_DOWN);
 public static volatile boolean enabled=true,repeat=true,reverseCamera=false,reverseAimX=false,aiming=false;
 public static volatile int applied,blocked,aimChanges,rollRepairs;
 private static int nativePrevious,nativeSuppressed,lastAimDescriptor,fieldDir;
 private static long nextFieldTurn;
 private static float padX,padY;
 private static ClassLoader loader;
 private static Method heroGet,roomGet,rotate,recenter,gameGet,topState;
 private static Field heroFieldCamera,heroBattleCamera,roomCamera,controls,scripted,manual;
 private static boolean failed;private static int context=-1;
 private static int lastNavReleaseBit,rollBit,rollStage;private static long lastNavReleaseAt;
 private static final int CAMERA=(1<<10)|(1<<11)|1,MOVE=15<<16,HORIZONTAL=(1<<16)|(1<<18),ACTION=1<<20;
 private CameraAssist(){}
 public static synchronized void setPadVector(float x,float y){padX=clamp(x);padY=clamp(y);}
 private static float clamp(float v){return Math.max(-1f,Math.min(1f,v));}
 public static synchronized void reset(){nativePrevious=0;nativeSuppressed=0;fieldDir=0;nextFieldTurn=0;lastAimDescriptor=0;padX=padY=0;aiming=false;context=-1;rollBit=0;rollStage=0;lastNavReleaseBit=0;lastNavReleaseAt=0;}
 public static synchronized void noteNavUp(int bit,long now){if((bit&MOVE)!=0){lastNavReleaseBit=bit&MOVE;lastNavReleaseAt=now;}}
 public static synchronized void noteNavDown(int bit,long now){bit&=MOVE;if(bit==0)return;long dt=now-lastNavReleaseAt;if(bit==lastNavReleaseBit&&dt>=0&&dt<=430){rollBit=bit;rollStage=1;Diag.event("ROLL ASSIST","Double-tap edge armed; gap="+dt+"ms");}lastNavReleaseBit=0;lastNavReleaseAt=0;}
 private static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 private static Method method(Class<?> c,String n,Class<?>...p)throws Exception{Method m=c.getDeclaredMethod(n,p);m.setAccessible(true);return m;}
 private static void resolve()throws Exception{
  ClassLoader l=Host.gameLoader;if(l==null)throw new IllegalStateException("Game class loader not ready");
  if(l==loader&&heroGet!=null)return;
  Class<?> h=Class.forName("CHero",false,l),r=Class.forName("CVincentRoom",false,l);
  Class<?> c=Class.forName("CCameraCompass",false,l),f=Class.forName("CCameraField",false,l);Class.forName("CCameraBattle",false,l);
  gameGet=method(Class.forName("CVincent",false,l),"m");topState=method(Class.forName("CIwGame",false,l),"r");
  heroGet=method(h,"X");roomGet=method(r,"h");heroFieldCamera=field(h,"D");heroBattleCamera=field(h,"F");roomCamera=field(r,"i");
  controls=field(h,"U");scripted=field(h,"ag");manual=field(f,"h");
  rotate=method(c,"g",int.class);recenter=method(c,"C");loader=l;
 }
 private static final class State{Object hero,cam;boolean field,battle,allowed;}
 private static State state()throws Exception{
  resolve();State s=new State();Object game=gameGet.invoke(null),top=game==null?null:topState.invoke(game);
  if(top==null||!"CGameStateRoom".equals(top.getClass().getName()))return s;
  s.hero=heroGet.invoke(null);Object room=roomGet.invoke(null);if(s.hero==null||room==null)return s;
  s.cam=roomCamera.get(room);s.allowed=controls.getBoolean(s.hero)&&!scripted.getBoolean(s.hero);
  s.field=s.allowed&&s.cam!=null&&s.cam==heroFieldCamera.get(s.hero)&&"CCameraField".equals(s.cam.getClass().getName());
  s.battle=s.allowed&&s.cam!=null&&s.cam==heroBattleCamera.get(s.hero)&&"CCameraBattle".equals(s.cam.getClass().getName());
  return s;
 }
 private static void context(State s){int c=s.battle?2:s.field?1:0;aiming=c==2;if(c!=context){context=c;Diag.event("CONTROL CONTEXT",c==2?"Right pad -> NATIVE HORIZONTAL AIM (CCameraBattle)":c==1?"Right pad -> CAMERA (CCameraField)":"Right pad context unavailable / game-owned");}}
 private static int decodePad(int raw){if((raw&(1<<PAD_LEFT))!=0)return -1;if((raw&(1<<PAD_RIGHT))!=0)return 1;if((raw&(1<<PAD_UP))!=0)return -2;if((raw&(1<<PAD_DOWN))!=0)return 2;return 0;}
 private static int cameraBit(int d){if(reverseCamera)d=-d;return d<0?1<<10:d>0?1<<11:0;}
 private static float shaped(float v,float dead){float a=Math.abs(v);if(a<=dead)return 0;float n=(a-dead)/(1f-dead);n=Math.min(1f,n);return v<0?-n:n;}
 private static boolean fieldTurnDue(int d,float magnitude,long now){
  if(d==0){fieldDir=0;nextFieldTurn=0;return false;}
  if(d!=fieldDir){fieldDir=d;long delay=Math.round(470-150*magnitude);nextFieldTurn=now+Math.max(250,delay);return true;}
  if(!repeat)return false;
  if(now<nextFieldTurn)return false;
  long interval=Math.round(650-330*magnitude);nextFieldTurn=now+Math.max(260,interval);return true;
 }
 private static void direct(State s,int use)throws Exception{
  if(use==1)recenter.invoke(s.cam);else rotate.invoke(s.cam,use==(1<<10)?1:-1);
  manual.setBoolean(s.cam,true);applied++;
  Diag.event("CAMERA ASSIST","Applied "+(use==1?"recenter":use==(1<<10)?"* quarter-turn":"# quarter-turn")+"; count="+applied);
 }
 private static int aimBit(int d){
  if(reverseAimX)d=-d;return d<0?1<<16:d>0?1<<18:0;
 }
 private static int applyRollEdge(int out,State s){
  if(rollStage==0)return out;
  if(s==null||!s.field){rollStage=0;rollBit=0;return out;}
  if(rollStage==1){rollStage=2;return out&~MOVE;}
  out=(out&~MOVE)|rollBit;rollStage=0;rollBit=0;rollRepairs++;Diag.event("ROLL ASSIST","Forced neutral + second edge delivered; count="+rollRepairs);return out;
 }
 public static synchronized int filter(int raw){
  int out=raw&~PAD_MASK;nativeSuppressed&=out;
  if(Host.paused||Host.ended){reset();return out;}
  int discrete=decodePad(raw),nativeEdges=(out&~nativePrevious)&CAMERA;nativePrevious=out;
  float vx=padX,vy=padY;if(Math.abs(vx)<.001f&&Math.abs(vy)<.001f&&discrete!=0){vx=discrete==-1?-1:discrete==1?1:0;vy=discrete==-2?-1:discrete==2?1:0;}
  State s=null;
  if(Host.gameLoader!=null&&!failed)try{s=state();context(s);}catch(Exception e){failed=true;aiming=false;Diag.event("INPUT BRIDGE","Context probe disabled after reflection failure: "+e.getClass().getSimpleName()+": "+e.getMessage());}
  else if(Host.gameLoader==null){aiming=false;}
  out=applyRollEdge(out,s);

  // Aim view: use the game's own original Left/Right D-pad channel. This is the
  // exact path the physical/left D-pad already proves works on-device. Right-thumb
  // vertical drift never becomes a game input; native Up/Down from the left pad stay intact.
  if(s!=null&&s.battle){
   int hd=discrete==-1||discrete==1?discrete:0;
   if(hd!=0){int bit=aimBit(hd);out=(out&~HORIZONTAL)|bit;
    if(hd!=lastAimDescriptor){lastAimDescriptor=hd;aimChanges++;Diag.event("AIM NATIVE","Right thumb -> original "+(hd<0?"LEFT":"RIGHT")+" D-pad aim; changes="+aimChanges);}
    return out&~nativeSuppressed;
   }
   lastAimDescriptor=0;
  } else lastAimDescriptor=0;

  // Field view: vertical drift is ignored; horizontal strength controls repeat cadence.
  float fx=shaped(vx,.16f);int fd=fx<0?-1:fx>0?1:0;
  if(s!=null&&s.field&&fd!=0){long now=System.currentTimeMillis();boolean turn=fieldTurnDue(fd,Math.abs(fx),now);int bit=cameraBit(fd);
   if(turn){if((out&MOVE)!=0&&enabled&&(out&ACTION)==0){try{direct(s,bit);}catch(Exception e){failed=true;Diag.event("CAMERA ASSIST","Disabled after reflection failure; original camera bit retained: "+e.getClass().getSimpleName()+": "+e.getMessage());out|=bit;}}else out|=bit;}
   return out&~nativeSuppressed;
  } else fieldTurnDue(0,0,System.currentTimeMillis());

  // Preserve original */#/0 keys from the full keypad / Center button.
  if(enabled&&!failed&&nativeEdges!=0&&(out&MOVE)!=0&&(out&ACTION)==0&&s!=null&&s.field){
   int use=(nativeEdges&(1<<10))!=0?1<<10:(nativeEdges&(1<<11))!=0?1<<11:1;
   try{direct(s,use);nativeSuppressed|=use;}catch(Exception e){failed=true;Diag.event("CAMERA ASSIST","Disabled after reflection failure; original keys retained: "+e.getClass().getSimpleName()+": "+e.getMessage());}
  } else if(nativeEdges!=0&&(out&MOVE)!=0&&s==null)blocked++;
  return out&~nativeSuppressed;
 }
}
