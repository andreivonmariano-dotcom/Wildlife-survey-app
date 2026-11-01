# LIKAS RC37 | Species chapter navigation and library rail continuity

**Date:** 29 October 2026 (Asia/Manila)
**Branch:** `feature/likas-core-fieldwork-post-rc4`
**Preview:** `0.27.3-rc37-library-navigation-preview`, versionCode 97. Existing application ID `org.phwildlife.survey.beta08`, signing configuration and SQLite schema 27 remain unchanged.

## Implementation scope

- In the cited 96-species research accounts, a horizontal "Jump to section" row links to the actual, populated Identification, Habitat, Range, Natural history and Conservation sections. Missing sections receive no shortcut.
- The links navigate within the existing account's ScrollView. Original facts, photographs, citations, source details and reference drawer remain intact.
- Species and habitat card rails retain individual scroll offsets after tab switching and Activity recreation. Shelf keys include media type and group so positions do not conflict.
- Previous and Next card controls immediately scroll under Less motion, system-disabled animations or enlarged text. Gestures remain supported.
- This pass changes no survey/database code, taxonomic records, wildlife images, legal conservation records, status assessments or rarity classifications.

## Verification gates

1. Run the full `scripts/test_*.py` suite and `:app:testDebugUnitTest :app:assembleDebug` with existing pinned signer.
2. Inspect Android 14 library smoke, then test 320dp and 200% font display plus TalkBack section labels.
3. Test both rail position restoration after rotating and switching tabs, profile chapter jumps, no-data sections, and large-font card controls.
4. Install over RC36 on a handset retaining survey records, then verify backup/export and historic records.
5. Compare versionCode, app package, signing certificate and APK file checksum. Do not call the preview field verified until handset checks pass.

**Affected requirements:** G-02, G-03, G-07, G-11, L-02, L-04, U-07, U-09, U-10, U-11, DL-01, CC-01.
