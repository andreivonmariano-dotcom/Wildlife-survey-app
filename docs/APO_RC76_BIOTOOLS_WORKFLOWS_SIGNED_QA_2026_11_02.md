# APO RC76 | BioTools workflow screen refinement signed QA

**Date:** 2 November 2026 (Asia/Manila)  
**Branch:** `feature/apo-biotools-workflows-v136`, draft PR #78 into `feature/apo-biotools-destinations-v135`  
**Exact application source tested and signed:** `80ca725f3fca516678f973d10be18da9e48c1df1`  
**GitHub Actions:** `APO RC76 BioTools screen refinement signed preview`, run **41378920723**, **SUCCESS**  
**Artifact:** `APO-v136-biotools-screen-refinement-signed-preview` (ID `13112258669`)

## Exact delivered APK

- Direct conversation file: `APO-v0.27.3-RC76-BioTools-Screen-Refinement-code136.apk`
- **11,752,828 bytes**; SHA-256 `b2ba708d064ade8a7afd4062aeaf76d268d39d7c094989d7929ea7d760679ba2`
- Android `applicationId org.phwildlife.survey.beta08`; `versionCode 136`; `versionName 0.27.3-rc76-apo-biotools-screen-refinement`
- **Same signing certificate** SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`
- Downloaded the exact Actions artifact in this conversation. The single `app-debug.apk` was extracted, checked against CI SHA-256, checked for complete ZIP integrity, checked for exact three bundled hashes, and copied as the direct conversation APK. A previous conversation's sandbox path must never be assumed valid.

## What changed

- **Surveys / Setup:** Clear four-step fieldwork instructions, lighter numbered headings and informative scope notes around method, project/checklist region, planned effort and GPS preflight. The pinned GPS Continue action and method constraints were not modified. The old `Standard LIKAS requirements` label now uses `Standard survey requirements`. No survey DB columns, GPS-handling rules or outcome states changed.
- **Field Records / Survey History:** Survey-outcome and free-text filters are now within the single vertical scroll with the survey cards so they cannot monopolize a small phone's viewport. Hardware Back from an open survey returns to the filtered survey list rather than immediately exiting the activity. Debounced search, pagination and status meanings remain.
- **Field Utilities / Study Templates:** Human-readable names on each effort criterion, separated survey context, completion thresholds and evidence requirements, and compact text-only rows for saved templates. Existing project/method scoping, validation thresholds, edit and delete confirmation, and previously copied study rules are retained.
- **Analysis & Exports / Research ZIP:** Before the file-picker action, the screen presents a read-only count of surveys across outcomes, undeleted observations, projects and stations; explains that archives include all outcomes but some analytical exports only complete surveys; distinguishes a research ZIP from an actual restorable backup; and warns that observer names and geolocations are sensitive. The existing single canonical `MainActivity.writeResearchArchive` writer remains unchanged. The screen now provides spoken status on progress, cancel, failure or success.
- **Data Quality Review:** Places the quality check, database integrity test and findings into one scrollable page with one pinned return header for compact phones and larger text. `DataAudit` and SQLite quick_check logic remain unchanged; checks continue on an existing background executor.

## Verification completed

- **403 Python regression/scientific/source tests PASSED**.
- JVM unit tests and Android `:app:assembleDebug`, `:app:assembleDebugAndroidTest` build completed successfully.
- CI verified original `org.phwildlife.survey.beta08` app ID, versionCode 136, exact versionName and original signing certificate.
- Downloaded APK SHA-256 and ZIP structure verified again in the current sandbox. Embedded assets confirmed unchanged:
  - APO master artwork `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`
  - Owner-provided 714-species PAIC catalog `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`
  - ESA WorldCover source derivative `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`
- Existing SQLite schema remains **28**. End Survey/Not Counted/Incomplete logic remains in source.

## Remaining acceptance gates

No physical-device upgrade with archived survey data, dynamic font scaling/320dp screenshot review, TalkBack screen-walk, GPS fix, recording/audio field trial, study-template edit validation on-device, research ZIP content validation, or backup/restore round trip was performed for RC76. Automated source tests and Android build do not establish real handset functionality or data migration safety. **RC76 is a signed development preview, not a stable field-validated release.**
