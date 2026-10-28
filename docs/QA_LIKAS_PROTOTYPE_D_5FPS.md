# LIKAS Prototype D: 5 FPS sprite player staging and visual QA
Recorded 28 October 2026, Asia/Manila.

## Governing source
- Current owner request: Q1 refine the four waterfall sprite sheets individually; Q2 clean duplicated static elements; Q3 write an Android sprite player using independently animated 5 FPS components, phone-local day/night transition and battery-aware playback.
- Requirements: U-01 through U-06, G-05, G-10, E-03 through E-05, CC-01 and DL-01.
- Source branch: feature/v0.27.4-layered-sprite-player-stage, stacked on feature/v0.27.3-startup-living-forest.
- App identity and SQLite untouched: org.phwildlife.survey.beta08, schema 27. Current build version remains 0.27.3-rc4 because no new APK containing these assets has been built.

## Implemented in local downloadable Prototype D asset pack
- Source C 28 components and 760 PNG keyframes retained at exactly 5 FPS (one frame every 200 milliseconds).
- All four waterfalls in each lighting state received separate downstream-moving pixel water strands, secondary foam and splash keyframes inside the original alpha masks. Original water silhouette and textured water source are retained, avoiding the previous flat blue repaint.
- Small sky bird markings on the static plates cleaned via localized pixel repair. Distant mountain mist and some source canopy shape remain static. This is **partial background cleanup**, not a full clean-room reconstruction of every moving shape.
- An independently assembled 1,488,772-byte WebP atlas archive includes four fixed day/night images, 28 atlas files and a JSON component manifest.
- Full editable asset archive: LIKAS_Layered_Sprites_5FPS_Prototype_D.zip (9,349,505 bytes; SHA-256 36ce19761297e07a206c9efc8112199d90c3e2846f63130e54a0ed90c3ef2f5d).
- Android asset archive SHA-256 def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498. Contents are downloadable in this chat; **binary assets have not been committed into GitHub**.
- Standalone reproducibility scripts: rebuild_assets.py and validate_assets.py. Validated 28 groups, 760 frame files and transparent alpha. No source image is moved/warped as a whole.

## Android implementation staged in GitHub
- app/src/main/java/org/phwildlife/survey/LikasSpriteForestView.java
- Android API 26+ View reads ZIP from app assets, uses 432×756 nearest-neighbor compositing, decodes independent WebP atlas cells with BitmapRegionDecoder on a HandlerThread, and swaps frame bitmaps under a lock.
- Local time: night 18:30–05:30, dawn crossfade 05:30–06:30, day 06:30–17:30 and dusk crossfade 17:30–18:30.
- Reduced motion and battery saver stop 5 FPS redraw, retain a fixed sprite frame, and check local time every 15 seconds. Visibility and detachment stop background playback.
- Frog launcher art remains unchanged; LIKAS title, tagline, version chip, Home and Resume remain native controls.
- **Integration intentionally not enabled in LikasLaunchActivity** until the matching sprite archive is committed into Android assets, its ZIP format is tested with Android's actual decoder and on-device test passes. Connecting the class now without its images would create a blank background.

## Release gates and open visual issues
1. Test actually decoding compressed WebP atlas subregions on API 26, 29 and 34. Inspect color, alpha, region bounds and source PNG fidelity.
2. Test 320dp/360dp portrait layouts and scaling. Confirm no button clashes with status/nav bars.
3. Check the four waterfalls and cloud/canopy masks in motion on a physical phone. Deep source mist and some canopy imagery remain painted into static backdrop, which can still look doubled.
4. Measure CPU use, memory, dropped frames and battery saver behavior in daylight and dawn/dusk simultaneous mode.
5. Confirm zero change to survey lifecycle, exact original database, photo/audio evidence, taxonomy or research ZIP exports.
6. Commit the versioned binary sprite archive to the private repository; switch startup to the new view; increment versionCode and verify persistent signing before producing a directly downloadable test APK.
7. No Android APK containing Prototype D has been compiled or installed. Compilation of the unconnected class does not prove media playback works.

Do not advertise the current RC4 APK as including this layered scene.
