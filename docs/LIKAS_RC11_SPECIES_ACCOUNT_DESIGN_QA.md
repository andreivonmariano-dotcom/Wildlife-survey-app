# LIKAS RC11 | Priority Species Account design and scientific review contract

Date: 28 October 2026 (Asia/Manila)
Working branch: `feature/likas-core-fieldwork-post-rc4`; draft PR #38
Preview: `0.27.3-rc11-compact-species-preview`; Android versionCode 60
Package: `org.phwildlife.survey.beta08`; SQLite schema 27 unchanged

## Scope and owner intent

The owner requested a complete visual and editorial refinement of the species accounts, referring to **92 priority species**. The authoritative in-app research registry has **96 distinct priority taxon keys**, arranged as 24 birds, 24 amphibians, 24 reptiles and 24 mammals. The redesign intentionally applies to all 96 so no existing source record is discarded. The exact identification of a separate 92-item subset is not documented.

## Confirmed source problem

Before RC11, TaxonDetailActivity stacked up to six independently authored research displays: an introductory guide, literature synthesis, RC7 priority card, 12-field readiness panel, RC10 new research pack and full raw-taxonomy/habitat sections. The resulting page repeated taxonomy, source notes, "not assessed" prompts, cautions and developer-style names such as "RC7" and "research status". This duplicated surface-level information and made a small-phone profile difficult to scan. The duplication lived in the UI composition, not necessarily in its underlying data.

## Unified account template

All 96 priority keys now render through `Rc11PrioritySpeciesView` from `TaxonDetailActivity`.

1. **Identity:** common name and italic scientific name, wildlife group and family, retained source-backed offline photos and existing attribution controls.
2. **At-a-glance cards:** at most two initially visible statements per populated category, with a compact **Show more** control for further evidence. Card types are Identification (magnifier), Habitat (leaf), Range (map pin), Natural history (tracks), Conservation (shield).
3. **Research trail:** a single collapsed **Sources & details** drawer holds original publication links, source editions, per-claim limitations, and the specialist-review note. Scientific warnings are not erased from data.
4. **Comparison:** existing trait and licensed photograph comparisons remain accessible. Original taxonomy keys, raw historic observations, media licenses and draft annotations are unchanged.
5. **Search:** viewing a species hides the oversized search and group-filter form. A compact **Search another species** action restores it.

Scientific source-selection order is RC10 species-specific literature (research batches 1–3) followed by category-matched RC8 evidence. Duplicates are suppressed by normalized category and statement text. Taxonomy-only and “Evidence limits” boilerplate are excluded from the primary field-character cards. Category cards with no sourced claims are omitted. No habitat, morphology, range or conservation fact is invented. The original 96-source registry and 12-field completion contract remain unchanged.

The general catalog retains the previous detail renderer for the other checklist entries pending its own redesign. This RC11 change only changes the visual reading path of the 96 priority species.

## Source coverage snapshot, not scientific certification

Current RC8 evidence, not counting taxonomy and generic limitations:
- Birds: 23/24 with at least one specific source claim, 17 identification, 10 habitat, 11 range, 1 behavior.
- Amphibians: 24/24 with a specific claim, 8 identification, 4 habitat, 19 range, 1 behavior.
- Reptiles: 24/24 with a specific claim, 13 identification, 6 habitat, 16 range, 0 behavior.
- Mammals: 23/24 with a specific claim, 11 identification, 5 habitat, 15 range, 1 behavior.

The later RC10 batches add 64 preliminary, individually cited research notes across 12 of these taxa. Source claims are not expert-approved differential keys. **0 of 96 profiles are currently specialist-certified complete.** Any category without reliable source content remains absent from the visual card rather than being populated with a fabricated placeholder. The science-account completion goal remains separate from the UI-template milestone.

## Release and acceptance gates

- Python source contract: `scripts/test_rc11_compact_species_accounts.py`, including 96 unique species, 24 per wildlife group, cited references and UI structure.
- Android emulator: `Rc11SpeciesAccountInstrumentedTest`, two new tests for a richly sourced Philippine Eagle and a sparse `Buceros hydrocorax` record. Verify absence of legacy stacked explanatory sections, visible custom icons, citation drawer and 320dp PNG screenshot.
- Existing SQLite backup/restore, old-signed-app upgrade, fixed End Survey, prior library, habitat and recorder checks must continue to pass. On the emulator script, expected count rises from 7 to 9 tests and source screenshot is exported as `emulator-qa/species-account-320dp.png`.
- A signed, versionCode 60 Android APK must be verified for application identity, signing certificate, source assets, media checksum and archive integrity before calling this an installable RC11 preview.
- Source verification alone does not establish a finished 96-species scientific reference, physical-device usability or expert biological validation.

## Next science editing program

Maintain a source-keyed, cited claim ledger and an independent review gate per taxon. Prioritize missing diagnostic, age/sex, range, habitat, behavior, conservation date and DENR data. Improve concise wording from published primary sources only. Use one template for all 96 and add supported notes in taxonomic batches. Do not overwrite source strings, historic observations, original photo rights or taxonomic synonym records. Preserve real uncertainty and conflicts inside the citation drawer.
