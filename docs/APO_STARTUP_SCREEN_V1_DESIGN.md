# APO startup screen | locked mountain-ribbon identity

**Updated:** 2 November 2026, Asia/Manila  
**Status:** Android source staged as a draft. No signed APK released or hardware QA completed.  
**Branch:** `feature/apo-startup-screen-v1-design` (derived from RC71 reference-packaged Android lineage).  
**Preview:** `APO_Startup_Blended_Preview.png` generated from a pixel-preserving extraction of the locked icon in the active owner chat. The preview is a static composition, not an emulator capture.

## Governing owner decision

The user explicitly locked **`forest_green_mountain_ribbon_icon.png`** in the immediately preceding chat turn. This supersedes the interim detailed rainforest/photographic mountain logo with embedded APO typography. The locked icon is a dark forest-green rounded-square canvas with a single flowing ivory mountain ribbon and a jade-green inner fold, **without lettering**.

- **Exact original file:** `forest_green_mountain_ribbon_icon.png`, 1254×1254 px, RGB.
- **Locked original SHA-256:** `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.
- **Immutable APK master file name:** `app/src/main/assets/apo_official_master.png` must be a byte-identical copy of that source.
- **Permitted technical derivatives only:** proportional resizing, Android-safe margins and alpha removal of pale *external canvas corners* for display. No change to the mountain shape, palette, fold, style, logo text, or composition. Keep the locked source unedited.
- **Current draft derivatives from this exact file:** `apo_splash_mark.png` (borderless mountain only, 1000px wide, SHA-256 `a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc`) and `apo_launcher_foreground.png` (432×432, SHA-256 `2a60cc5cda8dba006c1c713f15d565c4599ad5ec1bcaebadc7690cb66445af00`).
- **Chat-only asset handoff:** `APO_Startup_Borderless_Assets.zip` contains the borderless startup mountain and README. The unchanged approved original master is independently preserved as `forest_green_mountain_ribbon_icon.png`; `APO_Startup_Implementation_Assets.zip` is an earlier superseded border-retaining export. These files have **not yet been committed to the GitHub repository** through the available connector.

The two previously recorded APO icon directions are historical and superseded:
(1) the earlier **textless mountain ribbon** lock from a different PNG hash and
(2) the subsequent **detailed rainforest mountain + APO wordmark** candidate with a new generated PNG.
The present original file and SHA take priority. Do not infer which PNG is approved from the old file basename alone.

## Screen composition and behavior

- Full-screen near-black forest-green background with subtle tonal variation, unobtrusive Android status/navigation bars, and no animated forest environment.
- Startup emblem is only the mountain ribbon with a transparent field, sized proportionally at 180–276 dp width (roughly 0.55 height/width), so the logo flows into the forest-green screen without a tile, square or visible border. The **separate home-screen launcher icon** remains the exact locked rounded-square artwork.
- Native `APO` wordmark as a separate warm-ivory accessible TextView beneath the icon, **not baked into the PNG**. No subtitle, version chip, launch button, splash menu or onboarding at this stage.
- One-time 260 ms icon fade/translate overlapping a 230 ms wordmark fade. No looping motion. With Android animations disabled or in-app reduced-motion flag, present a static identity and navigate immediately when reference initialization permits. Tap-to-skip shortens cosmetic animation only, not data initialization.
- Automatically transition to `WelcomeActivity` Home, with its existing unfinished-survey Resume route intact. No start-screen GPS or network request.
- If checksum-verified offline installation is taking longer, reveal a small readable `Preparing offline references` status separately. On reference failure, preserve the explicit warning/continue path. Missing data is never represented as zero biodiversity or erased survey records.

## Source implementation status

- `LikasLaunchActivity.java`: updated to render the **borderless** `R.drawable.apo_splash_mark` at responsive proportional dimensions, one native `APO` TextView and a native top-to-bottom forest gradient. Loading-status, reduced-motion, startup navigation, worker-thread bootstrap and failure handling retained.
- `AndroidManifest.xml`: drafts Android display label `Apo`, icon names `@mipmap/apo_launcher` and `@mipmap/apo_launcher_round` while preserving Android application ID.
- `app/build.gradle`: draft versionCode **132**, `0.27.3-apo-startup-locked-ribbon-preview`, checks master and derivative SHA-256 values. A build with missing/unapproved icon files must fail instead of falling back to an older picture.
- XML adaptive icons and `scripts/test_apo_startup_locked_icon.py` are staged in the same branch.
- Inherited parent: RC71 `org.phwildlife.survey.beta08`, preinstalled Mindanao PAIC 714 reference plus ESA WorldCover, and SQLite **28** migration path. No survey data schema or sampling semantics changed by the startup edits.

## Required final integration and validation

Place the exact PNGs from the handoff ZIP at:
`app/src/main/assets/apo_official_master.png`,
`app/src/main/res/drawable-nodpi/apo_splash_mark.png`,
`app/src/main/res/drawable-nodpi/apo_launcher_foreground.png`.
Check SHA fingerprints before committing.

Then run the full Python/JVM/Android build pipeline on the RC71-derived branch, including reference checksum gates, package/version/signature checks and Android 14 small-display/large-font/reduced-motion checks. Confirm adaptive masks do not crop the ribbon. Verify startup loading/failure/reopen paths and install-upgrade behavior on an actual device with historical survey records. Only after verifying a current APK should a direct in-chat downloadable APK be provided.

**Current blockers:** binary PNG transfer to GitHub and signed build/artifact retrieval have not occurred in this chat. The container does not expose an Android SDK/Gradle executable or a network Git clone. There is no verified APK for this source stage. This document is a precise record of the source/design state, not proof of a field-ready release.

## 2 November 2026 | Accepted borderless blending update

- The owner requested removing the square icon border and matching the mountain emblem to the startup background. The **locked launcher master remains unchanged** (SHA-256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`).
- Pixel-source extraction took the original cream-and-jade mountain image and masked its surrounding dark green tile. The foreground RGB values are retained from the locked source, with a small edge antialias. A standalone transparent mountain derivative is `apo_mountain_ribbon_transparent.png` (SHA-256 `5f183a0c05e43fde8ecc9b15e4bdbc777d9418c7c3d1f77b31e5ed1c472ac382`).
- Android resource delivery: rename the exact byte content of `apo_mountain_splash_1000.png` (SHA-256 `a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc`) to `app/src/main/res/drawable-nodpi/apo_splash_mark.png`. The Gradle SHA gate and the Python contract now expect this hash. The unchanged launcher foreground derivative remains independently checksum-gated.
- `APO_Startup_Blended_Preview.png` illustrates the new composition and `APO_Startup_Borderless_Assets.zip` contains the prepared launch images and notes. This is a static mockup, **not** an Android screenshot or signed APK. Files are chat-attached only; binary transfer and CI signing remain pending.

## 2 November 2026 | Final gradient-only startup background update

The owner requested **removing every scenic silhouette**. The approved startup background is now a smooth, static **two-stop vertical green gradient only**. There are no mountains behind the icon, trees, ridgelines, clouds, mist, fireflies, landscape image layers or visible borders.

- Native Android `LikasLaunchActivity.java` uses `GradientDrawable.Orientation.TOP_BOTTOM` with **top `#0B4B39` / RGB(11,75,57)** and **bottom `#021918` / RGB(2,25,24)**. Status bar matches the top, and navigation bar matches the bottom. The gradient is drawn by Android, so no image-background file or bitmap scenery is bundled.
- Foreground remains the **same source-derived, transparent mountain ribbon** `R.drawable.apo_splash_mark` (SHA-256 `a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc`) with a separate accessible ivory `APO` TextView. The **locked launcher master remains untouched**, original SHA-256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.
- `APO_Startup_Pure_Gradient_Preview.png` is the static preview. Local pixel-level QA checked that its uncovered left/right positions match exactly and that unobstructed rows conform to a simple top-to-bottom linear RGB interpolation with max observed deviation of 0 digital levels. This is preview QA, not a device/emulator screenshot.
- The Python source contract now checks the exact gradient colors, two-stop initializer, matching navigation-bar color and absence of a drawable scenery background. Full Android CI, signing, launch, font scaling, data-preserving upgrade and physical device testing remain pending.
- A self-contained `APO_Gradient_Only_Android_Asset_Overlay.zip` was prepared in this conversation with exact Android resource paths for the preserved master, transparent mountain drawable and launcher foreground plus the preview. Its ZIP-integrity and internal file-byte checks passed. It is **not** a compiled APK, and the overlay has not been committed to GitHub. Continue only on the RC71-derived `feature/apo-startup-screen-v1-design` branch to preserve existing survey data and source checklist assets.

## 2 November 2026 | Final owner lock: gradient startup and motion

The owner expressly approved the **locked mountain-ribbon launcher icon** and the **plain gradient-only startup screen**, and requested implementation and an APK. No alternative shape, color palette, illustration, image-generated reinterpretation or background silhouettes are authorized. Keep original `forest_green_mountain_ribbon_icon.png` unchanged (SHA-256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`). The startup-only transparent source-derived mountain remains SHA-256 `a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc`.

### Motion contract, implemented in Java source

- Top-to-bottom native `#0B4B39` → `#021918` static background; native Android system bars match its ends. No bitmap background, silhouettes, particles or loops.
- Fade and translate the **mountain image** into place over **380 ms** using `DecelerateInterpolator(1.5f)`. Fade and translate the separate accessible `APO` TextView with **90 ms start delay** and **320 ms duration**, `DecelerateInterpolator(1.4f)`. The motions overlap. A tap skips cosmetics, not required reference installation.
- When references are ready, fade/translate the identity out over **190 ms** with `AccelerateDecelerateInterpolator`, then enter `WelcomeActivity` Home with Android fade-in/fade-out. Do not add a fixed hold timer. If the application reference bootstrap remains in progress, show truthful `Preparing offline references` and continue when complete. Preserve the existing explicit failure warning.
- With `ValueAnimator.areAnimatorsEnabled()==false` or `likas_ui/reduced_motion==true`, skip logo/wordmark motions **and** disable activity transition via `overridePendingTransition(0,0)`. Keep accessible image/wordmark labels and cancellation on Activity teardown.
- This is isolated startup design/source work, without changes to original Android package ID, previous signing certificate, schema28, the 714-species checklist, WorldCover resources, stored observations, GPS or exports.

### Build gate and honest release state

Updated source and Python source-contract assertions are staged on `feature/apo-startup-screen-v1-design`, versionCode 132. The owner-provided exact original and derived PNGs exist in the **chat-local** ZIP `APO_Gradient_Only_Android_Asset_Overlay.zip`. Original and derivative SHA-256 are enforced by `verifyApoArtwork` in `app/build.gradle`; missing files must fail rather than silently falling back to an old icon. GitHub repository currently does **not** contain those binary files, and the current runtime has neither a usable Android SDK/Gradle nor outbound access to clone the private repository. **No new APK has been built or tested**. Full Android CI, APK signing continuity, exact embedded art checks, historical-survey installed upgrade and direct APK delivery remain outstanding. Do not treat this code/documentation lock as evidence of a shipped build.
