# APO
## Offline-First Product Vision and Governing Source Document

**Version:** 1.0  
**Decision date:** 10 November 2026 (Asia/Manila)  
**Status:** Product-owner direction; governs future design and development  
**Product:** APO, the Philippines-focused wildlife field and learning application. Existing LIKAS package, repository paths and development history remain intact until a separate technical renaming decision.  
**Audience:** Product owner, developers, designers, biological-content editors, scientific reviewers and future project assistants.

> **Product promise.** APO puts a practical wildlife field toolkit, a readable Philippine biodiversity atlas, an offline biogeographic map and a searchable research library in one app. It is built for real field use and for learning, not to resemble a journal article.

## 1. Vision and audience

APO is an all-in-one **educational and scientific companion** for wildlife biologists, ecologists, conservationists, students, field technicians and interested naturalists in the Philippines. It should make survey work less cumbersome and Philippine wildlife knowledge easier to explore. Scientific reliability is a foundation of the product, but the main reading experience should feel like a well-designed natural-history field guide, not a publication database or technical report.

The app has two equally important uses: **work in the field** and **learn about nature**. A researcher should be able to record data without internet access. A curious reader should be able to browse a species, its habitat, its island context and relevant literature without learning specialized software first.

**Tone and depth.** Use clear, engaging, species-specific language. Show useful facts first. Keep taxonomy, methods, citations, uncertainties and data provenance available in unobtrusive details. Do not simplify scientific claims until they become misleading. Treat enjoyable discovery as an interface quality, not an excuse to invent rarity, ecological relationships or distribution records.

## 2. Decisions that govern the entire product

**APO-01 | Offline comes first.** Every feature in this charter must work in airplane mode, including reference browsing, maps, survey capture, reading stored papers, search and examination of the user's records. Initial versions have no operational dependence on sign-in, live APIs, cloud databases, map tiles or media streaming.

**APO-02 | Reference content is native.** Species accounts, taxonomy, ecosystem guides, geographic boundaries, reference maps, approved photos, illustrations and available research documents are prepared by developers and **bundled within the installed app**. Users must not need to import a spreadsheet, database, photographs, videos or a content pack to populate the library. No first-run content download is required. Future curated additions arrive through an app update, not a user-side import step.

**APO-03 | User-created field records are different from imported reference content.** Users can create surveys, observations, georeferenced localities and, where the field module provides them, capture photos or other evidence directly in the app. These are their own records, stored locally and kept separate from the curated atlas. Exporting and backing up user-created records remain permitted and necessary. Importing external reference datasets or media into the atlas is not part of the intended user workflow.

**APO-04 | Build geographic knowledge by PAIC.** Use Pleistocene aggregate island complexes (PAICs) as the principal editorial units. Start with **Greater Mindanao**. Complete and review its defined coverage before expanding editorial production to another PAIC. Continue until the planned Philippine biogeographic coverage is complete. The map can depict the national framework from the outset, without falsely implying that every region already has full species and ecosystem coverage.

**APO-05 | The interface is educational, not publication-style.** Avoid article-like walls of citations, dense audit labels, unexplained abbreviations and obligatory technical panels on the main screen. Keep short summaries, attractive native layouts, sensible navigation and optional deeper scientific details. Do not strip the data model of citations or qualifications.

**APO-06 | Field data and privacy are protected.** Never lose existing survey records through updates, silently rewrite identifications or location measurements, or publish precise sensitive-species sites. The map of a user's survey localities remains on-device by default. The distinction between observed occurrence, regional source reports and modeled suitability must remain explicit.

**APO-07 | Online features are a later phase.** Live papers/news, remote collaboration, cloud sync, shared atlases and online data refresh are out of scope until the offline feature set and planned offline geographic coverage meet their acceptance gates. An update to bundled content is not an online feature.

**APO-08 | Existing app continuity matters.** Retain the installed Android application ID, signing identity and compatible database migrations. APO branding and revised navigation do not authorize replacing the app with a fresh installation that discards existing LIKAS field records.

## 3. Product architecture and primary navigation

APO should expose four clear destinations, with **My Data** available as a supporting workspace:

- **Fieldwork:** plan, run, end, review and export wildlife surveys.
- **Atlas:** explore Philippine species and ecosystems in their PAIC context.
- **Map:** browse biogeographic regions and the user's surveyed localities.
- **Research Library:** browse, search and read the curated offline literature collection.
- **My Data:** projects, saved sites and routes, completed and incomplete records, local evidence, backups and exports. It may be a home workspace rather than a fifth permanent tab if usability tests favor that layout.

The Home screen should prioritize **Start Survey**, **Resume Survey** if a session is active, **Explore Atlas**, **Open Map**, and **Browse Papers**. A subtle featured-species or habitat card can encourage discovery, but must never displace a live fieldwork control. Do not create duplicate pathways or decorative explanations that slow field users down.

## 4. Fieldwork: an all-in-one survey toolkit

**Goal.** Reduce repeated writing, manual calculations and field switching without changing the meaning of survey data. The tools should be fast, legible outdoors, usable with unreliable GPS and recover gracefully from interruptions.

**Core field flow.** Choose project and survey method; set location, effort and sampling design; run the session; record observations and optional in-app evidence; review; finish or end early; inspect/export later. Preserve timestamps, units, survey method, observer, effort, coordinates, fix accuracy and data provenance.

**Method-aware recording.** Support the working survey types in the current app, including bird point counts and transects, herpetofauna visual encounter surveys and appropriate mammal methods. Add new protocols only with clear unit definitions, mandatory fields and completion criteria. Opportunistic sightings must remain distinct from standardized-effort surveys.

**Useful field tools.** Provide session and sampling-unit timers, pause/resume, automatic fresh GPS coordinates and GPS-sourced elevation where valid, route and station management, quick counts and species search from the embedded checklist, habitat notes, measurement aids and a local record review. Always label GPS altitude limitations and age/accuracy of location fixes. A planned transect length is not automatically a measured GPS route.

**End states are non-negotiable.** An active or paused survey always has an accessible **End Survey** control. A method-valid survey may be marked **Complete**. Ending before its required protocol is satisfied produces **Incomplete**, preserving actual effort and observations. **Cancel / Not Counted** retains an auditable record but excludes it from default analytical summaries. A completed zero-detection survey remains valid. Missing information is never converted to zero.

**Local data quality.** Keep distinct IDs for projects, sites, survey events, routes, stations, observations and evidence. Show missing or suspect values without inventing replacements. Allow a researcher to inspect what was recorded and export raw data with codebooks, timestamps, units and method metadata. Do not automatically infer species absence or habitat preference from one visit.

## 5. Atlas: species and ecosystems together

**Goal.** A single, lively but trustworthy field-guide-style library that answers: *What is this organism? Where has it been reported? What habitat does it use? What can I look for? Why is it interesting?* The Atlas has two connected views.

### Species Atlas

Species pages should prioritize a clear common and scientific name, native reference images when approved, useful identification features, verified regional distribution, habitat and natural-history narrative. Offer simple filters by taxon group, PAIC, island, family, habitat evidence and conservation category. Support scientific-name synonyms and unresolved identifications without erasing historic names.

Keep global IUCN assessments separate from Philippine DENR legal categories, including their authorities and dates. If an **educational discovery rarity** scale is retained, explicitly separate it from abundance, occupancy, IUCN threat status and legal protection. An unphotographed species uses a neutral placeholder, not a generated depiction presented as evidence.

### Ecosystems and Habitat Atlas

Create readable native guides for Philippine forest formations, wetlands, coasts, karst, grasslands, agricultural mosaics, freshwater habitats and other locally relevant ecosystems as evidence supports them. A habitat page should explain identifying features, vegetation structure, environmental conditions, characteristic ecological processes, conservation issues and field-relevant clues, with localized examples and source links.

Link species to habitats only where the underlying source supports the relationship. A word appearing in a species account is a **text match**, not by itself proof of habitat use. A species observed at a particular surveyed point is a user record, not automatic proof of a broader distribution. Keep these evidence types distinct in the data even when the reader sees a simple summary.

### PAIC-based content program

**First release unit: Greater Mindanao PAIC.** The supplied *APO / PHLibrary Book 01: Greater Mindanao*, Edition 1.82 (November 2026), is the current working editorial foundation. It covers seven principal islands for that volume: Mindanao, Samar, Leyte, Bohol, Dinagat, Siargao and Basilan. Its 715 entries comprise birds, amphibians, reptiles and mammals. Those numbers describe the present working book, not a guarantee that taxonomy, island occurrence, licensing or every species-level claim has passed final release review.

An island set used to organize a book is not automatically the complete geologic or biogeographic limit of a PAIC. Adjacent islands, disputed assignments, historical coastlines and other geographic schemes must be documented separately. The map and content database must preserve the selected PAIC authority/version rather than imposing an unreviewed boundary as settled science.

**Completion gate for one PAIC.** A PAIC is editorially complete when its agreed taxon-group scope and island coverage are documented; reviewed species accounts and ecosystem guides are searchable offline; source-qualified occurrence and status claims are distinguishable; cited literature and approved media are embedded or explicitly unavailable; the related map coverage is checked; and a content QA review records remaining gaps. Unsupported claims can remain absent. Completion does not mean every species is exhaustively studied.

Only after that gate is accepted should the team start the next PAIC's systematic content build. Keep reusable taxonomy and content structures so additions do not require redesigning the app each time.

## 6. Explore Map: Philippines and personal survey localities

The Map should open without internet and show the **Philippines, its islands, and an attributed PAIC/biogeographic-region layer**. Users can explore regions and tap a locality to open relevant Atlas content. The app must clearly label the framework and its source, resolution and relevant uncertainty. Boundaries may be generalized for display and must not imply precise Pleistocene coastlines or modern species ranges.

A second map layer presents **My Survey Localities** from locally stored user records. Pins may represent a study site, survey event or sampling station, but the type must be clear. Tapping a pin should open its date, method, local project and associated observations where the user has permission to see them. Provide filters by project, date, method, taxon group and survey status. Do not count an incomplete, cancelled or opportunistic event as standardized effort without explicit user action.

Use bundled, efficient vector or raster basemap data and native region geometry. No live map tiles, geocoding requests or server lookup should be necessary. Where GPS is unavailable, allow a plainly marked manual locality or missing coordinate without pretending that a precise fix exists. Sensitive sites should support local hiding or generalized display, and exported coordinates require a deliberate choice.

The map must distinguish **atlas distribution evidence**, **habitat information**, and **the user's actual surveyed sites**. Plotting a user observation never modifies a curated species' documented range automatically.

## 7. Offline Research Library

**Goal.** Let a researcher discover and use Philippine wildlife and ecology literature where there is no signal. Provide a built-in, searchable repository of **published papers and credible source documents**, with priority given to references used in the Atlas and its Greater Mindanao accounts.

Every entry should preserve title, authors, year, publication venue, DOI or persistent identifier where available, taxon, PAIC/island, topic, source type and bibliography details. Let readers search titles, authors, keywords and available text, browse by topic or region, bookmark references and jump from a species or habitat page to the cited source.

**Offline full text is conditional on legal availability.** Bundle the complete PDF or text only when its license, copyright status or permission authorizes redistribution in an app. Otherwise bundle a useful citation and any permitted abstract, summary or metadata. An external DOI/link can be shown for later online use but is not a substitute for offline content and must be marked as requiring a connection. Do not claim that a citation-only item is a locally readable full paper.

Offline collections have a **content edition date**. They cannot honestly promise real-time discovery of the latest papers. Curated additions and corrections arrive with new app builds during the offline phase. Live paper feeds and news belong to the subsequent online phase.

## 8. Scientific content, licensing and provenance

Treat the supplied Greater Mindanao book and national working checklists as **starting materials**, not automatic permission to redistribute or unquestionable taxonomic authorities. Preserve original names, versions and source references. The supplied reference set includes the WBCP 2025 bird checklist, Sy 2025-1 amphibian checklist, Sy 2026-1 reptile checklist, the Mindanao PAIC checklist and mammal reference material, subject to version, taxonomy and license checks.

For each native reference record, store at least: a stable taxon or habitat ID; displayed and original source name; source citation or document identifier; source version/date; island/PAIC claim and evidence type where relevant; editorial review status; and any media attribution and permission. Do not silently infer province or PAIC presence from Philippine-level inclusion.

Separate **curated reference data** from **the user's observations**. Retain user-entered identifications even when the current taxonomy changes; offer a reviewable updated determination. Keep observational coordinates, accuracy, date and original method details intact. Protect sensitive locality data and strip embedded coordinates from reference photos when not required.

No fabricated images, species facts, locality records, conservation categories or scientific certainty. Where the evidence is missing, use clear human-facing language such as “Not documented in this edition” without presenting the absence of a source as evidence of biological absence.

## 9. Visual and interaction direction

Adopt a clean, recognizably Philippine natural-history aesthetic. Prefer restrained forest greens, warm neutral reading surfaces, legible type and real, properly attributed wildlife or habitat photography over exaggerated scientific dashboards. Use the approved APO visual identity and existing photo-gallery direction unless a later explicit design decision replaces them.

Aim for **one main purpose per screen**, quick returning navigation, short labels, good contrast, clear active controls and comfortable touch targets. Provide readable scaling, including enlarged system fonts, and honor reduced-motion settings. A featured species, interesting field fact or discreet discovery collection can make browsing enjoyable without turning scientific records into game scores.

The source material should feel like a field guide: concise introductory content, useful natural-history prose and short fact labels, with original citations accessible on demand. Species photos are evidence-backed reference media, not decoration to be synthesized. Avoid repetitive disclaimers in each card, but do not hide uncertainty when it changes the interpretation.

## 10. Offline-first development sequence and completion gates

**Phase 0 — Establish the new charter.** Keep this document as the product-direction authority. Audit existing features and branches against its constraints. Preserve functioning survey code, local data, embedded Book 01 content and native photos. Remove or disable user-facing reference-import dependencies as replacements are embedded and verified; do not delete historical data or import capabilities before migration/compatibility review.

**Phase 1 — Reliable Fieldwork.** Finish the offline method-aware capture, end states, data integrity, GPS/elevation handling, local review and exports. Test interruption recovery and data-preserving Android upgrades. Fieldwork must work without network access.

**Phase 2 — Complete the Greater Mindanao Atlas.** Integrate the reviewed species accounts, habitats, media, taxonomic search and source records natively. Establish an auditable content build and licensing process; meet the PAIC completion gate in Section 5.

**Phase 3 — Complete the national Map and locality layer.** Ship a useful, sourced offline national PAIC map, with Greater Mindanao content linked first and user-recorded survey localities displayed safely.

**Phase 4 — Complete the offline Research Library.** Bundle the curated references and permitted texts, add cross-links and offline searching, and verify the content edition and rights.

**Phase 5 — Expand one PAIC at a time.** Repeat the established editorial and QA process for remaining Philippine biogeographic units. Mark regions as “coverage in progress” until their completion gate is met. Content milestones may overlap with engineering maintenance, but the systematic editorial sequence remains PAIC by PAIC.

**Phase 6 — Online features, only after offline acceptance.** Consider live research updates, optional sync, shared maps or cloud collaboration after core offline modules and the agreed geographic coverage are accepted. Online additions must not make the offline experience degrade.

**Acceptance of the offline milestone** requires a fresh install with all bundled references usable in airplane mode; data-preserving upgrade from the previous signed app; functioning surveys and end states; no required reference imports; offline atlas/map/library search; checked metadata and source rights; and visible separation of curated data from user observations. Device tests must cover large text, low-storage behavior and field interruptions. Actual app release status must be evidenced, not inferred from this charter.

## 11. Scope boundaries and open technical decisions

- **In scope now:** local survey creation and in-app evidence capture, curated native reference content, offline maps, local bookmarks, survey-locality visualization, backup/export and a built-in literature catalogue.
- **Out of scope during offline-first development:** mandatory account creation, cloud storage/sync, remote map tiles, live news, streaming content, community sightings as automatic range updates, in-app importing of third-party reference files, and scientific claims generated without reviewed evidence.
- **Technical decisions to resolve during implementation:** precise PAIC boundary authority; the list/order of remaining PAICs; map data licenses and package footprint; permitted bundled full texts; media redistribution rights; offline search indexing; and the final placement of My Data in navigation. These are implementation/evidence gaps, not reasons to defer this product direction.

## 12. Authority, change control and existing work

**Instruction priority:** (1) later explicit direction from the product owner; (2) this APO product charter for vision, scope, offline packaging and PAIC sequencing; (3) existing LIKAS master scientific, data-safety, survey and release standards where compatible; (4) validated current code and tests for what is actually implemented; (5) project-state and decision records for continuity. An older requirement for live research feeds or user-imported reference files is superseded when it conflicts with this charter. Scientific integrity, permissions, privacy, no-data-loss, Android signing continuity and direct-download requirements remain in force.

**Change discipline:** Record substantive changes in the repository decision log and project-state file. Give a new version number when this charter changes. Never equate “requested,” “documented,” “coded,” “tested,” “signed,” and “field-verified.” A documentation change does not produce an APK.

**Existing project continuity:** Preserve Book 01 editorial assets, the existing PHLibrary reader, the work toward offline species photo galleries and the current field-survey foundation. Those are inputs and development efforts, not evidence that every part of this charter has already shipped. The RC110 full-photo APK remains unverified at the time of this decision due to recorded CI startup failures.

---

**Controlling instruction for future work:** Design every feature to help users **record, explore, understand and revisit Philippine biodiversity without a connection**. Build and finish Greater Mindanao first, extend the atlas PAIC by PAIC, and do not substitute an import workflow or an online service for reference information that APO is meant to carry natively.