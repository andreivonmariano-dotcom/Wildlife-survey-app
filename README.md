# Philippine Wildlife Survey Beta v0.13

Offline Android application for bird, reptile, amphibian and mammal fieldwork. The application is independent of SMART and KALUMBATA.

## What changed

### Q1. Offline taxonomic reference and versioned import
- New **Offline species checklist** screen under Home. The local database includes the original small starter list, explicitly classified **unreviewed**.
- Import up to 45,000 rows from a researcher-prepared, UTF-8 CSV. Required columns: `taxon_key,taxon_group,common_name,scientific_name,family,source,source_version,region_codes,review_status`.
- Taxon groups: Birds, Reptiles, Amphibians, Mammals. Every taxon has an externally traceable ID, a source, and source version. Import is all-or-nothing. Existing field observation rows are not rewritten.
- A reviewed province occurrence can be marked with `P:<province_PSGC>`, e.g. `P:1102400000`. For broader evidence use `MINDANAO`, `VISAYAS`, `LUZON`, `PALAWAN`, `MINDORO` or `SULU`. Multiple codes use `|`, and a blank value means **not assessed**.
- Only `reviewed` imported taxon-region rows enter the restricted regional dropdown. Imported names without regional evidence remain accessible with the regional filter off. Users can always type a taxon absent from the shortlist.
- **Important:** this release does NOT contain a complete vetted Philippine biodiversity checklist. The species database supports substantial imports, with taxonomy and occurrence evidence kept separate. Do not equate unreviewed starter names with accepted current taxonomy or a comprehensive regional checklist.

Recommended 2026 bird-name backbone: AviList v2026, released 20 October 2026 and licensed CC BY 4.0, https://www.avilist.org/checklist/v2026/ . Regional distribution requires separate documented occurrence evidence. Reptile/amphibian/mammal taxonomies need separate specialist source review.

### Q2. Real offline Philippine province polygons
- Bundled offline low-resolution GeoJSON from `faeldon/philippines-json-maps`, PSGC **Q3 2026**, MIT-licensed. The app uses 88 province/province-equivalent features. It retains province names and PSGC codes.
- Source: https://github.com/faeldon/philippines-json-maps
- Polygon data have been rounded to four decimal places for small APK size. Geographic source revision: `PSGC-2026-Q3-lowres-v1`. Attribution required by project terms.
- Survey setup adds **Suggest province & checklist area from station/GPS**. It first uses registered station coordinates with known uncertainty. If not available, it tries a recent (under 5 minutes) accurate phone GPS fix. The location suggestion remains **opt-in** and never changes the selected area without confirmation.
- Point-in-polygon tests include holes and multipolygons, multiple matches, outside-data regions and near-boundary caution. GPS fixes worse than 200 meters and points less than 700 meters from boundaries are not automatically accepted as reliable assignments.
- Manual selection remains available. A geographic administrative polygon is NOT a faunal region or proof of species occurrence. Some archipelagos have special classification rules. Administrative province and manual island-checklist preference stay separate.
- `surveys.csv` exports the province, PSGC, geometry version, status and selection method when present.

### Q3. Faster wildlife entry and photographs
- Star ☆ beside the taxon entry saves locally favorite species or unidentified labels for fast retrieval.
- New **identification confidence** choice: Field-identified (not expert verified), Probable, Uncertain, Unidentified. Raw observations export confidence and supporting notes. Uncertain and unidentified sightings are excluded from preliminary species-level model candidates.
- Bird distance-quality, reptile/amphibian environment and mammal evidence sections are collapsible to reduce scrolling. Critical species, count, distance, and Save Observation remain accessible.
- **Attach photo from phone** opens Android's document picker. The selected image is immediately copied into private app storage (maximum 15MB per image), preserving it when the source file moves or permission expires.
- After the observation is saved, the photo is linked to its record ID. Survey History has **View photo evidence**. The ZIP export includes `photo_evidence_index.csv` and actual image files in `photos/`.
- Draft restoration retains the pending photograph, identification confidence and interpretation note.

### Scientific retention
Not Counted surveys are still archived but omitted from scientific/monitoring datasets. Survey effort pauses, two clocks, distance candidate flags, source provenance and station-level sampling history remain intact.

## Installation
v0.13 shares the Android application ID `org.phwildlife.survey.beta08` and cached signing key with v0.12. **Export a full backup ZIP before upgrading.** Install v0.13 over v0.12 without uninstalling. The migration upgrades the SQLite database to schema version 9 without dropping survey observations.

## Planned for future scientific maturity
- A vetted, country-wide species and synonym database based on accepted specialist checklists, including up-to-date sources and regional distribution records
- Fresh GPS fixes with documented uncertainty and multiple boundary products, including islands/biogeographic divisions
- More species-level validation checks, photo metadata/exif retention policy and direct camera capture with controlled evidence chains
- Detailed accessibility/field usability tests on small Android screens

See `docs/QA_v0_13.md` for device validation and `docs/TAXON_AND_BOUNDARY_FORMAT_v0_13.md` for import format and source provenance.
