#!/usr/bin/env python3
"""Restore the EXACT RC110 EXIF-free gallery derivatives for an RC111 UI-only build.

Source is the individually signed RC110 APK, not a new Google Drive download.
A full pinned APK SHA-256 and the entire ordered 333-image digest are required.
This does NOT re-verify photo-level identities, rights, or the original Drive bytes.
The separately recorded RC110 source-set digest remains part of its provenance.
"""
import hashlib
import os
import zipfile
from pathlib import Path

from prepare_drive_gallery import load_entries, validate_jpeg_bytes

ROOT=Path(__file__).resolve().parents[1]
BASELINE=Path(os.environ.get("APO_RC110_BASELINE_APK","/tmp/apo-rc110-signed.apk"))
ASSETS=ROOT/"app/src/main/assets"
APK_SHA="4062242f3e43379d55b195f3694a0b630841c74a606432e02e7a69b21ee855dc"
DERIVATIVE_SET_SHA="b4252f23b0f943b083ffacea96099f5391e71ceb3823c48ecbec86e9ffb07fac"
TOTAL_BYTES=127507271

def verify():
    if not BASELINE.is_file():
        raise SystemExit("Pinned RC110 signed APK missing: "+str(BASELINE))
    apk_bytes=BASELINE.read_bytes()
    actual=hashlib.sha256(apk_bytes).hexdigest()
    if actual!=APK_SHA:
        raise SystemExit("Refusing different RC110 baseline APK: "+actual)
    ordered=load_entries()
    total=0
    digest=hashlib.sha256()
    with zipfile.ZipFile(BASELINE) as pack:
        for record in ordered:
            name=record["offlineAsset"]
            data=pack.read("assets/"+name)
            validate_jpeg_bytes(data,name)
            digest.update(hashlib.sha256(data).digest())
            total+=len(data)
            destination=ASSETS/name
            if destination.is_file():
                if hashlib.sha256(destination.read_bytes()).digest()!=hashlib.sha256(data).digest():
                    raise SystemExit("Existing packaged owner photo differs from pinned RC110: "+name)
            else:
                destination.parent.mkdir(parents=True,exist_ok=True)
                destination.write_bytes(data)
    if digest.hexdigest()!=DERIVATIVE_SET_SHA or total!=TOTAL_BYTES:
        raise SystemExit("Pinned RC110 gallery derivatives fail 333-image aggregate checksum")
    print("Verified 333 unchanged EXIF-free photo derivatives from signed RC110 baseline.")
    print("Verified APK SHA-256, derivative-set SHA-256 and 127,507,271 image bytes.")
    print("Original source SHA c558f26c... remains RC110 provenance; not re-fetched in RC111.")
if __name__=="__main__":
    verify()
