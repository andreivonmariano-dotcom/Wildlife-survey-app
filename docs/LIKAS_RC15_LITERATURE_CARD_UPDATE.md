# LIKAS RC15 | Published literature and bibliography refinement

**Date:** 29 October 2026 (Philippines)  
**Working branch:** `feature/likas-core-fieldwork-post-rc4`  
**Priority registry:** 96 unchanged taxon keys  
**Scope:** Directly sourced field-guide facts and structured bibliography, no specialist approval flow

## Verified primary literature added in this batch

| Priority taxon | Evidence and publication |
|---|---|
| *Platymantis pygmaeus* | Meneses et al. (2026), *Raffles Bulletin of Zoology* 74:197–219, DOI 10.26107/RBZ-2026-0015. Romblon populations earlier labeled *P. pygmaeus* are the newly described *P. guiting*, identified through 16S, morphology and calls. Do not rewrite any original field identification. |
| *Hydrosaurus pustulatus* | Sanguila et al. (2026), *ZooKeys* 1272:203–251, DOI 10.3897/zookeys.1272.167304. Identification features and a single juvenile at Agusan River, plus riparian trees by streams near Agusan Marsh. An individual SVL of 95 mm is not an adult species range. |
| *Harpyionycteris whiteheadi* | Yangurin et al. (2025), *Journal of Threatened Taxa* 17:27551–27562, DOI 10.11609/jott.10046.17.10.27551-27562. Mt. Kampalili fruit-bat captures during 2023 in surveyed montane forest habitat. |
| *Pithecophaga jefferyi* | Sutton et al. (2026), *Bird Conservation International* 36:e33, DOI 10.1017/S0959270926100586. Habitat suitability modeled in Eastern Mindanao, 41% overlap in total mining concession area and 46% of indigenous ancestral-domain area with modeled high-suitability habitat. These are modeled areas, not confirmed nests. |
| *Penelopides panini*, *Rhabdotorrhinus waldeni* | Warguez et al. (2022), *Journal of Threatened Taxa* 14:20713–20720, DOI 10.11609/jott.7261.14.3.20713-20720. Point-count evidence of primary/secondary forest use, and species-specific association with medium versus large trees in Central Panay. The 2022 paper remains the verified habitat reference despite newer publications for other bird topics. |

The file `app/src/main/assets/rc15_publication_batch_v1.json` holds 13 paraphrased, specifically located claims for six existing priority taxa. Each claim includes authors, title, journal, year, DOI, evidence type, original source location and study-bound limitation.

The indexed corpus contains **96/96 species**, **241 per-species source links**, and **12/96 species with newly checked 2025–2026 publications**. The other 84 still require individual recency searches. The total includes original research, older reference documents and curated checklists, not 241 peer-reviewed papers.

## UI

`Rc14Bibliography.java` now renders **authors · year** on one line and **journal · DOI** below for references with verified structured metadata. The link is `Open study`. Older unstructured sources retain their original citation; the app does not invent missing author, journal or year metadata. The source list remains inside `Sources & details`, with three references initially and the rest on Show more. New field facts remain compact in icon-led cards, without repeated defensive explanations.

## Review and release gates

- `scripts/test_rc15_literature_cards.py` guards 96 keys, 13 claims and six species, 241 links and an evidence-linked citation for every claim.
- Existing source checks, original photo/copyright manifest, signing certificate, SQLite 27 and End Survey lifecycle are unchanged.
- Android compilation and emulator acceptance must pass on the final RC15 build. An Android version bump is required for side-by-side source identification, not a package ID change.
- Research source verification is a document-by-document task, not a claim that every publication through October 2026 has been identified. The final 84 taxa remain open.
