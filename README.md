# Philippine Wildlife Survey Beta v0.14.0

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
