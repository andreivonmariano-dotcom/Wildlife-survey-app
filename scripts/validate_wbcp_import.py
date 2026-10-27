#!/usr/bin/env python3
"""Validate private WBCP 2025 bird CSV before LIKAS imports it.

The copyrighted original checklist is never embedded in this repository.
Validation works with a user-supplied converted CSV only.
"""
import argparse
import csv
import hashlib
import pathlib

COLUMNS=("taxon_key","taxon_group","common_name","scientific_name",
         "family","source","source_version","region_codes","review_status",
         "source_url","ebird_url","sound_url")

def inspect(csv_file):
    seen_keys=set()
    seen_names=set()
    count=0
    mindanao=0
    endemic=0
    with open(csv_file,encoding="utf-8-sig",newline="") as source:
        reader=csv.DictReader(source)
        if not reader.fieldnames or not set(COLUMNS).issubset(reader.fieldnames):
            raise ValueError("Required attributed bird checklist columns missing")
        for line,row in enumerate(reader,2):
            if not row["scientific_name"] or not row["taxon_key"]:
                raise ValueError(f"Missing taxon ID/scientific name at row {line}")
            if row["taxon_group"]!="Birds":
                raise ValueError(f"Non-bird group at row {line}")
            if not row["source"].startswith("Wild Bird Club of the Philippines"):
                raise ValueError(f"Unexpected source at row {line}")
            if row["source_version"]!="2025-04-24_IOC15.1":
                raise ValueError(f"Version mismatch at row {line}")
            if row["review_status"] not in ("reviewed","unreviewed"):
                raise ValueError(f"Review status unrecognized at row {line}")
            if row["ebird_url"] and not row["ebird_url"].startswith(
                    "https://ebird.org/species/"):
                raise ValueError(f"Unexpected external bird reference at row {line}")
            if row["sound_url"] and not row["sound_url"].startswith(
                    "https://www.xeno-canto.org/species/"):
                raise ValueError(f"Unexpected sound reference at row {line}")
            key=row["taxon_key"].casefold()
            sci=row["scientific_name"].casefold()
            if key in seen_keys or sci in seen_names:
                raise ValueError(f"Duplicate taxon identifier/scientific name row {line}")
            seen_keys.add(key);seen_names.add(sci)
            count+=1
            if row["distribution_status"]=="Endemic":endemic+=1
            if "MINDANAO" in row["region_codes"].split("|"):mindanao+=1
    if count!=761:
        raise ValueError(f"Expected 761 WBCP 2025 species, got {count}")
    if endemic!=274:
        raise ValueError(f"Expected 274 WBCP source-coded endemics, got {endemic}")
    if mindanao!=179:
        raise ValueError(f"Expected 179 unambiguous Mindanao mentions, got {mindanao}")
    return {"species":count,"endemics":endemic,"mindanao_mentions":mindanao}

def main():
    arg=argparse.ArgumentParser()
    arg.add_argument("csv_file",type=pathlib.Path)
    arg.add_argument("--sha256",required=True,help="Verified checksum for converted CSV")
    args=arg.parse_args()
    digest=hashlib.sha256(args.csv_file.read_bytes()).hexdigest()
    if digest.lower()!=args.sha256.lower():
        raise ValueError("Bird source integrity check failed")
    print(inspect(args.csv_file))
    print("SHA256",digest)

if __name__=="__main__":main()
