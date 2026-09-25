package com.wakka.dirgebridge;
import android.app.*;import android.content.*;import android.os.Build;import android.view.*;import android.widget.*;import java.io.*;
final class Ui {
 static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
 static TextView text(Context c,String value,int size){TextView t=new TextView(c);t.setText(value);t.setTextSize(size);t.setTextColor(0xffe5e9f1);t.setPadding(dp(c,5),dp(c,5),dp(c,5),dp(c,5));return t;}
 static Button button(Context c,String value){Button b=new Button(c);b.setText(value);b.setAllCaps(false);b.setTextSize(15);b.setMinHeight(dp(c,44));return b;}
 static void insets(View root){root.setBackgroundColor(0xff080a10);root.setOnApplyWindowInsetsListener((v,in)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets i=in.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(i.left,i.top,i.right,i.bottom);}else v.setPadding(in.getSystemWindowInsetLeft(),in.getSystemWindowInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());return in;});root.requestApplyInsets();}
 static String device(){return "Platform: actual Android app (device run)\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nABIs: "+java.util.Arrays.toString(Build.SUPPORTED_ABIS)+"\nApp: Dirge Bridge 0.2.4 / com.wakka.dirgebridge\n";}
 static String read(File f){try(InputStream in=new FileInputStream(f)){return new String(com.wakka.dirge.core.Host.read(in,2*1024*1024),"UTF-8");}catch(Exception e){return "No game runtime log yet: "+e.getMessage()+"\n";}}
 static void error(Activity a,String message){new AlertDialog.Builder(a).setTitle("Dirge Bridge").setMessage(message).setPositiveButton("OK",null).show();}
 static void write(File f,String value)throws IOException{try(OutputStream out=new FileOutputStream(f)){out.write(value.getBytes("UTF-8"));}}
}
