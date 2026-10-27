#!/usr/bin/env python3
"""Extract explicitly named island/province source records from a vetted LIKAS CSV.

For private source-derived imports. This does not infer local occurrence for
taxa missing from the source or convert coarse distribution to survey GPS.
"""
import argparse
import csv
import re
from pathlib import Path

PROVINCES=("Davao del Sur","Davao del Norte","Davao Oriental","Davao de Oro")
FIELDS=["taxon_key","area_type","area_label","evidence_basis","evidence_text",
        "source","source_version","source_url","review_status"]
SOURCE_URL={
    "Amphibians":"https://www.researchgate.net/publication/360034142_Checklist_of_amphibians_of_the_Philippines_version_2025-1",
    "Reptiles":"https://www.researchgate.net/publication/362324825_Checklist_of_reptiles_of_the_Philippines_version_2026-1",
}
def extract(taxa):
    evidence=[]
    for row in taxa:
        text=(row.get("distribution_text") or "").strip()
        if not text:continue
        base=dict(taxon_key=row["taxon_key"],evidence_basis="original author distribution field",
            evidence_text=text,source=row["source"],
            source_version=row["source_version"],
            source_url=SOURCE_URL.get(row.get("taxon_group",""),""),
            review_status="source_listed")
        if re.search(r"\bMindanao\b",text,re.IGNORECASE):
            evidence.append(dict(base,area_type="island",area_label="Mindanao"))
        for province in PROVINCES:
            if re.search(r"\b"+re.escape(province)+r"\b",text,re.IGNORECASE):
                evidence.append(dict(base,area_type="province",area_label=province))
    return evidence
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source",type=Path)
    parser.add_argument("output",type=Path)
    args=parser.parse_args()
    with args.source.open(encoding="utf-8-sig",newline="") as f:
        records=list(csv.DictReader(f))
    if not records or "distribution_text" not in records[0]:
        parser.error("Expected attributed LIKAS catalog with distribution_text")
    data=extract(records)
    args.output.parent.mkdir(parents=True,exist_ok=True)
    with args.output.open("w",encoding="utf-8",newline="") as f:
        writer=csv.DictWriter(f,fieldnames=FIELDS)
        writer.writeheader();writer.writerows(data)
    island={r["taxon_key"] for r in data if r["area_type"]=="island"}
    davao={r["taxon_key"] for r in data if r["area_type"]=="province"}
    print(f"Mindanao source-listed taxon IDs: {len(island)}")
    print(f"Davao province source-listed taxon IDs: {len(davao)}")
    print(f"Evidence records: {len(data)}. Not station sightings.")
if __name__=="__main__":
    main()
