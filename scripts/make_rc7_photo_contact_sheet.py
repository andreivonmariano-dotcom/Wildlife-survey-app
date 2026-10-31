#!/usr/bin/env python3
"""Make a temporary, credited photo-review contact sheet. Never approve a photo."""
import json,urllib.request,hashlib,io,time
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageOps
data=json.loads(Path("rc7_inat_licensed_candidates.json").read_text())
target=Path("rc7_photo_review")
target.mkdir(exist_ok=True)
index=[]
thumbs=[]
for group in data["results"]:
    if not group["candidates"]:continue
    name=group["scientificName"]
    picked=None
    for candidate in group["candidates"]:
        try:
            req=urllib.request.Request(candidate["mediumUrl"],headers={"User-Agent":"LIKAS-Private-Research-Media-Review/1.0"})
            with urllib.request.urlopen(req,timeout=22) as stream:
                b=stream.read(900001)
            if not 1000<len(b)<900001 or not b.startswith(b"\xff\xd8\xff") or not b.endswith(b"\xff\xd9"):continue
            im=Image.open(io.BytesIO(b))
            im.load()
            if im.width<160 or im.height<160:continue
            path="inat_"+str(candidate["photoId"])+".jpg"
            (target/path).write_bytes(b)
            picked=dict(candidate)
            picked["sha256"]=hashlib.sha256(b).hexdigest()
            picked["fileName"]=path
            index.append(picked)
            frame=Image.new("RGB",(370,310),"white")
            cover=ImageOps.contain(im,(350,235))
            frame.paste(cover,((370-cover.width)//2,0))
            draw=ImageDraw.Draw(frame)
            draw.text((6,240),name[:48],fill="black")
            draw.text((6,256),"Photo #"+str(candidate["photoId"])+"  "+candidate["observer"][:27],fill="black")
            draw.text((6,271),candidate["photoLicense"]+"   "+candidate["photoAttribution"][:30],fill="black")
            thumbs.append(frame)
            print("REVIEW",name,candidate["photoId"],candidate["photoLicense"],flush=True)
            break
        except Exception as e:
            print("PHOTO FAILED",name,type(e).__name__,str(e)[:110],flush=True)
    if not picked:print("NO PREVIEW",name,flush=True)
    time.sleep(0.25)
if not index:raise RuntimeError("No reviewable licensed candidates")
for start in range(0,len(thumbs),12):
    chunk=thumbs[start:start+12]
    sheet=Image.new("RGB",(370*3,310*4),"#eeeeee")
    for i,im in enumerate(chunk):sheet.paste(im,((i%3)*370,(i//3)*310))
    sheet.save(target/("contact_%02d.jpg"%(start//12+1)),quality=88)
(target/"candidate_index.json").write_text(json.dumps({
    "status":"Temporary source-license screened only. Visual and attribution review mandatory before installing.",
    "speciesCount":len(index),"items":index
},indent=2))
print("TOTAL PHOTO REVIEW",len(index))
