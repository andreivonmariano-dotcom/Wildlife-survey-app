# Philippine Wildlife Survey v0.4 alpha
Standalone offline Android field-recording application for birds, reptiles, amphibians and mammals.

## Improvements
- Four taxon-class selectors (Birds, Reptiles, Amphibians, Mammals).
- Single-screen mobile field form with a fixed Save button and collapsible ecological notes.
- Offline bird scientific/common-name autocomplete, with **starter examples only**. This is NOT the complete authoritative Philippine checklist.
- Recent records suggested first; unidentified taxa and manual names accepted.
- Count plus/minus controls, visual/auditory/sign detections, camera-bearing capture and first-detection timestamp.
- Bird point-count timer (10-minute target), line-transect bearing and manually entered effort.
- Offline SQLite, survey completion flags, GPS fixes and zipped CSV exports.
- A separate beta app installation that leaves the earlier v0.3 app and its saved records untouched.
- No Bluetooth, external rangefinder, SMART or KALUMBATA integration.

## Get the Android APK
GitHub Actions → **Build Android Debug APK** → latest successful run → **wildlife-survey-debug-apk** artifact.
Extract `app-debug.apk` and install it on Android. Install this Beta alongside the existing v0.3 application. The apps have separate storage. Export old observations from v0.3 if needed.

## Scientific cautions
The built-in 83-item starter bird search list is not a vetted or complete Philippine species inventory. Consult the Wild Bird Club of the Philippines national checklist and AviList v2026 for systematic taxonomy work. This application does not currently bundle a fully reviewed master taxonomy. Bird horizontal distance is **manual**, and camera bearing is **magnetic**, not automatically corrected to true north. For point counts, retain first-detection distances; line transect distances require straight-line alignment and an accurate magnetic transect bearing. GPS coordinates are those of the **observer**, not automatically those of the bird. Records without distance remain observations but are not conventional distance-sampling-ready. A survey event with no detections is retained; this does not establish true species absence.

## Data exports
`surveys.csv` retains event ID, taxonomic group, method, start/end UTC, transect direction, 10-minute point count target, effort length when entered, and completion status. `observations.csv` retains taxon/group/name source, detection timestamp, count, observation method, manually entered horizontal distance, captured magnetic camera bearing and pitch, calculated perpendicular distance where applicable, observer GPS fix and accuracy, and optional ecological data. Older v0.3 observations are included with null columns for data not originally collected.

## Field testing
Test on device before relying on derived spatial or abundance values. Validate bearing using a known magnetic bearing, measure horizontal distances independently, check camera optics orientation, confirm GPS quality, and verify CSV values against handwritten control records.
