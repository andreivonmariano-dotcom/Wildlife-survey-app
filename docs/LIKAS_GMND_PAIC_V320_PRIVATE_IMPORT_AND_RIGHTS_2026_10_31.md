# LIKAS Greater Mindanao PAIC | v3.2.0 private research integration checkpoint
**31 October 2026, Asia/Manila.** Source workbook examined in current project conversation; this note contains no owner-supplied checklist rows.

## Source and completed internal checks
- Input: `LIKAS_Greater_Mindanao_PAIC_Vertebrate_Master_Checklist_v3.2.0_2026-10-31.xlsx`
- Exact input SHA-256: `ae66c2278ef9da8d96ff6e05a65d69802b0eaf592ef6517d569049b7446220fc`
- One source-master tab: **722 distinct scientific names/taxon IDs**, of which **721 Included** and **1 Excluded**.
- Included: **413 Birds, 156 Reptiles, 53 Amphibians, 99 Mammals**. The master contains 414 bird names because the excluded `Cinnyris aurora` remains auditable and is not counted.
- Island evidence: **1,323 rows**; source assertions: **880**; bibliography: **27**; taxonomic decision records: **28**.
- The named-species regional review queue has **0** remaining entries. This means the source compiler adjudicated listed candidates; it is not evidence of taxonomic completeness, independent confirmation of all regional reports, or a source census.
- Reconciliation of included IDs against master status, taxon ID/name uniqueness, island-evidence foreign IDs and source-assertion linkage passed; no missing island links, source assertions or source URLs among included taxa.
- The 81 cases on the v3.2 regional-resolution sheet were promoted with regional secondary compiled checklists. Three additional source-specific decisions were made from an observer article, an agency report of a monitoring-system record and a separate compiled provincial checklist. Compiled locality reports do not imply vouchers, current residency or site-level occupancy.
- Eight Camiguin-Sur-only species are tagged as associated island members outside the core historical PAIC. No provincial checklist product is restored.

## Research and source-rights decision
The owner explicitly states LIKAS is intended for **personal and noncommercial scientific research**. The WBCP official page (https://birdwatch.ph/philippine-checklist/) authorizes download for personal/scientific reference, requires citation for use in publications/reports, and requires written permission for commercial use. That is favorable for source consultation, but it is not an affirmative blanket license for wholesale public APK redistribution. Emerson Y. Sy's 2025-1 amphibian and 2026-1 reptile compilation redistribution rights remain unverified. Secondary checklist/database terms (e.g., Avibase) need their own review. The declaration of research purpose does not modify third-party rights.

Only source-free method, aggregate counts, licenses, checksums, and code may enter this **public** repository at this stage. The actual owner-provided v3.2 workbook, derived full CSV/JSON, localities, and private research ZIP were delivered directly in the project conversation and were **not** committed to GitHub. Public build inclusion is **not** cleared. The older `app/src/main/assets/likas_ph_checklists_v2026_private.b64` on this public repository branch should be treated as a separate legacy source-rights exposure requiring review; do not cite it as evidence that v3.2 source redistribution is lawful.

## Private reproducibility and transfer
A 21-entry ZIP export was locally generated containing the **unchanged source workbook**, owner-facing readme, SHA-256 manifest, independent QA report/results, stable ID preserved tables, bibliography, original source assertions, unresolved identification archive, taxonomic decisions, and an offline JSON snapshot. ZIP CRC and manifest checks completed, input workbook hash matched. ZIP SHA-256: `17d558084e900124e55025b578a4210bc5f00bcb2d7b663028f623eeef6ade76`. The local artifact is conversation-private, not a repository source or public release.

## Required import contract before an Android build
1. Provide a **user-selected, owner-local** PAIC source file import that validates schema, explicit version/source attribution, stable ID uniqueness, group scope, referenced island evidence and checksum. Do not attempt to pass this pack to the preexisting `ChecklistPackImporter`, which enforces a different four-CSV hash-pinned national data package.
2. Store PAIC candidate membership separately from national taxonomy and field observations. Model core PAIC versus associated Camiguin Sur, accepted scientific names versus exact source names, excluded versus unresolved IDs, and missing evidence versus confirmed absence.
3. Preserve secondary-source evidence class and its uncertainty. The 721 are **source-supported regional reference candidates**, not verified site detections or population counts. Preserve introduced, migrant, vagrant, historical and other source-reported qualifiers when present.
4. Never overwrite recorded survey names, GPS locations, data history, or the existing user field database on catalog updates. Validate migration in CI and on a real Android phone.
5. Keep restricted data outside public GitHub and public APK. A public prebundled dataset needs a separate source-by-source reuse/permission review or replacement with clearly licensed records.
6. Keep the legacy bundled national-source copyright issue open as a distinct task, including public repository history, while applying no destructive unreviewed removal.

## Status
This is a scientific research-data conversion/checkpoint and source-rights decision, **not** an Android application change. No SQLite migration, versionCode increment, physical-device validation, or newly compiled APK occurred. Latest checked science/UI branch predecessor has RC66 `versionCode 126` and SQLite schema 27; do not claim v3.2 is installed.
