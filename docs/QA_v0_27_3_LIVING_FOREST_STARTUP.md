# LIKAS v0.27.3 RC4 | Living forest startup and wordmark-only landing
Recorded: 27 October 2026, Asia/Manila.

## Scope
**Startup screen only**. Built on `ui/likas-startup-screen-r1-20261027` (RC3).
App ID `org.phwildlife.survey.beta08`, version `0.27.3-rc4`, versionCode 49,
SQLite version 27 unchanged.

## Owner decisions
- The frog remains the approved **installed app icon**, but no frog image is rendered in the startup scene.
- Only the **LIKAS** wordmark appears, with **Explore. Record. Conserve.** beneath.
- A small version chip in the **top-right** reads the installed PackageManager version dynamically.
- Keep the two truthful actions from RC3: `Open LIKAS` and context-only `Resume survey`.
- Preserve the *exact* source day/night high-resolution WebP imagery. No generated substitutes.
- Put actual ambient motion in the Android renderer, not into a static PNG or a video containing controls.

## Source-level animation changes
- **Clouds:** four translucent cloud banks travel horizontally at different speeds.
- **Waterfalls:** streams descend at image-space coordinates .536/.627/.744, with animated foam.
- **Lake:** independent moving light ripples and expanding rings under the falls, aligned to source-image coordinates.
- **Birds:** five animated flapping silhouettes travel across the daytime sky, fading during twilight.
- **Fireflies:** eighteen drifting, pulsing lights appear after dusk, fading during daylight.
- **Sun:** mobile sun glow is placed on a device-hour-based day arc. The halo pulses gently.
- **Moon:** a blue-white halo follows the location of the original moon painted into the night panorama. No second moon disc is added.
- **Foliage:** subtle moving leaves remain near the frame edges.
- **Performance/accessibility:** existing 33ms redraw target is retained, with 66ms redraws in system battery-saver mode, reduced motion through Android's animation settings, and no offscreen continuous repaint. Halo shaders, matrices, paints, and waterfall arrays are shared across frames. The time-of-day dawn/dusk blend is preserved.
- Source motion is procedural pixel-art-compatible Canvas overlay animation, **not** true per-pixel deformation of the original WebP trees/water. Device field review decides if it is visually cohesive.

## Safety and scope boundaries
No modification to survey event creation, GPS, altitude, taxon catalogs, SQLite migrations, audio/photos, field effort, export or survey outcomes. The forest view is decorative and excluded from the accessibility traversal. `Open LIKAS` still leads to Welcome/Home, and `Resume survey` still requires an unfinished event.

## Automated checks
`scripts/test_v0273_living_forest.py` tests the renderer and startup UI source contracts. Existing branding tests remain and were updated only to reflect approved screen-specific decisions. GitHub Actions runs Python, JVM and APK builds for pull requests. Do not describe the animation as physically verified without device video/frame pacing evidence.

## Field acceptance still required
1. 320dp/360dp phones in portrait. Verify LIKAS title and tagline do not overlap top-right version chip or bottom buttons at 100%-200% text.
2. Daylight and night test: verify bird/flapping and firefly movement, waterfalls and lake ripples, clouds, sun and moon halos on actual art. Watch 2-3 full 96-second panorama loops for popping or visual discontinuities.
3. Test phone time and timezone changes across 05:15, 06:30, 17:45 and 19:15 to validate the current dawn/dusk interpolation. Current ambient clock updates every five seconds while visible.
4. Test Android `Animator duration scale: Off`, app background/foreground, and battery saver. No unwanted continuous redraw while hidden.
5. Record actual frame intervals, heat, and battery draw on a low-end phone. Compare to RC3 with similar display brightness.
6. Start, finish, resume and cancel a survey to verify the redesigned startup does not create or alter an event.
7. Confirm existing installed APK and new candidate use the same signing certificate, application ID and increasing versionCode before trying an in-place update.

No stable release claim is made until the field acceptance gates pass.

## Automated verification and direct deliverable (2026-10-27)
- PR #35, source build commit `775b9c822d178ef2634f9a5a646bb729563bc1c0`. GitHub Actions run `40537117613`: **success**. Python source tests, JVM tests and Android debug assembly passed.
- CI artifact `wildlife-survey-debug-apk` (#12748084046) contains one `app-debug.apk`.
- Verified APK filename delivered in the current chat: `LIKAS-v0.27.3-RC4-living-forest-debug.apk`, size **1,392,591 bytes**, SHA-256 **`9a51219aacc8d841ef105ea853fa7a10f9005029b94765df0794876841479fe3`**.
- Embedded manifest contains `org.phwildlife.survey.beta08` and `0.27.3-rc4`; `app/build.gradle` declares versionCode 49. Both approved 2172×724 WebP files are embedded unchanged: day SHA-256 `8b62eda698354db6d410964a342e5ccbe8cd284e3577a51fc72e4839dface76f`, night SHA-256 `a21568f109b863cb86a27b8dfae87aebfa656914252232b6c70fc0c95f1467af`.
- The APK Signature Scheme v2 X.509 certificate SHA-256 is `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, byte-identical to the certificate used by the v0.27.2 RC3 test APK. Identity/signing continuity passes, but a real older-database installation and field test remain pending.
- All motion is native Android Canvas drawing; original scene photos remain static underneath animated elements. No MP4/GIF visual preview is presented as an exact app capture.
