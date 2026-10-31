# LIKAS Biodiversity Workspace: Home, Live Recorder, Species Library

**Date:** 28 October 2026 (Asia/Manila)
**Source workstream:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38
**Baseline:** v0.27.3 RC4, versionCode 49, SQLite schema 27
**Status:** Source implementation for review. No production APK or phone approval.
**Excluded:** `LikasLaunchActivity`, animated rainforest, startup artwork, app icon, unrelated survey storage.

## Owner's approved direction

The owner selected all three of the prior product critiques:
1. Home should feel like one biodiversity workspace, not a tools menu.
2. The Live Recorder should be faster and less crowded without removing scientific controls.
3. The Species Library should prioritize reference-backed identification and ecology without unsupported pseudo-probability scores.

## Home: workspace organization

**Visible sections**:
- **Fieldwork**: resume active survey or choose Birds, Herpetofauna, Mammals, Custom. Original resume/new-event logic preserved.
- **Workspace**: Projects & Stations, latest survey summary read-only from existing `surveys` records, Survey history, Dashboard. Project and past fieldwork are no longer hidden under More Tools.
- **Biodiversity Library**: one-step **Search Species Library** opens the actual `TaxonDetailActivity` name/synonym lookup; **Source catalogs & regional records** opens the catalog management/reference page. Clarifies that source listing does not prove station presence.
- **Field tools & settings**: optional disclosure for sound archive, camera/trap history, method evidence, study protocols, scientific data review and backup import.

**Intentionally not created:** An empty Habitat Library, fake Research Watch screen, tab navigation with unimplemented features, or additional persistent project preference. A latest-survey summary is explicitly labeled *recent*, not mislabeled *current project*.

## Live Recorder: protect high-frequency actions

- The existing session/GPS/effort HUD remains visible.
- **Mark now**, **Measure bearing**, and station progress remain accessible without an added interaction.
- Audio, trap/device and other method-specific evidence screens move behind **Audio, devices & evidence** disclosure when applicable. They retain the same navigation callbacks.
- An explicit **Record observation** section starts the species, individual count, confidence and measurement flow.
- Persistent **Save Observation**, **Review Records**, and **End Survey** controls are retained in the fixed footer, with unchanged Complete, Incomplete and Not Counted resolution.
- Underlying survey timers, GNSS service, data writes, session persistence, media references and SQLite schema are unchanged.

**Trade-off:** Audio/evidence capture now requires one disclosure tap for some methods. Test usability with ornithologists and acoustic fieldwork users before final acceptance. For workflows recording calls as a first-class event, a future method-aware quick-capture button is a reasonable revision.

## Species Library: evidence-first scientific presentation

- The library root is renamed **Species Library**, with primary **Search species & former names**, **Browse regional source records**, and source-version details.
- Catalog counts are computed from current installed source-attributed rows, not a hard-coded denominator. Incomplete source groups are marked; line breaks display normally.
- The reference view has separated **Identification and taxonomy**, **Conservation references**, **Habitat and elevation**, and **Regional source evidence**.
- No invented diagnostic characters were imported. A visible note states the absence of a reviewed diagnostic key and directs the user toward the record's cited literature.
- **Species-picker cards no longer display 60%/Likely or any other occurrence-like percentage**. They show geographic source scope, habitat-reference status and elevation-reference status separately.
- Deprecated Likely/Unlikely and score-based sort/filter controls are no longer presented. The visible ordering choices are **Name**, **Family**, and **Genus**. Internal historical heuristic scores are not recalibrated or used to claim station occurrence.
- Existing `source_listed` versus `unreviewed` separation, synonym lookup, uncertainty categories and references remain intact. Verbatim prior field observations are not rewritten.

## Source files changed in this implementation

- `WelcomeActivity.java` (Home only, not startup)
- `MainActivity.java` (presentation and grouping of evidence buttons)
- `SpeciesPickerActivity.java` (reference card display and visible sort controls)
- `HabitatLikelihood.java` (adds nonprobabilistic text status fields without changing existing scoring logic)
- `CatalogActivity.java` (library entry and runtime counts)
- `TaxonDetailActivity.java` (reference organization, diagnostics availability label)
- `scripts/test_v026_ui_audit.py` (updates legacy source assertions to new UI contracts)

## Acceptance boundary and future decisions

This is code-level UX implementation. Its scientific reference data are the same installed catalog, not a new identification database. No new photos, calls, diagnostic keys, current research feed or species distribution model were added.

Keep PR #38 draft pending source CI and later user review of installed screens. Original v0.27.3 RC4 remains the version offered for fieldwork. The owner's separate startup screen chat owns all animation and asset integration. A future merge should reconcile parallel branches without overwriting either workstream.

The owner previously deferred **formal test plans**. Automated checks and compilation in the existing development workflow are build verification, not a new requested field test plan.

## 28 October 2026 | Follow-on Q1/Q2/Q3, minimalist design and working diagnostic examples

The owner chose all three follow-up requests: refine the visual consistency of Home/Recorder/Species Library, speed method-specific observation capture, and start cited diagnostic profiles with a comparison feature.

**Shared design:** Added `FieldVisuals.section` for understated titles, optional notes and consistent spacing. Adopted on non-startup Home, Live Recorder, Species Library and reference detail sections. The startup activity, pixel frog launcher icon and forest asset workstream are untouched. This is source-coded style, not measured visual quality on a phone.

**Method-aware shortcut:** New pure Java `FieldQuickActions` chooses one additional *direct* capture action. Birds point-count and transect recordings surface **Record / review calls**. Amphibian calling surveys surface the same. Herpetofauna VES surfaces **Attach observation photo**. Mammal camera/live trap checks surface **Device checks & captures**, and track/sign and other protocol-based methods surface **Record method evidence**. The shortcut invokes the existing recording/photo/protocol screen. It does not create a new data pipeline, prepopulate a species name, start a microphone without user action, or alter survey timers. A method's quick action is omitted from the expandable evidence list to avoid two instances of the same control. Nonfunctional mammal bat acoustic capture is **not** advertised. The fixed Save, Review and End Survey controls remain visible.

**Diagnostic comparison:** Added `diagnostic_profiles_v1.json` bundled offline, `DiagnosticGuideActivity`, and a citation entry from Species Library and matching species account pages. The comparison displays character-by-character notes for two Philippine raptors and links each trait to its source. Philippine Eagle `Pithecophaga jefferyi` references DENR WildALERT and the University of Michigan Animal Diversity Web. Philippine Serpent-Eagle `Spilornis holospilus` references DENR NCR and ITIS. Sources accessed 28 October 2026 and recorded in the JSON. These are **source-checked introductory morphology cards**, **not independently expert-reviewed diagnostic keys**, identification automation, age/sex keys, local occurrence confirmations or new catalog taxon records. Bird images from third parties are deliberately not redistributed. Existing species, geography and survey tables remain unchanged.

**Implementation files:** `FieldVisuals.java`, `FieldQuickActions.java`, `DiagnosticGuideActivity.java`, `app/src/main/assets/diagnostic_profiles_v1.json`, non-startup Home/Recorder/Species Library/Taxon Detail classes, Android manifest, `FieldQuickActionsTest.java`, `scripts/test_diagnostic_profiles.py`, and revised source contracts.

**Release gate:** This remains an experimental draft on `feature/likas-core-fieldwork-post-rc4` (PR #38), preserving Android package `org.phwildlife.survey.beta08`, SQLite 27, `versionCode 49`. Build-unit CI is distinct from an installed-user experience. Do not present an APK as a released version without version advancement, signing continuity, data-retention testing and owner review. Formal field test plans remain deferred.
