# APO RC83 | Focused survey and station GPS preflight

**Date:** 2 November 2026 (Asia/Manila). **Branch:** `feature/apo-survey-gps-v143` from signed RC82. **Build target:** `0.27.3-rc83-apo-survey-gps`, versionCode **143**, package `org.phwildlife.survey.beta08`, SQLite **28**.

## Signed RC83 acceptance | 2 November 2026

- **Signed app/test source:** `2d52c0c602e955738f6960c89e276ec7eda07a72`, GitHub Actions `41397832886` **SUCCESS**. **432 Python regressions** passed; JVM, Android debug APK and AndroidTest APK compiled. Instrumentation passed **4 Android 14 tests at 160%** plus **4 at 200%/320dp** for both survey and station GPS screens, including original RC55 legacy checks. Four unique native screen captures verified with dimensions and hashes.
- **Direct-delivered signed APK:** `APO-v0.27.3-RC83-Refined-Survey-GPS-code143.apk`, **11,757,620 bytes**, SHA-256 `7cd62bf5706352298d15294c81034ab46922b4aba43dbb1f53e909b4a1edbf90`. Extracted directly from successful workflow APK artifact **13120031786** and independently ZIP-tested. CI verified `org.phwildlife.survey.beta08`, versionCode143, versionName `0.27.3-rc83-apo-survey-gps`, original signer certificate `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Extracted APK retains SHA-locked approved mountain emblem `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`, PAIC714 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`, WorldCover `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.
- **Screenshots:** Artifact **13120690245**: survey 160% `gps_survey_160.png` (1080×2280, SHA `589cd82131c60f6c62ed234269208734992bc61bc203bce5095486fdf01c651e`), station 160% `gps_station_160.png` (1080×2280, SHA `d5e3b00426c5bf77cc132a6169585197ae7de9386a4f8c4ab2562bc5c18aa2fc`), survey 200%/320dp `gps_survey_200.png` (960×2160, SHA `5da3a090d36cbd7825c9c15d6c75bb5c50ef5f05e142733cd750fca448f10107`), station 200%/320dp `gps_station_200.png` (960×2160, SHA `f18e3c89842126fb38c93a73d5bff9115eab9d4842a200acb3ff78d619e9aa6d`). Visual review of survey 160% and 200%: fix state and accuracy/freshness rule readable and fixed missing-GPS action visible; troubleshooting is initially collapsed. At 200% the larger typography pushes some disclosures below first fold, but they remain scrollable.
- **Release status:** Direct in-chat file attached for this session, GitHub PR #85 remains **draft/unmerged**. Real GNSS acquisition, precise permission grant/revocation, service resume across app backgrounding, actual survey-start/station persisted outcomes, old survey upgrade, TalkBack, and research ZIP remain **unverified on physical devices**.
- **History:** Initial source run failed on source-test version/legacy-label expectations, then first emulator run failed on hidden/nonattached Start-button expectations, then another emulator test incorrectly assumed one GPS status across an Android screen reconfiguration. These were test-contract issues; final successful run is the sole signed acceptance proof. The old RC4 art-lineage gate separately fails on this modern APO branch and is unrelated to the dedicated RC83 build.

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
