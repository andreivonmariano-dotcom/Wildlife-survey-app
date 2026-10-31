# LIKAS RC20 | Nine targeted identification gaps, source-aware ecology and compact account UI

**Date:** 29 October 2026, Philippines local time  
**Branch:** `feature/likas-core-fieldwork-post-rc4` (draft PR #38)  
**Version target:** `0.27.3-rc20-documented-species-preview` / versionCode 73  
**Non-goals:** No startup animation, icon, SQLite schema, observation, route, survey, photo licensing or application ID changes.

## New verified source corpus
`rc20_verified_gap_batch_v1.json` records 16 exact-source facts involving 9 of the 27 formerly missing identification accounts. These include:
- Original taxonomic diagnosis for *Leptobrachium mangyanorum* from Brown et al. 2010.
- Peer-reviewed comparative character descriptions for *Trimeresurus mcgregori* (Sanders et al. 2004) and *Axis calamianensis* (Mammalian Species 2021).
- Published monographic description and behaviour of *Rusa alfredi*, together with a 2016 dated global IUCN status.
- Attributed field- or species-guide traits for *Sarcophanops samarensis*, *Pteropus pumilus*, *Gallicolumba crinigera*, *Ceyx argentatus* and *Harpyionycteris whiteheadi*. These are explicitly NOT represented as original descriptions. They are supporting field-mark evidence, not single-trait diagnosis.
- BirdLife 2022 assessed global Near Threatened category for *Ceyx argentatus* and its documented forested watercourse habitat.

**Caveat on source scope:** The *Pteropus pumilus* Palawan occurrence conflict across MDD and Animal Diversity Web was not converted into a range statement. *Gallicolumba crinigera* subspecies provenance remains unresolved and is not auto-reconciled. IUCN national and global statuses remain separate. None of these new facts asserts site occurrence or changes any historical field identification.

## Counts derived from Android assets
- **96** unchanged species identities, source taxon keys and main sections.
- **393** source-backed biological facts, previously 377.
- **269** distinct taxon–source links, previously 261.
- Missing five-topic evidence: Identification **18** (from 27); Habitat **51** (from 52); Distribution **24** (unchanged); Natural history **75** (from 78); Conservation **76** (from 77).
- The 18 remaining Identification gaps and all other missing sections remain empty rather than filled with assumptions. The 9 new identification accounts are supported by mixed original and secondary evidence. Neither diagnosis completeness nor independent scientific review is implied.
- Source files and older scientific source packs remain unchanged. Added claims are appended to the unified corpus with their `sourceType`, original URL, `sourceLocation`, `sourceYear` and `studyScope`. Bibliography links are deduplicated per species and URL.

## UI refinement
The shared Android species detail view now displays a small, honest summary, `X of 5 research topics documented`, before reference photographs, preserving the five icon-led cards, scientific name, source drawer and original photo credits. Source and collapsed research section buttons update their accessibility descriptions when opened and closed. This avoids adding duplicate primary controls or unsupported percentages. The 320dp screenshot suite asserts the documented-topic summary for every species.

## Regression plan and acceptance boundary
- Source integrity: `scripts/test_rc20_sourced_species_and_ui.py` checks all 16 added fact identities, matching per-species bibliography URLs, type labels, dated conservation scope, complete coverage ledger and UI/version text.
- Existing 96-account integrity and literature tests have been updated to the new exact fact/reference counts.
- CI must run Python source tests, JVM tests, signed APK assembly, version/signature verification and Android 14 emulator's existing 13-test suite with in-place SQLite upgrade and 96 screen captures before stating RC20 acceptance.
- A passing 96-screen first-viewport audit cannot prove bottom-of-page layout, talkback, 200% font, accessibility on every handset or field readiness.

## Future research
Source-check the 18 missing original identification accounts and the remaining habitat/range/ecology/conservation evidence categories. Prioritize direct original species descriptions, targeted field surveys and dated national/global assessments. Leave any unsupported evidence section absent rather than manufacturing a generic field guide entry.

## Confirmed signed RC20 and Android emulator acceptance

**Scientific Core QA:** Runs `40808196208` and `40808189815` completed successfully on latest documentation/source HEAD `b8c441b4...`. A second verified successful source run `40808121265` created artifact `12868578618` named `LIKAS-RC20-documented-species-debug`. The newer HEAD artifact `12869087632` contains **byte-for-byte identical app-debug.apk**. Extracted APK is **9,553,587 bytes**, SHA-256 `268fed26f364bc670a3245eafe630dec9c51805c4767a25545cd7a643e534629`; APK ZIP integrity passed. Actual binary AndroidManifest.xml has package `org.phwildlife.survey.beta08`, versionCode **73**, versionName `0.27.3-rc20-documented-species-preview`; APK signing block v2 certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` matches the pinned RC4 identity. Confirmed `rc20_verified_gap_batch_v1.json` and full 393-fact corpus inside the APK.

**Android emulator:** Workflow run `40807884582` completed **success** on code commit `94f4f639...` (same Android source and version as the signed artifact; later commits touched tests and documentation only), with evidence artifact `12868574937`. Extracted Android 14 instrumentation log states **OK (13 tests)**. `all-96-audit-status.txt` shows `ALL-96-RENDERED-SPECIES-AUDIT-PASS`. The CSV has exactly 96 taxon rows, 24 each for birds, amphibians, reptiles and mammals, with 96 distinct 480 × 960 JPEG captures. Every JPEG byte count matches its audit record. The screenshot content verifies the new `X of 5 research topics documented` summary renders on every tested account.

**Upgrade preservation:** `previous-db.sqlite` and `after-upgrade-db.sqlite` are **byte-identical**, each 1,613,824 bytes, SHA-256 `cd3aa2331a0727ec5e20085e49a91cb7d3d910d1d25d3ee3dff64feefd57e212`. The emulator script additionally checks the old installation's private media preservation fixture. This remains an emulator fixture, not a real field-phone backup-and-restore demonstration.

**Visual review:** Four 24-screen contact sheets were inspected for the first viewport: species titles, source-coverage line, photos, scientific cards, controls and 320dp wrapping. No obvious severe overlapping or cutoff of the primary heading/card structure was found in the visual overview. Some accounts display only one or two evidence cards, accurately reflecting missing information. The screenshots do not validate below-the-fold layout, expanded bibliography or all accessibility font sizes.

**Delivery state:** Direct signed RC20 preview APK and emulator QA archive copied into the 29 October 2026 conversation's downloadable file space. Other conversations must verify or obtain new file attachments; prior sandbox URLs are not portable. The APK is a **development preview**, with unverified real handset installation, user-data migration with representative production databases, long field-session stability, battery use and screen-reader tests. PR #38 remains draft. Continue remaining 18 identification gaps without inventing characters.
