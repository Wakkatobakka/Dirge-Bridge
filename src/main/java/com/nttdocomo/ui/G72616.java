package com.nttdocomo.ui;
/**
 * 16 kbit/s (2-bit) G.726 / G.723-16 ADPCM decoder.
 *
 * This implementation is a clean Java port of the classic Sun Microsystems
 * g72x/g723_16 reference algorithm. The original reference source states that
 * it was provided for unrestricted use and may be copied or modified without
 * charge. See THIRD-PARTY-NOTICES.txt in the preservation package.
 */
final class G72616 {
 private static final int[] POWER2={1,2,4,8,0x10,0x20,0x40,0x80,0x100,0x200,0x400,0x800,0x1000,0x2000,0x4000};
 private static final int[] DQL={116,365,365,116};
 private static final int[] WI={-704,14048,14048,-704};
 private static final int[] FI={0,0xE00,0xE00,0};
 private static final class State{
  int yl=34816,yu=544,dms,dml,ap,td;
  final int[] a={0,0},b=new int[6],pk={0,0},dq={32,32,32,32,32,32},sr={32,32};
 }
 private G72616(){}
 private static int s16(int x){return (short)x;}
 private static int quan(int val){for(int i=0;i<POWER2.length;i++)if(val<POWER2[i])return i;return POWER2.length;}
 private static int fmult(int an,int srn){
  int anmag=an>0?an:(-an)&0x1fff,anexp=quan(anmag)-6;
  int anmant=anmag==0?32:(anexp>=0?anmag>>anexp:anmag<<(-anexp));
  int wanexp=anexp+((srn>>6)&0xf)-13,wanmant=(anmant*(srn&077)+0x30)>>4,retval;
  if(wanexp>=0)retval=(wanmant<<wanexp)&0x7fff;else retval=(-wanexp>=31)?0:wanmant>>(-wanexp);
  return ((an^srn)<0)?-retval:retval;
 }
 private static int predictorZero(State s){int sezi=0;for(int i=0;i<6;i++)sezi+=fmult(s.b[i]>>2,s.dq[i]);return sezi;}
 private static int predictorPole(State s){return fmult(s.a[1]>>2,s.sr[1])+fmult(s.a[0]>>2,s.sr[0]);}
 private static int stepSize(State s){
  if(s.ap>=256)return s.yu;int y=s.yl>>6,dif=s.yu-y,al=s.ap>>2;
  if(dif>0)y+=(dif*al)>>6;else if(dif<0)y+=(dif*al+0x3f)>>6;return y;
 }
 private static int reconstruct(int sign,int dqln,int y){
  int dql=dqln+(y>>2);if(dql<0)return sign!=0?-0x8000:0;
  int dex=(dql>>7)&15,dqt=128+(dql&127),shift=14-dex;
  int dq=shift>=0?(dqt<<7)>>shift:(dqt<<7)<<(-shift);
  return sign!=0?dq-0x8000:dq;
 }
 private static void update(int codeSize,int y,int wi,int fi,int dq,int sr,int dqsez,State s){
  int pk0=dqsez<0?1:0,mag=dq&0x7fff;
  int ylint=s.yl>>15,ylfrac=(s.yl>>10)&0x1f,thr1=(32+ylfrac)<<ylint,thr2=ylint>9?(31<<10):thr1,dqthr=(thr2+(thr2>>1))>>1;
  int tr=(s.td==0||mag<=dqthr)?0:1;
  s.yu=s16(y+((wi-y)>>5));if(s.yu<544)s.yu=544;else if(s.yu>5120)s.yu=5120;s.yl+=s.yu+((-s.yl)>>6);
  int a2p=0;
  if(tr==1){s.a[0]=s.a[1]=0;for(int i=0;i<6;i++)s.b[i]=0;}
  else{
   int pks1=pk0^s.pk[0];a2p=s16(s.a[1]-(s.a[1]>>7));
   if(dqsez!=0){
    int fa1=pks1!=0?s.a[0]:-s.a[0];
    if(fa1<-8191)a2p=s16(a2p-0x100);else if(fa1>8191)a2p=s16(a2p+0xff);else a2p=s16(a2p+(fa1>>5));
    if((pk0^s.pk[1])!=0){if(a2p<=-12160)a2p=-12288;else if(a2p>=12416)a2p=12288;else a2p=s16(a2p-0x80);}
    else{if(a2p<=-12416)a2p=-12288;else if(a2p>=12160)a2p=12288;else a2p=s16(a2p+0x80);}
   }
   s.a[1]=s16(a2p);s.a[0]=s16(s.a[0]-(s.a[0]>>8));
   if(dqsez!=0)s.a[0]=s16(s.a[0]+(pks1==0?192:-192));
   int a1ul=15360-a2p;if(s.a[0]<-a1ul)s.a[0]=s16(-a1ul);else if(s.a[0]>a1ul)s.a[0]=s16(a1ul);
   for(int i=0;i<6;i++){s.b[i]=s16(s.b[i]-(s.b[i]>>(codeSize==5?9:8)));if((dq&0x7fff)!=0)s.b[i]=s16(s.b[i]+(((dq^s.dq[i])>=0)?128:-128));}
  }
  for(int i=5;i>0;i--)s.dq[i]=s.dq[i-1];
  if(mag==0)s.dq[0]=s16(dq>=0?0x20:0xfc20);else{int exp=quan(mag),v=(exp<<6)+((mag<<6)>>exp);s.dq[0]=s16(dq>=0?v:v-0x400);}
  s.sr[1]=s.sr[0];
  if(sr==0)s.sr[0]=0x20;else if(sr>0){int exp=quan(sr);s.sr[0]=s16((exp<<6)+((sr<<6)>>exp));}
  else if(sr>-32768){int mm=-sr,exp=quan(mm);s.sr[0]=s16((exp<<6)+((mm<<6)>>exp)-0x400);}else s.sr[0]=s16(0xfc20);
  s.pk[1]=s.pk[0];s.pk[0]=s16(pk0);s.td=tr==1?0:(a2p<-11776?1:0);
  s.dms=s16(s.dms+((fi-s.dms)>>5));s.dml=s16(s.dml+(((fi<<2)-s.dml)>>7));
  if(tr==1)s.ap=256;else if(y<1536||s.td==1||Math.abs((s.dms<<2)-s.dml)>=(s.dml>>3))s.ap=s16(s.ap+((0x200-s.ap)>>4));else s.ap=s16(s.ap+((-s.ap)>>4));
 }
 private static short decodeCode(int i,State s){
  i&=3;int sezi=s16(predictorZero(s)),sez=s16(sezi>>1),sei=s16(sezi+predictorPole(s)),se=s16(sei>>1),y=s16(stepSize(s));
  int dq=s16(reconstruct(i&2,DQL[i],y)),sr=s16(dq<0?se-(dq&0x3fff):se+dq),dqsez=s16(sr-se+sez);
  update(2,y,WI[i],FI[i],dq,sr,dqsez,s);return (short)s16(sr<<2);
 }
 static short[] decodeLsb(byte[] data,int off,int len){
  if(off<0||len<0||off+len>data.length)throw new IllegalArgumentException("Bad ADPCM range");
  short[] out=new short[len*4];State s=new State();int p=0;
  for(int n=0;n<len;n++){int b=data[off+n]&255;out[p++]=decodeCode(b&3,s);out[p++]=decodeCode((b>>2)&3,s);out[p++]=decodeCode((b>>4)&3,s);out[p++]=decodeCode((b>>6)&3,s);}return out;
 }
}
