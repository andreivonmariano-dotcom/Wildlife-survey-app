# APO v132 (RC72) | Signed gradient startup APK release QA

**Recorded:** 2 November 2026, Asia/Manila  
**Branch:** `feature/apo-startup-screen-v1-design` (draft PR #74)  
**Exact built source commit:** `4ce953bbea100c1c6a90231434591ffb6f99f788`  
**CI:** GitHub Actions `APO locked-icon startup v132 signed APK` run `41332798258`, **SUCCESS**.  
**CI artifact:** `APO-v132-gradient-startup-signed`, artifact ID `13093373339`.  
**Verified direct-download file in active ChatGPT conversation:** `APO-v0.27.3-RC72-gradient-startup-code132.apk`. A conversation-local sandbox path is not a permanent external URL.

## APK identity and exact file

- Android `applicationId`: `org.phwildlife.survey.beta08` (unchanged).
- `versionCode 132`, `versionName 0.27.3-rc72-apo-gradient-startup`.
- APK size: **11,741,864 bytes**.
- APK SHA-256: **`4a4210ff77aa40cf8c3f7c0741ddad5baeabe17d54ac93dbfaa62810e78cdfeb`**.
- Original v0.8 debug/beta signing certificate SHA-256 (keystore-exported X.509 DER): `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. The CI independently called `apksigner verify --verbose --print-certs` against the final APK and matched that fingerprint.
- The packaged APK passed full ZIP integrity verification in CI and independently in the chat runtime.

## Verified artwork and references inside the built APK

The approved original file is in the APK under `assets/apo_official_master.png`, **1,503,328 bytes**, SHA-256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.

Android compiled the following visual resources without changing their PNG bytes:
- `res/drawable-nodpi-v4/apo_launcher_foreground.png`: **107,583 bytes**, SHA-256 `2a60cc5cda8dba006c1c713f15d565c4599ad5ec1bcaebadc7690cb66445af00`.
- `res/drawable-nodpi-v4/apo_splash_mark.png`: **148,937 bytes**, SHA-256 `a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc`.

Both public reference assets were independently checked in the APK:
- `assets/paic714_master.csv`: **230,291 bytes**, SHA-256 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`.
- `assets/paic_worldcover2020_500m.txt`: **149,412 bytes**, SHA-256 `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.

The source retains the previously verified SQLite schema **28** and no project task changed survey data access, outcome storage, or database schema. No existing on-device data were used in this build.

## Startup behavior implemented in code

The background is a native, stationary `#0B4B39` to `#021918` vertical gradient. The borderless source-derived mountain ribbon and separate `APO` wordmark appear with 380 ms/320 ms eased entrance animations, staggered by 90 ms. A 190 ms eased exit precedes Android fade into `WelcomeActivity`. No artificial hold timer. Both entrance and activity animation are disabled under reduced-motion settings. The existing checksum-based offline reference bootstrap, loading message, explicit error/Continue dialog, and Home/Resume navigation remain present.

## Tests and remaining field gates

- **380 Python source/scientific regression tests passed** after updating historically pinned LIKAS branding/version assertions to the new owner-approved APO identity. Scientific data source assertions were not weakened.
- The GitHub Actions run passed the Python regression stage, `prepare_mdd_mammals.py`, approved offline photo preparation, JVM unit tests, `:app:assembleDebug`, and `:app:assembleDebugAndroidTest`.
- The CI checked package ID/version via `aapt` and verified original signer via `apksigner`.
- APK resource files and included reference files were independently rehashed after downloading the CI artifact in chat.

**Still unverified:** Actual install/upgrade on a physical handset with existing RC71 survey records, sensor/GNSS behavior, historical field-record backup/restore, reduced-motion device UX, TalkBack and screenshot comparison on small/large-font screens. A successful signed build supports an in-place upgrade from RC71 in principle because appId and signing identity match, but real-device data migration is not claimed tested. Release remains a verified signed **development/field-preview APK**, not a field-certified stable release.
