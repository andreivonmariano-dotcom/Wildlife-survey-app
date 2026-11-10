# LIKAS RC8B | Species Library completeness and scientific UX audit

**Audited:** 28 October 2026, RC8 verified APK and current private development branch, draft PR #38.
**Scope:** species taxonomy, diagnostic depth, habitat links, citation integrity, offline photos and trait comparisons.
**Source baseline:** 761 WBCP birds, 116 Sy amphibians, 380 Sy reptiles, 246 MDD Philippine candidate mammal source records = **1,503 source entries**. This is not the number of unique, valid, reviewed full species profiles.

## Coverage audit from verified RC8 APK

| Metric | Actual |
|---|---:|
| Taxonomy source entries | 1,503 |
| Source-keyed priority taxa | 96 (24/group) |
| Introductory detailed diagnostic/morphological profiles | 17 in entire app, 14 within priorities |
| Priority research cards without introductory detailed morphology | 82 |
| Literature reference entries for priority taxa | 82 |
| Published trait statements in priority matrix | 467 |
| Taxonomy-group statements | 170 |
| Broad evidence limits/documentation | 97 |
| Identification-related statements | 94, covering 49 taxa |
| Range statements | 67, covering 61 taxa |
| Habitat/ecology statements | 25, covering 25 taxa |
| Behavior/sound statements | 3, covering 3 taxa |
| Conservation-related statements | 11, covering 10 taxa |
| Offline iNaturalist images | 49 photographs of 33 distinct priority taxa |
| Existing Commons images | 9 |
| Habitat profiles | 15 practical reference types |

**Meaning:** Almost all installed checklist names are not complete species accounts. The 82 literature entries are research summaries, not specialist-verified identification keys. Several statements are generic source, review or field-documentation text. Counted statements are not equivalent to independent diagnostic characters. Some cited references are secondary taxon overviews and must be cross-checked against primary diagnoses before an identification key can be designated specialist-approved.

## Observed problems and priorities

1. **Completeness obscured in search (high):** RC8 opened the search with its 96-priority filter, hiding the wider 1,503 source entries at startup. RC8B defaults to all source records and labels each result **Checklist only**, **Research summary**, or **Introductory morphology**. A later iteration should add a richer profile-depth filter and show field character coverage.
2. **Unstructured biological claims (high):** Free-text fields labelled Identification sometimes combine distribution, nomenclature or safety. Use a controlled trait dictionary, unit handling and sex/age/life-stage fields. Audit source editions and original study claims, not just URL reachability.
3. **Weak per-species habitat coverage (high):** Only 25/96 comparison taxa have direct habitat/ecology statements. RC8B adds 21 conservative, statement-exact published habitat associations covering 15 species and six of 15 habitat types. Unlinked means not yet reviewed, never locally absent.
4. **No expert-approved differential key (high):** Trait comparison shows different reference accounts, not a verified set of distinguishing characters. Publish side-by-side differential diagnostic characters only after comparative systematic literature review and recorded expert acceptance.
5. **Photographic coverage and anatomical annotation (medium):** 49 photos cover 33 of 96 priorities; photographs from the same species or different sex/age remain uneven. RC8B pairs any two approved source photos and records original authors and rights. It highlights literature-described visible character **in text**, not false spatial overlays. Photo-specific bounding boxes and key distinguishing structures require reviewer-drawn annotations.
6. **Habitat type crosswalk (medium):** The 15 practical guides are not exactly DENR's formal classification categories. Keep a source-dated official vocabulary crosswalk, distinguishing coastal, wetland, edaphic and forest-formations.
7. **Conservation category handling (medium):** National DENR and global IUCN classifications belong to separate authority, edition, assessment date and concept fields, not inferred from free-text guide references.
8. **Local detection separation (high):** Checklist, iNaturalist media, historic island evidence, habitat literature and real LIKAS survey detections must remain separate. No derived occurrence or suitability flags from these content links.

## RC8B additive implementation

- `rc8_species_habitat_links_v1.json`: 21 literal, source-linked habitat excerpt mappings for 15 taxa and 6 habitat profiles. Every entry has source taxon key, habitat ID, original claim, citation URL, review status and siteOccurrence=false.
- `HabitatSpeciesEvidenceActivity`: offline reference explorer and source-record drill-down. Empty lists explicitly mean missing research, not absence.
- `Rc8PhotoComparisonActivity`: original approved iNaturalist JPEGs, photographer credits and rights, image A/B selection including multiple images of one species, visible-character citation text. No invented anatomical location markers.
- Species Library search defaults to the complete catalog and marks profile depth visibly.
- Existing survey data, GPS, taxonomic raw identification and SQLite schema 27 remain unchanged.
- Research build permissions: 26 owner-authorized `dreidrei` images remain all-rights-reserved under direct private-research grant; 23 separate CC images retain original licenses.

## Definition of a completed species profile

A profile is **Complete (expert-reviewed)** only after all required components are independently evidenced: source taxonomy and synonyms; basic diagnostic characters against named lookalikes; size ranges and measurement definition; sex/age/life-stage variation; vetted photograph(s) when permission exists; direct habitat/microhabitat and geographic evidence with level of certainty; behavior and vocalization where supported; dated IUCN/DENR status separately; source-per-claim links; and reviewer approval/date. Not applicable and insufficient published data must be explicitly distinguished, with no invented values.

## Release QA boundaries

RC8B source additions require Python tests, real Android compilation, checksum verification, signing continuity and device upgrade checks. At document creation, Android compile and emulator verdict remain pending; record final facts only after successful CI. Keep draft PR #38 unmerged until a physical-device check is complete.
