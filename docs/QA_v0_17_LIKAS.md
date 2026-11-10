# LIKAS v0.17.0: startup, branding, UI and performance QA

Status: development beta. CI build and Java tests are necessary but not sufficient. Verify on actual field handsets before release.

## Identity and upgrade
- Display title LIKAS with subtitle Philippine Wildlife Survey. Keep the Android application ID `org.phwildlife.survey.beta08`, the data files and SQLite schema 12.
- Preserve the signing certificate from the installed app. Do not uninstall earlier builds to get around certificate mismatch. Back up all field data first.
- Confirm app launcher icon (leaf and compass mark), startup title, installed version label, Start New Survey, Continue Survey.

## Startup behavior
- Fresh install: Continue Survey is disabled. Start New Survey opens LIKAS's main menu and project/method setup.
- Existing unfinished survey: Continue Survey opens the active session with its original event ID, station IDs, pause state, timings, unsaved draft and attached evidence.
- Active survey plus Start New Survey: the home menu must explain that an active survey exists, without creating a second session.
- Background notification entry still reaches the active survey. Android Back should return without creating a new event.
- Cancel survey GPS setup and revisit Start New Survey. Existing setup selections must be recoverable. Check current behavior before claiming this passes.

## Forest art and performance
- Scene uses original Android Canvas drawing, not network imagery, video or GIF. Shapes loosely depict Philippine lowland dipterocarp emergent trees, large bases, understory palms and layered forest mist.
- Local 06:00-17:59: lighter palette. 18:00-05:59: darker palette. No server or location permission is needed.
- Animate only when the screen is visible, at a low frame rate. Respect Android's disabled animation setting. Check resource use on low-cost Android phones.
- Forest canvas must not run on survey recording, camera, GPS or background screens.
- Verify LIKAS wordmark and buttons remain readable in both palettes, portrait layouts, large fonts and TalkBack.

## Workflow clarity
- Home: three wildlife survey groups and Custom visible. Data & Tools collapses management screens without removing History, Projects, offline taxonomy, dashboard, audit or legacy import.
- Survey setup: descriptive paragraphs are shorter and chosen scientific settings remain unchanged. Check route design, station spacing, repeated route IDs, province suggestions and duration inputs.
- Recorder: session time, active effort and station clock remain distinct. Extra technical hints are not permanently displayed. Optional scientific fields remain available.

## Scientific integrity
- On Mark now and camera Capture bearing, freeze the current fresh GPS fix, if any. Later Save uses that snapshot, not coordinates from a later time.
- If the location is poor, stale, missing or mock, store NULL observer coordinates. Never invent a GPS position.
- Camera snapshot timestamps must match their captured fix. Save after a delay and verify location belongs to the original mark.
- Restore a marked observation draft and check the frozen location does not silently change.
- Catalog-backed observations must store actual taxon source and version in `name_source` rather than infer the source from the `·` display separator.
- Preserve verbatim identification, count, timestamp, zero-detection visits, photo references, route templates, historical data, pauses and all ZIP export columns.

## UI thread, long field sessions and exports
- Timer updates once per second. Sampling-unit pause metadata is cached for 5 s and station progress/track summaries for 4 s. Verify the displayed station progress catches up after transitions.
- Typing should no longer block on repeated synchronous preference commits.
- Export large ZIP archives off the UI thread. Verify export completeness, field responsiveness during export and error feedback on failure.
- Test 100+ observations and 30+ station changes. Record median and 95th-percentile screen transition duration, memory, dropped frames and battery consumption.
- Run `gradle :app:testDebugUnitTest :app:assembleDebug` and test installation over the previous certificate-matched release.

## Deferred before checklists
- Dedicated background worker for generic taxon CSV/legacy ZIP imports.
- Query-based, indexed taxonomy autocomplete for full Philippine species catalogs.
- More complete source-key relationships, a field-tested signing safeguard and broader automated Android UI tests.
