# LIKAS v0.19.5: Android app audit and field usability cleanup

Audit date: 2026-10-27
Code baseline: `feature/v0.19.4-auto-elevation` (`84814f8403021290a56c95066b05ed7f1300594a`).
Implementation: `feature/v0.19.5-ui-audit-cleanup`.
Review scope: survey setup and launch, observation form, bird/herpetofauna/mammal detail sections, species lookup, GPS gate, archive history, dashboard, SQLite export, and release build metadata.

**Evidence:** source review and Android CI. No device screenshots, accessibility service inspection, or physical field timings were collected in this review.

## Confirmed observations

1. `MainActivity.draw()` created and attached every taxon detail panel before `refreshAutocomplete()` hid those for other profiles. These unrelated views should never join the layout tree. The new version creates control instances needed for data binding but attaches only the appropriate group's section. The later display rules remain in place.
2. `MainActivity` contained a disabled method Spinner with no visible view, plus an unmounted area/filter panel. Its hidden filter was never reachable. Those controls, listeners, and redundant content were removed. The full/local checklist selector in Identify species remains intact.
3. Two amphibian-specific labels remained visible even when their Spinner fields were hidden. The call index label now follows amphibian-group visibility. Background noise label follows the amphibian calling method.
4. `WelcomeActivity.setup()` hid method-specific route widgets but left the route heading, spacing label, and repeat-route heading onscreen. These headings now follow the corresponding method. The existing route and station calculations are unchanged.
5. Setup displayed a full-width project management action and permanent optional stratum fields. Project management now sits next to project choice; optional stratum details are collapsed on first display.
6. `SpeciesPickerActivity` repeated permanent scientific cautions and placed a large unidentified action below other context, reducing list space. The unidentified action sits alongside search. A tap-accessible score explainer retains the caution that the index is not an occurrence probability.
7. Result cards repeated ecological detail and source text already present in `TaxonDetailActivity`. That long material is now visible under Details, with name, evidence status, and preliminary screening still in the list.
8. `HistoryActivity.recordDetail()` opened a dialog with Edit and More, then reopened another dialog with Edit again. One action list now offers Edit, Photos, History and Delete. Saved record editing and its audit log are unchanged.
9. `DashboardActivity` rendered in both `onCreate()` and the first `onResume()`. It now renders once per resume. Summary counting queries remain.
10. Home, GPS and dashboard contained repeated instructional sentences. These were shortened or moved behind details. The pinned End Survey action was not changed.

## Scientific safeguards

- End Survey remains visible and offers **Complete**, **Incomplete**, and **Not Counted**, subject to the existing survey-close policy.
- Point-count timing, missed stations, survey outcome audit, incomplete-effort handling, and existing station IDs are preserved.
- GPS preflight's fresh/nonmock/less-than-10-meter-reported-horizontal-accuracy gate remains unchanged. GPS elevation datum and uncertainty handling remain untouched.
- The full offline bird, reptile, amphibian and mammal catalogs remain installed. No taxonomy records or evidence sources were changed.
- Stored detections, SQLite schema, export headers, station environmental snapshots, and edit audit were not changed by the UI refactor.

## Testing

- New `FieldUiPolicyTest` covers profile-specific detail visibility, reptile vs amphibian call controls, and route-only setup sections.
- Existing JVM unit tests, Python source-processing tests and Android APK build run in the GitHub Actions workflow.
- The app version is `0.19.5-beta`, versionCode 34, to distinguish the build from 0.19.4.
- Release workflow metadata no longer claims that a newly built 0.19.5 APK is v0.16.

## Unresolved technical risks (separate follow-on refactors)

**P1: GPS listener database I/O.** `TrackService.record()` runs SQLite transactions directly in the GPS listener callback. A batched serial writer, flushed at stop/end, needs stress testing for event ordering and track integrity.

**P1: Large Android activities.** `MainActivity` still owns over 2,000 lines of recording, state, export, and UI logic. Split into lifecycle-safe coordinators by form, station navigation, state saving, and export. Do not rewrite the data model in the same change.

**P2: Screen redraw cost.** Main's station progress still performs SQLite count queries every four seconds on the UI thread. Dashboard runs several synchronous queries per resume. Cache changing values and calculate aggregates off the UI thread after benchmarking.

**P2: UI consistency and accessibility.** Activities still implement their own text/layout helpers. A common control library, saved disclosure state, keyboard traversal tests, TalkBack labels, and large-font testing should be handled in a dedicated UI pass.

**P2: Species rendering under large checklists.** The picker searches on a worker with a 5,000-row cap and page rendering, but sorting still materializes a potentially large list. Profile with Philippine checklists and slow storage before changing query semantics.

**P2: Survey-start transition.** SurveyGps and Main each validate location freshness; this is intentional defense, but a slower initial transition needs a controlled device test to confirm that a previously accepted fix is not rejected only because of Activity startup latency.

## Acceptance on a real handset

1. Open Birds, Herpetofauna and Mammals. Verify only each group's detail section is attached and opened, with no irrelevant labels.
2. Switch from reptiles to amphibians and back. Verify call index is only visible for amphibians, and noise only during amphibian calling methods.
3. Open each method in Setup. Route heading, spacing and repeat-route controls must appear only for supported methods. Verify planned point counts and saved route reuse.
4. Find a named species, enter an unlisted name, and choose unidentified. Verify national/local catalogs and source references remain reachable.
5. Save detections, attach a photo, mark a detection, pause effort, resume, change station, and end in Complete or Incomplete. Verify timestamps, survey audit and ZIP.
6. Cancel an active survey with a reason. Verify it remains archived as Not Counted and is absent from complete-only science summaries.
7. View a saved record, edit, inspect photos and edit history. Check the single-action dialog and deletion audit.
8. Check the GPS gate under poor sky visibility, Android 14 MSL elevation and Android 13 ellipsoidal elevation.
9. Test on a 320–360 dp phone, a large-font configuration and TalkBack. Confirm every key field and the pinned End Survey button can be reached with the keyboard shown.
10. Restore a pre-v0.19.4 database. Verify no observations, stations, source labels or exports are lost. Confirm update signing certificate matches the installed APK.

The visual cleanup is a narrow code change. It should not be described as a verified device-level performance improvement before the field tests above pass.
