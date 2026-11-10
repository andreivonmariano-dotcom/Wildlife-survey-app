# LIKAS RC7D | Android API34 Emulator Acceptance (28 October 2026)

**Result:** PASS for the single Android 34 emulator QA workflow, GitHub run 40694460009.
**Artifact:** `LIKAS-RC7-device-emulator-evidence` (artifact 12819361371).
**Source head at test:** `186574d04f59328b49993356f565044854f88b7b`.
**App:** package `org.phwildlife.survey.beta08`; Android build `0.27.3-rc7d-field-guide-device-preview`; versionCode 55; SQLite 27.
**Build:** core-science-qa successful on same source. Existing debug signing certificate continuity required in workflow.

## Actual upgraded Android installation

1. Booted disposable Android API34 Google APIs emulator and set a 320dp viewport (480-pixel width at 240 dpi).
2. Installed the earlier signed RC7C APK retrieved from GitHub Actions artifact 12816776641. Cold-launched the real `org.phwildlife.survey.LikasLaunchActivity`, which reached WelcomeActivity successfully.
3. Captured the prior version's SQLite database and SHA-256, inserted a private-media sentinel file, then installed the new RC7D APK in place without clearing app data.
4. Checked that the app-private media sentinel is byte-identical and the SQLite database bytes compare equal immediately after package upgrade.
5. Launched RC7D and confirmed installed `versionCode=55`, `versionName=0.27.3-rc7d-field-guide-device-preview`.
6. After the upgrade preservation check, deliberately cleared the **disposable emulator** app data to perform independent clean-workspace restore tests. No real user's data was touched.

## AndroidX instrumented tests

**Both tests passed:** `OK (2 tests)`, instrumentation elapsed 11.639 s.

- `BackupRoundTripInstrumentedTest.testFullBackupRestoresOnlyIntoFreshWorkspace`: creates a completed survey and private-media attachment; confirms restore is blocked over existing data; exports a current-v27 SQLite `VACUUM INTO` ZIP; checks manifest/schema/media SHA-256 and valid media payload; deletes only fixture data to obtain an empty test workspace; restores ZIP; confirms restored survey ID/time, attachment contents and second restore refusal into populated workspace.
- `SpeciesLibraryInstrumentedTest.testSourceCatalogFiltersAnd320dpAccessibility`: launches actual native library; asserts two content-described filter controls, searchable EditText, 96 priority cards and cited literature availability; searches `Pithecophaga`; finds one matching source record with a visible Philippine Eagle card and accessible activation description; captures `library-320dp.png` directly from the Android view hierarchy. Test guard: under 20 s.
- Generated screenshots: `startup.png`, `library-320dp.png`, `post-test.png`; `ui-hierarchy.xml` and logcat tail are attached to the GitHub Actions evidence artifact.

## Visual screen review

The 320dp library screenshot has visible Home navigation, a two-line "Philippine Species Library" title, source edition text, species search, two stacked filters, stacked regional and comparison controls, a brief source-vs-occurrence caution, and a readable Philippine Eagle reference card with a thumbnail. No clipping or lost action is visible in the screenshot. The native title wraps at 320dp, which is accepted for a narrow screen; broader font scaling still needs testing.

## Scientific and media integrity

- Exactly 96 source-keyed priorities, 24 in each major group, no added observer detections or checklist rewrite.
- Current app ships 26 owner-authorized `dreidrei` images with the original all-rights-reserved status and explicit private-research grant, plus 23 photographer-credited CC images and 9 hash-pinned Wikimedia Commons thumbnails.
- The verified Android APK has **58 JPEGs**. All 49 iNaturalist image SHA-256 values match the packaged files.
- ApplicationId/signing unchanged, SQLite schema unchanged, branch PR #38 remains draft.

## Outside the emulator acceptance scope

- Full physical handset RC7B/RC7C to RC7D migration, varied Android versions and filesystem implementations, power-loss recovery and fully atomic cross-file media/pref restore, backups from older SQLite builds, 200%-font accessibility and active TalkBack gestures, long-run field location/audio/photography, backup encryption and external-card storage, and independent taxonomic specialist approval.
- Earlier failed test runs involved script/test fixture and build-contract errors; the final Android API34 emulator result above is a full passing rerun, not an extrapolated claim from source tests.
- The older RC7C reference artifact expires under GitHub Actions retention. Pin its SHA for future upgrade QA or build it from a pinned source commit.

For full logs and screenshot evidence, use GitHub run https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/40694460009.
