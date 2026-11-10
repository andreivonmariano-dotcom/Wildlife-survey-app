# APO RC75 | BioTools destination overhaul, signed preview QA

**Date:** 2 November 2026 (Asia/Manila)
**Branch:** `feature/apo-biotools-destinations-v135` (draft PR #77 into `feature/apo-biotools-v134`)
**Exact tested app source commit:** `55e2864d4508bc512eafbdd9eb78eac4093030cc`
**GitHub Actions:** `APO RC75 BioTools destinations signed preview`, run **41376495935**, **SUCCESS**
**CI artifact:** `APO-v135-biotools-destinations-signed-preview`, ID **13111920410**

## Exact APK delivered in conversation

- Filename: `APO-v0.27.3-RC75-BioTools-Destinations-code135.apk`
- File size: **11,751,480 bytes**
- SHA-256: **`2083c18d98c2588c0d0382e7d6aaf0aa2885c890df91e78483c4ea710219263d`**
- AppId: `org.phwildlife.survey.beta08` (unchanged)
- `versionCode 135`, `versionName 0.27.3-rc75-apo-biotools-destinations`
- Certificate SHA-256 verified by CI `apksigner`: `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, identical to RC74 and original beta signer
- Direct-download file created in this conversation after extracting the GitHub Actions artifact; APK ZIP integrity and SHA-256 rechecked locally. Links are conversation-local; do not reuse unverified sandbox paths across chats.

## Implemented changes

- `ApoHomeUi.tool` adds compact, full-width, text-only navigation with naturally growing multi-line descriptions, accessible names, touch dimensions and ripple feedback.
- **Surveys:** Active-session Resume is preserved. Clearer survey outcome note describes Incomplete and Not Counted, without changing method completion, stored observations or End Survey. Birds/Herpetofauna/Mammals setup remains intact.
- **Projects & Stations:** Moves page controls into a scrollable layout. Adds a case-insensitive station-name/ID/habitat search and visible matched/total counts. Stations show a permanent ID, reference coordinates when both values exist, and habitat information. Invalid or non-finite latitude/longitude entries are rejected rather than silently treated as missing. Original visits and station IDs remain unchanged; manual reference-coordinate edits continue to clear stale uncertainty values.
- **Field Records:** Read-only survey and observation totals, read-only Complete/Incomplete/Not Counted/Active counts in history, and more accurate absent-date display. Saved records are not modified.
- **Analysis & Exports:** Clearer descriptive headings separate archived outcomes, point-count visits and other protocol unit records. Identifications are called labels rather than verified taxa. Bird distance candidates are explicitly not density estimates. Integrity `PRAGMA quick_check` is exposed in Data Quality Review instead of duplicating it in the descriptive dashboard, which retains the existing research ZIP export route.
- **Field Utilities:** Clarifies that new audio, device and protocol records require a supported active survey. Updates context/navigation headings and study-template input styling while preserving survey-linked and archived read-only gates.
- All menus retain the RC73/RC74 approved APO mountain launcher/startup, warm-ivory/forest-green visual direction and reduced-motion support. No database schema migration, new population estimator or external network dependency.

## CI verification

- **397 Python source and scientific regression tests passed**.
- JVM unit tests, `:app:assembleDebug` and `:app:assembleDebugAndroidTest` succeeded.
- CI verified exact appId, versionCode, versionName and original signing certificate.
- Independently reverified APK ZIP integrity and SHA-256 after extraction from current CI artifact.
- Embedded immutable artwork and scientific references remain SHA-locked:
  - APO master icon: `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`
  - Owner-authorized 714-species PAIC roster: `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`
  - ESA WorldCover derivative: `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`

## Outstanding validation

**No on-device installation or upgrade from RC74, real stored-survey data migration/retention test, Android screenshot accessibility check (320dp / 200% fonts / TalkBack), actual station-edit round trip, field GPS test, recorder lifecycle run, or end-to-end research ZIP content verification was performed for RC75.** Automated source contracts and Android compilation are not a substitute for handset QA. This APK is a **signed development preview**, not field-certified stable. PR #77 is draft and unmerged.
