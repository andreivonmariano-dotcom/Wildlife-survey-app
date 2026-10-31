# LIKAS | Uploaded COP30 raster processing, status and next scientific gates

**Date:** 2026-10-31, Manila. **Status: Pipeline implemented in public-safe Python source, but raster analysis NOT RUN.** This document explicitly records a blocked execution; no elevation values, tile coverage figures, raster derivatives, species altitudinal preferences or new APK are claimed.

## User-provided files
- `/mnt/data/rasters_COP30.tar.gz` (file-service metadata: **471,008,818 bytes**)
- `/mnt/data/rasters_COP30.tar_2.gz` (**132,469,974 bytes**)
- Both files are uploaded into the owner-private conversation runtime. The container-backed filesystem/processing tools returned `ClientError` even on `echo`, `date` and a plain `print`. As a result their TAR members could not be listed and no raster pixel inspected. Their internal coverage and integrity are **unverified**. The quoted sizes are upload metadata, not sizes derived from opening the files.

## Reproducible process (when the file execution environment is available)
Source: `scripts/likas_cop30_owner_private_elevation.py` (Python 3.10+, rasterio, geopandas, shapely, pyproj). Script is committed as **source only** and remains **untested against the owner's actual data**.

First, use `--inspect` to list all raster tiles and the actual GeoPackage layer names without writing extracted tiles:

```bash
python scripts/likas_cop30_owner_private_elevation.py \
  --archive /mnt/data/rasters_COP30.tar.gz \
  --archive /mnt/data/rasters_COP30.tar_2.gz \
  --polygon /mnt/data/LIKAS_PAIC_Working_Boundaries_2026-10-31/LIKAS_Greater_Mindanao_PAIC_WORKING_BOUNDARIES.gpkg \
  --inspect
```

After matching the reported GPKG layer names for the eight core islands and Camiguin Sur, extract the private Research Grade CSV from the previous owner-private ZIP and invoke:

```bash
python scripts/likas_cop30_owner_private_elevation.py \
  --archive /mnt/data/rasters_COP30.tar.gz \
  --archive /mnt/data/rasters_COP30.tar_2.gz \
  --polygon /mnt/data/LIKAS_PAIC_Working_Boundaries_2026-10-31/LIKAS_Greater_Mindanao_PAIC_WORKING_BOUNDARIES.gpkg \
  --core-layer REPLACE_WITH_INSPECTED_CORE_LAYER_NAME \
  --associated-layer REPLACE_WITH_INSPECTED_CAMIGUIN_LAYER_NAME \
  --points /path/to/THREE_PROJECTS_RESEARCH_GRADE_ONLY.csv \
  --output /private/location/LIKAS_COP30_ELEVATION_OUTPUT
```

**Input column mapping:** the script auto-detects several common standardized public-coordinate/uncertainty/taxon fields. If none match, it stops with the actual available CSV header names. Never fall back to iNaturalist `private_latitude` or `private_longitude`.

**Method:** check that Research Grade points are unobscured, have a public WGS84 coordinate and reported uncertainty <= 100 m, and have both center and 24 sampled geodesic uncertainty-circle perimeter points within a major-island polygon. Stream GeoTIFF DEM members from the two TAR archives into temporary nonpublic storage without path traversal, then sample available raster pixels. Data rows lacking raster coverage remain explicitly missing. Output original observation URL/ID and dates, point coordinates and per-species min/p05/median/p95/max DEM **surface heights**, with counts of distinct DEM pixels to expose duplicate-site sampling. Keep island types separate.

**Limitations:** the 24-vertex perimeter check approximates whole-error-circle land containment and intermediate-resolution shorelines still omit satellites. Elevation values are COP-DEM GLO-30 **DSM**, horizontal WGS84, vertical EGM2008. The dataset includes canopy/building height and is not high-precision animal height above MSL. Distinct raster pixels reduce but do not eliminate observation bias. Observed min/max are not range-wide vertical limits. The script has not been run or syntactically tested in this turn due unavailable compute tools.

## Other unresolved scientific issues
- **Land cover:** ESA WorldCover 2021 v200 (10 m, 11 classes) tiles **not uploaded**; no observed land-cover associations computed. The 2020/2021 products use different models, so cross-year differences cannot be interpreted as pure change. https://esa-worldcover.org/en/data-access
- **Species habitat preference:** presence-only iNaturalist observations do not establish selection; would require habitat availability and effort design.
- **Taxonomy:** prior v3.8.1 has 33 unresolved original iNaturalist names (155 observations); no unreviewed photographs relabeled. Original MDD v2.5 CSV crosswalk was completed for all 102 mammals, 101 exact species plus `Chaerephon plicata` -> `Mops plicatus` source synonym. Eight country-distribution mismatches still need original Mindanao voucher audit.
- **Conservation:** continue name-specific original DENR DAO 2019-09 legal crosswalk. The official IUCN 2026-1 API requires an access token https://api.iucnredlist.org/ and licensing/use constraints; no latest 725-species original Red List comparison performed.
- **Historical PAIC:** modern working polygons cover the eight main land islands, Camiguin Sur associated separately. Complete historical PAIC coverage needs satellite shorelines and a transparent paleogeographic model. Points outside the nine polygons are **uncertain**, not proven absent.
- **Android:** owner-private GIS/taxon datasets must be imported locally without publishing source-derived observations or overwriting historic surveys; no APK or database changed.

No output CSV, XLSX, GeoTIFF or ZIP **from these newly uploaded raster bytes** can be claimed until a file-processing tool succeeds and validates actual contents.
