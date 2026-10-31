#!/usr/bin/env python3
"""Private-source-safe raster archive inventory for LIKAS PAIC ecological QA.

Reads ZIP and .tar.gz inventories without extracting all data. Optionally stages
one raster member at a time to inspect its geospatial header with rasterio.
Never infers ecological habitat, dataset year, or DEM vertical datum from names.
"""
from __future__ import annotations

import argparse
import csv
import datetime as dt
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import tarfile
import tempfile
import zipfile

RASTER_SUFFIXES = {".tif", ".tiff", ".img", ".vrt", ".nc"}
MANIFEST_SUFFIXES = {".xml", ".json", ".txt", ".csv", ".prj", ".aux", ".md"}

def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(8 * 1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()

def eligible_member(name: str) -> bool:
    clean = name.replace("\\", "/")
    parts = PurePosixPath(clean).parts
    return bool(parts) and not clean.startswith("/") and ".." not in parts and not any(
        part in ("", ".") for part in parts
    )

def list_archive_members(path: Path) -> list[dict]:
    """List members and byte sizes without unpacking archival GIS data."""
    rows = []
    if zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as source:
            for member in source.infolist():
                if member.is_dir():
                    continue
                suffix = PurePosixPath(member.filename).suffix.lower()
                rows.append({"member": member.filename, "suffix": suffix,
                             "kind": "raster" if suffix in RASTER_SUFFIXES else
                             "sidecar" if suffix in MANIFEST_SUFFIXES else "other",
                             "size_uncompressed": member.file_size,
                             "size_compressed": member.compress_size,
                             "safe_member": eligible_member(member.filename)})
    elif tarfile.is_tarfile(path):
        with tarfile.open(path, mode="r:gz" if str(path).lower().endswith(".gz") else "r:") as source:
            for member in source:
                if not member.isfile():
                    continue
                suffix = PurePosixPath(member.name).suffix.lower()
                rows.append({"member": member.name, "suffix": suffix,
                             "kind": "raster" if suffix in RASTER_SUFFIXES else
                             "sidecar" if suffix in MANIFEST_SUFFIXES else "other",
                             "size_uncompressed": member.size,
                             "size_compressed": None,
                             "safe_member": eligible_member(member.name)})
    else:
        raise ValueError(f"Not a recognized ZIP or TAR archive: {path}")
    return rows

def copy_member(path: Path, member_name: str, output, max_bytes: int) -> None:
    """Copy a safe archive member without trusting its stored path."""
    if not eligible_member(member_name):
        raise ValueError(f"Unsafe member path: {member_name!r}")
    if zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as archive:
            info = archive.getinfo(member_name)
            if info.file_size > max_bytes:
                raise ValueError("Member exceeds configured uncompressed size cap")
            with archive.open(info) as source:
                shutil.copyfileobj(source, output, length=1024 * 1024)
    else:
        with tarfile.open(path, mode="r:gz" if str(path).lower().endswith(".gz") else "r:") as archive:
            member = archive.getmember(member_name)
            if not member.isfile() or member.size > max_bytes:
                raise ValueError("Unsafe or oversized TAR member")
            source = archive.extractfile(member)
            if source is None:
                raise ValueError("Cannot read TAR member")
            with source:
                shutil.copyfileobj(source, output, length=1024 * 1024)

def raster_header(path: Path, member_name: str, cap_bytes: int) -> dict:
    try:
        import rasterio
        from rasterio.warp import transform_bounds
    except ImportError as error:
        raise RuntimeError("Install rasterio for --inspect-raster") from error
    with tempfile.TemporaryDirectory(prefix="likas_raster_") as directory:
        suffix = PurePosixPath(member_name).suffix.lower()
        temporary = Path(directory) / ("member" + suffix)
        with temporary.open("wb") as output:
            copy_member(path, member_name, output, cap_bytes)
        with rasterio.open(temporary) as raster:
            info = {"driver": raster.driver, "crs": raster.crs.to_string() if raster.crs else None,
                    "width": raster.width, "height": raster.height, "bands": raster.count,
                    "dtypes": list(raster.dtypes), "nodata": raster.nodata,
                    "pixel_size": [abs(raster.transform.a), abs(raster.transform.e)],
                    "bounds_native": list(raster.bounds), "tags": dict(raster.tags())}
            info["bounds_epsg4326"] = (
                list(transform_bounds(raster.crs, "EPSG:4326", *raster.bounds, densify_pts=21))
                if raster.crs else None
            )
            return info

def inspect(archives: list[Path], headers: bool = False,
            cap_bytes: int = 4 * 1024 ** 3, checksum: bool = True) -> dict:
    report = {"tool_version": "0.4.0", "analytical_status": "archive_inventory_only",
              "generated_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
              "archive_count": len(archives), "archives": [], "raster_members": []}
    for archive in archives:
        archive = archive.expanduser().resolve()
        if not archive.is_file():
            raise FileNotFoundError(archive)
        members = list_archive_members(archive)
        record = {"archive_name": archive.name, "size_bytes": archive.stat().st_size,
                  "sha256": file_sha256(archive) if checksum else None,
                  "member_count": len(members),
                  "raster_member_count": sum(x["kind"] == "raster" for x in members),
                  "members": members}
        for member in members:
            if member["kind"] != "raster" or not member["safe_member"]:
                continue
            result = {"archive_name": archive.name, **member, "header_status": "not_inspected"}
            if headers:
                try:
                    if member["size_uncompressed"] > cap_bytes:
                        raise ValueError("Raster exceeds member size cap")
                    result["header"] = raster_header(archive, member["member"], cap_bytes)
                    result["header_status"] = "read"
                except Exception as error:
                    result["header_status"] = "error"
                    result["header_error"] = f"{type(error).__name__}: {error}"
            report["raster_members"].append(result)
        report["archives"].append(record)
    return report

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--archive", action="append", type=Path, required=True,
                        help="ZIP/TAR archive, repeat for multiple source files")
    parser.add_argument("--output", type=Path, required=True, help="Private inventory JSON")
    parser.add_argument("--inspect-raster", action="store_true",
                        help="Extract one raster at a time and inspect GDAL headers")
    parser.add_argument("--max-member-gib", type=float, default=4.0,
                        help="Maximum uncompressed individual raster to stage")
    parser.add_argument("--skip-sha256", action="store_true")
    args = parser.parse_args()
    if args.max_member_gib <= 0:
        parser.error("--max-member-gib must be positive")
    result = inspect(args.archive, headers=args.inspect_raster,
                     cap_bytes=int(args.max_member_gib * 1024 ** 3),
                     checksum=not args.skip_sha256)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Inspected {len(result['archives'])} archive(s); "
          f"found {len(result['raster_members'])} raster-like member(s).")
    print(f"JSON inventory: {args.output}")
    if args.inspect_raster:
        success = sum(m["header_status"] == "read" for m in result["raster_members"])
        print(f"Geospatial headers read: {success}/{len(result['raster_members'])}")
    print("No elevation pixels, land-cover classes, or species values were sampled.")

if __name__ == "__main__":
    main()
