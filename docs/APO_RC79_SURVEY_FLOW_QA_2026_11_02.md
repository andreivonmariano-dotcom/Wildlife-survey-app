# APO RC79 | Observation-first survey flow and end-of-survey clarity

**Date:** 2 November 2026, Asia/Manila  
**Development branch:** `feature/apo-survey-flow-v139`, directly derived from the verified RC78 recorder draft.  
**Build declared in source:** `0.27.3-rc79-apo-survey-flow`, versionCode **139**, package `org.phwildlife.survey.beta08`, SQLite **28**.  
**Status when authored:** Changes committed; signing, CI, APK, emulator screenshot and physical-phone acceptance are **not yet verified**.

## Signed RC79 build and Android emulator acceptance | 2 November 2026

- **Verified source revision:** `bdc8f067e00740b89adab893ae65f97c07d0422c`. Signed GitHub Actions run **41384887314** completed **successfully**; exact candidate `0.27.3-rc79-apo-survey-flow`, versionCode **139**, `org.phwildlife.survey.beta08`.
- **APK extracted directly from CI artifact** ID `13115261330` and verified in the current conversation: `APO-v0.27.3-RC79-Survey-Flow-code139.apk`, **11,755,588 bytes**, SHA-256 `a45503c9facdf9f4d1a3dfd631c943ec6ef40da007d60086811f0d1a5d5daf6e`. ZIP archive validation passed. CI `aapt`/Android `apksigner` confirmed package/code/version and original certificate `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Stored approved asset hashes were also independently rechecked in the extracted APK.
- **Automated test evidence:** **419 Python source/scientific tests**, Gradle/JVM build, AndroidTest APK compilation, and **one Android 14 API34 emulator instrumentation test at 160% font** passed. The emulator test verifies Save, Review and End visibility, disclosure expand/collapse, and captures the activity View tree.
- **Screenshot:** CI artifact ID `13114639688`, PNG `APO-RC79-Live-Recorder-Emulator-160pct.png`, **1080×2280** pixels, **205,630 bytes**, SHA-256 `0f6dd9e60b465572653e148d76af5e80b04de8a42d83cb3343065af6273a52b9`. Manual visual inspection shows persistent End Survey and a slimmer two-column footer, with the Record observation heading and top of the species entry above the dock. **Remaining UX limit:** the enlarged HUD card occupies considerable space; the species entry continues partly below the fold.
- **Release status:** A signed APK and screenshot were provided via verified **direct in-chat download links** during the RC79 development conversation. Those paths are session-specific and must never be reused by another chat without re-verification. RC79 is a **development preview, not physical-field-certified**.
- **Outstanding:** Device upgrade from RC78 preserving actual survey records, 320dp/200% layouts, TalkBack, keyboard/back navigation, actual GPS/microphone/camera, the Complete/Incomplete/Not Counted end-to-end paths and raw data/research ZIP contents.

## Scope and rationale

RC78's Android 14, 160% text-size screenshot found that a full-width three-action dock and the expanded sampling/detection sections placed observation entry below the initial screen. RC79 addresses the measured layout problem without removing critical data or survey exits.

- The primary Save Observation remains full width, with a height capped at 80dp. Secondary Review Records and End Survey may share one row at >=340dp and font scales below 190%; at 200% on narrower widths the actions stack to avoid clipping.
- For methods without station units or tracked transects, the live recorder defaults to a compact observation-first layout at >=160% font size. Sampling and detection controls remain accessible through a labelled show/hide disclosure. Methods requiring current sampling stations or line-transect GPS paths retain their visible sampling/detection sections.
- The End Survey unsaved-entry guard now includes non-default life stage, distance-source, first-detection position and movement selections, with the associated reset when the user explicitly discards the draft. No recorded or saved observation is deleted.
- The existing finalization choice shows a read-only method-completion checklist before asking for Complete, Incomplete or Not Counted. `SurveyClosePolicy` and persisted outcomes remain authoritative. No field values, GPS measurements, durations, effort thresholds or row counts are inferred.

## Verification gates

Automated coverage added or updated:

- `FieldLayoutPolicyTest`: normal phone, 160% and 200% font dock behavior, and height bounds.
- `scripts/test_apo_survey_flow_v139.py`: code contracts for survey-tool disclosure, persistent End Survey, hidden-unsaved draft checks and unchanged package/schema identity.
- Updated `RecorderUiEmulatorTest`: at 160%, confirms survey-tool disclosure expands/collapses while Save, Review and End Survey remain accessible and an app-only screenshot can be captured.
- `.github/workflows/apo-survey-flow-v139-emulator-qa.yml`: source/scientific regression suite, JVM tests, debug APK + AndroidTest compilation, identity/signer/art asset checks, Android 14 API34 emulator at 160% font with an instrumented recorder test, screenshot artifact and signed preview artifact.

**Pending evidence:** Actual signed workflow result, screenshot layout review, direct new APK retrieval, on-device 320dp/200% font, hardware Back/keyboard behavior, complete/incomplete/not-counted outcomes, RC78→RC79 migration with genuine archived surveys, camera/audio/GNSS, consent/permissions, research ZIP contents and TalkBack. Do not announce a signed release until the APK, package, signer, version and current-session direct download are verified.

## Protected invariants

No SQLite schema update, taxonomic/checklist import, PAIC714 or WorldCover edits, launcher change, signing key replacement, deleted history or modified survey-completion criteria. Zero-detection effort remains legitimate; unknown values remain missing. All changes are scoped to survey UI and unsaved draft handling.
