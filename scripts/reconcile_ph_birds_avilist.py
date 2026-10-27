#!/usr/bin/env python3
"""Conservatively match a Philippine occurrence checklist to AviList v2026.

Reads locally supplied CSV exports from the official AviList and an attributed
Philippine occurrence list. Does not infer national presence from global taxonomy.
Unmatched, duplicate and potentially split taxa are withheld for manual review.
For private scientific use until every source's redistribution terms are resolved.
"""
import argparse
import csv
import hashlib
import pathlib
import re
from collections import defaultdict

COLUMNS=["taxon_key","taxon_group","common_name","scientific_name","family",
         "source","source_version","region_codes","review_status"]
REVIEW=["original_name","match_status","candidate_names","reason"]
def norm(value):
    return " ".join((value or "").casefold().split())
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def rows(path):
    with path.open(encoding="utf-8-sig",newline="") as f:
        yield from csv.DictReader(f)
def reconcile(avi,occurrence,id_col,scientific_col,common_col,family_col,
              occurrence_scientific_col,source,source_version):
    by_name=defaultdict(list)
    for row in avi:
        name=(row.get(scientific_col) or "").strip()
        identifier=(row.get(id_col) or "").strip()
        if not identifier or not name:continue
        by_name[norm(name)].append(row)
    matched=[]
    review=[]
    used_ids=set()
    for record in occurrence:
        original=(record.get(occurrence_scientific_col) or "").strip()
        if not original:continue
        matches=by_name.get(norm(original),[])
        if len(matches)!=1:
            review.append(dict(original_name=original,
                match_status="unmatched" if not matches else "ambiguous",
                candidate_names="|".join(
                    sorted({x.get(scientific_col,"") for x in matches})),
                reason="Requires documented taxonomic crosswalk or split review"))
            continue
        chosen=matches[0]
        identifier=chosen.get(id_col,"").strip()
        if identifier in used_ids:
            review.append(dict(original_name=original,match_status="duplicate",
                candidate_names=chosen.get(scientific_col,""),
                reason="Multiple occurrence rows resolve to one accepted ID"))
            continue
        used_ids.add(identifier)
        matched.append(dict(
            taxon_key="AVILIST2026:"+identifier,taxon_group="Birds",
            common_name=(chosen.get(common_col) or "").strip(),
            scientific_name=(chosen.get(scientific_col) or "").strip(),
            family=(chosen.get(family_col) or "").strip(),
            source="AviList Core Team v2026; country evidence: "+source,
            source_version="AviList-2026|"+source_version,region_codes="",
            review_status="reviewed"))
    return matched,review
def write(path,fields,items):
    with path.open("w",encoding="utf-8",newline="") as f:
        writer=csv.DictWriter(f,fieldnames=fields,extrasaction="ignore")
        writer.writeheader();writer.writerows(items)
def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--avilist",required=True,type=pathlib.Path,
        help="Official AviList v2026 CSV export, preserving accepted species IDs")
    p.add_argument("--philippines",required=True,type=pathlib.Path,
        help="Attributed Philippine occurrence list CSV, not a global list")
    p.add_argument("--id-column",required=True)
    p.add_argument("--science-column",required=True)
    p.add_argument("--common-column",required=True)
    p.add_argument("--family-column",required=True)
    p.add_argument("--occurrence-science-column",required=True)
    p.add_argument("--occurrence-source",required=True)
    p.add_argument("--occurrence-version",required=True)
    p.add_argument("--output",required=True,type=pathlib.Path)
    p.add_argument("--review-report",required=True,type=pathlib.Path)
    p.add_argument("--avilist-sha256",required=True)
    p.add_argument("--occurrence-sha256",required=True)
    a=p.parse_args()
    for path,expected in [(a.avilist,a.avilist_sha256),
                          (a.philippines,a.occurrence_sha256)]:
        if digest(path)!=expected.lower():
            p.error("SHA-256 mismatch on "+str(path))
    matches,review=reconcile(list(rows(a.avilist)),list(rows(a.philippines)),
        a.id_column,a.science_column,a.common_column,a.family_column,
        a.occurrence_science_column,a.occurrence_source,a.occurrence_version)
    if not matches: p.error("No exact taxon matches; source columns need review")
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.review_report.parent.mkdir(parents=True,exist_ok=True)
    write(a.output,COLUMNS,matches)
    write(a.review_report,REVIEW,review)
    print("Matched Philippine candidate taxa:",len(matches))
    print("Unmatched, ambiguous or duplicate names for expert review:",len(review))
    print("Country source:",a.occurrence_source,a.occurrence_version)
    print("AviList v2026: CC BY 4.0. Preserve citation and source terms.")
if __name__=="__main__":
    main()
