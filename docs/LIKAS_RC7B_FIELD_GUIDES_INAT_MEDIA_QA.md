# LIKAS RC7B | Philippine literature field guides, iNaturalist photos and UI

**Date:** 28 October 2026  
**Branch:** feature/likas-core-fieldwork-post-rc4, draft PR #38  
**Build:** 0.27.3-rc7b-literature-inat-preview, versionCode 53  
**Package / database:** org.phwildlife.survey.beta08, SQLite 27 unchanged  
**Release:** private noncommercial scientific testing, not a public app-store release

## Taxonomic and ecological evidence

Existing embedded WBCP 2025 birds (761), Sy amphibians 2025-1 (116), Sy reptiles 2026-1 (380 source entries, one unresolved genus), and MDD v2.5 mammal candidates (246) remain separate from regional station evidence. Preserve original source keys and historic survey names. No reclassification of historical observation or status edits.

RC7A had 48 priority names, 14 with cited introductory accounts and 34 with source identity only. RC7B contributes 34 supplementary species-specific literature notes in:
- app/src/main/assets/rc7_literature_birds_amphibians_v1.json (19).
- app/src/main/assets/rc7_literature_reptiles_mammals_v1.json (15).

Each supplemental note contains original literature URL, publication name, several evidence-specific descriptions, identification/locality caution and specialist-review-pending status. These are source-derived descriptive summaries, **not** 34 independently validated identification keys. Species name, elevation or habitat evidence does not imply occurrence at the user's survey site. Historical statuses and current DENR assessments remain separate.

Android class Rc7LiteratureNotes.java displays the citations and cautions in the existing read-only TaxonDetailActivity. These accounts supplement 17 existing source-checked guides; no duplicate false 'reviewed' claim.

## iNaturalist media licensing, attribution and selection

Preferred user https://www.inaturalist.org/people/dreidrei was examined using the official iNaturalist API. Sampled photos were marked **all rights reserved**, photographer attribution from the source. Their attribution alone is not an open redistribution license. Do not package these photos without a rights grant or a compatible CC change.

From other iNaturalist contributors, 75 Creative Commons media candidates covering 29 first-wave draft taxa were found. The contact-sheet review retained **23** from **21 contributor accounts**, representing **21 CC BY-NC and two CC BY photographs**. Six rejected candidate frames were unusable for clear visual reference. Photograph attribution, individual photo ID, observer ID, observation URL, origin photo page URL, license URL, source JPEG URL, approval date, and file SHA-256 were retained. Human visual acceptance checks framing and utility but does not certify expert identification.

The immutable, compressed approval lock is docs/RC7_INAT_APPROVED_LOCK_V1.json.zlib.b64. Its decompressed compact-JSON SHA-256 is pinned in scripts/audit_inaturalist_photos.py; edits not matched by that hash stop the install. The build downloads only the 23 exact approved JPEGs, SHA-256 checks every file, verifies photo license and attribution, then writes offline_inaturalist_manifest_v1.json. The photo manifest is generated during CI; its repository template is empty. The CI will fail rather than ship 22/23 images.

The installed photo-source line must credit each individual contributor and show the photo's CC license and original source. Missing or all-rights-reserved media is never silently used. The 10 prior licensed Commons reference images are retained independently, subject to the existing online retrieval behavior and build inspection.

## Species Library UX

- Main offline library now renders compact name cards with licensed, downsampled thumbnails where available. Each card opens the exact read-only source taxon record.
- The wildlife-group and content filters stack on phone widths below 385dp, reducing clipping and crowding.
- Content filters: all source names, RC7A priority 48, literature notes 34, installed photo.
- All source names, synonyms, regional published evidence, accepted classification, source-dated IUCN/Philippine status, and comparative profiles remain available offline.
- Real field observation, GPS, End Survey lifecycle and camera workflows unchanged.

## Automated and device QA

- scripts/test_rc7b_field_guides.py verifies 34/34 exact draft species match, source links, field descriptions and separate specialist-review state; 23 locked photos, per-media CC license and attribution; UI filters and original SQLite 27 identity.
- scripts/test_rc7a_library.py now checks the approved compressed ledger and 23 source-card photo states, instead of assuming the manifest contains no images.
- Exact 23 photo count, photographer attribution and JPEG SHA-256 are mandatory in CI before Gradle assembly.
- Trusted RC4 PR lineage gate protects original package and signing continuity.
- **Not verified:** actual handset upgrade from RC6 to RC7B retaining real surveys and acoustic/media files; on-device 320dp and text scaling behavior; full backup ZIP-to-new-workspace restore; long field session, memory/performance, and specialist review for new biological assertions.

## Next release requirements

1. Perform RC6→RC7B signed over-install on sacrificial backed-up handset and compare original surveys, station IDs, photos, WAVs and zero-detection efforts.
2. Check real screen size, keyboard overlays, TalkBack focus, image decode memory, portrait scrolling, proper photographer credits and license links without internet.
3. Confirm the copyright holder's direct permission or new CC setting before adding images currently marked all rights reserved, including photos on dreidrei.
4. Keep PR #38 draft until those tests pass and scientific taxa are reviewed by an appropriate specialist.
