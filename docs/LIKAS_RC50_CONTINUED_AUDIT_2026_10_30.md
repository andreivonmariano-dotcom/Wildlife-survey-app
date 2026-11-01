# LIKAS continued audit: scientific ZIPs, media evidence and release readiness
**Reviewed 30 October 2026 (Asia/Manila).** Non-destructive source-level audit, with an independent synthetic SQLite fixture. No new Android/emulator test or source patch was run in this continuation.

## Scope and source
Repository `andreivonmariano-dotcom/Wildlife-survey-app`. Active development branch `feature/likas-core-fieldwork-post-rc4` at docs-only HEAD `f734c6033a598ff0e5a478600c1341e705d2b243`. RC50 draft source and most recent successful emulator-tested APK correspond to `feature/likas-rc50-field-reliability`, source `72c0c3b23c538562a274534fead9e2bba09f45e7`, PR #47, versionCode 110, SQLite 27, package `org.phwildlife.survey.beta08`. The default `main` branch contains historical code and is **not** the current Android build baseline.

Reviewed relevant portions of `MainActivity.java`, `ResearchExports.java`, `DistanceExport.java`, `SurveyOutcomeSql.java`, `DataAudit.java`, `FieldBackupArchive.java`, `FieldDataDeletion.java`, `AcousticEvidence.java`, `FieldBackupActivity.java`, `DashboardActivity.java`, `FieldDb.java`, `GpsElevation.java`, `StationEnvironmentStore.java`, `TrackService.java`, `HistoryActivity.java`, and `AndroidManifest.xml`. Not a complete line-by-line review of all app classes.

## P1: inconsistent completion status among the same research ZIP's outputs
- **Observed code:** `ResearchExports.java` (lines 14-15 and queries) now applies `SurveyOutcomeSql.RESOLVED`, which defines older ended NULL-outcome/complete=1 events as complete. But `MainActivity.java` still generates research ZIP datasets using `COALESCE(s.outcome,'active')` at lines 2265, 2268, 2271, 2280, 2292 and 2305. Among other effects, `distance_samples.csv` (2291-2293) and `distance_review.csv` (2304-2306) exclude qualifying ended legacy NULL-outcome completed events, and raw observation/unit rows may label the same event as active. The raw `surveys.csv` stores the nullable original outcome unchanged; raw preservation itself is appropriate, but derived labels must be consistent.
- **Further divergence:** `DistanceExport.java` computes `distance_flatfile_draft.csv` using `s.complete=1 AND COALESCE(s.outcome,'complete')='complete'` and analogous filters, never testing `s.ended`. Its population can differ from the two other generated distance CSVs. Although normal newly ended sessions set consistent fields, anomalous or imported historical rows deserve conservative handling rather than promotion to complete.
- **Independent proof:** In a disposable SQLite database with three synthetic bird point-count events, (A) ended, outcome NULL, complete=1; (B) ended, outcome complete, complete=1; (C) still active, outcome NULL, complete=1 (intentionally anomalous), the current `MainActivity` distance_samples filter returned only B; `DistanceExport` returned A+B+C; `SurveyOutcomeSql.RESOLVED` returned A+B. This proves rule inconsistency, not the existence of C in a field user's data. No production records were read or changed.
- **Impact:** Survey denominators and species/effort rows in different ZIP files may disagree even for the same stored legacy event.
- **Remedy and acceptance:** Centralize complete/incomplete/not-counted eligibility and status labels in **all** ZIP components including `MainActivity`, `DistanceExport`, `ResearchExports`, `DataAudit`; preserve the raw outcome column with a separate read-only resolved-outcome field where possible. Add an Android instrumentation export of mixed modern/legacy/active/not-counted events that checks expected survey IDs and every CSV's headers and records. Confirm zero-detection visits remain valid, and missing is never mapped to zero.

## P1: integrity report does not check protocol images or original call audio
- **Observed code:** `DataAudit.java` (around lines 160-174) checks the app-private `record_media` directory against `observation_media.file_name`. Its `MISSING_EVIDENCE_PHOTOS` code does not inspect `protocol_media.file_name` or `acoustic_clips.filename` under `files/acoustic`.
- **Impact:** A data-quality report can omit missing equipment/study-protocol photographs or call WAV files while those files are part of important evidence and may be requested by separate ZIP exporters. This is a coverage gap in that report, not proof that saved files are missing.
- **Remedy and acceptance:** Add read-only missing-file/malformed-name checks for both media tables with distinct issue codes and count/sample IDs. Test valid photos+WAV, one missing protocol photo, one missing WAV, mismatched extensions and deleted-record semantics. Do not fabricate or automatically delete media.

## P1 risk: multi-table export lacks a single explicit consistency snapshot
- **Observed code:** `MainActivity.java` export procedure (approximately lines 2245-2314) executes sequential `csv()` calls against a `FieldDb` opened in a worker thread, and each `csv()` creates its own query cursor (2316-2328). There is no surrounding explicit read transaction or SQLite `VACUUM INTO` snapshot in this ZIP export.
- **Why it matters:** Editing or adding observations while the export runs could produce cross-file inconsistencies, e.g. an observation appearing after `surveys.csv` was written. The read-only backup tool separately uses a SQLite snapshot, but that does not protect the research export.
- **Evidence limit:** Concurrency failure is a **plausible source-level risk**, not yet reproduced in emulator or real handset.
- **Remedy and acceptance:** Generate the complete research ZIP from a single, verified read-consistent database snapshot and stage/export referenced media carefully. Create an artificial concurrent-write test with slow export and assert parent-child relationships and file hashes.

## Existing release blockers and visual follow-up
- **Restore:** `FieldBackupArchive` RC50 handles caught Java exceptions and activates SQLite last. Sudden termination/power loss between multiple file moves can still leave a partial recovery and requires interrupted-restore recovery design, test matrix, and explicit status reporting.
- **Upgrade:** No in-place code109->110 historical user-data/media upgrade has been verified against representative old backups.
- **Device UI:** Prior real emulator evidence found History `Clear` wrapping at 200% font and a broken `Conservation` heading in Species detail. RC50 Home layout issue is fixed in Android 14 screenshots. New review should cover Home, GPS, observation, History correction, Settings and all three survey outcomes at 320/360/412dp and 100/200% text.
- **Privacy/security confirmed:** `AndroidManifest.xml` declares `android:allowBackup="false"`, only `LikasLaunchActivity` exported, all other activities and `TrackService` non-exported. No INTERNET permission in inspected manifest. This is a narrow manifest observation, not a full security audit.

## Recommended audit sequence (no implicit release)
1. **Next P1 task:** end-to-end ZIP fixture and reconciliation of all completion rules across all outputs; compare entries to a frozen SQLite expected ledger. Also run source-level checks that no missing is converted to zero.
2. Add integrity checks for original protocol photos and audio, then replay missing-file fixtures.
3. Install RC49 with synthetic archival data/photos/audio, upgrade in place to RC50, verify IDs/relationships/hashes. Run a distinct backup-restore fault test with process kill and crash-recovery workflow; do not touch real user data.
4. Android emulator field workflow walkthrough across screen sizes, keyboard and larger text; separately request physical-phone GPS, daylight contrast and TalkBack evaluation.
5. Audit locality-sensitive taxonomic and habitat evidence and future Research Watch publication provenance. Maintain planned versus shipped labels.

## Change control
This audit introduced no app changes or APK. Existing RC49 stays current integrated core; RC50 PR #47 remains draft. Do not report a resolved data-integrity issue until the changed code and emulator/fixture tests pass.
