# LIKAS RC56 | Screen 5 Live Survey Recorder visual audit

**Date:** 30 October 2026 (Asia/Manila). **Status:** Android 14 verified draft preview; not field released.

## Objective and source lineage
- Owner continues actual Android screen-by-screen layout/theme audit after RC52 Home, RC53 Fieldwork, RC54 Survey Setup and RC55 GPS preflight.
- Source: `feature/likas-rc56-live-recorder-screen5`, draft PR **#53** into RC55 draft PR #52, based on earlier chained draft PRs (#51, #50, #49, #48, #47). Integrated core RC49 stays unchanged. Tested app+source SHA `9aab5056b82f49cae2ff6b63caeef25ea6173a84`.
- VersionName `0.27.3-rc56-live-recorder-preview`, versionCode **116**, application ID `org.phwildlife.survey.beta08`, SQLite schema **27**, original debug signing identity retained. Locked approved art and offline catalog media not modified.
- Relevant LIKAS principles: S (survey validity and lifecycle), G (GPS source and missing values), A (accessible and readable controls), U (consistent UI), D (data safety).

## Actual changes to Screen 5
- Compact Back/Survey title/Export top bar matches the restrained LIKAS navigation style; at 200% font the Herpetofauna title displays “Herp survey” and exposes the full spoken label “Herpetofauna Survey.” Export is the exact existing research ZIP action.
- Pause effort/Station progress now stack at narrow widths or enlarged fonts. Mark detection, Measure bearing, method-specific quick evidence and Change remain available; no duplicate evidence action introduced.
- The distance entry and detection-type spinner now use a responsive vertical arrangement at narrow widths. The type spinner has a persistent visible field label and accessible description rather than a tightly compressed horizontal field.
- The count increment/decrement buttons use LIKAS secondary styles with explicit spoken actions and count state, including restored drafts. A Birds transect magnetic-bearing entry now has a persistent visible label, retaining the same method-specific show/hide logic.
- Herpetofauna Reptiles/Amphibians toggles use shared button styling and accessible selected-group semantics. Optional evidence and haptic-detail disclosures explicitly announce their expanded/collapsed state.
- The **Save Observation, Review Records, End Survey** fixed footer and its original callbacks are unchanged. End Survey still opens the Complete, Incomplete and Not Counted choices; scientific survey status and missingness logic unchanged.

## Disabled-GPS reliability finding and correction
- Initial disposable-emulator runs `41076919758` and `41077238899` exposed `ForegroundServiceDidNotStartInTimeException` from `MainActivity.onResume` starting `TrackService.startGnss` while Android GPS provider was disabled. This was a real app edge-case, not a screenshot issue.
- Recorder now checks `LocationManager.GPS_PROVIDER` enabled state before starting/restarting GPS recording in `onResume` and `startTracking`. It does not synthesize GPS fixes or change stored data.
- GPS status on the live recorder now explicitly says **Device GPS off** or **Precise GPS permission needed** rather than misleadingly saying **GPS searching** when the provider is disabled. Device GPS available and qualified-fix logic remain unchanged.
- No changes to TrackService sampling, coordinate persistence, fix-quality thresholds, detection GPS vs observer GPS distinctions, duration calculations, or export SQL.

## Real Android acceptance and deliverables
- Final workflow **41080947448 SUCCESS** on Android 14 API 34. **340 Python tests**, signed Android/JVM compile, **3 dedicated recorder Android tests** (Birds 360dp/100%, Mammals 412dp/130%, Herpetofauna 320dp/200%), **9 earlier Home/Fieldwork/Setup/GPS UI tests** and **1 research ZIP Android test**. All Android tests passed with zero failures (13 total).
- Recorder test launches a synthetic survey on a clean, disposable emulator with `start_without_gps=true`, never fabricates a location or observation, checks that End Survey is enabled and its full bounds are within the screen before/after scrolling, and captures two actual PNGs for each taxon.
- Actual PNGs reviewed. At 320dp/200% font, the compact Herp survey title fits, end controls remain fully visible and the form scroll exposes count, distance and detection type. The dense status card occupies much of the small screen; this is an accepted limited-space tradeoff, not a claim of physical sunlight/TalkBack acceptance.
- GitHub signed APK artifact `12990216240`. Direct APK `LIKAS-v0.27.3-RC56-live-recorder-preview-debug.apk` **9,786,688 bytes**, SHA256 `cf97e35e2e5960ca2efbbe8b17d6ce83b44db0c0c0315416f6c3f91fb130cd08`. APK and artifact ZIP CRC checked. APK v2 signer certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` and RSA PKCS#1 v1.5 SHA256 signedData signature independently verified. 18 approved pixel PNGs and 58 offline JPGs included.
- Android evidence artifact `12990266256`. Direct comparison PNG, evidence ZIP and APK attached in implementing chat; verify sandbox file paths in future chats.

## Outstanding verification
- No physical-device GPS acquisition/outdoor-glare/TalkBack test; keyboard-visible full-dock testing remains limited. Historic record migration, process-death restore, export snapshot concurrency and missing protocol photo/audio integrity still require separate QA.
- CI exercised opportunistic methods for the three taxon profiles, not every standardized method, trap protocol, camera workflow or field observation save under real conditions. Method logic and data schema were not intentionally changed.
- This is a **draft preview**, not a stable field release; upstream science-safety drafts remain unmerged.

## Next screen
Screen 6: **History and observation review**, with direct review navigation from live survey, filter layout at 320dp/200%, recorded-vs-current identification, and editable correction audit visibility. Use actual emulator screenshots and preserve append-only edit history and Not Counted exclusion.
