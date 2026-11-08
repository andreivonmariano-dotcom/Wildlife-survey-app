# APO RC99: 100-species habitat field-guide expansion

**Date:** 8 November 2026 | **Scope:** source-only development candidate, not a released or handset-verified APK.

## Decision
The owner asked that habitat accounts move beyond interchangeable “associated with forest” descriptions. The guide is educational. Species-level observations should be stated directly when supported, while ecological extrapolations may be offered as recognizable **field-search suggestions**. A field-search suggestion does not imply measured habitat selection, occurrence at an unverified site, new distribution, observed breeding, or increased rarity. Write naturally without repeating audit disclaimers in every paragraph.

## Reviewed content
- Immutable source: eight PHLibrary Book 01 **v1.75** JSON shards, 714 unique taxa.
- Existing reversible editorial layer **v1.76**: 434 rewritten articles.
- New **v1.77 habitat-cue overlay**: exactly **100** named species, split into 25 Birds, 25 Amphibians, 25 Reptiles and 25 Mammals.
- New information emphasizes forest strata, riparian substrates, water depth, wetland edges, canopy gaps, cave roosts versus foraging areas, elevated forest types, and accessible survey observation strategies.
- Two source citations to Sanguila et al. (2016), plus a de Jong et al. (2013) citation for A. jubatus, supply documented northeast Mindanao flying-lizard substrate contexts. The existing text already includes most other directly reported contexts. Entries with inferred field guidance use conditional, advisory language and carry an internal provenance label.
- The 100 changes are **not** 100 newly literature-verified habitat-preference studies. They are 100 species-keyed educational elaborations, with some source-derived observations and some ecological field expectations.

## Data and application guardrails
- New asset: `app/src/main/assets/phlibrary_habitat_cues_v177.json` with scientific-name keys, group, field cue and provenance label.
- New read-only `PhLibraryBook01.applyHabitatCues` runs after v1.76 editorial changes, inserting a sentence in the existing one-paragraph reader before terminal DENR status when present.
- The loader validates the base, editorial and rarity editions, 714-species universe, minimum 100 unique species keys, matching taxonomic group and narrative length. Extra DOI-linked citations are stored in the existing compact source list, not turned into a new UI section.
- Does not update species names, checklist membership, taxonomic crosswalk, regional occurrence, rarity tiers, IUCN or DENR status, survey SQLite data, app package identity, original approved media or signature.
- Preserve Discovery Rarity v0.3.1: Common **117**, Uncommon **343**, Rare **214**, Exceptionally rare **37**, Legendary **3**. Habitat specificity is an editorial ecological attribute, not a new rarity score.
- Android source versionCode **159**, versionName `0.27.3-rc99-phlibrary-habitats`, SQLite **29**, package `org.phwildlife.survey.beta08`.

## Focused examples

**Draco mindanensis:** In primary and mature secondary forest, published field observations describe active lizards on trunks about 5–10 m above ground. This is reported microhabitat, not a range-wide abundance estimate (Sanguila et al., 2016).

**Limnonectes magnus:** Small forest streams and their banks are useful search environments, with documented observations from aquatic, ground and low vegetation zones (Plaza et al., 2025; Sanguila et al., 2021).

**Acerodon jubatus:** A tree roost and a forest foraging area are different habitat components. A GPS study of six individuals near Subic Bay, Luzon, reported that most tracked foraging sites were in closed forest. Those Luzon observations do not establish a Mindanao-specific habitat preference (de Jong et al., 2013; DOI: 10.1371/journal.pone.0079665).

**Pseudorabdion ater:** Shaded forest leaf litter would be a genus-informed possible search substrate, **not** a documented species-level retreat site. Do not write it as an observed habitat.

## Verification and outstanding work
- Repository asset construction checked 100/100 unique taxonomic names against 714 source records and group labels.
- Source metadata for added Sanguila 2016 and de Jong 2013 citations checked against bibliographic record (ZooKeys 624, 1–132; DOI 10.3897/zookeys.624.9814).
- Python source regression: `scripts/test_rc99_phlibrary_habitat_cues.py` (pending CI execution).
- Android source/reader instrumentation: `Rc99PhLibraryHabitatInstrumentedTest` (pending CI execution).
- Emulator sequence: inherited RC95–RC98 checks and RC99 two-font reader tests (pending CI execution).
- APK original signer, version, upgrade preservation, physical phone records, daylight/readability, TalkBack: **not yet independently verified for RC99**.

Next editorial coverage should examine and improve additional species with weak habitat language rather than mechanically attaching the same field cue to every remaining account. Priority is real source-specific microhabitat when obtainable, with clear informed predictions for the rest.

**Primary sources used for calibration:** Sanguila, M. B., Cobb, K. A., Siler, C. D., Diesmos, A. C., Alcala, A. C., & Brown, R. M. (2016). The amphibians and reptiles of Mindanao Island, southern Philippines, II: The herpetofauna of northeast Mindanao and adjacent islands. *ZooKeys*, 624, 1–132. https://doi.org/10.3897/zookeys.624.9814. Plaza et al. (2025), *Asian Herpetological Research*, 16(4), 331–359, https://doi.org/10.3724/ahr.2095-0357.2024.0025. de Jong, C., Field, H., Tagtag, A., Hughes, T., Dechmann, D., Jayme, S., et al. (2013). Foraging behaviour and landscape utilisation by the endangered golden-crowned flying fox (Acerodon jubatus), the Philippines. *PLOS ONE*, 8(11), e79665. https://doi.org/10.1371/journal.pone.0079665.


## 2026-11-08 follow-up: coherent PHLibrary single-paragraph reader

The owner asked to continue the habitat enrichment and remove defensive or debate-like prose, especially "X rather than Y," while preserving accurate distinctions. RC99 source was revised in place, using reversible editorial layers over the unchanged 714 species source dataset.

- `phlibrary_reader_refinements_v178.json`: **44** original contrast passages rewritten into direct, fluent comparisons, preserving source facts and citations.
- `phlibrary_reader_polish_v179.json`: **94** sentence-level rewrites for additional defensive or meta-commentary expressions. Includes geographically limited observations and uncertain taxonomic identifications expressed in natural prose.
- `phlibrary_habitat_cues_v177.json`: 100 species individually reassessed (25 in each wildlife group). **47** cue sentences inserted beside relevant habitat material and **8** replaced unhelpful generic sentences. **45** proposals retained only as internal research metadata because the preceding source text already conveyed equal or better ecological detail. Those 45 cues are **not shown** in the app, avoiding repetition.
- Reader integration order: canonical v1.75 → editorial v1.76 → prose v1.78 → habitat v1.77 contextual placement → final prose v1.79. Compact bibliography remains accessible at the end of the one-paragraph reader.
- No changes to taxonomy, regional distribution fields, IUCN/DENR labels, Discovery Rarity v0.3.1, species count, SQLite29, saved survey data, package ID or artwork. All original citations remain traceable.
- A complete, independently reassembled **714-account** source-content audit showed **zero unmatched replacement anchors**, **zero missing citations**, and **zero occurrences** of the targeted expressions `rather than`, `does not`, `do not`, `should not`, `cannot`, `not establish`, `not necessarily`, and `not confirmed`. Meaningful negative scientific descriptors and unresolved identifications remain where needed.
- Regression source: `scripts/test_rc99_fluid_species_reader.py` reconstructs all layers, validates all 714 entries, the five original rarity totals, bibliography integrity, context insertion counts, and original species group fields. Android instrumented tests also assert readability and unchanged rarity counts.
- Previous RC99 CI test failures came from stale RC98 version assertions, MediaStore screenshot permission after emulator data resets, and an obsolete signed-upgrade versionCode assertion. Test-only fixes preserve Android14 app-scoped screenshots and expect code159. Source/Python regression, Java/Android build and original signer checks were observed passing in workflow runs **42451391444** and **42451388676** before their emulator stages completed; **do not label the new build accepted until complete workflow outcome, signed upgrade and artifact are verified**.
- The inherited default-main `rc4-source-gate` continues to reject modern SQLite29/RC99 as lacking historical SQLite27 and RC4 launch references. This is a separate repository governance/lineage policy conflict that requires an explicit policy decision. Do not bypass it or describe it as successful.
- Scientific meaning: field habitat notes combine published settings with cautious educational inference. They do not establish local habitat selection or alter encounter rarity. The revised text avoids repeated editorial caveats while retaining taxonomic uncertainties where the record actually needs them.

**Status at documentation update:** Source changes committed on draft PR #104, CI/emulator acceptance and physical handset test not yet fully confirmed. No RC99 direct APK handoff has been verified.
