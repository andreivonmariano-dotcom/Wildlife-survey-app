# APO RC78 | Live survey recorder refinement and Android emulator QA

**Date:** 2 November 2026 (Asia/Manila)  
**Current development branch:** `feature/apo-survey-recorder-v138`, draft PR #80 into `feature/apo-biotools-all-screens-v137`.  
**Exact latest signed app/test revision:** `1d63da9eda84ad75d31bdcb12fbc7c15693cc027`, GitHub Actions run **41383237787**, **SUCCESS**. The last app-source change precedes the emulator screenshot/test-only changes.  
**Build name:** `0.27.3-rc78-apo-survey-recorder`, Android versionCode **138**, appId **`org.phwildlife.survey.beta08`**.

## Direct APK and immutable identity

- **APK:** `APO-v0.27.3-RC78-Recorder-Emulator-code138.apk`, **11,755,020 bytes**.
- SHA-256: **`8485de0a265490aea115e55b7510c3a381accceec9585d7daf0afed82f907b39`**, identical in the initial and final signed CI verification; the APK was extracted and independently ZIP-verified in the active conversation.
- Original signer certificate SHA-256: **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**, retained.
- SQLite schema **28**, survey outcome model, app package, bundled approved mountain artwork, PAIC **714** catalog, and WorldCover reference retained. Source file hashes checked: mountain `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`; PAIC `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`; WorldCover `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.

## Screen and workflow changes

The user requested further survey-screen streamlining, prioritizing the **live recorder** and its linked screens. The following were implemented without modifying scientific classification, GPS-fix policy, field data schema, or measured effort semantics:

- **Live recorder:** Clear sampling/detection sections and reduced fixed action heights. The main Save action and secondary Review Records/End Survey actions remain pinned, with conditional vertical stacking for narrow/enlarged-text Android layouts.
- **Save state:** Read-only accessibility-live feedback explains that saving is unavailable while effort is paused or before opening an appropriate point count/sampling unit. This does not circumvent method-specific effort gates.
- **Incomplete drafts:** The End Survey flow checks unsaved species, count, distance, notes, habitat, behavior, identification evidence, photographs, confidence/evidence selection, and marked detection time. A deliberate discard confirmation clears the unfinished draft before the existing Complete/Incomplete/Not Counted choice. The disposition rules remain unchanged.
- **Station progress:** Separates recorded visits from registered project stations with no survey visits. Zero detections remain valid sampled effort; registered stations alone do not count as sampling.
- **Habitat/species preflight:** The Begin Survey and optional map-import actions have content-driven heights rather than 52/56dp fixed heights.

## Verified by automated and real emulator tests

- **415 Python scientific/source regression tests passed**.
- Android JVM unit tasks, debug app and debug AndroidTest APK compilation passed. Exact original signing certificate and binary identity were checked with `aapt`/`apksigner`.
- **Android 14 emulator (API 34), Pixel 4 profile, 160% system font:** one isolated instrumented test **passed** (`RecorderUiEmulatorTest.recorderHasPersistentSaveReviewAndEndSurvey`). It opened an actual, new Birds opportunistic survey with explicitly flagged missing initial GPS in the emulator. It verified Save Observation, Review Records, and End Survey in the native Android view, enabled and within viewport; sampling and detection sections were present.
- Real Android activity-view screenshot was taken during instrumentation, saved in Android MediaStore, extracted after test app teardown, and checked for valid PNG magic, dimensions and adequate image size. Final screenshot: **`APO-RC78-Live-Recorder-Emulator-160pct-Clean.png`**, **1080 × 2280 px**, **165,317 bytes**, SHA-256 **`740efe4692f055a2f0a9e2aa44c8673b317e479b98e30603695c1d1fe748665a`**. Screenshot artifact: `APO-v138-recorder-emulator-evidence`, ID **13114700741**, from final successful run **41383237787**; signed APK artifact ID **13114835118**.
- An earlier raw display capture showed an unrelated headless-emulator **Pixel Launcher not responding** system modal over the app. The final image intentionally captures APO's own View hierarchy so unrelated system overlays and startup Toasts are not mistaken for APO screens. This does not establish that the emulator operating system was free of ANRs.
- **Visual review finding:** At 160% text, the fixed three-button footer remains readable and reachable; however, it takes substantial vertical space, and the observation input form initially lies below the fold. The next UI pass should reduce nonessential HUD/dock height while retaining visible effort/GPS status and an always-accessible End Survey.

## Not tested or certified

No physical-device RC77→RC78 upgrade with archived field data, end-to-end Not Counted/Incomplete/Complete interaction, low-GPS survey station capture, actual microphone/camera/pitfall operations, real backup/restore, or research ZIP content verification was performed. This is a **signed development preview**, not field-certified stable. The screenshot is a native Android View-tree capture in the emulator, not a proof of full end-to-end accessibility, TalkBack use or production-device behavior. Release remains unmerged in draft PR #80.
