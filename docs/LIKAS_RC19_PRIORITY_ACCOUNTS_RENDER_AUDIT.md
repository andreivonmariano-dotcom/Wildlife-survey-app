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

## Verified Android 14 emulator evidence, 29 October 2026

Actual Android emulator workflow `40805609892` completed **success** and returned artifact `12868850112`. Extracted `instrumentation.txt` reports `OK (13 tests)`. The run includes `EMULATOR-SMOKE-PASS`, `RECORDER-DOCK-INSTRUMENTED-PASS` and `ALL-96-RENDERED-SPECIES-AUDIT-PASS`.

- 96 distinct real activity JPEG captures are present, 480 × 960 pixels at 320dp. Audit CSV has 96 rows (24 each of birds, amphibians, reptiles and mammals) and every listed JPEG byte count matches. Rendered card-count histogram: 14 with one, 35 with two, 27 with three, 13 with four, and seven with five.
- Reviewed four contact sheets covering all screenshot thumbnails. No obvious severe first-viewport card/header overlap was found. This is a *first-frame* visual sweep, not an end-to-end scroll or expanded-detail visual check, not a 200% font or TalkBack assessment and not physical handset acceptance.
- Original signed RC7C emulator upgrade fixture SQLite was preserved byte-for-byte: `previous-db.sqlite` and `after-upgrade-db.sqlite` both SHA-256 `07fdfede0556fac7bd03bd77a4b1112b4a4c771cc0db5e26cc8c20ec4eff20e1`.
- Scientific Core QA run `40805615786` succeeded. Re-downloaded artifact `12867598019`, extracted the 9,536,155-byte RC19 APK with SHA-256 `69e51aa59ac089e6198c1e0500eb7794483f3e0a1d083e0e21b868426cb8758b`; independently validated APK ZIP contents and v2 signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Runtime manifest version report confirms versionCode 72 and versionName `0.27.3-rc19-all-species-screen-audit-preview`.
- RC19 is a development preview. Its 96 working reference screens are not 96 scientifically complete accounts. The five source-coverage gap totals remain unchanged. Next work is sourced diagnosis research, not invented content.

