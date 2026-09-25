"""Preserve prior DEX classes while removing only explicitly replaced classes.
No renaming/obfuscation or security evasion. Fixed-width display-version strings
may be updated while recomputing the DEX signature/checksum.
"""
import struct,hashlib,zlib
from pathlib import Path
U=lambda b,o:struct.unpack_from('<I',b,o)[0]
def uleb(b,p):
 v=0;s=0
 while True:
  n=b[p];p+=1;v|=(n&127)<<s
  if n<128:return v,p
  s+=7
  if s>35:raise ValueError('Bad ULEB128')
def strings(b):
 out=[]
 for i in range(U(b,56)):
  p=U(b,U(b,60)+i*4);n,q=uleb(b,p);end=b.index(0,q)
  out.append((n,bytes(b[q:end])))
 return out
def classes(b):
 s=strings(b);to=U(b,68)
 return [s[U(b,to+U(b,U(b,100)+i*32)*4)][1].decode() for i in range(U(b,96))]
def patch(source:bytes,replacements:set,version_pairs=())->bytes:
 b=bytearray(source)
 if b[:8] not in (b'dex\n035\x00',b'dex\n038\x00',b'dex\n039\x00'):raise ValueError('Unexpected DEX version')
 old_strings=strings(b);ids=classes(b);off=U(b,100)
 found=set(ids)&replacements
 if found!=replacements:raise ValueError('Missing replacement class definitions: '+repr(replacements-found))
 kept=[bytes(b[off+i*32:off+(i+1)*32]) for i,n in enumerate(ids) if n not in replacements]
 b[off:off+32*len(kept)]=b''.join(kept);struct.pack_into('<I',b,96,len(kept))
 for i,(n,raw) in enumerate(old_strings):
  updated=raw
  for old,new in version_pairs:
   if len(old)!=len(new):raise ValueError('DEX display replacement must preserve byte length')
   updated=updated.replace(old,new)
  if raw!=updated:
   p=U(b,U(b,60)+4*i);_,q=uleb(b,p);b[q:q+len(raw)]=updated
 m=U(b,52)
 for i in range(U(b,m)):
  p=m+4+i*12
  if struct.unpack_from('<H',b,p)[0]==0x0006:struct.pack_into('<I',b,p+4,len(kept))
 b[12:32]=hashlib.sha1(b[32:]).digest();struct.pack_into('<I',b,8,zlib.adler32(b[12:])&0xffffffff)
 return bytes(b)
if __name__=='__main__':
 import argparse,zipfile
 p=argparse.ArgumentParser();p.add_argument('apk');p.add_argument('out');a=p.parse_args()
 with zipfile.ZipFile(a.apk) as z:s=z.read('classes.dex')
 Path(a.out).write_bytes(patch(s,{'Lcom/wakka/dirgebridge/GamePad;','Lcom/nttdocomo/ui/Canvas;'}))
