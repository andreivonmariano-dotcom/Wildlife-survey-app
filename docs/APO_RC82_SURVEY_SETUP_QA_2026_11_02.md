# APO RC82 | Wildlife-group survey setup refinement

**2 November 2026 (Asia/Manila)**  
**Branch:** `feature/apo-survey-setup-v142`, derived from signed RC81 code141.  
**Build:** `0.27.3-rc82-apo-survey-setup`, versionCode **142**. Original Android package/signature, SQLite **28** and bundled PAIC714/WorldCover/approved mountain assets must remain unchanged.

## Changes
- Wildlife-group selection opens the same method-specific setup but now shows a single native survey-method picker, current method description, and optional **Compare all methods** disclosure. The original native radio-group selection remains, synchronised in both directions with the picker. Restored method choices and scientific validation continue to depend on the existing radio selection.
- Removed repetitive instructions and collapsed GPS-based checklist-region assistance. The required project and checklist region remain directly visible. Geographic suggestions do not assert species occurrence and GPS preflight is still required before recording.
- Bird point counts retain direct planned route length, spacing, preview and duration. Optional repeat-route selection/new route saving is in a separate disclosure; **new routes are no longer silently saved by default**. Existing saved route reuse, station IDs, duration rules and planned-vs-actual effort remain intact.
- Observer details, study templates and habitat preparation remain available. One fixed **Continue to GPS** action is retained; no new recording shortcut or database rewrite.

## Acceptance
- `scripts/test_apo_survey_setup_v142.py` covers the picker, science-specific fields, persisted selections, optional tools and unchanged package/schema.
- New `SetupCompactUiEmulatorTest` verifies Birds, Herpetofauna and Mammals setup methods, hidden/revealed comparisons, GPS assistance, optional route controls and fixed GPS continuation.
- GitHub Actions `.github/workflows/apo-survey-setup-v142-emulator-qa.yml` builds the signed preview, verifies signer/reference identity, runs all source/JVM tests plus legacy setup instrumentation, and captures distinct app screenshots for all three groups at **160%** and **200%/320dp** text.
- **Release gate:** A new direct in-chat APK requires signed CI SUCCESS, exact matching original signer/package/code/version, extraction and a verified file path. Physical handset/database upgrade and field survey completion remain unverified.

**Initial status:** Source and QA checks committed; signed APK and screenshots pending.
