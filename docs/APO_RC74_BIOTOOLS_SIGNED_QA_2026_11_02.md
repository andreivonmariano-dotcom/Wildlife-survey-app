# APO RC74 | Signed BioTools menu and independent research export QA

**Recorded:** 2 November 2026 (Asia/Manila)  
**Branch:** `feature/apo-biotools-v134`, draft PR #76 into RC73 `feature/apo-text-home-v133`  
**Exact app/test source built:** `f4f948c6533bb34ef55245aa2994c1ac5faf23a3`  
**GitHub Actions:** `APO RC74 BioTools signed preview` run `41375198786`, **SUCCESS**  
**Artifact:** `APO-v134-biotools-signed-preview`, ID `13111126439`

## Exact delivered APK

- `APO-v0.27.3-RC74-BioTools-code134.apk`, **11,747,380 bytes**
- SHA-256 `09c96752003bda20660199a439485ba356558e740b59a37cffda0a2887ff0132`
- Android appId `org.phwildlife.survey.beta08` (unchanged); `versionCode 134`; `versionName 0.27.3-rc74-apo-biotools`.
- Original signer cert SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- Downloaded the exact CI artifact into the active conversation, extracted the APK to a direct-download file, confirmed full APK ZIP integrity and recomputed SHA-256 locally. No historical APK was relabeled.

## Implemented functionality

The BioTools entry opens a new five-row icon-free typographic menu:
1. **Surveys**: existing active-survey Resume and Birds/Herpetofauna/Mammals survey setup; retained End Survey outcomes and data model.
2. **Projects & Stations**: existing SitesActivity.
3. **Field Records**: HistoryActivity and HabitatRecordsActivity.
4. **Analysis & Exports**: DashboardActivity, AuditActivity, and newly registered ResearchExportActivity.
5. **Field Utilities**: CallLibraryActivity, TrapProtocolActivity, FieldProtocolsActivity and StudyProtocolsActivity.

Survey controls and tools are no longer hidden under the legacy `More field tools` toggle. All BioTools nav rows reuse the RC73 `ApoHomeUi.module` full-width, text-only UI, with reduced-motion-safe transitions.

**New export:** The independent `ResearchExportActivity` uses Android's system save-file picker (`ACTION_CREATE_DOCUMENT`), a background thread, and calls the **unchanged canonical** `MainActivity.writeResearchArchive` ZIP builder. No active survey is required. It is distinct from the restorable settings backup and discloses that sensitive survey locations and a potentially partial ZIP after failure need review. No survey row or export schema migration was made.

## Verified

- **390 Python scientific/UI source regression tests passed.** Superseded assertions about illustrated UI and the More field tools dropdown were updated to reflect current owner decisions without removing survey/identity contract tests.
- Android JVM tests, `:app:assembleDebug`, and `:app:assembleDebugAndroidTest` passed.
- CI `aapt` verified exact app package and version. `apksigner` verified retained original signing certificate.
- APK ZIP CRC/integrity and exact embedded assets were reverified after download:
  - Approved APO mountain original SHA-256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`.
  - Authorized PAIC 714-species roster SHA-256 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`.
  - ESA WorldCover derivative SHA-256 `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.
- Existing appId, unchanged database schema 28, survey lifecycle safeguards, and approved launcher/startup code retained in source.

## Not yet verified

No RC73-to-RC74 in-place upgrade with existing field records, physical-device data export/ZIP opening test, Android system-file-picker cancellation or error-path test, emulator screenshots for small screens/200% text, TalkBack navigation, actual GPS, or longitudinal field survey acceptance was performed. The ZIP export's compilation and source contract passed, but its end-to-end content has **not** been verified on a phone. RC74 is a **signed development preview**, not a field-certified stable release. PR #76 remains draft and unmerged.
