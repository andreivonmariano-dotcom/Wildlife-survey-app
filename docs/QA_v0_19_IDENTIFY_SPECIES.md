# LIKAS v0.19: dedicated Identify Species and documented occurrence evidence

Release status: field-test beta. This is **not** an occupancy probability model.

## Field workflow
1. Start an active bird, mammal, amphibian or reptile survey.
2. Tap the large Identify Species button on the recording form.
3. The new page displays the current wildlife group and geo scope, an
   editable name search, and scrollable cards with names, family, source,
   conservation codes, and positive province/island evidence where installed.
4. Herpetofauna surveys allow switching between Amphibians and Reptiles.
5. Lists load 35 names at a time. The next page loads on a deliberate tap,
   not by creating 1,503 individual Android views at startup.
6. Search common, scientific or historical synonym. Existing source
   attribution, unresolved names and honest unidentified entries remain.
7. Select Species returns the exact displayed common/scientific combination
   to the existing observation draft. No second survey or station is created.
8. Details opens the existing read-only species page with source references.
9. Return without selecting and confirm that species, count, detection time,
   GPS mark, photographs, notes and survey clock have not changed.
10. Rotate or background and return: no duplicated observation or survey.
    Continue the same active session, preserve draft and station identity.
11. Test empty or incomplete regional checklists: All species must still
    include national candidates. Do not exclude unexpected organisms.
12. Match authoritative binomials rather than showing duplicate starter
    entries. Keep original starter rows and historic observations in SQLite.

## Initial occurrence evidence, **not a probability**
- Province listed in source: positive cited occurrence reference.
- Island listed in source: positive cited occurrence reference.
- National candidate: local occurrence has not been assessed.
- No negative class is assigned from absent records.
- No 0-100% probability, "unlikely", "absent" or "does not occur" label
  appears without an independently validated fitted occupancy model.
- A current GPS fix helps the user inspect geographic context but the
  field-protocol time stamp/GPS quality rules remain unchanged.
- Phone GNSS altitude is not calibrated terrain elevation.
- Land cover is not inferred without an actual offline map layer.
- Species habitat preferences cannot be created by reading conservation
  status, family name or national checklist membership.

## Future true occurrence probability design
1. Establish the prediction question and spatial/temporal resolution:
   season, site occupancy vs habitat suitability, and biological group.
2. Import authoritative timestamped detection/non-detection surveys and
   vetted occurrence records with taxon IDs and spatial uncertainty.
3. Source offline elevation tiles and ESA WorldCover (2021; 10 m nominal)
   or regionally reviewed forest types. Record raster date, class confidence,
   resolution, canopy mismatch and terrain uncertainty.
4. Curate species-specific elevation bounds, forest/landcover preferences,
   activity season, and dispersal/island barriers from attributed literature.
5. Fit ecological species distribution / occupancy models with spatially
   blocked validation and calibration against held-out records.
6. Account for detection probability, observation effort, observer skill,
   sampling bias, taxonomic changes and detection method. Compare with
   repeated-visit survey data where possible.
7. Expose only evaluated models with taxon coverage, provenance, date,
   held-out skill score, predicted probability, uncertainty interval
   and clear "insufficient evidence" state.
8. Keep all species selectable even for predicted low suitability.
   Predicted suitability is never proof of local absence, nor a substitute
   for field identification and conservation assessments.
9. Do not run raster/model computation every second during a survey.
   Use offline tile caching, station-level snapshots and bounded workers.

References:
- USGS: https://www.usgs.gov/publications/estimating-site-occupancy-colonization-and-local-extinction-when-a-species-detected
- USGS occupancy site/detection covariates: https://code.usgs.gov/vtcfwru/occupancyTuts
- ESA WorldCover map: https://esa-worldcover.org/en/data-access

## Device acceptance
- With >1500 taxa loaded, test smooth search on inexpensive Android handset.
- Verify source-specific documented region markers and sorting of local
  records ahead of national-only candidates.
- Verify small and empty region evidence is shown honestly.
- Confirm all detections can be saved regardless of evidence status.
- Run Java unit and APK build. Verify signing certificate for in-place
  update and back up the field ZIP. Do not uninstall LIKAS.
