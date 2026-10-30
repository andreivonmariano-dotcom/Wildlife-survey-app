# LIKAS RC46 | Active Home, recorder keyboard and GPS hierarchy

**Date:** 30 October 2026 (Asia/Manila)  
**Application:** `0.27.3-rc46-field-layout-hierarchy-preview`, versionCode **106**, package `org.phwildlife.survey.beta08`, database schema **27**.  
**Source:** PR **#43**, merged to authoritative `feature/likas-core-fieldwork-post-rc4` as **`d8b19490347b643fe8258b6506c3f682f3f385fa`**.

## Implemented and preserved

1. **Home:** `LikasPortalUi.hero(Activity, boolean compact)` displays a smaller LIKAS header when an unfinished survey exists. The Resume Survey card remains directly below it, ahead of the three approved portal actions. The redundant EXPLORE LIKAS label is omitted only for active sessions. Text content now determines hero height rather than a fixed 152dp layout, and the hero artwork is hidden at 130%+ system font scaling to avoid text overlap. The original launcher and hero frog image files were not edited.
2. **Recorder:** With Android `adjustResize`, the live screen observes the visible frame and temporarily hides only the noncritical top navigation row and supplementary progress text while the keyboard is open. Session and sampling timers, pause/GPS warnings and the fixed **Save Observation / Review Records / End Survey** footer remain in their existing view hierarchy. The top row reappears when the keyboard closes; live status refresh respects keyboard visibility.
3. **GPS acquisition:** Retry, Location settings and cancel are presented before satellite diagnostics. The 290dp sky illustration becomes an optional 240dp section under a **Satellite details** disclosure. The verified-fix automatic start, explicit start-with-missing-GPS confirmation, station handling, receiver logic, underlying fix quality and loss-of-accuracy states remain unchanged.
4. **Scientific and installation invariants:** Existing 18 approved pixel PNGs, original frog launcher, referenced photographs, taxonomic evidence, SQLite 27, saved historical surveys, Complete / Incomplete / Not Counted logic, package name and pinned signing certificate unchanged. No source data rewrite.

## Automated checks

- PR #43 Scientific Core QA **41021452529**: **success**, **301 Python tests passed**, Gradle JVM unit tests plus `:app:assembleDebug` and `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL**.
- Added `scripts/test_rc46_field_layout_hierarchy.py`, updated stale RC10/RC26/RC33/RC40 source expectations and incremented app version to code **106** for normal update order.
- The active-branch push QA **41021671168** began after merge. Its final result, exact APK hash, signature and direct attachment should be recorded only after observed verification.

## Remaining validation

Physical/emulator instrumentation has **not** been executed. Test active and inactive Home at 320/360/390/412dp with 100/130/160/200% font, TalkBack, smallest-height device, GPS permission denied and no-fix paths, recorder keyboard opening/closing, and in-place RC45-to-RC46 upgrade retaining real records. The global-layout keyboard occlusion heuristic requires physical verification across navigation modes and soft keyboards. Check outdoor contrast and clipping after installing the exact signed development APK.

**Separate release blockers:** Non-atomic backup restore and derived research export legacy-outcome parity remain unresolved. RC46 is a development preview, not field-accepted or production-ready.

## Verified merged-branch binary | 30 October 2026

- Active branch push CI **41021671168**, source merge **`d8b19490347b643fe8258b6506c3f682f3f385fa`**, **success**. Artifact ID **12962235866**: `LIKAS-0.27.3-rc46-field-layout-hierarchy-preview-code106-debug`.
- Downloaded signed artifact and extracted exact `app-debug.apk` as `LIKAS-v0.27.3-RC46-field-layout-hierarchy-debug.apk` to the active conversation. Verified APK ZIP structure and entries, **9,779,564 bytes**, SHA-256 **`2750403b608ae9118c706c1cd5b0f3fbbfc5bda0fa1d7e8c64978631f983e479`**.
- Independently verified APK Signature Scheme v2 signature and its signed-content digest, with unchanged original signing-certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**. Parsed compiled Android manifest: **`org.phwildlife.survey.beta08`**, **versionCode 106**, **versionName `0.27.3-rc46-field-layout-hierarchy-preview`**.
- APK includes **18** approved icon PNGs, **58** offline JPGs and the bundled mammal reference JSON. CI checks the source art hash locks; independent APK check confirms package presence, not a new scientific/photo-license review.
- **Still unverified:** emulator/physical-device execution, font-scale screenshots, keyboard viewport on actual devices, GNSS receiver behavior, backup-restore failure recovery, real installation over RC45, TalkBack and field acceptance. Neither this signing check nor unit tests establishes these results.
