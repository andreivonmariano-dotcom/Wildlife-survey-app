# LIKAS RC70 | Owner-private preinstalled reference build

**Date:** 2026-11-01 (Asia/Manila)  
**Draft PR:** #71 targeting `feature/likas-rc69-habitats-species-preflight`  
**Source code:** `feature/likas-rc70-private-bundled-references`  
**App target:** `0.27.3-rc70-private-offline-references-preview`, versionCode 130, unchanged application ID `org.phwildlife.survey.beta08` and SQLite schema 28. The ORIGINAL signer SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` must be retained for in-place upgrades.

## Owner request and privacy boundary

The owner wants the 714-species PAIC checklist and optional WorldCover-based habitat recommendation map already installed in the same Android app, without the two file-picker imports. The connected source repository is PUBLIC. The underlying taxonomic-checklist source redistribution permissions remain unresolved. Do **not** commit the owner-private CSV or geospatial reference file (or reversible encodings thereof), expose their bytes through workflow inputs, or upload a private-content APK as a public GitHub Actions artifact.

A valid personal build instead uses a **local ignored overlay**:
- `app/private-assets/paic714_master.csv`: owner-approved exact 714-row 26-column CSV, SHA256 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`, 230,291 bytes. Catalog still has stable `LIKAS725:` internal keys to preserve historical IDs. The eleven explicitly removed species are absent.
- `app/private-assets/paic_worldcover2020_500m.txt`: 16-tile historical ESA WorldCover-derived 2020 grid, SHA256 `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`, 149,412 bytes, input format `LIKAS_WORLDCOVER_2020_500M_V1`. It is generalized 0.005° grid data, not a current or ground-verified habitat observation.
- Personal ZIP `LIKAS_RC70_Private_Preinstalled_References_Overlay.zip`, 138,240 bytes SHA256 `b90893820b526846b9f7ebec2582f7b2acc952a4b880d9d7ff4a1d2a9f2c126d` was generated and verified inside owner's ChatGPT conversation. The ZIP itself is **not** in public Git.

`app/private-assets/` is in `.gitignore`. Gradle builds with no private assets as the default public variant, but verifies the presence and both exact file checksums when either file appears. No ownership inference is made from the SHA checksum.

## Startup behavior

`BundledPrivateReferences` checks whether reference assets are packaged and, if so, verifies both byte hashes before any database changes. `LikasLaunchActivity` performs the potentially expensive import on a worker thread before entering Home; a failure triggers an explicit warning and allows access to old data. The app imports the approved 714-source roster via the existing validated, transactional `MindanaoMasterImporter`, which archives old reference data, preserving field observations and historical identifications. Already active PAIC catalogs and previously user-imported maps are not overwritten. A successful one-time marker prevents repeated installation on ordinary launches. No habitat is automatically field-confirmed; the preflight screen continues to distinguish mapped candidate classes from observer selections.

The bundled map is preinstalled via existing `PaicLandCoverPack.install`; the manual map button is hidden when a packaged source has been successfully activated. Analogously, the optional master CSV button is hidden when the packaged 714 catalog is already active. This affects **only private builds**; the public source fallback keeps its manual import UI. `BundledPrivateReferencesTest` covers exact 714-row quoted CSV parsing, truncated and malformed input.

## Testing and release gates

- CSV input independently validated as 714 unique scientific names; map has 16 individually decoded 360,000-byte grids. Overlay ZIP validated for CRC and exact SHA256 of both contained files.
- A first GitHub RC70 source build on `92d03bb2c8aa35bdf5566b3259d4ad64570f64fe` passed **374 Python tests and Java compilation**, but one **test fixture** incorrectly wrote a comma-bearing elevation field without CSV quoting. The CSV parser correctly rejected it. Fixed in source commit `d141c763b38bcc0216df2a9a87bd074e5a8f653e`; wait for passing CI before claiming updated unit-test acceptance.
- Public CI has original signing certificate but deliberately **does not contain the private reference files**. This chat runtime has both owner files but **no Android SDK or original signing keystore**. Therefore a **fully bundled, correctly signed APK cannot be produced or verified from these two separated environments alone**. Do not present a code130 public CI APK as if references were embedded.
- The inherited trusted PR lineage check still expects literal SQLite schema 27; current valid RC69/RC70 schema 28 causes PR lineage failure. Never evade it by changing the migration, spoofing the old schema, or bypassing reviewed trust.
- Outstanding before a distributable private APK: assemble and sign a private build with original key and both ignored overlay files, inspect APK assets/checksums and v2 signature; on Android emulator/real phone test 27→28 upgrade with historic records, startup and backup consistency, exact 714 rows, no omitted species, 16 land-cover tiles, map suggestions versus observer confirmations, return to survey flow, and 200%-font layout.

**Affected project requirements:** G-01/02/03/04/05/09/10/11/12, T (taxonomic provenance), H (habitat provenance), S (survey lifecycle), L (GNSS and elevation), D (export/backup), E (release checks).
