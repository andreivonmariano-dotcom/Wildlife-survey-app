# LIKAS v0.18.2: Emerson Y. Sy checklist import acceptance

Data source: two private user-provided spreadsheets, not copied into GitHub.
- Sy amphibians, version 2025-1, source workbook SHA-256:
  2872e13f198767516b6e6054a79f06fa030fd81db7a6a7ada9d64e0af289411c
- Sy reptiles, version 2026-1, source workbook SHA-256:
  a62bc3f6c0426f8ddcd901bc83d0e5d24b52fec7eea3184f05a7ce0cfa2e9794

## Extracted records
- Amphibians: 116 named entries, 100 endemic, 11 native, 5 introduced.
- Reptiles: 380 entries, including 379 species plus 1 unresolved
  genus-level King Cobra/Ophiophagus entry. Among resolved entries,
  292 endemic, 80 native and 7 introduced.
- Mindanao: 45 amphibian and 128 reptile entries whose *source text*
  explicitly lists Mindanao. The latter includes the unresolved
  genus-level entry.
- 19 named amphibian synonyms from 16 source rows. The source reptile
  workbook has no comparable synonym column.
- Source status fields: distribution, endemism/native/introduced, PH DAO
  2019-09, workbook IUCN category, CITES and taxonomic authority/year.
- The source checklists are dated snapshots, not a live IUCN lookup.

## App workflow
1. Back up project ZIP, then upgrade without uninstalling or resetting app ID.
2. Schema 14 -> 15 adds metadata columns to taxon_catalog, not field_records.
3. In Data & Tools > Offline taxonomy, choose Import species checklist CSV.
   Import the combined source-derived file
   LIKAS_Philippine_Herpetofauna_Sy_2025_2026_FULL.csv.
4. Confirm 496 entries; inspect count by Reptiles=380 and Amphibians=116.
5. Search Ansonia mcgregori: accepted scientific name with McGregor's common
   name, endemic and original IUCN LC / PH VU codes underneath.
6. Search Pelobatrachus ligayae using former Megophrys ligayae.
   The accepted label must be suggested. The alias remains linked and does
   not rewrite historical observation strings.
7. With Mindanao area selected, inspect explicit source-reported island
   candidates; national unassessed species remain searchable if strict
   documented-only filtering is off.
8. Search Ophiophagus. It must retain the source's genus-level uncertainty,
   and no false species epithet or conservation category.
9. Repeat import. Stable taxon keys should prevent duplicate rows; synonyms
   should not duplicate. Confirm SQLite transaction rollback on malformed CSV.
10. Browse 60-result limits and long common names on a low-cost handset.
11. Save detections and ZIP export. Verbatim and attributed name_source fields
    must not silently change with taxon updates.
12. Check normal/herpetofauna/amphibian-calls method group switching.

## Publication
No full source table has been committed to this private source branch.
Redistribution terms are unverified. The user can import source-derived
files locally for private scientific evaluation; public APK bundling
requires explicit source permissions.

## Broader checklists
- The comprehensive Philippine bird occurrence reconciliation with
  AviList v2026 is separate from this source-derived herpetofauna import.
- Endemism and conservation metadata for mammals/birds require their own
  version-pinned sources; blank fields must not be fabricated.
