# APO RC96 | PHLibrary Book 01 one-paragraph reader acceptance

**Date:** 8 November 2026 (Asia/Manila)  
**Status:** Signed Android development preview, **not** physical-field accepted.  
**Owner direction:** Match Book 01 v1.75 with one continuous paragraph per species, visually small sources at the bottom, no separate expanded identification/habitat/conservation sections.

## Correct baseline
- User confirmed **APO RC95**, branch `feature/apo-rc95-biotools-survey-navigation-v155`, versionCode **155**, SQLite **29**, original signing key.
- RC96 implementation is isolated on `feature/apo-rc96-phlibrary-paragraph-reader-v156` (draft PR **#101**, based directly on RC95). Last tested app-source commit: `2ce141194b85ddd4f8ac4e515dfaf5301116628a`. The historical RC59-based PR #100 was closed unmerged.
- APK app ID remains `org.phwildlife.survey.beta08`, code **156**, versionName `0.27.3-rc96-phlibrary-reader`. Database schema remains **29**.

## Implemented
- 8 offline JSON shards copy 714 full species paragraphs and supporting short author-year source labels from the owner-supplied **PHLibrary Book 01 Greater Mindanao v1.75**. Distinct group counts: Birds 408, Amphibians 53, Reptiles 154, Mammals 99.
- Discovery Rarity v0.3.1 badges: Common 117, Uncommon 343, Rare 214, Exceptionally rare 37, Legendary 3; not a field-detection probability or conservation assessment.
- Existing PHLibrary hub now offers `Book 01 · Greater Mindanao`, with offline name/family/order search and group/rarity filtering. Existing RC95 PAIC714 source checklist and research tool destinations remain.
- Matching current-catalog taxa open a streamlined detail page with common/scientific names, family/order, origin/IUCN/rarity badges, compact approved offline photograph and photographer/license line, **one continuous original narrative paragraph**, and a small, tappable author-year reference strip at the bottom. Full photo caption and rights appear on tapping the photo credit. Previous/next traverse the book's taxonomic order.
- Retired historic taxa and non-Book01 species retain their existing taxon-reference interface. The only scientific-name mismatch against `paic714_master.csv`, PAIC `Chaerephon plicata` versus Book 01 `Mops plicatus`, is linked as a named synonym for display, without modifying either source's original name.
- Only presentation, bundled read-only book data, and associated tests changed. Original survey records, survey lifecycle, GPS/altitude, signed APK identity, approved art hashes, source PAIC, WorldCover, backup/export logic and schema were not edited.

## Completed QA and exact handoff
- GitHub Actions run **42397732027** completed **success**, including **480 Python tests**, JVM tests, Android application/instrumentation compilation, and source/photo/checksum verification.
- On Android 14 emulator, passed the inherited PHLibrary, BioTools, process-restart and data/archive test groups as well as Book 01 reader tests at **360dp/100%** and **320dp/200%** font. The latter asserted full 714 data availability, intact single paragraph, presence of rarity, source list, and native scrolling.
- Real Android screenshot capture produced six image files, covering directory, profile top and profile bottom at each of the two scales. We manually inspected a combined contact sheet. Final hierarchy displays PHLibrary header, species name/badges before optional image, readable account text, and references at the bottom. Enlarged-text layouts naturally scroll, rather than compress or truncate scientific text.
- A genuine signed **RC92 code152 → RC96 code156** Android emulator upgrade using `adb install -r` passed seeded synthetic survey/observation, media-byte and research ZIP verification without data clearing. This is **not** a test on the owner's physical device/database.
- Signed APK from CI artifact **13583349956** was retrieved in the conversation and checked as exact file `APO-v0.27.3-RC96-PHLibrary-Reader-code156.apk`. Size **12,014,759 bytes**, SHA-256 `bb507824a25e7a213e2116870e7c6449a18add6418f67ff38d35f2f2ce7c8adf`. APK ZIP CRC passed, 714 Book01 accounts and all 5 tiers independently read from the APK, while core PAIC and approved APO art present. CI independently confirmed original signing certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- Other archived signed APKs, RC95 source and startup artwork were not overwritten. **No PR merge or production release was performed**.

## Remaining limitations
- Book1's in-paragraph author-year source mentions are copied intact and presented again in the small reference strip. Some authors' full bibliographic details could not be uniquely matched from the manuscript during extraction. Conservative matching yielded a full bibliography string for **183 of 2,757 reference instances**; the remaining buttons display the original short source label, not a fabricated bibliographic entry. Completing robust individual source links requires a separate bibliography-indexing pass.
- This preview reuses the originally approved nine Commons JPEGs with exact SHA-256 restoration from the previously signed RC95 APK artifact. CI's temporary artifact expires around **10 November 2026 UTC**. Establish a durable, rights-compliant asset source/cache before repeat builds after expiry. Never accept changed Commons thumbnail bytes just because a file URL still resolves.
- Physical handset testing, TalkBack, outdoor sunlight, a real owner's historical database upgrade and extensive long-session field validation remain outstanding. The UI and data acceptances above are real emulator and source checks, not proof of all field use.

## Handoff
- Keep draft PR #101 based on current APO RC95, not historical LIKAS main/RC59.
- Use the verified in-chat direct APK attachment rather than an Actions page for download. A conversation-local sandbox path must be reverified in any new chat.
