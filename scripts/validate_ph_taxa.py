#!/usr/bin/env python3
"""Validate a source-attributed, permissioned Philippine species checklist.

Converts a verified CSV to the exact format accepted by LIKAS's offline catalog.
This tool deliberately does not guess country occurrence from world taxonomy,
copy a copyrighted PDF, or label unreviewed names as locally confirmed.
"""
import argparse
import csv
import hashlib
import io
from pathlib import Path

FIELDS = (
    "taxon_key", "taxon_group", "common_name", "scientific_name", "family",
    "source", "source_version", "region_codes", "review_status",
)
GROUPS = {"Birds", "Amphibians", "Reptiles", "Mammals"}
STATUS = {"reviewed", "unreviewed"}

def validate(raw: bytes, group: str, source: str, version: str,
             allow_regions: bool = False):
    if len(raw) > 25_000_000:
        raise ValueError("Input exceeds Android 25 MB checklist limit")
    rows = csv.DictReader(io.StringIO(raw.decode("utf-8-sig"), newline=""))
    if not rows.fieldnames or not set(FIELDS).issubset(set(rows.fieldnames)):
        raise ValueError("Source must have all nine LIKAS columns: " + ", ".join(FIELDS))
    if group not in GROUPS:
        raise ValueError("Unknown taxon group")
    if not source.strip() or not version.strip():
        raise ValueError("Pinned source and version are required")
    species, keys, names = [], set(), set()
    for line, row in enumerate(rows, 2):
        if any(value is None for value in row.values()):
            raise ValueError(f"Malformed CSV at row {line}")
        taxon = {key: (row.get(key) or "").strip() for key in FIELDS}
        if not taxon["taxon_key"] or not taxon["scientific_name"]:
            raise ValueError(f"Missing taxon identifier or scientific name at row {line}")
        if taxon["taxon_group"] != group:
            raise ValueError(f"Wrong group on row {line}: {taxon['taxon_group']}")
        if taxon["source"] != source or taxon["source_version"] != version:
            raise ValueError(f"Missing or mismatched citation on row {line}")
        if taxon["review_status"] not in STATUS:
            raise ValueError(f"Invalid review status on row {line}")
        if taxon["region_codes"] and not allow_regions:
            raise ValueError(f"Unverified province/island codes on row {line}; remove them")
        key = taxon["taxon_key"].lower()
        if key in keys:
            raise ValueError(f"Duplicate taxon key on row {line}")
        keys.add(key)
        pair = (taxon["taxon_group"], taxon["scientific_name"].casefold())
        if pair in names:
            raise ValueError(f"Duplicate scientific name on row {line}")
        names.add(pair)
        species.append(taxon)
        if len(species) > 45000:
            raise ValueError("Checklist exceeds Android 45,000-row limit")
    if not species:
        raise ValueError("Empty checklist: no taxa to import")
    return sorted(species, key=lambda taxon: (
        taxon["scientific_name"].casefold(), taxon["taxon_key"],
    ))

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--group", required=True, choices=sorted(GROUPS))
    parser.add_argument("--source", required=True)
    parser.add_argument("--version", required=True)
    parser.add_argument("--input-sha256", required=True,
                        help="Pinned SHA-256 of the vetted source CSV")
    parser.add_argument("--documented-regions", action="store_true",
                        help="Permit manually vetted island/province region_codes")
    args = parser.parse_args()
    raw = args.input.read_bytes()
    digest = hashlib.sha256(raw).hexdigest()
    if digest.lower() != args.input_sha256.lower():
        raise ValueError("Source checksum mismatch: dataset not imported")
    rows = validate(raw, args.group, args.source, args.version,
                    allow_regions=args.documented_regions)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", newline="", encoding="utf-8") as out:
        writer = csv.DictWriter(out, fieldnames=FIELDS, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    print(f"Validated {len(rows)} {args.group} taxa, source {args.source}, "
          f"version {args.version}; SHA-256 {digest}")
    print(f"Import {args.output} through LIKAS > Data & Tools > Offline checklist")

if __name__ == "__main__":
    main()
