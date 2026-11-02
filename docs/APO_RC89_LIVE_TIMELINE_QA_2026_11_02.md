# APO RC89 | Step 2: live observation timeline and auditable corrections

## Final signed development-preview acceptance | 2 November 2026

**App/test commit:** `6745bf00e4fd3a2e413f5aad27edf5ed70b7ca65`. **Successful workflow:** GitHub Actions **41427619415** completed successfully. **456 Python scientific/source regression tests** passed, Android/JVM app and test compilation passed, original signing certificate/package/version and locked PAIC714/WorldCover/artwork checks passed. Android 14 API34 emulator ran **7 tests at 160% font and 7 tests at 200%/320dp**, with no failures. Test coverage includes in-memory SQLite event isolation, soft-deletion exclusion, corrected-record indicators and expandable unsaved-vs-saved timeline, plus the RC88 recorder controls and RC87 habitat species picker.

**Direct APK delivered:** `APO-v0.27.3-RC89-Live-Observation-Timeline-code149.apk`, **11,769,024 bytes**, SHA-256 `8f3e7c3468829854da19f2ca2d2023aed29f8a1740be86c8fe3d1d05fba1040f`. Extracted and ZIP-validated from CI artifact **13132932660**. All three embedded public/signed identity reference SHA-256 digests were independently reverified against approved pinned values. The same SHA-256 was reported inside signed CI run. Six independent Android screenshots artifact **13133176373** passed PNG integrity, actual dimensions (1080×2280 at 160%, 960×2160 at 200%) and distinct hash checks. Visual inspection confirms initial recorder controls and fixed Save/Review/End remain reachable; screenshots show the **collapsed timeline**, while expanded interaction is verified by instrumentation, not by the screenshots.

**Previous initial run corrections:** Legacy source assertions needed updates for the stronger mandatory edit reason and event-scoped History navigation, and one Python test file contained a malformed newline escape. These were corrected. Final successful run is the only release-candidate evidence.

**Remaining:** Real handset in-place upgrade with populated SQLite29 data, direct on-device save→timeline→edit→research ZIP audit roundtrip, actual GPS/photo/audio, interrupted drafts, lifecycle, TalkBack and completion/incomplete/not-counted behavior. RC89 remains a **signed developer preview**, not field-certified. Current PR #91 remains draft.

2 November 2026 | Development build `0.27.3-rc89-apo-live-timeline`, versionCode **149**, SQLite **29** (no database migration), package `org.phwildlife.survey.beta08`, original Android signer.

## What changed
- The active survey recorder shows a collapsed **Saved timeline** with count of *non-deleted, persisted field_records for this survey only*, separately from **Draft: No unsaved observation** / **Unsaved draft: Not counted in timeline**. The count never treats a marked time, typed text, unsaved attachments or survey station as a biological detection.
- Tapping the timeline loads the four most recent persisted observations, ordered by actual stored detection timestamp then row ID, displaying their raw identification, count, timestamp, and a **Corrected** indicator when `record_edits` includes an audited edit. A fallback opens the full record history. This list only *reads* `field_records`; deleted records are excluded.
- Selecting a saved timeline entry opens that record's existing History review with photographs, recorded metadata, correction and audit history. This is a scoped deep link: the requested record must belong to the survey event; no cross-project or cross-survey exposure.
- History's existing transactional before/after JSON snapshots in `record_edits` are the single source of correction provenance. Correction reason must contain >=5 characters. Invalid distance text is not silently blanked. A change in raw identification clears any previous authoritative taxon key/source version and changes `name_source` to `user_corrected`. Edited name is not silently remapped to another catalog taxon.
- The timeline refreshes after successfully recording a new observation, on return from History, and when expanded/collapsed. It is collapsed by default to preserve the 320dp/200% observation controls and fixed Save/Review/End footer. Original survey completion logic and all biological reference assets remain unchanged.

## QA requirements
- Source regressions `scripts/test_apo_rc89_timeline.py`: saved-only event scoping, soft-deletion, corrected flag, deep-link boundary, before/after audit, required reasons, taxon provenance, no SQLite schema changes, original survey dispositions.
- Android tests `ObservationTimelineEmulatorTest`: in-memory SQLite rows from multiple surveys including soft-deleted records and changed records; visible, expandable recorder timeline and distinct draft state. Original RC88 recorder/UI and RC87 PAIC habitat tests retained.
- Dedicated workflow `.github/workflows/apo-rc89-live-timeline-v149-qa.yml` should complete science source tests, JVM compilation, actual Android14 emulator at 160% and 200%/320dp, exact signer/package/version/art/source integrity, and signed APK/screenshot artifacts before direct delivery.
- Release boundary: draft PR, not field-certified. Real handset in-place SQLite28/29 preservation, device GNSS/photo/audio, pause/effort and survey closure, TalkBack, edited export roundtrip, and process-death draft recovery remain separate gates.

## Verified status
QA pending while authored. Do not claim a new APK or CI result until checked.
