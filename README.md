# Philippine Wildlife Survey Beta v0.11

Standalone, offline Android field recording. Supports birds, herpetofauna and mammals. Independent of KALUMBATA and SMART.

## Protected survey outcomes
Every finished survey gets one of three statuses:

- **Complete**: fulfilled active sampling effort and sampling-unit rules. Retained for preliminary analysis screening.
- **Incomplete**: interrupted, short or missing required effort. Retained for reinspection, not automatically accepted by models.
- **Not Counted**: excluded, with a **required reason**. This option appears as a visually separate red **hold for two seconds** button on the Finish Survey screen. Pressing quickly does nothing. After holding, the user must enter a reason of at least five characters and explicitly confirm.

A Not Counted survey remains in `surveys.csv`, its observations remain in `observations.csv` with `survey_outcome`, and the reason remains in `surveys.csv` and `survey_outcome_audit.csv`. This is a scientific exclusion flag, **not deletion**. Species, distance, occupancy, SDM and operational counted summaries exclude Not Counted surveys. The archived counts are shown separately on the dashboard.

## Dual clocks and effort pauses
- **TOTAL SESSION** shows actual elapsed wall-clock time, including breaks and travel.
- **ACTIVE EFFORT** subtracts explicit pauses.
- Point-count/station interval uses effective sampling time and restarts for each station.
- During a pause, sampling entry is disabled and GPS tracking stops. Resume to record data and restart location tracking.
- `effort_pauses.csv` and `sampling_units.csv` retain pause duration.
- At 30 seconds before the interval, the clock flashes red. At zero, the clock becomes solid red and continues in **OVERTIME** until manually finished.
- Optional vibration is **off by default** and can be enabled inside the survey. It triggers once near 30 seconds and again at overtime. Device vibration permission is declared.
- The UI timer stops redrawing in the background; its values are recomputed from saved timestamps upon return. A survey paused through an Android app restart remains paused.

## Station progress
The in-survey **Station progress** button shows registered stations with zero visits, active visits and completed/incomplete visits. Survey History provides access to the same screen. Suggested next stations still put unvisited ones first.

## Method-specific field effort
At the end of individual visits, equipment-related methods may log:
- verified number of devices and device-hours
- **camera-days** for camera-trap checks, based on measured periods of camera operation
- **trap-nights** for trapping or drift-fence checks, based on verified operational trapping effort
- **recorder-hours** for acoustic methods
- known downtime and verification notes
- site-specific search area, length, environmental conditions and observer minutes where applicable

These are user-verified values. The app does **not** estimate functioning camera-days from visit duration or convert the time spent checking equipment into true trapping effort.

`verified_deployment_effort.csv`, `amphibian_call_visits.csv`, `sampling_units.csv` and archive exports retain fields for quality review. `amphibian_call_visits.csv` contains each listening visit and its positive species-calling records, without fabricated absences.

## Export policy
- The scientific export files (`distance_flatfile_draft.csv`, `occupancy_visits.csv`, `sdm_occurrences.csv`, `herpetofauna_observations.csv`, `mammal_observations.csv`, `verified_deployment_effort.csv`, `amphibian_call_visits.csv`, `transect_tracks.geojson`) contain only surveys with outcome `complete`, pending further quality review.
- The **raw archive** files (`surveys.csv`, `observations.csv`, `sampling_units.csv`, `point_counts.csv`, `survey_outcome_audit.csv`, `effort_pauses.csv`, `edit_history.csv`) retain excluded records with explicit status and explanations.
- This separation prevents invalid survey attempts from inflating ecological effort denominators, but does not automatically validate complete surveys.

## Installation
v0.11 has the same Android application ID `org.phwildlife.survey.beta08` and cached signing key as v0.10. Export data from v0.10 before installing v0.11 over the existing Beta. Preserve the old ZIP backup.

## Literature
- USGS amphibian monitoring: https://www.usgs.gov/centers/eesc/science/north-american-amphibian-monitoring-program
- HUTAN camera-trap handbook, operational camera days: https://www.hutan.org.my/wp-content/uploads/Reports/Other%20reports/Camera_trap_manual.pdf
- Android VibrationEffect: https://developer.android.com/reference/android/os/VibrationEffect

See [v0.11 device test plan](docs/QA_v0_11.md). Automated Android compilation does not substitute for phone or field testing.
