# APO | Startup screen v1: implementation-ready design proposal

> **2 November 2026 revision following owner-supplied image:** The owner now supplied a detailed Mount Apo rainforest / APO lettering icon and requested high-quality regeneration without white outer edges. The earlier textless ribbon-icon lock in this document is **superseded for the current candidate**. A refined 1254×1254 RGBA PNG was produced in the owner chat at `APO_Icon_Refined_Transparent_Edges_1254px.png`, SHA-256 `6b576763069bebdab87cb0676a028875a18518e655c127881f89e8faf51bfa46`. The derived 512×512 file is `APO_Icon_Android_512px.png`, SHA-256 `d26f245bc85f2aeb2f0f43b00d283cef384dacfc0cbf27259a54ae7f3dd99723`. The raster image includes the text **APO**: the startup should use a single centered icon without a second text wordmark. Source and launcher metadata have been staged in a protected draft branch, but image binaries have **not** been transferred to GitHub and no Android APK can yet be compiled or delivered. Remaining sections below describe the preceding proposal and should be treated as superseded wherever they conflict with this dated amendment.


**Prepared:** 2 November 2026, Asia/Manila  
**Status:** Design proposal for owner review. No Android code, approved icon binary, APK, or launcher metadata has been changed by this document.  
**Source branch:** `feature/apo-startup-screen-v1-design`, derived from the owner-approved APO branding lock `feature/apo-minimal-brand-design-lock` at `1e1bcaa976ab0333e087719b590259513567438f`.  
**Relevant rules:** G-07, U-03, U-04, U-06, E-01, CC-01, DL-01. Use the latest master requirement wording at implementation time.  

## Design intent

The opening is a short, calm transition into an offline biodiversity tool for Philippine biologists, not a separate landing page. The approved name is **Apo**, represented as uppercase **APO** in the wordmark. Do not print a subtitle that reduces the platform to fieldwork. Do not bring back the old frog, retro forest plate, waterfall, fireflies, pixel art, or attention-heavy animations.

The **exact** approved textless mountain icon is locked in `docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md`, original source `a_clean_modern_minimalist_logo_app_icon_on_a_rou.png`, 1254 x 1254 RGB PNG, SHA-256 `eae6582c90ff8bbe394a795763aacb68da40ca9a81acc79ec587b2c227e9c284`. The original binary has **not** been found in the current GitHub branch or current chat's attached assets. Do not replace it with a merely similar generated logo. Hold icon-bearing APK implementation until the actual master is available and hash-checked.

## Proposed startup composition

- A full-screen near-black forest-green field with subtle tonal depth. Sample production colors from the approved original image, not from an invented palette. Avoid an animated background, fake light shafts, gradient banding and particles.
- Center one vertical identity unit slightly above the viewport midpoint: the approved mountain mark at approximately 112–128 dp visual container width, followed by a 16–22 dp breathing gap and standalone **APO** wordmark. Preserve the mountain silhouette's negative space and Android adaptive-icon safe margins. Do not crop the rounded artwork into a new shape without inspecting the original.
- Wordmark: warm-ivory text, native sans-serif with restrained tracking, approximately 38–44 sp at normal type scale. Do not bitmap-render text into the image; use an accessible TextView. Allow scaling/clamping on 320 dp phones and in landscape rather than clipping or forcing small text.
- No version number, slogan, launch button, onboarding carousel, GPS prompt or feature tiles. Android opens Home directly. Home continues to own Resume Survey.
- Only when first-launch reference installation actually delays entry: show **Preparing offline references** as separate, small, readable, live-status text near the bottom safe area. Do not show a fake percentage or indefinite looping spinner. Avoid animating the APO wordmark into a status message.

## Motion and launch timing

- Normal device animation settings: a single understated mountain-opacity/translation reveal over roughly 220–300 ms, then a wordmark fade/translate of roughly 180–220 ms, with visual overlap. Use hardware-accelerated property animation. Any optional outline/trim-path effect may be added only if it faithfully derives from the approved art instead of drawing a new mountain.
- Navigate to Home as soon as essential offline initialization permits, with a short crossfade. No minimum splash timer and no decorative wait. If loading is already complete, do not hold the user to finish an animation. Tap-to-skip can complete the transition, but cannot bypass required reference-state reporting.
- Honor `ValueAnimator.areAnimatorsEnabled()`, Android animator scale, and the existing in-app `reduced_motion` preference. With motion reduced, show the static identity and continue as soon as references are ready.
- Do not start GPS, network fetching, audio, survey capture, or animation loops during startup. Stop pending animation callbacks on Activity teardown.

## Truthful initialization, safety and failures

The current `LikasLaunchActivity.java` starts `BundledPrivateReferences.ensureInstalled` on a worker if a packaged reference bundle is present. Preserve its no-main-thread-install behavior, preinstalled PAIC 714/WorldCover checksum gates, survey history, and explicit reference failure dialog. Change the foreground screen only, not the database schema, observation ownership or survey outcomes.

- First launch with reference work unfinished: identity remains visible and the status appears after a brief delay, or at the moment the normal reveal ends, whichever prevents flicker.
- On success: dismiss the status and proceed to `WelcomeActivity`, preserving Home/Resume.
- On failure: state that references were not installed and offer the existing explicit **Continue** route so pre-existing surveys remain accessible. Do not imply missing references equal no recorded wildlife or no survey records.
- On process recreation/back/rapid reopen: avoid double navigation, stale callbacks or duplicate installation UI.

## Integration plan after exact source recovery

1. Hash-check original icon; preserve master immutable. Generate traceable Android derivatives under `apo_` names, inspect small launcher masks, and update adaptive foreground/round icon plus accessible app label **Apo** without changing `applicationId org.phwildlife.survey.beta08` or the pinned signer.
2. Replace the existing `LikasLaunchActivity` LIKAS typography with the two-layer approved mark + APO TextView layout, implementing the launch states above. Reuse launch/bootstrapping behavior rather than creating another Android splash activity.
3. Advance versionCode strictly above RC71's **131**, retain the already established SQLite **28** migration path, and keep the owner-authorized public 714 checklist and ESA WorldCover bundled references unchanged.
4. Add source/JVM and Android tests for initial load, instant reopen, delayed bootstrap, reduced motion, missing/failing references, safe-area/font scaling and exact icon/packaged-reference SHA checks. Test 320/360/412 dp at 100%, 130% and 200% type sizes, launcher squircle/round masking and physical-phone upgrade with historical survey data.
5. Build and verify exact APK package, versionCode, embedded approved artwork, reference checksums and original signing certificate. Deliver a **direct in-chat APK file** per DL-01. A GitHub Actions page is not the deliverable.

## Current baseline and nonclaims

On 2 November 2026, GitHub default `main` HEAD was `44f3c5a76455c6532a74c1c6e5b5c2d7218f908f`, but it is an obsolete v0.15 Android baseline. The APO branding draft is based on RC71 `feature/likas-rc71-public-preloaded-paic-references`, versionCode **131**. In that draft's current source `LikasLaunchActivity`, the splash still renders a centered **LIKAS** wordmark with an animation and handles reference bootstrapping. Its `AndroidManifest.xml` label still says **LIKAS** and launcher icon still resolves to `@mipmap/ic_launcher`. The new APO icon and startup have **not** shipped. No build, signing, emulator or physical-phone test has been run for this design document.
