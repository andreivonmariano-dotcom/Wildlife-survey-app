# APO RC100: natural-history integration and scientific QA

**Date:** 8 November 2026. **Candidate:** reader v1.80, Android versionCode160 (`0.27.3-rc100-integrated-habitats`). **Status:** draft PR #105, pending a complete successful signed Android CI run.

## Editorial implementation

- Started from the 714 species records in immutable Book 01 v1.75 and reversible source overlays v1.76, v1.78, v1.77 and v1.79. The full companion manuscript is `docs/APO_RC100_INTEGRATED_HABITAT_ACCOUNTS_2026_11_08.md`; a 59-page Word working manuscript was generated for direct in-chat review and visually inspected across all pages.
- Exactly 100 species were individually reviewed in the previous pass: 25 birds, 25 amphibians, 25 reptiles, 25 mammals. A new exact-text overlay `app/src/main/assets/phlibrary_habitat_integration_v180.json` consolidates 47 previously inserted cue sentences into connected ecological prose. There are 48 exact replacements because the Gonyosoma account requires two steps to preserve locality-specific records and position the habitat discussion with its tree-dwelling behavior.
- The eight previous generic habitat replacements remain, with their original text and scientific uncertainty preserved. The 45 review notes already redundant with source-backed accounts remain undisplayed. The other 614 baseline-only species have unchanged reader content and an explicit research backlog.
- Species identification, conservation categories, geographic localities, original references, all 714 taxonomy keys, Discovery Rarity v0.3.1, package ID, signing key, SQLite schema 29, UI art and survey data are untouched.
- Field search descriptions remain explicitly inferential where sources do not document a particular microhabitat. Luzon GPS evidence for Acerodon jubatus remains scoped to Luzon. Locality citations in the Gonyosoma oxycephalum account remain attached to the original island records.

## Content and document checks

- Reconstructed 714 resulting paragraphs from all versioned JSON layers and checked exact anchors and retention of all existing short citations. **Zero** unmatched anchors, **zero** short-citation losses and **zero** repeated `rather than` rhetorical phrases in the reconstructed text.
- Rechecked taxon counts: 408 birds, 53 amphibians, 154 reptiles, 99 mammals. Historical Discovery Rarity distribution remains 117 Common, 343 Uncommon, 214 Rare, 37 Exceptionally rare, 3 Legendary.
- Generated `APO_PHLibrary_Seamless_Integrated_Habitat_Accounts_RC100_Working_Draft.docx` in the current conversation, **59 pages**, SHA-256 `4665da97826c3dabfa8079795f3cd873ff0a198cb1c29a4f0a48c100a5cd3d8e`. Rendered all 59 pages and reviewed five page grids and a full-sized reader page. No obvious clipping, blanks or overlaps in reviewed renders. The DOCX link is conversation-local, and other chats should use the committed Markdown manuscript.
- Bibliographic limitations from the prior manuscript remain: 2,572 of 2,765 recorded citation entries have only a short citation without a complete bibliographic record. The text is editorial integration of existing source packs, not a fresh systematic review of each source.

## Android and release gates

- Source safety test: `scripts/test_rc100_integrated_habitat_reader.py`; Android instrumented test: `Rc100PhLibraryIntegratedHabitatInstrumentedTest.java`; emulator script: `scripts/rc100_phlibrary_integration_emulator_qa.sh`.
- The CI workflow `.github/workflows/apo-rc100-phlibrary-integrated-v160-qa.yml` requires source regressions, Android JVM/build, signature+catalog checks, Android14 reader navigation and font-scale tests, and a signed synthetic no-clear RC92 code152 → RC100 code160 upgrade.
- Initial CI attempts failed due outdated versionCode159 assertions in inherited tests and one new test substring mismatch; these have been corrected. Do not claim a signed APK or all-green CI until a current-run artifact is independently retrieved and verified.
- The inherited `rc4-source-gate` runs against historical main/SQLite27 contracts and remains an acknowledged separate failure. Draft PR #105 must not be merged or treated as a field-verified release without an explicit baseline policy resolution, source review and actual device validation.
