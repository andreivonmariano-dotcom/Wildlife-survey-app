# APO RC92 | Step 5: durable approved photos and survey closure research QA

2 November 2026 · Target `0.27.3-rc92-apo-closure-integrity`, code **152**, package `org.phwildlife.survey.beta08`, retained original Android signer and **SQLite29 unchanged**. Branch `feature/apo-rc92-durable-media-closure-qa-v152` directly extends the signed RC91 developer preview.

## The reproducible reference-asset change

Exactly **nine Wikimedia Commons JPEGs** from the already reviewed and source-attributed `offline_photo_manifest_v1.json` and `docs/RC7_PINNED_COMMONS_PHOTO_SHA256.json` are now retained as byte-identical files in `app/src/main/assets/offline_photos/`. Initial one-time sourcing used the **previously signed RC90 APK**, exact approved source APK SHA-256 `8d24c96581618570aace24a50d3e6a7be641a2d082f0cf53b05ced69da9e27c3`. Every JPEG was verified independently against the existing photo hash lock before committing to the version-control branch. The successful initial bot commit must be visible before claiming this. The final RC92 CI uses **only committed image bytes**, `python3 scripts/prepare_offline_photos.py`, and no external Commons fetch or expiring GitHub Actions artifact. The photo lock/attribution, PAIC714, WorldCover and owner-selected artwork remain unchanged.

The earlier recovery script remains for provenance and disaster recovery; the signed RC90 artifact is no longer a build dependency.

## Independent end-to-end emulator tests

- `SurveyClosureArchiveEmulatorTest` operates on the **actual app SQLite29 schema** and calls the same `MainActivity.writeResearchArchive` function used by the UI. It produces a real ZIP and verifies named CSV outputs, not merely SQL string patterns.
- A **completed, zero-detection point-count visit** has a recorded visit denominator in `occupancy_visits.csv`, without an invented species absence or fabricated SDM occurrence. Completed sampling is distinct from a complete species inventory.
- **Incomplete and Not Counted surveys** preserve their actual raw recorded observations in `observations.csv`, while remaining excluded from analytical exports restricted to completed survey events. `survey_outcome_audit.csv` and `effort_pauses.csv` retain disposition and interrupted-effort provenance, including a measured 120,000 ms pause.
- `edit_history.csv` retains before and after correction records and a reason. Actual SQLite and CSV rows must round-trip through ZIP, with no fabricated species IDs.
- Existing `SurveyClosePolicy.resolve` is checked for completed station, unfinished station, inadequate active effort, missing route measurement, explicit Not Counted, and valid zero-detection completion. `TrapProtocolPolicy` distinguishes unchecked devices, still-deployed devices and undocumented capture data.
- `SurveyClosureDialogEmulatorTest` launches a new disposable **camera-trap survey** and inspects the actual Android End Survey dialog: **Finish Complete**, **End Incomplete**, **Cancel Survey · Not Counted**, the missing device-record warning and **Review device checks** link. It writes a genuine screenshot at 160% and 200% font size to the emulator's app-specific Pictures directory for CI retrieval.
- Existing live recorder, species picker, timeline and adaptive method-panel emulation remain in the same workflow, with eight total screenshots rather than six.

## Required gate and remaining physical phone tests

Run dedicated `.github/workflows/apo-rc92-durable-media-closure-v152-qa.yml`. No APK should be offered until **Python scientific/source regressions, JVM and Android compilation, original signer/package/assets, Android14 instrumented tests at 160% and 200%/320dp, and download + SHA verification** pass. Source-only reproducibility matters: the workflow must no longer fetch predecessor APKs.

Physical handset testing cannot be performed by cloud/emulator CI. With a **backup of an existing populated SQLite29 database**, the owner must separately verify an in-place upgrade preserves projects, saved sightings, edits and attachments; a completed point-count station with zero detections; an interrupted sampling visit with pause/resume; a missed-station Incomplete outcome; a Not Counted outcome excluded from analyses; a camera-trap check with a deployed-but-checked device; a missing device check that produces Incomplete; and a save→correct with reason→ZIP export roundtrip. Review the ZIP in a separate research tool and compare UUIDs, UTC timestamps, pause seconds, status and original taxon labels. Also test GNSS, photograph/audio input, background/process restart, TalkBack and phone resolution. **No physical-field certification follows from emulator success.**

## Acceptance state

Implementation and emulator validation pending completion at time of writing. Do not claim a signed code152 APK or final QA result unless confirmed.
