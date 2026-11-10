#!/usr/bin/env python3
"""Create offline high-resolution, EXIF-free LIKAS owner-preview species gallery.

This is a private research-development preview, not permission to redistribute
photographs. The original 333 Google Drive photos are never changed.
The aggregate digest pins *every original byte*, in filename order: a changed,
missing, extra or incorrectly downloaded image blocks the APK build.
"""
import hashlib
import json
import os
import re
from pathlib import Path

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
MANIFEST = ASSETS / "offline_drive_gallery_manifest_v1.json"
SOURCES = Path(os.environ.get("LIKAS_DRIVE_SOURCE_DIR",
                str(ROOT / ".taxonomy-cache/likas-drive-originals")))
EXPECTED_SOURCE_SET_SHA256 = "c558f26caa678386270f183e9c868cf5bb80f09d3525b24601d0268d0cdc7ce7"
Image.MAX_IMAGE_PIXELS = 45_000_000


def load_entries():
    document = json.loads(MANIFEST.read_text(encoding="utf-8"))
    entries = sorted(document["items"], key=lambda value: value["fileName"])
    if len(entries) != 333 or document.get("count") != 333:
        raise SystemExit("The official owner collection must contain 333 photographs")
    seen_ids = set()
    seen_files = set()
    seen_outputs = set()
    for index, entry in enumerate(entries, 1):
        name = entry["fileName"]
        asset = entry["offlineAsset"]
        drive_id = entry["driveFileId"]
        if not re.fullmatch(r"[A-Z][a-z]+_[A-Za-z0-9_]+\.(jpg|png)", name):
            raise SystemExit("Unexpected source photo name: " + name)
        if asset != f"offline_photos/drive_{index:04d}.jpg":
            raise SystemExit("Offline gallery index mismatch: " + name)
        if not re.fullmatch(r"[A-Za-z0-9_-]{20,80}", drive_id):
            raise SystemExit("Invalid Google Drive source id")
        if name in seen_files or drive_id in seen_ids or asset in seen_outputs:
            raise SystemExit("Duplicate Drive photo entry: " + name)
        if not 1024 < entry["sourceSizeBytes"] < 35_000_000:
            raise SystemExit("Unexpected source photo size: " + name)
        seen_files.add(name)
        seen_ids.add(drive_id)
        seen_outputs.add(asset)
    return entries


def ensure_download(entry):
    name = entry["fileName"]
    target = SOURCES / name
    size = entry["sourceSizeBytes"]
    if target.is_file():
        if target.stat().st_size != size:
            raise SystemExit("Cached source size mismatch; refusing to overwrite: " + name)
        return target
    if os.environ.get("LIKAS_DRIVE_SOURCE_DIR"):
        raise SystemExit("Missing local source photo: " + str(target))
    # The build retrieves all owner-designated photos; the APK itself never
    # needs network or the Android Drive picker.
    try:
        import gdown
    except ImportError as ex:
        raise SystemExit("Missing gdown for owner-photo build") from ex
    SOURCES.mkdir(parents=True, exist_ok=True)
    partial = target.with_name(target.name + ".part")
    # Google Drive's unauthenticated link gateway may intermittently decline
    # an otherwise authorized file. Retry with bounded pauses; never weaken
    # the pinned-size and complete 333-file SHA-256 validations below.
    import time
    last_error = "Google Drive returned no file"
    for attempt, pause_seconds in enumerate((0, 7, 15, 30, 60, 90), 1):
        if pause_seconds:
            print(f"Retrying {name} after {pause_seconds}s (attempt {attempt}/6)",
                  flush=True)
            time.sleep(pause_seconds)
        partial.unlink(missing_ok=True)
        try:
            downloaded = gdown.download(
                id=entry["driveFileId"], output=str(partial), quiet=True,
                use_cookies=False, resume=False)
        except Exception as ex:
            downloaded = None
            last_error = str(ex)
        if downloaded is None or not partial.is_file():
            last_error = "Drive link gateway declined the requested file"
            continue
        if partial.stat().st_size != size:
            last_error = ("Downloaded byte length differs from approved source: "
                          + str(partial.stat().st_size) + " vs " + str(size))
            continue
        partial.rename(target)
        return target
    raise SystemExit("Drive download failed after 6 attempts for " + name
                     + ": " + last_error
                     + ". No partial gallery APK is permitted.")


def validate_jpeg_bytes(data, asset_name):
    """Reject malformed JPEG payloads and embedded EXIF metadata."""
    if not data.startswith(b"\xff\xd8\xff") or not data.endswith(b"\xff\xd9"):
        raise SystemExit("Invalid packaged JPEG: " + asset_name)
    if b"Exif\x00\x00" in data:
        raise SystemExit("EXIF retained in packaged JPEG: " + asset_name)


def prepare():
    entries = load_entries()
    digest = hashlib.sha256()
    for index, entry in enumerate(entries, 1):
        source = ensure_download(entry)
        original = source.read_bytes()
        if len(original) != entry["sourceSizeBytes"]:
            raise SystemExit("Source size mismatch for " + entry["fileName"])
        digest.update(hashlib.sha256(original).digest())
        if index % 40 == 0:
            print(f"Downloaded and fingerprinted {index}/333 source photos", flush=True)
    actual = digest.hexdigest()
    if actual != EXPECTED_SOURCE_SET_SHA256:
        raise SystemExit("Owner photo source checksum set changed: expected " +
                         EXPECTED_SOURCE_SET_SHA256 + ", got " + actual +
                         ". Do not build from changed Drive photographs.")
    print("All 333 original photographs match cryptographically pinned source set.",
          flush=True)

    total = 0
    for index, entry in enumerate(entries, 1):
        source = SOURCES / entry["fileName"]
        target = ASSETS / entry["offlineAsset"]
        target.parent.mkdir(parents=True, exist_ok=True)
        try:
            with Image.open(source) as image:
                # Several .jpg originals are MPO multi-picture captures.
                # Preserve one primary photographic frame per Drive file.
                if image.format not in ("JPEG", "PNG", "MPO"):
                    raise ValueError("Unexpected image format")
                if image.format == "MPO":
                    image.seek(0)
                # Downsample the JPEG decoder first to avoid decoding the
                # original 20+ megapixel frame at full size on every build.
                image.draft("RGB", (1920, 1920))
                image.thumbnail((1920, 1920), Image.Resampling.BICUBIC)
                image = ImageOps.exif_transpose(image).convert("RGB")
                if min(image.size) < 32:
                    raise ValueError("Image dimensions are too small")
                image.save(target, format="JPEG", quality=85,
                           progressive=True, exif=b"")
        except Exception as ex:
            raise SystemExit("Failed to process " + entry["fileName"] +
                             ": " + str(ex)) from ex
        data = target.read_bytes()
        validate_jpeg_bytes(data, entry["offlineAsset"])
        total += len(data)
        if index % 40 == 0:
            print(f"Prepared {index}/333 JPEGs, {total//1_000_000} MB", flush=True)
    print(f"High-resolution gallery ready: 333 images, {total:,} packaged bytes")


if __name__ == "__main__":
    prepare()
