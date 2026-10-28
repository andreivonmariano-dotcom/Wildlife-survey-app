# LIKAS v0.16.0 Parallax Preview: Android Performance and Release Gate

**Date:** 28 October 2026 (Asia/Manila)
**Branch:** feature/rainforest-parallax-v2
**Status:** Native source exists on feature branch. APK assembly and handset FPS are NOT verified.

## What changed
- Java-native Canvas background added to the existing WelcomeActivity hero.
- Six separate moving planes or components: clouds, ridges, mid-canopy, mist, waterfall/river, foreground leaves. Local-time color interpolation shares one geometric scene across all four states. Optional night fireflies.
- Frame requests via postInvalidateOnAnimation(), tied to Android display vsync.
- Motion stops when activity is paused, respects Android animator-disable and battery saver, and provides a static view when disabled.
- No database migration, field-survey or taxon import logic changed. Existing Android package ID is retained.
- Preview version: 0.16.0-parallax-preview, versionCode 18. Intended ONLY for testing on devices with the established LIKAS signing key.

## Test preparation (not yet executed)
Do NOT distribute a new key as an app upgrade. Use the established .signing/wildlife-beta08.keystore, as configured in app/build.gradle.

Build on a developer machine with Android SDK 35 and Gradle 8.11.1:

\`\`\`bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
\`\`\`

Confirm output path \`app/build/outputs/apk/debug/app-debug.apk\`, package \`org.phwildlife.survey.beta08\`, versionCode 18 and versionName 0.16.0-parallax-preview. Verify APK certificate matches a previous installed working beta.

## Phone frame measurements (not yet executed)
Use a physical Android phone with a stable, known 60 Hz display refresh rate. Keep the LIKAS home hero on screen, unplug the device and let thermal conditions settle. Test in daylight, night, battery saver and reduced motion. On a 90/120 Hz display, report the actual refresh rate and expected frame cadence.

\`\`\`bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell dumpsys display | grep -i 'refresh'
adb shell dumpsys gfxinfo org.phwildlife.survey.beta08 reset
adb shell monkey -p org.phwildlife.survey.beta08 -c android.intent.category.LAUNCHER 1
# Keep the home forest hero visible and observe motion for at least 30 seconds.
adb shell dumpsys gfxinfo org.phwildlife.survey.beta08 framestats > likas_frame_stats.txt
adb shell dumpsys meminfo org.phwildlife.survey.beta08 > likas_memory.txt
\`\`\`

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

## Blocker
The currently connected GitHub writing integration did not permit creating a feature-branch CI workflow, and the tool environment has no Android SDK or Gradle installation. No new APK was compiled or retrieved in this working session. Do not publish an unverified binary or overwrite the old v0.15 release asset.

## Next
Run Gradle test/build with the established signing key, retrieve the exact new APK into the active chat, verify package/version/certificate, then deliver a direct downloadable APK link. Perform a real handset performance test before calling the animation 60 FPS validated.
