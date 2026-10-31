# LIKAS Greater Mindanao PAIC: provisional present-day island polygons v0.3

**31 October 2026, Asia/Manila**. This is a geographic screening **draft**, not a reviewed PAIC palaeogeographic boundary or species range map. No personal coordinates or raster values are contained here.

## Geometry and citation
Eight polygons were extracted from the bundled GSHHG intermediate-resolution shoreline geometry in Basemap 2.0.0. Seven principal islands are coded `CORE_PAIC`: **Mindanao, Samar, Leyte, Bohol, Basilan, Dinagat, Siargao**. **Camiguin Sur** is coded separately as `CAMIGUIN_SUR`. The named principal island grouping is explicitly given by Hosner et al. 2014 (Evolution, https://doi.org/10.1111/evo.12459); Field Museum https://philippines.fieldmuseum.org/natural-history/narrative/4090 supports the regional grouping and the biogeographic distinction. Camiguin's glacial isolation is discussed at https://www.philippineplants.org/General/Biogeography.html.

Shoreline source: Wessel & Smith GSHHG, https://www.ngdc.noaa.gov/mgg/shorelines/shorelines.html ; https://doi.org/10.5281/zenodo.7007502 . GSHHG has LGPL licensing conditions. The local Basemap intermediate dataset is generalized and must not be represented as a survey-grade shoreline.

**Boundaries mean current island coastlines, not the -120m late-Pleistocene bathymetric shoreline.** This release deliberately excludes smaller satellite islands such as Samal, pending individually reviewed membership, and does not construct administrative or rectangular pseudo-boundaries.

## Local private candidate-coordinate screen
Against the existing 3,487 unobscured, accepted-taxon-linked Research Grade source observations with reported positional accuracy <= 100 m:
- 2,853 strictly inside the eight principal island polygons with the location's positional-accuracy circle away from the shoreline;
- 22 coastline/boundary positional-uncertainty cases;
- 118 on other current landmasses, requiring separate island/PAIC review;
- 494 off the selected present-day land polygons. May include coastal or marine wildlife observations, missing satellite islands or source coordinate error.

Counts total **3,487**, with 62 of the 2,853 inside Camiguin Sur. These are preliminary shoreline screening results, **not** verified species occurrence on the PAIC, and zero of them have modelled raster elevation or land-cover values. The previous source-locality discrepancy of 59 observation rows across 26 species remains unresolved; do not upgrade these records to approved species ecology claims.

## Deliverables and test
Originating private chat: GeoJSON containing 8 named island polygons, GeoJSON containing 2 provisional geographic regions, GeoPackage, Google Earth KML, screening aggregates without coordinates, map PNG, method guide, script and ZIP. All geometries had nonempty valid polygons, different named islands, disjoint core vs Camiguin, and the package passed ZIP CRC. Individual location coordinates are neither committed nor embedded in the map. Since these files are local private attachments, do not claim they are GitHub-release or Android-installed assets.

## Next scientific gates
1. Review named smaller satellite islands, shoreline positional uncertainty and marine/coastal records, and reconcile the earlier 59-record discrepancy. An appropriate bathymetric reconstruction is needed if an actual Pleistocene extent, rather than present-day land screening, is required.
2. Obtain source/version/vertical-datum documented Copernicus GLO-30 (DSM) tiles or another suitably licensed DEM/DTM for reproducible elevation. Google Earth may be used for one-off visual checks, but Google Maps Elevation API results generally cannot be bulk stored to build an independent offline ecological atlas: https://developers.google.com/maps/documentation/elevation/policies .
3. Obtain the actual licensed land-cover raster GeoTIFF files with georeferencing, pixel size, acquisition/reference year and class legend. No accessible land-cover GeoTIFF was found among this chat's mounted files or relevant connected file search in this session.
4. Explicit expert review is required before marking the draft polygons as trusted for extraction. The old v0.2 pipeline's “reviewed_on/reviewer” metadata gate is **not** bypassed by naming the draft region features.

## App status
Documentation and user-deliverable local geodata only. No Android Java/Gradle/assets, schema, historical user GPS/observations, APK, or production species ecological values changed. Continue active RC55 live-recorder work independently.
