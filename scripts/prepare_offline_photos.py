#!/usr/bin/env python3
"""Build the licensed thumbnail cache for the offline LIKAS APK.

The source and license are pinned in app/src/main/assets/offline_photo_manifest_v1.json.
Only exact Commons file titles in that reviewed manifest may be requested.
No original location EXIF is copied into LIKAS reference records.
"""
from pathlib import Path
import json
import urllib.parse
import urllib.request
import time

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
MANIFEST=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
DEST=ASSETS/"offline_photos"
DEST.mkdir(parents=True,exist_ok=True)
UA="LIKAS/0.27.3 (offline biodiversity reference pack; attribution in app)"

def fetch(url, max_bytes=900000):
    req=urllib.request.Request(url,headers={"User-Agent":UA,"Accept":"image/jpeg,application/json"})
    with urllib.request.urlopen(req,timeout=28) as response:
        data=response.read(max_bytes+1)
        if len(data)>max_bytes:raise ValueError("Media exceeds allowed APK size")
        return data

def thumbnail(file_title):
    api="https://commons.wikimedia.org/w/api.php?"+urllib.parse.urlencode({
        "action":"query","format":"json","prop":"imageinfo",
        "titles":"File:"+file_title,"iiprop":"url","iiurlwidth":"440"})
    data=json.loads(fetch(api,160000))
    pages=data.get("query",{}).get("pages",{})
    for page in pages.values():
        image=page.get("imageinfo",[])
        if image:
            return image[0].get("thumburl") or image[0].get("url")
    raise ValueError("Wikimedia Commons file not found: "+file_title)

def prepare():
    failures=[]
    completed=0
    for item in MANIFEST["items"]:
        title=item["fileName"]
        filename=item["offlineAsset"].split("/")[-1]
        if not filename.startswith(("species_","habitat_")) or not filename.endswith(".jpg"):
            raise ValueError("Unsafe offline output filename")
        path=DEST/filename
        if path.exists() and path.stat().st_size>1000:
            print("Already cached",filename,flush=True);completed+=1;continue
        success=False
        for attempt in range(4):
            try:
                url=thumbnail(title)
                image=fetch(url)
                if not image.startswith(bytes([255,216,255])):
                    raise ValueError("Expected JPEG thumbnail, got "+repr(image[:20]))
                if len(image)<1000:raise ValueError("JPEG thumbnail too small")
                path.write_bytes(image)
                completed+=1
                success=True
                print("Bundled",filename,len(image),flush=True)
                break
            except Exception as ex:
                if attempt>=3:
                    failures.append(filename+": "+str(ex))
                    print("Not bundled",filename,str(ex),flush=True)
                else:
                    print("Retry source file",filename,"attempt",attempt+2,str(ex)[:100],flush=True)
                    time.sleep(3*(attempt+1))
        time.sleep(.65)
    print("Offline photo thumbnails:",completed,"/",len(MANIFEST["items"]),flush=True)
    if failures:print("\\n".join(failures),flush=True)
    if completed<3:raise SystemExit("Fewer than three licensed reference photos available")

if __name__=="__main__":prepare()
