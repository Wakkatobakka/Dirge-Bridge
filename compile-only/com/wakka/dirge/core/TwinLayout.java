package com.wakka.dirge.core;
public final class TwinLayout {
 public static final int NONE=-1,OPTIONS=-2,NAV=-3,CAMERA=-4;
 public float camX,camY,camW,camH;
 public int hit(float x,float y){return NONE;}
 public boolean inAction(float x,float y){return false;}
 public int navigation(float x,float y,int previous,boolean ignored){return NONE;}
 public Area area(int key){return null;}
 public static final class Area { public boolean has(float x,float y){return false;} }
}
