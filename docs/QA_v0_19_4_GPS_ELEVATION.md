# LIKAS v0.19.4: automatic GNSS elevation, station ecology, species evidence

## Automatic elevation and datum handling
- The continuous foreground GNSS receiver supplies the **latest live fix**
  to the Identify Species screen. Refresh every 5 seconds, but redraw the
  list only on signal availability changes or material altitude changes.
- Qualifying screen fix: Android GPS_PROVIDER, non-mock, horizontal
  accuracy strictly below 10 meters, fix age at most 10 seconds.
- Android API 34+: use location.hasMslAltitude()/getMslAltitudeMeters().
  If only WGS84 ellipsoid altitude is available, a native platform
  AltitudeConverter runs on a single service worker, not the UI thread.
  The conversion uses Android's local geoid asset to derive MSL datum.
- A usable altitude-based **screening input** further requires reported
  MSL vertical accuracy at most 50 m (68th percentile). A good horizontal
  fix does not establish the accuracy of height.
- Android 26–33: native Location.getAltitude() is WGS84 ellipsoid height,
  not terrain elevation above mean sea level. It is shown with that datum
  and **never silently used** in the species elevation comparison.
- Missing MSL, stale GPS, missing vertical accuracy or uncertainty >50 m:
  display unavailable/not scored for the elevation component. Never insert
  zero meters or guess a geoid correction.
- The Site Conditions dialog defaults to fresh automatic GPS MSL. The
  researcher can explicitly switch to manually measured MSL elevation
  with optional uncertainty or to an Unassessed state.
- The 0–100 percentage is still an **illustrative screening index**, not
  a calibrated probability of site occupation or species absence.

## Per-station environmental record and export
SQLite schema 20 -> 21 creates station_environment, keyed to visit_id,
event_id and station_id. It does not alter any previously saved survey row.

On starting each sampling unit or point count, capture the native GNSS
altitude with explicit MSL or ellipsoid datum, vertical uncertainty,
timestamp and qualification status. Missing location remains NULL.
In a picker visit, save the researcher-selected habitat code and source,
plus a separately stored manual MSL override and uncertainty. Do not
rewrite the original GPS snapshot using manual elevation.

The research ZIP includes station_environment.csv with individual
station visits and outcome, even for incomplete or canceled surveys.
Existing detection and taxonomy records remain unchanged.

## Mindanao ecological evidence expansion
- Philippine Crocodile, Sy reptile taxon SYREP2026:001:
  freshwater wetland/riparian habitats, occasional mangroves,
  recorded up to 850 m (Animal Diversity Web,
  https://animaldiversity.org/accounts/Crocodylus_mindorensis/).
- Olive-capped Flowerpecker, WBCP taxon WBCP2025:Dicaeum_nigrilore:
  Mindanao moist montane/submontane forest, often above 900 m
  (EcoRegistros 2026,
  https://www.ecoregistros.org/sheet/Dicaeum-nigrilore-nigrilore).
- These references expand the prior four reviewed profiles to **six**,
  leaving 1,497 of 1,503 source catalog taxa unassessed for habitat.
- All six use strict_habitat=0. Observed altitudinal values are not
  demonstrated physiological limits, and no low probability is inferred
  from a single documented locality or survey without detection.
- Original bird, herp and mammal checklists remain embedded offline.

## Physical-device regression tests
1. Back up ZIP then install over v0.19.3 using the same signing certificate.
2. Android 14+: capture fresh native MSL or on-device geoid conversion.
   Observe numeric MSL height with reported ± vertical uncertainty.
3. Android 13: observe WGS84 ellipsoid height labeled and not scored.
4. Verify a location of ±8 m horizontal accuracy and ±80 m vertical
   accuracy is **not eligible** for elevation scoring.
5. Test invalid/fake provider, stale fix, no altitude field, no vertical
   accuracy, lost GPS and recovery; no false 0 m heights.
6. Open Identify Species, read the automatically shown altitude, change
   sorting and select a species. Check the station timer and draft remain.
7. Switch to Manual MSL or Unassessed and back to Automatic. Verify
   the screening data changes and survey/visit settings persist.
8. Start nine stations, capture and export each station's GPS datum,
   source, accuracy, and selected habitat. No site metadata from an
   earlier station is reported as though recorded at the current one.
9. Compare results between an Android 14+ phone and an older device at
   the same benchmark site. Field GNSS altitude is an *estimate*, never
   surveyed bench elevation.
10. Incomplete and Cancel Survey retain original environmental evidence
    with their status. Test ZIP export and restart/restore.
11. Run :app:testDebugUnitTest and :app:assembleDebug, then validate
    30–60 minute real handset sessions for lag, battery and GPS return.

Official Android API reference:
https://developer.android.com/reference/android/location/Location
https://developer.android.com/reference/android/location/altitude/AltitudeConverter
