# Philippine Wildlife Survey Beta v0.15.0

Offline Android wildlife field application for Birds, Herpetofauna and Mammals. Independent of SMART and KALUMBATA. No map UI in this release.

## Q2: repeatable routes with permanent stations
For **bird point-count** monitoring, survey setup now offers:
- **New route: generate unique stations**, with optional (on by default) **Save this route for future monitoring**, plus a route name.
- **Existing saved routes in the selected project**. Selecting one loads its recorded planned length, spacing and number of stations.

An initial 2 km route with 250 m spacing creates nine permanent IDs at chainages 0, 250, …, 2000 m. Saving a route template registers those same station IDs in `route_template_stations`. A subsequent survey using the same template starts fresh point-count visits at those *same station IDs*, not new sites. Each visit gets its own timestamp, point-count ID, GPS/accuracy and detection records. Historic station coordinates remain unchanged, and GPS deviation is flagged for review.

Only **point-count routes** are reusable as fixed monitoring stations. Bird line-transect and herpetofauna VES route lengths remain *design values only*. Their GIS tracks are captured per field event, not silently reused as geographic waypoints.

The database migration to schema 11 adds `route_templates`, `route_template_stations`, and `surveys.route_template_id`. Original surveys, station IDs, observation data and archived photographs are not discarded. ZIP export now includes `route_templates.csv` and `route_template_stations.csv`.

## Q3: scientific data quality and stability audit
Home now has **Scientific data review**, a read-only checker of:
- SQLite quick_check and orphaned survey, observation and unit links
- Invalid or missing station coordinates and absent GPS accuracy
- Missing, cross-project or incomplete reusable route station records
- Survey planned/actual station count mismatches
- Point-count completion that falls short of actual active station duration
- Survey completion despite unvisited stations
- Duplicate use of unrelated site identity, invalid detection counts and contradictory unknown taxa
- Large differences between repeat-visit GPS positions and stored station reference positions, considering uncertainty
- Missing photo files referenced by the observation evidence index

The audit never deletes or changes source records. **`data_quality_flags.csv`** is included in the ZIP export. A clean automated report is not proof of ecological validity. Direct handset GPS, camera behavior, migration integrity, field methods and taxonomic identification still require field review.

**Critical scientific correction:** for station-based monitoring, `observer_effort_minutes` is calculated from completed sampling-unit intervals with documented pauses removed. Travel between stations is not counted as stationary effort. The whole-session timer keeps recording actual elapsed time, and the second session clock is explicitly labeled *Session excluding pauses*, not sampling effort.

Automated Java tests check route planning arithmetic, including 2 km / 250 m = 9 stations, nondivisible route distances, zero spacing and numeric overflow. GitHub Actions runs unit tests before assembling the APK.

## Species catalog and sources

The app's existing built-in taxonomy is still a **small, unreviewed starter list**, not an exhaustive official Philippine species list.

### Mammals
Use the **American Society of Mammalogists Mammal Diversity Database (MDD) v2.5**, July 28, 2026, for up-to-date mammalian scientific names and *country-level* distributions.

In Home > Offline species checklist, choose **Import Philippine mammals from MDD v2.5 CSV**. Select the exact official source file `MDD_v2.5_6904species.csv` from the [MDD Zenodo record](https://zenodo.org/records/21654811). The importer verifies fields, selects records where `countryDistribution` explicitly includes the Philippines, and excludes taxa marked recently extinct or domestic. It imports taxon ID, scientific/common name, family, provenance and version into the local catalog. No province-level or faunal-region association is invented. Synonyms and reviewed locality evidence can be appended in later versions.

MDD v2.5 source: [mammaldiversity.org](https://www.mammaldiversity.org/) and [Zenodo 21654811](https://zenodo.org/records/21654811).

For Philippine and island-specific occurrence verification, consult [Field Museum Synopsis of Philippine Mammals](https://www.fieldmuseum.org/project-narrative/science/synopsis-of-philippine-mammals) and [Philippine Mammal Project publications](https://www.philippinemammalproject.com/publications.html). The Field Museum site is not a replacement for MDD v2.5 taxonomy updates.

### Amphibians and reptiles
Emerson Y. Sy maintains:
- **Checklist of amphibians of the Philippines**, version **2025-1**, updated February 25, 2025
- **Checklist of reptiles of the Philippines**, version **2026-1**, updated February 28, 2026

Those versions are documented on [Emerson Sy's research profile](https://www.researchgate.net/profile/Emerson-Sy). The app's attributed CSV importer is prepared to receive curated entries from the checklists. **Their full datasets are NOT bundled in this release.** Distribution, synonymy and redistributable source licensing need separate review. The [Reptile Database](https://www.reptile-database.org/data/index.html) provides a useful independent taxonomic cross-check. Avoid conflating lists of invasive/exotic pet trade species with native species inventories.

### Birds
AviList v2026 provides an attributed global avian taxonomic backbone under CC BY 4.0, but its global names alone cannot establish occurrence in Mindanao or other Philippine regions. Source: https://www.avilist.org/checklist/v2026/

Regional suggestions in the UI are **positive evidence only**. Any unassessed name remains available with the regional suggestion filter off.

## Field installation
v0.15 keeps Android application ID `org.phwildlife.survey.beta08` and the established persistent signing key. **Export your existing ZIP before updating in place**. Do not uninstall the older version. New SQLite version 11 keeps previous observations.

See `docs/QA_v0_15.md` for migration, saved routes, data quality, scientific methods and import checks. CI unit tests and APK compilation are not substitutes for physical-phone testing.
