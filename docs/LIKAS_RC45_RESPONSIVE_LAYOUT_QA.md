# LIKAS RC45 | Responsive layout and field-interface consistency

**Date:** 30 October 2026 (Asia/Manila)  
**App:** `0.27.3-rc45-responsive-layout-preview`, versionCode **105**, package `org.phwildlife.survey.beta08`, SQLite **27**.  
**Source:** PR **#42**, merged into `feature/likas-core-fieldwork-post-rc4` at **`e5f27c5efe4a0012543d39dd642a87a0c4d096e9`**. Source HEAD will move with documentation commits; compare actual app files against the merge.

## Implemented layout changes

- Fieldwork, Library, Settings and survey shortcuts use a shared responsive rule: stack paired cards when the window is narrower than **390dp** or system text scale reaches **130%**. The original 160%-only switch left narrow-phone card titles cramped.
- Stacked survey choices and Library/Settings paired tools now render as compact horizontal actions rather than tall vertical mini-cards. Wide layouts retain the approved two-column cards. All Birds, Herpetofauna and Mammals entry points remain.
- The species picker search now uses nearly the full content width with a 48dp suggestions target. **Unidentified** remains an explicit visible secondary action on the next row. Catalog filtering, uncertain identifications and GPS evidence are untouched.
- Discovery shelves reduce unhydrated slot minima from 410dp/625dp to content-guided species **310dp/400dp** and habitat **330dp/420dp** (normal/large type). Long text can grow the cards. Lazy hydration, nearby-card recycling, photo rights and swipe restoration are unchanged.
- Shared navigation headers wrap up to two lines at enlarged type; the back arrow has more reliable sizing. The redundant spoken “Back to Back” label on the species detail screen is fixed.
- The live recorder's auxiliary actions use the existing font-aware touch-height rule rather than 46–47dp exact heights, without altering its persistent **Save Observation**, **Review Records**, **End Survey** footer or the **Complete / Incomplete / Not Counted** lifecycle.
- Preserved all **18** approved pixel-art PNGs, original frog launcher, offline species/habitat references, source data, archive format, signing identity and app ID.

## Automated verification

- **PR #42 QA:** Scientific Core QA **41014610988** passed, including **297 Python tests**, JVM unit tests, `:app:assembleDebug` and `:app:assembleDebugAndroidTest` compilation. Signed app packaging succeeded using the preexisting pinned certificate check. New `scripts/test_rc45_responsive_layout.py` and `FieldLayoutPolicyTest.java` assertions cover width breakpoints and source invariants.
- **Development-branch QA:** Push run **41014826232** was in progress when this document was initially written. Do not claim active-branch binary delivery from the PR run, which did not publish an artifact.

## Outstanding release and visual acceptance

These results establish that the source builds, not that the visual outcome has been inspected on a device. Capture Home, Fieldwork, Library, Settings, Discover, Species Picker and Recorder screenshots at **320/360/390/412dp** and **100/130/160/200%** font scales, with and without the keyboard. Run TalkBack navigation, one-handed reach and bright-light contrast checks. Verify installed upgrade over RC44 with real recorded surveys and every End Survey outcome.

Continue the separate data-integrity workstream for non-atomic restore and legacy-outcome export parity; these are not solved by layout-only changes.

**Disposition:** RC45 source integrated on the active branch after successful PR QA; active-build artifact inspection and direct chat APK delivery must be recorded separately after verification.

## Active-branch signed artifact verification | 30 October 2026

- **Active-branch Scientific Core QA:** run **41014826232**, **success**, based on merged app-code commit **`e5f27c5efe4a0012543d39dd642a87a0c4d096e9`**. Artifact **12958709056**, named `LIKAS-0.27.3-rc45-responsive-layout-preview-code105-debug`.
- **Verified APK:** `LIKAS-v0.27.3-RC45-responsive-layout-debug.apk`, **9,778,644 bytes**, SHA-256 **`60986cea4643c867b08536fef705d7021869473d9cc4de94d4e16af57af94455`**. ZIP CRC passes, compiled DEX and binary manifest present, 18 approved icon PNGs, 58 offline photos and mammal checklist bundled.
- **Independently parsed binary manifest:** package `org.phwildlife.survey.beta08`, versionCode **105**, versionName `0.27.3-rc45-responsive-layout-preview`.
- **Independently verified APK Signature Scheme v2:** RSA SHA-256 signature and signed-content chunk digest both valid; signer certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`** exactly matches RC44. No replacement signing identity or package migration.
- **Direct download:** exact APK was copied into the active conversation's `/mnt/data/` and directly linked there. Other chats must verify this file separately.
- **Not tested:** actual phone upgrades, 320dp/200%-text rendered screenshots, TalkBack, field GPS, on-device backups/exports, battery/performance and survey data retention. Production acceptance is not claimed.
