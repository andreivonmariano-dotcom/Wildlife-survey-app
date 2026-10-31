# LIKAS | Greater Mindanao PAIC prepolygon spatial evidence QA v0.2

**Date:** 31 October 2026 (Asia/Manila)
**Status:** Private source-data QA completed; reviewed-region polygons and environmental raster extraction **not completed**. This record contains no exact iNaturalist coordinates.

## Source datasets
- Private owner harmonized 725-taxon checklist v3.8.0 and taxonomy/spatial review v3.8.1. Their underlying sources and reviewed taxonomic concepts are preserved, not automatically revised.
- Private iNaturalist exports: birds 806422, herpetofauna 806424, mammals 806426.
- Candidate vertebrate taxa: 414 birds, 156 reptiles, 53 amphibians, 102 mammals.

## Independent source QA results
- 12,811 exported rows, 11,746 Research Grade; 1,065 Needs ID excluded.
- 11,591 Research Grade rows matched to reviewed taxon name concepts. **155** from **33** unresolved source-name concepts remain unlinked.
- 9,067 accepted-linked Research Grade rows have unobscured public coordinates, before PAIC geographic screening.
- 3,487 accepted-linked rows pass the basic public-coordinate quality screen with positive reported positional accuracy <= 100 m. This is **prepolygon candidate** count, not terrain elevations.
- The owner's prior v3.8.0 reviewed prepolygon count is **3,428**, a discrepancy of **59 rows across 26 species**. Previous locality exclusions are not attributable to individual source observation IDs from the summary fields alone. **Hold the 26 affected species for source-locality reconciliation** before any trusted raster association. Do not silently overwrite previous counts.
- The QA has *not* classified any observation as confirmed in the PAIC, nor extracted a single elevation or land-cover raster value.

## Reproducible processing
A local-only Python script and nine synthetic geospatial/unit tests were run successfully. Tests cover public coordinate eligibility, privacy/obscured exclusion, no accuracy, CORE_PAIC vs CAMIGUIN_SUR separateness, ambiguous border circles, out-of-region, geometry source-metadata validation, and synthetic GeoTIFF sampled/nodata/outside values.

Private deliverables in the originating chat: non-geographic 725-species QA workbook, plus ZIP with code, sample metadata template, per-species QA, and a **private** subset of unmasked source public coordinates for local raster extraction. These files were NOT committed to the public repository, and their sandbox links are session-specific.

## Pending inputs and scientific gates
1. Obtain/review source-documented geographic geometry delineating **CORE_PAIC** and isolated **CAMIGUIN_SUR** separately. Geologic island complexes cannot be substituted with administrative polygons or a broad rectangle.
2. Reconcile 59 observations with the previous workbook's locality screening on a **row-by-row** basis.
3. Access and locally version the appropriate Copernicus GLO-30 **digital surface model** with vertical datum (EGM2008), geospatial provenance, and raster metadata. This surface includes vegetation/buildings and must not be called a bare-earth elevation.
4. Access and locally version dated ESA WorldCover **2021 v200**, 10 m, with source legend and temporal mismatch caveats. Land cover at a reported point is not a direct field habitat description.
5. Classify geo-uncertain and marine points separately; account for repeat sites, source geoprivacy and observation dates. Report observed raster environments, not habitat selection/preference/elevational limits without appropriate availability-and-effort analysis.
6. Specialist-review species ecological summaries and license/redistribution rights before any Android integration.

## Current app impact
No Android code, SQLite migration, survey events, recorded GPS data, source catalogue, splash artwork or APK changed. Screen-by-screen RC55 / Screen 5 implementation proceeds independently.

## References
- Copernicus DEM GLO-30 registry: https://registry.opendata.aws/copernicus-dem/
- Copernicus vertical datum: https://documentation.dataspace.copernicus.eu/APIs/SentinelHub/Data/DEM.html
- ESA WorldCover 2021 v200: https://esa-worldcover.org/en/data-access
- Field Museum faunal biogeography: https://philippines.fieldmuseum.org/natural-history/narrative/4090
