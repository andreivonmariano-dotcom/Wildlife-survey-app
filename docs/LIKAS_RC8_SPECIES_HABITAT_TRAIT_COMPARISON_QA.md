# LIKAS RC8 | Philippine Species & Habitat Evidence Library, Comparative Traits

**Date:** 28 October 2026. **Branch:** feature/likas-core-fieldwork-post-rc4, draft PR #38.
**Android:** 0.27.3-rc8-species-habitat-trait-preview, versionCode 56, org.phwildlife.survey.beta08.
**Database:** SQLite schema 27 unchanged; this release's comparison matrix never writes recorded survey data.

## User priority
Focus strictly on the Species and Habitat Library, extend Q1's previous 31 taxonomy-only priority accounts and Q2's offline photographic galleries, and make trait comparisons a research-use first-class screen.

## Scientific source baseline
- 96 source-keyed priority taxon records, 24 each for birds, amphibians, reptiles, mammals, from the already authorized private WBCP Birds 2025, Sy amphibians 2025-1, Sy reptiles 2026-1 and MDD 2.5.
- 14 priority taxa have the earlier introductory multi-trait morphological account in diagnostic_profiles_v1.json. The wider original introductory comparison screen retains 17 taxa total and remains accessible for source character review.
- 82 literature entries remained from RC7A/RC7B, including 34 first-wave notes and 48 second-wave notes. This release added 18 sourced amphibian/reptile/mammal records and 13 cited bird/reptile entries with per-fact HTTPS evidence. All 48 second-wave cards now have a taxon-specific source note; diagnosis and field recognition are still **not specialist-approved**.
- **RC8 evidence matrix:** app/src/main/assets/rc8_evidence_comparisons_v1.json. Exact count 96 species, 15 habitats, 467 source-linked species statements and 75 habitat comparative statements as of compile 28 Oct 2026. Each trait has an original label, literal evidence text, category, source publisher/title, HTTPS reference and review status.
- Observational metadata is not synthesized. Unknown characters are rendered **Not assessed**, not absent or zero. Similar island, elevation, vegetation or coloration is not proof of a species identity, habitat type, or site presence.

## Fifteen Philippine habitat accounts
The former 9 habitat profiles remain, including lowland, montane, mossy, limestone/karst, ultramafic, mangrove, freshwater wetland, riparian and disturbed mosaic.
Six new reference guides are:
1. Tropical subalpine rainforest
2. Beach forest / coastal strand
3. Freshwater swamp forest
4. Peat swamp forest
5. Tropical semi-evergreen rainforest
6. Tropical moist deciduous forest

The expansion cites DENR BMB's DAO 2025-23 / Philippine Biodiversity Strategy and Action Plan 2024-2040 classification. The 15 accounts are **practical reference types**, not a claim that LIKAS exactly matches every one of DENR's 12 forest formations. Some existing accounts are broader or include nonforest ecosystems. Each records field indicators, water/substrate, vegetation, field measurements and caveats. No universal elevation ranges or automatic GPS habitat assignment.

## New side-by-side research comparison UI
- Activity: Rc8TraitComparisonActivity, registered privately in AndroidManifest.
- Species: select one of four wildlife groups and two source taxa, 24 candidates per group, with optional taxon preselection from its detail screen.
- Habitat: switch to 15 field-guide types. Filter by Terrestrial, Edaphic, Coastal, Aquatic or Human-modified references.
- Species comparison categories: Identification; Habitat & ecology; Range; Behavior & sound; Conservation; Taxonomy; Evidence limits.
- Habitat categories: Field indicators; Substrate & water; Vegetation; Measurements; Scientific cautions.
- Each category shows source-linked reference A and B side by side in vertically stacked phone-friendly sections. Each value retains publisher, source link, and scientific review status. Missing traits display **Not assessed**.
- The first offline taxon photograph renders as a thumbnail with accessible photographer metadata; the full source, author license and multiple offline photos remain in the species account. This is not a field detection.
- Launchers: Species Library Compare traits, each RC7 priority account, the taxon detail page, and Habitat & Ecology Compare habitat traits. The 17-species introductory diagnostic key is still available as a separate reference.
- Purely read-only: neither comparison mode creates, infers, reidentifies nor writes records in SQLite.

## Photo-library status and rights
26 owner-credited dreidrei photos are authorized for the PRIVATE, NONCOMMERCIAL LIKAS research edition by the user's direct grant, retaining their original all-rights-reserved status. Twenty-three other iNaturalist photographs have their own individual Creative Commons photo licenses. Nine Wikimedia Commons references are source-attributed and SHA-pinned. RC8 maintains 58 offline JPEGs, pending new build inspection, and photo galleries expand by a user tap. No additional image was marked approved solely on an API species label.

## Test and release gates
- scripts/test_rc8_comparisons.py verifies 96 unique keys, 15 references, 467 citations, literal per-fact sources, 13 remaining second-wave species, category filters and no writes.
- Existing scientific source, media/photo rights, migration and RC4 lineage tests must pass.
- Android instrumented test TraitComparisonInstrumentedTest verifies the comparison filters, 96 species partition and 15 habitat toggle on Android API 34. Other instrumentation tests cover species search and backup/restore.
- Ship only a verified signed APK from successful CI, checking exact media count, source matrix and signature. Retain PR #38 as draft until physical-device layout, accessibility, backup interruption and upgrade checks complete.

## Long-term trait comparison backlog
Create reviewed, morphology-specific comparison keys per group, with photograph-backed differentiating characters and measurements. Use controlled anatomical vocabularies and taxonomic authority per field. Add multi-species comparisons, sex/age variation, vocal descriptions and specimen-level evidence where available. Do not confuse literature-backed research notes with a diagnostic species key.
