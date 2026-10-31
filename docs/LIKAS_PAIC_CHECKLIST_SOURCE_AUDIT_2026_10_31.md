# LIKAS PAIC species checklist: source audit and geographic framework

**31 October 2026 (Asia/Manila). Status: review draft. Species membership checklist NOT finalized.**
**Scope:** Library reference data only. No Android UI, database schema, GPS, survey events, raw observations, approved artwork, exports or signing changes.
**Baseline:** RC66 draft PR #63 (`feature/likas-rc66-species-account-screen`, versionCode126); integrated core RC49 and newer preview PR chain remain unmerged.

## Owner direction and decision boundary

The owner prioritized finalizing PAIC-linked species evidence **before** building the interactive Philippines map. This document records the audit and a working, reference-backed biogeographic unit framework. It does not construe this direction as approving a final region list, complete species distribution, specific PAIC geometry or a field release.

### Adopt for working review: five major PAICs plus two island groups

The published literature does not treat every named faunal region as one formerly continuous island. For the geographic navigation *review*, use seven selectable biological regions, drawing on Pitogo (2025) and Meneses et al. (2024):

1. **Greater Luzon** (`GLUZ`): Luzon, Catanduanes, Polillo, Marinduque.
2. **Greater Mindanao** (`GMND`): Mindanao, Samar, Leyte, Bohol, Basilan, Dinagat, Siargao.
3. **Greater Palawan** (`GPAL`): Palawan, Busuanga, Coron, Culion, Balabac, Dumaran.
4. **West Visayas / Greater Negros–Panay** (`GNPY`): Negros, Panay, Cebu, Masbate, Ticao, adjacent smaller islands with separately verified links.
5. **Greater Mindoro** (`GMDR`): Mindoro; Lubang and Semirara have source-dependent grouping and are held as associated exceptions.
6. **Sulu Island Group** (`SIG`): Jolo, Tawi-Tawi archipelago and its source-resolved islands. Basilan is grouped with Mindanao for this reference scheme, not counted twice.
7. **Romblon Island Group** (`RIG`): Romblon, Tablas, Sibuyan. This is a faunal island-group unit, not an assertion that all three shared one Pleistocene land bridge.

**Peripheral/ambiguous:** Batanes; Babuyan Group; Camiguin Sur; Siquijor; Lubang; Semirara; Cuyo; Cagayan de Sulu; Maestre de Campo; Calayan. These must remain separately discoverable and unassigned by default until the project's precise scheme is adopted. For instance, Pitogo (2025) associates Babuyan with Luzon while Meneses et al. (2024) treats Babuyan as a separate center. Do not silently resolve this difference by grouping or omitting those islands. Likewise Camiguin Sur is distinct from Camiguin Norte.

**Caveat:** A matching modern island or island group can be associated with a navigation unit without demonstrating Pleistocene land connectivity or PAIC-wide occurrence. Boundary polygons will require an independently sourced, projection-documented map layer before a map launch.

## Audited installed data (RC66 debug preview, private bundled catalog)

The packaged `assets/likas_ph_checklists_v2026_private.b64` was decoded read-only. Its compressed payload SHA256 matched `7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6`, as required by `BundledCatalog.java`. The checked source records contain:

- **1,257** WBCP/Sy taxon records: **761 birds**, **116 amphibians**, **380 reptiles**. Mammals are maintained separately; no equivalent source-linked PAIC assignments were approved in this audit.
- **363** existing `taxon_occurrence_evidence` candidate source citations: **352 unique taxon references with `area_type=island` and `area_label=Mindanao`** plus 11 additional Davao-province references. Those 11 do not add 11 unique taxa. These are **source-listed**, not new independent field survey observations.
- Current region-reviewed positive list is restricted to **352 Greater Mindanao taxon entries grounded in Mindanao Island source fields**. These are not automatically present on Samar, Leyte, Bohol or every part of Mindanao.
- An exact island-name scan of `distribution_text` produces **1,344 additional unreviewed region–taxon candidate pairs** across the seven zones (and 352 source-listed Mindanao pairs). These are strings to review, not new accepted occurrences. At least 905 of the 1,257 national taxa still lack a previously imported source-listed island record.

### Working evidence review queue (not final regional species totals)

| Region | Existing source-listed taxon pairs | Additional island-text candidates |
|---|---:|---:|
| Greater Luzon | 0 | 460 |
| Greater Mindanao | 352 | 47 |
| Greater Palawan | 0 | 285 |
| West Visayas | 0 | 244 |
| Greater Mindoro | 0 | 144 |
| Sulu Island Group | 0 | 68 |
| Romblon Island Group | 0 | 96 |

The zeroes mean **unreviewed geographic coverage in the current installed locality table**, not zero species. The lexical candidate figures are neither regional richness estimates nor confirmed taxon occurrences. They can include introduced, vagrant, historical, extirpated or uncertain notes and may miss taxa without explicit island names.

## Species-by-region acceptance contract

Every accepted `taxon_key × biogeographic_unit` link must preserve:
- Stable catalog taxon key and concept/name as used at source date; synonyms mapped separately.
- Exact island/locality named by the source; `area_type` and `area_label`; optional reported coordinates with uncertainty only when genuinely provided.
- The original source excerpt and scope, named checklist/publication, authors, version/date, DOI or stable URL, and row/page/reference locator where available.
- Occurrence class: native/resident, migratory, vagrant, introduced, historical, extirpated, uncertain or unassessed **only where actually reported**; missing is never interpreted as resident.
- Evidence state: `source_listed` (explicit, reviewed source locality), `needs_review` (parsed candidate), `disputed`, `rejected` (with reason), `not_assessed`. No default `absent`.
- Reviewer, review date, taxon crosswalk status and decision explanation for accepted/rejected rows, ideally a source-specific sample/provenance link.
- Review `PAIC` mapping separately from island evidence. The island evidence remains the atomic claim. A source mention of Mindanao cannot be spread to all Greater Mindanao member islands.
- Deduplicate by taxon/source/taxon concept and locality, not common name or PAIC alone. Track introduced/vagrant categories without erasing occurrence records.

**Licensing:** WBCP/Sy originals are private scientific reference data supplied by the owner. Their presence in a test APK does not prove rights to redistribute complete derived location lists publicly. Complete workbook is a private review artifact, not a public redistributed checklist.

## Work still required before marking a finalized PAIC checklist

- Define and review island-to-region exceptions and map boundary source and date; resolve whether to show outlying groups as separate tappable zones.
- Source-check each of the 1,344 unreviewed pair candidates from original checklist row and/or locality literature, including exclusions, doubtful records, introduced and migratory taxa.
- Integrate mammals via source-level island records and reconcile taxon keys/accepted names. A country-only MDD entry is insufficient.
- Seek additional published locality sources for species lacking explicit island names; a completeness percentage requires a defined source universe, not the current count of parsed tokens.
- Review record-by-record provenance and source redistribution rights. Then generate a version-pinned approved membership snapshot and coverage/uncertainty report with auditable counts.
- Only after a defensible checklist snapshot exists should the map query these accepted links. Keep full national search separately available and offline.

## References for the geographic model

- Pitogo, K. M. (2025). *Gaps and biases in vertebrate wildlife genetics from a global biodiversity hotspot*. Environmental Conservation. https://www.cambridge.org/core/journals/environmental-conservation/article/gaps-and-biases-in-vertebrate-wildlife-genetics-from-a-global-biodiversity-hotspot/77AF0A3084B2C16799390BADA15BF2EE
- Meneses, C. G., et al. (2024). *The state of Philippine herpetology*. ZooKeys, 1190, 213–257. https://doi.org/10.3897/zookeys.1190.109586
- Sánchez-González, L. A., Hosner, P. A., Moyle, R. G. (2015). *Genetic differentiation in insular lowland rainforests: Insights from historical demographic patterns in Philippine birds*. PLOS ONE. https://doi.org/10.1371/journal.pone.0134284
- Heaney's historical PAIC concept is discussed in the papers above; do not cite a secondary summary as if the original 1985 paper had been directly reviewed.

## Local audit deliverable

A private `LIKAS_PAIC_Checklist_Source_Audit_2026-10-31.xlsx` workbook was generated with region definitions, exceptional islands, 363 existing source-listed evidence rows, 1,696 region/taxon working rows (352 listed + 1,344 unreviewed), all 1,257 national catalog records and citations. The workbook exists as a chat attachment only, not a committed or public APK resource. Do not claim its paths survive into other chats.
