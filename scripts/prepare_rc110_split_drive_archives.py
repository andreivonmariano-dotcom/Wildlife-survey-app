#!/usr/bin/env python3
"""Restore the owner's 333 original photos from three split Google Drive ZIPs.

These are standard contiguous byte-split ZIP files (.zip.001, .zip.002, ...).
Read each segment in order; do not treat each segment as a standalone archive.
No archive bytes are included in git. All originals are checked again against
the RC110 cryptographic source-set lock by prepare_drive_gallery.py.
"""
import json
import os
import shutil
import time
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "app/src/main/assets/offline_drive_gallery_manifest_v1.json"
SOURCES = ROOT / ".taxonomy-cache/likas-drive-originals"
STAGING = ROOT / ".taxonomy-cache/likas-drive-split-archives"

# Exact IDs and sizes observed from the user-supplied 2026-11-10 archive folder.
# Order within each group is the original byte order, NOT Drive listing order.
ARCHIVES = {
    "Birds": {
        "stem": "Birds-20261110T005917Z-1-001.zip",
        "count": 163,
        "parts": [
            ("1N6XX-OXM_gP3IHVZdgNiLJ8nkdRicr-5", 104857600),
            ("1KQoMn-hS3AFyF9zlSMaGuJDXdwtnCP4z", 104857600),
            ("1DYzgdfl_zxdt1eU1YyU3EzOf3d0M1RZu", 104857600),
            ("1Kab0e-ABmbQG8_DIpYTsnGfTmsqwm6wr", 104857600),
            ("1OiIB7t0K-PTnHJuRF98pemN-j2jc0xcO", 104857600),
            ("1f2OYZvckw6O3mGhjltd1tTO14exx2MZ4", 104857600),
            ("1NXvRf3ACjSuLQLtfvHeBbRNU_GlqSFl6", 104857600),
            ("1W9szJ2-6-8ZKh1QbY4x3rwCd8OvJL6VG", 104857600),
            ("1SAvyTlq31No8efH5HbWx1L6Lb4YLTG8c", 104857600),
            ("15mVZwVFEuVKPFHP7DKuNu-xtzuge1ll0", 15803034),
        ],
    },
    "Reptiles and Amphibians": {
        "stem": "Reptiles and Amphibians-20261110T005914Z-1-001.zip",
        "count": 149,
        "parts": [
            ("1VXbgOvJMh5oB2asRTQdXDcc3jKAnAznQ", 104857600),
            ("18XFNCy8OaknTYVvF0BWocpi86--w5I2H", 104857600),
            ("1-PtmJSKnNN1FHyOgvLmJdv-G_9G4twWL", 104857600),
            ("1hfKLLrBSzXKfQYpLgkgBKljd0wg6so9i", 104857600),
            ("1V4NAuSc_bAGDfizDHUCWip_1yg3aVtsr", 104857600),
            ("1LtsdcLihJ7GgHth7O4WjJqDkPf6eQuK1", 104857600),
            ("1a5E_O4-QN7akv96e1MJnAm04P5GVAtAv", 104857600),
            ("1M2hTijSkHdPoB3qIDxQLzWFe4RxgRRHl", 104857600),
            ("1re1QByvJwdqpG4xfBrYmikr8eKbL9Fiz", 104311471),
        ],
    },
    "Mammals": {
        "stem": "Mammals-20261110T005914Z-1-001.zip",
        "count": 21,
        "parts": [
            ("1CpPI_1Z6MUhxt20fKOxOdU9nIfmM0Kl2", 104857600),
            ("1OKw4aAF3tNbR1xta7pocm05P9LsU7cyP", 104857600),
            ("1XU1g-L2mijEsChzmDyR6VAf3KzhWqc0n", 17918264),
        ],
    },
}


def download_part(drive_id: str, target: Path, required_size: int) -> None:
    if target.exists():
        if target.stat().st_size == required_size:
            return
        raise SystemExit("Wrong cached split archive part size: " + str(target))
    try:
        import gdown
    except ImportError as ex:
        raise SystemExit("gdown==5.2.0 required to fetch Drive split archives") from ex
    target.parent.mkdir(parents=True, exist_ok=True)
    partial = target.with_name(target.name + ".download")
    for attempt, pause in enumerate((0, 12, 30, 60, 90), 1):
        if pause:
            print(f"Retry part {target.name} after {pause}s ({attempt}/5)", flush=True)
            time.sleep(pause)
        partial.unlink(missing_ok=True)
        try:
            downloaded = gdown.download(id=drive_id, output=str(partial),
                                        quiet=True, use_cookies=False, resume=False)
        except Exception as ex:
            print(f"Drive archive-part request error: {type(ex).__name__}", flush=True)
            downloaded = None
        if downloaded and partial.is_file() and partial.stat().st_size == required_size:
            partial.rename(target)
            print(f"Received {target.name}: {required_size:,} bytes", flush=True)
            return
        print(f"Archive part not received as pinned: {target.name}", flush=True)
    raise SystemExit("Unable to download exact split archive part " + target.name
                     + " from the user-supplied Drive folder")


def restore_group(group: str, spec: dict, expected: dict) -> None:
    if len(expected) != spec["count"]:
        raise SystemExit("Unexpected photo count for " + group)
    if all((SOURCES / filename).is_file()
           and (SOURCES / filename).stat().st_size == size
           for filename, size in expected.items()):
        print(f"Already staged {group}: {len(expected)} original files", flush=True)
        return

    paths = []
    for index, (drive_id, size) in enumerate(spec["parts"], 1):
        path = STAGING / f'{spec["stem"]}.{index:03d}'
        download_part(drive_id, path, size)
        paths.append(path)

    assembled = STAGING / spec["stem"]
    with assembled.open("wb") as output:
        for part in paths:
            with part.open("rb") as source:
                shutil.copyfileobj(source, output, length=1024 * 1024)
    if assembled.stat().st_size != sum(size for _, size in spec["parts"]):
        raise SystemExit("ZIP byte concatenation mismatch: " + group)

    try:
        with zipfile.ZipFile(assembled, "r") as archive:
            files = [info for info in archive.infolist() if not info.is_dir()]
            if len(files) != len(expected):
                raise SystemExit(f"Wrong {group} ZIP photo count: {len(files)}")
            actual_names = [info.filename for info in files]
            if len(set(actual_names)) != len(actual_names):
                raise SystemExit("Duplicate source archive members in " + group)
            expected_names = {f"{group}/{name}" for name in expected}
            if set(actual_names) != expected_names:
                missing = sorted(expected_names - set(actual_names))
                extra = sorted(set(actual_names) - expected_names)
                raise SystemExit(f"{group} ZIP member names differ: "
                                 f"missing={missing[:4]}, extra={extra[:4]}")
            SOURCES.mkdir(parents=True, exist_ok=True)
            for info in files:
                filename = info.filename[len(group) + 1:]
                if info.file_size != expected[filename]:
                    raise SystemExit("Wrong ZIP member file size: " + info.filename)
                destination = SOURCES / filename
                if destination.exists():
                    if destination.stat().st_size != info.file_size:
                        raise SystemExit("Existing source photo differs: " + filename)
                    continue
                temporary = destination.with_name(destination.name + ".partial")
                temporary.unlink(missing_ok=True)
                with archive.open(info) as source, temporary.open("wb") as output:
                    shutil.copyfileobj(source, output, length=1024 * 1024)
                if temporary.stat().st_size != info.file_size:
                    raise SystemExit("Wrong extracted length for " + filename)
                temporary.rename(destination)
            print(f"Restored {group}: {len(files)} exact-size photos; ZIP CRC passed",
                  flush=True)
    finally:
        assembled.unlink(missing_ok=True)
        for part in paths:
            part.unlink(missing_ok=True)


def main() -> None:
    entries = json.loads(MANIFEST.read_text(encoding="utf-8"))["items"]
    if len(entries) != 333 or sum(spec["count"] for spec in ARCHIVES.values()) != 333:
        raise SystemExit("Only the 333-photo RC110 owner collection may be staged")
    groups = {group: {} for group in ARCHIVES}
    for entry in entries:
        group = entry["group"]
        if group not in groups:
            raise SystemExit("Unknown group for photo " + entry["fileName"])
        name = entry["fileName"]
        if "/" in name or "\\" in name or name in groups[group]:
            raise SystemExit("Unsafe or duplicate member: " + name)
        groups[group][name] = entry["sourceSizeBytes"]
    for group, spec in ARCHIVES.items():
        restore_group(group, spec, groups[group])
    print("All 333 archive photos staged for SHA-256 and JPEG conversion.", flush=True)


if __name__ == "__main__":
    main()
