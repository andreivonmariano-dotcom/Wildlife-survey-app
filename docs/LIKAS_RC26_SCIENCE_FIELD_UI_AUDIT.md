# LIKAS RC26: Philippine primary trophic evidence, field-screen readability, phone acceptance boundary

Date: 29 October 2026, Asia/Manila. Branch `feature/likas-core-fieldwork-post-rc4` (draft PR #38).

## Q1. Evidence extension

Four new individually attributed records from two primary peer-reviewed sources have been added to existing species accounts, not the taxonomic checklist:

1. **Limnonectes magnus**, two Natural history records from Plaza, Metillo and Sanguila (2021), *Journal of Tropical Ecology* 37, DOI https://doi.org/10.1017/S0266467421000158. This northeast Mindanao stream study used stomach contents and stable-isotope analysis in seven sympatric frog species. Its abstract supports an opportunistic generalist insect-feeding interpretation for the locally sampled L. magnus, not a fixed nationwide prey diet. DOI and institutional listing verified on 29 Oct 2026.
2. **Baletemys kampalili**, one detailed Habitat and one Natural history record from Rowsey et al. (2022), *Journal of Mammalogy* 103:1259–1277, DOI https://doi.org/10.1093/jmammal/gyac057. Original Mt. Kampalili paratypes were caught under and near moss-covered fallen logs. Two of three original specimens were captured with earthworm bait. The bait is not confirmed stomach contents or an observed feeding event. Exact sensitive capture coordinates are not repeated in the app. This does not establish current occupancy, habitat preference, or population size.

The signed bibliography retains prior citations. A single new L. magnus bibliography link is added. Total corpus: 96 priority species, 452 fact records and 303 per-species publication links. Missing: Identification 1, Habitat 40, Distribution 23, Natural history 65, Conservation 69. Dicaeum kampalili retains unfilled Identification from uninspected 1952 original text.

## Q2. Screen-by-screen source findings and fixes

- **Home:** Existing 200% UI contained horizontal Survey history/Dashboard shortcuts with fixed 52dp containers, despite shared button minimum height scaling to 96dp. RC26 gives each action actual measured minimum height, stacks shortcuts vertically at large font, and lets the brand wordmark size naturally. All existing Home and data-management destinations remain.
- **Survey Setup:** Persistent Continue to GPS used fixed 56dp height at 200%, conflicting with the same 96dp minimum action sizing. RC26 gives the pinned footer its full measured action height, retains a full spoken GPS-preflight action, stacks project picker/Manage controls vertically at large font, and increases advanced template controls. No automatic survey start or GPS fabrication.
- **Survey History:** Search/outcome filters previously used short, fixed inputs without full spoken descriptions. RC26 supplies a compact large-font title, 72dp search/outcome fields, and explicit accessibility labels. Survey outcomes and read-only data queries remain unchanged; history detail action buttons receive action-height scaling at large text.
- **Habitat regression:** RC25 initial emulator ran 15 tests, **14 passed, one failed**, asserting that the My records tab was clipped at 200% on a 320dp device. The root cause was setting a 58dp tab container around a button with a 96dp internal minimum. RC26 sizes both tabs and the fixed research disclosure to `FieldLayoutPolicy.actionHeightDp(fontScale)`. The new emulator must independently prove all controls uncut.

## Q3. Field workflow quality gate

Added an Android 200%-font Home, Setup and History instrumentation test. It captures `home-200percent-font.png`, `survey-setup-200percent-font.png`, `history-200percent-font.png`. With the existing RC25 Habitat/Species, RC24C Compare/Conservation, recorder and 96-taxon tests, the disposable 320dp Android 14 suite now requires **16 tests**, retained prior SQLite bytes and labeled media fixtures.

Physical field handset access is not connected to this development session. The separate `docs/LIKAS_RC26_PHONE_FIELD_QA.md` describes pending **real-device** checks for End Survey Complete, Incomplete, Not Counted; effort-only zero-detection records; GNSS/altitude provenance; photo/WAV file linkage; research exports; in-place backup and restore; power, long sessions and TalkBack. No real-device claims are made from emulator evidence.

## Release boundaries

Candidate code82 `0.27.3-rc26-fieldwork-readability-preview`, package `org.phwildlife.survey.beta08`, SQLite27, pinned Android v2 signing certificate. No startup forest/icon changes, taxonomy key rewrites, survey/migration, completed archive, GPS readings, or media storage edits. Build/source QA, emulator checks, signer inspection, actual APK attachment and real-phone tests must be separately reported. The draft PR remains unmerged.

## RC26B correction following failed 16-test Android emulator run

The first RC26 emulator run 40824440899 finished with **14/16 passed**. The Habitat My records button remained partly clipped at 200% despite a 96dp tab container. In RC26B the tab children use natural measured height, and the explanatory note and research disclosure enter the scroll area to protect the permanent tabs. A second test failure measured Continue to GPS before a layout frame following Home→Setup navigation. The instrumentation is split into separate activity callbacks with waitForIdleSync between them. This does not remove the GPS action or reduce its height.

Code83 RC26B is a separate preview requiring a fresh Android run and its own signed debug APK. All 452 facts, 303 links, package identity, SQLite27 and existing survey records remain unchanged. Do not claim first-run emulator acceptance.
