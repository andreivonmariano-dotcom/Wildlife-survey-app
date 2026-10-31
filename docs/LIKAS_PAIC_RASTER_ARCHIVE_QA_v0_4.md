# PAIC raster archive recovery and inventory v0.4

**31 October 2026, Asia/Manila** · **Scientific source-preparation utility only, not an ecological result.**

## Confirmed private owner inputs (Project source attachments)

| Archive | Byte size | Materialization result in this conversation |
| --- | ---: | --- |
| `terrascope_download_20261031_201913.zip` | 67,734,369 | Copied as raw bytes to `/mnt/data/likas_rasters/` by Project Files |
| `terrascope_download_20261031_201741.zip` | 267,666,850 | Rejected by 100 MiB materialization limit |
| `rasters_COP30.tar_2.gz` | 132,469,974 | Rejected by 100 MiB materialization limit |
| `rasters_COP30.tar.gz` | 471,008,818 | Rejected by 100 MiB materialization limit |

These are container-copy restrictions, **not** evidence that the larger Project source uploads are corrupted or cannot be processed in a functioning environment. At the time of this update, local shell and both Python execution tools returned generic ClientError even on `echo` / `1+1`. No raster member was opened, no geospatial headers read, and no observation raster sample produced. Do not claim a DEM tile or land-cover class is in an archive from its filename alone.

## New repository utility

`scripts/likas_paic_raster_archive_inventory.py` reads archive ZIP/TAR member lists without unpacking the full archive. With the optional Rasterio environment and `--inspect-raster`, it stages one GIS raster member at a time and reports the native CRS, WGS84 extent when reprojectable, pixel size, NoData, band information, source metadata tags and size. It records an archive SHA-256 unless `--skip-sha256` is given. It never copies archive paths to a working destination and rejects unsafe member names. Its output has the status `archive_inventory_only`, explicitly not a species environmental result.

Example, **once the source archives and container executor are available**, for the accessible ZIP:

```bash
python scripts/likas_paic_raster_archive_inventory.py \
  --archive /mnt/data/likas_rasters/terrascope_download_20261031_201913.zip \
  --output /mnt/data/likas_raster_inventory_v04_PRIVATE.json \
  --inspect-raster
```

For multiple archives, repeat `--archive`. Include Rasterio in the Python environment if geospatial header reading is requested. For large members, ensure sufficient scratch disk and raise `--max-member-gib` only after validating file sizes. The inventories can contain local paths or raster tags; keep them private until reviewed.

Synthetic tests: `python -m unittest discover -s scripts -p 'test_likas_paic_raster_archive_inventory.py' -v`. This is a test command, not a claim of successful execution. A branch workflow runs Python compilation and tests without any private source data.

## Scientific quality gates before environment values

1. Inspect actual member names and raster header metadata, confirm relevant PAIC extent and source-provided layer roles (DEM/DSM versus land-cover), source version, resolution, vertical reference and land-cover legend/year.
2. Geographic polygon v0.3 is **provisional present-day major islands**, not a reviewed LGM PAIC boundary. Include smaller satellite islands only following evidence review.
3. Source coordinate candidates (3,487 rows) are private and have only been screened for coordinate quality. Earlier 59-row, 26-taxon source discrepancy and 155 unresolved taxon-linked records remain held.
4. Verify real raster coverage before any sampling. A masked NoData or off-coverage pixel is missing, never zero. Treat canopy-biased DSM surface height as a surface, not bare-earth elevation.
5. Save observation-specific raster values with tile ID, coordinate source, pixel/sample method, source acquisition date, datum and uncertainty. Distinguish land-cover label from literature habitat and from demonstrable habitat selection.
6. Species summaries must report number of screened observations **and distinct locations** and should not treat opportunistic presence-only records as absence, preference or full ecological tolerance.

## Mobile release scope

This branch only adds a scientific Python inventory utility, synthetic tests, CI and documentation. It does not modify Android survey code, startup, assets, application ID, schema, migration, production catalog or APK.
