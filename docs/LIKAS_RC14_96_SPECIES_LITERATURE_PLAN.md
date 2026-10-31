# LIKAS RC14 | Current-publication search and bibliography standard

**Date of verification:** 29 October 2026  
**Scientific scope:** all 96 priority species (24 birds, 24 amphibians, 24 reptiles, 24 mammals)  
**Identity:** package `org.phwildlife.survey.beta08`; SQLite v27; unchanged original checklists and observations

## Current literature audit, accurately scoped

The local source index `app/src/main/assets/rc14_96_species_literature_index_v1.json` covers **96/96 priority species** with **235 species–reference links**, deduplicated *within each taxon*. These links include taxonomy/checklists, historically sourced account details, specialist websites and papers. A bibliography link does not assert peer review. Every indexed record carries its original source string, source type, URL, and a publication year only where explicit in the published research pack.

The targeted 2025–2026 peer-reviewed search yielded **10 priority taxa** with a verified recent publication and **13 concise research statements** in `rc14_recent_peer_reviewed_2025_2026_v1.json`. Group counts: birds 1, amphibians 4, reptiles 2, mammals 3. The remaining **86 species have not been individually cleared against all papers published through 29 October 2026**. Do not call the bibliography the "latest research for all 96" or infer that a prior paper is the last paper ever published.

"Latest" is a per-taxon **research verification task**, not a claim based only on a publication year. Current papers take precedence for taxonomic corrections and distribution evidence. Older original descriptions remain relevant for diagnostic morphology and type measurements.

## Studies verified in this batch

| Taxon | Primary study | Finding kept in the app |
|---|---|---|
| *Pithecophaga jefferyi* | Bacus et al. 2025, *Ecology and Evolution*, doi:10.1002/ece3.72572 | Mitochondrial diversity and maternal lineages in 32 Greater Mindanao samples |
| *Rusa alfredi* | Javier et al. 2025, *GigaByte*, doi:10.46471/gigabyte.150 | 2.52-Gb draft assembly with 24,531 annotated genes |
| *Acerodon jubatus* | Constantino et al. 2026, *Mammalia*, doi:10.1515/mammalia-2025-0097 | GPS tracking from two tagged flying foxes, pooled modified-habitat observations |
| *Dugong dugon* | Ng et al. 2026, *Global Ecology and Conservation*, doi:10.1016/j.gecco.2026.e04287 | 344 feeding trails and higher-biomass seagrass associations **in Singapore**, not an inferred Philippines site record |
| *Pelophryne lighti* | Diesmos et al. 2026, *ZooKeys*, doi:10.3897/zookeys.1269.173854 | A documented Samar Island Natural Park record |
| *Platymantis rabori* | Same 2026 *ZooKeys* study | Samar documentation and species photographs |
| *Limnonectes magnus* | Same 2026 *ZooKeys* study | Samar documentation |
| *Leptobrachium lumadorum* | Same 2026 *ZooKeys* study | Samar documentation |
| *Hydrosaurus pustulatus* | Same 2026 *ZooKeys* study | Samar documentation |
| *Brachymeles hilong* | Same 2026 *ZooKeys* study | Exclusion of historic Samar assignments, referencing Mindanao-restricted contemporary taxonomy |

The *P. dorsalis* complex was revised in Pitogo et al. (2026), *Zootaxa* 5884(2):271–300 (doi:10.11646/zootaxa.5884.2.3). This separate taxonomic watch item concerns species absent from the current 96 priority keys. Preserve its distinction instead of silently assigning names to an unrelated priority account.

## Bibliography interaction and editorial standard

- `Rc11PrioritySpeciesView` renders new recent-study claims ahead of older publication-backed records, retains source context, and keeps concise **Identification, Habitat, Range, Natural history, Conservation** cards.
- `Rc14Bibliography` reads the 96-entry reference index. **Sources & details** shows at most three distinct papers or curated sources initially, with a Show-more control. Each source includes its original citation and an **Open study** DOI link or **Open source** website link.
- `Literature coverage` replaces the former specialist review and approval system. No sign-off or named expert approval queue is required.
- Missing evidence does not generate placeholder text. Study-specific scope remains attached to the exact statement, including sample sizes and outside-Philippines survey locations.
- Conservation global IUCN assessments and Philippine DENR legal listings must stay separate and dated. Checklist presence cannot certify local occurrence.
- Original survey, media-rights, diagnostic source assets, and taxon keys remain untouched.

## Engineering and completion gates

- `scripts/test_rc14_latest_literature_index.py` tests all 96 taxon keys, 13 claims/10 taxa, approved HTTPS URLs, exact dated source classification and bibliography action.
- Updated Android test checks DOI references on the Philippine Eagle screen, with existing upgrade/backup/Recorder/Species tests still required.
- The source change must compile into a new signed versionCode 66 or newer binary before any APK delivery. Verify the pinned Android certificate and complete app data preservation. No handset test claim without actual handset evidence.
- Research work remains open on 86 species. For each, search publisher, taxonomic revision, DOI and current species name and synonyms, read the actual paper, add concise and bounded claims, and record checked date. Do not interpret missing search results as proof that no paper exists.
