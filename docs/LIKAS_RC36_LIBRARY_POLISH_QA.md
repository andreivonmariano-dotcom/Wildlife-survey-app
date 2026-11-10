# LIKAS RC36 | Offline library polish and crash prevention

**Date:** 29 October 2026, Asia/Manila. **Baseline:** RC35 signed crash fix, applicationId `org.phwildlife.survey.beta08`, SQLite 27, pinned signer. RC36 versionCode **96**, versionName `0.27.3-rc36-library-polish-preview`.

## Purpose

The owner asked to continue improving the current LIKAS design. Following the null-status crash reported in RC34 and patched in RC35, this pass focuses on the **current horizontal Species / Habitats library**, not expanded scientific status coverage or another taxonomy update. It retains the U-10 three-space portal (Fieldwork, Library, Settings) and U-11 no-priority-tier policy.

## Implemented

1. **Measured swipe rails.** Every species or habitat shelf has its own card position counter and large, accessible Previous/Next actions. Buttons use smooth native horizontal scroll. Direct swipes still work. The controls show `1 / 24`, etc., from actual loaded accounts, not inferred rarity.
2. **Responsive cards.** Species and habitat cards use viewport-aware widths, ample text space, and taller minimum frames for large-font users. None of these controls rewrites records or species metadata. The full searchable species catalog and 15 source-linked habitat details remain accessible.
3. **Licensed habitat imagery.** Karst, mangrove and mossy-forest habitat cards use existing approved, offline Commons photos. Other habitats use original vector symbols, not fabricated ecology images. Full photo license and photographer credit remain in the habitat detail.
4. **Memory-conscious photo display.** Tiny preview bitmaps use a bounded 4 MiB cache and RGB_565 for card thumbnails, not full original-resolution image decode on each repeated card render. All source and license manifests are unchanged.
5. **Crash-safe cards.** RC35's null status guard remains. Species with no individually reviewed dated IUCN or DENR assessment display a neutral edge and `Status unverified`, not a manufactured category. The underlying six checked dated status entries remain unchanged.
6. **Navigation continuity.** Species/Habitats tab selection is saved across Activity recreation. Existing library and detailed activity routes are preserved. Optional fades respect Less motion.
7. **Regression coverage.** `scripts/test_rc36_library_polish.py` checks card counters, photo permissions, status neutrality, app ID and SQLite version. New Android test `Rc36LibrarySwipeInstrumentedTest` exercises opening both decks, stepping the bird carousel, and recognizing real habitat photos. The previous RC35 null-status Android test stays in place. `.github/workflows/library-android-smoke.yml` adds an Android 14 emulator test job specifically for these tests; its run status must be verified separately from compile success.

## Boundaries and acceptance

- No IUCN category or DENR law was reclassified. Only six of 96 old research accounts have individually reviewed dated display categories, not six Philippine species in total. No local rarity badge is assigned.
- Original 456-fact RC18 corpus and 8+26+15 reviewed overlays, 15 habitat descriptions, approved photograph files, taxonomic source IDs, signing key, SQLite27, End Survey controls and user survey records remain unchanged.
- New UI can be judged functional only after **source tests, assembled debug APK, signer/package verification** and ideally real Android emulator launch. Compilation of an Android test alone is not evidence it ran. Inspect 320dp phone and 200% font screenshots, TalkBack and actual handset upgrade independently.
- This pass does not redesign all field forms, habitat infographics or the 1,500-name search catalog. It refines the library rails and maintains scientific source separation.

**Governing requirements:** G-01, G-02, G-03, G-05, L-02, L-04, H-01, H-02, U-07, U-09, U-10, U-11, DL-01, CC-01.
