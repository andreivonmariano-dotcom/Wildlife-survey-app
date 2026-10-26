# v0.7 Release test plan (field and data integrity)

## Install
- Export v0.6 data, then update the existing beta app without uninstalling.
- Confirm new welcome home, current live event resume, and existing v0.6 surveys in history.
- Confirm schema migrates once; no duplicate historical observations.

## Survey management
- Create a project ID and register a station with an ID, name, habitat and coordinates.
- Start bird point count at the registered station. Confirm station persists across repeated visits.
- Start a herpetofauna VES. Toggle Reptiles and Amphibians within that event and verify both rows share event ID.
- Finish a structured survey without detections; verify event retained.
- Finish bird line transect with manually measured length and correctly matched magnetic azimuth.

## History
- View saved surveys and observations.
- Correct a species ID, count, distance and movement. Confirm edit timestamp and prior values in audit.
- Delete one observation and confirm it is absent from normal observation export but present in edit history.
- Back out of editing without saving and verify record unchanged.
- Repeat after force-closing the app and restarting.

## Bird measurements
- Verify bird distance source (Unknown/Estimated/Measured), initial position (Unknown/Yes/No) and movement status.
- Confirm candidate status only flags qualifying data. Verify perpendicular distance numerically for a straight transect.
- Test missing GPS, compass inaccuracy, camera opened/closed and unknown distance.
- Verify negative distance and zero counts are blocked.
- Confirm corrected edits recalculate the candidate flag and perpendicular distance.

## Export
- Check `projects.csv`, `sites.csv`, `surveys.csv`, `observations.csv`, `distance_review.csv`, `edit_history.csv`.
- Check UTC times, stable event and station IDs, zero-detection events and text quoting.
- Verify point count effort, transect length, area unit and nullable spatial attributes.
- Compare recorded measurements against handwritten reference values before scientific analysis.

**Release note:** A successful APK build does NOT substitute for field validation or distance-sampling model validation.
