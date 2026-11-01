# APO startup screen | locked mountain-ribbon identity

**Updated:** 2 November 2026, Asia/Manila  
**Status:** Android source staged as a draft. No signed APK released or hardware QA completed.  
**Branch:** `feature/apo-startup-screen-v1-design` (derived from RC71 reference-packaged Android lineage).  
**Preview:** `APO_Startup_Screen_Concept.png` generated in the active owner chat. The preview is a static composition, not an emulator capture.

## Governing owner decision

The user explicitly locked **`forest_green_mountain_ribbon_icon.png`** in the immediately preceding chat turn. This supersedes the interim detailed rainforest/photographic mountain logo with embedded APO typography. The locked icon is a dark forest-green rounded-square canvas with a single flowing ivory mountain ribbon and a jade-green inner fold, **without lettering**.

- **Exact original file:** `forest_green_mountain_ribbon_icon.png`, 1254×1254 px, RGB.
- **Locked original SHA-256:** `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.
- **Immutable APK master file name:** `app/src/main/assets/apo_official_master.png` must be a byte-identical copy of that source.
- **Permitted technical derivatives only:** proportional resizing, Android-safe margins and alpha removal of pale *external canvas corners* for display. No change to the mountain shape, palette, fold, style, logo text, or composition. Keep the locked source unedited.
- **Current draft derivatives from this exact file:** `apo_splash_mark.png` (512×512, SHA-256 `57c03faf976f11d8e251940bb623d647a1a4ab5ff52680c35ae16d5e4bceec8a`) and `apo_launcher_foreground.png` (432×432, SHA-256 `2a60cc5cda8dba006c1c713f15d565c4599ad5ec1bcaebadc7690cb66445af00`).
- **Chat-only asset handoff:** `APO_Startup_Implementation_Assets.zip` contains the untouched original master, display derivatives and source note. These files have **not yet been committed to the GitHub repository** through the available connector.

The two previously recorded APO icon directions are historical and superseded:
(1) the earlier **textless mountain ribbon** lock from a different PNG hash and
(2) the subsequent **detailed rainforest mountain + APO wordmark** candidate with a new generated PNG.
The present original file and SHA take priority. Do not infer which PNG is approved from the old file basename alone.

## Screen composition and behavior

- Full-screen near-black forest-green background with subtle tonal variation, unobtrusive Android status/navigation bars, and no animated forest environment.
- Icon centered in the upper half of the screen's middle composition at about 208–232 dp square. It may retain its existing rounded-square background as a *locked feature* of the artwork.
- Native `APO` wordmark as a separate warm-ivory accessible TextView beneath the icon, **not baked into the PNG**. No subtitle, version chip, launch button, splash menu or onboarding at this stage.
- One-time 260 ms icon fade/translate overlapping a 230 ms wordmark fade. No looping motion. With Android animations disabled or in-app reduced-motion flag, present a static identity and navigate immediately when reference initialization permits. Tap-to-skip shortens cosmetic animation only, not data initialization.
- Automatically transition to `WelcomeActivity` Home, with its existing unfinished-survey Resume route intact. No start-screen GPS or network request.
- If checksum-verified offline installation is taking longer, reveal a small readable `Preparing offline references` status separately. On reference failure, preserve the explicit warning/continue path. Missing data is never represented as zero biodiversity or erased survey records.

## Source implementation status

- `LikasLaunchActivity.java`: updated to render `R.drawable.apo_splash_mark` and one native `APO` TextView. Background, loading-status, reduced-motion, startup navigation, worker-thread bootstrap and failure handling retained.
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
