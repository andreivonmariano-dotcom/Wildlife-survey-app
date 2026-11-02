# APO RC81 | Compact recorder at 320dp and 200% text

**Date:** 2 November 2026, Asia/Manila  
**Branch:** `feature/apo-survey-compact-320-v141` derived from signed RC80 code140.  
**Target:** versionName `0.27.3-rc81-apo-survey-compact-320`, versionCode **141**, original `org.phwildlife.survey.beta08`, unchanged SQLite **28**.

## Verified signed Android preview | 2 November 2026

- **Source tested:** `719c45ea79b68a5912ae60180d6cf54ba7cf4ffb` (subsequent documentation commits do not change the APK). GitHub Actions **41388167462 SUCCESS**. `426` Python scientific/UI tests passed, Android/JVM debug app and AndroidTest compiled, and one instrumented recorder UI test passed at each of **160% (1080×2280)** and **200%/320dp (960×2160)** on Android 14.
- **Direct-delivered RC81 APK:** `APO-v0.27.3-RC81-Survey-Compact-code141.apk`, **11,756,772 bytes**, SHA-256 `3d42823bfe59cfd2c631fac294a68b8edff23bc3699c32e1da9dec85c72900a8`. Exact archive extracted from CI artifact **13115909146**, checked as a valid APK ZIP, and file hash matched CI log. Android CI verified package `org.phwildlife.survey.beta08`, code **141**, versionName and original signing cert SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Bundled mountain, PAIC714 and ESA WorldCover assets matched approved immutable SHA-256 hashes, rechecked in the extracted APK.
- **Screenshots:** Artifact **13116297951**. Valid distinct Android View captures: `APO-RC81-Recorder-160pct.png` (198,179 bytes, 1080×2280, SHA-256 `6f6720a15718579b332737d9aacaa1b455ca50bdc8c206813a78026a177a80da`) and `APO-RC81-Recorder-200pct-320dp.png` (197,453 bytes, 960×2160, SHA-256 `e5fce6dc36bfd4d0c5c04ee4f26fa5a9a864701ff0ca03c4f687034459ccfda8`).
- **Visual review:** At 200%/320dp the Record observation title and top of the species input are now visible above a fixed full-width Save and horizontal Review / End Survey row; the previous RC80 screenshot showed none of the observation form at that size. The species input is not fully visible without scrolling. It remains a preview with room for further refinement, not a complete accessibility certification.
- **Release boundary:** Signed **developer preview**, draft PR #83 remains unmerged. Legacy historical RC4 forest/sprite PR lineage gate is incompatible with this current APO branch and separately fails; the dedicated signed code141 workflow passes. Historical-data handset upgrade, GPS/media behavior, full Complete/Incomplete/Not Counted flows, backup/restore and research ZIP round-trip remain unverified.

## Visual correction

A genuinely distinct 960×2160 Android 14 emulator screenshot (320dp logical width at 200% system text) showed that RC80's vertically stacked three-button persistent footer and lengthy timer/GPS status left the observation form below the initial viewport. Its 160% screenshot was acceptable. The two images were initially mistakenly identical because a second MediaStore screenshot got a duplicate filename; CI now asserts two unique filenames, pixel dimensions, and different image hashes.

RC81:
- Shortens the compact unpaused-session HUD for continuous/nonstation methods. Method remains visible separately. Station counts and tracked transects still display their specific unit timer. This does not equate session time with stationary sampling effort.
- Collapses GPS searching text to a concise but still explicit unverified survey-start status. Full source, accuracy and timestamp details remain available through the always-visible GPS row.
- Uses a two-column **Review / End Survey** footer on 320dp 200% layouts; stacks below 320dp or at extreme font-scale/narrow-width combinations. Save remains full width, all controls remain at least 48dp touch height.
- Reduces the sampling/detection disclosure height and label at 200%. All tools remain reachable. Survey completion, station drafts, and scientific data logic from RC80 are retained.

## Evidence gates

- `scripts/test_apo_survey_compact_v141.py` and existing Python, JUnit and Android instrumentation regressions.
- `.github/workflows/apo-survey-compact-v141-emulator-qa.yml` compiles signed code141 with retained signer, immutably checks PAIC714, WorldCover and mountain emblem, and takes **two distinct** instrumented Android 14 screenshots at 160% and 200%/320dp.
- Direct in-chat APK only after successful CI, exact package/signature/version and file hash checks.
- Real handset data-preserving upgrade, GNSS, camera/audio, Complete/Incomplete/Not Counted end-to-end, ZIP and TalkBack remain unverified. Keep PR draft and unmerged.

**Status at creation:** Source and QA code committed; CI and APK verification pending.
