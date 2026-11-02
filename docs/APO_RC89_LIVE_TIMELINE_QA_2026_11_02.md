# APO RC89 | Step 2: live observation timeline and auditable corrections
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
