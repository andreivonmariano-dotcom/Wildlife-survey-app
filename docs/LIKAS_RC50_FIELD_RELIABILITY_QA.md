# LIKAS RC50 reliability QA | 30 October 2026

## Scope and baseline
- Draft PR #47, source branch `feature/likas-rc50-field-reliability` based on tested RC49 versionCode 109, SQLite 27.
- RC50 versionName `0.27.3-rc50-field-reliability-preview`, code 110. Existing application ID, private debug signing certificate and 18 approved PNG designs protected.
- Affected governing rules: G-03, G-05, G-06, G-07, G-09, G-10, G-11, S-03–S-07, D-03, D-06, E-01/E-02, U-04–U-06.

## Changes in the draft
1. Home's full-height decorative contour View made a wrap-content FrameLayout expand to the complete available viewport in RC49. Contours now render directly in the hero FrameLayout's onDraw pass, behind real text and icons. No art changed. Screenshot evidence: Android 14 360dp normal-font Home shows Fieldwork, Library, Settings on first view. In the 320dp/200% font view the scroll container exposes lower entries normally.
2. ResearchExports uses the same read-only `SurveyOutcomeSql.RESOLVED` rule as History/Dashboard. Ended legacy NULL-outcome surveys with `complete=1` are included; active, incomplete, and Not Counted events are not promoted to complete. Raw records are not altered. Includes station-environment outcome display.
3. FieldBackupArchive first unpacks and hashes the ZIP, copies attachments into private staging, validates prepared SQLite, then atomically renames each staged item into place. SQLite activation occurs last. If an exception happens mid-install, reverse-order rollback restores prior preferences, removes new media, and restores the former empty SQLite database. A failed rollback preserves staging for manual inspection. Existing full-workspace/nonempty-target protections remain. **Multi-file power-loss atomicity is not guaranteed.** Manual crash recovery, stale staging and process-death cases need further work before a field-stable release.

## Tests actually executed
- Scientific Core QA: run **41042261772**, success. Python source, SQLite fixtures, Android JVM tests and compilation.
- RC4 PR lineage gate: run **41042258091**, success on preceding changed branch state; existing ancestry/signing policy retained.
- Android 14 API 34 emulator acceptance: run **41042252175**, success, artifact **12969974565** for screenshots, accessibility hierarchy and test logs.
- Three connected Android tests: `Rc40HomeLayoutInstrumentedTest.mainMenuShowsThreeCompactEntries`, `surveySetupDisplaysUnitsForPrefilledNumbers`, and `BackupRoundTripInstrumentedTest.testFullBackupRestoresOnlyIntoFreshWorkspace` all passed. The backup test deliberately threw after installation of the first attachment, verified source empty workspace remained recoverable, and then verified a complete restore with archived survey and media.
- Signed APK artifact **12969829842**. Direct chat APK size **9,783,876 bytes**, SHA-256 **5ca287b4de521125f028d606355c5a5288340f40ca3312aa9541c2da2942a07e**. APK ZIP CRCs passed; independent APK v2 certificate SHA-256 **f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937** matches the pinned signer and the v2 signedData signature was verified. On-device/upgrade testing of this exact preview with old records not completed.

## Acceptance limitations and decision
RC50 passes the scoped regression work but remains a draft, not a stable release. Full historical in-place upgrade, crash/power-loss injection, weathered real-device conditions, TalkBack, and old-data research export comparison remain pending. Do not silently migrate, delete or overwrite user data. The previously reviewed RC49 app stays the official active source until deliberate integration.

## Direct artifact policy
The verified RC50 APK was attached directly to the implementing conversation. Future conversations should retrieve and verify the exact artifact anew, and must not invent a sandbox file link.
