package com.nttdocomo.ui;
import com.wakka.dirge.core.Host;
import com.wakka.dirge.core.CameraAssist;
/** Same v0.1.3 contract, with the optional camera assist at the game-thread poll. */
public abstract class Canvas extends Frame {
 private final Graphics graphics=new Graphics();
 public Canvas(){} public Graphics getGraphics(){return graphics;}
 public int getKeypadState(){return CameraAssist.filter(Host.readKeys());}
 public void processEvent(int type,int key){} public abstract void paint(Graphics g);
 public void repaint(){graphics.lock();paint(graphics);graphics.unlock(true);}
}
