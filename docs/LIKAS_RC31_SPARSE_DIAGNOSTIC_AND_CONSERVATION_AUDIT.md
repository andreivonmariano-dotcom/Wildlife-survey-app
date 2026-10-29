# LIKAS RC31 | Sparse priority-species research, dated conservation assessment, and diagnostic evidence

**Date:** 29 October 2026. **Scope:** 96 curated taxa, not all Philippine species. Scientific-core branch `feature/likas-core-fieldwork-post-rc4`. This is an evidence-completeness report, **not** a certification that any species is identified from a photograph.

## Verified research state

| Metric | RC30 | RC31 |
| --- | ---: | ---: |
| Sourced biological facts presented | 464 | **490** |
| Populated topic slots / 480 | 294 | **316** |
| Species with ≥1 source in all five topics | 13 | **17** |
| Empty Identification slots | 1 | **0** |
| Empty Habitat slots | 37 | **32** |
| Empty Distribution slots | 18 | **11** |
| Empty Natural history slots | 61 | **57** |
| Empty Conservation slots | 69 | **64** |

**Interpretation:** A topic may be populated by a single source; it is not proof that a complete diagnostic key, full breeding biology, current population status, reliable island occurrence, or cleared live photograph exists. RC31 adds **26** scoped facts across **8** previously sparse taxa. Original RC18 (456 facts) and RC30 (8 facts) remain unchanged. All science changes are read-only assets merged into a copy in app memory. Zero records are not treated as species absence.

## Q1: Narrow, verified work on eight sparse accounts

| Taxon | New fields and scientific limits |
| --- | --- |
| *Buceros hydrocorax* | Forest ecology, northern Luzon–Marinduque range, fruit/invertebrate diet, separate IUCN 2020 VU and DENR 2019 EN statuses |
| *Rhabdotorrhinus leucocephalus* | Lowland forest, Mindanao/nearby-island history, feeding, IUCN 2020 NT |
| *Dicaeum retrocinctum* | Older 2016 habitat/range/diet notes with source year retained, plus recorded IUCN 2024-2 NT change from prior VU |
| *Actenoides hombroni* | Mindanao montane forest, vocal behavior, 2024 IUCN LC despite older VU assessments |
| *Kaloula picta* | Agusan Marsh 2026 voucher locality, other-survey habitat context, morphological characters with study-locality caveats |
| *Varanus rasmusseni* | Original 2010 type-series scale counts and historical 1961 Tawi-Tawi specimen provenance, not a photograph-only key |
| *Crateromys paulus* | 1981 singleton-type diagnosis, Ilin specimen provenance, DENR national 2019 CR. No invented living occurrence |
| *Dicaeum kampalili* | Original 1953 replacement name and 2023 Cornell taxonomic split context only. This **is not a field identification key**, notwithstanding its now populated Identification topic |

**Source URLs and page or section locators** are stored in `app/src/main/assets/rc31_reviewed_sparse_species_assessments_v1.json` for every one of the 26 notes. The companion APA 7 exact-URL catalog contains 27 reference records, a mix of peer-reviewed publications, dated official assessments, original descriptions and explicit undated specialist pages. A total of 27 records does **not** mean the 306 older catalog links have fully verified bibliographies.

## Q2: Current records versus dated national and global status

**Separate regulatory categories.** DENR Administrative Order 2019-09 is a Philippine national legal list. IUCN assessments are global science categories and can be updated independently. RC31 records explicit assessment authority, edition and category in the fact JSON. No national status is derived from an IUCN category. No `n.d.` specialist webpage is given an invented publication year.

| Taxon | Global IUCN evidence | Philippine national evidence | Interpretation |
| --- | --- | --- | --- |
| *Buceros hydrocorax* | 2020 VU from IUCN Hornbill Specialist Group summary | DAO 2019-09 EN | Different authorities and editions; do not combine |
| *Rhabdotorrhinus leucocephalus* | IUCN assessed 9 Sep 2020 NT | Not independently entered in RC31 | National status remains a separate review |
| *Dicaeum retrocinctum* | Older IUCN 2016 VU; IUCN 2024-2 status table lists NT | Not independently entered in RC31 | An older source page is not the latest global assessment |
| *Actenoides hombroni* | IUCN assessed 20 Jun 2024 LC | Not independently entered in RC31 | Global LC and documented decrease are not contradictory |
| *Crateromys paulus* | No newer current global category verified in this tranche | DAO 2019-09 CR | National CR is not asserted to be global CR |

The remaining 96-species status matrix needs systematic original IUCN assessment-DOI and original DENR order review. Do not infer a listing from checklist abundance, protected area, endemicity or missing status fields. The app's conservation filter recognizes the four older IUCN source-type labels but keeps DENR items separate.

## Q3: Diagnosis and photograph quality gate

The original *Varanus rasmusseni* diagnosis includes scale counts from the **type series**, not a simple camera character. The original *Crateromys paulus* description uses a single historical type specimen. *Dicaeum kampalili* now has a recovered 1953 naming publication but still lacks vetted comparison characters for an ordinary observer. The Agusan Marsh *Kaloula picta* description includes color and toe-web characters with variable dorsal markings. These cases must be labeled **study-supported** or **taxonomy only**, not automated species identification.

Existing offline-photo rights and attribution remain unchanged. The core offline-photo manifest records five of the 96 priority taxa with photographs. The workflow's separately approved photo pack may contain more eligible media, but a file's licensed status does not by itself verify its taxonomic identification, geographic locality, age, sexual stage or suitability for a diagnostic comparison. A Wikimedia Commons *Buceros hydrocorax* museum-mounted specimen was found with CC BY 2.0 metadata, but it is **not** an in-the-wild reference photo and has not been added to the app. No new downloaded photo was bundled in this release.

Recommended audit columns per species: original diagnosis location, measurement sample, differential taxa, characters visible on image, sex/age variation, photo provenance and photographer, license/attribution, full APA record, local versus broader evidence, and a review decision. A species may have five topics populated and still lack a diagnostic profile or usable photo.

## Current outstanding sparse cohort

The following **28 taxa** have at most two populated topics after RC31. A record with more than two topics still needs review.

- *Varanus bangonorum* (Reptiles): 1/5 topics, 2 fact(s). Missing: Habitat, Distribution, Natural history, Conservation
- *Cyrtodactylus agusanensis* (Reptiles): 1/5 topics, 2 fact(s). Missing: Habitat, Distribution, Natural history, Conservation
- *Varanus nuchalis* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Cuora philippinensis* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Ophiophagus salvatana* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Gallicolumba luzonica* (Birds): 2/5 topics, 2 fact(s). Missing: Distribution, Natural history, Conservation
- *Dicaeum kampalili* (Birds): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Platymantis cornutus* (Amphibians): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Platymantis diesmosi* (Amphibians): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Pelophryne albotaeniata* (Amphibians): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Limnonectes acanthi* (Amphibians): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Varanus palawanensis* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Varanus samarensis* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Trimeresurus schultzei* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Gekko gigante* (Reptiles): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Axis calamianensis* (Mammals): 2/5 topics, 2 fact(s). Missing: Habitat, Natural history, Conservation
- *Erythropitta kochi* (Birds): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Leptobrachium mangyanorum* (Amphibians): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Sanguirana everetti* (Amphibians): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Crateromys australis* (Mammals): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Platymantis banahao* (Amphibians): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Fejervarya vittigera* (Amphibians): 2/5 topics, 3 fact(s). Missing: Distribution, Natural history, Conservation
- *Varanus rasmusseni* (Reptiles): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Naja philippinensis* (Reptiles): 2/5 topics, 3 fact(s). Missing: Habitat, Natural history, Conservation
- *Pelophryne lighti* (Amphibians): 2/5 topics, 4 fact(s). Missing: Habitat, Natural history, Conservation
- *Varanus cumingi* (Reptiles): 2/5 topics, 5 fact(s). Missing: Habitat, Natural history, Conservation
- *Dugong dugon* (Mammals): 2/5 topics, 5 fact(s). Missing: Distribution, Natural history, Conservation
- *Limnonectes beloncioi* (Amphibians): 2/5 topics, 7 fact(s). Missing: Habitat, Natural history, Conservation

## Implementation and acceptance gates

Affected governing master rules: G-01, G-02, G-03, G-04, G-05, G-08, G-10, G-11, G-12, L-03, L-04, U-07, U-09. Do not reassign observer GPS to literature locations. No SQLite schema, active survey controls, recorded observations, source asset, signing key, or startup art is changed by the RC31 science overlay.

The initial CI and Android build gates require verification from current commit, then APK package/version/signing comparison. Emulator screenshots, 200%-text tests, upgrade with owner's records, TalkBack, physical field identification and photography are independent release gates.

## Follow-on research order

1. Verify original diagnostic studies and conservation assessments for the remaining **28 sparse taxa**, concentrating first on studies with auditable specimen records.
2. Work through **64 empty conservation**, **57 natural-history**, **32 habitat**, and **11 distribution** sections without inventing traits.
3. Clear original photographer permissions and taxonomic identity of diagnostic images separately, then author paired comparisons with explicit limitations and sex/age/stage.
