# LIKAS
## Master Project Charter, Product Requirements, and Scientific Standards

**Version:** 1.0.5 (29 October 2026 academic field-guide and reference amendment; canonical filename retained)  
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
- At the 27 October baseline, the repository contained forest day/night plates and an animation class that was not attached to the startup activity. Those startup-specific resources were retired on 28 October 2026. Adaptive launcher branding remains.

**Decisions established during this project's conversations but not to be assumed complete in the released APK:**
- A visible **End Survey** action throughout an active session, with **Complete**, **Incomplete**, and **Cancel / Not Counted** meanings.
- Automatic elevation updates from a sufficiently fresh, valid GPS location fix, with traceable source and uncertainty. No silent replacement of previously saved records.
- Use of the provided Philippine amphibian checklist (Sy 2025-1), reptile checklist (Sy 2026-1), and WBCP 2025 bird checklist, subject to verification, exact source mapping, and redistribution permission.
- A high-quality tropical forest startup design with gentle motion and phone-time-aware day/night transitions was previously requested, then superseded by the 28 October 2026 decision to remove all old startup scenery before a new design.
- A minimalist pixel-style frog emerging halfway from pond water as the preferred icon direction, with no lily pad; this later design decision supersedes the busier installed icon concept and requires asset review before replacement.
- A thorough reduction of duplicate controls, stale labels, text artifacts, and explanatory clutter.

**Do not label as shipped without an APK and device test:** the full provided checklists, live research feed, complete species profiles, GIS habitat atlas, any future startup-scene redesign, and new minimalist frog icon.

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

**U-03 | Minimal title startup (owner revision, 28 October 2026).** Replace the animated day/night forest startup scene with a text-only LIKAS wordmark on a simple solid forest-green background. Transition automatically to Home through a short, accessibility-aware fade/scale animation. Honor system reduced-motion settings and let the user skip motion by tapping. Resume Survey stays available on Home, not as an extra startup button. Keep the approved launcher icon and historical field records unchanged. No heavy scene renderer or forced long splash delay.

**U-04 | Performance and accessibility.** Keep startup and secondary interface animations brief and nonessential. Permit subtle transitions for navigation and expanded research details, never for primary field-recording controls. Respect system animation/reduced-motion settings, large type and battery constraints. Provide a static fallback. All labels, buttons, timers, and warnings must be real accessible UI controls.

**U-05 | Field-first ergonomics.** Large touch targets, high sunlight contrast, glove-friendly spacing, one-handed observation capture, meaningful offline feedback, and dark-mode legibility. Never place critical actions beneath system navigation or a decorative layer.

**U-06 | Clarity.** Remove duplicated station actions, redundant explanatory paragraphs, developer artifacts, dead toggles, and ambiguous labels. Use short language in the interface, with protocol help available on demand. Prevent accidental exits without trapping users in a survey.

**U-07 | Academic citation and natural scientific prose (owner decision, 29 October 2026).** Write species and ecology statements as concise biological findings, not publisher-led announcements such as “Organization describes” or “published species notes.” Place APA 7 author–date citations in the text beside supported facts and collect source-linked bibliography at the end of each reference account. Avoid repeated “Source:” lines under every paragraph. Preserve each original claim, full source identity, URL or DOI, source date, research scope, and review status in the stored research data. Do not invent missing bibliographic authors, dates, article titles or geographic observations. Mark incomplete reference entries for metadata review.

**U-08 | Minimal research interface (owner decision, 29 October 2026).** Use a consistent restrained palette, short labels, clear hierarchy, readable research cards, compact Back navigation and optional smooth transitions that do not impede accessibility. Keep survey initiation, Resume, Save Observation and End Survey readily available. Group secondary data-management and advanced research tools under labeled disclosures without removing them.

**U-09 | Illustrated scientific accounts and reference completion (owner decision, 29 October 2026).** Give every species account the same readable order: identity and taxonomy, vetted offline photograph with photographer/license credit if available, identification, habitat, geographic range, natural history, dated conservation evidence, and APA 7 author–date references. Put the organism and verified findings ahead of publisher descriptions and technical notices. Group longer study caveats under clear expandable research details. Do not fill a missing photo with invented illustrations or make unsupported site-occurrence claims. Treat bibliography completion as a measured review task: retain an exact link, source type and original source text for each record, mark incomplete metadata, and replace provisional labels only after checking original publication records. A source link is not automatically an APA-complete reference. Audit each Android screen at normal and enlarged text, including direct image-license and source-access controls.

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

**Phase 0 | Protect and polish the current field core (P0).** Verify end-of-survey state handling; reliable resume; accurate timers and effort; GPS fixes and altitude metadata; method-specific completion; no duplicated controls; backup/restore; correct species list import states; review the preferred icon separately, keep old startup scenery removed pending a new design, and complete release QA.

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
10. Check the launcher icon and startup-screen readability on a small Android phone. For any future animation, test reduced motion and low battery conditions.

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
- `docs/FIELD_DATA_DICTIONARY_v0_7.md`, `docs/BIRD_DISTANCE_SAMPLING_PROTOCOL_v0_7.md`, `docs/QA_v0_15.md`, `docs/LIKAS_BRAND_SYSTEM.md` (the former startup-forest document was removed on 28 October 2026).
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
- [ ] Record explicit approval of the final frog icon and any future replacement startup-scene artwork before changing Android resources.
- [ ] Conduct a license review for the three project-provided Philippine checklists before shipping full lists within the APK.

**Revision history:** v1.0, 27 October 2026: Initial unified charter, evidence and source rules, field-survey state model, platform pillars, technical governance, and phased roadmap.

**v1.0.1, 28 October 2026:** U-03 amended by the product owner's explicit startup-scene reset. Retired the unused forest image plates, `ForestBackdropView`, and dedicated forest-background instructions. Kept launcher icon and shared brand colors. New scenery remains undecided; no APK/device validation is claimed.

**v1.0.2, 28 October 2026:** User superseded earlier forest scene directions with a minimalist wordmark-to-Home transition. T/H/L specifications reinforced: source-checked offline habitat and species accounts need dated row-level citations and licensing-compliant, visibly credited photographs; never auto-fill unsupported diagnostic or locality facts. Starter offline reference accounts are not a substitute for full licensed taxon coverage. Accessibility and source-provenance rules unchanged.


**v1.0.3, 29 October 2026:** Adopted U-07 and U-08 for APA 7 inline citations, deduplicated bibliographies, concise evidence-first biological copy, minimalist field UI and short accessible secondary transitions. Updated U-04 to allow unobtrusive non-startup UI motion. Raw research data, survey science rules and installed device data remain protected. RC28 scientific core is a development preview pending independent emulator/phone acceptance.

**v1.0.4, 29 October 2026:** Owner accepted a combined 30-screen UI review, full APA reference cleanup and illustrated offline research field-guide direction (U-09). RC29 implements a reviewed subset of scholarly DOI metadata without implying the remaining source records are APA complete; preserve verified photo licenses and all survey data and state semantics. Physical/emulator acceptance is an independent release gate.


### U-10 | Three-space portal and original retro artwork (owner accepted 29 October 2026)

The short LIKAS splash opens a top-level portal containing **Fieldwork**, **Library** and **Settings** only. Use short, distinct labels and minimalist original artwork on the primary action cards. Fieldwork includes surveys, active survey resume, stations, history, data summaries and method-specific capture tools. Library includes species, habitats, photography, published comparisons, conservation and original source citations. Settings includes backup/restore, storage, imports, app metadata and motion accessibility. Do not put destructive data management next to start-survey actions, and do not start a survey merely by entering Library or Settings.

The default visual language uses warm pale surfaces, deep forest-green and pond-teal accents, soft corners, generous text spacing and a few hand-drawn vector glyphs. Avoid dense spreadsheet-like stacks on hub screens. Maintain readable 200%-font layouts, large touch targets, system Back behavior, and reduced-motion support. Approved original forest startup imagery and the retro pixel-art pond-frog icon are maintained by an explicit owner-approved asset review, with exact source file and copyright provenance. The front-facing frog emerging half-submerged in water may be visually informed by *Staurois natator*. The mascot **must not** be shown or cited as a research photograph or verified taxonomic evidence. The change must preserve Android identity, signing certificate, source science and all user survey records and complete/incomplete/not-counted outcomes.

**Implementation boundary:** RC33 creates the navigation/hub redesign, theme and icon. It does not declare every deeper form, species account or habitat list visually complete. Record device screenshots and accessibility acceptance separately from source-level tests and APK assembly.
