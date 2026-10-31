# LIKAS | Birds of Greater Mindanao iNaturalist Research Grade coordinate cleaning

**Research checkpoint:** 31 Oct 2026 (Asia/Manila). **Owner-private files only.** No coordinate rows, personal/source-private fields or original iNaturalist CSV uploaded to public GitHub.

## Source
Owner-uploaded `observations-806422.csv.zip`, original query `quality_grade=research&identifications=any&iconic_taxa[]=Aves&rank=species&projects[]=birds-of-greater-mindanao`, export timestamp 2026-10-31T10:17:36Z.

## Verified outputs
- Original 7,710 unique observation IDs, 275 iNaturalist scientific names; all source fields label `research`, `Aves` and `captive_cultivated=false`.
- All 7,710 source rows have publicly displayed latitude/longitude, but **1,061** have deliberately **obscured/displaced** coordinates. These were redacted in *every* cleaned table and GeoJSON. **33 source rows** also contained populated `private_latitude`/`private_longitude` which were **not** exported. No observer names, private place guesses, original free-text localities, photographs or user IDs in derivatives.
- **6,649 unobscured** WGS84 point rows retained, with uncertainty bins: ≤30 m 1,946; >30–100 m 747; >100–250 m 931; >250–1,000 m 614; >1,000 m 1,372; unknown 1,039. Original public coordinates preserved without reconstructing hidden locations.
- A conservative named localities screen flags **105 observations** with words such as Palawan/Siquijor/Cebu/Mindoro etc. This is **not** a verified PAIC GIS boundary exclusion.
- **2,655** coordinate and named-locality screened potential terrain candidates with reported public horizontal uncertainty ≤100 m; **1,917** of these at ≤30 m for higher-resolution landcover context. They represent **185** iNaturalist names and are **not** yet polygon-validated as Greater Mindanao occurrences.
- Taxonomic name status reuses private previous source audit: 249 iNaturalist names exact LIKAS v3.4 bird matches; eight provisional genus/name combinations; 18 substantive review names. All original scientific names retained and unmatched not automatically added.
- Repeated identical public points retained and counted as duplicates by position for future sampling effort / pseudoreplication controls, not deduplicated as wildlife observations.
- All cleaned rows include observation ID/link/date, public WGS84 coordinates (only for open), horizontal uncertainty and source field, taxon review, geoprivacy, region unvalidated flag, and blank `elevation_dem_m` and `landcover_class`. **No DEM / raster sampling performed**, and no current occupancy/habitat selection inferred.

## Geographic and environmental methodology
Authoritative island/PAIC polygon overlay remains pending. Core Greater Mindanao PAIC must distinguish from separate associated Camiguin Sur. For raster analysis track point uncertainty, buffered categorical landcover summaries, DEM reference datum, raster date/resolution and historical observation year. Copernicus DEM GLO-30 is a DSM with vertical datum EGM2008: https://dataspace.copernicus.eu/explore-data/data-collections/copernicus-contributing-missions/collections-description/COP-DEM . ESA WorldCover 2021 v200 is 10 m landcover: https://esa-worldcover.org/en/data-access . 2021 classes must not be asserted as the contemporary cover at 1980–2020 bird observation dates.

## Private deliverable QA
Owner directly received UTF-8 CSVs (entire cleaned 7,710; open public 6,649; terrain candidates 2,655; high-resolution candidates 1,917; 275-name species coverage), GeoJSON (2,655 geometries), QA and reproducible standard-library cleaning script in a ZIP. ZIP CRC verified; GeoJSON independently opened using GeoPandas as valid EPSG:4326 points. Private pack SHA256 `7ea1e9ec9e92ed3a26d0b3eddc55a36f33d8079e5e95ddaab245e71fe943cafc`. This file path alone does not guarantee download availability in a later chat.

**No LIKAS Android code, checklist accepted count, SQLite schema, app dataset, signing, or APK changed.** Next: island polygon intersection/coordinate uncertainty review; then independently obtained DEM and landcover raster tiles. Keep user-private coordinate-bearing derivative outside public repo/public app assets.
