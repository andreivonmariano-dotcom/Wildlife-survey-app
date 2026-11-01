#!/usr/bin/env python3
"""Review second/third owner-authored images without changing any media rights."""
import hashlib,io,json,time,urllib.request
from pathlib import Path
from PIL import Image,ImageOps,ImageDraw
from collect_owner_dreidrei_photos import collect,ROOT
owned=json.loads((ROOT/"docs/RC7_DREIDREI_OWNER_APPROVED_LOCK_V1.json").read_text())
prior_ids={x["photoId"] for x in owned["items"]}
counts={}
for x in owned["items"]:counts[x["taxon"]]=counts.get(x["taxon"],0)+1
rows=collect(46)
out=Path("rc7_owner_extra_review");out.mkdir(exist_ok=True)
chosen=[];images=[]
for p in rows:
 if p["photoId"] in prior_ids:continue
 name=p["taxon"]
 if counts.get(name,0)>=3:continue
 if len(chosen)>=25:break
 try:
  request=urllib.request.Request(p["mediumUrl"],headers={"User-Agent":"LIKAS-private-owner-photo-review/1.0"})
  with urllib.request.urlopen(request,timeout=25) as response:raw=response.read(900001)
  if not 1000<len(raw)<=900000 or not raw.startswith(b"\xff\xd8\xff") or not raw.endswith(b"\xff\xd9"):continue
  image=Image.open(io.BytesIO(raw));image.load()
  if image.width<160 or image.height<160:continue
  filename="inat_"+str(p["photoId"])+".jpg"
  (out/filename).write_bytes(raw)
  c=dict(p,sha256=hashlib.sha256(raw).hexdigest(),fileName=filename)
  chosen.append(c);counts[name]=counts.get(name,0)+1
  card=Image.new("RGB",(360,310),"white")
  thumb=ImageOps.contain(image,(354,230))
  card.paste(thumb,((360-thumb.width)//2,0))
  d=ImageDraw.Draw(card)
  d.text((6,236),name[:40],fill="black")
  d.text((6,251),"photo "+str(p["photoId"]),fill="black")
  d.text((6,266),p["photoAttribution"][:44],fill="black")
  d.text((6,281),"all rights reserved | owner grant",fill="black")
  images.append(card)
  print("EXTRA REVIEW",name,p["photoId"],flush=True)
 except Exception as e:print("SKIP",p["photoId"],str(e)[:90],flush=True)
 time.sleep(.22)
for off in range(0,len(images),12):
 sheet=Image.new("RGB",(1080,1240),"#eeeeee")
 for i,card in enumerate(images[off:off+12]):sheet.paste(card,((i%3)*360,(i//3)*310))
 sheet.save(out/("additional_%02d.jpg"%(off//12+1)),quality=85)
(out/"index.json").write_text(json.dumps({"status":"visual review pending","items":chosen},indent=2))
print("ADDITIONAL_PREVIEW_COUNT",len(chosen),flush=True)
