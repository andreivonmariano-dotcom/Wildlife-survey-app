# LIKAS | Species, Habitat and Data Usability Roadmap

**Owner direction:** 28 October 2026. Deliver all three approved directions together: a richer Philippine Species Library, a credible Habitat and Ecology Atlas, and a safer project/backup/data model. Prioritize useful, intuitive controls including species dropdown suggestions and owner-controlled deletion of saved data.

**Branch:** `feature/likas-core-fieldwork-post-rc4` (draft PR #38); basis: signed v0.27.3 RC4, versionCode 49, SQLite 27. **Startup animation, forest assets and launcher icon are owned by a separate chat.**

## Changes implemented in this usability pass

### Fast species selection and reference retrieval

- `SpeciesReferenceSuggestionAdapter` queries the offline `taxon_catalog` and `taxon_synonyms` with a bounded 90-row SQL candidate set and up to 24 visible suggestions, preferring attributed checklist entries above historical starter records. It supports current common names, scientific names and former names. Suggestions do not create or overwrite observations.
- `TaxonDetailActivity` uses an autocomplete search bar and a dedicated, accessible **Show species suggestions** disclosure. Choosing an entry opens its source account. Existing search-button and keyboard Find routes remain.
- `SpeciesPickerActivity` offers the same dropdown, restricted to its selected taxonomic group, retaining an explicit **Unidentified** entry and evidence-first local species list. Selecting a suggestion narrows the finder; the user still chooses **Use species** before changing the live observation.

### Local data management

- A new **Manage saved data** screen is reachable from **Field tools & settings** on Home. It lists survey, observation, project, station, route, study-template and favorite counts.
- Users can permanently delete one selected **archived** survey, all archived surveys with dependent observations, field-effort rows, trap/coverboard/sign details and attached media, or individually delete unused stations, projects, routes and study templates. They can clear the whole project/station/route/template workspace after dependent surveys are removed, clear favorite species, or reset all user-generated field data.
- Every deletion requires an explicit typed confirmation. Dangerous bulk actions are visibly disabled during active surveys, and workspace reset is disabled until surveys are removed. Related saved draft and resume preferences are cleared after survey deletion. Active or paused surveys cannot be deleted. Bulk deletion uses an SQLite transaction and removes the known dependency graph in child-to-parent order. A default offline project is restored after a project/workspace reset.
- On successful database deletion, only known, app-managed original audio/photo filenames are removed from private storage. Any failure to remove attached files is disclosed. No arbitrary filesystem paths or external document-provider photos are targeted.
- Installed scientific checklists, taxonomic synonym crosswalks, locality evidence and diagnostic reference assets are **not** deleted through user-record management. These are source packages, not observation records.
- **Critical limitation:** Current CSV/ZIP research export is **not** a proven complete reinstallable backup. Deletion remains irreversible within this app. Whole-version restore, secure ZIP roundtrip, attachment archive and audit receipts remain next development priorities.

### Habitat and ecology entry point

- A new **Habitat & Ecology** Home destination opens `HabitatRecordsActivity`, a read-only view of stored station habitat labels and recent per-visit environment records with recorded habitat source, elevation, uncertainty and missing-data labels.
- It links back to Projects & Stations. No map layers, plant-cover classification or species occurrence inference is invented. Up to 35 rows are displayed from existing database data.

## Species Library: next scientific data work

**Present:** Birds, amphibians, reptiles and mammal catalog sources, source versions, aliases, attributed island/province evidence, indexed specimen names, 8 starter cited morphology profiles across four wildlife groups, reference-only conservation and habitat information. Those 8 profiles remain source-checked and **await independent expert diagnostic review**.

**Target species-account sections:** stable taxon/source ID; accepted name and authorship; common and verified local names; synonyms/taxon concept; sex/age/life-stage traits; morphological and acoustic diagnosis with clear uncertainty; similar-species comparison; genuinely cited island/province records separate from GPS site observations; natural history/behavior/microhabitats; recorded elevation range and datum; independent DENR/IUCN/CITES snapshot dates; taxonomy/source review chronology; cited media and reuse rights.

**Sequence:** (1) complete catalog/source reconciliation and missing-data counts; (2) build scientific review metadata and compare similar taxa within groups; (3) add expertly reviewed diagnostic keys and credited images/audio where licensed; (4) richer search filters and saved identification comparison sheets. Never infer local occurrence from national checklist inclusion.

## Habitat and Ecology Atlas: target design

**Existing:** Site habitat text and coordinates, `station_environment` per-visit habitat/elevation provenance, selected microhabitat/temperature/humidity fields on wildlife observation and method screens, GPS height with source/uncertainty.

**Next structures:** separate timestamped habitat observation, environmental measurement, vegetation structure, and map-layer source references. Every numeric measure needs value, unit, method/device, recorder, uncertainty/quality and time. Site identity, survey event and per-visit sampling unit must remain separate foreign keys.

**Recommended first content:** Philippine lowland dipterocarp, submontane/montane/mossy forest, limestone/karst, wetlands, riparian zones, coastal/mangrove and disturbed habitats, each with reference and scheme version. Avoid assigning habitat only from elevation.

**Later offline mapping:** user-selected basemaps and licensed data sources, protective filtering on sensitive stations, source/year/spatial resolution/coordinate reference system and offline coverage status. Never convert planned transect length into fabricated map geometry.

## Project/data organization and improvements after this pass

1. **Recoverability first.** Add a true current-version backup that includes user tables, media and checksum manifest. Write versioned restore staging/validation and crash-safe import rather than treating old v0.7 importer as new restore.
2. **Delete at finer scopes.** Improve current per-item survey/station/project/route/study-template controls with individually scoped audio clip and photo removal. Block deletion of active/dependent records until ownership is clear. Preserve a local deletion receipt and a clear record of which backup was exported, if any.
3. **Navigation and accessibility.** Keep high-frequency Home entries visible and archive/deletion tools discoverable but off the recorder. Support context-aware back navigation, visible filters, one-handed 48dp touch controls and clearer field labels. Add optional recent species and recent station shortcuts after source-reviewed design.
4. **Separate code responsibilities.** Extract survey session engine, taxon catalog repository, habitat evidence repository, project archive, and asynchronous aggregates from the large Android activities. Preserve original DB IDs and survey outcomes.
5. **Scientific outputs.** Extend existing ZIP, CSV, occupancy and GeoJSON exports with verified Darwin Core Event and Humboldt metadata mappings. Sensitive coordinates need explicit masking before externally shared exports.
6. **Future Research Watch.** Primary source metadata, original publication date, retrieval date, DOI/link, saved references and honest offline cache status, without blending news items into survey evidence.

## User-facing data vocabulary

- **End survey** records an outcome and keeps archival evidence.
- **Delete an observation** is a reversible-looking soft-delete recorded in its audit history, distinct from wholesale owner-confirmed permanent deletion.
- **Delete archived survey** permanently removes the selected event and linked records after confirmation.
- **Delete all surveys** removes the entire saved survey evidence archive only after all active sessions have ended.
- **Reset workspace** removes projects and reusable plans only after dependent surveys are removed, then restores a blank default local project.
- **Delete all my field data** is a complete *user-generated data* reset. It leaves the scientific reference library installed.

## Status and limits

This pass modifies current Android source, not startup scenes. No new SQLite schema or version code was introduced. No original field record was deleted during development. The deletion features are high-impact and remain on draft PR #38 pending build and device-level acceptance. Even a successful source/build check does not establish physical upgrade/restore safety or production readiness.

**Governing spec:** G-01 to G-10, U-05/U-06, T-01 to T-06, H-01 to H-05, D-01 to D-08, C-01/C-06, E-01 to E-06. Formal field-test planning remains deferred at the owner's direction.
