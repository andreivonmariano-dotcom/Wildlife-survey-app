# LIKAS app audit and visual clarity pass
**Repository assessed:** `andreivonmariano-dotcom/Wildlife-survey-app`  
**Baseline:** main @ `44825d70956151839bd8d90b14ef799c5ce8ea76` (2026-10-27)  
**Scope:** native Android source and documentation review. No physical handset or Android emulator verification is claimed.

## Principles for LIKAS
1. Scientific records are primary. Never remove or reinterpret survey evidence merely to simplify a screen.
2. Field recording must work offline and with wet hands, fatigue, and intermittent GPS. Primary actions remain easy to find.
3. Show the immediate decision first. Put provenance, tutorials, and rare imports behind clear disclosure controls.
4. Use one set of readable typography, spacing, forest-green action colors, and feedback states.
5. Keep **Complete**, **Incomplete**, and **Not Counted** semantically separate. Every survey exit must preserve its archive and status rules.
6. Avoid describing a checklist, GPS fix, taxonomic range, track estimate, or automated QA output as independently verified.
7. Prefer reversible changes, accessible touch targets, and clear in-app terminology.

## Findings addressed in this branch
- **Home information overload (high usability):** `WelcomeActivity.home()` stacked full-width tiles for every survey and management entry, plus recent items. It is now grouped into compact survey/workspace tiles, a small recent section, and an explicit additional-tools disclosure.
- **Survey ending obscured (high field risk):** `MainActivity.draw()` placed the survey end/start button inside the vertical observation scroller. It is now a persistent action above that scroller, labeled **End survey** once started. Existing finish-confirmation choices and safeguards are retained.
- **Unnecessary method widget (low severity):** the in-survey `methodSpinner` was built, populated, disabled, and never attached to the layout. It was removed. The selected method stays visible as text.
- **Explanatory copy inside the observation form (low severity):** an inert footer tip was removed. Recording behavior is unchanged.
- **Taxonomy screen overflow (high usability):** `CatalogActivity.refresh()` previously inserted a long technical guide into the summary, with no outer `ScrollView`. The screen now scrolls and presents a short catalog status with an expandable attribution/CSV guide.
- **Button feedback inconsistency (medium usability):** shared `UiKit.button()` now uses a bounded ripple for touch feedback while keeping color-coded button meaning.
- **Researcher-facing text artifacts (medium usability):** major recording section labels now use sentence case with clearer padding and spacing.

## Unresolved findings

### P0: scientific correctness and safety of recorded data
- **Coverage:** README states that built-in taxonomic names are an incomplete, unreviewed starter. Philippine amphibian, reptile, and bird workbook contents are not fully bundled. Do not present the catalog as complete or verified.
- **Elevations:** `MainActivity.onLocationChanged` shows horizontal GPS accuracy and latitude/longitude, but the observation schema does not hold GPS altitude, vertical accuracy, time, or vertical datum. Treat automatic elevation as separate schema, capture, export, and QA work.
- **QA scope:** `DataAudit` reviews many structural conditions but cannot establish taxonomic identification, ecological effort validity, or handheld GPS accuracy. Retain field QA protocols.
- **Archive migrations:** any new SQLite schema must migrate older installations without dropping survey, observation, photograph, or route data. Include a backup/upgrade test.

### P1: maintainability and field workflow
- **Large activity:** `MainActivity.java` was about 120 KB at baseline, and `WelcomeActivity.java` was about 47 KB. Isolate survey state, forms, location capture, and navigation in future refactors using incremental, test-backed changes.
- **Testing gaps:** automated tests cover route arithmetic. No connected Android UI tests, physical GPS/compass tests, screen-size snapshots, or instrumentation migration tests were present in the reviewed tree.
- **Unmatched styling:** screen-specific hard-coded colors, text sizes, and padding remain in `SitesActivity`, `HistoryActivity`, `StationProgressActivity`, `DashboardActivity`, and parts of `MainActivity`. Move shared patterns into `UiKit` over several commits.
- **Survey setup complexity:** route templates, regional suggestions, station spacing, durations, and protocol fields share one long setup screen. Split common requirements from advanced protocol parameters only after regression tests cover route and method selection.
- **Control terminology:** distinguish `End survey` (opens outcomes), `Finish point` (closes one visit), and `Save observation` (writes a detection). Do not overload these actions.

### P2: brand, motion, and visual refinement
- **Forest imagery:** repository includes 240 × 80 WebP day/night scene plates. They are substantially below the detail requested for the final visual system. Upscaling alone does not add detail.
- **Animated forest integration:** `ForestBackdropView` is present, but the current welcome layout does not attach it. A higher-resolution portrait composition and visual performance testing should precede integration.
- **Frame pacing:** the backdrop targets roughly 15 FPS. Evaluate native handset performance and power, then use smoother animation where performance permits.
- **Approved icon mismatch:** the existing launcher artwork follows an earlier forest/frog brand direction. Replace it only from the exact approved minimalist frog-emerging-from-water asset, not a substitute illustration.
- **Accessibility:** review 12sp metadata, contrast on colored controls, text scaling to 200%, small screens (320dp width), touch targets, switch-access semantics, and daylight legibility.

## Verification for this change set
1. Install a CI-signed fresh test build, then open the home screen at 320dp width, large text, and both screen orientations allowed by activities.
2. Start each survey profile. Verify method selection, projects, regional species suggestions, timed protocols, and station creation still work.
3. Scroll to the bottom of the observation form. Confirm **End survey** stays on screen. Test Complete, Incomplete, and long-hold Not Counted with archived metadata.
4. Resume an active point count after leaving the screen. Confirm elapsed clocks, GPS station records, point station sequence, and saved observations are unchanged.
5. Open the offline checklist on a small handset. Verify buttons and back control are usable before expanding technical guidance. Import both attributed CSV and valid MDD CSV.
6. Confirm recent surveys reflect the database and link to full History. Verify scientific audit and legacy import remain reachable using the disclosure.
7. Test button press feedback, field sunlight legibility, TalkBack focus order, landscape restrictions, and accessibility scaling.
8. Export a ZIP and verify it still contains original survey tables, media, and data quality flags. Upgrade from a backed-up v0.15 installation.
9. Use the PR-specific build workflow for compile/unit-test validation. Its signing key is temporary and **not compatible with upgrading an existing install**. Do not publish this artifact as the standard release APK.

## Release gate
Do not merge based solely on compilation. Review the PR, confirm automated validation, then complete a representative field-device walkthrough. Keep the existing production signing key and APK publication flow on the protected main branch.
