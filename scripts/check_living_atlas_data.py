#!/usr/bin/env python3
"""Verify immutable APO Living Atlas source editions without network access.

The DOCX manuscript and XLSX checklist are editable Drive sources, not live
Android dependencies. An optional CSV export from the existing "Master Checklist"
sheet reports proposed taxonomic differences without changing user data.
"""
import argparse
import base64
import csv
import gzip
import hashlib
import io
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
BOOK_HASH = "a87021a615a93f64b56cb3c8974e57deaf69bdd7f0ca7478b70068bad533e885"
BIB_HASH = "cb597385f1fc10bcca5f3f9aa5ddafd64b37d762f481816ca479e26df3a9751e"
GROUPS = {"Birds": 409, "Amphibians": 53, "Reptiles": 154, "Mammals": 99}


def pinned_json(path, expected_sha, max_bytes):
    encoded = "".join((ASSETS / path).read_text(encoding="ascii").split())
    compressed = base64.b64decode(encoded, validate=True)
    with gzip.GzipFile(fileobj=io.BytesIO(compressed)) as source:
        data = source.read(max_bytes + 1)
    if len(data) > max_bytes:
        raise ValueError("Oversized Living Atlas pack: " + path)
    observed = hashlib.sha256(data).hexdigest()
    if observed != expected_sha:
        raise ValueError("Incorrect pinned SHA-256 for " + path)
    return json.loads(data)


def normalize(value):
    return " ".join(str(value or "").split()).casefold()


def approved_habitat_link(pack, edge):
    return (pack.get("reviewStatus") == "approved" and
            edge.get("scientificReview") == "approved" and
            edge.get("habitatMappingReview") == "approved" and
            edge.get("siteOccurrence") is False)


def compare_checklist_csv(csv_text, book_names):
    reader = csv.DictReader(io.StringIO(csv_text))
    if not reader.fieldnames or "Species Name" not in reader.fieldnames:
        raise ValueError("Export the 'Master Checklist' tab with the 'Species Name' column")
    names = [normalize(row["Species Name"]) for row in reader
             if row.get("Species Name") and normalize(row["Species Name"])]
    duplicated = sorted(name for name, n in Counter(names).items() if n != 1)
    if duplicated:
        raise ValueError("Duplicate checklist scientific name: " + ", ".join(duplicated[:5]))
    checklist = set(names)
    book = set(book_names)
    return {
        "checklistCount": len(names),
        "bookOnly": sorted(book - checklist),
        "checklistOnly": sorted(checklist - book),
        "status": "requires_editorial_crosswalk" if checklist != book else "names_align",
    }


def verify(asset_dir=ASSETS, checklist_csv=None):
    global ASSETS
    old_assets = ASSETS
    ASSETS = Path(asset_dir)
    try:
        registry = json.loads((ASSETS / "living_atlas_sources_v1.json").read_text())
        if (registry["schemaVersion"] != 1 or
                registry["indexEdition"] != "living-atlas-data-v1" or
                registry["regionId"] != "greater-mindanao"):
            raise ValueError("Incorrect Living Atlas registry edition")
        version = registry["compiledReference"]
        if (version["bookEdition"] != "1.82" or version["speciesCount"] != 715 or
                version["bibliographyCount"] != 1159):
            raise ValueError("Book 01 registry count/version conflict")
        book = pinned_json(version["bookAsset"], BOOK_HASH, 1_800_000)
        bibliography = pinned_json(version["bibliographyAsset"], BIB_HASH, 500_000)
        records = book["records"]
        if (book["edition"] != "1.82" or book["speciesCount"] != 715 or
                len(records) != 715 or bibliography["edition"] != "1.82" or
                len(bibliography["bibliography"]) != 1159):
            raise ValueError("Bundled Book 01 edition/count mismatch")
        counts = Counter(item["g"] for item in records)
        if counts != Counter(GROUPS):
            raise ValueError("Unexpected Book 01 taxon groups: " + repr(counts))
        book_names = [normalize(record["s"]) for record in records]
        if len(set(book_names)) != len(book_names) or "" in book_names:
            raise ValueError("Duplicated/empty Book 01 taxon names")
        provisional = [item for item in records if item["s"] == "Oreophryne cf. nana"]
        if (len(provisional) != 1 or
                provisional[0].get("t") != "Unranked" or
                provisional[0].get("iucn") is not None):
            raise ValueError("Provisional Oreophryne must retain its source uncertainty")

        profiles = json.loads((ASSETS / registry["habitatProfilesAsset"]).read_text())
        habitats = profiles["profiles"]
        habitat_ids = [p["id"] for p in habitats]
        if len(habitat_ids) != len(set(habitat_ids)) or not habitats:
            raise ValueError("Invalid habitat source identifiers")
        links = json.loads((ASSETS / registry["habitatEvidenceAsset"]).read_text())
        ids = set()
        eligible = []
        for edge in links["edges"]:
            if edge["id"] in ids:
                raise ValueError("Duplicate habitat relationship: " + edge["id"])
            ids.add(edge["id"])
            if edge["habitatId"] not in habitat_ids:
                raise ValueError("Unknown habitat in source edge: " + edge["habitatId"])
            if approved_habitat_link(links, edge) and normalize(edge["scientificName"]) in book_names:
                eligible.append(edge["id"])
        result = {
            "indexEdition":registry["indexEdition"],
            "region":registry["regionId"],
            "bookEdition":"1.82",
            "bookSpecies":len(records),
            "bookGroups":dict(counts),
            "bibliographyEntries":len(bibliography["bibliography"]),
            "habitatProfiles":len(habitats),
            "sourceHabitatEdges":len(links["edges"]),
            "approvedBookHabitatEdges":len(eligible),
            "sourceHabitatReviewStatus":links.get("reviewStatus",""),
            "fieldDatabaseModified":False,
            "referenceSourceConnection":"offline_bundled",
            "editorialDriveFilesUnmodified":True,
        }
        if checklist_csv is not None:
            result["checklistComparison"] = compare_checklist_csv(
                Path(checklist_csv).read_text(encoding="utf-8-sig"), book_names)
        return result
    finally:
        ASSETS = old_assets


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--checklist-csv", type=Path,
        help="Optional CSV export of the editable Master Checklist; read-only comparison")
    options = parser.parse_args()
    print(json.dumps(verify(checklist_csv=options.checklist_csv),
        indent=2, ensure_ascii=False, sort_keys=True))


if __name__ == "__main__":
    main()
