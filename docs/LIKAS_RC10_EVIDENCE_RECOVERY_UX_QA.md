# LIKAS RC10 | Scientific evidence, draft recovery and library usability QA

**Date:** 28 October 2026 (Asia/Manila)
**Branch:** feature/likas-core-fieldwork-post-rc4, draft PR #38
**Target APK:** 0.27.3-rc10-science-recovery-ux-preview, versionCode 59
**Identity:** org.phwildlife.survey.beta08, existing signing identity, SQLite schema 27
**Scope:** Research reference and non-startup UI. No survey database schema or startup changes.
**Governing requirements:** G-03, G-04, G-05, G-07, G-10, G-11, G-12, L, T, U and E.

## RC9 code audit: confirmed issue and corrective work

1. **Data-loss exposure:** Rc9PhotoAnnotationReviewActivity.draftPack() previously turned an invalid, unreadable or oversize saved JSON into a blank JSONObject. A later Save could overwrite the original drafts with a new pack, silently losing the earlier review work. RC10 now throws on invalid existing data, displays an error and refuses the overwrite.
2. **Interrupted write risk:** Raw openFileOutput truncated the sole JSON copy. RC10 uses android.util.AtomicFile for startWrite, finishWrite and failWrite, with AtomicFile.openRead recovery.
3. **Recovery gap:** Review drafts are not part of survey ZIP backup. RC10 adds an explicit Storage Access Framework JSON document export. It does not claim the survey ZIP now restores annotations. A user must still export the separate JSON file before clearing application storage. Import and fully integrated backup/restore remain unimplemented.
4. **Overcrowded small-screen library:** Four research destinations occupied permanent vertical space above the search results on narrow phones. RC10 groups regional evidence, cited trait comparisons, photo comparisons and differential research under one labeled expandable control, keeping the primary search and browse flow visible.
5. **Evidence visibility:** RC9's completeness panel displayed only readiness counts and missing labels. RC10 displays original source-backed statements, publishers, URLs and review state for matching 96-priority profiles. It does not fabricate missing evidence. Source categories available for detail include taxonomy, diagnosis, applicable sex/age/measurement evidence, habitat, range, behavior and conservation. A curated differential screen remains a separate research reference.
6. **Research triage:** New library filters expose priority taxa with unassessed diagnosis or habitat fields. Here “unassessed” indicates a research gap, not absence from a locality or evidence of ecological unsuitability.

## Scientific coverage and open review debt

RC9 remains the authoritative source for the actual 96-profile readiness counts: taxonomy 96, diagnosis 49, differential four taxa, sex-specific evidence six, life stage two, measurements 22, habitat 25, range 61, behavior three, complete IUCN + DENR assessment zero, rights-cleared photo representation 33, and specialist signoff zero. No key is certified-complete. These measures overlap and do not constitute a validated species identification percentage.

The RC8 evidence matrix contains 467 attributed statements, not 467 independent diagnostic characters. Source statements may be secondary summaries, citation-limited or specific to one sex, life stage, island or named subspecies. Geographic source evidence is not field occurrence. Sources and media remain unchanged in RC10. The private research catalog has 1,503 names, not 1,503 complete scientific accounts.

Outstanding science work: independent taxonomy/differential validation, source-specific dated IUCN and DENR records, island occurrence assessment, sex and juvenile variation, independent photo marker review, actual field diagnosis, and fuller published habitat ecology for all priority taxa.

## Tests and remaining release gates

- New Python source-contract test: scripts/test_rc10_ux_recovery.py
- Existing full Python tests, JVM/Gradle Android compile, pinned media checks and signing checks run in scientific-core CI on each branch push.
- Must inspect the exact built RC10 APK: app ID, versionCode 59, schema version 27, signature identity, 96 source-backed keys, 58 credited offline reference JPEGs, and ZIP integrity.
- **Still required for safe field use:** Android emulator UI test of annotation creation and JSON export, deliberate interrupted-write recovery exercise, document-picker cancellation, permission errors and data survival under in-place upgrade, physical handset checks for accessibility and readability, and ZIP/data restoration. Android Studio/JUnit compilation is not proof of those checks.
- The preexisting trusted PR policy still has a startup requirement conflict with the owner's newer minimal startup decision. Keep PR #38 draft until reconciled.
- Do not promote preview as stable or infer specialist biological approval from CI.

## Follow-on iterations for Q1/Q2/Q3

1. Test atomic recovery and separate annotation JSON restoration under a realistic data-failure fixture. Add import with schema/version validation if explicitly approved, then cover private drafts in a unified backup manifest.
2. Research, independently review and cite missing identification and ecological traits for the 96 priority species in small taxonomic batches. Retain unsupported status as Not assessed.
3. Consolidate the same navigation and typography rules across Home, survey setup/live recorder, Species Library and Habitat & Ecology without changing survey controls or startup assets.
4. Retest actual SQLite 27 survey histories and zero-detection records after on-device update. Preserve owner/photo rights and sensitive localities.
