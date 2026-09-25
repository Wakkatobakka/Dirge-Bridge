package com.wakka.dirge.core;
import java.util.*;
/** Density-independent geometry for the frozen two-thumb baseline. v0.2.4 keeps the v0.2.2 Action/camera geometry unchanged. */
public final class TwinLayout {
 public static final int NONE=-1,OPTIONS=-2,NAV=-3,CAMERA=-4;
 public final float width,height,navX,navY,navSize,camX,camY,camW,camH,camSize,actionX,actionY,actionSize;
 public final boolean mirrored;
 public final List<Area> areas=new ArrayList<Area>();
 public static final class Area {
  public final int key;public final String title,hint;public final float x,y,w,h;
  Area(int k,String t,String hint,float x,float y,float w,float h){key=k;title=t;this.hint=hint;this.x=x;this.y=y;this.w=w;this.h=h;}
  public boolean has(float a,float b){return a>=x&&b>=y&&a<x+w&&b<y+h;}
  public float cx(){return x+w/2;}public float cy(){return y+h/2;}
 }
 public TwinLayout(float w,boolean large,boolean mirror,int lift){
  width=Math.max(240,w);mirrored=mirror;float gap=6,margin=7;
  float col=(width-2*margin-2*gap)/3;
  add(1,"Cerberus","1 · weapon",margin,7,col,42);
  add(2,"Machine gun","2 · weapon",margin+col+gap,7,col,42);
  add(3,"Rifle","3 · weapon",margin+2*(col+gap),7,col,42);
  float side=(width-2*margin-gap)/2;
  // Field camera now takes the former Action strip. Action gets the large right-thumb circle below.
  camX=mirror?margin:margin+side+gap;camY=55;camW=side;camH=48;camSize=camW;
  float softX=mirror?margin+side+gap:margin;
  add(21,"Select","L",softX,55,(side-gap)/2,48);
  add(22,"Back","R",softX+(side+gap)/2,55,(side-gap)/2,48);
  navSize=Math.min(large?164:146,(width-66)/2);actionSize=Math.min(large?172:154,(width-66)/2);
  float left=margin;navX=mirror?width-margin-navSize:left;
  actionX=mirror?margin:width-margin-actionSize;
  navY=128;actionY=navY;
  // Action remains an Area so ordinary pointer ownership/masking stays unchanged.
  add(20,"ACTION","OK · interact · fire",actionX,actionY,actionSize,actionSize);
  float centerLeft=Math.min(navX+navSize,actionX+actionSize),centerRight=Math.max(navX,actionX);
  float centerW=Math.max(48,centerRight-centerLeft-8),centerX=(width-centerW)/2;
  float centerTop=navY+Math.max(0,(Math.max(navSize,actionSize)-98)/2);
  add(0,"Center","camera",centerX,centerTop,centerW,46);
  add(OPTIONS,"Pad","options",centerX,centerTop+52,centerW,46);
  height=Math.max(Math.max(navY+navSize,actionY+actionSize),centerTop+98)+Math.max(0,Math.min(48,lift))+8;
 }
 private void add(int k,String t,String hint,float x,float y,float w,float h){areas.add(new Area(k,t,hint,x,y,w,h));}
 public boolean inNav(float x,float y){return x>=navX&&y>=navY&&x<navX+navSize&&y<navY+navSize;}
 public boolean inCam(float x,float y){return x>=camX&&y>=camY&&x<camX+camW&&y<camY+camH;}
 public boolean inAction(float x,float y){float dx=x-(actionX+actionSize/2),dy=y-(actionY+actionSize/2);return dx*dx+dy*dy<actionSize*actionSize*.25f;}
 private int fourWay(float dx,float dy,int previous){float ax=Math.abs(dx),ay=Math.abs(dy),dead=navSize*.075f;if(dx*dx+dy*dy<dead*dead)return NONE;boolean ph=previous==16||previous==18,pv=previous==17||previous==19;if(ph&&ay<=ax*1.28f)return dx<0?16:18;if(pv&&ax<=ay*1.28f)return dy<0?17:19;return ax>ay?(dx<0?16:18):(dy<0?17:19);}
 public int navigation(float x,float y,int previous,boolean ignored){if(!inNav(x,y))return NONE;float dx=x-navX-navSize/2,dy=y-navY-navSize/2;return fourWay(dx,dy,previous);}
 public int navigation(float x,float y,int previous){return navigation(x,y,previous,true);}public int navigation(float x,float y){return navigation(x,y,NONE,true);}
 public int hit(float x,float y){if(inNav(x,y))return NAV;if(inCam(x,y))return CAMERA;for(Area a:areas){if(a.key==20){if(inAction(x,y))return 20;}else if(a.has(x,y))return a.key;}return NONE;}
 public Area area(int key){for(Area a:areas)if(a.key==key)return a;return null;}
}
