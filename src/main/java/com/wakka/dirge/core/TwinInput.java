package com.wakka.dirge.core;
import java.util.*;
/** Pointer-owned controls. Right pad publishes an analog vector plus private synthetic directions. */
public final class TwinInput {
 private static final class Contact{int role,key,dir;float x,y;boolean canceled;Contact(int r,float x,float y){role=r;key=r;this.x=x;this.y=y;}}
 private final Map<Integer,Contact> contacts=new HashMap<Integer,Contact>();private TwinLayout layout;
 public TwinInput(TwinLayout l){layout=l;}public void layout(TwinLayout l){clear();layout=l;}public void clear(){contacts.clear();CameraAssist.setPadVector(0,0);}public int count(){return contacts.size();}
 private boolean occupied(int role){for(Contact c:contacts.values())if(c.role==role)return true;return false;}
 private static boolean navKey(int k){return k>=16&&k<=19;}
 public void down(int id,float x,float y,long now){if(contacts.containsKey(id))return;int r=layout.hit(x,y);if((r==TwinLayout.NAV||r==TwinLayout.CAMERA)&&occupied(r))r=TwinLayout.NONE;Contact c=new Contact(r,x,y);contacts.put(id,c);update(c,x,y);if(c.role==TwinLayout.NAV&&navKey(c.key))CameraAssist.noteNavDown(1<<c.key,now);}
 public void move(int id,float x,float y,long now){Contact c=contacts.get(id);if(c==null)return;int old=c.key;
  // v0.2.4: Weapon Aim / Field Camera is an independent control. Pointer ownership
  // never transfers from Action into the aim strip; lift from Action and touch the strip.
  update(c,x,y);if(c.role==TwinLayout.NAV&&old!=c.key){if(navKey(old))CameraAssist.noteNavUp(1<<old,now);if(navKey(c.key))CameraAssist.noteNavDown(1<<c.key,now);}}
 private void update(Contact c,float x,float y){c.x=x;c.y=y;if(c.role==TwinLayout.NAV){c.key=layout.navigation(x,y,c.key,true);return;}
  if(c.role==TwinLayout.CAMERA){float dx=(x-layout.camX-layout.camW/2)/(layout.camW/2);int d=0;
   // The right control is deliberately horizontal-only in both contexts. Vertical thumb drift is ignored.
   float dead=c.dir==0?.14f:.10f;if(Math.abs(dx)>=dead)d=dx<0?-1:1;c.dir=d;c.key=TwinLayout.NONE;return;}
  if(c.role>=0||c.role==TwinLayout.OPTIONS){TwinLayout.Area a=layout.area(c.role);boolean inside=c.role==20?layout.inAction(x,y):a!=null&&a.has(x,y);if(a==null||!inside)c.canceled=true;c.key=c.canceled?TwinLayout.NONE:c.role;}}
 public boolean up(int id,long now){Contact c=contacts.remove(id);if(c!=null&&c.role==TwinLayout.NAV&&navKey(c.key))CameraAssist.noteNavUp(1<<c.key,now);if(c!=null&&c.role==TwinLayout.CAMERA)CameraAssist.setPadVector(0,0);return c!=null&&c.key==TwinLayout.OPTIONS&&!c.canceled;}
 public boolean up(int id){return up(id,System.currentTimeMillis());}
 public int mask(long now){int m=0;float vx=0,vy=0;boolean haveVector=false;for(Contact c:contacts.values()){if(c.role==TwinLayout.NAV){int nk=layout.navigation(c.x,c.y,c.key,true);c.key=nk;}
   if(c.role==TwinLayout.CAMERA){vx=Math.max(-1,Math.min(1,(c.x-layout.camX-layout.camW/2)/(layout.camW/2)));vy=Math.max(-1,Math.min(1,(c.y-layout.camY-layout.camH/2)/(layout.camH/2)));haveVector=true;int bit=c.dir==-1?CameraAssist.PAD_LEFT:c.dir==1?CameraAssist.PAD_RIGHT:-1;if(bit>=0)m|=1<<bit;}else if(c.key>=0)m|=1<<c.key;}
  CameraAssist.setPadVector(haveVector?vx:0,haveVector?vy:0);return m;}
 public float cameraOffsetX(){for(Contact c:contacts.values())if(c.role==TwinLayout.CAMERA)return Math.max(-1,Math.min(1,(c.x-layout.camX-layout.camW/2)/(layout.camW/2)));return 0;}
 public float cameraOffsetY(){return 0;}
}
