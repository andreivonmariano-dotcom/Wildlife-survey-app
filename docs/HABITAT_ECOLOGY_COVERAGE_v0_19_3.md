# LIKAS v0.19.3 habitat reference ledger

Last checked: 2026-10-27.
This ledger records source-checked species ecological *examples*, not a
complete model of Philippine wildlife occurrence or elevation limits.

## Installed ecological profiles (4 of 1,503 source taxa)

| Accepted source key | Habitat codes | Elevation reference (m) | Reference |
| --- | --- | --- | --- |
| WBCP2025:Pithecophaga_jefferyi | lowland, secondary | 150 to 1450 | Animal Diversity Web, https://animaldiversity.org/accounts/Pithecophaga_jefferyi/ |
| MDD:1000915 (*Carlito syrichta*, historically *Tarsius syrichta*) | secondary, lowland | unassessed | Neri-Arboleda et al. 2002, https://doi.org/10.1017/S0952836902000997 |
| SYAMP2025:001 (*Barbourula busuangensis*) | riparian | unassessed | Aznar et al. field study 2025, https://pmc.ncbi.nlm.nih.gov/articles/PMC11740736/ |
| SYAMP2025:002 (*Ansonia mcgregori*) | riparian, montane, lowland | unassessed | IUCN, Threatened Amphibians of the World, https://www.iucn-amphibians.org/wp-content/uploads/sites/4/2018/11/TAW-Threatened-Species-Profiles_Part1.pdf |

The eagle account provides a published **reported occurrence span** for
the species. It is not a hard physiological boundary. The other three
sources report habitats, but do not establish species-wide minimum and
maximum altitudes. Their elevation fields remain NULL.

All four records are **non-strict**: `strict_habitat=0`. This avoids
interpreting a preference as an absolute exclusion from every other
land-cover class. The source's geographic extent and publication date
should be checked before broader generalization.

No conservation category, taxonomy family, national checklist membership,
or citation to an unvisited webpage was translated into invented habitat
or elevation requirements.

## Coverage by taxon group

The installed catalog includes the source taxon groups:
- Birds 761 (WBCP 2025)
- Amphibians 116 (Sy 2025-1)
- Reptiles 380 (Sy 2026-1)
- Mammals 246 (MDD 2026 v2.5)

Thus, 1,499 taxa do not yet have reviewed ecology profiles. The picker
displays their missing profile elements as unassessed. Island/province
checklist evidence is a different input and must not be mislabeled as
reviewed habitat evidence.

## Future sourcing pathway

1. Retrieve a complete, legally usable *species-level* habitat and
   elevation dataset from the original authority. Published taxonomies
   (WBCP/Sy/MDD) are not species ecological trait databases.
2. Reconcile accepted scientific names and synonyms to stable LIKAS
   `taxon_key` values. Confirm independent occurrence data, not
   guesses based on family/genus.
3. Record source/version, URL, publication year, study geography,
   habitat categories, reported elevation extremes, reviewer, and
   whether any constraint is really a hard limit.
4. Preserve missing values as NULL. Do not turn a highest observed
   elevation at one study site into a species-wide bound.
5. Use private scientific study accounts carefully and confirm
   license before bundling another organization's complete tables.
6. Validate predicted indices against independent records and repeat
   survey detection effort before describing them as actual probability.

Relevant authoritative avenues:
- IUCN Red List API v4: https://api.iucnredlist.org/help
  (API terms restrict commercial data use and require compliance)
- BirdLife DataZone methodological info:
  https://datazone.birdlife.org/dataset-information
  (habitat and elevation information underpins 1-km area of habitat maps,
  but that does not grant blanket permission to redistribute it)
- ASM Mammal Diversity Database: https://www.mammaldiversity.org/
  (authoritative taxonomy, not automatic species habitat preferences)

## Picker field controls

- Sort: common name, family, genus, index descending or ascending.
- Filter: all, Likely, Unlikely, Insufficient Data.
- Site conditions: manually selected habitat, elevation and uncertainty;
  no phone-derived terrain altitude or map-based land cover is asserted.
- Score labels accompany a color-coded 0–100 bar. **Percentage means
  preliminary screening index, not probability of occurrence**.
- Missing scores do not have bars and always sort last in scored views.
- The user's filter and environmental settings are retained per active
  survey ID, not automatically copied to the next unrelated survey.
