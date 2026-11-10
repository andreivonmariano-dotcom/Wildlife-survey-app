# Wildlife Survey App v0.16.1: source audit and v0.17 cleanup plan

Audit date: 2026-10-27
Audited revision: `004ab2579afcca3241b59a8b65b9ea7ca9945d59` on `feature/v0.16-gps-preflight`.
Scope: Android Java source, SQLite schema and migrations, GNSS foreground service, field recording, setup, taxonomy, imports, exports, history, dashboard, GitHub CI.
Evidence class: code review. The branch passes the configured Java unit-test and APK workflow. No Android instrumentation tests or device performance measurements were run for this audit.

## Protect these research behaviors
- Retain permanent station IDs, exact observation timestamps, verbatim names, raw GPS uncertainty, missing-coordinate flags, zero-detection visits, protocol timing, pauses, audit history and export compatibility.
- Retain existing SQLite data and historic survey exports through migrations. Never silently relabel old positions or taxonomy sources.
- Keep precise survey and station location gates. Under-10-m Android-reported horizontal accuracy does not certify true horizontal error.

## P0: fix before layout refactoring

1. **Observation GPS not frozen or checked.** `MainActivity.markDetection()` stores a detection timestamp only. `MainActivity.save()` reads the latest mutable `location` at Save, without testing age, reported accuracy, or mock status. A detection can be tagged with a later or stale location. Change to a validated GPS snapshot at marking or camera capture. Preserve unknown coordinates and expose a quality status.
2. **Taxonomy source attribution is inferred from punctuation.** `MainActivity.save()` assigns `name_source=offline_starter_v0.4` for any raw label containing ` · `. MDD and imported checklists use the same display separator. Associate the selected catalog taxon key, source, version and review state with each new observation. Preserve historical `raw_identification` and existing rows unchanged.
3. **Survey GPS approval is checked twice across a screen transition.** `SurveyGpsActivity.launchWithFix()` validates a fix. `MainActivity.toggleEvent()` repeats a 10-second age check after constructing the recording view and opening SQLite. A slow first launch can reject an accepted fix. Move transition ownership into one survey-start coordinator and make the accepted-fix handoff explicit, without relaxing freshness at capture time.
4. **Stop GPS action has competing owners.** `TrackService` exposes a Stop GPS notification action. `MainActivity.onResume()` starts the service again for an active event. Define clear states: acquiring, surveying, paused, manually stopped, completed. Do not restart after a manual stop without explicit consent.
5. **Signing identity still needs a release check.** `.github/workflows/build-apk.yml` restores a signing key from Actions cache and can generate a replacement on cache miss. A release workflow must validate the expected certificate fingerprint before publishing an APK intended to update installed apps.

## P1: remove main-thread bottlenecks

6. **Timer tick performs database queries.** `MainActivity.status()` runs every second. In station surveys it reads sampling-unit pause totals, recorded station count, and visit progress. Keep display timers in memory. Refresh station counts only on start, close, resume and data changes.
7. **Draft persistence blocks the UI.** `MainActivity.persistDraft()` serializes many fields and calls SharedPreferences `commit()` during editing, pausing and navigation. Use an ordered background writer and `apply()` for noncritical preferences. Keep final draft saves durable at transitions. Preserve crash recovery.
8. **Import and export run on the main thread.** `MainActivity.onActivityResult()` writes the entire ZIP export in its Activity callback. `CatalogActivity` processes generic CSV import in `onActivityResult`, and `ImportActivity` reads the legacy ZIP on the main thread. Use a single background I/O queue, progress text, cancellation and atomic commits. The dedicated MDD import already uses a worker.
9. **History renders all rows as Buttons inside a ScrollView.** `HistoryActivity.surveys()` and `observations()` create one view per database row. Paginate records or use RecyclerView with stable IDs, then fetch detail on demand.
10. **GPS track persistence runs synchronously on receiver callbacks.** `TrackService.onLocationChanged()` writes SQLite transactions for accepted and rejected track points during an active transect. A serial background I/O queue with bounded batches and flush-on-stop retains audit reasons without blocking GNSS delivery.
11. **Duplicated screen refreshes.** `DashboardActivity.onCreate()` calls `render()`, then initial `onResume()` calls `render()` again. `WelcomeActivity` rebuilds Home in `onResume()`. Update view state in place rather than recreating entire screens.
12. **Checklist search builds a full in-memory list.** `MainActivity.updateTaxaSuggestions()` loads the taxon catalog and creates a new ArrayAdapter. For comprehensive Philippine taxa, use limited, query-based name search, a short typing debounce, and indices where evidence supports them. Keep unidentified categories and unrestricted verbatim entry.
13. **Initial catalog seeding happens during database opening.** `FieldDb.onCreate` and `upgradeTwelve()` can read and insert the MDD asset on the calling thread. Provide an indexed, versioned seed step and a clear installed-catalog state. Do not block the first survey screen opening for taxonomy setup.

## P2: streamlined field workflow

14. **Home contains six detailed management cards and repeated explanatory copy.** Show Resume first, then the three survey groups and Custom. Group History, Projects, Scientific review, Offline checklist, Dashboard, and Legacy importer under a secondary Data and tools view. Keep backup/import controls discoverable.
15. **Survey setup shows too many method descriptions and optional configuration sections.** Start with Project, Method, Observer, and method-specific duration or route. Fold region suggestions, stratum, optional effort parameters and repeat-route controls under relevant sections. Display a single short route summary.
16. **Recording screen mixes configuration, effort controls and observation entry.** Main view: station, timer, compact GPS state, species, count, detection method, distance where needed, and one fixed Save button. Put camera, notes, photographs, identification confidence, bird distance quality, and habitat details in short expandable sections. Keep the Next station action present only for station methods. Move destructive Finish survey into a confirmation menu.
17. **Long instructions are repeated across screens.** Replace permanent technical paragraphs with a small Help or Info affordance and contextual error text. Keep warnings that change field decisions, including unknown GPS uncertainty, late observations and incomplete effort.
18. **Setup navigation is reset before GPS acquisition finishes.** `WelcomeActivity.startSelected()` marks its state Home before launching `SurveyGpsActivity`. Keep draft setup state until survey creation is confirmed. Cancel should return to the same configuration with the same selections.
19. **Two visual vocabularies and several hand-built helpers.** `UiKit` exists, but activities duplicate typography, button helpers and card padding. Move styles and spacing to one small UI system without an immediate framework rewrite.

## Target workflow

Home -> Survey setup -> One survey-start GNSS check -> Recording -> Station transition -> Recording -> Finish confirmation -> Review/Export.

- Survey-start GNSS sky chart: one screen. Station change: use continuous GPS and show a compact check only for stale or inaccurate fixes.
- Station transition: one deliberate action after arrival and selected station identity, capture station GPS, then begin the station timer. Keep ongoing survey and station effort clocks separate.
- Observation entry: one-tap Save for the default method. Advanced research fields are available with one expansion, never deleted from storage.
- Pause and navigation: restore scroll and draft without rebuilding the entire recording page.

## Implementation sequence

**Gate A: data and lifecycle correctness.** Add regression tests for GPS snapshot timing, mock/stale readings, manual stop state, station IDs, detection timestamps, taxonomy provenance and setup cancellation. Fix P0 issues. Preserve database compatibility.

**Gate B: responsiveness.** Move ZIP, generic catalog import, legacy ZIP import and track-point writing off the UI thread. Cache the timer and station progress. Paginate history. Add trace markers for slow queries and screen creation.

**Gate C: UX cleanup.** Extract small screen sections from `MainActivity` without rewriting the Java app. Apply the simplified Home, setup, recording and station flow. Move help text out of the default view. Keep accessibility labels and glove-friendly targets.

**Gate D: checklist readiness.** Replace full catalog-to-array rendering with indexed, capped queries. Store taxon-key and source/version provenance. Then begin licensed and reviewed national taxon imports.

## Acceptance checks and release gate

- Run tests on a phone upgraded from v0.15, retaining the database and application ID. Validate signing-key fingerprint before distributing an update.
- No GPS value invented for poor, stale, mocked, or missing readings. Every new observation has a valid detection-time coordinate or a clear missing-quality status.
- One tap from a confirmed fresh station position to start its visit. The station sky chart stays hidden.
- No per-second SQLite reads from the visual clock. No large ZIP/CSV parsing or export on the UI thread.
- Target a responsive 60 Hz interface: monitor frames longer than 16.7 ms and set a measurable jank budget after baseline testing. Measure median and P95 transitions on representative low-cost Android handsets.
- Repeat a 30-minute field recording session, a 100-observation history, a 2,000-taxon lookup and a large ZIP export. Record dropped frames, load times, storage, memory and battery data.
- Confirm timing, pause records, missed visits, zero-detection surveys, taxon source labels and all CSV columns remain research-valid.

## Deferred work

- Full Philippine bird and Sy herpetofauna catalogs are deferred until the taxonomy search, provenance and import path pass the acceptance checks.
- Keep the historical `sightings` table during compatibility work. Do not remove legacy tables without a proven, reversible data migration.
- Do not migrate the whole Java application to another UI framework just to gain smoother transitions. Measure the modular cleanup first.