#!/usr/bin/env python3
"""Restore exact RC110-verified offline JPEGs from a pinned signed APK.

This reuses byte-identical approved RC110 personal-preview derivatives. No Google
Drive download, photo relabeling, new identification or redistribution claim.
The prior RC110 CI separately verified all 333 original source images.
"""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
SOURCE=Path(sys.argv[1] if len(sys.argv)>1 else "/tmp/apo-rc110-signed.apk")
EXPECTED_APK_SHA="4062242f3e43379d55b195f3694a0b630841c74a606432e02e7a69b21ee855dc"
EXPECTED_GALLERY_DIGEST="b4252f23b0f943b083ffacea96099f5391e71ceb3823c48ecbec86e9ffb07fac"
EXPECTED_GALLERY_BYTES=127507271

def validate_images():
    manifest=json.loads((ASSETS/"offline_drive_gallery_manifest_v1.json").read_text())
    entries=sorted(manifest["items"],key=lambda e:e["fileName"])
    if len(entries)!=333 or manifest.get("count")!=333:
        raise SystemExit("Manifest is not the complete 333-photo verified edition")
    digest=hashlib.sha256();total=0
    for i,entry in enumerate(entries,1):
        asset=entry["offlineAsset"]
        if asset!=f"offline_photos/drive_{i:04d}.jpg":
            raise SystemExit("Gallery asset index no longer matches frozen manifest")
        p=ASSETS/asset
        data=p.read_bytes()
        if not (data.startswith(b"\xff\xd8\xff") and data.endswith(b"\xff\xd9")):
            raise SystemExit("Invalid JPEG: "+asset)
        if b"Exif\x00\x00" in data:
            raise SystemExit("EXIF metadata in: "+asset)
        digest.update(hashlib.sha256(data).digest())
        total+=len(data)
    if digest.hexdigest()!=EXPECTED_GALLERY_DIGEST or total!=EXPECTED_GALLERY_BYTES:
        raise SystemExit("RC110-verified offline gallery differs; refusing RC111 build")
    print(f"Retained 333 byte-exact RC110 offline JPEGs ({total:,} bytes), pinned digest OK")

def restore():
    if not SOURCE.is_file():
        raise SystemExit("Verified signed RC110 APK unavailable: "+str(SOURCE))
    if hashlib.sha256(SOURCE.read_bytes()).hexdigest()!=EXPECTED_APK_SHA:
        raise SystemExit("Signed RC110 baseline fingerprint changed")
    with zipfile.ZipFile(SOURCE) as z:
        bad=z.testzip()
        if bad:
            raise SystemExit("Corrupted source APK member: "+bad)
        manifest=json.loads((ASSETS/"offline_drive_gallery_manifest_v1.json").read_text())
        for record in manifest["items"]:
            name=record["offlineAsset"]
            output=ASSETS/name
            output.parent.mkdir(parents=True,exist_ok=True)
            output.write_bytes(z.read("assets/"+name))
        approved=json.loads((ROOT/"docs/RC7_PINNED_COMMONS_PHOTO_SHA256.json").read_text())["items"]
        for record in approved:
            name=record["offlineAsset"]
            contents=z.read("assets/"+name)
            if hashlib.sha256(contents).hexdigest()!=record["sha256"]:
                raise SystemExit("Approved Commons source image mismatch: "+name)
            output=ASSETS/name
            output.parent.mkdir(parents=True,exist_ok=True)
            output.write_bytes(contents)
    validate_images()
    print("Approved Commons images restored unchanged from signed RC110")

if __name__=="__main__":
    if len(sys.argv)>1 and sys.argv[1]=="--verify":
        validate_images()
    else:
        restore()
