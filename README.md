# Philippine Wildlife Survey Beta v0.8

Independent offline field collection for Philippine Birds, Herpetofauna and Mammals. No SMART, KALUMBATA or external rangefinder integration.

## What changed
- Welcome page has a clear dark forest header, survey choices and access to Dashboard, Survey History, Stations and Import.
- Main recording form gains **Home**, **Review** and an anchored **Save Observation** button. Actions have distinct primary, navigation, caution and camera colors.
- Camera stays off until explicitly opened. Camera closes after bearing capture.
- Active survey context lives in the local database. Incomplete observation forms are saved as a device-local draft. Fields autosave after typing pauses (~0.8 seconds), and save again when backgrounded or switching to camera.
- Restart can reopen the active survey rather than resetting the user to Home, unless **Home** was deliberately selected.
- Setup page retains a partially completed survey setup when switching away from the app.
- Fieldwork Dashboard displays survey counts, completion statuses, observation totals and counts grouped by taxon. **No density estimation is attempted.** The dashboard runs SQLite \`PRAGMA quick_check\` upon user request.
- Bird survey setup supports a point count radius, duration, station, observer, strata label and study area. Line transects require a matching **magnetic** line bearing.
- Bird observations capture estimated/measured distance, distance to initial detected position, and movement. Point count radius and line-direction availability affect the candidate analysis flag.
- Export includes linked CSV data, distance sample effort, a **species-specific draft flatfile** with rows for sampled events with zero eligible detections, and the audit trail. The flatfile must be filtered by one species and one protocol before R Distance analysis. Missing stratum area or length are left empty, never fabricated.
- A dedicated **Import v0.7 backup** screen imports the previous Beta's exported CSV ZIP into a fresh, empty v0.8 database transactionally. Read the migration warning below.

## Installation and preservation of earlier data
**IMPORTANT:** v0.8 installs alongside v0.7. It uses Android ID \`org.phwildlife.survey.beta08\`, not the original Beta ID. This decision is deliberate: previous GitHub runs did not retain their signing keys and Android could reject an in-place update. Do not uninstall v0.7 until data has been exported, imported, and verified.

1. In v0.7, open an active survey if needed and use **Export** to save your ZIP backup. Export first, then keep the old app installed.
2. Install v0.8 from the v0.8 GitHub Release APK.
3. On v0.8 Home choose **Import v0.7 backup**, select that ZIP and confirm.
4. Check project/station register, survey history, record counts and sample CSV. Keep an external copy of the ZIP.
5. Start new v0.8 surveys only after import if past records are needed: import refuses to run into a database containing survey data to avoid ID conflicts.

The new signing key is generated in a fixed path and cached by GitHub Actions for follow-up v0.8 builds. For longer-term releases, a durable protected signing key must be set up. Cache expiry still threatens continuity, so backup data routinely.

## Scientific data limitations
Distance-sampling \`distance_eligible\` is **only a screening candidate flag**. It is not an authoritative scientific validity decision. Field-calibrate compass and distance estimates. Record initial detection position and method-specific effort. R \`Distance\` needs per-species and per-method input, stratum areas, valid effort and sample-level nondetection rows. Detections missing distance remain biodiversity observations. Methods that require camera deployment, live trap nights or acoustic recorder effort still provide preliminary logging only.

## Known limits
- No complete reviewed Philippine taxonomic checklist yet.
- No field-tested GPS accuracy thresholds, geomagnetic declination correction or transect segment geometry.
- No audio/photo evidence capture or automatic GIS maps yet.
- Database quick_check verifies internal storage consistency, not ecology or species accuracy.
- The v0.7 import recognizes exports from v0.7 with \`projects.csv\`, \`sites.csv\`, \`surveys.csv\`, \`observations.csv\` and \`edit_history.csv\`. It is not a universal import format. Deleted observations excluded from the v0.7 observation CSV are not reconstructed.
- The application should be tested on a real device for configuration changes, process death, data migration and CSV values.

## Docs
- [v0.8 release and QA plan](docs/RELIABILITY_AND_QA_v0_8.md)
- [Distance-sampling workflow](docs/BIRD_DISTANCE_SAMPLING_PROTOCOL_v0_7.md)
- [Field schema](docs/FIELD_DATA_DICTIONARY_v0_7.md)

## Reference sources
- Android save UI state: https://developer.android.com/topic/libraries/architecture/views/saving-states-views
- R Distance flatfile: https://distancesampling.org/Distance/reference/flatfile.html
