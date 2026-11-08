# RC103 PHLibrary 2.0 native redesign: build acceptance and audit

**Reviewed:** 9 November 2026 Asia/Manila. **Source:** draft PR #108, `feature/apo-rc103-phlibrary-2-native-v163`, commit `8ba06f835aeb0365ef7e411a6fe64607a0434dea`. **CI:** [run 42509673962](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42509673962), successful. **Status:** Signed development preview, not merged, not installed on owner phone.

## Direct-chat APK handoff

- `LIKAS_RC103_PHLibrary_2_0_signed_code163.apk`; **12,468,370 bytes**, SHA-256 `0ab8f0583e5ad2ccaf9796eafb2862c33e51ae7308d3e1fbaddde0a58cb88bc2`. Signed GitHub Actions artifact ID **13628709605**, local extraction from `app-debug.apk`; outer/inner ZIP CRC verified.
- VersionName `0.27.3-rc103-phlibrary-2-native`; code **163**; package `org.phwildlife.survey.beta08`. CI verified signing identity. No schema migration: SQLite29 and original signed upgrade path retained.
- Actual emulator evidence artifact ID **13629151702**; signed RC92→RC103 data-preservation artifact ID **13628729649**. Both downloaded and inspected; AndroidRuntime error evidence 0 bytes, upgrade seed and post-install test each `OK (1 test)`. Tests cover 360×800 100% text and 320×720 200% text with source, reader, startup and native Home/Explorer/Profile screenshots. Real phone/TalkBack testing not performed.
- Full audit and screenshot contact-sheet are direct conversation artifacts, not GitHub-hosted binaries. The direct APK link must be verified again in future chat sessions.

## Implemented native experience

- Forest-illustrated native PHLibrary Home, compact search, four wildlife groups and links to habitat atlas, Discovery Rarity, original bibliography and saved species.
- Virtualized 715-account ListView explorer with name, family/order and verbatim-article text search, exact-name-first ranking, result context, wildlife group, Discovery Rarity and global IUCN filters, continuous results and saved-state/scroll recovery.
- Improved single-paragraph Book 01 v1.82 reader with independent Discovery Rarity and IUCN panels, selectable article text, font controls, taxonomic same-family links, references and persistent device-local saved species.
- Habitat browse with 15 source-informed profiles, and offline searchable bibliography of 1,159 entries.
- Original official APO artwork and survey/PHLibrary/source assets not modified. **All 130 APK asset paths and SHA-256 checksums matched the signed RC102 APK.** Decoded book is 715 (409 birds, 53 amphibian entries, 154 reptiles, 99 mammals), rarity totals 124/336/214/37/3 plus one Unranked. Two `Ceyx` entries separate; provisional `Oreophryne cf. nana` remains Unranked.

## Audit findings and decision boundary

1. **High: Citation resolution.** The canonical book has **2,760 attached short citation entries, zero individually populated full citation fields**. The separate 1,159-entry bibliography has citation text and some URLs but no verified author–date→bibliography mapping. Do not call article citations fully resolved.
2. **High: Habitat evidence coverage.** Existing profile pack has 15 definitions, but the reviewed edge graph has only **21 links, 15 named taxa, and 6 habitat IDs**. This is a source-backed partial index, not a complete 715-taxon habitat atlas. No link does not mean absence.
3. **Medium: Discovery semantics.** Search for places and habitat words is raw text matching, not verified local occurrences or habitat selection. Taxonomic historical aliases are not integrated into PHLibrary explorer queries.
4. **Medium: Long headings at 200% text.** Source and status remain readable but the South Philippine Dwarf-kingfisher name wraps awkwardly, pushing the natural-history narrative below the initial fold. The Home forest hero was made content-height-adaptive and verified to avoid earlier clipping.
5. **Medium: Bookmarks and performance.** Saves use scientific-name SharedPreferences rather than stable concept keys. Full-text matching runs on the UI thread each keystroke; low-end device latency and TalkBack focus traversal not measured.
6. **Release:** User may test the signed RC103 preview in-place over the existing app; **do not uninstall or clear storage**. Physical owner phone upgrade is not confirmed. PR #108 remains draft. No claim of broad field-readiness.

The owner's implementation authorization was for this native concept and review, not approval of taxonomic guesses, unlicensed wildlife photos, unsourced occupancy, migration of survey identification records, or automatic merge into main.
