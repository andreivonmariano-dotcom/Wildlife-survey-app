# APO RC73 | Signed text-only Home preview QA

**Date:** 2 November 2026 (Asia/Manila)
**Branch:** `feature/apo-text-home-v133` (draft PR #75 into `feature/apo-startup-screen-v1-design`)
**Exact source commit built:** `ac75b6abe1af2b5e3e5c758a1bdec86f075fc9c1`
**GitHub Actions:** `APO RC73 text-only Home signed preview`, run `41373151540`, **SUCCESS**.
**CI artifact:** `APO-v133-text-home-signed-preview`, artifact ID `13110156812`.

## Direct-delivered APK identity

- Filename: `APO-v0.27.3-RC73-text-home-code133.apk`.
- Byte length: **11,744,944**.
- SHA-256: **`c21fbc5ba9d9b4bbc9e455148d680609f21974d2f53523b35ddbbb4a9eb2125d`**.
- Gradle/Android ID: `org.phwildlife.survey.beta08` (unchanged).
- `versionCode 133`, `versionName 0.27.3-rc73-apo-text-home`.
- Verified unchanged signer certificate SHA-256: `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- Runtime downloaded the exact GitHub Actions artifact into the current conversation, extracted the APK, rehashed it, and verified full APK ZIP integrity. The direct link is conversation-local and must not be reused from another chat.

## UI implementation

Main Home uses `ApoHomeUi.java` rather than legacy illustrated Home hero/tiles. It presents **BioTools** (existing survey/history/analysis), **PHLibrary** (existing species/habitat library), **PHMaps** (existing site/station and habitat record tools; full geospatial atlas unimplemented), and **BioDiscover** (research and conservation feed clearly marked in development). Header **Settings** is an accessible text control. Conditional **Resume current survey** remains; no second fieldwork-centered Home hero.

The Home entrance fades and moves 6dp over 220ms when Android animators and Less motion permit. No generated images or icon glyphs are displayed in the Home module rows. Historic resources remain for nested screens. The owner-locked launcher and gradient startup are unchanged.

## Verification

- **385 Python scientific and source-regression tests passed**, after replacing obsolete three-card LIKAS Home assertions with explicit four-module navigation checks; scientific/field capture checks remain in place.
- JVM unit tests and Android `:app:assembleDebug` plus `:app:assembleDebugAndroidTest` passed.
- CI `aapt` verified unchanged package ID and new version.
- CI `apksigner` verified existing signer certificate. Complete APK archive integrity checked again after download.
- Bundled approved APO original master image SHA-256: `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.
- Bundled owner-authorized 714-species PAIC roster SHA-256: `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`.
- Bundled ESA WorldCover-derived map SHA-256: `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.
- These three embedded asset hashes were independently checked after downloading the APK.

## Unverified field-release boundaries

No real-phone install/upgrade from RC72, archived survey migration test, field GPS/GNSS test, active survey lifecycle on-device test, TalkBack test, reduced-motion device review or small-phone 200% font screenshot acceptance was conducted for RC73. Although unchanged package and signer permit an in-place Android upgrade in principle, preservation of actual existing survey data has not been verified on a handset. Draft PR #75 remains open, unmerged. The APK is a signed **development preview**, not field-certified stable.
