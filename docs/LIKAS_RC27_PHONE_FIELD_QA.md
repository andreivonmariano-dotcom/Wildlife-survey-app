# LIKAS RC27 | On-device field QA register

**NO PHYSICAL HANDSET TEST HAS BEEN PERFORMED.** All result states below start as `NOT TESTED`. The following is a repeatable acceptance procedure for an Android phone and a test-only LIKAS project, not a report of completed field results.

## Preserve research records before installation

- Use a spare handset or a verified backed-up installation. Do **not** uninstall LIKAS, clear app storage, run `adb shell pm clear`, or change the package ID/signing key on any phone containing historical records.
- Verify the **full** LIKAS backup archive separately from research CSV/media ZIP exports. A research ZIP is not automatically reinstallable.
- Confirm the original and candidate installer package: `org.phwildlife.survey.beta08`, same signing certificate, higher versionCode. Record device model, OS, storage, current app version and backup SHA256 locally.
- Work in a test project labelled `LIKAS-RC27-DEVICE-QA`. Do not embed private rare-species GPS coordinates or personal identification in shared evidence.

## Evidence template

For each case, record: case ID, device/Android version, LIKAS versionCode, start/end timestamp (Asia/Manila), operator identifier, actual steps, expected result, actual result, pass/fail/not tested, evidence filename/checksum, and reproducible defect. Do not mark PASS without reviewing the evidence.

| ID | Test action | Acceptance condition | State |
|---|---|---|---|
| F01 | In-place upgrade on a consented backup/clone | Record IDs, survey statuses, GPS metadata and media SHA-256 preserved | NOT TESTED |
| F02 | New survey with method-complete effort, End Survey → Complete | Persisted complete event with verified effort and timestamp | NOT TESTED |
| F03 | Partial effort, End Survey → Incomplete | Partial observations retained, outcome incomplete | NOT TESTED |
| F04 | Cancel planned field event → Not Counted | Audit reason saved, event excluded from default analytic totals | NOT TESTED |
| F05 | Complete legitimate zero-detection visit | Effort/visit row present, no fabricated occurrence row | NOT TESTED |
| F06 | Outdoor warm/cold GNSS fixes | Fix timestamp, age, horizontal accuracy, vertical source and uncertainty logged; missing remains missing | NOT TESTED |
| F07 | Camera allowed, denied and missing original | Photo metadata links and original bytes retained, permission denial visible | NOT TESTED |
| F08 | WAV short and ≥30-minute recording with lock screen | Original WAV bytes, labels, time and media links retained, no invented species label | NOT TESTED |
| F09 | Offline airplane-mode field workflow and interruption | Survey state and observations persist, no duplicate closure after process restart | NOT TESTED |
| F10 | ZIP export to a secure local destination | CSV joins, methods, effort, outcomes, GPS, photo and WAV attachments inspectable | NOT TESTED |
| F11 | Full backup restore on spare device only | Restored record/media hashes match validated backup | NOT TESTED |
| F12 | Narrow Android screen at font 100/160/200% | Home, Setup, History, Habitat, Species, Recorder, conservation actions not clipped | NOT TESTED |
| F13 | Android TalkBack interaction | Focus order, labels, tappable touch bounds and Back/End Survey spoken access | NOT TESTED |
| F14 | Sustained field use, saver mode, temperature and battery | No silent loss; record battery percentage and observed power use rather than passing an unmeasured threshold | NOT TESTED |

## Repeatable safe phone evidence collection

The included `scripts/rc27_phone_readonly_audit.sh` captures **read-only** Android package and device build metadata with `adb`. It performs no install, reset, deletion or private survey extraction. Run it only on a phone deliberately connected through Android debugging. Sensitive location and user-record data must be reviewed locally and excluded from publicly shared QA files.

For real observation verification, compare exported ID sets, counts and cryptographic hashes from before and after the proposed upgrade in a secure local workspace. A simple positive test on the emulator does not establish device acceptance.

## Release gate

RC27 cannot be marked field-certified until the operator collects the above evidence on a real phone, reviews all failures and signs off the research dataset recovery checks. Mark no hardware test PASS merely from CI.
