# LIKAS | Greater Mindanao PAIC checklist: source-scope audit

**31 October 2026, Asia/Manila. Research draft. Not an approved production checklist.**

## Scope and source identity

Owner requested Greater Mindanao first, after selecting a scientific PAIC checklist ahead of the interactive Species Library map. This work audits **existing source statements only** from RC66's private WBCP 2025 bird and Sy 2025-1 amphibian / 2026-1 reptile source bundle. The binary bundle is pinned by SHA256 `7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6`. All historical survey data, original scientific statements, assets, database schema 27, package, APK and interfaces are untouched. This branch builds on the PAIC research draft PR #64 and the emulator-tested RC66 draft chain.

**Important correction to prior research notes:** The previous audit called 352 legacy records `area_type=island, area_label=Mindanao` source-listed Mindanao Island taxa. That is a statement about the imported **label**, not proof of source-backed occurrence on Mindanao proper. Exact inspection of underlying source narratives finds 34 geographic-scope mismatches, described below. The original 352 rows have NOT been deleted or silently corrected.

## Geographic unit

Greater Mindanao's reference framework uses **Mindanao, Samar, Leyte, Bohol, Basilan, Dinagat and Siargao** as principal present-day member islands (Hosner et al. 2014, Fig. 2). Brown et al. 2016 also discusses **Biliran** and associated smaller islands. **Bucas Grande** is a PAIC-associated island to audit separately, not proof of species-level linkage. **Camiguin Sur** remains a separately tracked peripheral island, not an automatically included Pleistocene land bridge. Scientific geographic grouping does **not** permit inference that a species occurs throughout all member islands.

- Hosner et al. (2014): https://doi.org/10.1111/evo.12459
- Brown et al. (2016): https://doi.org/10.3897/zookeys.624.9814
- Field Museum mammal faunal regions: https://philippines.fieldmuseum.org/natural-history/narrative/4090

## Reconciled source audit (read-only)

The private supplied workbook linked in the implementing chat has a **399-taxonomic-record Greater Mindanao review pool** (birds **193**, amphibians **50**, reptiles **156**). This total represents unique **source taxon keys**, not independently validated biological species, native or resident richness, population size, or confirmed current occupancy.

Of the **352** preexisting `Mindanao Island` labels:
- **318** name `Mindanao` directly in a source narrative. This is a lexical scope finding; among these are introduced, vagrant, seasonal and historical records that should be displayed separately, not called native residents.
- **26** use `Greater Mindanao` as a PAIC-scale phrase with no independent Mindanao-proper locality.
- **Seven** have no Mindanao geographic name in the narrative and instead refer to Eastern Visayas or member islands there.
- **One** mentions offshore islands south of Mindanao in a historical/extirpated context. This is not evidence of extant presence on Mindanao proper.

Thus **34 of 352** legacy island labels require geospatial scope review. An example is *Sarcophanops samarensis*, whose source describes Eastern Visayas, whereas its imported label is Mindanao Island. The published within-PAIC genomic study distinguishes the Samar/Leyte/Bohol *S. samarensis* pattern from *S. steerii* in other parts of Greater Mindanao. Source: https://pmc.ncbi.nlm.nih.gov/articles/PMC8528567/

The **47 additional** source-text candidate taxa, absent from the 352 imported legacy keys, were triaged at the distribution-statement level:
- **32** have an apparently positive named-member-island statement. **These are checklist-derived candidate statements**, not separately corroborated records.
- **Five** mention islands in negative/exception clauses (not positive occurrences).
- **Five** are vagrant, accidental, winter-visitor or passage records.
- **Two** are marine/coastal and must be separated from terrestrial resident faunal analyses.
- **One** is introduced/nonnative, **one** is historical/former, and **one** locality has a question mark.

**The 32 are not automatically promoted to approved app occurrence data.** No new PAIC-specific range, absence, occupancy, threat classification, species count, abundance or checklist certainty was inferred.

## Priority repairs before a map

1. Distinguish an island-specific occurrence from a source statement about the whole PAIC or Eastern Visayas. Keep both provenances and exact date/source/version.
2. Preserve temporal/biological qualifiers on each island statement: resident, migrant/vagrant, introduced, marine, historic/extirpated, ambiguous, or negated. Negative mentions must never be turned into presence records.
3. Preserve original source taxon rank. Genus-only and subspecies entries cannot be silently normalized to accepted species.
4. Validate every newly proposed island record against the original checklist and independent occurrence literature before flagging `reviewed`. Preserve the original wording and auditable reviewer/source location.
5. The **mammal** PAIC checklist remains unreviewed: the Mammal Diversity Database is taxonomy, not an island locality record. MOBIOS+ 2023 v1.8 is an occurrence-data lead, but its advertised broad bounding coordinates and CC-BY-NC 4.0 license require record-level georeferencing and rights review. **No MOBIOS+ records were downloaded or joined in this audit.** https://ipt.pensoft.net/resource?r=mobios_data
6. Original source-material license terms must be respected. WBCP restricts use to personal/scientific reference without commercial authorization and requires appropriate citation. https://birdwatch.ph/philippine-checklist/ . Sy reuse rights remain to be checked before public redistribution.

## QA and delivery

Private artifacts created and linked directly in the chat:
- `LIKAS_Mindanao_PAIC_Checklist_Review_2026-10-31.xlsx` (8 sheets; source statements and adjudication fields)
- `LIKAS_Mindanao_PAIC_Private_Review_2026-10-31.csv` (399 source taxon keys)
- `LIKAS_Mindanao_PAIC_47_Additional_Candidates_2026-10-31.csv` (47 review candidates)
- `LIKAS_Mindanao_PAIC_34_Legacy_Scope_Flags_2026-10-31.csv` (34 geographic mismatches)
- `LIKAS_Mindanao_PAIC_Audit_Summary_2026-10-31.json` (aggregate numbers only)

The bundled-payload checksum and row counts were checked; all exported CSVs have unique taxon keys and the XLSX has eight valid sheets with passing ZIP CRC. Workbook key cells and formula-error scan were checked. **Private WBCP/Sy text rows are deliberately NOT committed to this public repository.** The documented category assignment remains a reproducible source-text triage and human review queue, not final acceptance.

## Release boundary

**Research source audit only.** This must not be merged as a completed PAIC checklist, counted as a resident richness inventory, used to depict confirmed per-island species distribution, or advertised as a shipped interactive map. No APK was built for this audit. Owner review, specialist source verification, mammals, GIS boundaries and licensing remain outstanding.
