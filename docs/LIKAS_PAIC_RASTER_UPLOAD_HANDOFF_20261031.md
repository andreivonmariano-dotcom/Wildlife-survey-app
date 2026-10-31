# LIKAS PAIC raster inputs: upload verification and processing gate

**Recorded:** 31 October 2026 (Asia/Manila)
**Scope:** Geospatial data availability and scientific provenance only. No private coordinates or raster data are committed.

## Project source files confirmed
The following user-supplied **Project source records** were returned by the file inventory. Sizes are exact source-materialization metadata where returned.

| File | Observed size | Local materialization result |
| --- | ---: | --- |
| `terrascope_download_20261031_201913.zip` | 67,734,369 bytes | Original bytes materialized to the current conversation's `/mnt/data` workspace |
| `terrascope_download_20261031_201741.zip` | 267,666,850 bytes | Project materialization rejected: exceeds 104,857,600-byte per-file limit |
| `rasters_COP30.tar_2.gz` | 132,469,974 bytes | Project materialization rejected: exceeds 104,857,600-byte per-file limit |
| `rasters_COP30.tar.gz` | 471,008,818 bytes | Project materialization rejected: exceeds 104,857,600-byte per-file limit |

*Materialization failure is not proof that the original upload is invalid or that a separate directly mounted attachment could not be accessed through a healthy container runtime.*

## Important distinction: discovered vs scientifically processed
- Files were discovered and one ZIP was copied locally by the Project file service.
- This session's container-backed Python and shell execution tools consistently returned `ClientError`, including on a trivial command. Consequently, **ZIP/TAR member lists, GeoTIFF presence, CRS, bounds, resolution, vertical datum, NoData, tile overlap and per-observation sampling could not be inspected**.
- Archive names are **not sufficient evidence** that they contain Copernicus DEM GLO-30 or any specific Terrascope land-cover product, class scheme or imagery year. Their authoritative metadata must be read from files before use.
- No observation-associated elevation values, cover classes or species ecology summaries were generated. No local raster consistency tests were performed.
- The prior provisional island footprints remain **draft**, and prior coordinate QA / unmatched-name / satellite-island exclusions still apply.

## Next attempt when local runtime is available
1. Check exact, currently mounted paths for all four archives before applying any Project materialization limits.
2. Inspect archive directory structure, compression, sizes and embedded metadata without extracting complete 471-MB collections unnecessarily.
3. Validate GeoTIFF CRS, pixel extent, missing-data flags, resolution, band meanings, DSM surface model vertical datum, and cover year/class legend.
4. Clip or use windowed raster sampling at privately staged, accepted-name-linked, unobscured high-quality public source locations. Evaluate repeated identical coordinates and reported positional uncertainty.
5. Keep unsupported geographic memberships / outside raster bounds / NoData / unresolved taxa **unassigned**. Preserve raw sources without writing exact sensitive coordinates to GitHub.
6. Produce private per-record and per-species QA outputs with a clear audit log, then validate at least several points against original geographic source metadata before ecological guide entries.

No Android app code, user survey records, SQLite schema or APK were changed in this attempt.
