# Philippine Wildlife Survey Beta v0.9

Offline Android app for Birds, Herpetofauna, and Mammals. No SMART or KALUMBATA integration.

## v0.9 field updates
- Each bird **point count** is its own sampling unit, with `point_count_id`, station ID, start/end timestamp, target duration and completion status. One survey session can contain several station counts.
- **Finish point · Next station**: choose from registered stations, close the current point and start the next timer. Bird observations remain linked to the original station. The separate **Finish survey** action closes the whole field session.
- A bird recorded outside the point-count timer remains in the biodiversity observations but is excluded from the preliminary distance-sampling candidate flag. Short point counts are flagged incomplete.
- Bird line-transect duration is set during survey setup (for example, 120 minutes). VES and amphibian listening surveys also accept planned duration.
- The remaining time appears on the survey screen. During the final 30 seconds the text pulses red. An alert is displayed near the end, without making sound.
- A **location foreground service** records GPS track positions for line transects and VES transects, including screen-off / app-in-background operation where permitted by Android. It shows a persistent notification. Recording ends when the field session is closed.
- Survey screen shows **GPS walked path ~meters**, computed from accepted GPS fixes with filters for poor accuracy, unrealistically rapid jumps, short jitter and long gaps. This is NOT a verified straight-line transect length. The official effort-length field remains a separate observer-entered value.
- Herpetofauna form adds air temperature, relative humidity, substrate, microhabitat, weather and optional amphibian calling index. VES duration and searched area are separate survey-level data.
- Survey history groups observations by point-count station and visit number. The dashboard reports point visits and GPS path totals.

## Data exports
ZIP includes `point_counts.csv`, `track_points.csv`, `transect_tracks.geojson`, `distance_samples.csv`, `distance_flatfile_draft.csv`, `distance_review.csv`, `occupancy_visits.csv`, `sdm_occurrences.csv`, `herpetofauna_observations.csv`, and previous datasets.
- R Distance draft: each point count is a separate sample; distance, effort and study area use consistent **km / km²** units. Line samples use the measured route length in km. True zero-detection rows are not created for observations missing a distance.
- Occupancy file contains actual sampling units and positive detections, **not artificial absences**. Occupancy models need defined target species, repeated visits and detection covariates.
- SDM file flags GPS coordinates as **observer positions**, not exact wildlife locations. No background or pseudoabsence points are invented.
- GIS track file is the filtered observer walking path in WGS84 longitude/latitude, not a verified transect survey design.

## Installation and data integrity
v0.9 has the same app ID (`org.phwildlife.survey.beta08`) and signing key as v0.8. Export existing data first, then install v0.9 over v0.8. Earlier v0.7 remains separately installed. Schema is updated non-destructively to version 5.

## Android permissions
Foreground GPS recording starts only from an active transect in the app and uses a visible ongoing notification. Android may interrupt tracking when location permission is revoked, battery-saving policies interfere, or GPS quality deteriorates. A missing GPS track is not imputed.

## Scientific status
**Beta field prototype.** Camera cannot measure target distance. GPS walked distance is not validated route effort. The bird species starter names are unreviewed. Distance, occupancy and SDM export tables remain candidates, not finalized analytical datasets. Survey protocol adherence, taxonomic verification, effort calibration and model assumptions require researcher review.

See [v0.9 field QA](docs/QA_v0_9.md).
