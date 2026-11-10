#!/usr/bin/env python3
"""Reuse exact 333 EXIF-free photos from the previously verified signed RC110 APK.

Avoids recurring anonymous Google Drive split-archive fetches. This path uses the
byte-identical derivatives previously generated from all 333 SHA-verified originals.
The original master-source digest remains separately documented and unchanged.
"""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
APK_SHA="4062242f3e43379d55b195f3694a0b630841c74a606432e02e7a69b21ee855dc"
DERIVED_SHA="b4252f23b0f943b083ffacea96099f5391e71ceb3823c48ecbec86e9ffb07fac"
TOTAL_BYTES=127507271

def main():
    if len(sys.argv)!=2:
        raise SystemExit("Usage: restore_rc110_verified_gallery.py <signed-RC110.apk>")
    apk=Path(sys.argv[1])
    if hashlib.sha256(apk.read_bytes()).hexdigest()!=APK_SHA:
        raise SystemExit("Pinned signed RC110 APK checksum mismatch")
    entries=sorted(json.loads((ASSETS/"offline_drive_gallery_manifest_v1.json").read_text())["items"],
                   key=lambda v:v["fileName"])
    if len(entries)!=333:
        raise SystemExit("The owner gallery manifest must contain exactly 333 photos")
    with zipfile.ZipFile(apk) as archive:
        if archive.read("assets/offline_drive_gallery_manifest_v1.json")!=(
                ASSETS/"offline_drive_gallery_manifest_v1.json").read_bytes():
            raise SystemExit("Pinned APK does not contain the current owner-gallery manifest")
        digest=hashlib.sha256()
        total=0
        for index, item in enumerate(entries,1):
            name=item["offlineAsset"]
            if name!=f"offline_photos/drive_{index:04d}.jpg":
                raise SystemExit("Gallery source-order mismatch")
            data=archive.read("assets/"+name)
            if not data.startswith(b"\xff\xd8\xff") or not data.endswith(b"\xff\xd9"):
                raise SystemExit("Bundled photo is not valid JPEG framing: "+name)
            if b"Exif\x00\x00" in data:
                raise SystemExit("Bundled photo retains EXIF: "+name)
            digest.update(hashlib.sha256(data).digest())
            total+=len(data)
            dest=ASSETS/name
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(data)
    if digest.hexdigest()!=DERIVED_SHA or total!=TOTAL_BYTES:
        raise SystemExit("Owner-gallery derived source set no longer matches signed RC110")
    print(f"Restored 333 pinned EXIF-free photos ({total:,} bytes); source lineage retained.")

if __name__=="__main__":
    main()
