# LIKAS RC71 | Publicly embedded Mindanao PAIC 714 and WorldCover map

**Date:** 2026-11-01 (Asia/Manila). **Branch:** `feature/likas-rc71-public-preloaded-paic-references` (draft PR #72, based on RC70). **Application ID:** `org.phwildlife.survey.beta08`. **Version:** `0.27.3-rc71-preloaded-paic714-worldcover` (code 131). **SQLite schema:** 28 with the RC69 27→28 migration; historic fieldwork remains separate from reference replacement.

## Product-owner data authorization and data license

Owner explicitly authorized publishing their **harmonized 714-species Mindanao PAIC checklist** in the public GitHub source and distributable Android APK. It contains 408 bird, 53 amphibian, 154 reptile and 99 mammal entries; the 11 audited omissions are absent. No new occurrence assertions were added. The original references and scientific provenance still deserve acknowledgement as documented in the owner’s master and research record.

The bundled 16-tile `paic_worldcover2020_500m.txt` derives from **ESA WorldCover 2020 v100** (original 10 m dataset), grouped to approximately 500 m land-cover cells for lightweight offline recommendations. ESA WorldCover 2020 is licensed **Creative Commons Attribution 4.0 International (CC BY 4.0)**. Attribution is shipped in `app/src/main/assets/PAIC_WORLD_COVER_ATTRIBUTION.txt` and accessible from the Habitats & Species screen. Original dataset: Zanaga et al. (2021), DOI `10.5281/zenodo.5571936`; primary license: `https://esa-worldcover.org/en/data-access`. Required credit: © ESA WorldCover project 2020 / Contains modified Copernicus Sentinel data (2020) processed by ESA WorldCover consortium. The derived map is historical land cover, not remotely sensed field confirmation, and no habitat is auto-confirmed.

## Exact public source inputs

- `app/src/main/assets/paic714_master.csv`: **230,291 bytes**, SHA-256 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`. Exact 714-row/26-column source, 715 CSV physical lines.
- `app/src/main/assets/paic_worldcover2020_500m.txt`: **149,412 bytes**, SHA-256 `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`. Exactly 16 independently compressed 600×600 grids. ESA-derived map, not a current 10 m ecological habitat layer.
- Both inputs are committed publicly with original bytes and validated by Gradle, Python regression checks and GitHub Actions. No private overlay is required for code131.
- `BundledPrivateReferences` retains the existing stable bootstrap implementation name but is now invoked automatically against public `app/src/main/assets` files. Original SHA-256 input checking is mandatory; startup installs the master/map on first launch without file pickers. Existing master/map are preserved when already active. No survey or photo archive is silently removed. Any startup failure is disclosed to the observer.

## Signed build and direct user deliverable

- Dedicated **RC71 CI run `41322915705` SUCCESS** on exact app, data and test source SHA `b5690095ddf8035a35aca21fd9db3c019a0f775c`. Scientific + source regressions (**377 Python tests**), JVM unit tests, Android app and AndroidTest APK compilation, prebuilt source asset SHA checks, inside-APK byte-for-byte SHA checks, AAPT package identity/version, Android `apksigner` original certificate verification and ZIP CRC all passed.
- **Verified public installable artifact:** `LIKAS-v0.27.3-RC71-PAIC714-WorldCover-code131.apk`, **9,979,480 bytes**, SHA-256 **`76620a2ae6d102012fc7e08740049e2e562f262e01f462bf63dd46e023ccbe6d`**. Downloaded through the connected GitHub workflow artifact and independently extracted to a ChatGPT direct-download path. Verified `AndroidManifest.xml`, `classes.dex`, 714 master CSV SHA, 16-tile map SHA, and attribution text inside this exact APK. Original signer SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` checked in dedicated CI.
- The user received a direct APK link in conversation, not an Actions page. Asset ZIP and individual imports are no longer required for normal RC71 use.

## Remaining release/field constraints

This signed APK is a **development build** with final reference packaging. No actual Android handset/emulator tests were run to verify startup auto-activation, a 27→28 upgrade against real historical surveys, backup/restore, all habitats by location, GPS field behavior, TalkBack, 200% font scaling, or species selection during active survey. Do not call it certified for production field deployment until those pass.

The historical trusted RC4 lineage gate still rejects the literal schema28 constructor while checking the older schema27 string. This is a governance gate issue that must be reviewed and modernized before merging all development PRs; do not introduce a false schema marker to evade it. PR #72 remains in draft on the active development line and source is public.

**Affected requirements:** G-01/02/03/04/05/09/10/11/12, S, L, T, H, D, U, C, E.
