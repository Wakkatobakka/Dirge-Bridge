package com.nttdocomo.ui;
public final class TestPcmStreamPad{
 private static int n;private static void ok(boolean b,String m){n++;if(!b)throw new AssertionError(m);}public static void main(String[]a){
  short[] x={11,22,33};short[] y=PcmStreamPad.ensureMinimum(x,8);ok(y.length==8,"padded length");ok(y[0]==11&&y[1]==22&&y[2]==33,"audio preserved");for(int i=3;i<8;i++)ok(y[i]==0,"tail silence "+i);
  short[] z=PcmStreamPad.ensureMinimum(x,3);ok(z==x,"no copy at threshold");ok(PcmStreamPad.ensureMinimum(x,0)==x,"disabled floor");
  boolean threw=false;try{PcmStreamPad.ensureMinimum(null,8);}catch(IllegalArgumentException e){threw=true;}ok(threw,"null rejected");
  System.out.println("PASS: short-stream padding preserves PCM and appends silence; assertions="+n);
 }}
