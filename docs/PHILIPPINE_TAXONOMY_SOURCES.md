# LIKAS: authoritative Philippine taxonomy register

Revision: 2026-10-27. This register concerns national species candidates, not a
validated checklist for any single island, province, forest, station or survey.

## Installed and planned sources

### Mammals: installed through CI build preparation

- Authority: American Society of Mammalogists, Mammal Diversity Database v2.5.
- Official source: https://zenodo.org/records/21654811
- Source file: `MDD_v2.5_6904species.csv`, MD5 `533e662fd5b8f66a5f56c191e7efac44`.
- Source released 2026-07-28. Source retrieval is pinned and checked in
  `scripts/prepare_mdd_mammals.py`.
- The v0.16 build workflow already prepares approximately 246 Philippine
  candidate mammals filtered using MDD's countryDistribution. Extinct and
  domestic species are excluded, uncertain country membership and flagged
  names remain marked unreviewed.
- The national list is not evidence for presence on Mindanao, Davao del Sur,
  a particular site or the observer's current GPS location.

### Birds: latest source verified, national filtering pending

- Authority: AviList Core Team, *AviList: The Global Avian Checklist*, v2026.
- Official release: 2026-10-20, DOI https://doi.org/10.2173/avilist.v2026
- Source: https://www.avilist.org/checklist/v2026/
- License: Creative Commons Attribution 4.0, requires attribution and a link
  to the license for any redistributed derivative.
- AviList v2026 is a **global taxonomic backbone**, not a complete Philippines
  country occurrence filter. Full national coverage requires a separate
  verified Philippine occurrence list with matching scientific names.
- Do not assign Philippines occurrence from global taxonomy membership, or
  classify a global candidate as verified local fauna.

### Amphibians: latest author version located, full bundle pending

- Authority: Emerson Y. Sy, *Checklist of Amphibians of the Philippines*,
  version 2025-1, updated 2025-02-25.
- Author's record:
  https://www.researchgate.net/publication/360034142_Checklist_of_amphibians_of_the_Philippines_version_2025-1
- Author's downloadable PDF does not establish permission to redistribute
  the complete checklist in a publicly shared Android APK. Request written
  permission for bundling or use a clearly licensed machine-readable source.
- Do not claim the previous small starter list is a complete national list.

### Reptiles: latest author version located, full bundle pending

- Authority: Emerson Y. Sy, *Checklist of Reptiles of the Philippines*,
  version 2026-1, updated 2026-02-28.
- Author's record:
  https://www.researchgate.net/publication/362324825_Checklist_of_reptiles_of_the_Philippines_version_2026-1
- The source is author-uploaded and marked 'may be subject to copyright'.
  Full APK redistribution terms require verification.
- Versioned source names, stable IDs and scientific nomenclature must be
  checked against the author's actual source, not reconstructed from search
  results or outdated species catalogs.

## Import contract

The existing app's Offline checklist importer accepts a UTF-8 CSV with
exactly these nine named columns:

`taxon_key,taxon_group,common_name,scientific_name,family,source,source_version,region_codes,review_status`

Stable keys should be prefixed by the source name (for example `AVILIST:`
for birds or `SY26:` for a permissioned, version-checked reptile catalog).
The taxon_group value is one of Birds, Reptiles, Amphibians, Mammals.
Review status is either reviewed or unreviewed.

Regional codes must remain empty unless evidence for that region was
separately verified. A blank region code means *not assessed*, not absent.
Allow researchers to type any name or unresolved identification.

## Repeatable preparation

Use `scripts/validate_ph_taxa.py` with an independently checked input CSV,
verified file SHA-256, group, authority and version. The script verifies each
row, rejects duplicate keys/names, preserves source attribution and writes
a LIKAS-ready CSV. No species list, author PDF or source file is embedded
without explicit provenance and redistribution clearance.

The published checklist is never silently mapped onto existing survey rows.
Historic `raw_identification`, `name_source` and edits stay intact.

## Release gates for a national source

1. Source obtained from original publisher, author or official repository;
   release version and checksum recorded.
2. Source redistribution terms confirmed. Full data rows validated and
   reviewed by group; endemic, migrant, introduced and uncertain statuses
   remain distinguishable in future occurrence metadata.
3. Stable identifiers and source date stored; accepted scientific names and
   synonyms reconciled without rewriting verbatim observation text.
4. National candidates and local evidence must remain separate.
5. Unit tests, Android upgrade migration, offline large-catalog search,
   30-minute field sessions and CSV/ZIP export checked on a real handset.
