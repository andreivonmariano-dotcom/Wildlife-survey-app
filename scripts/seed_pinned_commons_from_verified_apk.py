#!/usr/bin/env python3
"""Restore already approved Commons JPEGs from a separately verified signed APK.

This avoids external Commons rate limiting. The source must be exactly the
previously signed RC90 candidate, and every thumbnail must match the existing
independent source-and-license lock. No new images are approved here.
"""
from pathlib import Path
import hashlib
import json
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / "docs/RC7_PINNED_COMMONS_PHOTO_SHA256.json"
MANIFEST = ROOT / "app/src/main/assets/offline_photo_manifest_v1.json"
DEST = ROOT / "app/src/main/assets/offline_photos"
EXPECTED_APK = "8d24c96581618570aace24a50d3e6a7be641a2d082f0cf53b05ced69da9e27c3"

def sha256(raw):
    return hashlib.sha256(raw).hexdigest()

def seed(apk_path):
    apk_path = Path(apk_path)
    if not apk_path.is_file() or sha256(apk_path.read_bytes()) != EXPECTED_APK:
        raise ValueError("Previous signed APK missing or SHA-256 mismatch")
    lock = json.loads(LOCK.read_text(encoding="utf-8"))
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    names = {item["id"]: item for item in manifest["items"]}
    if lock["requiredCount"] != 9 or len(lock["items"]) != 9:
        raise ValueError("Pinned photo lock count changed")
    validated = {}
    with zipfile.ZipFile(apk_path) as archive:
        for entry in lock["items"]:
            image_id = entry["id"]
            if image_id not in names:
                raise ValueError("Missing approved image in manifest: "+image_id)
            meta = names[image_id]
            if (entry["offlineAsset"] != meta["offlineAsset"] or
                    entry["commonsFile"] != meta["fileName"]):
                raise ValueError("Mismatch with approved source metadata: "+image_id)
            asset_name = entry["offlineAsset"]
            if (len(Path(asset_name).parts) != 2 or
                    Path(asset_name).parts[0] != "offline_photos" or
                    not Path(asset_name).name.endswith(".jpg")):
                raise ValueError("Unexpected approved file path")
            raw = archive.read("assets/"+asset_name)
            if (len(raw) <= 1000 or len(raw) > 900000 or
                    raw[:3] != bytes((255,216,255)) or
                    raw[-2:] != bytes((255,217)) or
                    sha256(raw) != entry["sha256"]):
                raise ValueError("Approved JPEG checksum invalid: "+image_id)
            validated[Path(asset_name).name] = raw
    # Write only after every approved item is independently validated.
    DEST.mkdir(parents=True, exist_ok=True)
    for name, raw in validated.items():
        (DEST/name).write_bytes(raw)
    print("Restored exact approved Commons photo bytes:", len(validated))

if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: seed_pinned_commons_from_verified_apk.py <signed-old.apk>")
    seed(sys.argv[1])
