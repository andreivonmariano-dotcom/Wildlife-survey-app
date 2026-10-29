# LIKAS RC33 | Three-way app portal and Staurois pixel mascot QA

**Date:** 29 October 2026. **Feature branch:** `feature/likas-core-fieldwork-post-rc4`.

## User decisions

The owner explicitly prioritized improving LIKAS aesthetics before further science expansion. The app must separate **Fieldwork**, **Library**, and **Settings** after a quick startup. The owner requested short labels and minimalist original artwork for buttons. The mascot must retain the small retro pixel-art frog seen front-on with only the upper face above water, now visually referencing a supplied *Staurois natator* photograph. The pixel frog is an illustrated brand mascot, **not a verified species diagnostic photograph**.

## What changed

- `WelcomeActivity` displays a quiet forest/pond hero and three large accessible illustrated action cards. It retains a visible **Resume survey** when an active SQLite survey is present.
- **Fieldwork** retains the separate wildlife-group buttons for Birds, Herpetofauna, Mammals, Custom, project and station access, archive, dashboard, field habitat site records, audio, trap and field protocols, study templates. Back from setup/custom goes to Fieldwork, Back from Fieldwork goes to the home portal.
- New `LikasLibraryActivity` groups the offline taxon library, habitat guide, differential characters, licensed photo comparisons, conservation information and original source/checklist views. No survey starts from this library.
- New `LikasSettingsActivity` provides short Data, Backup, Storage, Import, Audit, About and **Less motion** controls. The setting is honored by both optional page animations (`UiMotion`) and the quick splash. Sensitive data deletion still uses the preexisting, separately confirmed workflow.
- New `LikasPortalUi` draws lightweight forest contours and hand-coded Canvas glyphs. No new fonts, external network data or third-party icon set is required.
- `UiKit` receives a coordinated pale sage, deep forest and pond-teal palette, softer corner radii and wider spacing. Existing detailed pages retain their original survey functions for now.
- The user-approved pixel art is stored at `app/src/main/res/drawable-nodpi/likas_natator_frog.png`. The adaptive launcher and round launcher reference the new bitmap, and the portal hero displays it. The existing originally approved `likas_frog_icon.webp` and original forest images remain in source, unchanged and cryptographically checked. New image PNG SHA-256: `0e82434278acd1d78ff84aef7c307ceb683453b8dedf7505014bd8509b257b36`. The 64×64 pixel texture is intentional for small retro launcher art.

## Checks

- New Python `test_rc33_portal_navigation.py` checks the three destinations, active survey resume, original setup and source separation, display icons and reduced motion setting. Existing Python source, taxonomic and database safety tests passed with their UI contract assumptions updated for the new architecture.
- Scientific-core GitHub Actions run **40848568986** passed on app/test commit `33e1ef461739355185ad308f6803989b54188fa6`. It included source tests, original metadata/migration checks, all 49 checksum-pinned approved iNaturalist images and Android Gradle debug compile/assemble.
- Downloaded exact GitHub artifact **12885059712**, `LIKAS-RC33-portal-ui-debug`, extracted into `/mnt/data/LIKAS-v0.27.3-RC33-portal-ui-natator-debug.apk`.
- Independent APK ZIP CRC passed. Actual APK size **9,647,716 bytes**, SHA-256 `a6d85ae79b6fa7b04360bf9be88e738411cf355221be348f9a52deb473f23dc8`. Compiled `AndroidManifest.xml` reports package **`org.phwildlife.survey.beta08`**, versionCode **93**, versionName **`0.27.3-rc33-portal-ui-preview`**. APK Signing Block v2 certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**, equal to RC32/old pinned signing identity.
- The APK physically contains `res/drawable-nodpi-v4/likas_natator_frog.png` with the exact approved 2,324-byte pixel icon fingerprint. The prior 96 priority species scientific assets and 49 pinned licensed/authorized photos remain in the build.
- **Not verified:** Android 14 320dp/200%-font emulator screenshot acceptance, TalkBack on all destinations, install-over-existing with owner survey records, external data exports, physical-camera/GPS field use, or redesign of every detailed form. This release is a **development preview**, not a field-certified version.

## Next design pass

Improve secondary screens, especially survey setup clarity, species browse cards and habitat library reading layout, with custom artwork and responsive large-font behavior. Preserve point-count effort, zero observations, complete/incomplete/not-counted survey outcomes, provenance, and existing user attachments.
