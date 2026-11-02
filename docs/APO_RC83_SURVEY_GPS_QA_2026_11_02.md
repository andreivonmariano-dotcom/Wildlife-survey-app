# APO RC83 | Focused survey and station GPS preflight

**Date:** 2 November 2026 (Asia/Manila). **Branch:** `feature/apo-survey-gps-v143` from signed RC82. **Build target:** `0.27.3-rc83-apo-survey-gps`, versionCode **143**, package `org.phwildlife.survey.beta08`, SQLite **28**.

## Changes

- Survey GPS displays live acquisition state, reported horizontal accuracy and fix age, plus the actual eligibility rule (accuracy strictly under 10 m, age ≤10 seconds). A qualifying, nonmock, fresh GPS fix still advances automatically. No artificial or inferred position is created.
- Coordinates, GNSS signal/sky view, device ellipsoidal altitude and vertical accuracy remain in an **expandable GPS details** panel. The initial view no longer exposes all raw diagnostics.
- The retry and Android location-settings buttons live in **GPS troubleshooting**, surfaced when blocked by permission, disabled provider or stopped receiver. Missing permission and disabled provider remain distinct statuses.
- A single persistent **Continue without GPS** action is deliberately confirmed, flagging missing start coordinates permanently in the survey record and export. Station mode has a distinct **Start station without GPS** action. The navigation header supplies Back, so the duplicated bottom Back action is removed.
- Text changes retire outdated LIKAS permission instructions without modifying permission requests or device settings.
- Exact GNSS acceptance, monotonic age, original package/signing continuity, immutable PAIC714/WorldCover/mountain assets, study effort and Complete/Incomplete/Not Counted outcomes remain unchanged.

## QA plan

- Existing RC55 Python/Android instrumented tests updated for the same original provenance gates, with new RC83 source contracts.
- `.github/workflows/apo-survey-gps-v143-emulator-qa.yml`: Python regressions, JVM debug app+AndroidTest build, original signer/package/schema/immutable reference validation and native Android 14 emulator screenshots of **survey and station GPS** at 160% (1080×2280) and 200%/320dp (960×2160). GPS is disabled in the disposable emulator so the preflight remains on screen without inventing an accurate location.
- Deliver a direct in-chat APK **only after successful CI, actual artifact extraction, package/certificate/hash verification and screenshots**. Real outdoor GNSS and continuous service, real handset archived-record upgrade and survey completion remain separate acceptance gates.

**Current status:** Source committed, signed build and emulator results pending.
