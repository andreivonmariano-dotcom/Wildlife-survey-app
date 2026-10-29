# LIKAS RC19 | Priority research gap pass and every-screen Android audit

**Date:** 29 October 2026, Philippines time
**Scope:** source-bounded species-account content + instrumented 320dp image capture on every one of 96 priority taxa
**Android target:** 0.27.3-rc19-all-species-screen-audit-preview, versionCode 72, existing package `org.phwildlife.survey.beta08`, schema 27, old signing identity

## Implemented source evidence

New `app/src/main/assets/rc19_priority_literature_batch_v1.json` stores **21 source-documented claims across seven species**. Of these, **18** are biological card facts and three taxonomic-history notes are retained in the scientific references rather than forcing a taxonomy card onto the five-section layout. The unified actual UI corpus `rc18_unified_96_priority_accounts_v1.json` now contains **377 distinct source-linked biological facts** (previously 359), with **261 taxon-source citation links** in the bibliography (previously 256).

- *Siebenrockiella leytensis*: Diesmos et al. 2012, Chelonian Research Monographs, IUCN/SSC specialist monograph. Adult shell morphology, Palawan stream/river habitats, correction of erroneous historical Leyte type-locality, 1–2 eggs per clutch, explicitly historical 2000 IUCN CR assessment.
- *Pelochelys cantorii*: Das 2008, turtle monograph and Choudhury et al. 2021 dated IUCN global assessment. Described large softshell body, freshwater/coastal habitat, global CR assessment **dated July 2018** and declining trend. Philippine populations' complex taxonomy must not be silently collapsed into a universal local species determination.
- *Phloeomys pallidus* and *P. cumingi*: Field Museum comparative species research pages and ASM Mammal Diversity Database. Northern pale/white dark-masked vs southern mahogany brown cloud rats, forest browsing, regional biogeography, original nomenclature. These are **curated scientific database accounts**, not new peer-reviewed papers.
- *Platymantis negrosensis*, *P. hazelae* and *P. banahao*: AMNH Amphibian Species of the World, with original 1920/1997 references. Localities and montane habitat ranges clearly marked as taxonomic literature summaries, without making an uncited morphological diagnosis.

**All 96 species still have actual content, sources and the same five compact card sections.** New coverage ledger: 69/96 Identification (27 remain), 44/96 Habitat (52 remain), 72/96 Distribution (24 remain), 18/96 Natural history (78 remain), 19/96 Conservation (77 remain). All unsupported scientific sections are omitted. Account completeness must not be stated as 100% biological knowledge.

## Real Android screen audit

`Rc11SpeciesAccountInstrumentedTest.testAll96ActualSpeciesScreensHaveCitedCardAndNoBoilerplate` launches the real `TaxonDetailActivity` **96 separate times** on the 320dp Android 14 emulator, using each original taxon key from the assets. For each activity it checks the species name, Sources & details drawer, at least one rendered icon-led card, and the absence of legacy defensive copy. A JPEG screenshot of each screen is saved as `files/rc19-all-96-screen-audit/000.jpg` through `095.jpg` with CSV manifest `files/rc19-all-96-screen-audit.csv`.

`scripts/run_emulator_rc7_qa.sh` now requires **13 instrumentation tests** and copies the 96 screenshot files into `emulator-qa/all-96-species-screens`, plus the CSV manifest and audit status marker. This is stronger than inspecting JSON only. CI acceptance additionally checks upgrading from the original signed RC7C install, byte-identical preserved SQLite data, recorder control screenshot, source test counts, app signing continuity, and no unsigned/expired media.

**At commit creation:** New versionCode 72 signed APK, 13/13 emulator pass, full screenshot ZIP and handset acceptance **not yet confirmed**. Do not claim these tests passed until their exact GitHub runs are checked. For direct APK handoff, export real APK bytes into current `/mnt/data` and verify package, version, signature and embedded corpus before linking.

## Next research work

The remaining 27 identification gaps and topic deficits require targeted original diagnostic papers, island-specific research and separately dated IUCN/DENR conservation records. A 96-record bibliography is not equal to 96 current species diagnoses. Keep source discovery at exact original-publication or curated authority level, never fill blanks with animal-family generalities.
