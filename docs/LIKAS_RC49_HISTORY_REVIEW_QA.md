# LIKAS RC49 | History and observation review (Part 3)

**Date:** 30 October 2026 (Asia/Manila)  
**Version:** `0.27.3-rc49-history-review-preview`, versionCode **109**.  
**Status:** Implementation committed on an isolated branch; full CI, merged-build artifact and device validation are independent gates.

## Objective and governing rules

Part 3 of the accepted incremental LIKAS interface refinement adopts the shared RC47 design patterns on History and saved-record review. Requirements **S-01, S-04–S-07, D-02, D-03, D-06, D-08, U-04–U-06, U-08 and C-01/C-02** apply. Keep historical raw observations, UTC timestamps, uncertainty, project/survey IDs, record snapshots and audit actions intact. A UI improvement never rewrites evidence or silently changes analysis eligibility.

## Implemented

- **History search and filters:** Labeled survey outcome spinner, full-width search input using the same design system as Fieldwork, and a 48dp+ **Clear** action. Search still filters the same group, method, station and start-date fields with the existing 220ms debounce. Pagination remains 40 surveys/observations per page, preserving sort and SQL outcome semantics.
- **Survey detail hierarchy:** Compact, outlined outcome and method/start-end summary; short observations heading above recorded detections. Preserves verified Complete/Incomplete/Not Counted/Active meanings and zero-detection event display.
- **Read-only observation review:** Tapping a record still opens the established action sheet but now offers **View recorded details** before editing, photos, edit history and soft deletion. A scrollable view presents the current stored identification, scientific name, taxon group, count, detection method/time, distance and its source, confidence/evidence, habitat, behavior, notes and observer-GPS coordinates/accuracy, with a clear **Not recorded** label for NULL/missing values. This is a display of stored fields, not a new identification or geospatial claim; earlier corrected values remain in Edit history.
- **Correction form:** Shared native `UiKit.fieldInput` styling and a multiline notes editor improve long-text access without changing the existing input validation, reason option or transactional record-edit operation.
- **Audit readability:** Edit history now shows recorded **Before** and **After** JSON snapshots, timestamp, action and reason, in a selectable scrollable view. The stored snapshots, append-only `record_edits` table, original IDs and tombstone-based soft deletion are not modified.

## Data invariants and QA

- No change to `FieldDb` schema **27**, package `org.phwildlife.survey.beta08`, signing configuration, survey state machine, survey completion/pauses, location/altitude measurements, media storage, scientific references or locked 18 pixel-art PNGs and frog launcher.
- `scripts/test_rc49_history_review.py` covers read-only data paths, display null handling, preserved input validation/audit transactions, outcome SQL, pagination and version identity.
- The app must pass the full Python/JVM/Android tests and signed merged-branch APK inspection. These automated gates do **not** establish visual correctness at 320dp/200% font, TalkBack reading order, keyboard reflow, a large archival edit history, actual APK upgrade with real surveys, or field daylight contrast.
- **Separate release blockers remain:** Non-atomic backup restore and inconsistent legacy NULL-outcome handling in some derived exports. Neither is fixed in this History UI pass.
