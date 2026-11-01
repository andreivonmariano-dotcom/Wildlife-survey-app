# LIKAS RC68 | Mindanao PAIC 714 checklist and Species Library screen

**Date:** 1 November 2026, Asia/Manila.  
**Source branch:** `feature/likas-rc68-mindanao-paic714-species-library`, branched from RC67 source revision `28a92c67f26d9be6b865bb9dec7eafac2b89eb9c`.  
**Intended build:** `0.27.3-rc68-paic714-library-preview` / versionCode **128**, unchanged application ID `org.phwildlife.survey.beta08`, original signer required, SQLite schema **27**.  
**Status:** Source implemented. Android CI and physical-phone acceptance to be established separately; no app deployment or signed APK claimed by this document.

## Input and verified transformation

Owner-selected workbook `LIKAS_Mindanao_PAIC_Final_Checklist_714_Species_2026-11-01.xlsx` has 714 unique scientific names and exactly eleven species in its distinct Omit 11 worksheet, absent from its Master Checklist. Converted privately into `LIKAS_Mindanao_PAIC_714_Private_Import_2026-11-01.csv`, UTF-8, 26 columns, **230,291 bytes**, SHA-256 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`. Group counts: Birds **408**, Amphibians **53**, Reptiles **154**, Mammals **99**. The approved sorted case-folded scientific-name roster SHA-256 is `a666cb283265131d53591a782e3e4a0a8a3661521266e30f6d58d0dbcba1552c` (UTF-8 with newline separators, no trailing newline). All eleven omitted names are rejected explicitly by the importer; the name-roster hash prevents a different 714-row dataset being accepted. Source-specific missing IUCN/DENR cells remain SQL NULL.

The publicly accessible GitHub source contains only importer logic and the roster digest, **not** the checklist CSV or its proprietary full contents. Original taxon IDs beginning `LIKAS725:` intentionally remain stable for overlapping names, so historical observation foreign keys and linked reference identifiers are not silently rewritten when the catalog count changes. This prefix is internal, not a count displayed to users.

## Screen 1 and supporting data integration

- `CatalogActivity` prompts the owner to activate exactly the approved 714-species CSV, shows current activation counts, and fixes an existing undefined `libraryCounts` variable in its refresh method.
- `Rc7SpeciesLibraryActivity` displays **Mindanao PAIC species** when the private 714 catalog is active; group/search/page controls continue to work on the shared active `taxon_catalog`. Historical regional-evidence and comparison tools are hidden while PAIC714 is active to avoid offering conflicting legacy coverage. The photo filter remains usable.
- `TaxonDetailActivity` shows harmonized source-appropriate labels and distinct IUCN vs DENR snapshots, source distribution, habitat, and guide elevation without merging unrelated legacy descriptions.
- `MindanaoMasterImporter` validates the exact roster and counts, archives old reference data once in an atomic database transaction and swaps active taxon tables, without modifying actual surveys, observation media, survey effort, or historical identification text.
- `SpeciesPickerActivity` currently reads the same shared catalog, but its auto-generated geographic/elevational checklists and survey setup UI are **not modified** in this screen.

## Acceptance criteria still required

1. CI test Python and `MindanaoMasterImporterTest`, including pinned edition and omission rejection; signed APK must retain original certificate, package, and code128.
2. Android emulator: import private CSV through system document picker; verify exactly 714 entries, group split 408/53/154/99, correct status missingness, all 714 descriptions, omitted-name absence, active Library and Live Recorder catalog queries.
3. Before/after data comparisons on a realistic filled database, including detection table, media paths, all three survey closure outcomes, source archive and export ZIP. Test a repeated import, malformed CSV rollback, process death, and migration from a previous on-device version.
4. Readable Species Library and detail page screenshots at 360/440dpi and 200% font. Real handset / GPS field tests and TalkBack remain outstanding.

**Licensing:** WBCP, Sy and other underlying source data redistribution needs clearance before bundling the roster into a public APK. For now, the owner privately selects the CSV, and the legacy bundled catalog remains until activation. A one-time private import is not proof that all installed devices already show 714 taxa.

**Affected master requirements:** G-01, G-03, G-09, G-10, G-12, T-01–T-05, D-01, L-01/02 where relevant to later matching, U (clear source status), E (APK signing and QA).
