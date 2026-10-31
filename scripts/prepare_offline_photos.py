#!/usr/bin/env python3
"""Build the licensed thumbnail cache for the offline LIKAS APK.

The source and license are pinned in app/src/main/assets/offline_photo_manifest_v1.json.
Only exact Commons file titles in that reviewed manifest may be requested.
No original location EXIF is copied into LIKAS reference records.
"""
from pathlib import Path
import json
import hashlib
import urllib.parse
import urllib.request
import time

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
MANIFEST=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
LOCK=json.loads((ROOT/"docs/RC7_PINNED_COMMONS_PHOTO_SHA256.json").read_text())
PINNED={item["id"]:item for item in LOCK["items"]}
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

def sha256(payload):
    return hashlib.sha256(payload).hexdigest()

def valid(path,record):
    if not path.is_file():
        return False
    raw=path.read_bytes()
    return (1000<len(raw)<=900000 and raw.startswith(b"\xff\xd8\xff")
            and raw.endswith(b"\xff\xd9")
            and sha256(raw)==record["sha256"])

def prepare():
    if LOCK.get("requiredCount")!=9 or len(PINNED)!=9:
        raise SystemExit("Approved Commons manifest must contain exactly nine hashes")
    originals={p["id"]:p for p in MANIFEST["items"]}
    if any(id not in originals for id in PINNED):
        raise SystemExit("Approved Commons item missing from source manifest")
    completed=0
    errors=[]
    for id,record in PINNED.items():
        original=originals[id]
        if original["offlineAsset"]!=record["offlineAsset"] or original["fileName"]!=record["commonsFile"]:
            raise SystemExit("Approved Commons source mismatch for "+id)
        filename=original["offlineAsset"].split("/")[-1]
        if not filename.startswith(("species_","habitat_")) or not filename.endswith(".jpg"):
            raise SystemExit("Unsafe pinned Commons destination")
        target=DEST/filename
        if valid(target,record):
            completed+=1
            print("Pinned Commons verified from cache",filename,flush=True)
            continue
        if target.exists():target.unlink()
        for attempt in range(3):
            try:
                source=thumbnail(original["fileName"])
                image=fetch(source)
                if len(image)<=1000 or not image.startswith(b"\xff\xd8\xff") or not image.endswith(b"\xff\xd9"):
                    raise ValueError("Incomplete JPEG")
                if sha256(image)!=record["sha256"]:
                    raise ValueError("Source thumbnail bytes differ from pinned SHA-256")
                target.write_bytes(image)
                assert valid(target,record)
                completed+=1
                print("Pinned Commons downloaded",filename,len(image),flush=True)
                break
            except Exception as ex:
                print("Retry Commons",filename,attempt+1,ex,flush=True)
                if attempt==2:errors.append(filename+": "+str(ex))
                else:time.sleep((attempt+1)*1.2)
        time.sleep(.25)
    # Never ship an unpredictable set after a partial photo download.
    if errors or completed!=LOCK["requiredCount"]:
        raise SystemExit("Pinned Commons media incomplete: "+str(errors))
    print("Offline Commons verified SHA-256:",completed,"of",LOCK["requiredCount"],flush=True)

if __name__=="__main__":prepare()
