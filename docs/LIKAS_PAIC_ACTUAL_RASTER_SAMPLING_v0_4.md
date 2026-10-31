# LIKAS: Actual raster sampling of private PAIC observation candidates v0.4

**31 October 2026 (Asia/Manila)** | **Status: Local extraction verified, scientific interpretation provisional.**

## User source files and recovery
The user reuploaded split data files. All 3 chunked datasets were reconstructed in the local /mnt/data scientific workspace. Each reconstructed full archive matched the available truncated-original prefix. The valid full archives are:
- `rasters_COP30.tar.gz`: 471,008,818 bytes; SHA-256 `8f5639cd8f9f2558643d794714dfc21a88e8f03bb3c5bc77ebcabe8d06f1dcf9`
- `rasters_COP30.tar_2.gz`: 132,469,974 bytes; SHA-256 `1060ce4ed0f2357384de9a13dd12d19d558cfe65c4b8086a2a32f3f44539f93f`
- `terrascope_download_20261031_201741.zip`: 267,666,850 bytes; SHA-256 `1fd12a4ba2d2e73798546417c5c80d4a5e730e68242cc934179fcc74f881e337`
- `terrascope_download_20261031_201913.zip`: 67,734,369 bytes (previously available).
TAR streams decompressed to one `output_hh.tif` each; ZIP CRCs passed. The two height raster crops have EPSG:4326, 1/3600° spacing, no TIFF-encoded vertical datum, and unset NoData. They are *user-labeled* Copernicus DEM/COP30, not independently certified as bare-earth elevation. The two rasters overlap; 86 observation points sampled in that area had identical values in both.

The land-cover archives hold ESA WorldCover **2020 v100**, EPSG:4326, 10-m categorical land-cover maps (NOT 2021 v200). Across both archives there are 16 distinct class-map tiles, with 2020 legend and CC BY 4.0 tags embedded in the GeoTIFFs. InputQuality tiles were identified and not mistaken for class maps. Class code 0 is missing.

## Private extraction result
Starting from the 3,487 public unobscured <=100 m accuracy, accepted-name-linked, Research Grade iNaturalist observation candidates:
- 2,853 were inside the eight **provisional present-day principal island** polygons and outside the recorded position-accuracy radius of the mapped coastline.
- 22 near polygon edges were excluded from sampling. Another 612 lay outside the selected major island footprints, corresponding to v0.3 categories 118 on other current land and 494 off selected land/coast.
- All 2,853 accepted polygon-inside positions yielded numeric user-supplied COP30 *surface-associated* height values. Three were -0.5 m and are flagged as coastal/reference review. The raster datum remains **unverified**.
- 2,076 of the 2,853 (all with reported positional accuracy <=30 m) yielded a valid ESA WorldCover 2020 class. Sampling a 10-m cell cannot establish field habitat when reported accuracy is up to 30 m.
- At species concept level, **221** of 725 have one or more sampled surface heights and **205** have one or more sampled WorldCover 2020 classes. Species with no qualifying points have **missing evidence**, not absence or zero ecological tolerance.
- WorldCover temporal check: of 2,076 class assignments, **1,960** were applied at later observation dates, **52** earlier and **64** dated 2020. These are temporally mismatched proxies for many records.
- The 59-row source-locality discrepancy across 26 taxa remains; 925 currently inside-polygon observations are from these held taxa and cannot be treated as trusted ecological evidence until reconciled. The 155 unresolved taxon-linked records were not used.

## Verified tests and privacy
A local independent audit validated the 3,487 observation IDs as unique, 725 species totals, 2,853 sampled surface heights, 2,076 class codes, and 221/205 species counts. It repeated 12 raster pixel reads matching the observation CSV. It checked 86 overlap points, all with 0 m height discrepancy in those two crops. The summary workbook was inspected and exported, and the private package ZIP passed CRC.

**No observation coordinates, raw uploaded raster bytes, or privately attributed species-locality dataset were committed to GitHub.** The raw point-level sample CSV is available only in the originating private conversation deliverable, labelled PRIVATE. The companion 725-row species summary has no coordinates and is still explicitly provisional.

## Interpretation guardrails
No analysis compared usage with environmental availability, corrected for observer or detection bias, or established habitat selection, preferences, elevational tolerances, geographic absence, habitat suitability, or spatially predictive distribution models. Landsat/Sentinel classification 2020 is not a species microhabitat description. The species guide still needs reviewed literature descriptions, original source-name reconciliation, specialist-reviewed PAIC and satellite-island geometry, source DSM vertical datum verification, temporal/accuracy uncertainty review, and distribution/privacy QA.

## App release state
This is an offline scientific research analysis, not an Android app update. Existing RC55 draft, SQLite data, code signing, owner field observations, app assets and APK are unchanged. Scientific outputs must not be imported as approved species ecological preferences.
