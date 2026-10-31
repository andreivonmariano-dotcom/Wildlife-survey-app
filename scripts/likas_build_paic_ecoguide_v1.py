#!/usr/bin/env python3
"""Build coordinate-free LIKAS PAIC ecological guide v1 from private scientific CSVs.

This is a reproducible *local* reference compiler. It never fetches data and
never uploads private observations. Literature input is optional, but habitat
claims require a specific attributed source record, not a model-invented claim.
"""
from __future__ import annotations
import argparse
import csv
import hashlib
import json
import re
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import urlparse

POLICY_VERSION = "PAIC-ECOGUIDE-1.0"
SUMMARY_FIELDS = (
    "taxon_id", "group", "scientific_name", "prepolygon_candidate_count",
    "inside_major_islands", "distinct_public_coordinate_pairs_inside",
    "dsm_numeric_count", "dsm_nonnegative_count", "dsm_min_m", "dsm_p10_m",
    "dsm_median_m", "dsm_p90_m", "dsm_max_m", "worldcover2020_class_count",
    "worldcover2020_classes_observed", "worldcover2020_tree_cover_count",
    "worldcover2020_temporal_mismatch_count", "source_qa_status",
    "species_inference_status",
)
LITERATURE_FIELDS = (
    "taxon_id", "habitat_description", "habitat_citation",
    "habitat_reference_url", "published_site_elevations_m",
    "published_elevation_citation", "published_elevation_reference_url",
    "reported_locality_scope", "source_taxon_name",
)
WRITE_FIELDS = (
    "taxon_id", "group", "scientific_name", "habitat_guide",
    "elevation_guide", "mapped_land_cover_2020", "evidence_basis",
    "published_habitat_citation", "published_habitat_url",
    "published_elevation_citation", "published_elevation_url",
    "records_n", "distinct_locations_n", "map_class_records_n",
    "source_note", "evidence_policy",
)
BLOCKED = "HELD_59_ROW_RECONCILIATION"
ALLOWED_QA = {BLOCKED, "NO_RECONCILIATION_DELTA"}
EXPECTED_TAXA = 725

def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as src:
        for chunk in iter(lambda: src.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def read_csv(path: Path, fields: tuple[str, ...]) -> list[dict]:
    with path.open("r", encoding="utf-8-sig", newline="") as src:
        reader = csv.DictReader(src)
        missing = set(fields) - set(reader.fieldnames or [])
        if missing:
            raise ValueError(f"{path.name} missing required columns: {sorted(missing)}")
        return list(reader)

def number(row: dict, key: str) -> int:
    value = (row.get(key) or "").strip()
    if not re.fullmatch(r"\d+", value):
        raise ValueError(f"Invalid nonnegative integer {key} for {row.get('taxon_id')}")
    return int(value)

def m(value: str) -> str:
    """Original number already reported by sample export; no new interpolation."""
    if not value:
        return ""
    try:
        x = float(value)
    except ValueError as err:
        raise ValueError("Non-numeric mapped elevation") from err
    if not (-500 <= x <= 10000):
        raise ValueError("Mapped elevation is outside QA bounds")
    return f"{x:g}"

def reference_valid(url: str) -> bool:
    p = urlparse(url.strip())
    return p.scheme == "https" and bool(p.netloc)

def assert_literature(row: dict) -> None:
    habitat = (row.get("habitat_description") or "").strip()
    elev = (row.get("published_site_elevations_m") or "").strip()
    if habitat and (not row.get("habitat_citation") or not reference_valid(row.get("habitat_reference_url", ""))):
        raise ValueError(f"Unattributed habitat description: {row.get('taxon_id')}")
    if elev and (not row.get("published_elevation_citation") or
                 not reference_valid(row.get("published_elevation_reference_url", ""))):
        raise ValueError(f"Unattributed published elevation: {row.get('taxon_id')}")
    if len(habitat) > 1200:
        raise ValueError("Narrative too long: require original-attributed concise paraphrase")
    if elev and not re.fullmatch(r"[0-9., –\-]+", elev):
        raise ValueError("Published numeric elevation must contain only supplied numbers and separators")

def compile_guide(summary: list[dict], literature: list[dict] | None = None,
                  expected_taxa: int | None = EXPECTED_TAXA) -> tuple[list[dict], dict]:
    if expected_taxa is not None and len(summary) != expected_taxa:
        raise ValueError(f"Expected {expected_taxa} species rows; received {len(summary)}")
    sids = [r["taxon_id"].strip() for r in summary]
    if any(not i for i in sids) or len(set(sids)) != len(sids):
        raise ValueError("Missing or duplicate taxon ID")
    litby = {}
    for r in literature or []:
        taxon = r["taxon_id"].strip()
        if not taxon or taxon in litby:
            raise ValueError("Literature rows require unique taxon_id")
        assert_literature(r)
        litby[taxon] = r
    unrecognized = set(litby) - set(sids)
    if unrecognized:
        raise ValueError(f"Literature references unknown taxa: {sorted(unrecognized)[:5]}")

    out = []
    held = 0
    species_with_map = 0
    species_with_literature = 0
    for src in summary:
        taxon = src["taxon_id"].strip()
        qa = src["source_qa_status"].strip()
        if qa not in ALLOWED_QA:
            raise ValueError(f"Unknown source QA code for {taxon}: {qa}")
        is_held = qa == BLOCKED
        held += int(is_held)
        lit = litby.get(taxon, {})
        habitat = (lit.get("habitat_description") or "").strip()
        published_elev = (lit.get("published_site_elevations_m") or "").strip()
        n = number(src, "dsm_numeric_count")
        unique_n = number(src, "distinct_public_coordinate_pairs_inside")
        land_n = number(src, "worldcover2020_class_count")
        inside_n = number(src, "inside_major_islands")
        if not (unique_n <= inside_n and n <= inside_n and land_n <= inside_n):
            raise ValueError(f"Inconsistent occurrence counts for {taxon}")
        # Protect against presenting pre-existing source discrepancies as regional summaries.
        suspicious_negative_surface = n > 0 and bool(src.get("dsm_min_m")) and float(src["dsm_min_m"]) < 0
        use_raster = n > 0 and not is_held and not suspicious_negative_surface
        use_map = land_n > 0 and not is_held
        species_with_map += int(use_map)
        species_with_literature += int(bool(habitat or published_elev))

        parts = []
        if habitat:
            parts.append("Published habitat: " + habitat)
        if use_map:
            classes = (src.get("worldcover2020_classes_observed") or "").strip()
            if not classes:
                raise ValueError(f"Missing mapped classes for {taxon}")
            parts.append("WorldCover 2020 mapped classes at observation locations: " + classes)
        habitat_text = " | ".join(parts) if parts else "Not documented in the current sources"

        elevation_parts = []
        if use_raster:
            minval, maxval = m(src["dsm_min_m"]), m(src["dsm_max_m"])
            if not minval or not maxval:
                raise ValueError(f"Incomplete DSM range for {taxon}")
            if unique_n == 1:
                elevation_parts.append(
                    f"Raster surface height at one recorded location: {minval} m"
                )
            else:
                elevation_parts.append(
                    f"Raster surface heights at recorded locations: {minval}–{maxval} m "
                    f"({unique_n} distinct coordinate pairs, {n} records)"
                )
        if published_elev:
            elevation_parts.append("Published collection/locality elevations: " +
                                   published_elev + " m")
        elevation_text = " | ".join(elevation_parts) if elevation_parts else "Not documented in the current sources"

        basis = []
        if habitat or published_elev:
            basis.append("published accounts")
        if use_raster:
            basis.append("screened public observations + COP30-labeled surface-height raster")
        if use_map:
            basis.append("ESA WorldCover 2020 v100")
        note = []
        if is_held:
            note.append("Source count discrepancy retained. Raster-based regional summary withheld until row reconciliation.")
        if suspicious_negative_surface:
            note.append("Negative raster height detected; automatic elevation guide withheld pending source-value check.")
        if use_raster:
            note.append("Surface model vertical datum not verified; heights do not establish species elevation tolerance.")
        if use_map:
            note.append("2020 land cover is a dated map class and may differ from field habitat or observation-year conditions.")
        if not basis:
            note.append("No coordinates or attributed habitat account available in the supplied inputs.")

        out.append({
            "taxon_id": taxon, "group": src["group"].strip(),
            "scientific_name": src["scientific_name"].strip(),
            "habitat_guide": habitat_text, "elevation_guide": elevation_text,
            "mapped_land_cover_2020": src["worldcover2020_classes_observed"] if use_map else "",
            "evidence_basis": " + ".join(basis) if basis else "none supplied",
            "published_habitat_citation": lit.get("habitat_citation", ""),
            "published_habitat_url": lit.get("habitat_reference_url", ""),
            "published_elevation_citation": lit.get("published_elevation_citation", ""),
            "published_elevation_url": lit.get("published_elevation_reference_url", ""),
            "records_n": str(n if use_raster else 0),
            "distinct_locations_n": str(unique_n if use_raster else 0),
            "map_class_records_n": str(land_n if use_map else 0),
            "source_note": " ".join(note),
            "evidence_policy": POLICY_VERSION,
        })
    manifest = {
        "policy_version": POLICY_VERSION,
        "taxa_total": len(out), "taxa_with_mapped_cover_unheld": species_with_map,
        "taxa_with_attributed_literature": species_with_literature,
        "source_reconciliation_hold_taxa": held,
        "retains_coordinates": False, "requires_specialist_approval": False,
        "display_generic_provisional_label": False,
        "interpretation": "evidence-informed associations, not ecological selection or numeric likelihood",
    }
    return out, manifest

def build(summary_file: Path, literature_file: Path | None, output_dir: Path):
    records = read_csv(summary_file, SUMMARY_FIELDS)
    literature = read_csv(literature_file, LITERATURE_FIELDS) if literature_file else []
    rows, manifest = compile_guide(records, literature)
    manifest["input_sha256"] = {
        "species_environmental_summary_csv": sha256(summary_file),
        "literature_accounts_csv": sha256(literature_file) if literature_file else None,
    }
    manifest["created_at_utc"] = datetime.now(timezone.utc).isoformat()
    output_dir.mkdir(parents=True, exist_ok=True)
    csv_path = output_dir / "LIKAS_PAIC_Ecological_Guide_v1_NO_COORDS_PRIVATE.csv"
    with csv_path.open("w", encoding="utf-8", newline="") as dest:
        writer = csv.DictWriter(dest, fieldnames=WRITE_FIELDS)
        writer.writeheader()
        writer.writerows(rows)
    manifest["output_csv_sha256"] = sha256(csv_path)
    (output_dir / "LIKAS_PAIC_Ecological_Guide_v1_manifest_PRIVATE.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"Produced {len(rows)} coordinate-free guide accounts under {POLICY_VERSION}.")
    print("No original files altered. Sources and output checksums written to private manifest.")

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--species-csv", required=True, type=Path)
    parser.add_argument("--literature-csv", type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()
    build(args.species_csv, args.literature_csv, args.output_dir)

if __name__ == "__main__":
    main()
