# LIKAS RC67: Mindanao v3.6 private catalog replacement, QA and release gate

**Date:** 1 November 2026 (Asia/Manila)  
**Source branch:** `feature/likas-rc67-mindanao-master-catalog`, based on verified RC66 `a8fc610f5998d058920c29060b89cd95f987f1c0`.  
**Build intent:** `0.27.3-rc67-mindanao-master-preview`, versionCode 127, unchanged application ID `org.phwildlife.survey.beta08`, SQLiteOpenHelper schema version 27.  
**Status:** source-stage only pending CI, private CSV import/instrumentation, signed APK retrieval, real handset testing and redistribution rights.

## Input and data checks

Owner upload: `LIKAS_725_Clean_Master_Checklist_v3_6_2026-11-01.xlsx`, one sheet, 725 unique binomial names, nine columns. Derived user-private `LIKAS_Mindanao_725_Private_Catalog_Import_v3_6.csv` SHA256 `b7bd31f5c8a897d778fecfb38fb64c324f97473410feef528d7120509aa18441`, 350,049 bytes. Group counts: 414 Birds, 53 Amphibians, 156 Reptiles, 102 Mammals; 133 distinct families and 37 orders. The source has 24 unprovided global IUCN categories and 440 unprovided DENR categories, which the import leaves SQL NULL. A lexical screening found 90 distributions without the text "Mindanao" or "Philippines". These are **review flags**, not proof of island absence. Original habitat and elevation strings are kept as guide-level reference text rather than converted into independently confirmed ecological limits.

`v3.6` is an abbreviated display derivative of the larger v3.5 evidence master. It has no per-species source citations, evidence dates or definitive island occurrence verification. Do not infer Mindanao presence from inclusion. WBCP and other original checklist redistribution permissions are unresolved. **Do not commit the private data CSV to the public GitHub repository or package it as a public APK asset.**

## Source implementation

- `CatalogActivity`: local Android document-picker action for the exact owner-supplied derived CSV, 725-record validation, explicit replacement confirmation and asynchronous transaction.
- `MindanaoMasterImporter`: one atomic SQLite transaction backs up existing `taxon_catalog`, `taxon_synonyms`, `taxon_occurrence_evidence` and `taxon_habitat_profiles` to `likas_prior_*` archive tables before clearing active reference tables and installing 725 keyed rows. Stores raw source habitat/elevation in `likas_master_attributes`, with an installation audit. No `surveys`, `field_records`, `sightings`, `record_edits`, `observation_media`, `taxa_favorites` or other field-data tables are modified by the importer.
- `TaxonDetailActivity`: identifies `LIKAS725:` entries and shows only their current master taxonomy, distribution, separated IUCN/DENR snapshot, habitat/elevation and caveats. It does not merge unverified v3.6 facts with older separate species narrative profiles.
- `Rc7SpeciesLibraryActivity`, `SpeciesPickerActivity` and name suggestions already read the shared `taxon_catalog`, so the active set becomes 725 after import. Old sources can be recovered from the local archive tables, but restoration UI is not implemented.

## Scientific and workflow boundaries

- The CSV is not bundled; newly installed app has existing source catalogs until its owner selects and activates the private CSV. The source branch alone is not an installed update.
- No new `taxon_occurrence_evidence` is asserted. The Live Recorder's "Local source records" filter therefore has no newly verified v3.6 island-locality evidence until an attributable review layer is imported. "All species" still lists all active candidates.
- Historic raw identifications remain unchanged, including names not present in the new active source reference. Existing favorites may retain original raw strings.
- The one-time archive uses SQLite tables inside the same transaction. Validate backup/export inclusion and real schema/data copies before shipping. A database schema version bump may be required if later automatic upgrades must manage these importer-created tables.

## Test gates

- Verify Java unit tests compile and run (`MindanaoMasterImporterTest` checks complete/duplicate/truncated/incorrect-group/blank-source cases).
- On Android emulator, activate the actual CSV and confirm exactly 725 active rows, exact 414/53/156/102 group counts, both Library and Live Recorder names, proper NULL global/national statuses, 725 habitat/elevation detail rows, no legacy catalog search results, archives populated, second import idempotent in active rows and rollback on malformed input.
- On a copy of real fieldwork data, compare counts, IDs and byte/hash integrity of observations, survey records, media, synonyms and habitat evidence before and after activation. Confirm ZIP export/backup and reinstallation.
- Verify app package ID, code127, original signing certificate, version name, startup/library assets, RC66 responsive UI, real phone readability, picker search and offline operation.
- No APK is accepted solely because a branch or CI compile exists. Deliver a verified **direct downloadable** APK in the implementing conversation if available. Do not substitute GitHub links.

**Affected requirements:** G-01, G-03, G-04, G-09, G-10, G-12, L (species library), T (taxonomy/provenance), D (data), U (user experience), E (release checks).
