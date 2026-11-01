# LIKAS RC51 research ZIP parity and emulator acceptance
**Reviewed 30 October 2026 (Asia/Manila).** Real source implementation and Android 14 device-level research ZIP fixture. The test project is entirely synthetic.

## Baseline and changes
- Development branch `feature/likas-rc51-research-export-audit`, last tested source SHA `a2d3277897390948225229cd0bdf8a9d6d838f2c`, draft PR #48 into RC50 draft PR #47. Authoritative active core branch still RC49.
- VersionName `0.27.3-rc51-research-export-preview`, versionCode 111. Application ID `org.phwildlife.survey.beta08`, pinned original signer, SQLite schema 27, approved artwork/taxonomic reference bundles all unchanged.
- `SurveyOutcomeSql.java`: canonical `RESOLVED` expression and `COMPLETE` derived eligibility string.
- `MainActivity.java`: all research ZIP survey-outcome labels now use read-only `RESOLVED` expression instead of `COALESCE(...,'active')`. Analytical distance samples and review use canonical eligibility; point-count distance samples require completed visits. The UI exports the exact same ZIP writer now accessible to instrumentation as `writeResearchArchive`. It does not rewrite original saved survey outcomes.
- `DistanceExport.java` and `ResearchExports.java`: use the same `SurveyOutcomeSql.COMPLETE` predicate. No schema migration or delete operations.
- Historical test contracts were repaired after new constants/helper extraction changed source structure without weakening their data checks.

## Tests actually executed
- GitHub workflow `LIKAS RC51 research ZIP and recovery emulator acceptance`, run **41047380026**, SUCCESS; full GitHub Actions source/SQLite/Python tests, Gradle unit tests, Android app and AndroidTest compilation, original signing identity and Android 14 screenshot verification.
- New `Rc51ResearchZipInstrumentedTest.actualZipKeepsRawHistoryAndExcludesOnlyIneligibleAnalyticalEvents` executed on a clean Android 14 API 34 emulator. JUnit XML: **tests=1, failures=0, errors=0, skipped=0**.
- Fixture includes (1) modern complete, (2) ended legacy NULL-outcome complete, (3) valid complete zero-detection, (4) incomplete, (5) Not Counted despite complete legacy flag, (6) active, (7) intentionally anomalous NULL-outcome active with complete=1. Assertions verify original raw surveys and positive observations retained, expected eligible survey IDs in five distinct derived files, and zero-detection effort in occupancy/Distance sampling output. Did not test concurrent writers.
- New Android screenshot's UI hierarchy validates all three Home navigation entries visible at 360dp.
- Earlier separate RC50 backup fault injection and Home layout tests: run **41042252175** passed; RC51 did not rerun backup interruption because the new fixture must execute on an empty emulator.
- The first new CI attempt had source-test incompatibilities with new code structure. Another attempt tried to clear the emulator between test suites and failed at Android package-manager reset. The final acceptance run used a fresh disposable emulator and succeeded. Those earlier failures were QA harness issues, not evidence of lost field data.

## Signed APK and evidence
- Artifact **12972409386** extracted into direct chat APK, **9,783,808 bytes**, SHA-256 **`5f202a9590ac835ab92ebbde00fa91bb8d2f322f5101fcc15e68e06e0759fec1`**; ZIP entry CRC verification passed and expected APK manifest/classes present. `aapt dump badging` confirms package `org.phwildlife.survey.beta08`, code 111, versionName `0.27.3-rc51-research-export-preview`, minSDK 26 and targetSDK 34.
- Independently parsed APK v2 signature block and verified certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, key and signer signedData cryptographic signature (RSA PKCS1 SHA-256). This confirms original certificate identity; Android API 34 emulator also installed/executed the package.
- Emulator screenshots, JUnit XML, and instrument log are in artifact **12972524169**. Direct APK/evidence ZIP attached in implementing chat. Future sessions must verify local paths before linking.

## Unverified / not claimed
This preview is not field-stable. No existing historical full workspace or physical phone was upgraded. Research ZIP lacks a demonstrated read-consistent multi-table snapshot under concurrent recording; CSVs can theoretically reflect different time points. `DataAudit` still lacks explicit integrity checks for protocol photograph and recorded WAV references. RC50 restore power-loss/process death safety is unverified; actual GPS/elevation and accessibility checks remain. Do not merge the draft branches automatically.

## Next safety task
Add an isolated read-consistent export snapshot/fault/concurrency fixture; audit missing protocol-photo/acoustic file evidence; replay archived real-world-like records and media on RC49->RC50->RC51 in-place emulator upgrade. Only after these checks should user-data field deployment be considered.
