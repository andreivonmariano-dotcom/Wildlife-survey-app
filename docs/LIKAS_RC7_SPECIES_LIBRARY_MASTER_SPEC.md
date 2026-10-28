# LIKAS RC7: Philippine Species Library Master Specification

**Document:** LIKAS-RC7-SL-001  
**Version:** 1.0, proposed for the next RC7 library implementation  
**Prepared:** 28 October 2026 (Asia/Manila)  
**Intended use:** Private, noncommercial Philippine biodiversity research  
**Code baseline:** LIKAS v0.27.3 RC6, versionCode 51, package `org.phwildlife.survey.beta08`, SQLite 27  
**Scope:** Species Library, taxonomy, field-guide text, reference media and diagnostic comparisons  
**Excludes:** Changes to active survey methods, GPS, original observation records, startup assets, existing signing key or SQLite schema until an independently reviewed migration is needed.

## Authority and release boundary

This specification implements existing LIKAS Master Project Charter requirements **G-01, G-02, G-03, G-04, G-05, G-08, G-09, G-10, G-11, G-12, T-01 through T-06, D-01 through D-08, U and E**. Requirements that already govern surveys or source traceability remain in force. The most recent owner decision is to use the **WBCP 2025** and **Emerson Y. Sy 2025-1 / 2026-1** checklists for the app's private noncommercial research library, with attribution. Those data are already available in the RC6 research asset. The exact source records, original spellings and aliases remain intact.

**Private-use rules:** LIKAS may include the supplied checklists in private, access-controlled builds for research reference. This owner authorization does not itself grant third-party public redistribution or modification rights. WBCP expressly permits personal/scientific reference use and asks for proper citation if its material appears in another report or publication. Sy workbook redistribution terms remain unverified. A public Android listing, open repository dataset, commercial package or publicly downloadable APK is **a separate release gate**. Keep source workbooks and compiled private derivatives out of public mirrors. App users must see the title, authority, version, date and source URL. Do not remove copyright or source notes. User-generated field observations remain the researcher's own records, not material to overwrite in a catalog update.

**Status distinction:** This document, JSON schema, 96-item candidate plan, example and validator are deliverables. The full RC7 enriched text, photographs, species comparison packs, new Android UI or APK are not implemented by this specification alone. The shipped RC6 baseline contains 17 introductory accounts. No RC7 app release is claimed.

## Product objective and coverage model

A single reliable offline library should let a field worker find every source-listed Philippine taxon, read what is known, understand uncertainty, and inspect specialist-reviewed identification characters without implying verified occurrence at a particular station. Store source-attributed taxonomy separately from literature-based ecology, conservation assessments, regional evidence and field observations.

**Verified RC6 source baseline:**

| Source | Research-catalog entries | Status |
|---|---:|---|
| WBCP 2025, birds | 761 | Already bundled in private research catalog |
| Sy 2025-1, amphibians | 116 | Already bundled in private research catalog |
| Sy 2026-1, reptiles | 380 | Includes one unresolved genus-level Ophiophagus record, not a species |
| ASM MDD 2.5, Philippine mammal candidates | 246 | Country listing, not a site occurrence |
| **Combined source records** | **1,503** | Source records, not validated detailed accounts |

The private WBCP/Sy compressed bundle has SHA-256 `7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6`. Its taxon records total 1,257, with 363 island/province source-evidence links. The 246 mammal candidates are prepared separately from pinned MDD 2.5. Every import must compare counts, bundle hashes and stable source keys against this baseline. A changed source file must receive a new edition and an explicit reconciliation report, not a silent replacement.

### Account tiers

| Tier | Scope | Mandatory content | Publication state |
|---|---|---|---|
| **Baseline** | Every source-listed taxon, including unresolved ranks | Source taxon key, group/rank, accepted source name, family if supplied, known common names, full source/version, review state, locality evidence only where sourced | Usable in private offline search, with blank or `not_assessed` enrichment fields |
| **Enriched** | Curated priority taxa | Overview, source-supported visual or acoustic marks, habitat, any supported elevation bounds, ecology, survey advice, misidentification caution, conservation sources, citation per claim | Editor/source review required, never called a validated ID key |
| **Diagnostic** | High-confusion taxa | All enriched elements, direct side-by-side evidence, alternative diagnoses and life-stage/sex limits, annotated licensed media where available, named scientific review | Expert approval before presenting as verified identification key |

**Tier does not equal threat category.** Risk categories remain `not_assessed` until a dated IUCN or DENR source has been checked for that taxon. A gallery image or a regional checklist entry does not raise a tier.

## Official source register and citation forms

- **Birds:** WBCP Records and Rarities Committees. Checklist of Birds of the Philippines (2025, 24 April source workbook); taxonomy in that workbook follows IOC 15.1.
- **Amphibians:** Emerson Y. Sy. Checklist of Amphibians of the Philippines, v2025-1 (25 February 2025).
- **Reptiles:** Emerson Y. Sy. Checklist of Reptiles of the Philippines, v2026-1 (28 February 2026).
- **Mammals:** American Society of Mammalogists. Mammal Diversity Database v2.5 (28 July 2026), DOI: 10.5281/zenodo.21654811.

Use these source identifiers inside RC7 data: `WBCP2025`, `SY_AMPH_2025_1`, `SY_REPT_2026_1`, and `MDD_2_5`. Keep the original version, source row ID, date and historical name, not only the modern name displayed to the reader. Additional references used for enrichment should have distinct IDs and citations. Primary sources include DENR-BMB official species and legal instruments, IUCN Red List assessments with dates, IUCN SSC profiles, BirdLife species accounts, museum specimens, peer-reviewed articles and recognized taxonomic repositories. Every derived statement records the original source IDs, source scope and review state. No copyrighted article text or original workbook should be copied into a public release without the relevant grant.

**Suggested user-facing references:**

- WBCP Records Committee and Rarities Committee. *Checklist of Birds of the Philippines*, 2025 edition (project spreadsheet dated 24 April 2025). Wild Bird Club of the Philippines. https://birdwatch.ph/philippine-checklist/
- Sy, Emerson Y. *Checklist of Amphibians of the Philippines*, version 2025-1, 25 February 2025. Original research checklist supplied by the project owner.
- Sy, Emerson Y. *Checklist of Reptiles of the Philippines*, version 2026-1, 28 February 2026. Original research checklist supplied by the project owner.
- Mammal Diversity Database. 2026. *Mammal Diversity Database* (version 2.5). American Society of Mammalogists, Zenodo. https://doi.org/10.5281/zenodo.21654811

## Deterministic import and taxonomy crosswalk

1. Read the **existing private bundled checklist** and the pinned, checksum-checked MDD candidate CSV. Import only the source records, their keys, source versions and available original columns. Never synthesize a row from global taxonomy alone.
2. Retain `sourceTaxonKey` permanently. Create a separate `taxonConceptId` to identify the proposed scientific concept. Never use a mutable scientific-name string as the sole database key.
3. Preserve raw name, former scientific names, synonyms, family, source-listed flags, origin classes and regional source evidence. For splits, lumps, genus transfers or spelling changes, add an explicit relationship and migration review queue. An unreviewed matching candidate must not auto-rewrite a historical observation.
4. Distinguish `species`, `subspecies`, `genus_unresolved` and `other_unresolved`. Keep the Ophiophagus source entry as genus unresolved and out of confirmed species totals until independently reconciled.
5. Keep a `not_assessed` value rather than manufacturing IUCN/DENR status, elevation range, habitat, island range, abundance or detection probability. Country-level inclusion never establishes station presence.
6. Before publishing any new source edition, compare additions, removals, name changes, splits/lumps, duplicate IDs, taxonomy rank changes and citation/rights changes against the prior edition. Require reviewer approval of each ambiguous crosswalk.

## Q2. Ordered priority program

The two candidate waves contain **eight batches of 12 species = 96 names**. Every name and source key in the companion JSON was checked against the actual RC6 research APK. Candidate selection reflects endemicity, conservation interest, survey relevance, and identification difficulty. It is *not* an assigned threat score or an approved expert determination.

**RC7A:** 48 candidates, 12 each for birds, amphibians, reptiles and mammals. Prioritize already cited examples, well-documented threatened taxa, and endemic targets useful for training the richer account model.

**RC7B:** 48 candidates, 12 each in the same four groups. Focus on closely related and look-alike taxa, difficult herpetofauna, Philippine monitors, forest birds, and mammals with varied source coverage. Each group requires at least two individual diagnostic comparisons and one source uncertainty review after account writing.

### Exact eight proposed batches

#### RC7A | BIRDS

`Pithecophaga jefferyi`, `Cacatua haematuropygia`, `Gallicolumba platenae`, `Dicaeum quadricolor`, `Rhabdotorrhinus waldeni`, `Anthracoceros montani`, `Gallicolumba menagei`, `Gallicolumba keayi`, `Penelopides panini`, `Sarcophanops samarensis`, `Erythropitta kochi`, `Dicaeum haematostictum`.

#### RC7A | AMPHIBIANS

`Barbourula busuangensis`, `Platymantis insulatus`, `Platymantis spelaeus`, `Platymantis paengi`, `Platymantis hazelae`, `Platymantis rabori`, `Leptobrachium lumadorum`, `Leptobrachium mangyanorum`, `Limnonectes magnus`, `Ansonia mcgregori`, `Pelophryne lighti`, `Sanguirana everetti`.

#### RC7A | REPTILES

`Crocodylus mindorensis`, `Hydrosaurus pustulatus`, `Varanus olivaceus`, `Varanus mabitang`, `Varanus bitatawa`, `Varanus cumingi`, `Varanus nuchalis`, `Cuora philippinensis`, `Siebenrockiella leytensis`, `Pelochelys cantorii`, `Trimeresurus mcgregori`, `Ophiophagus salvatana`.

#### RC7A | MAMMALS

`Bubalus mindorensis`, `Manis culionensis`, `Sus cebifrons`, `Rusa alfredi`, `Acerodon jubatus`, `Carlito syrichta`, `Cynocephalus volans`, `Crateromys australis`, `Crateromys heaneyi`, `Sus oliveri`, `Dobsonia chapmani`, `Pteropus pumilus`.

#### RC7B | BIRDS

`Buceros hydrocorax`, `Rhabdotorrhinus leucocephalus`, `Anthracoceros marchei`, `Penelopides affinis`, `Penelopides mindorensis`, `Gallicolumba crinigera`, `Gallicolumba luzonica`, `Dicaeum retrocinctum`, `Dicaeum kampalili`, `Dicaeum anthonyi`, `Actenoides hombroni`, `Ceyx argentatus`.

#### RC7B | AMPHIBIANS

`Platymantis cornutus`, `Platymantis diesmosi`, `Platymantis isarog`, `Platymantis negrosensis`, `Platymantis naomii`, `Platymantis pygmaeus`, `Platymantis banahao`, `Pelophryne albotaeniata`, `Limnonectes beloncioi`, `Limnonectes acanthi`, `Kaloula picta`, `Fejervarya vittigera`.

#### RC7B | REPTILES

`Varanus bangonorum`, `Varanus dalubhasa`, `Varanus marmoratus`, `Varanus palawanensis`, `Varanus samarensis`, `Varanus rasmusseni`, `Naja philippinensis`, `Naja samarensis`, `Trimeresurus schultzei`, `Gekko gigante`, `Cyrtodactylus agusanensis`, `Brachymeles hilong`.

#### RC7B | MAMMALS

`Axis calamianensis`, `Sus ahoenobarbus`, `Rusa marianna`, `Phloeomys pallidus`, `Phloeomys cumingi`, `Crateromys paulus`, `Crateromys schadenbergi`, `Acerodon leucotis`, `Harpyionycteris whiteheadi`, `Desmalopex microleucoptera`, `Dugong dugon`, `Baletemys kampalili`.


**Per-record progression:** `matched_to_source` → `content_drafted` → `citations_checked` → `editor_reviewed` → `expert_reviewed` (only if achieved) → `approved_for_target_build`. Entry-specific status remains `queued_for_review` until work is done. Display the last reviewed date. The source record is never removed just because enrichment remains incomplete.

**Suggested first diagnostic comparisons for editorial review:** Philippine Eagle versus Philippine Serpent-Eagle as an educational comparison; Philippine monitor lizard groups only after morphology references are sourced; Cebu and Palawan endemic flowerpeckers with age/sex limits; Visayan bleeding-hearts by plumage and island scope; limestone Platymantis with type locality and call evidence. A genus-level identification remains a valid answer. Do not auto-identify an animal from an image or predict occupancy from habitat alone.

## Q3. Versioned JSON data contract

**Machine source:** `LIKAS_RC7_SPECIES_PACK.schema.json`, JSON Schema Draft 2020-12. Companion `LIKAS_RC7_MINIMAL_VALID_EXAMPLE.json` passes the schema and semantic validator. Source-level validation script: `validate_rc7_library.py`.

The root package requires `schemaVersion = 1.0.0`, `datasetId`, `edition`, `rightsScope`, and three arrays: `sources`, `taxa`, `media`. Set `rightsScope=private_research` for data derived from the supplied checklists. Public export requires a separate rights review and explicit allow-list. All unexpected JSON properties are rejected.

Each `taxa[]` record requires these fields:

- `taxonConceptId`: LIKAS-stable concept key. Never reused across a taxonomic split or lump.
- `sourceTaxonKey`: key matched to the exact checklist/MDD row. Never fabricated from the modern display name.
- `taxonGroup`: one of Birds, Amphibians, Reptiles, Mammals.
- `identity`: source name, taxonomic rank, optional authorship, family/order, common names, language/region-attributed local names, historical names, taxonomic state, citations.
- `distribution`: Philippine source-listing status; source-indexed island/province evidence; elevation bounds with method, datum and references; origin class; and `sitePresenceInferred=false`.
- `conservation`: independent nullable `iucnGlobal` and `denrNational` assessments with status, date, source, review state and a legal instrument for a DENR category.
- `fieldGuide`: baseline/enriched/diagnostic tier; cited traits and ecological statements; look-alike concept IDs; clear limitations; boolean expert diagnostic-key approval.
- `mediaIds`: linked images, sounds, illustrations, or specimen media. Empty is permitted.
- `sourceIds`, `review`, and `conceptRelations`: claim provenance, review state and reasoned crosswalk relationships.

`source[]` stores publisher, complete title, dates, URL, citation, rights and license metadata. `media[]` stores subject state (wild, captive, museum, unknown), author, license/permission proof, caption, transformation note, offline path, checksum and actual packaged state. Null means unknown or unassessed, not zero.

### File and API boundaries

- Retain legacy Android `diagnostic_profiles_v1.json` until a tested importer and UI adapter read the new contract. Do not replace the old asset in place without a compatibility bridge.
- Keep checklist taxonomy, enrichment references, image manifest, user observation tables, and regional evidence in separate storage layers. Do not blend read-only reference records with mutable field observations.
- A generated public package must omit private source rows when reuse permission is not documented. Publishing metadata about a source does not confer full-text redistribution rights.
- Avoid eager loading of all diagnostic text and images during the first Home paint. Index source IDs, aliases and accepted names for local search. Fetch reference media on demand. Opening a taxon should not start GPS, microphone, or camera use.
- Never pull live threats/status from a web API on field-survey startup. Offline statuses carry an as-of date and offline label.

## Image and sound pipeline

A media record may have `not_bundled`, `bundled_verified`, `missing_retry` or `license_blocked` state. Only `bundled_verified` permits a local asset path to appear as usable. Each file needs exact attribution, permission/license proof, a locked source URL, cryptographic hash and type check. A missing asset must not appear as a valid photograph. Media references distinguish wild, captive, specimen and unknown subjects. Do not invent a visual from a text description to simulate a real photo.

Use an approved, checksum-pinned **offline priority image pack** instead of nonrepeatable live download attempts during every CI build. Deduplicate original media, store JPEG/WebP derivatives after checking license compatibility, strip unnecessary geotag EXIF and keep credit visible from the species page. Build budget for the first pilot: up to **30 MB compressed media** beyond the baseline, with an individual hero reference image target below **150 KB**. Revisit size limits after measuring load times on field phones. An internet-only secondary gallery can be optional, never required for offline species names, citation text or survey recording. Public export and reuse of media are separate from the private research-image permission check.

## Search, filtering and comparisons

Search should match casefolded scientific names, WBCP/Sy common names, formerly accepted synonyms, sourced local names, and source taxon key. A no-hit match must leave free-text field identification available. Show family/taxon group, tier, origin class, status-as-of date, source confidence and a source-evidence link. Filters include group, island only where directly supported, habitat and elevation only where sourced, profile tier, global/national status as separate controls and actual offline image availability. Search results must never claim statistical occurrence likelihood.

For diagnostic comparisons, show cited trait by trait, note sex/age limits, and allow unresolved identification. An expert review state and approval marker are needed before a comparison can be called a verified key. The first launch can show the existing RC6 introductory source-checked examples labeled as such.

## Automated science and engineering acceptance

The companion validator checks the schema plus scientific semantic rules:

1. All required keys, enumerated values, dates, data types, and forbidden unexpected fields.
2. Unique taxon concepts, source keys and group-specific accepted scientific names. Explicit genus-unresolved handling.
3. Referential integrity of every citation, taxon-media ID and taxonomic relation. Ambiguous names go to human review.
4. A numeric elevation claim needs a sourced method, and minimum cannot exceed maximum. Unknown uses `null`, not 0.
5. No source-derived region record can set `confirmedStationPresence=true`. Missing geographic evidence means unassessed, not absent.
6. IUCN and DENR remain independent, with assessment dates and citations. DENR classifications need a legal instrument.
7. Expert-only diagnostic approval is blocked for unreviewed profiles.
8. Bundled photo/audio claims require a real path, permission proof and a SHA-256 match, not merely a license string.
9. A public distribution pack is blocked if any source has unclear public reuse rights or any taxon lacks public release approval.
10. Priority JSON must contain exactly eight batches, 12 distinct source-matched candidates in each, using the source bundle hash from the verified RC6 APK.

**Release gates:**

- **RC7A content gate:** source counts stay at 761/116/380/246, and 48 RC7A names pass source/identity tests. The 48 are target candidates, not a claim that 48 profiles were written.
- **RC7B content gate:** another 48 are reviewed against their source keys. Existing 17 accounts must not be overwritten without merge/review.
- **RC7C diagnostic/media gate:** cited comparisons, reproducible licensed media pack and full media verification manifest. Check tarsier offline asset reproducibility after the RC6 reduction from 10 to nine bundled JPEGs.
- **RC7D device gate:** airplane-mode indexing and search, long species-card scrolling, back navigation, memory/storage use, citation opening, 320dp/large text, mixed-language names, and 30-minute survey operation. Test in-place RC4→RC6→RC7 installs, audit/export identity, media availability, SQLite migrations (if any) and actual backup/restore on sacrificial data.
- **Public release gate:** separate written rights review for third-party source rows, articles, images and sounds; independently pass source and data QA before any open app distribution.

**Proposed numerical targets:** 100% of current 1,503 source records remain searchable, 100% of priority source keys validated, zero unidentified source references, zero silent observation edits, zero bogus verified-photo links and zero unreviewed diagnostic-key approvals. Target 96 candidate accounts across two waves, with actual authored/reviewed count reported per build. No claim of complete Philippine biodiversity outside these four groups.

## Change management and current development state

The existing branch `feature/likas-core-fieldwork-post-rc4` remains a draft implementation workstream and should not be promoted solely from this specification. Maintain original package ID, RC6 signer and database compatibility. Keep the private source data separate from public redistribution artifacts. Every subsequent content update must record: source versions and hashes, newly written versus already existing accounts, specialist review decisions, media bundled versus missing, CI result, exact APK version and signing proof, and physical-device findings. The first RC7 implementation task is a **read-only schema-to-existing-catalog adapter** with no field-table migration and an automated source-matched import report.

## References and source links

- LIKAS Master Project Charter v1.0 (27 October 2026), governing project file.
- WBCP Philippine Checklist: https://birdwatch.ph/philippine-checklist/
- WBCP 2025 checklist news: https://birdwatch.ph/2025/
- ASM MDD official version history: https://www.mammaldiversity.org/releases/
- ASM MDD v2.5 Zenodo version: https://zenodo.org/records/21654811
- Emerson Y. Sy amphibian v2025-1 and reptile v2026-1 source workbooks, supplied privately by the owner; author and version printed within the app.
- LIKAS repository prior audit: `docs/QA_v0_18_7_BUNDLED_CATALOGS.md`, `docs/PHILIPPINE_TAXONOMY_SOURCES.md`, and `docs/LIKAS_RC6_NAV_RECOVERY_SPECIES_QA.md`.

---

**Acceptance boundary:** This RC7 specification and its validator can be adopted now. Publishing full new guides, public media packs and a new Android APK requires their separate implementation and checks.