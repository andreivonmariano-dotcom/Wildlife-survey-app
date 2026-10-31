# LIKAS Greater Mindanao PAIC ecological guide | evidence policy lock v1.0
**Decision date:** 2026-10-31 (Asia/Manila)
**Owner decision:** Approved guide framing; no specialist sign-off gate and no generic "provisional evidence" label on species cards.
**Scope:** Versioned reference rules and the existing private PAIC v0.4 research inputs, **not** a claim that all 725 species have complete habitat descriptions or that an Android APK has been released.
**Evidence baseline:** Harmonized private v3.8.0, taxonomy/spatial v3.8.1, iNaturalist projects 806422/806424/806426, GSHHG present-day principal-island outlines v0.3, owner-supplied COP30-labeled DSM crops (datum absent in TIFF tags), ESA WorldCover **2020 v100**.

## Product decision and public wording (hard-locked)
The purpose is a usable **evidence-informed guide to likely environmental associations**, assembled from recorded locations, digital surface elevation, satellite-mapped land cover and published habitat accounts. It is not a habitat-selection, distribution, niche-envelope, occupancy, or actual/full elevational preference study.

- Headline: **Likely elevation and habitat associations**. Short subtitle: "Based on documented sightings, mapped environments and published accounts." Do not display the generic scientific-stage label "provisional" on each species.
- A numeric value from a DEM/DSM is **surface elevation at a screened observation location**, not animal altitude, selected elevation or a complete elevational range. The COP30 raster's source-reported product type is DSM; **do not assert its vertical datum until checked against the originating export metadata**.
- ESA WorldCover **2020 v100** 10 m classes describe *mapped land cover at the public sighting location*, not observed vegetation structure or habitat preference. Land-cover year and positional accuracy remain disclosed in Research details.
- Literature observations are reported as **Published habitat accounts** or **Published locality elevations** with linked author/year, locality scope, original taxon name/concept and DOI/URL. Do not silently replace site-specific descriptions with an island-wide statement.
- "Likely" is ordinary guide language reflecting documented associations, **not a calculated probability**. Do not show confidence percentages, probability ranks, "optimal", "strict", "restricted to", "avoids", "absent from", or "preferred" without separate valid comparative evidence.
- Where the guide lacks a value, show "Not documented in the sources used", or omit the field. Unknown is never zero.
- Specialist endorsement is **not a release prerequisite**. Verified references, valid identifier mapping, redistribution permissions, source QA and truthful category labels *are* required mechanical integrity gates.

## Data paths for every taxon
1. **Citizen science + literature:** Show an observed surface-elevation distribution when usable screened locations exist. Add published site elevations and separately cite published habitat accounts. Present WorldCover class counts as mapped associations, not field habitat.
2. **Citizen science only:** Show location-associated DSM elevations and mapped 2020 land-cover associations as relevant, always showing independent location count and observation count. Do not silently infer exact forest formation from Tree cover.
3. **Literature only (zero usable coordinates):** Use source-reported habitat settings, environment/vegetation terms, microhabitat and only those numeric elevations explicitly reported by a traceable source. A "montane forest" account permits the *qualitative* habitat descriptor, but does **not** authorize an arbitrary minimum or maximum meter value.
4. **Neither source present:** Keep the taxon in the catalog, show "No habitat or elevation account documented in the current guide", and allow later source addition. Do not borrow attributes from congeneric taxa without explicit, supported genus-level wording that is not portrayed as species-specific.
5. **Ambiguous taxonomy:** Preserve all original names and source records. Do not assign 155 unresolved iNaturalist records to one of the 725 accepted concepts without a defensible crosswalk.

## Suggested species-card presentation
**Likely environments** — short source-grounded habitat text; if both literature and land-cover exist, make it clear which came from which.
**Elevation guide** — "Raster surface elevations at recorded sites: X–Y m across N distinct public locations (M observations)" OR "Published collection sites: X and Y m"; never "species elevational preference: X–Y m".
**Evidence used** — "iNaturalist public observations"; "WorldCover 2020 v100"; "published records (Author, Year)" as applicable.
**Research details** (expandable) — original observation count, unique coordinate count, sample year span, selection criteria, accuracy, raster product/resolution/datum, map year/legend, specific references and known gaps. This section preserves evidence status and unresolved QA without displaying a generic provisional banner.

## Fixed numerical evidence inventory (v0.4, private)
- **725 taxa:** 414 birds, 156 reptiles, 53 amphibians, 102 mammals.
- 3,487 public unobscured RG accepted-linked coordinate candidates with reported accuracy <=100 m. Draft present-day major-island screening: 2,853 inside, 22 near shorelines, 612 beyond those principal-island footprints. Core PAIC and Camiguin Sur are separate review areas. This is not a reviewed palaeogeographic PAIC boundary.
- 2,853 within-polygon location-associated COP30 raster values; **221 taxa** with one or more values. Three -0.5 m samples require review and must not generate a positive habitat interpretation automatically.
- 2,076 within-polygon public records with accuracy <=30 m and valid WorldCover 2020 class; **205 taxa** with one or more mapped classes. Observation year differs from 2020 for 1,960 later and 52 earlier records.
- **26 taxa** have an unresolved source-locality-count reconciliation affecting 59 prior-vs-new candidates (925 currently inside points of these taxa). They can receive independently cited literature accounts immediately, but do not represent their disputed raster series as an unrestricted, verified regional summary.
- **504 taxa** lack this extraction's screened DSM samples. This is **not** proof they have no habitat information in literature.
- The user-private no-coordinate species summary was independently parsed: **725 unique taxon IDs**, 725 rows; sums 3,487 prepolygon, 2,853 inside, 2,853 DSM samples, 2,076 WorldCover classes, 221/205 taxa with those types. **No full 725-species published-habitat text review has been performed**.

## Descriptive sample-size rules
- Always report occurrence records **and distinct public coordinate pairs**; do not present repeated observations at one point as independent sites.
- One usable location: "Recorded at one mapped location", with its surface elevation if valid. Two to four distinct locations: describe sampled site values without suggesting a robust elevational distribution. At five or more distinct locations: the observed p10–p90 and min–max may be shown, labeled as *recorded-site* values, not preferred/typical species range.
- Mapped class frequency can describe the recorded points only. It cannot be termed "preferred habitat", and 10 m classes with up to 30 m reported coordinate uncertainty must have a method caveat.
- Published site elevation and raster elevation are separate numerical arrays; never silently merge them into one range.

## Immutability, reproducibility and licensing
- Source workbooks, private point records, GeoTIFFs and raster metadata are immutable raw inputs. Changes create a new imported evidence version and hash, never overwrite original value, coordinates, source names, or original citations.
- The guide display/data rule version is **PAIC-ECOGUIDE-1.0**. Any future change to this locked interpretation needs a new version plus a dated append-only decision entry. The existing scientific v0.4 sample dataset is a frozen baseline, not itself renamed as independently approved environmental truth.
- Never publish sensitive coordinates, observation-level private files, private GIS candidate datasets or unlicensed third-party text/media in GitHub or APK assets.
- A future compiled offline release may include only the reviewed-for-rights, coordinate-free, attributable species summaries and authorized imagery; gate source licensing, file integrity, taxonomic joins and app regression QA. No mandatory specialist review gate.
- This lock applies to reference **methods and presentation**. It does not waive factual QA, revise original ecology literature, prove actual species distributions, approve the provisional island outlines, or modify the RC55 Android app.

## Governing requirements
G-01, G-03, G-04, G-05, G-08, G-09, G-11, G-12; T-01/T-02/T-03/T-05/T-06; H-01/H-03/H-04; L-02; D-01/D-06; U-07/U-09; C-02/C-05; E-02.
