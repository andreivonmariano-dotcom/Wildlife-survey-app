# LIKAS v0.16.0 Parallax Preview: Android Performance and Release Gate

**Date:** 28 October 2026 (Asia/Manila)
**Branch:** feature/rainforest-parallax-v2
**Status:** CI Android build and Java unit tests passed. APK package/version and certificate verified against v0.15 baseline. Physical-device FPS, upgrade, and regression checks remain unverified.

## What changed
- Java-native Canvas background added to the existing WelcomeActivity hero.
- Six separate moving planes or components: clouds, ridges, mid-canopy, mist, waterfall/river, foreground leaves. Local-time color interpolation shares one geometric scene across all four states. Optional night fireflies.
- Frame requests via postInvalidateOnAnimation(), tied to Android display vsync.
- Motion stops when activity is paused, respects Android animator-disable and battery saver, and provides a static view when disabled.
- No database migration, field-survey or taxon import logic changed. Existing Android package ID is retained.
- Preview version: 0.16.0-parallax-preview, versionCode 18. Intended ONLY for testing on devices with the established LIKAS signing key.

## Build procedure (executed in GitHub Actions on 28 October 2026)
Do NOT distribute a new key as an app upgrade. Use the established .signing/wildlife-beta08.keystore, as configured in app/build.gradle.

Build on a developer machine with Android SDK 35 and Gradle 8.11.1:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

Confirm output path `app/build/outputs/apk/debug/app-debug.apk`, package `org.phwildlife.survey.beta08`, versionCode 18 and versionName 0.16.0-parallax-preview. Verify APK certificate matches a previous installed working beta.

## Phone frame measurements (not yet executed)
Use a physical Android phone with a stable, known 60 Hz display refresh rate. Keep the LIKAS home hero on screen, unplug the device and let thermal conditions settle. Test in daylight, night, battery saver and reduced motion. On a 90/120 Hz display, report the actual refresh rate and expected frame cadence.

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell dumpsys display | grep -i 'refresh'
adb shell dumpsys gfxinfo org.phwildlife.survey.beta08 reset
adb shell monkey -p org.phwildlife.survey.beta08 -c android.intent.category.LAUNCHER 1
# Keep the home forest hero visible and observe motion for at least 30 seconds.
adb shell dumpsys gfxinfo org.phwildlife.survey.beta08 framestats > likas_frame_stats.txt
adb shell dumpsys meminfo org.phwildlife.survey.beta08 > likas_memory.txt
```

Measure actual frame presentation cadence in Android Studio Profiler / System Trace. Record median and 95th percentile frame times, missed-vsync frames, Janky frames, frame-rate setting, thermal state, and RAM use. A 60 FPS label requires observed sustained cadence close to 16.7 ms on the target 60 Hz device. Vsync scheduling *alone* does not prove 60 FPS.

## Required functional regression checks
1. Start fresh Birds, Herpetofauna, Mammals and Custom surveys from home and confirm existing setup methods.
2. Return to Home, resume an active survey and confirm End Survey remains accessible.
3. Verify History, Projects, Taxa, Audit and dashboard cards retain their original navigation.
4. Check offline launch and activity background/foreground during a survey. Verify recorded data is unchanged.
5. Test at 320 dp width, text scale 200%, TalkBack, and direct-sunlight conditions.
6. Force animator duration scale 0 and battery saver. Forest must remain static and buttons stay usable.
7. Switch phone time zone and time. Confirm morning/noon/dusk/night colors with no sudden art geometry swap.
8. Verify icon, app ID, signing certificate, versionCode increase and database continuity.

## Verified CI artifact and direct delivery

- CI workflow: `Build Android Debug APK`, run `40630133535`, source commit `23e1c8f5bb48fe790caabebec0931493531a8d25`, result **success** on 28 October 2026.
- Java unit tests and `:app:assembleDebug` passed. The workflow's existing signing key cache was restored and verified; no new key was generated.
- GitHub Actions artifact ID `12791401395`. The ZIP was retrieved in ChatGPT, and its actual `app-debug.apk` was extracted to `/mnt/data/LIKAS-v0.16.0-parallax-preview-debug.apk` in the originating conversation.
- Extracted APK: **280,733 bytes**, SHA256 `9de10ad747a2b2b0454ecc295b7bf6d9d9bc3faee69921a0cd0f01e474b32653`.
- Parsed binary AndroidManifest: package `org.phwildlife.survey.beta08`, versionCode **18**, versionName `0.16.0-parallax-preview`.
- Parsed APK v2 signing certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. This exactly matches the previous v0.15 APK recovered from successful `main` CI run `40627444544` (versionCode **17**, versionName `0.15.0-beta`).
- The current conversation provided a **direct in-chat APK link**. This build is a preview, not a merged production release. A prior conversation's sandbox link must not be reused without verifying its presence again.
- The safe feature branch workflow now builds `feature/rainforest-parallax-v2` and only performs GitHub release publication on `main`, so the existing stable release was not overwritten.

## Unverified handset acceptance checks

- Actual Android 60 FPS, frame-time p95, jank, thermal and memory behavior.
- Physical-device install-over v0.15, preserved survey records, resumed survey access, TalkBack, 200% font, and battery saver.
- Visual equivalence to the richer concept art: source includes added canopy texture, rocky ravine, water spray and foreground ferns, but no Android framebuffer capture was performed.

## Next
Test the downloaded preview APK on a real device. Log frame timing, upgrade compatibility, and regressions. Keep pull request #37 in draft until device tests and product-owner visual review pass.
