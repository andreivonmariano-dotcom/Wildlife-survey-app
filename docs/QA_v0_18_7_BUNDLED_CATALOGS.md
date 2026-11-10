# LIKAS v0.18.7: fully offline, self-contained scientific checklist QA

**Private research build only.** Uploaded WBCP 2025 and Emerson Y. Sy
2025/2026 checklists are embedded by the researcher's request. WBCP's
published terms permit personal/scientific reference and restrict commercial
use without written permission. Explicit permission to redistribute Sy's
complete workbook data has not been verified. Do not publish this private
binary as a public or commercial APK.

## Verified built-in authorities
- **Birds**: 761 WBCP 2025 entries, IOC v15.1 names, with common/scientific
  names, source-dated conservation labels, original distribution notes,
  historic synonym field and 179 explicitly listed Mindanao references.
- **Amphibians**: 116 entries from Emerson Y. Sy checklist 2025-1.
- **Reptiles**: 380 entries from Emerson Y. Sy checklist 2026-1, including
  one unresolved genus-level Ophiophagus entry, not an invented species name.
- **Herpetofauna evidence**: 184 source location references (173 Mindanao,
  11 province references across the named Davao provinces).
- **Mammals**: 246 Philippine national candidates from the official MDD v2.5,
  DOI 10.5281/zenodo.21654811. The Android CI downloads this pinned source,
  checks the MD5 and packages it as JSON separately inside the same APK.
- WBCP + Sy local bundle: 1,257 taxon entries and 363 source location links.
  Expected 1,503 total source records including 246 MDD candidates.

## Integrity and upgrade
1. Verify SHA-256 of built-in zlib-compressed dataset matches
   `7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6`.
2. Run `python3 -m unittest discover -s scripts -p 'test_*.py'`.
3. Run `gradle :app:testDebugUnitTest :app:assembleDebug`.
4. On a new install, opening Offline taxonomy must report 761 Birds, 116
   Amphibians, 380 Reptiles and MDD mammal candidates. Legacy starter
   records are excluded from displayed authoritative source counts.
5. Upgrade from v0.18.6 with existing recorded surveys. SQLite 17 -> 18
   must install the new catalog automatically on database opening, without
   deleting projects, tracks, stations, observation photos or source notes.
6. Search each group offline with Flight Mode on. Search by both common and
   scientific name. Region scope is separate from presence at the station.
7. Confirm taxon_synonyms references remain available. Original verbatim
   observation names, timestamps, GPS details and historical outcome audits
   must not change.
8. No ZIP picker or CSV import choice appears on the standard Offline taxonomy
   screen. Show catalog sources and version details in one tap.
9. Ensure update signing certificate matches earlier LIKAS Beta APK. Back up
   existing research data before testing installation.
10. A corrupted/changed private asset must fail the catalog integrity test,
    rather than partially modifying the database or fabricating taxon records.

## Publication and scientific scope
National checklist membership does not establish provincial, island or
station presence. Locality evidence preserves only explicitly reported
published areas. A taxon missing from source evidence is unassessed, not
proven absent. Source-dated IUCN and Philippine status fields are not live
conservation assessments.

Before public distribution, secure author/publisher redistribution permission
or substitute sources with clear licenses. The private GitHub branch and
workflow artifact are for the researcher's controlled testing.
