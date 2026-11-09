#!/usr/bin/env python3
"""Prepare hash-locked, EXIF-free, offline JPEG species photographs for RC109.

Source photographs are user-supplied, not independently identified or licensed
for public release. They are included only in this owner-review development
preview. This build step never contacts Google Drive or a network.
"""
import hashlib
import io
import json
from pathlib import Path
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
MANIFEST = json.loads((ASSETS / "offline_photo_manifest_v1.json").read_text())
OWNED = [x for x in MANIFEST["items"]
         if x.get("rightsBasis") == "owner_requested_in_app_preview"]
if len(OWNED) != 9:
    raise SystemExit("Expected exactly nine approved preview file mappings")
names = set()
Image.MAX_IMAGE_PIXELS = 40_000_000
for record in OWNED:
    if record["taxon"] in names:
        raise SystemExit("Duplicate owner preview taxon " + record["taxon"])
    names.add(record["taxon"])
    source = ROOT / record["nativeSource"]
    dest = ASSETS / record["offlineAsset"]
    if not source.is_file() or not source.resolve().is_relative_to(
            (ROOT / "app/photo_sources/owner_native").resolve()):
        raise SystemExit("Missing or unsafe source photograph: " + str(source))
    raw = source.read_bytes()
    if not 1024 < len(raw) < 12_000_000 or not raw.startswith(b"\xff\xd8"):
        raise SystemExit("Unsupported native photo source: " + source.name)
    digest = hashlib.sha256(raw).hexdigest()
    if digest != record["sourceSha256"]:
        raise SystemExit("Native photo source checksum mismatch: " + source.name)
    with Image.open(io.BytesIO(raw)) as original:
        if original.format != "JPEG":
            raise SystemExit("Unexpected photo format: " + source.name)
        image = ImageOps.exif_transpose(original)
        image = image.convert("RGB")
        image.thumbnail((900, 900), Image.Resampling.LANCZOS)
        dest.parent.mkdir(parents=True, exist_ok=True)
        image.save(dest, format="JPEG", quality=80,
                   optimize=True, progressive=False, exif=b"")
    result = dest.read_bytes()
    if not (1000 < len(result) < 450_000 and result[:3] == b"\xff\xd8\xff"
            and result.endswith(b"\xff\xd9") and b"Exif\x00\x00" not in result):
        raise SystemExit("Unsafe or oversized packaged thumbnail: " + dest.name)
    print(f"{record['taxon']}: embedded preview {len(result)} bytes")
print("Native owner photo preparation passed:", len(OWNED), "photos")
