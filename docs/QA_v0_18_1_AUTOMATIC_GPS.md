# LIKAS v0.18.1: automatic survey start and checklist location QA

Development branch: feature/v0.18.1-auto-gps.
Check the app on physical Android handsets with existing, backed-up v0.18
data. No stage claims full Philippine national checklists for all groups.

## Automatic survey start
1. Configure all required survey fields, method, project and observer.
2. Tap the single Start Survey action in setup. The GPS screen acquires
   a fresh Android GPS_PROVIDER fix; **there is no second Start button**.
3. A valid non-mock current fix with reported horizontal accuracy strictly
   below 10 m and monotonic age <=10 seconds automatically opens the recorder
   and starts the survey effort clock. The original configured values remain.
4. Check a 10.0 m fix, stale fix >10 s, missing accuracy, approximate location,
   mock fix and disabled provider. None must start the survey.
5. Switch to Android Home and leave the GPS screen. The foreground service
   may continue tracking, but no new survey should start while the GPS
   Activity is paused. Return to the app, confirm it starts once, not twice.
6. Check rapid sequential good GNSS callbacks. Exactly one survey row and
   exactly one launch event should be created.
7. Cancel GPS before a qualifying fix: the app returns to existing setup
   without creating a survey or station visit. The setup fields persist.
8. At planned and manual sampling stations, after selecting the station,
   the active qualifying fix starts the visit directly. If not ready, the
   compact GPS acquisition screen starts it automatically once qualified.
   Its missing-GPS override still requires explicit confirmation.
9. Check zero-detection visits, timers, station IDs, and GPS quality flags.

## Automatic regional checklist selection
1. In Setup tap **Choose checklist area by GPS**.
2. The app must only use a current GPS_PROVIDER fix with accuracy <10 m
   and monotonic fix age <=10 s. Mock/stale reading must not qualify.
3. A single unambiguous enclosing mapped province polygon at least the
   documented 700 m caution band from boundaries assigns the coarse
   island-group scope (Mindanao, Visayas, Luzon, etc.) automatically.
4. Check that the selected scope and province appear without an
   intermediate confirmation dialog. Manual selector remains editable.
5. Check overlapping provinces, a position within 700 m of a provincial
   boundary, outside country coverage, poor GPS, and unmapped island
   assignments. The app must not infer a nearest checklist or change the
   manual selection.
6. The island-group scope is **not** evidence that any catalog species
   occurs at the survey location. Test region-only filtering independently
   from the national search; unfamiliar names remain recordable.
7. Press Home during polygon resolution or switch setup method. No
   delayed asynchronous result should overwrite new user choices.
8. Import regional candidate taxonomy separately from verified island/
   province occurrence data, preserving review status and source version.

## Scientific synonym crosswalk
1. SQLite schema 13-to-14 upgrade creates taxon_synonyms without rewriting
   field_records or past raw_identification.
2. Import a UTF-8 synonym CSV with columns:
   accepted_taxon_key,alias_name,source,source_version
3. The accepted taxon key must exist in taxon_catalog. One unknown key
   must roll back the entire CSV transaction.
4. Search a known imported alias. The suggestion should show the
   *accepted* current taxon name from taxon_catalog.
5. Save a new detection with the accepted selected name. Original historic
   observations bearing the older scientific name must remain unchanged.
6. Verify catalog provenance and ZIP exports after an install upgrade.

## Build and release checks
- Run Python taxonomy-validator tests and Android Java unit tests.
- Compare installed signing certificate with the v0.18 APK before upgrade.
- Verify in-place database migration and original field data with a ZIP backup.
- Confirm battery use and accurate field positions on two handsets.
- Maintain normal, disabled, and interrupted foreground GNSS behavior.
