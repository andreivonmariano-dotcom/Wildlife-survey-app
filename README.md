# Philippine Wildlife Survey Beta v0.12

Offline scientific field recording for Birds, Herpetofauna and Mammals. No SMART or KALUMBATA integration.

## New: survey geographic scope and region-aware species lookup
- After choosing Birds, Herpetofauna or Mammals, survey setup now asks for **Geographic checklist area**. Available choices are all Philippines / unspecified, Mindanao, Visayas, Luzon, Palawan, Mindoro, Sulu Archipelago, or other/uncertain.
- The **geographic region** is a checklist suggestion setting, independent of the **stratum label** used for density or survey sampling. The source of geographic selection is currently `manual_region_choice_no_polygon`.
- When the survey starts, its region code, chosen method and the `field-curated-region-evidence-v1-2026-10` revision are saved in the survey database, observation metadata and exported CSV. Changing regions requires a new survey.
- A **Regional suggestions only** control narrows displayed options to a small, positively documented starter subset. Switch it off to display the complete built-in starter list. The selected locality cannot make the observation form reject any other taxon typed by the observer.
- Regional evidence metadata are **positive-only**. A name without a regional match means **not yet assessed**, never confirmed absent. The regional subset is NOT a complete checklist for that island.
- For amphibians there is currently **no independently curated island-specific subset**. The app falls back to all amphibian starter names rather than displaying an empty list.
- For each taxon group, the **Unidentified** button opens non-species identity categories: unidentified bird, raptor, owl, passerine, frog, toad, snake, lizard, bat, rodent, etc.
- Such entries have `identification_level=unidentified`, no fabricated Latin binomial, and remain available in raw biological observation exports. New unidentified entries are omitted from preliminary species-level distance and SDM model datasets. Free-text entries remain supported and are flagged for review.
- Re-identifying an observation in Survey History recalculates its rank, regional evidence and distance candidate status.

## Assigning geographical boundaries: scientific design
This release **does not claim to classify phone GPS coordinates into Mindanao, Luzon or other islands**. User-selected names are saved with provenance and are distinct from polygons.

A geographic polygon release must:
1. Use legally reusable, versioned **WGS84 multipolygons** for actual island coastlines and, separately, administrative regions and provinces. The two geography classes are different.
2. Store boundary source, date, license, resolution, geometry hash and the interpretation of Mindanao (main island vs greater faunal group vs political regions).
3. Run **point-in-multipolygon** on a fresh recorded GPS fix, considering accuracy. If a fix overlaps a boundary or accuracy is poor, return **uncertain**, never force a location.
4. Offer a manual override that preserves both GPS-based proposal and observer decision, with the reason and timestamp.
5. Preserve coordinates separately from area labels. Neither an admin-region label nor the name 'Mindanao' is evidence of a species' presence.
6. Use documented and versioned species-region evidence with review and taxonomic synonym reconciliation before expanding the full offline checklists.

**Potential boundaries:** geoBoundaries `gbOpen` administrative GeoJSON under CC BY 4.0, or appropriately licensed Philippine administrative and island polygons. Administrative regions alone are not suitable proxies for faunal regions.
The project's existing `AdminMapPH.zip` file contains a JPG image, not usable polygon geometry.

## Taxonomic data integrity
The app currently uses a small, unreviewed offline starter list. Regional matching covers only positively checked entries. The regional Bird suggestions partly derive from the 2026 Avibase Mindanao checklist, and examples for the other groups derive from peer-reviewed surveys and specialist sources. This does not mean the full lists can be redistributed without reviewing their licenses.

For scaling up: select a fixed version of AviList or eBird/Clements for accepted bird taxa and IDs, plus the Wild Bird Club of the Philippines regional material where permission allows. Use Amphibian Species of the World and the Reptile Database with separate specialist/mammal taxonomic references, preserving source version, ID and synonymy. Store checked occurrence references separately from taxonomy. Review elevation, seasonal movement, introduced taxa, questionable coordinates and habitat.

## Screen usability priorities
- Keep **species, number, encounter modality and Save Observation** on the primary screen.
- Keep detailed behavior, weather, distance-quality and sampling-protocol options in collapsible areas.
- Prominent **Unidentified** and **Regional suggestions only** controls reduce typing.
- Repeat locations, observers and environmental variables between visits without overwriting the observation event.
- Survey History remains the source for audited corrections and Not Counted exclusions.

## Data policy and installation
Schema version 8 adds `geographic_region_id`, `geographic_selection_method`, `regional_taxa_version`, `identification_level`, `regional_evidence_status` and `geo_region_at_detection`. Older records remain, marked as legacy with no validated region.

v0.12 uses the same application ID `org.phwildlife.survey.beta08` and cached signing key as v0.11. Export a full backup ZIP from the previous version, then install v0.12 as an update. Verify survey and observation counts after upgrade.

## References
- geoBoundaries open geographic data: https://www.geoboundaries.org/api.html
- Avibase Mindanao checklist: https://avibase.bsc-eoc.org/checklist.jsp?region=PHmn
- WBCP checklist terms and regional coverage: https://birdwatch.ph/philippine-checklist/
- AviList versioned taxonomy: https://www.avilist.org/checklist/v2025b/
- eBird/Clements 2025 taxonomy: https://www.birds.cornell.edu/clementschecklist/introduction/updateindex/october-2025/2025-citation-checklist-downloads/
- Reptile Database species distribution: https://reptile-database.reptarium.cz/Hydrosaurus/pustulatus
- IUCN SSC pig distribution: https://www.iucn-wpsg.org/visayan-warty-pig-detailed
- GBIF event and coordinate-quality guidance: https://techdocs.gbif.org/en/data-publishing/data-quality-recommendations

See [field QA plan](docs/QA_v0_12.md).
