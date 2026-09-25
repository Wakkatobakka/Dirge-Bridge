import com.wakka.dirge.core.*;
public class TestCameraAssist {
 static int n;static void ok(boolean b,String m){n++;if(!b)throw new AssertionError(m);}static void eq(int a,int b,String m){ok(a==b,m+" expected "+b+" got "+a);}
 static int left=1<<16,up=1<<17,right=1<<18,down=1<<19,star=1<<10,hash=1<<11,action=1<<20;
 static int pl=1<<CameraAssist.PAD_LEFT,pu=1<<CameraAssist.PAD_UP,pr=1<<CameraAssist.PAD_RIGHT,pd=1<<CameraAssist.PAD_DOWN;
 static void reset(){Host.paused=false;Host.ended=false;Host.gameLoader=TestCameraAssist.class.getClassLoader();CameraAssist.enabled=true;CameraAssist.repeat=true;CameraAssist.reverseCamera=false;CameraAssist.reverseAimX=false;CameraAssist.reset();CVincent.game.state=new CGameStateRoom();CHero.hero=new CHero();CVincentRoom.room=new CVincentRoom();CVincentRoom.room.i=CHero.hero.D;}
 static void nativeUnchanged(int raw,String name){int old=CHero.hero.D.turns,oldApplied=CameraAssist.applied;eq(CameraAssist.filter(raw),raw,name+" input");ok(CHero.hero.D.turns==old&&CameraAssist.applied==oldApplied,name+" camera untouched");}
 public static void main(String[]a){
  reset();nativeUnchanged(star,"stationary native camera");
  reset();eq(CameraAssist.filter(up|star),up,"walking star consumed only camera");ok(CHero.hero.D.turns==1&&CHero.hero.D.h,"original field-camera method used");CameraAssist.filter(up|star);ok(CHero.hero.D.turns==1,"native edge latch prevents duplicate");CameraAssist.filter(up);CameraAssist.filter(up|hash);ok(CHero.hero.D.turns==0,"opposite native direction");CameraAssist.filter(up);CameraAssist.filter(up|1);ok(CHero.hero.D.centers==1,"native recenter");

  reset();eq(CameraAssist.filter(pr),hash,"field right pad immediate # pulse");eq(CameraAssist.filter(pr),0,"held field right waits for rate timer");
  reset();eq(CameraAssist.filter(pl),star,"field left pad immediate * pulse");
  reset();eq(CameraAssist.filter(pu),0,"field vertical drift ignored");eq(CameraAssist.filter(pd),0,"field down drift ignored");
  reset();eq(CameraAssist.filter(up|pr),up,"walking right pad preserves movement only");ok(CHero.hero.D.turns==-1,"walking right pad invoked field-camera turn");
  reset();CameraAssist.setPadVector(.3f,.95f);eq(CameraAssist.filter(pu),hash,"field camera uses horizontal analog vector despite vertical dominance");

  // v0.1.9: right-thumb battle aim uses the exact native Left/Right D-pad path.
  reset();CVincentRoom.room.i=CHero.hero.F;
  eq(CameraAssist.filter(pl),left,"right pad native aim left");ok(CameraAssist.aiming,"aim context published");
  eq(CameraAssist.filter(pr),right,"right pad native aim right");
  eq(CameraAssist.filter(up|pr),up|right,"left-pad Up preserved while right thumb aims right");
  eq(CameraAssist.filter(down|pl),down|left,"left-pad Down preserved while right thumb aims left");
  eq(CameraAssist.filter(left|pr),right,"right thumb owns horizontal channel while held");
  eq(CameraAssist.filter(action|pr),action|right,"action coexists with right-thumb native aim");
  CameraAssist.reverseAimX=true;eq(CameraAssist.filter(pr),left,"aim X reverse independent");
  CameraAssist.setPadVector(0,.95f);eq(CameraAssist.filter(0),0,"vertical-only right thumb ignored");

  reset();CVincentRoom.room.i=CHero.hero.F;eq(CameraAssist.filter(left|action),left|action,"native left-pad aim/mode fallback unchanged");
  reset();CameraAssist.noteNavUp(right,1000);CameraAssist.noteNavDown(right,1180);eq(CameraAssist.filter(right),0,"roll repair inserts sampled neutral");eq(CameraAssist.filter(right),right,"roll repair delivers second edge");ok(CameraAssist.rollRepairs>0,"roll repair counted");

  reset();CVincentRoom.room.i=new Object();eq(CameraAssist.filter(pr),0,"non-field/non-battle strips synthetic input");
  reset();CHero.hero.U=false;eq(CameraAssist.filter(pr),0,"control lock strips synthetic input");
  reset();CHero.hero.ag=true;eq(CameraAssist.filter(pr),0,"scripted state strips synthetic input");
  reset();CVincent.game.state=new CGameStatePauseMenu();eq(CameraAssist.filter(pr),0,"pause menu strips synthetic input");
  reset();CVincent.game.state=new CGameStateCutScene();eq(CameraAssist.filter(pr),0,"cutscene strips synthetic input");
  reset();Host.gameLoader=null;eq(CameraAssist.filter(pr|up),up,"loader not ready strips synthetic input");Host.gameLoader=TestCameraAssist.class.getClassLoader();CameraAssist.filter(0);eq(CameraAssist.filter(up|star),up,"early not-ready does not disable later assist");
  reset();Host.paused=true;eq(CameraAssist.filter(pr|up),up,"pause strips synthetic input and preserves native bits");
  reset();CameraAssist.reverseCamera=true;eq(CameraAssist.filter(pr),star,"field camera reverse independent");
  reset();CHero.hero.D.fail=true;eq(CameraAssist.filter(up|pr),up|hash,"first reflection failure falls back to native camera bit");ok(CHero.hero.D.turns==0,"failed reflection did not mutate camera");

  System.out.println("PASS: horizontal-only native battle aim, strength-paced field camera, natural/reverse X, roll edge repair, native fallback, and context gates");
  System.out.println("Assertions: "+n+". Uses MOCK game classes; right-thumb battle aim now intentionally follows the original D-pad path proven by the device left D-pad.");
 }
}
class CGameStateRoom {} class CGameStatePauseMenu {} class CGameStateCutScene {} class CGameStateGui {}
class CIwGame {Object state=new CGameStateRoom(); public Object r(){return state;}}
class CVincent extends CIwGame {static CVincent game=new CVincent(); public static CVincent m(){return game;}}
class CCameraCompass {int turns,centers;boolean fail;public void g(int d){if(fail)throw new IllegalStateException("test failure");turns+=d;}public void C(){centers++;}}
class CCameraField extends CCameraCompass {boolean h;}
class CCameraBattle extends CCameraCompass {static int i=55;int lastDir=-1,lastAmount,calls;public void a(int d,int amount){lastDir=d;lastAmount=amount;calls++;}}
class CHero {static CHero hero=new CHero();CCameraField D=new CCameraField();CCameraBattle F=new CCameraBattle();boolean U=true,ag;public static CHero X(){return hero;}}
class CVincentRoom {static CVincentRoom room=new CVincentRoom();Object i;public static CVincentRoom h(){return room;}}
