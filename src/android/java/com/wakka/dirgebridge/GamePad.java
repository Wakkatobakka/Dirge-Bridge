package com.wakka.dirgebridge;
import android.app.AlertDialog;import android.content.*;import android.graphics.*;import android.os.SystemClock;import android.view.*;
import com.wakka.dirge.core.*;
/** Context-aware touch controls layered over the proven runtime. */
final class GamePad extends View {
 boolean classic,large,mirrored;int lift;
 private final float density;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private ControlLayout legacy;private PadInput oldInput;private TwinLayout twin;private TwinInput twinInput;
 private boolean repeat,reverseCamera,reverseAimX,walking;private int shownMask;private int legacyOptionsId=-1;private boolean legacyOptionsValid;
 private final SharedPreferences prefs;
 private final Runnable tick=new Runnable(){public void run(){if(Host.paused||Host.ended){clear();return;}if(twinInput!=null&&twinInput.count()>0){publish();postDelayed(this,25);}}};
 GamePad(Context c){super(c);density=getResources().getDisplayMetrics().density;prefs=c.getSharedPreferences("camera-controls-v014",0);repeat=prefs.getBoolean("repeat",true);walking=prefs.getBoolean("walking",true);reverseCamera=prefs.getBoolean("reverseCamera",false);reverseAimX=prefs.getBoolean("reverseAimX",false);CameraAssist.enabled=walking;CameraAssist.repeat=repeat;CameraAssist.reverseCamera=reverseCamera;CameraAssist.reverseAimX=reverseAimX;setFocusable(true);setClickable(true);setContentDescription("Compact four-way movement on the left. The upper-right field-camera strip turns or aims left/right. The large lower-right Action button fires, confirms, or interacts.");}
 void configure(boolean c,boolean l,boolean m,int b){clear();classic=c;large=l;mirrored=m;lift=b;legacy=null;twin=null;requestLayout();invalidate();Diag.event("CONTROL MODE",(classic?"Original keypad":"Compact 4-way baseline + horizontal camera/aim")+"; walking assist="+walking+"; repeat="+repeat+"; camera reverse="+reverseCamera+"; aim reverseX="+reverseAimX); }
 int heightForWidth(int pixels){float w=Math.max(240,pixels/density);return Math.round(density*(!classic?new TwinLayout(w,large,mirrored,lift).height:(new ControlLayout(w,true,large,mirrored,lift).height+32)));}
 private void rebuild(){float w=Math.max(240,getWidth()/density);clear();if(!classic){twin=new TwinLayout(w,large,mirrored,lift);twinInput=new TwinInput(twin);CameraAssist.enabled=walking;CameraAssist.repeat=repeat;CameraAssist.reverseCamera=reverseCamera;CameraAssist.reverseAimX=reverseAimX;legacy=null;oldInput=null;}else{legacy=new ControlLayout(w,true,large,mirrored,lift);oldInput=new PadInput(legacy);twin=null;twinInput=null;}}
 protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);if(width==0)width=(int)(360*density);setMeasuredDimension(width,resolveSize(heightForWidth(width),h));}
 protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);rebuild();}
 protected void onDetachedFromWindow(){clear();super.onDetachedFromWindow();}
 void clear(){legacyOptionsId=-1;legacyOptionsValid=false;removeCallbacks(tick);if(oldInput!=null)oldInput.clear();if(twinInput!=null)twinInput.clear();shownMask=0;Host.cancelTouch();CameraAssist.reset();invalidate();}
 private void publish(){shownMask=Host.paused||Host.ended?0:twinInput!=null?twinInput.mask(SystemClock.uptimeMillis()):oldInput!=null?oldInput.mask():0;Host.setTouchKeys(shownMask);invalidate();}
 private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold,float max){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTypeface(bold?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);float measure=p.measureText(s);if(measure>max)p.setTextSize(size*max/measure);c.drawText(s,x,y-(p.ascent()+p.descent())/2,p);}
 private void box(Canvas c,float x,float y,float w,float h,boolean circle,boolean on,boolean action){p.setStyle(Paint.Style.FILL);p.setColor(on?0xff9d4464:action?0xff602b40:0xff202c3e);RectF r=new RectF(x,y,x+w,y+h);if(circle)c.drawOval(r,p);else c.drawRoundRect(r,11,11,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(on?0xffe396b1:0xff44536a);if(circle)c.drawOval(r,p);else c.drawRoundRect(r,11,11,p);p.setStyle(Paint.Style.FILL);}
 protected void onDraw(Canvas c){super.onDraw(c);if(twin==null&&legacy==null)rebuild();c.save();c.scale(density,density);
 if(twin!=null){
  for(TwinLayout.Area a:twin.areas){boolean on=a.key>=0&&(shownMask&(1<<a.key))!=0;boolean action=a.key==20;box(c,a.x,a.y,a.w,a.h,action,on,action);if(action){text(c,a.title,a.cx(),a.cy()-7,23,0xfff6f7fa,true,a.w*.72f);text(c,a.hint,a.cx(),a.cy()+17,10,0xffd7c9cf,false,a.w*.72f);}else{text(c,a.title,a.cx(),a.cy()-6,a.w<62?12:14,0xfff6f7fa,true,a.w-6);text(c,a.hint,a.cx(),a.cy()+12,10,0xffbbc7d8,false,a.w-5);}}
  drawNav(c,twin.navX,twin.navY,twin.navSize);
  float cx=twin.camX+twin.camW/2,cy=twin.camY+twin.camH/2;
  boolean padOn=(shownMask&CameraAssist.PAD_MASK)!=0;
  box(c,twin.camX,twin.camY,twin.camW,twin.camH,false,padOn,false);
  text(c,CameraAssist.aiming?"WEAPON AIM  ◀   ▶":"FIELD CAMERA  ◀   ▶",cx,cy-5,11,0xffe2e8f0,true,twin.camW-12);
  float ox=twinInput==null?0:twinInput.cameraOffsetX();p.setStrokeWidth(1.2f);p.setColor(0xff65758e);c.drawLine(twin.camX+twin.camW*.18f,cy+11,twin.camX+twin.camW*.82f,cy+11,p);p.setColor(0xff8ca1bc);c.drawCircle(cx+ox*twin.camW*.28f,cy+11,4.5f,p);
 } else {
  if(!classic)drawNav(c,legacy.navX,legacy.navY,legacy.navSize);
  for(ControlLayout.Area a:legacy.areas){boolean on=a.key>=0&&(shownMask&(1<<a.key))!=0;box(c,a.x,a.y,a.w,a.h,a.circle,on,a.key==20);float size=a.key==20?20:classic?22:14;if(a.hint.isEmpty())text(c,a.title,a.cx(),a.cy(),size,0xfff6f7fa,true,a.w-8);else{text(c,a.title,a.cx(),a.cy()-6,size,0xfff6f7fa,true,a.w-8);text(c,a.hint,a.cx(),a.cy()+13,10,0xffbbc7d8,false,a.w-6);}}
  box(c,7,legacy.height+3,legacy.width-14,25,false,false,false);text(c,"Camera pad options",legacy.width/2,legacy.height+15,12,0xffd3deec,true,legacy.width-30);
 }
 c.restore();}
 private void drawNav(Canvas c,float x,float y,float size){
  text(c,CameraAssist.aiming?"AIM / ORIGINAL · 4-WAY":"MOVE · 4-WAY",x+size/2,y-10,10,0xffacbbce,true,size);p.setColor(0xff182333);c.drawRoundRect(new RectF(x,y,x+size,y+size),22,22,p);
  float s=size*.39f;float[][]xy={{x+size*.305f,y+size*.015f},{x+size*.015f,y+size*.305f},{x+size*.595f,y+size*.305f},{x+size*.305f,y+size*.595f}};int[]k={17,16,18,19};String[]t={"▲","◀","▶","▼"};
  for(int i=0;i<4;i++){box(c,xy[i][0],xy[i][1],s,s,false,(shownMask&(1<<k[i]))!=0,false);text(c,t[i],xy[i][0]+s/2,xy[i][1]+s/2,23,0xfff6f7fa,true,s-6);}
  p.setColor(0xff64758a);c.drawCircle(x+size/2,y+size/2,3.5f,p);
 }
 public boolean onTouchEvent(MotionEvent e){if(twin==null&&legacy==null)rebuild();if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);int a=e.getActionMasked(),ix=e.getActionIndex();long now=SystemClock.uptimeMillis();if(a==MotionEvent.ACTION_CANCEL||Host.paused||Host.ended){clear();return true;}
  
  for(int i=0;i<e.getPointerCount();i++){if(i==ix&&(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP))continue;int id=e.getPointerId(i);float x=e.getX(i)/density,y=e.getY(i)/density;if(id==legacyOptionsId){legacyOptionsValid&=x>=7&&x<legacy.width-7&&y>=legacy.height&&y<legacy.height+32;}else if(twinInput!=null)twinInput.move(id,x,y,now);else oldInput.move(id,x,y);}
  boolean options=false;
  if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_POINTER_DOWN){int id=e.getPointerId(ix);float x=e.getX(ix)/density,y=e.getY(ix)/density;if(twinInput!=null)twinInput.down(id,x,y,now);else if(y>=legacy.height&&y<legacy.height+32&&legacyOptionsId==-1){legacyOptionsId=id;legacyOptionsValid=true;}else oldInput.down(id,x,y);}
  else if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP){int id=e.getPointerId(ix);if(id==legacyOptionsId){options=legacyOptionsValid;legacyOptionsId=-1;legacyOptionsValid=false;}else if(twinInput!=null)options=twinInput.up(id,now);else oldInput.up(id);}
  publish();removeCallbacks(tick);if(twinInput!=null&&twinInput.count()>0)postDelayed(tick,25);
  if(options)showOptions();if(a==MotionEvent.ACTION_UP)performClick();return true;
 }
 public boolean performClick(){super.performClick();return true;}
 private void save(){prefs.edit().putBoolean("walking",walking).putBoolean("repeat",repeat).putBoolean("reverseCamera",reverseCamera).putBoolean("reverseAimX",reverseAimX).remove("camera").remove("reverseAimY").remove("reverse").apply();CameraAssist.enabled=walking;CameraAssist.repeat=repeat;CameraAssist.reverseCamera=reverseCamera;CameraAssist.reverseAimX=reverseAimX;clear();twin=null;legacy=null;ViewGroup.LayoutParams lp=getLayoutParams();if(lp!=null&&getWidth()>0){lp.height=heightForWidth(getWidth());setLayoutParams(lp);}requestLayout();invalidate();}
 private void showOptions(){clear();String[] names={"Turn field camera while moving","Hold field-camera turn to repeat","Reverse third-person camera","Reverse horizontal weapon aim"};boolean[] checked={walking,repeat,reverseCamera,reverseAimX};AlertDialog d=new AlertDialog.Builder(getContext()).setTitle("Camera / aim options").setMultiChoiceItems(names,checked,(dialog,which,on)->{if(which==0)walking=on;else if(which==1)repeat=on;else if(which==2)reverseCamera=on;else reverseAimX=on;save();}).setPositiveButton("Done",(dialog,which)->{}).setNeutralButton("How it works",(dialog,which)->showHelp()).create();show(d);}
 private void showHelp(){AlertDialog d=new AlertDialog.Builder(getContext()).setTitle("Frozen control baseline").setMessage("Left thumb:\nA compact four-way D-pad is now permanent in both third-person and first-person play. It carries the game's original Up / Down / Left / Right inputs.\n\nRight thumb:\nIn third person it turns the original field camera left/right. In first-person aiming it adjusts the weapon only left/right. Vertical motion on the right control is deliberately ignored, so thumb drift cannot become an unwanted up/down command.\n\nNatural, non-inverted directions are the default.\n\nA rapid same-direction movement double-tap gets a guaranteed neutral sample between taps to make the game's roll detector more reliable.\n\nAction fires/interacts. Center recenters the normal field camera. The toolbar Keypad remains the full original phone-key fallback.").setPositiveButton("Close",null).create();show(d);}
 private void show(AlertDialog d){if(getContext() instanceof GameActivity)((GameActivity)getContext()).showModal(d);else d.show();}
 static int map(int k){switch(k){case KeyEvent.KEYCODE_DPAD_LEFT:return 16;case KeyEvent.KEYCODE_DPAD_UP:return 17;case KeyEvent.KEYCODE_DPAD_RIGHT:return 18;case KeyEvent.KEYCODE_DPAD_DOWN:return 19;case KeyEvent.KEYCODE_ENTER:case KeyEvent.KEYCODE_DPAD_CENTER:case KeyEvent.KEYCODE_BUTTON_A:return 20;case KeyEvent.KEYCODE_BUTTON_X:case KeyEvent.KEYCODE_Q:return 21;case KeyEvent.KEYCODE_BUTTON_B:case KeyEvent.KEYCODE_E:return 22;case KeyEvent.KEYCODE_STAR:return 10;case KeyEvent.KEYCODE_POUND:return 11;default:return k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9?k-KeyEvent.KEYCODE_0:-1;}}
}
