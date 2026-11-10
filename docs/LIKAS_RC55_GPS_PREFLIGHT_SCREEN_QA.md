# LIKAS RC55 | Screen 4 GPS preflight actual-UI audit
**30 October 2026 (Asia/Manila).** Scope: `SurveyGpsActivity.java`. **Status: draft** pending Android 14 capture and test review.

## Source and design scope
- Branch `feature/likas-rc55-gps-preflight-screen4`, draft PR #52 into RC54 draft PR #51 (RC53 #50, RC52 #49, RC51 #48, RC50 #47 remain draft). Integrated core RC49 unchanged.
- Preview `0.27.3-rc55-gps-preflight-preview`, versionCode **115**, application ID `org.phwildlife.survey.beta08`, DB schema **27**, original signing identity and approved art retained.
- Relevant specification requirements: GPS provenance/accuracy and missingness (G, S), offline field workflow (G, U), accessible actions (A, U), export/scientific reproducibility (D, S), and historical data protection (C).
- This is a real Android activity design and status-copy correction; no survey/observation record is mutated. GNSS receiver logic and scientific GPS acceptance policy remain pinned.

## Inspected issues
- The old screen listed multiple large buttons in a single ScrollView, burying Cancel, while the app automatically advances on a valid fresh fix. It used a different title styling from Home/Fieldwork/Setup.
- One primary TextView mixed ephemeral status and location diagnostics. `updateLocation()` overwrote disabled-provider or denied-permission status with `GNSS running` when `TrackService.currentFix()==null`, even though the receiver could not acquire a fix. This confused a user about why the app did not start.
- Previous live fix text showed coordinates, horizontal accuracy and age, but no explicit source/ellipsoidal-altitude wording. A device altitude must never be represented as terrain elevation or silently overwritten.
- Optional satellite chart stays optional; do not infer surveyed wildlife coordinates from the observer's position.

## Implementation
- Uses shared `UiKit.page` navigation header and a `FieldVisuals.section` hierarchy, separate status card and details, consistent typography and spacing; safe actions are pinned beneath ScrollView. Main scroll contains Check current GPS again, Open location settings, and (survey mode only) optional satellite diagnostics.
- Survey mode pinned actions: **Start without GPS · mark start missing**, with original explicit confirmation and blank start coordinates; **Return to survey setup**, with original `cancel()`. Station mode pinned actions: **Start without GPS · flag missing**, with original confirmation; **Return without starting station**.
- **No second Start tap:** GPS service still automatically opens the survey or station on a qualified fix. `validFix` still requires GPS provider and `GpsFixPolicy.acceptableElapsed`. No thresholds, permission requests, timers, data storage or GPS extra names were changed.
- Priority in periodic status refresh: missing precise permission → disabled provider → no current fix → stale/low-quality/mock fix. A disabled provider cannot be reported as working. Distinguishes GPS reported horizontal accuracy (±m), fix age (seconds), device coordinates and device-reported **ellipsoidal altitude** (with vertical accuracy only if reported). Blank altitude is unavailable, never zero or inferred land elevation.
- Existing accepted survey and station GPS-missing semantics, full privacy and no-MSL inference remain intact.

## Android QA planned
- CI `.github/workflows/rc55-gps-preflight-android.yml`; screenshot/Android XML evidence at 360dp/100%, 412dp/130%, and 320dp/200%, separate survey- and station-mode screens with GPS deliberately disabled, plus scrolled capture.
- Script `scripts/rc55_gps_emulator_acceptance.sh` grants precise permission on disposable emulator then disables the GPS provider through Android system controls to verify that the visible status says `Device GPS is switched off`. Does not synthesize a location.
- New Python source contract `scripts/test_rc55_gps_preflight.py` and Android instrumentation `Rc55GpsPreflightInstrumentedTest.java` verify status/altitude source and the action/toggle hierarchy. Rerun RC54 setup, RC53 Fieldwork, Home and exact RC51 ZIP Android fixtures. No user records are loaded.
- QA result, screenshot inspection, signed artifact checksum and original-signing verification **not yet established** at initial commit. Do not label field-ready or merged. Real GNSS satellite reception, vertical datum/device behavior, 200% font/physical phone TalkBack, process-death restoration and historical upgrade tests remain open.

## Next visual target
After passing this GPS preflight pass, **Screen 5 Live recorder**: survey state/timer, Save/Review/End Survey controls, wildlife location distinct from observer fix, 200% font/keyboard overlap, and no loss of partial observations. Preserve method and lifecycle semantics.
