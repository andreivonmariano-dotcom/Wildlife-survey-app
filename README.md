# Philippine Wildlife Survey — v0.5 Beta

An Android offline field survey prototype for **birds, reptiles, amphibians and mammals**, independent of SMART and KALUMBATA.

## Changes in v0.5
- **Camera closed by default.** The main form no longer activates or displays the camera.
- **Mark now** captures the first-detection timestamp without using the camera, suitable for auditory detections.
- **Open camera** launches a dedicated bearing/pitch view, and **Capture bearing** returns to the observation form.
- Portrait camera preview uses a 4:3 compatible camera mode where available. The app fits the preview **without stretching** and centers any unused screen space as black margins.
- The camera is released on close or switching away from the camera screen. The orientation sensor is registered only in the camera screen.
- Offline bird-name starter search, four taxon groups, site-event timers, ecology notes, and CSV exports are retained.

## Install / retain existing data
The Android Beta application ID remains `org.phwildlife.survey.beta`. New Beta updates should preserve existing v0.4 Beta data if the APKs have the same signature. The build workflow caches a testing debug-signing key. Export observations before updating as a safeguard. The earlier v0.3 app remains a separate installation with separate storage.

## Instructions
1. Select a taxon group and survey method, then press **Start survey**.
2. For a heard bird, press **Mark now** immediately. Enter the species and estimated horizontal distance, then save.
3. For a visual detection, press **Open camera** and aim the central crosshair toward the initial detected position. Press **Capture bearing** to return to the form.
4. Enter the taxon, number observed and manual horizontal distance, then save. **Clear mark** discards a mistaken direction/time capture.
5. End survey and export zipped `surveys.csv` and `observations.csv`.

## Scientific caveats
The camera provides **magnetic azimuth and inclination**, not range. It does not determine a bird's GPS coordinates, height, or distance on its own. Estimates of bird distances must be made manually. Perpendicular distances computed for straight line transects require a matching **magnetic** transect bearing. Smartphone compass readings require device-specific field calibration. The first-detection timestamp should be marked at the actual first encounter, even if the camera is opened later. Distance-sampling readiness needs site/station metadata, survey-design information, detection records and survey effort beyond what this preliminary app captures. The embedded checklist remains an **unreviewed starter list**, not a complete or authoritative Philippine checklist.

## Next validation
- Confirm the camera stays OFF on app launch and activates only after **Open camera**.
- Confirm visual aspect ratio and crosshair alignment on the actual phone.
- Confirm **Mark now** timestamp is preserved after a later camera bearing capture.
- Test closing the camera without saving a bearing.
- Export v0.4 Beta data before installing v0.5. Check that observations persist after upgrade.
- Verify survey CSV exports and calculated perpendicular distance against reference values.
