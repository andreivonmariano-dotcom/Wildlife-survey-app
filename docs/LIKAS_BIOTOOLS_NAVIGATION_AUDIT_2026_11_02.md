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
- **Fix:** device picker now shows all camera-trap, drift-fence, and live-trap survey events, even before any trap deployment is registered. Previously it joined only `trap_deployments`, concealing empty or newly started device-method events.
- Fields such as Projects & sites and Habitat & Ecology can be opened from several places. **Fix:** neutral Back label for caller-relative finish rather than inaccurate Home navigation.
- Species Browse > species choice > recorder, Call library > audio detail > call library, and picker > species reference > picker remain under the existing system navigation.

## Redundancy inspection
The recorder's method-specific Quick capture mirrors one available evidence action, but `reflectQuick` hides that same choice in the expandable Other evidence tools list. Therefore, the source already avoids displaying that duplicate action simultaneously. Mark detection timestamps an encounter; Measure bearing opens the sighting camera; Save Observation persists a record. These are different operations and were deliberately not consolidated. Pause, station advancement, review, and End Survey also have distinct purposes.

## Regression guards
- New pure policy `SurveyNavigationPolicy` defines when detail navigation returns to its picker versus its caller, and when the history screen returns directly to the recorder.
- Added `SurveyNavigationPolicyTest` for picker-owned records, caller-owned records, history details, and the survey-list root.
- Source changes are UI-only and keep `org.phwildlife.survey.beta08`, versionCode 109, schema 27, signing configuration, stored records, scientific completion rules, and startup assets unchanged.

## Still requires verification
- Unit tests and compilation of this exact branch in GitHub Actions or a local Gradle environment; Android Back and header Back on an API 34 emulator; handset 200% font and 360dp layout; nested dialogs and resume after task switching.
- Physical-device upgrade and record preservation before an installable release. This branch is **not** the RC51 app, does **not** ship an APK and must not be installed as a lower-version build over versionCode 111.
- Full automated UI traversal of every dialog/form action is not complete. The route descriptions above are source-inspected paths, not claims of touchscreen testing.

**Related project rules:** G-02, G-03, G-06, G-07, G-10, G-11 and S/U user-interface requirements. No requirement meaning was altered.
