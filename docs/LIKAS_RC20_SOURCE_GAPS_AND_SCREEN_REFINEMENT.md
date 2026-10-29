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
