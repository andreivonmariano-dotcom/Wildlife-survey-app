# LIKAS | BioTools survey-navigation audit
**Date:** 2 November 2026, Asia/Manila
**Base examined:** `feature/likas-core-fieldwork-post-rc4` at `5cae072c99427d2b318fdde567c194884d31965b`, versionCode 109 (RC49), SQLite v27. **Scope:** navigation and screen usability, not startup/animation. The parallel RC50/RC51 preview chain is not included in this patch.

## Verified routes in the source
- Home > Fieldwork > Birds / Herpetofauna / Mammals > setup > GNSS preflight > survey recorder. Preflight cancellation returns to the saved setup. The explicitly flagged no-GPS route still exists.
- Recorder > Home persists the draft and leaves the survey active for Resume. The pinned End Survey control retains Complete, Incomplete, and Not Counted, including explicit archiving of partial and zero-detection survey events. No survey-outcome or SQLite code changed.
- Recorder > Review Records > observations/history uses the current event ID. **Fix:** Review Records now identifies the recorder as its caller, so both header Back and Android Back return directly to the recorder instead of jumping to an unrelated all-surveys list.
- Fieldwork > History > survey > observations. **Fix:** header Back and Android Back both return to the survey list. History opened from Fieldwork exits to Fieldwork rather than pretending to navigate to Home.
- Recorder > Station progress: returns to recorder. History > Station progress: returns to history. **Fix:** common Back label instead of inaccurately promising Survey on the second route.
- Recorder > Other evidence / quick capture > Field protocol records or Trap and camera records: direct event-scoped view and Back returns to recorder.
- Fieldwork > More field tools > Protocols > survey details: **fix:** both header Back and Android Back return to the protocol-survey picker. Fieldwork > More field tools > Camera traps > survey details: same navigation fix for the device-survey picker.
- **Fix:** device picker now shows all camera-trap, drift-fence, and live-trap survey events, even before any trap deployment is registered. Previously it joined only `trap_deployments`, concealing empty or newly started device-method events. Each row now shows the resolved outcome as well as the readable method and start date. Selecting a row uses its already-read method, removing a redundant database query.
- **Efficiency fix:** current-survey Station progress lists actual recorded visits first, with an explicit no-visits state. The unrelated project-station inventory was removed from this screen, as projects and stations have their own dedicated manager. No site or station data were deleted.
- Fields such as Projects & sites and Habitat & Ecology can be opened from several places. **Fix:** neutral Back label for caller-relative finish rather than inaccurate Home navigation.
- Species Browse > species choice > recorder, Call library > audio detail > call library, and picker > species reference > picker remain under the existing system navigation.

## Redundancy inspection
The recorder's method-specific Quick capture mirrors one available evidence action, but `reflectQuick` hides that same choice in the expandable Other evidence tools list. Therefore, the source already avoids displaying that duplicate action simultaneously. Mark detection timestamps an encounter; Measure bearing opens the sighting camera; Save Observation persists a record. These are different operations and were deliberately not consolidated. Pause, station advancement, review, and End Survey also have distinct purposes.

## Regression guards
- New pure policy `SurveyNavigationPolicy` defines when detail navigation returns to its picker versus its caller, and when the history screen returns directly to the recorder.
- Added `SurveyNavigationPolicyTest` for picker-owned records, caller-owned records, history details, and the survey-list root.
- Source changes are UI-only and keep `org.phwildlife.survey.beta08`, versionCode 109, schema 27, signing configuration, stored records, scientific completion rules, and startup assets unchanged.

## Verification performed
- GitHub Actions **LIKAS Scientific Core QA**, run `41441579728`, **SUCCESS** for source commit `a85636c498be43d95d930324f116f2370765dd87`. The run completed 316 Python tests, Gradle JVM unit tests, app compilation and AndroidTest APK compilation. The CI job confirmed that a debug APK was built, but the workflow did **not** publish an APK artifact for direct chat delivery. RC4 PR lineage gate `41441573079` also passed.
- An intermediate run `41441315757` failed an outdated source-contract test requiring the removed unrelated-station section. The test was updated to assert the intentional layout. A later intermediate run `41441480269` failed a Wikimedia Commons HTTP 429 on the pinned eagle image before Gradle; this external download issue was not reproduced in the successful source run.

## Still requires verification
- Tap-testing Android Back and header Back on an API 34 emulator; handset 200% font and 360dp layout; nested dialogs and resume after task switching.
- Physical-device upgrade and record preservation before an installable release. This branch is **not** the RC51 app, does **not** ship an APK and must not be installed as a lower-version build over versionCode 111.
- Full automated UI traversal of every dialog/form action is not complete. The route descriptions above are source-inspected paths, not claims of touchscreen testing.

**Related project rules:** G-02, G-03, G-06, G-07, G-10, G-11 and S/U user-interface requirements. No requirement meaning was altered.

## Android 14 emulator inspection (2 November 2026)
- **First real-device pass:** GitHub Actions `41444816304`, successful on source `2adf10875e8bf8a944019c53a78eba4fb8dce70b`. Six of six AndroidJUnit4 device tests passed on Android 14 API 34 configured at 360×800 px and 160 dpi (360 dp width). The KVM-accelerated runner installed both app and androidTest APKs, exercised real Android Back events and screen controls, and captured logcat/runtime evidence. Artifact `13141622013`, `LIKAS-BioTools-API34-navigation-evidence`. AndroidRuntime error log was empty.
- **Passed behaviors:** History list and recorder-owned review routing; no-deployment camera-trap survey selection; field-protocol picker return paths; current-survey-only station progress; visible recorder Save/Review/End dock. The test fixtures were temporary sample survey records on a disposable emulator, not imported field data.
- **Screenshot inspection:** Seven PNG files captured at 360×800. Four presented rendered UI; three (`history_survey_list.png`, `device_no_deployments.png`, `protocol_detail.png`) were blank because the test captured the new screen synchronously before Android's layout pass. The controls still passed activity assertions. This is a screenshot harness timing error, **not evidence that the app displayed blank screens**.
- **Corrective expansion:** Subsequent test commit `84f09dfe45e549b7d3555bb76223e3d3f48fbab5` delays those screenshots until idle and adds Fieldwork > Birds > Setup > Back, archived-audio Back and a real 200%-font recorder layout check. The new suite has eight Android tests. The runner now checks for exactly eight successful cases. The expanded run is pending verification; do not claim eight passed until the corresponding job succeeds.
- **Audio exit correction:** `CallLibraryActivity` header Back and Android Back now share a save-aware navigation routine. If recording is active it initiates the save and stays on screen; if saving is ongoing it prevents exit until save callback finishes. This is source-level behavior, not yet verified with physical microphone input.
- **Remaining:** Complete/Incomplete/Not Counted must be exercised through real end-survey dialogs with persisted effort and analytical eligibility checks; GPS preflight/cancellation on emulator; actual audio recording and file persistence; 200%-font on-device results from expanded run; restoration of a real archived dataset; signed in-place upgrade from RC51 lineage. Source build is RC49-derived versionCode 109 and is **not** a safe installable replacement for later code111.

