#!/usr/bin/env python3
"""Pin the official MDD v2.5 and derive an offline Philippine mammal catalog.

Build-time internet is used only for this publicly archived source. The Android
app itself performs no taxonomy download. Output is not distribution evidence
at the island/province level.
"""
import csv
import hashlib
import io
import json
from pathlib import Path
import re
from urllib.request import Request, urlopen

SOURCE = "https://zenodo.org/records/21654811/files/MDD_v2.5_6904species.csv?download=1"
OFFICIAL_MD5 = "533e662fd5b8f66a5f56c191e7efac44"
DEST = Path("app/src/main/assets/ph_mammals_mdd_v2_5.json")
CACHE = Path(".taxonomy-cache/mdd-v2.5.csv")

def truthy(value):
    return str(value).strip().lower() in {"1", "true", "yes"}

def philippines_membership(value):
    # Retain MDD's uncertain Philippine membership (e.g. Philippines?)
    # as an explicitly unreviewed national candidate, rather than discarding it.
    for part in re.split(r"[|;,]", value or ""):
        item = part.strip()
        if item.strip("?").strip().lower() == "philippines":
            return True, "?" in item
    return False, False

def extract(data):
    reader = csv.DictReader(io.StringIO(data.decode("utf-8-sig"), newline=""))
    required = {"id", "sciName", "mainCommonName", "family",
                "countryDistribution", "extinct", "domestic", "flagged"}
    if not reader.fieldnames or not required.issubset(reader.fieldnames):
        raise ValueError(f"Missing source fields: {required-set(reader.fieldnames or [])}")
    taxa = {}
    for row in reader:
        in_philippines, uncertain_range = philippines_membership(row["countryDistribution"])
        if not in_philippines:
            continue
        if truthy(row["extinct"]) or truthy(row["domestic"]):
            continue
        key = str(row["id"]).strip()
        scientific = str(row["sciName"]).strip().replace("_", " ")
        if not key or not scientific:
            continue
        if key in taxa:
            raise ValueError(f"Duplicate MDD taxon id: {key}")
        taxa[key] = {
            "taxon_key": "MDD:" + key,
            "common_name": (row["mainCommonName"] or "").strip(),
            "scientific_name": scientific,
            "family": (row["family"] or "").strip(),
            "review_status": "unreviewed" if truthy(row["flagged"]) or uncertain_range else "reviewed"
        }
    if len(taxa) < 20 or len(taxa) > 1200:
        raise ValueError(f"Suspicious Philippine candidate count: {len(taxa)}")
    return sorted(taxa.values(), key=lambda x: x["scientific_name"])

def main():
    source = CACHE.read_bytes() if CACHE.is_file() else b""
    if hashlib.md5(source).hexdigest() != OFFICIAL_MD5:
        request = Request(SOURCE, headers={"User-Agent": "Philippine-Wildlife-Survey/0.27"})
        with urlopen(request, timeout=90) as response:
            source = response.read(26_000_000)
        if hashlib.md5(source).hexdigest() != OFFICIAL_MD5:
            raise ValueError("Official MDD v2.5 checksum mismatch: do not build altered taxonomy")
        CACHE.parent.mkdir(parents=True, exist_ok=True)
        CACHE.write_bytes(source)
    # Even cached data must match the pinned official checksum on every build.
    candidates = extract(source)
    DEST.parent.mkdir(parents=True, exist_ok=True)
    DEST.write_text(json.dumps(candidates, ensure_ascii=False, separators=(",", ":")),
                    encoding="utf-8")
    print(f"Prepared {len(candidates)} MDD v2.5 Philippine mammal candidates in {DEST}")
    print("Authority: American Society of Mammalogists, doi:10.5281/zenodo.21654811")

if __name__ == "__main__":
    main()
