# v0.9 Field QA and instrument checks

## Point counts
1. Register three different stations within one project. Start a 10-minute bird point count at Station A.
2. Record several birds and verify they all carry the same `point_count_id`.
3. At 30 seconds remaining, verify the timer turns red and pulses. At 0:00, end the current point.
4. Choose **Finish point · Next station**, select Station B and verify the timer resets with a new point ID and station. Keep the original event ID.
5. Finish Station B and move to Station C. Export data and verify 3 point counts under one survey event.
6. Finish a point early and check `completed=0`. Record a late bird and check `protocol_window_flag=outside` and candidate=0.
7. If an observation is partly typed before changing stations, the app warns and offers to save first.

## Line transects
1. Configure line transect for 120 minutes. Verify countdown and end alert.
2. Grant **precise** phone location to start the GPS foreground service and confirm the notification.
3. Walk a known route outdoors with GPS. Check walking-path estimate updates on screen. Compare against measured length.
4. Switch to another app, lock the screen briefly and return. Confirm service continues where Android permits.
5. Visit a canopy-covered site. Check rejected fixes in `track_points.csv` and route gaps. Do not assume all line geometry is captured.
6. Save `transect_tracks.geojson` and open it in GIS. Verify timestamps and coordinates from `track_points.csv`.
7. End survey and confirm location foreground service notification disappears.
8. Enter the independently measured route length for line distance-sampling effort.

## Ecology
1. Test herpetofauna VES, changing between Reptiles and Amphibians in the same session.
2. Record air temperature, humidity, substrate, microhabitat, weather and optional calling index, then check exported CSV.
3. Test an amphibian calling survey with planned listening duration and record call index. Do not treat 0 call index from a saved sighting as a complete non-detection visit.

## Analytics
1. Compare `point_counts.csv` against Survey History and `observations.csv`.
2. Check species-specific point `Sample.Label` values and line transect lengths in `distance_flatfile_draft.csv`.
3. Verify `occupancy_visits.csv` contains visit denominators plus positives but does not invent species nondetection values.
4. Inspect `sdm_occurrences.csv` for observer geolocation labels and GPS accuracy.
5. Test an upgrade from v0.8 with existing observations and import history. Preserve a backup ZIP.
6. Check one point timer and one line timer across phone backgrounding and app restoration.

## Limitations and further work
Dedicated GPS track map viewing, configurable GPS thresholds, true-north bearings and changing transect segment headings are not yet implemented. Amphibian calling protocols require additional listening-condition controls and repetition. Further model exports depend on selected species, sampling design and verified effort.
