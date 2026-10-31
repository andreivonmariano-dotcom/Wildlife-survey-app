# LIKAS RC44 | Independent source audit, 30 October 2026

**Inspected baseline:** active RC44 fieldwork branch at `a5354b624c32a4b5cd7238eca6b03381266e2e73` before this documentation commit. App `0.27.3-rc44-ui-harmony-preview` (code 104), package `org.phwildlife.survey.beta08`, SQLite 27. The historical default branch is not a valid Android build baseline.

**Scope:** current repository and RC39–RC44 QA, survey state, backup, research exports, UI and selected scientific reference data. This is a source audit, not execution of device/emulator tests or a new APK build.

## Findings and priority

1. **High: non-atomic restore.** `FieldBackupArchive.restoreIntoEmpty` installs the database before copying all media/preferences. An I/O failure can leave a partially restored workspace despite the original empty database safety copy. Test a staged complete-archive activation, injected failures and rollback before relying on recovery in production. G-03/G-10.
2. **High: derived export legacy-outcome mismatch.** History and Dashboard resolve historical NULL outcomes with `SurveyOutcomeSql.RESOLVED`; multiple `ResearchExports` datasets filter `COALESCE(s.outcome,'active')='complete'` instead. Ended legacy complete events may count on screen but disappear from analysis-ready exports. This is not a claim that raw records are deleted. Add shared resolver fixtures including zero detections and Not Counted. G-05/G-06/G-09.
3. **High release gate: device validation.** RC44's documented CI passed 292 Python tests, JVM tests and signed APK compilation; Android instrumentation was compiled but not executed. Phone upgrade with real preserved surveys, 320dp/200%-font, TalkBack, GNSS/elevation and interrupted restore remain unverified. G-02/G-03/G-07/G-10.
4. **Medium: copy-shortening can alter scientific scope.** `Rc13FieldGuideCopy.fieldText` removes generic suffixes about historical localities, island records or absent site confirmation. Review rendered text against each source fact and its geographic/study scope; keep original citations and uncertainty. G-01/G-04.
5. **Medium: duplicated headings and explanatory copy.** Home hero plus `EXPLORE LIKAS`, Library's `BIODIVERSITY`/`Species & habitats`/`Offline field guides`, long recovery instructions and the GPS phrase `not scored` merit a small copy edit. Never remove essential privacy, sampling, datum or source limits. Preserve the owner's approved 18 icons and frog launcher. G-05/G-07.
6. **Medium: maintainability.** The 2,541-line `MainActivity` and 1,098-line `WelcomeActivity` mix field state and presentation. Refactor only behind behavior tests, keeping persistent End Survey and all outcomes, original IDs and SQLite schema. G-06/G-09.
7. **Medium: guide coverage and source status.** The 96 curated research accounts are distinct from the full national species checklist. Their evidence coverage is incomplete and must not be presented as a complete diagnostic key. Keep unreviewed IUCN/DENR statuses neutral, scope national versus local records, preserve licenses. G-04/G-12.
8. **Medium: private backup versus shareable export.** Unencrypted recovery ZIPs contain precise field locations and may contain personal data; distinguish full local recovery from separately approved redacted research sharing. G-08.
9. **Low: project-state ambiguity.** Default `main` remains historical v0.15 code; state files mix the current status with a large chronology. Keep a concise current branch/head summary and append chronology elsewhere. Confirm unused forest-media references with startup owner before deletion.
10. **Roadmap:** Live, source-dated Research Watch and comprehensive ecology atlas remain unverified product goals, not shipped modules.

## Existing safeguards to preserve

Current recorder source shows a pinned `Save Observation`, `Review Records` and `End Survey` footer. Its End Survey dialog offers Complete, Incomplete and Not Counted, and `SurveyClosePolicy` checks method-defined effort. Dashboard/History share the read-only legacy status resolver. RC44 source documentation reports signer continuity and hash-locked approved art. A successful build does not prove field behavior.

## Recommended implementation sequence

**First:** staged backup restore and derived-export outcome reconciliation, verified on historic fixtures. **Then:** a controlled, source-checked copy and navigation pass that removes redundant words without losing scientific limitations, followed by a version-incremented, correctly signed and directly attached APK. **Next:** execute emulator tests and a real Android in-place upgrade with archived records and all survey outcomes. **Later:** incremental recorder modularization, continued scientific evidence completion, and separately scoped research/atlas features.

**Disposition:** Audit recorded; **no Android source, survey data, taxonomy, license metadata, icons or APK changed**. Wording changes are proposed, not claimed shipped. A more detailed downloadable report was created in the audit conversation; its session-local link is not a durable GitHub download.
