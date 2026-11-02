# APO RC81 | Compact recorder at 320dp and 200% text

**Date:** 2 November 2026, Asia/Manila  
**Branch:** `feature/apo-survey-compact-320-v141` derived from signed RC80 code140.  
**Target:** versionName `0.27.3-rc81-apo-survey-compact-320`, versionCode **141**, original `org.phwildlife.survey.beta08`, unchanged SQLite **28**.

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
