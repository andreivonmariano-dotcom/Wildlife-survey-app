# Automatic GPS Elevation in LIKAS

## Data capture

LIKAS displays a live elevation estimate beneath the observer's GPS coordinates. It updates with the latest accepted phone GPS fix. A fix must be no older than 30 seconds, have horizontal accuracy of 75 m or better, and not be a mock location. Elevation requires its own altitude field.

The app prefers a phone-supplied **mean-sea-level (MSL)** altitude on Android 14+ when available. Otherwise it uses **WGS84 ellipsoidal altitude**. These are different height references. LIKAS stores the datum next to every reading. It never labels ellipsoidal height as meters above sea level or silently converts it.

GPS vertical uncertainty is stored separately from horizontal uncertainty. Values with reported vertical uncertainty over 100 m are left blank and flagged. If the phone does not report vertical uncertainty, LIKAS retains its elevation value with `vertical_accuracy_unknown` status. All missing values remain NULL, never zero.

Each detection gets a snapshot at **save time**, not a potentially outdated value from the start of the survey. Station visits record elevation at the instant their point count or sampling unit begins. Existing site positions are not silently replaced by repeat visits.

## Manual correction

Tap the elevation display to enter an altitude in meters for the **next detection only** or to return to GPS mode. The correction is saved with `elevation_source=manual`, `elevation_datum=unspecified`, and no invented vertical uncertainty. The manual override clears after a successful observation save. It does not alter GPS-derived station records.

Past observations can be reviewed and their elevation corrected in History. Corrections are captured in the observation edit audit. Editing an elevation clears the GPS datum and vertical accuracy for that corrected value.

## Export fields

The field record includes `elevation_m`, `elevation_vertical_accuracy_m`, `elevation_datum`, `elevation_source`, `elevation_status`, and `elevation_gps_at`. These appear at the end of `observations.csv`. Station and point count exports include elevation, vertical accuracy, datum, source, and status. `sites.csv` contains the first captured site elevation and its provenance.

Existing observations remain unchanged after database schema 11 → 12 migration. Historical elevation values are NULL.

## Field validation

- Check Android 14+ with an MSL-capable fix. The UI should say `MSL` and exports should say `mean_sea_level`.
- Check an older Android 8–13 device. Raw altitude should be labeled `WGS84 ellipsoid`, never MSL.
- Disable GPS or wait more than 30 seconds. Stale altitude should disappear from the current display and saved elevation should be NULL.
- Supply a position with coordinates but no altitude. Save coordinates with `altitude_unavailable` and NULL elevation.
- Supply a vertical accuracy over 100 m. The UI should flag uncertain elevation and export NULL elevation with the reported uncertainty.
- Supply a fix with no vertical accuracy. Save the elevation, but flag `vertical_accuracy_unknown`.
- Open a point count, a generic sampling unit, and an observation. Compare GPS timestamps and stored snapshot datums.
- Enter a manual override, save a detection, then save another. Only the first should be manual.
- Correct a saved observation's elevation in History and confirm the audit shows the old reading and correction.
- Upgrade an existing v0.15 database. Verify previous surveys and all their observations remain intact, with NULL elevation for historical records.

**Field note:** Phone vertical accuracy is often substantially worse under dense canopy or near cliffs. LIKAS does not replace survey-grade GNSS, barometric calibration, or topographic elevation models.
