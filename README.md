# Philippine Wildlife Survey Beta v0.14.1

## v0.14.1 station workflow cleanup and GPS checklist corrections

This patch removes conflicting manual station setup from the primary interface. The Home page now labels the project manager **Projects**, and its **Add sampling station** control has been removed. Station rows remain available for reviewing and correcting names, habitats and locations. The survey setup screen never asks for a station before starting a project-based survey.

- Bird point-count routes create their planned sampling stations automatically. The active survey's Start Point / Finish Point flow is the single route for beginning the next point count. An unsaved-observation warning returns to that same planned route, rather than opening the old legacy station picker.
- Structured herpetofauna and mammal survey visits default to **Generate station at my current site**. Existing stations remain available as secondary choices for intentional repeat visits.
- The bird line-transect, herpetofauna VES and point-count methods are selected in Setup only. The recording screen now shows a method label rather than a redundant disabled selector.
- Recent survey cards identify the **project**, survey type and design rather than showing "No station" for project-based sessions.
- Users can create/manage projects from within survey setup. Project names refresh after returning to the setup screen without discarding route and observer entries.
- Old surveys and saved station IDs are preserved, including legacy workflows that originally used manually assigned station identifiers.

### GPS-driven checklist suggestions

**Suggest checklist using current GPS** requests a *new one-time fix* from the phone's GPS provider. No saved station coordinates and no cached last-known position are consulted.

1. Ask for fine-location permission if it is not already granted.
2. Obtain a new GPS fix, or stop after 25 seconds.
3. Reject fixes older than 30 seconds, accuracy worse than 150 m, or simulated positions.
4. Query bundled PSGC Q3 2026 polygon boundaries offline.
5. Warn for boundary-edge, overlapping, unsupported or uncertain matches. Leave the checklist unchanged.
6. When the match is appropriate, show the detected province and proposed checklist island group for explicit confirmation.

A changed or failed lookup clears the old GPS-based suggestion so stale province labels do not carry into the next survey. The geographic choice remains manually editable. This categorizes an **administrative location**, not verified species occurrence or faunal range.

### Data integrity correction

Editing a generated station's **coordinates** in Projects clears its old `coordinate_uncertainty_m`, so a new manually entered position cannot silently inherit a prior GPS accuracy estimate. Previously recorded point-count coordinate snapshots remain unchanged. Coordinate corrections still need a documented source in field notes.

The app remains fully offline. No location coordinate is sent to a server by the checklist suggestion. Use the v0.14.1 in-place APK update and **export a ZIP backup before installing**.

See [v0.14.1 regression checklist](docs/QA_v0_14_1.md).

---

## v0.14 route planning baseline

Offline field records for birds, amphibians, reptiles and mammals. Standalone from SMART and KALUMBATA.

## A project is not a station
The setup screen now selects a **project** and the **survey method/design**. It no longer requires selecting a sampling station before starting a survey. Stations are sampling units within a survey, not substitutes for project membership.

### Bird point counts: automatically planned stations
Enter the proposed route length in **km** and the point spacing in **meters** (100, 250, 500, or a custom spacing from 50 to 2,000 m). A live preview reports the number of stations.

Stations are placed at chainage 0, interval, 2×interval ... through the **last full spacing interval** no greater than route length. For a 2,000 m route and 250 m intervals: **9 stations**, at 0, 250, 500, 750, 1000, 1250, 1500, 1750 and 2000 m. Ten points at 250 m separation would require 2,250 m between the first and last point. A non-divisible route length does not add a closely spaced endpoint station. The residual route distance is shown in setup.

When the survey session starts:
1. The app records the survey's project, route design, length, spacing and planned count.
2. It creates distinct permanent `STN-` identifiers for all planned stations and links them to the survey.
3. Station GPS fields are **blank at planning time**. These offsets are route chainages, not geographic coordinates.
4. Once physically at Point 1, tap **Start point 1 · 0 m**. A fresh device GPS position is captured along with time and uncertainty, and its own point-count timer begins.
5. Press **Finish point** when that stationary sample is complete. Travel to Point 2, then tap **Start point 2 · 250 m**. The whole-session clock runs throughout travel, but the point-count timer begins anew at each point.
6. Repeat through the last planned station. The next-station button is disabled after all points are visited. Finishing the survey early flags incomplete coverage.

Only GPS fixes from the previous 30 seconds with accuracy at most 75 m and a non-mock source are accepted for an automatically recorded station coordinate. If no acceptable fix exists, the app asks the observer to wait for GPS or explicitly begin with **GPS missing** status. GPS is never inferred from station number or assumed from a route bearing.

The existing bird point-count timing, overtime, pause handling, bird detections linked to point IDs, and Not Counted exclusion controls are preserved. Legacy surveys without a route plan keep their older manually selected station workflow.

### Transect methods
Bird line-transect and herpetofauna VES setup accept a **planned route length in km**, such as 1 km. Those methods record a continuous GPS path, not automatically generated point-count stations. The track displays estimated walked distance alongside planned route length. Its GPS path estimate remains different from measured survey effort.

Other standardized herpetofauna and mammal station visits can start under a project and select an existing station or **Create new station here**, generating an ID and saving a GPS fix if available. No station is required to launch the overall survey session.

## Database migration
Schema 10 adds `surveys.planned_route_m`, `planned_station_spacing_m`, `planned_station_count` and `route_design`, `survey_stations` containing planned chainages and actual GPS fix status, and a point-count GPS snapshot. It adds optional GPS fields to other sampling-unit visits. Existing data tables are **not dropped or rewritten**.

### ZIP exports
- `surveys.csv`: project, survey method, planned route length, spacing and count
- `survey_stations.csv`: every generated station, sequence, chainage, visit state, GPS coordinates and accuracy/status
- `point_counts.csv`: actual start/end, target and achieved count interval, point ID, station ID, planned chainage, actual station GPS/time/accuracy/status
- `sites.csv`: permanent station IDs and GPS coordinates when captured
- `track_points.csv` and `transect_tracks.geojson`: continuous GPS-tracked transects
- Previous archival, research, and photo ZIP exports remain available

## Methodological considerations
A 250 m station spacing is supported by USFS bird point-count guidance as a common minimum, but open-country and highly mobile bird communities can require larger separation, and survey independence cannot be guaranteed solely from chainage. Point-count detections belong to stationary visits. Transit time is **not** point-count sampling time. GPS coordinates are measured separately from planned route chainage. A polyline or real-time distance-to-next-station navigation will require an explicit route geometry, not just route length and nominal spacing.

- Ralph et al., *Managing and Monitoring Birds Using Point Counts*: https://www.fs.usda.gov/psw/publications/documents/wild/gtr149/pg161_168.pdf
- BC amphibian/reptile visual encounter survey protocol: https://www2.gov.bc.ca/assets/gov/environment/plants-animals-and-ecosystems/wildlife-wildlife-habitat/amphibians-reptiles-and-turtles/visual-survey-protocol-and-data-sheet-oct312014.pdf

## Install and verification
Android application ID and signing cache remain the same as v0.13.1 (`org.phwildlife.survey.beta08`). **Export a v0.13.1 backup before installing v0.14.0 as an update.** Do not uninstall the previous Beta. Compilation does not validate field GPS behaviour on a handset.

See `docs/QA_v0_14.md` for regression tests.
