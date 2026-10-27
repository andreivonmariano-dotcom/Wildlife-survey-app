# LIKAS
## Master Project Charter, Product Requirements, and Scientific Standards

**Version:** 1.0 (foundational specification)  
**Date:** 27 October 2026  
**Product:** LIKAS, a Philippines-first biodiversity field and knowledge platform  
**Status:** Proposed authoritative project specification, pending adoption into Project instructions and the development repository  
**Intended audience:** Product owner, wildlife biologists, ecologists, designers, developers, scientific reviewers, and future development assistants  
**Repository reviewed:** `andreivonmariano-dotcom/Wildlife-survey-app`, branch `main`, commit `44825d70956151839bd8d90b14ef799c5ce8ea76`  

> **The LIKAS promise:** One dependable tool for recording, understanding, analyzing, and keeping current with Philippine biodiversity. Field records come first. Scientific credibility is never traded for visual polish or convenience.

---

# 1. Vision and identity

**LIKAS** will grow from an offline wildlife-survey Android application into an all-in-one biodiversity toolkit designed for field biologists and ecologists working in the Philippines. Its core purpose is to connect four workflows in one coherent product: **field surveys**, **species knowledge**, **habitat and ecological context**, and **wildlife research and conservation updates**.

The product is grounded in Philippine biodiversity, its taxonomic complexity, island biogeography, field conditions, conservation needs, and research practice. It must support professional field studies without forcing users to switch among scattered notebooks, checklists, GPS utilities, spreadsheets, and literature feeds.

**Product mission.** Make sound field documentation and access to relevant biodiversity knowledge faster and more dependable, including in remote areas without mobile data.

**Primary audience.** Philippine wildlife biologists, graduate students, field researchers, ecological consultants, conservation workers, protected-area staff, biodiversity monitoring teams, and academic collaborators. Wider natural-history users can be served through simpler modes later, without diluting professional field standards.

**Long-term position.** A unified research companion, not merely a species identification app, not a social network, and not a replacement for scientific review, permits, or established analytical methods.

# 2. Product pillars and success criteria

## P1. Field Survey Studio
Record standardized and opportunistic biodiversity observations with method-specific effort, accurate event relationships, photos, field coordinates, and trustworthy export. This is the flagship module and must remain usable offline.

## P2. Philippine Species Library
Provide searchable, versioned taxonomic references for Philippine biodiversity. Show identification information, known distribution, habitats, ecology, endemicity, national and global conservation assessments, and citations. Clearly separate vetted information from incomplete or unreviewed data.

## P3. Habitat and Ecology Atlas
Give structured context for vegetation, ecosystems, microhabitats, climate and weather metadata, elevation and terrain, disturbance, and geospatial features. Never manufacture a habitat class, elevation, or occurrence locality from insufficient evidence.

## P4. Research and Conservation Watch
Show recent, sourced Philippine biodiversity papers, discoveries, research methods, conservation policy updates, and selected credible news. Each entry must display its publication date, source, original link, and whether the record is a research paper, preprint, official notice, or news report.

## P5. Analysis and Research Export
Make collected data inspectable, portable, reproducible, and appropriate for downstream tools such as R, GIS, statistical analysis packages, and biodiversity data repositories. Report sampling effort and limitations next to results.

**Success means:** reliable field capture; retained data across upgrades; defensible survey status and effort; traceable species references; fast retrieval without internet; a clean interface in rain, gloves, sunlight, and darkness; exportable raw data; and research/news entries that readers can verify.

# 3. Non-negotiable development rules

**G-01 | Scientific correctness outranks appearance.** The app must not imply that an estimated distance, GPS altitude, regional checklist, or automated analysis is more accurate than its source supports.

**G-02 | Offline field work is mandatory.** Starting, pausing, recording, finishing, reviewing, and exporting core surveys cannot depend on a server. Connected features degrade gracefully.

**G-03 | No silent data loss.** Never delete or rewrite original survey, observation, media, source, or identifier data through upgrades, cleanup, import, or design changes. Use migrations, backups, and explicit audit records.

**G-04 | Preserve provenance.** Every curated biological assertion and imported name requires its reference, version, retrieval date where relevant, and review status. Preserve original identification text and subsequent determinations separately.

**G-05 | Unknown is not zero.** Missing elevation, GPS, effort, distance, counts, identification, or absence data must be recorded as missing, unknown, or unavailable, not assigned a plausible-looking value.

**G-06 | Method requirements control completion.** A method is only complete after its configured mandatory sampling units and effort criteria have been satisfied. Users can end early without falsely marking completion.

**G-07 | Simple field interactions outrank decoration.** One clear primary action per stage. All survey exit states are accessible. Never bury recording controls under startup animation or unnecessary explanations.

**G-08 | Sensitive locality protection is built in.** Exact locations of vulnerable taxa, restricted survey sites, and culturally sensitive knowledge must not be automatically published or added to public feeds.

**G-09 | One coherent data model.** Survey setup, saved routes, station visits, observations, the species library, habitat measurements, media, and exports share stable IDs. No parallel features with conflicting definitions.

**G-10 | Existing records remain compatible.** Maintain the existing Android application identity and signing continuity through updates, unless a separately approved migration strategy exists. Test upgrades against archived real-world data copies.

**G-11 | Accurate product claims.** Label every feature as implemented, prototype, planned, or blocked. A passing build is not proof of field or scientific validation.

**G-12 | Source licensing matters.** A downloadable personal/scientific checklist is not automatically licensed for redistribution inside an app. Record licenses and permissions prior to bundling, copying images, or distributing third-party full text.

# 4. Current baseline as of 27 October 2026

The connected repository documents an offline Android application at **v0.15.0** with SQLite-backed projects, survey events, observations, bird point counts, herpetofauna and mammal survey options, photographs, route templates, a scientific data review screen, and ZIP/CSV exports. It contains a CI workflow for testing and APK creation. The existing project is a functional foundation, not a finished all-in-one biodiversity platform.

**Verified in repository documentation and code:**
- Three main survey profiles: birds, herpetofauna, and mammals; some associated methods are explicitly basic-log only.
- Reusable saved routes with permanent station IDs for bird point counts. A 2,000 m design with 250 m spacing creates nine station chainages, from 0 through 2,000 m.
- Fresh GPS at station visits, with station locations distinguished from planned chainages and from repeated visit measurements.
- Session duration, station timers, and observer sampling effort treated as different quantities.
- A read-only scientific integrity check and ZIP export of data-quality flags.
- A mammal importer for the official MDD v2.5 CSV, subject to source-file selection and version validation.
- A small embedded starter taxon list. This is **not** a comprehensive national checklist.
- Existing forest day/night image plates, an animation class, and adaptive launcher branding. The repository itself says the forest animation was not yet connected to the startup activity at the examined commit.

**Decisions established during this project's conversations but not to be assumed complete in the released APK:**
- A visible **End Survey** action throughout an active session, with **Complete**, **Incomplete**, and **Cancel / Not Counted** meanings.
- Automatic elevation updates from a sufficiently fresh, valid GPS location fix, with traceable source and uncertainty. No silent replacement of previously saved records.
- Use of the provided Philippine amphibian checklist (Sy 2025-1), reptile checklist (Sy 2026-1), and WBCP 2025 bird checklist, subject to verification, exact source mapping, and redistribution permission.
- A high-quality tropical forest startup design with gentle motion and phone-time-aware day/night transitions.
- A minimalist pixel-style frog emerging halfway from pond water as the preferred icon direction, with no lily pad; this later design decision supersedes the busier installed icon concept and requires asset review before replacement.
- A thorough reduction of duplicate controls, stale labels, text artifacts, and explanatory clutter.

**Do not label as shipped without an APK and device test:** the full provided checklists, live research feed, complete species profiles, GIS habitat atlas, full animation integration, and new minimalist frog icon.

# 5. Product structure and navigation

The target information architecture has five top-level destinations: **Survey**, **Species**, **Habitats**, **Research**, and **Library / My Data**. Use names that remain clear on small screens. Keep the active survey accessible through a persistent resume shortcut.

**Home.** Present Start Survey, Resume Survey when needed, recent projects, offline-data status, and direct entry to species or research resources. The startup artwork is secondary to readable, responsive controls.

**Survey.** Project → method → site/route → parameters → active field session → review → end state. Keep forms short and show contextual fields only for the selected protocol.

**Species.** Search common, scientific, synonym, and unresolved names; browse taxa by group and Philippine geographic evidence. A species detail page must distinguish taxonomic identity from confirmed local occurrence.

**Habitats.** Browse ecological reference material, field habitat classifications, environmental measurements, and user-authorized map layers. Offline explanatory content must remain available without network access.

**Research.** Filter by date, taxa, geographic region, topic, publication type, and evidence source. Save references for offline reading only when the provider's license permits local storage.

**My Data.** Projects, registered sites, saved routes, active and archived surveys, media, audit reports, import history, exports, and backup/restore tools. Not Counted events stay in the archive but are clearly excluded from analytical summaries by default.

# 6. Survey lifecycle: strict state model

**S-01 | The app must represent distinct states:** Draft, Active, Paused, Completed, Incomplete, and Not Counted / Cancelled. The exact database implementation may use status fields or derived states, but exports must preserve these meanings. The UI must not treat Not Counted as a survey with zero observations.

**S-02 | The End Survey control is present throughout an Active or Paused survey.** It remains available after the planned final transect, station, or interval, not hidden behind a method screen. Ending early is allowed.

**S-03 | Complete.** Offered only when required method-specific criteria have been satisfied: survey setup is valid, required stations or sampling units were addressed, minimum active effort is met, and necessary finish metadata are supplied. Optional fields do not block completion.

**S-04 | End Incomplete.** Available at any point after the session begins. Save the observed work, actual duration, completed and missed units, reason when supplied, and end timestamp. The survey remains available for analysis only with explicit completeness filters.

**S-05 | Cancel / Not Counted.** Must require deliberate confirmation, state the impact, and record a reason or a defined user-decline value. Retain the event and linked records for audit, yet exclude it from normal analytical denominators and reports. No silent deletion.

**S-06 | Resume and interruption.** App restarts, phone calls, loss of GPS, screen rotation, and backgrounding must not create duplicate events or station records, reset total elapsed time, or silently turn an incomplete unit into a complete one.

**S-07 | Zero-detection surveys remain valid.** A completed, documented sampling effort with no detections is a zero-detection event, not a species absence claim outside the stated effort and detection context.

**S-08 | Structured methods and basic logs differ.** For every method, display whether it supports full effort-based analysis or only opportunistic/basic records. Do not market basic-log export as scientifically standardized sampling.

**S-09 | No unannounced survey mutation.** Changing route parameters after observations exist requires a controlled edit, linked change history, and preservation of original design metadata.

# 7. Supported survey methods and planned expansion

**Birds, P0.** Fixed-duration point counts, reusable station routes, line transects, casual sightings, auditory detections, flyovers, and raptor encounters. Record observer, dates, station/route, actual effort, detection modality, species confidence, flock size, initial horizontal radial or perpendicular distance where relevant, bearing reference, weather and habitat as available. A candidate distance-sampling flag is not proof of model suitability.

**Herpetofauna, P0.** Visual encounter surveys, timed microhabitat search, transects, opportunistic observations, coverboard checks, and amphibian call records as protocols mature. Record amphibian/reptile group, species or unresolved identity, count, life stage where possible, calling/visual detection, microhabitat, search effort, day/night context, site conditions, and measurement/photo evidence. Keep survey search effort separate from encounter count.

**Mammals, P0/P1.** Camera-trap deployments and retrievals, direct sightings, acoustic bat detections, tracks/sign, spotlighting, live-trap event logging where authorized, and opportunistic encounters. Camera surveys must eventually support independent deployment periods and usable camera-trap days. Live-trapping must not be inferred from an ordinary sighting entry.

**Flora and vegetation, P1.** Plots, quadrats, belt transects, species inventories, DBH, height, basal-area inputs, canopy cover, regeneration classes, phenology, geotagged mother trees, and habitat structure. Plot shape, area, inclusion threshold, plot ID, and measurement units are mandatory for comparable structure metrics.

**Further taxa, P2.** Insects, freshwater taxa, fungi, and marine/coastal biodiversity become separate protocol modules only after their reference sources, domain reviewers, and required sampling fields are defined.

**Method rule:** A method profile includes a unique code, scientific purpose, required fields, optional fields, effort definition, finish criteria, export schema, unit tests, and human field-test script. Project-defined templates may adjust parameter values but cannot falsify or remove minimum scientifically necessary metadata.

# 8. GPS, elevation, route geometry, and measurements

**L-01 | Latest defensible GPS fix.** Display timestamp, horizontal accuracy, fix age, and location source where available. A stale fix must not be represented as the current station coordinate. Existing code uses a candidate freshness of at most 30 seconds and horizontal accuracy of at most 75 m for point-count station start, subject to future scientifically justified, protocol-specific settings.

**L-02 | Automatic elevation.** Read device-reported altitude from a fresh location fix only when present and credible. Record value in meters, vertical datum or altitude reference where known, method (GNSS/DEM/manual), fix time, and reported vertical accuracy if available. Geoid height and ellipsoidal altitude are not interchangeable. If no valid altitude exists, show “Unavailable” rather than 0 m. Do not call raw GNSS altitude verified ground elevation.

**L-03 | Snapshot rather than silent overwrite.** Each observation and station visit receives its own associated fix, or an explicit reference to a shared fix. New location updates may refresh a live display but must not rewrite old records without a tracked correction.

**L-04 | Real vs planned geometry.** A route length or chainage is a design parameter, not a geographic line. Planned bird stations cannot be placed on a map through fabricated coordinates. Actual route traces come from measured GPS tracks, manual surveyed geometry, or a user-approved imported layer.

**L-05 | Accurate spatial semantics.** Store WGS84 latitude and longitude and horizontal uncertainty separately. Distinguish observer location, fixed station coordinate, animal position estimate, and route waypoint. Do not treat a bearing or radial distance as a target coordinate without a documented calculation and its assumptions.

**L-06 | Offline map roadmap.** Offline base layers, project boundaries, permitted field tracks, geospatial overlays, and ecological context should be introduced after secure storage and coordinate semantics are stable. Never make maps a prerequisite for existing field entry.

# 9. Philippine Species Library: sources and evidence rules

**T-01 | Source-aware catalog.** A taxon entry needs stable taxon/source identifier, accepted scientific name, author or authority where available, synonyms, English and local names if verified, major group, rank, checklist authority, release/version, citation, license, and last-review metadata.

**T-02 | Distribution evidence.** Country-level checklist inclusion means the species occurs somewhere in the Philippines under that source. It does not prove presence in Mindanao, a province, a mountain range, a habitat, or a survey station. Geographic suggestions are positive evidence, not exclusions.

**T-03 | Taxonomic uncertainty.** Allow `sp.`, genus-level labels, unknown calls, provisional identifications, and conflicting determinations. Preserve the raw field label and show a reviewed identification separately.

**T-04 | National and global status.** Display Philippine DENR threatened-species status and IUCN Red List status in separate fields with assessment dates and sources. Do not infer one from the other. Maintain distinct origin/distribution classes such as endemic, resident, migratory, introduced, and uncertain.

**T-05 | Version changes.** Updating a checklist must not mutate historical observation names without an auditable reconciliation. Record accepted-name changes, split/lump mappings, and taxon concepts when needed.

**T-06 | Import quality gate.** Verify file checksum where available, expected columns, encoding, duplicates, taxon ranks, versions, licenses, and row-level exclusions. Report how many records were imported, ignored, or sent for review. Never fill unknown island distributions from generalized ranges.

**Candidate source register:**
- **Birds:** Wild Bird Club of the Philippines, *2025 Checklist of Birds of the Philippines*, including the Excel supplied earlier in this project. WBCP states personal/scientific reference use and reserves permission requirements for commercial use. Review redistribution rights before bundling the workbook or substantial list content.
- **Amphibians:** Emerson Y. Sy, *Checklist of Amphibians of the Philippines*, v2025-1, 25 February 2025, from the project-supplied spreadsheet. Confirm source columns, version, and reuse terms.
- **Reptiles:** Emerson Y. Sy, *Checklist of Reptiles of the Philippines*, v2026-1, 28 February 2026, from the project-supplied spreadsheet. Verify native, introduced, and captive-trade distinctions.
- **Mammals:** American Society of Mammalogists, Mammal Diversity Database v2.5 (28 July 2026) as the taxonomy backbone, with Philippine-country filtering and separate reviewed locality evidence. The existing import feature does not mean that the CSV is already bundled.
- **Cross-checking:** AviList, the Reptile Database, documented museum collections, peer-reviewed local literature, GBIF occurrence records with quality filters, DENR-BMB national resources, and IUCN assessments, subject to provenance and license.

**Species-detail content model:** taxon header; identification traits; distinguishing similar taxa; photos/audio with credit and permissions; habitat and elevation evidence; Philippine distribution by supported locality; activity and ecology; conservation status; credible threats; citations; update date; and uncertainty. Empty fields should remain empty or show an explicit “Not yet reviewed.”

# 10. Habitat, environment, and ecological context

**H-01 | Hierarchical habitat records.** Store a plain-language field description and, where supported, a controlled habitat classification with scheme, level, code, version, and reviewer. “Montane forest” or “secondary growth” must not be assigned solely from elevation.

**H-02 | Field measurements.** Support time-stamped air/water temperature, relative humidity, weather, rainfall, canopy cover, slope, aspect, substrate, waterbody type, stream conditions, disturbance evidence, vegetation structure, and local microhabitat features. Every measurement stores units, collection method, and device or observer source.

**H-03 | Environmental layers.** DEM elevation, mapped land cover, watersheds, protected-area boundaries, forest cover, climate normals, and published habitat models must name their source, spatial resolution, year, projection, and known uncertainty. Derived values are labeled *mapped estimates*.

**H-04 | Cross-links.** Habitat observations link to the exact event, station, plot, or occurrence. Habitat summaries cannot substitute for direct detection evidence or conflate vegetation class with species range.

**H-05 | Philippine context.** The knowledge library should cover Philippine forest formations, karst, riparian systems, freshwater wetlands, grasslands, mangroves, coastal habitats, and other priority ecosystems through reviewed references. Geographical terms must remain scientifically traceable.

# 11. Research and conservation updates

**R-01 | Feeds distinguish source types.** Separate peer-reviewed papers, preprints, government and protected-area notices, conservation organization updates, and credible news. Attach original title, author/source, DOI or stable link, original publication date, retrieval date, category, taxa, topic, and geographic relevance.

**R-02 | Metadata first.** Candidate literature discovery includes Crossref, OpenAlex, journal/publisher feeds, universities, research repositories, government sites, and authorized society feeds. Deduplicate by DOI and trusted identifiers. Do not mirror paywalled full text or protected images.

**R-03 | Freshness labels.** The feed must clearly distinguish “published,” “updated,” and “retrieved” dates. A recently discovered 2018 article is not a 2026 discovery. Display last successful synchronization time and stale/offline state.

**R-04 | Editorial quality.** Summaries must stick to the paper or official source, separate findings from hypotheses, and state sample size/location when central. Retractions, corrections, and changed taxonomic conclusions require visible follow-up markers.

**R-05 | Offline research.** Store a small, user-managed cache of permitted article metadata, citations, abstracts where licensed, and saved reading lists. The field survey app remains functional without feed access.

**R-06 | Philippine relevance.** Provide filters for species groups, provinces/islands, habitat types, conservation topics, field methods, invasive species, and taxonomic discoveries. Relevance must not be presented as verified occurrence in the user's survey site.

# 12. Unified data model and export standards

**D-01 | Core entities.** `Project`, `Site`, `RouteTemplate`, `RouteTemplateStation`, `SurveyEvent`, `SamplingUnit/Visit`, `Observation`, `Identification`, `Measurement`, `Media`, `TaxonReference`, `HabitatRecord`, `LiteratureItem`, `ImportBatch`, and `AuditEntry`. Do not reuse a station ID as an event ID or observation ID.

**D-02 | Stable identifiers.** Repeated visits to a permanent station retain the station ID but create new visit, sampling-unit, and event IDs. Historical links remain intact.

**D-03 | Times and metadata.** Store UTC timestamps, source timezone when relevant, protocol code/version, software version, imported-source version, observer/recorder, location source/accuracy, units, and user-entered original text.

**D-04 | Effort is first-class.** Duration spent sampling, duration paused, travel time, number of sampled stations, transect distance, area searched, camera deployment duration, and recorder uptime must have different units and definitions. For point counts, session clock time must never be substituted for summed active point-count effort.

**D-05 | Observation versus event.** A sampling event with no detections must remain exportable. Missing species or counts do not produce synthetic occurrence or absence rows.

**D-06 | Audits and edits.** Corrections append provenance: old value, new value, field, timestamp, actor, and reason. Tombstone/soft-delete states must not quietly erase evidence from forensic exports.

**D-07 | Research-friendly outputs.** Maintain complete raw ZIP/CSV export with linked IDs, photographs, media index, method metadata, quality flags, and version manifest. Add interoperable Darwin Core Event-oriented export and TDWG Humboldt inventory/effort mappings once implementation is validated. Support GeoJSON for authentic geospatial geometry in a later phase.

**D-08 | Analytical data controls.** Default analysis excludes Not Counted events and soft-deleted records, and reports incomplete effort separately. Users can view all archived records for audit. Export filters must be described and reversible.

# 13. Scientific analysis boundaries

**A-01 | Descriptive summaries.** Report counts, occurrence records, richness, encounter rates, time and distance effort, and protocol-specific summaries with explicit denominators. A count of detections is not population size.

**A-02 | Diversity estimates.** Shannon, Simpson, evenness, and accumulation curves require documented sample scope and comparable sampling design. Present limitations and sample size alongside figures. Never show diversity values from mixed unsystematic observations as comparable site rankings.

**A-03 | Distance sampling.** Treat point-count/line-transect candidate rows as review inputs. Density estimation requires independent scrutiny of survey design, true distances, detection assumptions, truncation, replicate effort, and suitable sample sizes. The app must not imply density from bearings and manual distances alone.

**A-04 | Occupancy and trend analysis.** Repeated visits, detection histories, appropriate temporal closure, and documented effort are needed. A first detection or a missing entry is not sufficient evidence of occupancy trend or decline.

**A-05 | Flag questionable data, preserve originals.** Quality checks catch missing fields, conflicting relationships, implausible coordinates, incorrect durations, invalid counts, broken media references, and sampling gaps. They must not silently delete or “correct” observations.

# 14. Visual system and field usability

**U-01 | Brand.** Keep LIKAS recognizable through deep forest green, moss, muted river tones, and cream typography. The identity may use restrained retro pixel art; the survey interface must remain modern, crisp, minimal, and readable.

**U-02 | Preferred icon direction.** A small pixel-art frog with only the upper portion of its face above pond water. No lily pad, no crowded landscape, no tiny lettering. Review the actual final approved image before replacing the installed launcher.

**U-03 | Forest startup background.** Use a polished, higher-resolution Philippine tropical forest illustration, gentle leaf/cloud/water/glow motion, and gradual day/night rendering based on local device time. Avoid harsh jumps, rough loops, overly coarse visible pixels, and camera movement that obscures controls. Current repository day/night plates are low-resolution previews and require a higher-quality replacement prior to treating the work as approved.

**U-04 | Performance and accessibility.** Limit animation to ambient/startup surfaces and suspend it when hidden. Respect reduced-motion preferences and battery constraints. Provide a static fallback. All labels, buttons, timers, and warnings must be actual accessible UI controls, not embedded words in artwork.

**U-05 | Field-first ergonomics.** Large touch targets, high sunlight contrast, glove-friendly spacing, one-handed observation capture, meaningful offline feedback, and dark-mode legibility. Never place critical actions beneath system navigation or a decorative layer.

**U-06 | Clarity.** Remove duplicated station actions, redundant explanatory paragraphs, developer artifacts, dead toggles, and ambiguous labels. Use short language in the interface, with protocol help available on demand. Prevent accidental exits without trapping users in a survey.

# 15. Privacy, permissions, security, and legal context

**C-01 | Data ownership.** Survey records belong to the project user or organization under the applicable project agreement. No automatic public submission, publication, analytics upload, or sharing.

**C-02 | Sensitive coordinates.** Access-controlled locations, threatened-species nests, caves, rare taxa, field personnel routes, indigenous ecological knowledge, and restricted habitats require explicit sharing decisions. Provide coordinate generalization or suppression on public-facing export modes, retaining precise originals in protected local records.

**C-03 | Permissions.** Request location, camera, microphone, media access, and network permissions only for the functions using them. The base field form must work if optional permissions are denied, with missing values recorded as such.

**C-04 | Legal notices.** Philippine wildlife collection, handling, access to protected areas, and activities in ancestral domains may require authorization. Relevant legal frameworks include Republic Act 9147 and rules affecting protected areas and indigenous peoples. The app should record permit/reference metadata where appropriate, not promise that use of the software satisfies legal obligations.

**C-05 | Licensing.** Track taxonomic checklist rights, photo and audio credits, map-layer rights, research abstracts, and open-data license terms. Bundling third-party datasets is an explicit release-review step.

**C-06 | Backup and recovery.** Backups must be transparent, restorable, integrity-checked, and exportable without a server login. Never move to a new signing key or package identity without an approved data migration and documented recovery path.

# 16. Technical direction and release discipline

**E-01 | Preserve the present foundation.** The current repository is an Android/Java application with SQLite persistence and GitHub Actions. Refactor incrementally rather than rebuilding the product simply to add new screens.

**E-02 | Modular boundaries.** Separate survey engine and protocol rules, location services, data storage and migrations, taxon catalog, habitat layer, news retrieval, analytics/export, and presentation. A species-source update must not affect the live survey timer or database migration behavior.

**E-03 | Versioned migrations.** Each schema migration needs a rollback/recovery plan, integration test from a real previous-version database, row-count and ID checks, and photo/media integrity verification. Preserve established Android app signing continuity.

**E-04 | Testing.** All requirement IDs need automated test coverage where feasible, plus physical Android tests for GPS fix quality, background/resume, camera evidence, permissions, poor connectivity, device timezone, low battery, thermal load, and readable screen contrast.

**E-05 | Release transparency.** Every APK release includes version, commit, build date, migration notes, new features, known issues, QA results, supported data source versions, and a direct download artifact. State clearly if device tests have not been performed.

**E-06 | Never mask failures.** Permission denial, damaged backup, interrupted import, incomplete download, missing GPS, unavailable altitude, and lack of connectivity must produce understandable user-facing states. Silent fallback must not fabricate measurements.

# 17. Phased roadmap and priority

**Phase 0 | Protect and polish the current field core (P0).** Verify end-of-survey state handling; reliable resume; accurate timers and effort; GPS fixes and altitude metadata; method-specific completion; no duplicated controls; backup/restore; correct species list import states; improved icon/background without performance loss; release QA.

**Phase 1 | Philippine species reference (P0/P1).** Curate bird, herpetofauna, and mammal catalogs; verify supplied checklist inputs; license review; provenance/version displays; distribution confidence; endemicity/conservation distinctions; searchable offline species profiles; identification evidence and unresolved taxa.

**Phase 2 | Broader biodiversity and habitats (P1).** Add vegetation methods, plot data, habitat cards, ecological measurements, offline layers, geospatial quality controls, and rich Philippine ecosystem information.

**Phase 3 | Research and news intelligence (P1).** Build verified scholarly metadata discovery, source-labeled conservation updates, saved references, topic/geography filters, date transparency, and cautious short summaries.

**Phase 4 | Analysis and collaborative work (P2).** Publish reviewable ecological summaries, export mappings to Darwin Core/Humboldt, team roles, signed data packages, import/export interoperability, and optional user-approved synchronization.

**Phase 5 | Additional ecosystems and taxa (P2).** Add rigorously scoped insect, freshwater, fungal, and marine/coastal survey modules after protocol review and taxonomic-source readiness.

**Priority definitions:** P0 protects data integrity, established use, and central field workflow. P1 advances an approved pillar without endangering P0. P2 adds specialist capabilities after data models and scientific review have matured. There are no unapproved promises of dates or coverage.

# 18. Definition of done and acceptance gates

A feature is **Done** only if its requirement ID and intended state are documented; UI and storage behavior match; offline failure mode is handled; data and source provenance are retained; method validation passes; existing records survive migration; export behavior is tested; accessibility is acceptable; and a physical-device or explicitly documented equivalent test has run.

**Mandatory regression scenarios:**
1. Start and end a survey at each state: complete, early incomplete, not counted. All states remain archived and exports filter them correctly.
2. Complete a zero-detection point count, and verify that survey effort is retained without a synthetic occurrence.
3. Save a bird point-count route designed as 2,000 m / 250 m, verify nine stations, revisit with stable station IDs and fresh distinct visits.
4. Interrupt or background a point count, restore the app, and verify no duplicate record or timer corruption.
5. Record a stale or inaccurate GPS fix and unavailable elevation. Confirm the app does not present fabricated precise values.
6. Import valid and malformed checklists. Confirm version/license checks and no accidental loss of existing observations.
7. Upgrade from the previous signed APK with an export backup. Verify sites, surveys, photos, tracks, routes, audit history, and counts.
8. Operate in airplane mode, verify core survey functions, and label research feed content as cached or unavailable.
9. Review exact vs masked sensitive coordinates in private and shareable exports.
10. Check new icon and startup animation on a physical small Android screen, including reduced motion and low battery conditions.

A failed P0 regression blocks release. A feature without field validation must be labeled unvalidated in the changelog.

# 19. Governance for future chats, contributors, and developers

**Authority order:** (1) legal, scientific, safety, and data-integrity constraints; (2) the user's most recent explicit decision; (3) this approved master specification; (4) current accepted issue requirements and source-of-truth checklists; (5) existing source code and established design conventions. When sources conflict, document the conflict rather than guessing.

**Before any code change:** read this specification and the latest README, data dictionary, survey protocol, source-register notes, and QA plan. Identify affected requirement IDs, current state, planned state, database implications, export changes, and regression tests.

**When implementing:** make the smallest coherent change. Do not remove features or user data as a shortcut. Use descriptive names and simple screens. Protect observer-time accounting and stable entity identity. Add source attribution and version metadata to new biological reference data.

**After implementing:** state what actually changed, what is merely planned, tests run, APK artifact produced, and remaining limitations. Update a small changelog under the same requirement IDs. Do not state that an imported checklist or new animation is live without verifying the installed build.

**Change control:** every deviation from G-01 to G-12 needs an explicit documented user decision and corresponding revision entry. Revise this document by version number and date. Maintain a decision log for contentious questions such as checklist licenses, taxonomic authority, GPS validity thresholds, icon approvals, and migration strategies.

**Instruction for future project chats:** Treat this charter as the starting specification, not a one-time proposal. Check the current repository before claiming implementation. Keep all new features traceable to a pillar and requirement ID. Keep existing field workflows working.

# 20. Source register and provenance

**Reviewed development sources:**
- Repository: `https://github.com/andreivonmariano-dotcom/Wildlife-survey-app` (private connected repo); README and code inspected 27 October 2026 at commit `44825d70956151839bd8d90b14ef799c5ce8ea76`.
- `docs/FIELD_DATA_DICTIONARY_v0_7.md`, `docs/BIRD_DISTANCE_SAMPLING_PROTOCOL_v0_7.md`, `docs/QA_v0_15.md`, `docs/LIKAS_BRAND_SYSTEM.md`, `docs/LIKAS_FOREST_BACKGROUND.md`.
- Earlier LIKAS project discussions on survey completion states, elevation, Philippine checklists, startup artwork, and preferred frog icon. Items from discussions are requirements, not verification of a shipped feature.

**External reference sites (checked 27 October 2026):**
- WBCP Philippine checklist and use terms: `https://birdwatch.ph/philippine-checklist/`
- DENR-BMB bird dataset information: `https://www.foi.gov.ph/agencies/denrbmb/national-bird-species/`
- Philippine threatened-fauna source context and RA 9147: `https://pcsd.gov.ph/republic-act-9147/`
- National statistical references to DAO 2017-11 and DAO 2019-09: `https://psa.gov.ph/`
- Mammal Diversity Database: `https://www.mammaldiversity.org/`
- TDWG Darwin Core: `https://dwc.tdwg.org/terms/`
- TDWG Humboldt Extension: `https://eco.tdwg.org/`
- Crossref API: `https://www.crossref.org/documentation/retrieve-metadata/rest-api/`
- OpenAlex API: `https://help.openalex.org/api/`

**Citation rule:** A product source register is not a substitute for row-level citation. Biological names, statuses, images, range assertions, environment layers, and summarized studies require record-level source information in the actual application.

---

## Adoption checklist

- [ ] Approve LIKAS Master Project Specification v1.0 as the project's governing product document.
- [ ] Place the authoritative Markdown copy in the GitHub repository, preferably `docs/LIKAS_MASTER_PROJECT_SPEC.md`.
- [ ] Add the short companion `AGENTS.md` / Project-instructions text pointing to the canonical document.
- [ ] Update README and QA issue tracker to link requirement IDs and distinguish shipped, planned, and blocked items.
- [ ] Record explicit approval of the final frog icon and high-resolution forest asset before replacing Android resources.
- [ ] Conduct a license review for the three project-provided Philippine checklists before shipping full lists within the APK.

**Revision history:** v1.0, 27 October 2026: Initial unified charter, evidence and source rules, field-survey state model, platform pillars, technical governance, and phased roadmap.