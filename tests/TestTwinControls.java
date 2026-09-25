import com.wakka.dirge.core.*;
public class TestTwinControls {
 static int n;static void ok(boolean b,String m){n++;if(!b)throw new AssertionError(m);}static void eq(int a,int b,String m){ok(a==b,m+" expected "+b+" got "+a);}
 static int k(int c){return 1<<c;}
 public static void main(String[]args){
  for(int w:new int[]{240,320,360,384,412,480,600})for(boolean big:new boolean[]{false,true})for(boolean mirror:new boolean[]{false,true})for(int lift:new int[]{0,16,48}){
   TwinLayout l=new TwinLayout(w,big,mirror,lift);ok(l.height>=l.navY+l.navSize&&l.height>=l.actionY+l.actionSize,"vertical fit");
   for(TwinLayout.Area a:l.areas){ok(a.x>=0&&a.y>=0&&a.x+a.w<=w&&a.y+a.h<=l.height,"button bounds");eq(l.hit(a.cx(),a.cy()),a.key,"button hit");
    for(TwinLayout.Area b:l.areas)if(a!=b)ok(!(a.x<b.x+b.w&&a.x+a.w>b.x&&a.y<b.y+b.h&&a.y+a.h>b.y),"no button overlap");}
   ok((l.navX<l.actionX)!=mirror,"mirroring");eq(l.hit(l.camX+l.camW/2,l.camY+l.camH/2),TwinLayout.CAMERA,"camera/aim strip exists");eq(l.hit(l.actionX+l.actionSize/2,l.actionY+l.actionSize/2),20,"action circle exists");
  }
  System.out.println("PASS: 84 width/size/handedness/clearance configurations; button bounds and collision checks");
  TwinLayout l=new TwinLayout(384,false,false,16);TwinInput in=new TwinInput(l);float nx=l.navX+l.navSize/2,ny=l.navY+l.navSize/2,cx=l.camX+l.camW/2,cy=l.camY+l.camH/2,rad=l.camW/2;
  int PL=k(CameraAssist.PAD_LEFT),PU=k(CameraAssist.PAD_UP),PR=k(CameraAssist.PAD_RIGHT),PD=k(CameraAssist.PAD_DOWN);

  // Permanent compact four-way geometry in all contexts.
  CameraAssist.aiming=false;
  eq(l.navigation(nx,l.navY+4,TwinLayout.NONE,false),17,"four-way up");
  eq(l.navigation(l.navX+4,ny,TwinLayout.NONE,false),16,"four-way left");
  eq(l.navigation(l.navX+l.navSize-4,ny,TwinLayout.NONE,false),18,"four-way right");
  eq(l.navigation(nx,l.navY+l.navSize-4,TwinLayout.NONE,false),19,"four-way down");

  // Independent pointer ownership: field movement + analog right pad coexist.
  in.down(41,nx,l.navY+5,0);eq(in.mask(0),k(17),"forward");in.down(77,cx+rad*.65f,cy,0);eq(in.mask(50),k(17)|PR,"movement and right pad coexist");
  eq(in.mask(900),k(17)|PR,"right pad continuous; CameraAssist owns rate timing");in.up(77,920);eq(in.mask(900),k(17),"lifting right pad preserves movement");
  in.down(77,cx-rad*.65f,cy,1000);eq(in.mask(1001),k(17)|PL,"other horizontal direction");in.up(41,1010);eq(in.mask(1002),PL,"lifting movement preserves right pad");
  in.move(77,cx,cy,1100);eq(in.mask(1100),0,"right pad center releases");in.move(77,cx+rad*.4f,cy,1110);eq(in.mask(1110),PR,"retilt reengages");in.clear();eq(in.mask(1110),0,"cancel clears all pointers");

  // Right pad is horizontal-only. Vertical thumb travel is intentionally ignored.
  in.down(91,cx,cy,0);eq(in.mask(0),0,"right pad center neutral");in.move(91,cx,cy+l.camH*.35f,5);eq(in.mask(5),0,"right pad vertical drift ignored");
  in.move(91,cx+rad*.8f,cy+l.camH*.35f,10);eq(in.mask(10),PR,"right pad right despite vertical drift");in.move(91,cx-rad*.8f,cy-l.camH*.35f,15);eq(in.mask(15),PL,"right pad left despite vertical drift");
  in.move(91,cx,cy,20);eq(in.mask(20),0,"horizontal dead zone");in.clear();

  // Aiming context keeps the exact same original four directions on the left pad.
  CameraAssist.aiming=true;in.down(50,nx,l.navY+l.navSize-6,0);eq(in.mask(0),k(19),"aiming keeps left-pad Down");
  in.move(50,l.navX+6,ny,10);eq(in.mask(10),k(16),"aiming left");in.move(50,nx,l.navY+6,20);eq(in.mask(20),k(17),"aiming up");in.up(50,30);in.clear();CameraAssist.aiming=false;

  // Four-way directional hysteresis keeps an engaged axis stable near diagonals.
  in.down(60,nx,l.navY+8,0);eq(in.mask(0),k(17),"starts up");
  in.move(60,nx+l.navSize*.18f,ny-l.navSize*.22f,10);eq(in.mask(10),k(17),"boundary wobble preserves up");
  in.move(60,l.navX+l.navSize-8,ny+l.navSize*.06f,20);eq(in.mask(20),k(18),"clear right switches");in.clear();

  // Right pad and movement each have a single owning finger.
  in.down(2,cx+rad*.7f,cy,0);in.down(7,cx-rad*.7f,cy,10);eq(in.mask(10),PR,"right pad second finger cannot steal");in.up(2);eq(in.mask(20),0,"nonowner cannot stay held");in.clear();
  in.down(2,nx,l.navY+5,0);in.down(7,l.navX+5,ny,10);eq(in.mask(10),k(17),"navigation ownership");in.move(2,l.navX+5,ny+15,20);eq(in.mask(20),k(16),"slide navigation");in.move(2,nx,ny,30);eq(in.mask(30),0,"neutral navigation");in.clear();

  // Sliding a discrete action finger cannot accidentally press a neighboring button.
  TwinLayout.Area a=l.area(20),b=l.area(21);in.down(100,a.cx(),a.cy(),0);eq(in.mask(0),k(20),"action held");in.move(100,b.cx(),b.cy(),10);eq(in.mask(10),0,"action slip does not trigger select");in.move(100,a.cx(),a.cy(),20);eq(in.mask(20),0,"canceled action does not restart");in.clear();

  // v0.2.4 regression: Action and Weapon Aim are independent controls.
  in.down(101,a.cx(),a.cy(),0);eq(in.mask(0),k(20),"action begins normally");
  in.move(101,l.camX+l.camW*.82f,cy,10);eq(in.mask(10),0,"action slide does not steal aim strip");
  in.up(101,20);in.clear();
  CameraAssist.aiming=true;
  in.down(102,l.camX+l.camW*.82f,cy,30);eq(in.mask(30),PR,"weapon aim right touch");
  in.move(102,l.camX+l.camW*.18f,cy,40);eq(in.mask(40),PL,"weapon aim sweeps left");
  in.up(102,50);eq(in.mask(50),0,"weapon aim releases cleanly");in.clear();CameraAssist.aiming=false;
  TwinLayout.Area options=l.area(TwinLayout.OPTIONS);in.down(1,options.cx(),options.cy(),0);eq(in.mask(0),0,"options not a phone key");ok(in.up(1),"options opens on release");in.down(1,options.cx(),options.cy(),0);in.move(1,-5,-5,5);ok(!in.up(1),"options drag cancels");

  in.down(3,cx+rad*.7f,cy,0);ok(in.cameraOffsetX()>.5f,"camera knob x offset");eq((int)(in.cameraOffsetY()*100),0,"camera knob y locked");in.clear();
  for(int code:new int[]{0,1,2,3,20,21,22}){a=l.area(code);in.down(code+300,a.cx(),a.cy(),0);eq(in.mask(0),k(code),"original key mapping "+code);in.up(code+300);}
  System.out.println("PASS: frozen compact four-way movement, horizontal-only right pad, pointer ownership, action safety and original key mappings");
  System.out.println("Assertions: "+n+" (includes geometry combinations; not device touch tests)");
 }
}
