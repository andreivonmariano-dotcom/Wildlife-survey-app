# RC107 PHLibrary Habitat Atlas and Saved Species | Signed QA acceptance

**Date:** 9 November 2026. **Branch:** `feature/apo-rc107-phlibrary-habitat-reader-saved-v167`; draft PR #112, based on signed RC106. **Exact tested source HEAD:** `4bd0643b80358541f3f4c3500a6ad92d7cdbef02`. **Version:** `0.27.3-rc107-habitat-reader-saved`, code **167**, package `org.phwildlife.survey.beta08`, SQLite29 unchanged.

## Accepted user-facing behavior

Each of the 15 original Habitat Atlas categories now opens one continuous selectable habitat paragraph rather than multiple expanding sections and a literature-linked species list. The separate editorial reader asset `phlibrary_habitat_reader_v1.json` contains all 15 paragraphs; the original `offline_habitat_profiles_v1.json` and `rc8_species_habitat_links_v1.json` remain untouched. The habitat index has no repeated header. Each profile has `Browse species`, which opens Species Atlas with its relevant educational **Habitat** filter active; results display a short selected habitat label. The filter uses the same 15 habitat IDs, synonyms and broader educational suggestions for rarely named forest types. The filter name is **Habitat**, editorial tier is **Rarity**, and the source species narratives are not rewritten.

Saved Species is an explicit saved-collection context. Its `Clear` opens a confirmation with `Cancel` / `Clear saved`, durably deletes bookmarks, and stays on the empty Saved screen. General Explorer Clear continues to reset temporary search/filter/sort controls. Saved-only reset never turns into the full 715-species atlas. The native test verifies before/after saved counts and screen text.

## Verified tests and signed artifact

**CI run `42567233830`: SUCCESS.** All scientific/source regression gates, JVM tests, Android compilation, original signer/identity checks, Android 14 native instrumented tests on 360×800 @100% and 320×720 @200%, and signed RC92 code152 → RC107 code167 no-clear-data upgrade completed successfully. Each native preset passed `Rc103PhLibrary2InstrumentedTest` **7/7**, including habitat list/detail, Habitat-filtered species, Saved Species confirmation clearing, Home, species reader and bibliography. Other preserved source/startup instrumentation also passed. Evidence artifact: `13652625150`; 24 screenshot PNGs in ZIP (20 PHLibrary interface views plus 4 startup-motion views). Signed upgrade artifact: `13652560457`; pre and post instrumentation each `OK (1 test)`, zero AndroidRuntime error bytes. Signed APK artifact: `13652276706`.

**Exact signed APK:** `LIKAS_RC107_PHLibrary_Habitat_Saved_signed_code167.apk`, **12,776,228 bytes**, SHA256 **`639a3677bf46d98d9aa1115a25ce296ab83fa0b2503d4ce28c4825bcb7ed441c`**. ZIP CRCs verified locally, APK V2 signer DER SHA256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**, exact original cert. Independently compared all APK assets to signed RC106: **131/131 original asset files are byte-identical**, including the canonical 715 account pack, 1,159 bibliography, 15 original habitat definitions, 21 legacy source links, all approved photographs and survey references. Exactly one asset added: `assets/phlibrary_habitat_reader_v1.json`.

The final native 20-view review is saved in the current conversation as `RC107_Final_PHLibrary_20_Screens.png`. It includes Home, Habitat index/detail, Habitat-filtered species, Saved before/after, general Explorer, reader and bibliography/candidates at both font scales. The 200% Habitat paragraph requires vertical scrolling, as expected. The saved collection clears without redirecting. The screen-to-screen audit is separately preserved as `PHLibrary_RC107_Habitat_Saved_Route_Audit.md` in the current conversation.

## Follow-on design priorities

1. Add fast search in the Habitat index and only necessary category shortcuts, keeping its clean list.
2. Rank Habitat-related species by relevance of narrative terms and ecological context rather than source order, as an educational discovery aid.
3. Add a direct unsave control within Saved Species result cards, with reversible one-by-one removal.
4. Add verified, owner-approved representative habitat photographs only where correct habitat identity and image rights are established. Keep the single flowing article.

**Limits:** This is a signed development preview in an unmerged draft PR, not field-certified stable release. The owner phone, TalkBack, low-memory device behavior, every combination of filters and long-duration 10-minute photo transitions are not independently verified. Educational habitat suggestions can include plausible associations but are not observations inserted into the user's field surveys; original source scientific data are unchanged. Bibliography citation crosswalk and durable taxon-concept bookmark IDs remain uncompleted.
