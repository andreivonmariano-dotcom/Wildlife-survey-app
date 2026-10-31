#!/usr/bin/env python3
"""LIKAS owner-private Copernicus GLO-30 screening and elevation extraction.

No network calls. Input is the owner's three-project PRIVACY-SAFE Research Grade
CSV and existing working core/associated island polygon GeoPackage. Never read
private_latitude/private_longitude. Never infer habitat preference from points.

Requires Python 3.10+, rasterio, geopandas, shapely and pyproj.
Use --inspect to list ZIP/TAR tile files and GPKG layer names before sampling.
The full data workflow intentionally requires an explicit approved polygon
layer for core and associated Camiguin Sur.
"""
from __future__ import annotations

import argparse
from collections import Counter, defaultdict
import csv
import hashlib
import io
import json
import math
from pathlib import Path
import shutil
import tarfile
import tempfile
import zipfile

FIELD_NAMES = {
    "id": ("observation_id", "id", "inat_observation_id", "observation_uuid"),
    "lat": ("public_latitude_wgs84", "public_latitude", "latitude", "lat", "decimalLatitude"),
    "lon": ("public_longitude_wgs84", "public_longitude", "longitude", "lon", "decimalLongitude"),
    "accuracy": ("public_uncertainty_m", "public_positional_accuracy_m", "positional_accuracy",
                 "coordinate_uncertainty_m", "coordinate_uncertainty_in_meters",
                 "coordinateUncertaintyInMeters"),
    "name": ("original_scientific_name", "likas_accepted_name", "likas_name", "scientific_name",
             "inat_scientific_name", "taxon_name"),
    "taxon": ("likas_taxon_id", "taxon_id", "taxon_key"),
    "date": ("observed_on", "observed_date", "date"),
    "quality": ("source_quality_grade", "quality_grade", "quality", "grade"),
    "geo": ("geo_quality", "geoprivacy", "observed_on_geoprivacy", "coordinates_obscured_source", "coordinates_obscured"),
}
TIF_SUFFIXES = (".tif", ".tiff")


def choose_field(fields, kind):
    matched = [x for x in FIELD_NAMES[kind] if x in fields]
    return matched[0] if matched else None


def is_private(row):
    # Only the public latitude/longitude fields are eligible. Private-coordinate
    # fields are deliberately not read, even if the public point is blank.
    geo = " ".join(str(row.get(k, "")).lower()
                   for k in ("geoprivacy", "taxon_geoprivacy", "geo_quality",
                             "observed_on_geoprivacy", "coordinates_obscured",
                             "coordinates_obscured_source"))
    return ("obscured" in geo or "private" in geo or
            geo.strip() in {"true", "1"})


def digest_sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(8 * 1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def source_tiles(archives, temporary, byte_limit, inventory_only=False):
    """Stream only genuine raster members, preventing traversal and zip bombs.

    Never call tar.extractall. Raster file names are rewritten into a
    temporary private directory, independent of archive paths.
    """
    used = 0
    tiles = []
    archive_index = []
    index = 0

    def save_member(file_object, original_name, size, archive_name):
        nonlocal used, index
        if size < 1 or used + size > byte_limit:
            raise ValueError("Raster extraction budget exceeded: " + original_name)
        used += size
        index += 1
        newpath = temporary / ("tile_%05d.tif" % index)
        if not inventory_only:
            with newpath.open("wb") as dest:
                shutil.copyfileobj(file_object, dest, 8 * 1024 * 1024)
        tiles.append((newpath, archive_name, original_name, size))

    for archive in archives:
        ap = Path(archive)
        if not ap.is_file():
            raise FileNotFoundError(ap)
        archive_index.append({"filename": ap.name, "bytes": ap.stat().st_size,
                              "sha256": digest_sha256(ap)})
        with tarfile.open(ap, mode="r|gz") as tar:
            for member in tar:
                if not member.isfile():
                    continue
                name = member.name
                lower = name.lower()
                if lower.endswith(TIF_SUFFIXES):
                    fh = tar.extractfile(member)
                    if fh is None:
                        continue
                    with fh:
                        save_member(fh, name, member.size, ap.name)
                elif lower.endswith(".zip"):
                    # Some downloaded Copernicus tiles are ZIP files inside a TAR.
                    # Spool to disk, never hold potentially huge ZIPs in memory.
                    member_fh = tar.extractfile(member)
                    if member_fh is None:
                        continue
                    zip_path = temporary / ("nested_%05d.zip" % (index + 1))
                    with member_fh, zip_path.open("wb") as out:
                        shutil.copyfileobj(member_fh, out, 8 * 1024 * 1024)
                    with zipfile.ZipFile(zip_path) as z:
                        for entry in z.infolist():
                            if entry.is_dir() or not entry.filename.lower().endswith(TIF_SUFFIXES):
                                continue
                            with z.open(entry) as raster_data:
                                save_member(raster_data, name + "!" + entry.filename,
                                            entry.file_size, ap.name)
                    zip_path.unlink()
    if not tiles:
        raise ValueError("No GeoTIFF DEM members discovered in supplied archives")
    return tiles, archive_index


def select_points(csv_path, max_uncertainty):
    with open(csv_path, newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        cols = reader.fieldnames or []
        names = {kind: choose_field(cols, kind) for kind in FIELD_NAMES}
        required = ("id", "lat", "lon", "accuracy", "name")
        missing = [kind for kind in required if names[kind] is None]
        if missing:
            raise ValueError("Missing required fields " + ", ".join(missing) +
                             ". CSV columns: " + repr(cols))
        counts = Counter()
        selected = []
        ids = set()
        for row in reader:
            counts["total"] += 1
            if names["quality"] and row[names["quality"]].strip().lower() != "research":
                counts["non_research"] += 1
                continue
            if is_private(row):
                counts["obscured_redacted"] += 1
                continue
            try:
                lat = float(row[names["lat"]])
                lon = float(row[names["lon"]])
                accuracy = float(row[names["accuracy"]])
            except (TypeError, ValueError):
                counts["missing_coordinate_or_accuracy"] += 1
                continue
            if not (math.isfinite(lat) and math.isfinite(lon) and
                    -90 <= lat <= 90 and -180 <= lon <= 180 and
                    math.isfinite(accuracy) and 0 <= accuracy <= max_uncertainty):
                counts["failed_geospatial_accuracy"] += 1
                continue
            oid = row[names["id"]].strip()
            if not oid or oid in ids:
                counts["duplicate_or_empty_id"] += 1
                continue
            scientific_name = row[names["name"]].strip()
            if not scientific_name or len(scientific_name.split()) < 2:
                counts["no_species_binomial"] += 1
                continue
            ids.add(oid)
            selected.append({
                "observation_id": oid, "latitude": lat, "longitude": lon,
                "uncertainty_m": accuracy, "scientific_name": scientific_name,
                "taxon_id": row.get(names["taxon"], "") if names["taxon"] else "",
                "observed_date": row.get(names["date"], "") if names["date"] else ""
            })
            counts["accuracy_pass"] += 1
    return selected, counts


def validate_against_islands(points, path, core_layer, associated_layer):
    import geopandas as gpd
    from pyproj import Geod
    from shapely.geometry import Point
    from shapely.ops import unary_union

    polygon_groups = []
    for layer, category in ((core_layer, "CORE_MAJOR_ISLANDS"),
                            (associated_layer, "ASSOCIATED_CAMIGUIN_SUR")):
        gdf = gpd.read_file(path, layer=layer)
        if gdf.crs is None:
            raise ValueError("Boundary CRS missing: " + layer)
        gdf = gdf.to_crs(4326)
        geom = unary_union([g for g in gdf.geometry if g is not None and not g.is_empty])
        if geom.is_empty or not geom.is_valid:
            raise ValueError("Boundary geometry invalid: " + layer)
        polygon_groups.append((category, geom))
    geod = Geod(ellps="WGS84")
    accepted, status = [], Counter()
    for point in points:
        lat, lon, radius = point["latitude"], point["longitude"], point["uncertainty_m"]
        p = Point(lon, lat)
        found = None
        for cat, geom in polygon_groups:
            if not geom.covers(p):
                continue
            # Approximate full uncertainty circle using 24 geodesic perimeter
            # points. For shoreline-adjacent cases, retain a caution even if all
            # samples pass; a simplified coastline is not survey-accurate.
            fully_inside = True
            for bearing in range(0, 360, 15):
                x, y, _ = geod.fwd(lon, lat, bearing, radius)
                if not geom.covers(Point(x, y)):
                    fully_inside = False
                    break
            if fully_inside:
                found = cat
            else:
                status["shoreline_uncertain"] += 1
                found = None
            break
        if found:
            accepted.append({**point, "island_scope": found})
            status[found] += 1
        elif not any(g.covers(p) for _, g in polygon_groups):
            status["outside_selected_major_island_polygons"] += 1
    return accepted, status


def sample_elevations(points, tiles):
    import rasterio
    from rasterio.warp import transform

    results = {}
    tile_info = []
    # One raster at a time; memory stays bounded even for many 1-degree tiles.
    for tile, source_archive, original_member, nbytes in tiles:
        with rasterio.open(tile) as ds:
            if ds.crs is None or ds.count < 1:
                raise ValueError("DEM tile lacks coordinate system/band: " + original_member)
            record = {"tile": original_member, "archive": source_archive,
                      "crs": str(ds.crs), "bounds": tuple(ds.bounds),
                      "width": ds.width, "height": ds.height,
                      "nodata": ds.nodata, "pixel_resolution": tuple(ds.res)}
            tile_info.append(record)
            candidates = []
            coords = []
            if ds.crs.to_epsg() == 4326:
                for p in points:
                    x, y = p["longitude"], p["latitude"]
                    if ds.bounds.left <= x < ds.bounds.right and ds.bounds.bottom < y <= ds.bounds.top:
                        candidates.append(p)
                        coords.append((x, y))
            else:
                lon_arr = [p["longitude"] for p in points]
                lat_arr = [p["latitude"] for p in points]
                xs, ys = transform("EPSG:4326", ds.crs, lon_arr, lat_arr)
                for p, x, y in zip(points, xs, ys):
                    if ds.bounds.left <= x < ds.bounds.right and ds.bounds.bottom < y <= ds.bounds.top:
                        candidates.append(p)
                        coords.append((x, y))
            for p, (x, y), sample in zip(candidates, coords, ds.sample(coords, masked=True)):
                val = sample[0]
                if hasattr(val, "mask") and bool(val.mask):
                    continue
                elevation = float(val)
                if not math.isfinite(elevation) or not (-150 < elevation < 5500):
                    continue
                if ds.nodata is not None and elevation == ds.nodata:
                    continue
                oid = p["observation_id"]
                rr, cc = ds.index(x, y)
                resolution_score = abs(ds.res[0] * ds.res[1])
                hit = {**p, "surface_elevation_egm2008_m": round(elevation, 2),
                       "source_tile": original_member, "source_archive": source_archive,
                       "raster_row": rr, "raster_col": cc,
                       "raster_cell": original_member + ":" + str(rr) + ":" + str(cc),
                       "_resolution": resolution_score}
                if oid not in results or resolution_score < results[oid]["_resolution"]:
                    results[oid] = hit
    return results, tile_info


def quantile(sorted_values, frac):
    if not sorted_values:
        return ""
    index = (len(sorted_values)-1)*frac
    low = math.floor(index)
    high = math.ceil(index)
    return round(sorted_values[low]*(high-index)+sorted_values[high]*(index-low), 2) if low != high else round(sorted_values[low], 2)


def write_results(output_dir, points, hits, source_info, archives, failures):
    output_dir.mkdir(parents=True, exist_ok=True)
    point_file = output_dir / "LIKAS_PRIVATE_COP30_VALIDATED_OBSERVATION_ELEVATIONS.csv"
    point_headers = ["observation_id", "scientific_name", "taxon_id", "observed_date",
                     "latitude", "longitude", "uncertainty_m", "island_scope",
                     "surface_elevation_egm2008_m", "source_archive", "source_tile",
                     "raster_cell", "interpretation"]
    with point_file.open("w", newline="", encoding="utf-8") as f:
        wr = csv.DictWriter(f, fieldnames=point_headers)
        wr.writeheader()
        for p in sorted(hits.values(), key=lambda a:a["observation_id"]):
            row = {**p, "interpretation": "COP30 DSM at observation; NOT preference"}
            wr.writerow({key: row.get(key, "") for key in point_headers})
    summary_file = output_dir / "LIKAS_PRIVATE_COP30_SPECIES_OBSERVED_ELEVATION_SUMMARY.csv"
    grouped = defaultdict(list)
    for p in hits.values():
        grouped[p["scientific_name"]].append(p)
    species_headers = ["scientific_name", "n_observations", "n_distinct_dem_cells",
                       "observed_dsm_min_m", "observed_dsm_p05_m",
                       "observed_dsm_median_m", "observed_dsm_p95_m",
                       "observed_dsm_max_m", "elevation_reference", "scope",
                       "elevational_preference"]
    with summary_file.open("w", newline="", encoding="utf-8") as f:
        wr = csv.DictWriter(f, fieldnames=species_headers)
        wr.writeheader()
        for name, group in sorted(grouped.items()):
            cells = {x["raster_cell"]: x["surface_elevation_egm2008_m"] for x in group}
            vals = sorted(cells.values())
            scope = "|".join(sorted({p["island_scope"] for p in group}))
            wr.writerow({
                "scientific_name": name, "n_observations": len(group),
                "n_distinct_dem_cells": len(vals), "observed_dsm_min_m": min(vals),
                "observed_dsm_p05_m": quantile(vals, .05),
                "observed_dsm_median_m": quantile(vals, .5),
                "observed_dsm_p95_m": quantile(vals, .95),
                "observed_dsm_max_m": max(vals), "elevation_reference": "Copernicus DEM GLO-30 DSM; EGM2008 (source-defined)",
                "scope": scope, "elevational_preference": "NOT ESTABLISHED"
            })
    report = {"processed_public_accurate_points": len(points),
              "with_dem_sample": len(hits), "without_dem_coverage": len(points)-len(hits),
              "species_with_dsm": len(grouped),
              "screening": dict(failures),
              "archives": archives, "tiles": source_info,
              "notes": [
                  "COP30 is a DSM; canopy and buildings affect surface heights",
                  "Sea-level geoid EGM2008 is orthometric reference, not WGS84 ellipsoid height",
                  "The observed elevation range is sampling-limited, never a species preference",
                  "The coastline is intermediate-resolution modern GSHHS, excludes satellites",
                  "Sampling precision reflects point uncertainty and raster grid",
                  "No ESA WorldCover dataset sampled; habitat selection not estimable",
                  "No obscured/private coordinates or user identifiers were accessed"
              ]}
    (output_dir/"LIKAS_PRIVATE_COP30_ELEVATION_QA.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"points":len(points),"sampled":len(hits),
                      "species":len(grouped), "files":[str(point_file),str(summary_file)]},
                     indent=2))


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--archive", action="append", required=True, help="COP30 .tar.gz; repeat for all parts")
    ap.add_argument("--points", type=Path, help="Privacy-safe Research Grade CSV")
    ap.add_argument("--polygon", type=Path, help="Owner-private PAIC GeoPackage")
    ap.add_argument("--core-layer", help="Exact GPKG layer for eight core major islands")
    ap.add_argument("--associated-layer", help="Exact GPKG layer for Camiguin Sur")
    ap.add_argument("--output", type=Path, default=Path("LIKAS_PRIVATE_COP30_RESULTS"))
    ap.add_argument("--max-uncertainty", type=float, default=100.0)
    ap.add_argument("--max-unpacked-gb", type=float, default=12.0)
    ap.add_argument("--inspect", action="store_true", help="List DEM tile members/GeoPackage layers without sampling")
    args = ap.parse_args()
    if args.max_uncertainty <= 0 or args.max_unpacked_gb <= 0:
        ap.error("max-uncertainty and max-unpacked-gb must be positive")
    with tempfile.TemporaryDirectory(prefix="likas_private_cop30_") as d:
        scratch = Path(d)
        tiles, archives = source_tiles(args.archive, scratch,
                                       int(args.max_unpacked_gb * 1024**3),
                                       inventory_only=args.inspect)
        if args.inspect:
            print(json.dumps({"archive_source_checksums":archives,
                              "dem_members":[{"name":name,"archive":arc,"bytes":size}
                                             for _,arc,name,size in tiles]},
                             indent=2))
            if args.polygon:
                import fiona
                print("Available GeoPackage layers:", fiona.listlayers(str(args.polygon)))
            return
        if not all((args.points,args.polygon,args.core_layer,args.associated_layer)):
            ap.error("sampling requires --points, --polygon, --core-layer and --associated-layer")
        points, screened = select_points(args.points,args.max_uncertainty)
        approved, polygons = validate_against_islands(
            points,args.polygon,args.core_layer,args.associated_layer)
        hits,source_info = sample_elevations(approved,tiles)
        write_results(args.output,approved,hits,source_info,archives,
                      {**screened,**polygons})

if __name__ == "__main__":
    main()
