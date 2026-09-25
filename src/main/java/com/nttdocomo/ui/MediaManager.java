package com.nttdocomo.ui;import java.io.*;import com.wakka.dirge.core.*;
public final class MediaManager {
 public static MediaImage getImage(InputStream in)throws IOException{final Image image=Host.platform.decode(Host.read(in,16777216));return new MediaImage(){public Image getImage(){return image;}public void use(){}public void unuse(){}public void dispose(){}};}
 public static MediaSound getSound(InputStream in)throws IOException{return new MldSound(Host.read(in,16777216));}
}
