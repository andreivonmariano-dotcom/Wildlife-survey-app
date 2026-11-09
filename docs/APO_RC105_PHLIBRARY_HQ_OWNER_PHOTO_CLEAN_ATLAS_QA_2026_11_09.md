# RC105 PHLibrary clean atlas and owner Mindanao photos | Signed QA

**Date:** 9 November 2026 (Asia/Manila). **Status:** Signed, emulator-tested **development preview**, not a production or field-certified release. **Source:** `feature/apo-rc105-phlibrary-clean-atlas-v165` exact HEAD `866c5562d084c7c8bc19ac1ee698b352ccbdfa34`, draft PR [#110](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/pull/110), based directly on signed RC104. No main app-code merge.

## Owner-approved screen changes

- **Home:** Four real user-provided Mindanao forest/landscape photographs (see `docs/APO_RC105_PHOTO_HERO_ASSET_MANIFEST_2026_11_09.md`), automatically cycled at ten-minute wall-clock boundaries with ~950 ms crossfade; no network dependency or manual import. Cleaner four-entry navigation to Species Atlas, Habitat Atlas, References and Saved species. Discovery Rarity removed as a Home menu action but remains a distinguishable educational species badge/filter. At 200% text, Back fits and Home search stacks full width.
- **Explorer:** Continuous native `ListView` virtualization preserved, no Show More. Compact group-chip row, Filters, Sort and result count. Filter sheet supports family, genus, source-article habitat **mentions**, Discovery Rarity, IUCN and saved-only; habitat word matches are not validated occurrence or habitat use. Button reset semantics and singular count corrected. At 200% status pills no longer strand the rarity icon on a separate line.
- **Species reader:** The full, exact Book 01 v1.82 narrative remains one selectable paragraph. Repeated Book 01 kicker removed, Save action moved near top, font controls kept. Icon-and-text Discovery Rarity versus named global IUCN badges have separate color palettes, with explanatory dialogs preventing conflation. National status remains separate, not inferred from IUCN.
- **Habitat Atlas:** Profiles grouped according to five source categories (Terrestrial, Edaphic, Aquatic, Coastal, Human-modified). Expandable original field indicators, water/substrate, vegetation and limitations. 21 existing provisional source links (15 taxa, six of 15 habitats) remain traceable via source title and URL; unlinked profiles are not treated as negative survey evidence. The `Find textual mentions` path remains distinct from verified associations. Habitat header count clutter removed.

## CI/build checks

**GitHub Actions run:** `42550418551`, conclusion **SUCCESS**, head SHA above. CI passed Python scientific and source tests (including `scripts/test_rc105_phlibrary_clean_atlas.py`), JVM tests, Android debug and instrumentation compilation, original signer and package/version checks, Android 14 emulator, and signed RC92 code152 to RC105 code165 **no-clear-data** upgrade.

**Android 14 native QA:** normal 360×800 at 100% font and narrow 320×720 at 200% font. The existing preserved source-reader/startup checks plus `Rc103PhLibrary2InstrumentedTest` passed **4/4 tests in each configuration**, covering photo decode, Back text fit, full-width Home search, species search/split kingfishers/provisional frog, no-wrap rarity label, source narrative statuses, and opening a source-linked Habitat Atlas detail. Ten PHLibrary screen PNGs were captured (Home, Explorer, Reader, Habitat Index, Habitat Detail × two scale configurations), plus four preserved startup screenshot states, total **14 native PNGs**. Evidence artifact ID `13645152209` (name `APO-RC105-PHLibrary-clean-atlas-emulator-evidence`). User-facing contact image `RC105_Final_PHLibrary_10_Screens.png` created in active conversation.

**Signed upgrade:** seeded RC92 app/test, installed RC105 over the same package without data clear and checked preserved rows/media/exports. Evidence ID `13645223103`; both pre/post instrumentation say `OK (1 test)` and `android_runtime_errors.txt` has **0 bytes**. This tests one signed upgrade path and does not replace a real-phone field-workflow test.

## Independent APK checks and exact handoff

- File: `LIKAS_RC105_PHLibrary_Clean_Atlas_Mindanao_Photos_signed_code165.apk`
- Version name: `0.27.3-rc105-phlibrary-clean-atlas`; version code **165**. App package: `org.phwildlife.survey.beta08`. SQLite schema **29** unchanged.
- Size **12,769,635 bytes**. APK SHA-256 **`1dfed7670598bda7209cac5de265e18564b211a46e5fd82557d352772016e139`**.
- Original signing certificate DER SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**. Independently extracted from APK V2 signing block, exact match to RC104 and CI signer check. Archive CRC validation passed.
- Verified byte-for-byte against signed RC104 for canonical assets: `phlibrary_book01_v182.json.gz.b64`, `phlibrary_book01_bibliography_v182.json.gz.b64`, `offline_habitat_profiles_v1.json`, `rc8_species_habitat_links_v1.json`, `paic714_master.csv`, `paic_worldcover2020_500m.txt`, `apo_official_master.png`. No user observations or historic survey identities rewritten.
- Signed new APK includes `assets/phlibrary_mindanao_forest_strip.webp.b64`. It decodes to **285,824-byte**, 1080×2432 WebP 4-frame strip, SHA-256 `b24352b109ae83d9baa41fda9a7363a8403f57a7bfe4ce41dbc1d981a208666f`, byte-identical to optimized source. All four original supplied 2048px JPEGs are preserved byte-identically in the separate `PHLibrary_RC105_Mindanao_Forest_Photos_HQ.zip` archive (3,147,006 bytes; SHA-256 `4985b013de366ddc5f54dde5fbed6dbe481d514aab32a4280393d57d34e1ee28`).

## Remaining limitations

The signed APK is a **development preview**, PR #110 remains open/draft/unmerged, and installation on the owner's real phone, TalkBack, low-end-memory performance, long-running 10-minute crossfades and public image-rights review are not verified. Native evidence covers screen rendering and sampled flows, not exhaustive functionality of every possible family/genus or habitat mention filter combination. The source bibliography still has unresolved per-article citation joins; no reviewed scientific associations were invented and the original 15-habitat/21-edge evidence boundary remains. Device-local bookmarks are still keyed by scientific names rather than durable concept IDs.

**Handoff:** The APK, contact-sheet PNG, emulator evidence ZIP, upgrade evidence ZIP and high-resolution four-photo archive were all materialized in the current conversation under `/mnt/data` and verified locally. Those sandbox links are session-dependent; later chats must reverify or redownload the exact artifacts. Do not present a GitHub Actions page as the primary download or claim the preview is stable/field certified.
