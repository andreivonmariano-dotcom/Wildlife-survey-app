# RC101 | Canonical PHLibrary Book 01 v1.82 integration and QA

**Date:** 8 November 2026 (Asia/Manila)  
**Development branch:** `feature/apo-rc101-phlibrary-v182-canonical-v161`, PR #106 (draft)  
**Baseline:** RC100 `7d4421993fd1af496237946fdfafb47fb94f458d` (signed Android emulator upgrade CI previously successful).  
**App:** `org.phwildlife.survey.beta08`, code **161**, `0.27.3-rc101-phlibrary-book-v182`, SQLite **29**, original signing configuration. No field database migration.

## Owner authority and scientific reference

The owner locked **PHLibrary Book 01 v1.82** (SHA256 `a35f0a53e0586a071c1bd9ff807150643313982503693cd06ea8905f7aeaa279`) and **Discovery Rarity v0.3.4** (SHA256 `2f0756f5a662e1ea33ac069cf3d7c805949d5568f164198807cb615c1ccd87cc`) as app content sources. Original files are in personal Library `/PHLibrary/Locked References/`; see `docs/PHLIBRARY_CANONICAL_BOOK_V182_REFERENCE_LOCK_2026_11_08.md`.

A Python converter `scripts/build_phlibrary_v182.py` extracts Word account heading, group, order, family, badges, full untouched natural-history paragraph, explicit IUCN badge and author-year citation fragments. It checks the source Word and rarity workbook SHA256 before output, cross-checks **every account in order** against the companion workbook and checks all group and rarity distributions. All 715 paragraphs remain **exactly as written in v1.82** (not reconstructed from old v1.75/overlays).

## Canonical assets committed

- `app/src/main/assets/phlibrary_book01_v182.json.gz.b64`: deterministic gzip of UTF-8 JSON containing 715 complete records, with decoded SHA256 `a87021a615a93f64b56cb3c8974e57deaf69bdd7f0ca7478b70068bad533e885`. Original decompressed JSON is 1,163,654 bytes; gzip is 199,640 bytes.
- `app/src/main/assets/phlibrary_book01_bibliography_v182.json.gz.b64`: full original **1,159-entry** Word bibliography, decoded JSON SHA256 `cb597385f1fc10bcca5f3f9aa5ddafd64b37d762f481816ca479e26df3a9751e`.
- `app/src/main/assets/phlibrary_book01_taxon_crosswalk_v182.json.gz.b64`: historical provisional frog and split kingfisher mapping, **autoMapObservations=false**. No survey record or old catalog ID change.
- Archived v1.75 JSON and v1.76–v1.80 overlays remain intact for traceability; no longer applied by runtime canonical reader.

Current Book v1.82 record counts: **715** (409 Birds, 53 Amphibian entries incl. 1 provisional `Oreophryne cf. nana`, 154 Reptiles, 99 Mammals). Tiers **124 Common, 336 Uncommon, 214 Rare, 37 Exceptionally rare, 3 Legendary, 1 Unranked**. `Ceyx melanurus` and `Ceyx mindanensis` have separate entries. Prior `Aphantophryne nana` is not a current accepted Book entry.

## Reader and navigation changes

- `PhLibraryBook01` loader reads one versioned compressed file, validates the exact decoded SHA256 and the entire 715-account taxonomy/rank contract **before** publishing the in-memory record list. Read failure is atomic; the directory offers a Retry action. No write access to the survey database.
- Uses original body paragraph unmodified and still displays source-defined taxon, status and reader edition separately. `Oreophryne cf. nana` displays “Unranked · Provisional identification”. Original short citations remain in article text.
- New offline `PhLibraryBibliographyActivity` provides search within all 1,159 original bibliography entries. Account citation excerpts open separately with an optional direct link to bibliography search. Missing author/title/DOI metadata are **not invented**. Existing 2,760 per-account citation fragments are currently unresolved pointers rather than independently matched full records. A future bibliography-reconciliation pass should match these to exact original sources; preserving full original reference text is the first step.
- `PhLibraryDirectoryActivity`: 715 dynamically labeled accounts, group filters, five rarity tiers plus Unranked, full-text account/description search and an explicit retry on verification failure. Full-text results are **not** evidence of a species being present at the observer's locality. Existing scrollable 24-record pagination is retained as a first migration step; broad faceted index/performance and habitat explorer redesign are later phases.
- Text-only, icon-free/photo-free reader direction retained; old survey database and package/signing contracts unchanged.

## Tests and release gates

- New `scripts/test_rc101_phlibrary_v182.py` checks gzip authenticity, count/tier split, all inline citations, bibliography and source/Android identity.
- New Android `Rc101PhLibraryV182InstrumentedTest` validates all 715 taxa, rarity distribution, frog identification, separate kingfishers, one-paragraph text, complete bibliography.
- Inherited RC96–RC100 Android UI tests updated for 715 entries and revised source line. Archived source tests still verify exact historical 714-row dataset.
- New CI `.github/workflows/apo-rc101-phlibrary-book-v161-qa.yml`: source/JVM/Android compile, original signature/checklist assets, Android14 normal/200% font emulator, signed RC92→RC101 synthetic no-data-clear upgrade, and signed APK artifact upload.
- **Do not claim all CI checks green until the exact latest RC101 run succeeds.** The first run failed on stale version assertions in historical source tests; these are being repaired. Physical owner-handset upgrade, screenshot visual acceptance and full bibliographic linking remain separate checks.
- Latest owner-approved content source also contains educational habitat inference. Do not silently convert these prose descriptions into empirical selection/occurrence data.

## Continuing tasks

1. Complete RC101 CI/Android/source test corrections without deleting historical science or survey tests.
2. Check signed APK artifact SHA256, package, signer, code161, on-device upgrade integrity; deliver direct file to ChatGPT when verified.
3. Only after acceptance implement second-phase integrated habitat discovery and advanced filtering/bookmarks. Do not merge draft PR without review.


## 2026-11-08 23:41 PHT continuation: signed upgrade assertion fix

- Exact PR #106 head before correction `38ae179ff3a8a60df64fafc4c16436b2b2170be9` produced Actions run **42495260907**: source/JVM/build/signature and Android14 reader checks passed; signed RC92→RC101 upgrade instrumentation **failed** on inherited assertion `expected:<160> but was:<161>`. Both `adb install` operations returned Success. At this point the failure was test-version expectation, **not proof of user-data loss**. CI nevertheless failed and did not export an APK.
- Corrected `Rc95SignedUpgradeInstrumentedTest.java` on the **RC101 branch only**: assertion now expects versionCode **161** and explicitly names RC101, with updated code160→code161 comment. Commit `f53cea77c55770494296507ba09bb9931e378730`; no production app logic, source data or schema modified.
- New workflow **42496200691** was triggered on this commit. Final conclusion and independent artifact checks must be verified before claiming successful RC101 signing or distributing an APK.
