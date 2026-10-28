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
