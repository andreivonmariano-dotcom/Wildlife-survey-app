# LIKAS v0.17.1 screenshot review and field-test gate

Review date: 2026-10-27. Scope: original user screenshots of v0.17 startup and survey menu, recording layout, import/history lag, and field data consistency.

## Screenshot comparison
- Startup: compare with v0.17 photo. Daytime forest has less flat/circular canopy, irregular layered trees, readable LIKAS title and subtly different daylight colors. Nighttime palette still provides high contrast. No video or external assets.
- Continue Survey: present but legible when disabled. After an unfinished survey exists, it becomes fully active without changing its event identifier.
- Main menu: align LIKAS logo/title/subtitle. Shorten four survey cards; Data & Tools remains a single expandable group. Confirm status-bar background and icon contrast.
- Recording: pinned Save, readable station/active/session timers. "Session options" hides Finish Survey and vibration preferences; "Species search options" hides regional filter copy; confidence is under "Ecology and notes". Ensure the researcher can find these with one tap and no scientific field disappears.
- Detection Mark now: shows mark status only once marked. Clearing it also clears the frozen GPS reading. Camera bearing remains available.
- GPS row: show brief accuracy/state; tap opens full coordinates.

## Scientific regression
1. Back up existing observations and ZIP before installing. Do not uninstall or use a different signing key.
2. Create/resume birds, herpetofauna, mammals and custom survey. Verify no project, station or prior record disappears.
3. Create detections with and without Mark now, and through camera bearing. Save later. Observer coordinates must come from the detection-time fix, or remain NULL when no qualifying fix existed.
4. Check taxon attribution: catalog names carry real source and version, starter arrays keep starter provenance, user-entered unknown names remain user-entered. Verify export columns have not been dropped.
5. Create and close point-count stations with zero detections. Verify paused time, survey effort and station GPS flags are unchanged.
6. Return to LIKAS launch while a survey is unfinished. Continue must restore that event with its timers and unsaved draft.
7. Use notification Stop GPS, then switch away/back. GNSS must stay off until explicit retry. New survey acquisition is explicitly permitted to start GNSS.
8. Verify a station with a current <10 m fix starts without the satellite sky map; stale fix opens compact station check.

## Performance and lag
- Inspect 80+ historic surveys and 200+ observations. History loads only the first 40 records, then 40 more per tap. No duplicate survey metadata should appear on the appended pages.
- Generic 1-25 MB attributed checklist CSV validation and insertion run off the UI thread. Bad imports must fail before mutating the catalog. Cancellation must avoid import.
- Export a large ZIP in the background. The recording interface must continue to respond.
- Measure P95 navigation time, frame jank, APK process memory and battery on two field phones. Automated build success is not a field-performance measurement.
- Test 30-minute survey and background GNSS under dense canopy and open sky.

## Release gate
- Run GitHub `testDebugUnitTest` and debug APK build.
- Run all critical screens on a real Android device, confirm accessible text contrast at larger font scale and interaction targets.
- Confirm installed APK signing certificate equals that of the existing installed release before in-place update. Preserve package `org.phwildlife.survey.beta08` and SQLite schema 12.
- Resolve crash/data issues and verify route/data exports before promoting the draft PR. Comprehensive checklists remain deferred until imported-taxonomy search and provenance checks pass.
