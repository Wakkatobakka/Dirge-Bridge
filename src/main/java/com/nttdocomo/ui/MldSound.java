package com.nttdocomo.ui;
import java.io.IOException;import com.wakka.dirge.core.Diag;
final class MldSound implements MediaSound {
 private final byte[] bytes;private MldDecoder.Decoded decoded;private IOException failure;private static boolean announced;
 MldSound(byte[] b){bytes=b;if(!announced){announced=true;Diag.event("AUDIO BACKEND","MLD adat/adpm 2-bit G.726 decoder enabled (8 kHz mono Lost Episode profile)");}}
 synchronized MldDecoder.Decoded decoded()throws IOException{if(decoded!=null)return decoded;if(failure!=null)throw failure;try{decoded=MldDecoder.decode(bytes);Diag.event("AUDIO DECODE","Decoded "+bytes.length+" byte MLD to "+decoded.pcm.length+" PCM samples @ "+decoded.sampleRate+" Hz");return decoded;}catch(IOException e){failure=e;throw e;}}
 public void use(){}public void unuse(){}public void dispose(){}
}
