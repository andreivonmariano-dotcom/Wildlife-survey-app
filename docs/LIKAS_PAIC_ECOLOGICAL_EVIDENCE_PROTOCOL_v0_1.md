# LIKAS PAIC ecological evidence guide | protocol v0.1
**Recorded:** 31 October 2026, Asia/Manila
**Status:** Proposed scientific analytical method. PRIVATE owner datasets were reviewed locally and were NOT committed here. No Android code, database schema or APK changes.

## Scope
Develop an evidence-informed ecological guide for reviewed Greater Mindanao PAIC vertebrates. Keep core PAIC and Camiguin Sur geographically distinct, and retain original taxon concepts, source versions and unresolved identifications.

## Mandatory claim distinctions
- Literature-recorded elevation identifies a documented collection/observation site, not a species' elevational tolerance or ecological optimum.
- Environmental raster elevation at a screened occurrence is the **location-associated value**, not an animal's measured altitude. Record terrain model / surface model, CRS, datum, resolution, version, pixel method and uncertainty. Copernicus GLO-30 is a digital **surface** model.
- Satellite land-cover class describes map cover at the layer's year and resolution, not necessarily habitat structure, microhabitat or conditions when an older observation was made.
- Opportunistic presence-only iNaturalist records describe reported occurrence. Do not assert habitat preference or selection without a defined availability comparison and bias/detection-aware design.
- Unassessed evidence remains “not assessed.” Missing is not zero.

## Proposed scientific pipeline
1. Link each Research Grade record to a source-checked accepted taxon through a versioned crosswalk. Leave ambiguous IDs unlinked.
2. Screen public unobscured observations using documented, approved CORE_PAIC and CAMIGUIN_SUR polygons. Apply boundary/positional-uncertainty checks. Separate marine/coastal evidence.
3. Exclude obscured/randomized coordinates from fine-scale raster sampling. Never expose private source coordinates or user identities. Preserve original public uncertainty and survey dates.
4. Report both observation count and distinct geographic locations, and audit spatial clustering/observer-site bias.
5. Sample licensed, versioned DEM/DSM rasters with unit/datum/method metadata. Do not conflate these with on-device GNSS ellipsoidal altitude.
6. Extract dated land-cover classes inside uncertainty footprints when justified; label remote-sensing proxies and temporal mismatch.
7. Attach original literature claims, site elevation, study methods, voucher status, DOI, taxonomy concept and reviewer to each species.
8. Display separate 'Published locality evidence', 'Screened occurrence-associated environmental values', and 'Unresolved/untested ecological inference' sections. Do not invent ranges for taxa without evidence.
9. Release a private reviewed reference pack only after licensing, QA, Android import/migration tests and sensitive-data checks. Keep source survey records intact.

## Verification status 31 Oct 2026
The owner-provided v3.8.0 and v3.8.1 PAIC workbooks and three iNaturalist project exports have been inspected locally. No approved authoritative polygon dataset, DEM/DSM raster, or land-cover raster was available during this session. No terrain or habitat raster values were computed, and no app importer or APK was built.

## Authorities
- iNaturalist DQA: https://help.inaturalist.org/en/support/solutions/articles/151000169936
- Copernicus DEM: https://dataspace.copernicus.eu/explore-data/data-collections/copernicus-contributing-missions/collections-description/COP-DEM
- ESA WorldCover 2021 v200: https://esa-worldcover.org/en/data-access
- Published voucher/montane habitat example: https://doi.org/10.1093/jmammal/gyac057
