# Philippine Wildlife Survey Beta v0.7

Android offline wildlife survey platform, independent of SMART and KALUMBATA.

## New in v0.7
- Welcome home with **Recent Surveys**, Survey History, Projects & Stations.
- Register projects and station IDs. Record optional station GPS coordinates and habitat, then reuse them across survey visits.
- Require previously registered stations for structured survey methods.
- Survey history lists completed and active visits. Tap a visit for its observations.
- Edit taxon identification, count, distance, notes, first-position status, measurement source, and movement.
- Observation edits write the previous and new values to an audit table.
- Deletion **soft-deletes** the observation, retaining an audit trail.
- Bird measurement quality controls classify estimated versus measured horizontal distance, initial position and flyovers.
- Export linked `projects.csv`, `sites.csv`, `surveys.csv`, `observations.csv`, `distance_review.csv`, and `edit_history.csv`.
- Dedicated on-demand camera from v0.5 and method-specific surveys from v0.6 remain.

## Install
Use the GitHub Release `v0.7.0-beta`, open its Assets, and download `app-debug.apk`.
Export old data from v0.6 before upgrading. Keep the existing Beta installed and install over it. App ID remains `org.phwildlife.survey.beta`. The GitHub build caches its debug signing key.

## Scientific classification
The `distance_eligible` field is **only a candidate flag**, not a guarantee that data are analysis-ready. Correct inference requires sampling design, sufficient detections, proper effort and stratum area, initial detection distances, and review of detection assumptions. See:
- [Field Data Dictionary](docs/FIELD_DATA_DICTIONARY_v0_7.md)
- [Bird Distance Sampling Protocol](docs/BIRD_DISTANCE_SAMPLING_PROTOCOL_v0_7.md)
- [Field QA](docs/QA_TEST_PLAN_v0_7.md)

The app currently holds a **small, unreviewed** species-name starter list, not a complete Philippine taxa checklist. Sound detections and estimated distances require particular caution.

## Method scopes
Birds: point counts, line transects, area search, opportunistic.
Herpetofauna: VES, transect VES, amphibian call, cover boards, pitfall/drift checks, opportunistic.
Mammals: direct transect, track/sign, spotlight, camera checks, bat acoustic, live-trap checks, opportunistic.
Specialist capture/acoustic/camera methods remain **basic logging**, not full effort-complete survey systems.

## Status
APK build compilation is automated. Field validation, true-north camera calibration, strict workflow design for every method, and dataset modelling checks remain open tasks.
