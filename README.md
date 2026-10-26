# Philippine Wildlife Survey Beta v0.10

Standalone offline Android application for bird, herpetofauna and mammal field surveys. The app is not connected to SMART or KALUMBATA.

## v0.10 two-clock field interface
- **TOTAL SURVEY TIME** counts upward from survey-session start. It does not reset on a new sampling station. It is reconstructed from saved UTC start time after app backgrounding or process restart.
- **CURRENT UNIT** shows a station/visit countdown when the method has a target duration. At zero, it keeps running and displays **OVERTIME +HH:MM:SS** in solid red. For the final 30 seconds, the countdown pulses red. A one-time on-screen warning appears near the deadline and at overtime.
- Untimed methods use an **ELAPSED** unit clock. Opportunistic observations use the survey clock.
- Zero never automatically ends a survey, a point count, a unit visit, or a GPS track.

## Bird point counts
- Each point count has an independent ID and station. Tap **Finish point · Next station** after counting at the current station.
- Stations not visited during the active survey appear at the top of the next-station list.
- A progress indicator shows the number of visits completed and distinct stations visited.
- Ended stations keep their previously recorded bird observations linked to the original point-count ID.
- Ending early marks the point incomplete. Birds detected in overtime remain biodiversity records but cannot pass the preliminary distance-sampling timing flag.
- The whole survey clock keeps running across all points, including travel between stations. Travel is **not counted** as point-count effort.

## Herpetofauna and mammal station visits
- For suitable standardized searches, inspections and recording protocols, each station visit now has a stable sampling-unit ID, recorded start and end, unit duration, completion status and station ID.
- The **Finish visit · Next station** action prioritizes unvisited stations and lets the observer close a visit or move to the next.
- Time-targeted methods include VES, amphibian calling, area searches, cover-board searches, track/sign surveys, spotlighting and bat acoustic logs. Equipment deployment checks still use elapsed visit times and equipment-specific effort fields rather than claiming their run time equals camera-nights or trap-nights.
- At visit closure, relevant forms accept verified search area, searched length, equipment IDs, number of devices and device-hours.
- Amphibian calling has an ordinal 0–3 index, background noise, air temperature, humidity, weather and life stage fields. A 0 call index does not fabricate a detected individual. Unit rows can exist with zero observations.
- Herpetofauna visual encounter records include microhabitat, substrate, life stage and height above ground.
- Mammal records include direct/indirect evidence type, equipment ID, observation height and **count_basis** such as individuals, sign events, acoustic events or image detections. This prevents arbitrary evidence counts being mislabeled as animal abundance.
- Repeat visits with no detections remain saved as actual visits, not inferred confirmed absences for every species.

## Transect timing and GPS
- Bird and mammal line transects and herpetofauna VES transects retain their GPS track and walked-distance estimate.
- Configured timer continues in overtime until **Finish survey** is pressed.
- The estimated GPS path length is not treated as certified straight-line line-transect effort.
- Tracks remain subject to GPS filtering, battery management and Android foreground location permission.

## Database migration and data exports
- Version 6 adds `sampling_units` and unit-specific fields without deleting old datasets. Earlier point count visits stay in `point_counts`.
- Exports add `sampling_units.csv`, visit-specific metadata in `observations.csv`, unit-aware `occupancy_visits.csv`, mammal and herpetofauna observation tables, SDM records with unit IDs and existing distance/GIS files.
- Zero-detection visit rows and observation records are separate. A missing distance does not become a nondetection.
- Use this as **analysis preparation**, not a species occupancy or density estimate.

## Upgrade
- v0.10 has the same application ID and dedicated debug signing key as v0.9 (`org.phwildlife.survey.beta08`). Install over v0.9. Keep an exported ZIP backup before updating.
- Download the compiled `app-debug.apk` from the GitHub Release `v0.10.0-beta`.

## References and limitations
- USGS amphibian calling monitoring describes fixed-time call-index visits, environmental conditions and audible disturbances: https://www.usgs.gov/centers/eesc/science/north-american-amphibian-monitoring-program
- USGS visual encounter survey field effort can be measured by time or distance: https://www.usgs.gov/publications/optimizing-walking-pace-maximize-snake-detection-rate-a-visual-encounter-survey
- Mammal camera-trap surveys must retain deployment and camera-night effort: https://nationalzoo.si.edu/conservation-ecology-center/snapshot-usa
- Scientific instruments still require field calibration. The app does not measure animal distance with its camera, and phone GPS positions are observer locations.
- The application has not been physically tested by an automated handset lab. Verify field forms, period boundaries, completion flags, exports and GPS behavior on a real phone.

See [v0.10 QA checklist](docs/QA_v0_10.md).
