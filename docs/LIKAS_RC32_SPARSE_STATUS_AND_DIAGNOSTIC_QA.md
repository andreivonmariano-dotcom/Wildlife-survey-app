# LIKAS RC32 | Sparse species, independent conservation checks and photographed differential keys

**Date:** 29 October 2026. **Development branch:** `feature/likas-core-fieldwork-post-rc4`. **APK candidate:** `0.27.3-rc32-sparse-evidence-keys-preview`, versionCode 92. This is a scientifically bounded expansion of RC31, not a complete 96-species field guide.

## Source-derived research growth

| Measurement | RC31 | RC32 source |
| --- | ---: | ---: |
| Priority species | 96 | **96** |
| Original RC18 sourced facts | 456 | **456 unchanged** |
| Total RC18+RC30+RC31+RC32 research notes | 490 | **505** |
| Populated sections of 480 | 316 | **325** |
| Species with some content in all five topics | 17 | **18** |
| Species with only one or two populated topics | 28 | **23** |
| Empty Identification | 0 | **0** |
| Empty Habitat | 32 | **29** |
| Empty Distribution | 11 | **9** |
| Empty Natural history | 57 | **56** |
| Empty Conservation | 64 | **61** |
| Exact-link reviewed APA entries | 28 | **33** |

These figures are **minimum content coverage only**, not verified full diagnostics, accepted status for all species, verified occupancy, safe handling protocols or photo confirmation. Twenty-three extremely sparse accounts remain. Five populated topics is not scientific-completeness certification.

## Q1 | Fifteen research records for six sparse species

`app/src/main/assets/rc32_sparse_priority_ecology_diagnostics_v1.json` contains 15 individually cited facts with source type, year when known, source passage/section and limitation.

- **Varanus bangonorum:** 2011–2022 observations at Abra de Ilog, Mindoro, recorded secondary lowland forest sites, feeding on cane toads, local trapping, and a named specialist-group synthesis distinguishing global LC 2021 from Philippine DAO 2019-09 Category D. The last record is **secondary**, not marked as independently verified by IUCN or the original legal text. Primary: Zdunek and Webb (2023), *Herpetozoa*, DOI 10.3897/herpetozoa.36.e96841.
- **Cyrtodactylus agusanensis:** stream-bank boulder encounters and eastern Mindanao range under the revised taxonomy (Sanguila et al. 2016), and original comparative counts against *C. gubaot* (Welton et al. 2010). Scale counts require specimen examination.
- **Cuora philippinensis:** two documented Agusan Marsh specimen localities, disturbance-edge forest records and visible shell/head characters from Sanguila et al. (2026), DOI 10.3897/zookeys.1272.167304. Neither specimen establishes modern station occurrence.
- **Ophiophagus salvatana:** the original 2024 Luzon king cobra diagnosis, differentiating adult/juvenile body bands and internal anatomy, and geographic provenance of the type series. Source: Das et al. (2024), DOI 10.5852/ejt.2024.961.2681. No claim that appearance from a photograph is conclusive or that close contact is safe.
- **Crateromys australis:** a Philippine DENR government account confirms the national Critically Endangered categorization. The secondary page has a typographical mistake in the legal order number, so the record is flagged **needs primary-text cross-check**; no direct global IUCN category inferred.
- **Dugong dugon:** the Philippine DENR 2019-09 national Critically Endangered list. This is not an IUCN global status.

These are historical and scientific source records, not user survey sightings. The original records and photo rights remain unchanged.

## Q2 | Ninety-six-species conservation audit

The companion JSON ledger `docs/LIKAS_RC32_96_STATUS_REVIEW_LEDGER.json` contains one row for every priority species. It preserves independently rechecked international and Philippine status separately. **Three species have direct dated IUCN evidence, one has a dated IUCN status relayed by the Hornbill Specialist Group, and three have individually checked DENR entries in the current targeted reviewed ledger.** Earlier imported citations still appear in source accounts but are not silently promoted to currently verified status. The specialist synthesis for *V. bangonorum* and government summary for *C. australis* are explicitly marked secondary until compared to dated original assessments.

Absence of a status record means **not yet verified**, not LC, not Data Deficient, not an absence, and not a zero count. Synonyms and changing taxonomic concepts require checking before importing legal or Red List categories.

## Q3 | Two new sourced, side-by-side differential keys

`app/src/main/assets/rc9_differential_traits_v1.json` now stores **six** comparison pairs, including two new source-linked pairs with four scoped characters each:

- *Cyrtodactylus agusanensis* versus *C. gubaot*: counted midbody dorsals, paravertebrals, postcloacal lateral tubercles and ventral/canthal marks. Original source: Welton et al. (2010), DOI 10.1655/HERPMONOGRAPHS-D-10-00005.1. The comparison is not usable from distant photographs without these small characters.
- *Ophiophagus salvatana* versus *O. hannah*: adult bands, juvenile band counts, pterygoid dentition, and dated 2024 taxonomic geography. Original source: Das et al. (2024), DOI 10.5852/ejt.2024.961.2681. The key does not direct observers to approach or handle cobras.

The existing **36/96 priority taxa with approved packaged media** in the RC31 APK are preserved. A 2023 Bangon Monitor research photograph and other image search candidates were identified, but image download, subject verification, license and author-credit validation were **not completed** in this tranche. **No new candidate photo is claimed to be bundled.** This is a photographic coverage blocker, not permission to invent or copy an image. Existing 49 checksum-approved/private-authorized iNaturalist images remain under their original license and purpose restrictions.

## App integration and testing

Read-only `Rc30SpeciesEvidence.merge` now applies RC30 (8), RC31 (26) and RC32 (15) packs to a copy of original JSON, with all-or-nothing failure behavior. Species details and trait comparisons inherit the same record set. The two new pairs appear in the already shipped **Similar species** view through its authored source comparison registry. No SQLite schema, user observations, field recorder, End Survey choices, photos, GPS values or original scientific asset is rewritten.

**Quality gates to record separately:** Python source and evidence checks; JVM tests; signed APK assembly; APK version/package and v2 certificate; real Android 14 instrumentation and enlarged-text screenshots; physical upgrade with historic survey records. A successful compile is not field certification.

## Next source batch

Target the **23 remaining one-/two-topic priority species**, especially field-confirmed primary habitat and life-history work. For statuses, check each original IUCN version and DENR official source against the scientific name and synonyms. For photos, require exact species ID, authored license text, image-hash pin, caption, and suitability for side-by-side identification. Never fill any of these fields with estimates.
