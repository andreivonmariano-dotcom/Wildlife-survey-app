# APO RC95 | BioTools survey and succeeding-screen navigation audit

**Date:** 3 November 2026 (Asia/Manila)  
**Source:** `feature/apo-rc95-biotools-survey-navigation-v155` based on unmerged RC94 draft, versionCode **155**, package `org.phwildlife.survey.beta08`, SQLite **29**.  
**Release state:** Source implementation and dedicated acceptance scripts, not an approved on-phone release until signed CI and backup/upgrade gates succeed.

## Actual navigation map and corrections

| Entry | Control | Destination or action | Back / ownership now enforced |
| --- | --- | --- | --- |
| APO Home | BioTools | BioTools hub | Home |
| BioTools | Surveys | Surveys hub with existing active-survey Resume or Birds/Herpetofauna/Mammals creation | BioTools |
| Surveys | Wildlife group | Method, project and effort setup | Surveys, both Back controls |
| Setup | Continue to GPS | `SurveyGpsActivity`, then optional `HabitatsSpeciesActivity` or `MainActivity` | Cancel GPS returns to configured setup, not a new survey |
| Optional Habitats & Species | Back | Configured survey setup | Label corrected from false "Survey GPS" to "Survey setup" because GPS activity is already finished |
| Optional Habitats & Species | Begin survey | Live recorder; source habitat choices preserved | Recorder owns subsequent effort and finish states |
| Live recorder | Home / Android Back | Active-survey leave confirmation: Keep recording or Go Home | Same confirmation on both controls; draft persisted, survey retained active |
| Live recorder | Mark detection | Unsaved detection timestamp and fresh qualified observer fix if available | Remains recorder draft; no invented observation |
| Live recorder | Save Observation | Validated saved field record | Remains live recorder |
| Live recorder | Review Records / saved timeline record | Event-filtered History, optionally focused on a record | Both Back controls now close directly to recorder; do not jump into all survey history |
| History via Field Records | Survey row | Archived or active survey observations | Both Back controls return to survey list; list Back returns to Field Records |
| Observations | Sound / method evidence / device records / station progress | Event-specific read-only or editable detail based on real survey state | Back returns to observations, not an unrelated module |
| Live recorder | Station progress | Event's recorded sampling units or planned event stations | Back to live recorder, no unrelated project inventory |
| Live recorder | Field method evidence | `FieldProtocolsActivity` for supported current method | Back to live recorder |
| Live recorder | Trap & camera | `TrapProtocolActivity` for supported current method | Back to live recorder |
| Live recorder | Sound recordings | `CallLibraryActivity` with event, group and station | Both Back paths first stop and save live WAV, then allow leaving; clip detail returns to recording list |
| Field Utilities | Field protocols | Picker of recorded supported method surveys | Detail Back returns to picker, picker Back to Field Utilities |
| Field Utilities | Trap & camera records | Picker of **all** camera, drift-fence and live-trap surveys | Picker includes no-deployment events; detail Back returns to picker |
| BioTools Field Records | Site habitat records | `HabitatRecordsActivity` with `initial_tab=records` | Opens saved site records, not the general reference guide |
| Setup / PHMaps / BioTools | Projects & Stations | `SitesActivity` | Header says previous screen because callers differ |
| Setup / Field Utilities | Study templates | `StudyProtocolsActivity` | Header says previous screen because callers differ |
| Multiple entry points | Habitat & Ecology | `HabitatRecordsActivity` | Caller-neutral Back label |
| Live recorder | End Survey | Complete, Incomplete or Not Counted, each with existing scientific validation, outcome audit and data preservation | Unchanged survey outcome policy |

## Concrete defects in RC94 parent and fixes

1. Recorder-owned Review Records did not tag its origin. The History detail treated Back as "All surveys" rather than returning to the recorder. The intent now marks `navigation_origin=recorder`; History uses one `returnToCaller()` for both Back mechanisms.
2. Station progress included `SELECT id,name FROM sites WHERE project_id=?` and listed sites never visited in this survey. That inventory belongs under Projects & Stations. It is removed from this event-specific progress screen; recorded sampling visits and planned event station states remain separate.
3. Field and device record detail showed misleading parent labels. Both use the same caller-aware Back handler and distinguish picker-owned detail from History or live-recorder ownership.
4. Device-survey picker joined `trap_deployments`, hiding surveys without deployments. It now queries supported survey method events directly, displays outcome and start date, and reuses the selected row method without an extra SQL lookup.
5. Sound header Back previously warned without stopping a live recorder, while Android Back stopped the recording. Both now call `navigateBack()` and block premature exit during saving.
6. The recorder's Home button bypassed the leave-survey prompt used by Android Back. Both now call `requestLeaveRecorder()`; Keep recording does not end effort.
7. Field Records' Site habitat records link opened the guide tab by default. It now passes `initial_tab=records`.
8. Optional habitat setup falsely labeled its parent GPS. It actually returns to the setup because the GPS preflight activity closes on successful continuation.
9. Multiple-entry projects, templates and habitat-record screens had static parent captions that could be inaccurate. They now announce caller-relative previous-screen navigation.

## Deliberately retained distinct controls

Mark detection (unsaved timing), Save Observation (persist validated evidence), Review Records (edit with audit), Pause (effort accounting), Next station (sampling unit state), and End Survey (outcome) are scientifically distinct. They must not be consolidated into a single action. Method quick capture still suppresses matching duplicate actions in More tools. A complete survey still requires method-specific criteria; an incomplete survey keeps partial evidence and Not Counted remains archived but excluded from default analytical export. No changes to `FieldDb`, biological checklists, survey outcome or GPS policy are intended here.

## Test gates

- Python navigation contracts: `scripts/test_apo_rc95_biotools_routes.py`.
- Android14 event-scoped routes: `Rc95BioToolsNavigationInstrumentedTest` covering recorder-owned History, archived History list, empty device deployments, station progress, protocol and audio Back. Repeat at 160 and 200 percent system font.
- Preserve previous RC93 GPS/audio/process/SQLite29 backup tests and RC94 PHLibrary tests.
- Signed original-certificate RC92 code152 → RC95 code155 `adb install -r` without `pm clear`; verify synthetic Complete/Not Counted/active records, zero-detection effort, WAV SHA-256, audit and research ZIP.
- Verify package/signing identity, pinned PAIC714, WorldCover, approved photo and art hashes.
- Actual physical phone, real GNSS/microphone, existing field records and complete usability walkthrough remain separately unverified. No observed scientific occurrence or taxon record was manufactured by navigation changes.

**Acceptance:** Do not mark an APK released or present a download before checking successful CI, extracting the exact APK, and verifying its signer and embedded references. Draft PR must remain unmerged until these gates are reviewed.  
**Governing priorities:** data preservation, method integrity, offline UX and accessible field navigation; G, S, U, E requirements and prior survey closure decisions. Master spec is referenced by policy; the expected `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` path was not present in the inspected branch.

## Final automated acceptance: 3 November 2026

**Verified tested implementation:** commit `cfba9200540259e2985fc906cb4ae84075940f11`, GitHub Actions run [41560848770](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/41560848770), **SUCCESS**. A later documentation-only commit does not modify the tested Android binary.

- **475 Python scientific, UI and source regressions** passed, plus JVM and Android app/test compilation. Original app ID `org.phwildlife.survey.beta08`, signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` and exact PAIC714, WorldCover, approved art references passed CI checks.
- **Android14 emulator:** three GPS/audio tests, one genuine resumed-survey/process-restart test, one SQLite29 archive/restore/rollback test, two PHLibrary tests at each of 160% and 200% font, and five BioTools navigation tests at each font setting, all passed. Both AndroidRuntime logs empty. Four PHLibrary screenshots were downloaded and visually inspected, all with substantial rendered foreground pixels, including the formerly blank 200%-font hub. Evidence artifact `13196853583`.
- **Real signed no-clear upgrade:** RC92 code152 original-signed APK installed and seeded with synthetic archived/not-counted, completed-with-zero-detection and active events, a survey outcome audit, original identification text and WAV bytes. RC95 code155 installed over it with `adb install -r`. Pre- and post-upgrade tests each passed, confirming retained SQLite29 data, identical WAV hash and appropriately filtered ZIP research output. Evidence artifact `13196918726`. This is not a test of an actual user's handset or field database.
- **Signed downloadable APK:** `APO-v0.27.3-RC95-BioTools-Navigation-code155.apk`, size **11,776,104 bytes**, SHA-256 `bea7a1bb9725cf85bc69b6f286dcf5c4de1fe408f392c2ef053e9879765931ec`. Retrieved directly from run artifact `13197003456`, extracted, ZIP-tested and independently checked for the exact locked art `6fe44e51...`, PAIC `1ebf7c4a...`, WorldCover `e86e0a90...` hashes. CI's `apksigner` verified the retained certificate. A fresh direct sandbox attachment was produced in the owner chat. Session-local paths cannot be reused in other chats without checking.
- **QA correction:** First fully green workflow `41560095040` accepted a blank PHLibrary hub screenshot at 200% despite passing functional tests. `Rc94PhLibraryInstrumentedTest` now retries compositor captures and rejects screenshots without actual dark/foreground app pixels, rather than accepting a valid PNG/size alone. Final run `41560848770` verified all four real screenshots.
- **Outstanding:** Real handset, existing user data with long history, actual field GNSS and microphone, all 17 method-specific pathways and continuous outdoor use remain unverified. Draft PR #99 stays unmerged. Treat the APK as a **signed development preview**, not field-certified software.

