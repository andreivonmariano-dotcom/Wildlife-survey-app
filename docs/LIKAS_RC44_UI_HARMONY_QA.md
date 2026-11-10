# LIKAS RC44 | Rounded icon and forest palette UI audit

**Date:** 30 October 2026 (Asia/Manila)
**Scope:** visual presentation and nonblocking navigation only.
**Source branch:** `feature/likas-rc44-ui-harmony`, PR #41 into `feature/likas-core-fieldwork-post-rc4`.
**Target:** `0.27.3-rc44-ui-harmony-preview`, versionCode **104**, unchanged package `org.phwildlife.survey.beta08` and SQLite 27.

## Owner request and implementation

Source: owner-supplied RC43 screenshots of Home, Fieldwork, Library and Settings.

- **Corner treatment:** `LikasPortalUi.addApprovedIcon()` sets `FrameLayout.setClipToOutline(true)` against rounded holders. Icons are not edited or regenerated. Pixel filtering stays off. Dedicated frame sizes: large 64dp / mini 52dp / compact action 50dp. Top-right frog *decorative hero image* is rounded separately; the approved launcher source is untouched.
- **Colors:** shared `UiKit` deep forest, canopy, moss, leaf and warm background tokens now drive Home, Fieldwork, Settings, Library cards, supporting outlines and hero/session gradients. Red/amber/blue semantic states are not forcibly recolored, and all species imagery retains its own approved palette.
- **Less visual clutter:** Third survey-group Mammals becomes a compact horizontal action instead of an oversized one-column 126dp card, keeping Birds and Herpetofauna as two-column cards. Data check uses the existing approved Reports glyph for recording audit. Unapproved shield/conservation and audio glyphs retain functional vector fallbacks within harmonized rounded holders; no new illustration is claimed to be owner-approved.
- **Motion:** Brief 155ms Home, Fieldwork and setup page entrance fades, 160ms optional Field tools and Study rules reveals, immediate hide with cancellation. Honors Less motion preference, Android system animator setting and large-font accessibility. The field recording screen, save/end controls and GPS flows were not rewritten.
- **Safety:** 18 approved icon PNGs remain identical under `scripts/test_rc43_approved_icon_lock.py`; no new icons, no plant/invertebrate icon, no Custom Survey creation. Method, survey event, observation, archival, signing, library and telemetry code not changed.

## Validation gates

- Run `python3 -m unittest discover -s scripts -p 'test_*.py'` and Gradle `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`.
- CI must check original signing certificate and approved licensed photos, and include signed APK only on successful workflow.
- Verify APK binary versionCode 104, signing SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, all 18 PNG hashes and offline data.
- Device checks *still needed*: 320dp phone layouts, 200% fonts and TalkBack, press and scroll responsiveness, Reduced-motion switch, update over RC43 with active and historical records, battery/field GPS and backup restore.
- These pending field checks prevent labeling this preview production-ready.

**Audit status:** source implementation is committed. Automated CI and signed APK results must be appended only after observed success; this file alone is not proof of compilation or on-device behavior.

## Verified signed RC44 preview | 30 October 2026

- **Source code tested:** `31f04de4b4041d1c18277168b7ca4c1727eb19ea`. Subsequent pending changes on the development branch were documentation-only.
- **Scientific Core QA run:** **41006102621** (push) **success**. **292 Python tests passed**, including original RC43 exact 18-icon byte/hash locks, the RC44 rounding/palette/motion contract and historical survey contracts. Gradle `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` **BUILD SUCCESSFUL**. Instrumentation was compiled but not executed on an emulator or phone. Artifact **12955084788** (`LIKAS-0.27.3-rc44-ui-harmony-preview-code104-debug`).
- **Verified binary:** `LIKAS-v0.27.3-RC44-ui-harmony-debug.apk`, **9,777,896 bytes**, SHA-256 `0f4c710bd91144e0115d2d71cbe562d1b3ecfd6a3db299c9dfc5f127d56c471e`. Valid APK ZIP and v2 signature, signed-content digest verified independently; certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` matches RC43. Binary manifest confirms `org.phwildlife.survey.beta08`, versionCode **104** and versionName `0.27.3-rc44-ui-harmony-preview`.
- **Immutable approved assets:** All **18** `res/drawable-nodpi-v4/likas_approved_*.png` in the signed APK have the approved pack's SHA-256 digests. Compiled launcher resources and all pre-existing image files are **byte-for-byte identical** to the verified RC43 APK. No asset was regenerated or recolored.
- **Offline references:** **58** bundled JPGs and `assets/ph_mammals_mdd_v2_5.json` confirmed present.
- **Delivery:** APK extracted from the exact successful CI artifact and placed at `/mnt/data/LIKAS-v0.27.3-RC44-ui-harmony-debug.apk` in the current conversation. Do not assume this path exists in another chat without checking.
- **Unverified:** physically installing/upgrading RC43 with saved data, 320dp/large-font/TalkBack, transition smoothness on phone, real fieldwork and GNSS, and fault-injected partial backup recovery. Do not call this production/field-accepted.

## Active-branch merge and reproducibility confirmation

PR **#41** merged into `feature/likas-core-fieldwork-post-rc4` at **`ea0efb15aa1ad53ab520cd9619c44429e4e6226a`**. The subsequent **push-triggered** Scientific Core QA run **41006647041** completed **success** and uploaded signed artifact **12956325239**. The APK downloaded from this active-branch artifact was compared byte-for-byte with the delivered RC44 APK. **Identical:** SHA-256 `0f4c710bd91144e0115d2d71cbe562d1b3ecfd6a3db299c9dfc5f127d56c471e`, 9,777,896 bytes.

This confirms that the directly delivered RC44 APK matches the code now merged into the authoritative active development branch. Documentation-only commits after the merge do not change application code or binary assets. Device upgrade and visual accessibility testing remain unverified.
