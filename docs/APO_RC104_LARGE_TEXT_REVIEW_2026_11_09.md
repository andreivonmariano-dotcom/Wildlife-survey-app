# RC104 | PHLibrary 2.0 large-text legibility follow-up

**Reviewed:** 9 November 2026, Asia/Manila.
**Code baseline:** RC103 draft PR #108, commit `8ba06f835aeb0365ef7e411a6fe64607a0434dea`.
**Follow-up branch:** `feature/apo-rc104-phlibrary-large-text-v164`, draft PR #109, based directly on RC103.
**Release status at initial record:** Source fix submitted; new signed APK and Android emulator acceptance not yet verified. RC103 code163 remains the last independently checksum-verified APK.

## Evidence and root causes

Downloaded and inspected RC103 Actions screenshot evidence artifact 13629151702 from passing run 42509673962. The normal 360×800/100% and 320×720/200% screenshots show the working offline PHLibrary Home, Explorer, and species reader. At 320×720 with 200% text:

- Explorer toolbar `Clear` button is visually truncated (appears as `Clea`). The native compact action has fixed 66dp width, 12sp label, and 6dp left/right padding, insufficient at the tested text scale.
- South Philippine Dwarf-kingfisher common-name heading visibly wraps inside `Dwarf` on the reader masthead. The large-font heading requested 20sp, doubling to ~40px at that scale in limited available width. The locked `Ceyx mindanensis` name and source account are not defective.
- The 320dp Home remains vertically scrollable; partly visible below-fold group cards and horizontally scrollable group chips should not be mistaken for missing content.

## Narrow source change

- `PhLibraryDirectoryActivity.java`: compact Clear button width increased to 82dp, retaining accessible text, label, and 48dp row height.
- `PhLibraryBook01.java`: font-scale >=1.6 reader common-name heading changed from 20sp to 16.5sp. At 200% this remains larger than the original normal-scale base, with less risk of breaks midword; article body and title content unchanged.
- `app/build.gradle`: versionCode 164, versionName `0.27.3-rc104-phlibrary-large-text`. Original package `org.phwildlife.survey.beta08`, signer configuration, and SQLite schema29 preserved.
- Inherited source/upgrade test expectations updated for code164. New `scripts/test_rc104_large_text.py` requires source invariants. Native `Rc103PhLibrary2InstrumentedTest` adds measured reset-label bounds and tests that the long common name does not break within a word when large text is configured.
- Added GitHub Actions `apo-rc104-phlibrary-large-text-v164-qa.yml` with source and JVM build, original signer/asset checks, Android14 100%/200% screenshots, and signed RC92→RC104 no-clear-data upgrade acceptance. Earlier RC103 visual assertions remain inherited.
- No changes to the canonical 715 book source, rarity v0.3.4, historical taxon IDs, habitats, survey records, reference assets, bibliography, original APO startup emblem, or offline artwork.

## Release gates and known research limits

Do **not** label RC104 shipped or hand off a newly named APK until the exact new workflow run completes successfully, its code164 signed APK is extracted into the active conversation and checked for ZIP integrity, package/version, signature continuity, checksum, and all CI evidence. Inspect the new 320dp/200% screenshots to verify that both reported visual defects are actually corrected. Real handset and TalkBack testing still remain distinct acceptance checks.

The pre-existing RC103 scientific limitations persist: the 715-account book has 2,760 unresolved article citation fragments, alongside 1,159 separately accessible original bibliography entries. The 15 habitat profiles have 21 linked edges for 15 taxa across 6 habitat IDs. Search of source prose is a textual mention, not site occupancy or habitat selection. Bookmarks currently use scientific names instead of stable taxon-concept IDs. A mobile readability fix does not resolve those evidence-model limitations.

**Governing requirements:** G-01 to G-06, G-10 to G-11, U-14, and no-data-loss CC-01/DL-01. Keep PR #109 draft for scientific and device review.

## Final signed RC104 acceptance (9 November 2026, Asia/Manila)

- **Source:** draft PR #109 branch `feature/apo-rc104-phlibrary-large-text-v164`, verified source HEAD `720b967003b30ac5bf70026526ea7fed57f11983`. Actions workflow run **42540392122 succeeded** after the owner-preserving version assertions were updated. The PR is still draft and no owner-phone installation was attempted.
- **APK:** the signed `app-debug.apk` was extracted from artifact **13640983498** into the current chat as `LIKAS_RC104_PHLibrary_Large_Text_signed_code164.apk`, **12,468,466 bytes**, SHA-256 **`bac771eea242ac42d40db6061d487a279d8cb4ef72fe256031a1a5e86ee45e09`**. Outer artifact ZIP and inner APK fully pass CRC inspection; APK signing block present. CI verifies Android package `org.phwildlife.survey.beta08`, `versionCode 164`, `versionName 0.27.3-rc104-phlibrary-large-text`, and the unchanged original signer certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- **Content verification:** directly compared all **130 APK asset paths** and SHA-256 byte hashes to the independently reverified signed RC103 APK. Exactly 130 match, with no added, removed or changed offline reference assets. Survey schema remains SQLite29; original 715 canonical v1.82 article dataset, rarity v0.3.4, atlas links, bibliography, startup art and field records unchanged.
- **Emulator QA:** Android14 normal 360×800/100% and compact 320×720/200% screen/reader and startup tests passed. Downloaded artifact **13640844274**, inspected new Home/Explorer/Profile screenshots visually. The compact Explorer `Clear`, `Sort` and `Filters` text fits, and the South Philippine Dwarf-kingfisher heading wraps at its hyphen rather than within `Dwarf`. New in-app instrumentation checks both the toolbar inner widths/heights and the reader name line breaks.
- **Real signed upgrade:** artifact **13641481465** contains `pre_upgrade_seed.txt`, `post_upgrade_validation.txt` and empty `android_runtime_errors.txt`. Both instrumentations returned `OK (1 test)`. Upgrade from original signed RC92 code152 to signed RC104 code164 retained seeded survey records, linked media and export on the emulator, with `adb install -r` and no data clear.
- **Release status:** directly delivered verified *development preview*, not stable release, no real-owner-phone upgrade or TalkBack/performance acceptance. Exact chat-local file paths cannot be assumed valid in a subsequent chat.
- **Scientific limitations:** no change to the unresolved 2,760 short-to-full article citation links, partial 21 edge/15 taxon/6 habitat source-link graph, text-mention search semantics or name-keyed bookmarks. Keep future scientific work separate from the narrow display fix.

The CI runs before the final screenshot follow-up failed only because source-level assertions expected prior versionCode163 or an old release-name regex. The final run passed the unchanged scientific/survey checks with their release metadata expectations updated. No data migration, taxonomy conversion, or historical-record rewrite was introduced.
