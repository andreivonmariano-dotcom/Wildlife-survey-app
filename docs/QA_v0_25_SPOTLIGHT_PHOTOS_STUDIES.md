# LIKAS v0.25: Spotlight, linked photo/GPS evidence and study templates

Date: 27 October 2026
Parent branch: `feature/v0.24-coverboard-area-sign-protocols`

## Delivered

### Q1: Full spotlight field-effort module
- Mammal `spotlight` no longer has the **basic log** label.
- The existing survey station and observation form continue to record taxonomy, detections, field GPS and survey time.
- **Method Effort & Evidence** now opens a spotlight editor with route/area ID, observer, real UTC start and end, search beam width (m), visual detection distance (m), measured transect/route distance (m), lamp/beam setup, weather, search bias and repeat-count controls.
- A return to the same route ID requires an explicit repeated-pass flag. Repeated effort remains distinct from new unique spatial coverage.
- A requested Complete outcome needs at least one valid spotlight pass plus the existing unit-duration and planned-station criteria. Zero wildlife detections remain possible.
- Archived spotlight passes are available from History and the field evidence tools screen. All rows appear in `spotlight_passes.csv`.
- Beam width and effective visibility are observer-reported field measurements, not calibrations of the lamp or proof of a detectability correction.

### Q2: Original photographs and separate GNSS evidence
- Each coverboard **inspection** and **individual mammal sign** records the observer's available fresh, non-mock GNSS fix under 10 m accuracy, including accuracy and fix UTC time.
- Without an acceptable reading, the entry is stored with NULL coordinates and `gps_status='missing_or_unverified'`. No position is fabricated or recovered from a different station visit.
- After saving a board inspection or sign encounter, LIKAS offers **Attach photo**. Images are copied from the Android document picker into private app storage. Original files, not preview bitmaps, are preserved.
- Photos are linked by `target_type` and exact check or sign `target_id`, with a survey ID. Record ownership is verified before the file is accepted.
- Photos can be opened from that record during an active survey or later in History, even after the survey has ended.
- Original copied files export under `protocol_photos/`, with `protocol_photo_index.csv`. Missing referenced files cause an export error instead of an incomplete ZIP.
- Added nullable GPS fields and GPS quality flags to `coverboard_checks.csv` and `track_sign_encounters.csv`.

### Q3: Configurable per-study completion rules
- Home → **Study protocol templates** allows the observer to create a named template for a project and a survey method.
- Configurable requirements: minimum completed station/point visits, minimum active effort minutes, minimum documented method effort units/passes, verified **initial** survey GPS, and at least one archived photo.
- Setup has a new **Study completion requirements** selector. Standard LIKAS rules are the default, or the observer selects a matching project/method template.
- The chosen template's exact values are copied at survey start into `survey_study_protocols`. Editing or deleting a template later does **not** alter the recorded survey's requirements.
- These are *extra* requirements. Standard LIKAS route, station, method and clock rules always remain in force.
- If any selected study requirement fails, a requested Complete outcome becomes Incomplete. End Incomplete and Cancel / Not Counted remain accessible, with all field data retained.
- Historical surveys without a selected template retain their prior completion conditions.
- Templates and applied survey snapshots export separately as `study_protocol_templates.csv` and `survey_study_requirements.csv`.

## Database migration
An additive version 25 → 26 migration creates:
- `spotlight_passes`
- `protocol_media`
- `study_protocol_templates`
- `survey_study_protocols`

New nullable latitude, longitude, accuracy and fix timestamps plus a GPS quality status are added to `coverboard_checks` and `track_sign_encounters`. Existing rows are retained and marked GPS missing. No source checklist data, detection records, photo records, survey end outcomes, sound recordings or station plans are overwritten.

## QA matrix
- Start spotlight survey without GPS, enter a measured pass, log zero detections and test Complete only after station and time criteria.
- Repeat the same route, verify the second pass is marked repeated and both passes export independently.
- Save a coverboard inspection with a fresh GNSS fix; then disable receiver and save another, confirming NULL location and an explicit status.
- Attach multiple JPEG/PNG/WebP photos to a single board inspection; reopen and preview each after ending the survey. Confirm ZIP has byte-for-byte copies.
- Save a mammal sign with an uncertain label; attach a photo. Confirm it does not become an automatically identified species.
- Create a study template requiring 3 visits, 20 minutes, 2 evidence units, start GPS and one photo. Attempt Complete with each rule missing in turn, and verify Incomplete.
- Start a second survey from the same template, then edit or delete the template. Confirm both surveys retain their original snapshots.
- Run a project using **Standard LIKAS requirements** to verify no change to earlier survey outcomes.
- Verify APK operation at 320 dp and 200% font scaling, permission denied, interrupted document picker, low disk space, slow GNSS and large source images.
- ZIP-check all new CSVs and copied photographs, survey IDs, record IDs, nullable GNSS fields and template snapshot values.

## Limits

No source-image camera capture is initiated by the linked-photo action. The user chooses an existing phone image through Android's picker. Photos and GPS fixes attach to **specific evidence records**, not inferred points along an entire search route.

The configured minimum number of visits is meaningful for station-based methods. Non-station methods use zero completed visits, so their templates should use effort minutes and documented passes. A template requiring start GPS will intentionally prevent a no-initial-GPS survey from being marked Complete. It does not prevent an observer from recording or ending that survey.

The remaining `bat_acoustic` basic log still needs calibrated ultrasonic hardware. Actual phone camera, GPS, file-picker, accessibility and survey lifecycle testing remains outstanding.
