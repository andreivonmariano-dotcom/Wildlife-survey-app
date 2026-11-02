# APO RC82 | Wildlife-group survey setup refinement

**2 November 2026 (Asia/Manila)**  
**Branch:** `feature/apo-survey-setup-v142`, derived from signed RC81 code141.  
**Build:** `0.27.3-rc82-apo-survey-setup`, versionCode **142**. Original Android package/signature, SQLite **28** and bundled PAIC714/WorldCover/approved mountain assets must remain unchanged.

## Signed RC82 emulator verification | 2 November 2026

- **Exact signed app/test/CI source:** `85aa224bd0031e9eaf6bd0ce9b024939de06ab01`. Workflow **41391752701 SUCCESS**: **429** Python scientific/UI source tests, JVM/Android app and test APK compilation, and **4 actual Android 14 instrumented setup tests at 160% plus 4 at 200%/320dp** (new three-group validation and legacy RC54 methods). The first attempts failed on an outdated navigation test, screenshot shell formatting and captures before view layout. These were corrected; **only the final run is acceptance evidence**.
- **Signed preview:** `APO-v0.27.3-RC82-Refined-Survey-Setup-code142.apk`, **11,757,508 bytes**, SHA-256 `747b7c92fd34ce5d38b4a099f871ef96fe951dbeb6c6f1f829f9a41411d30601`. Extracted and ZIP-validated from successful CI APK artifact **13117454483**. CI verified Android package `org.phwildlife.survey.beta08`, version code **142**, version name `0.27.3-rc82-apo-survey-setup`, original signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- **Immutable assets independently verified inside extracted APK:** original APO mountain PNG `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`; PAIC714 `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`; WorldCover `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.
- **Six native app screenshot captures:** `13118150979` artifact. Birds, Herpetofauna and Mammals at 160% (1080×2280) and 200%/320dp (960×2160), all six screenshots have different SHA-256 hashes. Visually inspected Birds and Herpetofauna at 200%: selected method and description visible, full method comparison collapsed, project section begins in initial viewport, fixed Continue to GPS remains. The native picker ellipsizes the longest method labels at 200%; the popup contains their full names, but a dedicated accessible dropdown polish pass remains useful.
- **Release boundary:** Signed **developer preview** directly attached in the user chat, not production or field-certified. Draft PR #84 unmerged. The legacy unrelated RC4 forest PR lineage gate still fails separately; dedicated RC82 build passes. Physical archival-survey upgrade, device GPS/camera/audio, route template reuse, complete/incomplete/not counted persistence, TalkBack and exports remain unverified.

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
