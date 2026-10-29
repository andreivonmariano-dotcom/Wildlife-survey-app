# LIKAS RC21 | Targeted original-source research, threatened-endemic conservation and phone-test QA

**Date:** 29 October 2026 (Asia/Manila)  
**Source:** `feature/likas-core-fieldwork-post-rc4`, PR #38 (draft)  
**Android target:** `0.27.3-rc21-endemic-fieldwork-preview`, versionCode 74  
**Scope:** Non-startup reference, recorder accessibility and testing only. Package `org.phwildlife.survey.beta08`, database schema 27 and pinned signing identity remain unchanged.

## Q1 | Diagnostic research on 18 open species

The RC20 ledger listed **18** priority species without a sourced Identification entry. RC21 adds **six** source-supported entries, rather than generating unsupported characters:

- *Sus oliveri*: comparative downward-sloping long skull and warnings about uncertain field phenotypes, based on the IUCN SSC Wild Pig Specialist Group's published monograph summary. This is not a complete external key.
- *Sus ahoenobarbus*: adult beard and mane from the same specialist group, with explicit age restriction.
- *Crateromys schadenbergi*: bushy tail and dense pelage from a labeled secondary museum-linked species account.
- *Varanus samarensis*: comparative revised-taxonomy color pattern from Welton et al. (2014) referencing Koch et al. (2010), DOI source archived on Zenodo; does not identify a monitor by markings alone.
- *Varanus marmoratus*: specimen scale-count ranges summarized by the Reptile Database, not a photographic field key.
- *Rusa marianna*: adult unspotted coat and three-tined antlers from the published Handbook of the Mammals of the World species treatment.

**Twelve** identification sections remain without acceptable primary species-level diagnosis and must remain empty. These are *Platymantis spelaeus*, *P. hazelae*, *Sanguirana everetti*, *Dicaeum kampalili*, *P. isarog*, *P. negrosensis*, *P. banahao*, *Pelophryne albotaeniata*, *Limnonectes acanthi*, *Fejervarya vittigera*, *Trimeresurus schultzei* and *Gekko gigante*. Some original papers or repositories identify the taxa but do not currently provide verified unique diagnostic character text. Taxonomy, type locality, and genus-wide characters are not substituted for a species diagnosis.

## Q2 | Conservation priorities with dated evidence

Review focused on endemic taxa with elevated conservation concern and accessible original or official evidence. The new batch includes:
- *Gallicolumba menagei*: 2021 IUCN global CR category and lowland forest. Historical records do not establish current presence.
- *Gallicolumba keayi*: 2021 global CR from BirdLife DataZone.
- *Gallicolumba crinigera*: 2023 global VU from BirdLife.
- *Anthracoceros marchei*: 2021 global VU, declining trend, forest habitats and threats.
- *Crocodylus mindorensis*: freshwater ecology, nest guarding from Smithsonian; separately labeled **Philippine national** Critically Endangered classification from a 2025 DENR notice citing DAO 2019-09.
- *Sus ahoenobarbus*: June 2016 global NT assessment, published 2017, with appropriate date.
- *Crateromys schadenbergi*, *Rusa marianna* and *Sus ahoenobarbus*: habitat and natural-history facts from identified secondary or published monographs.

These categories are not presumed to be current 2026 reassessments. Omitted national status is not a substitute for global status. No point-location or faunal-island occurrence is inferred from country-level checklists. Prior 96 taxon keys remain unchanged.

**Current source ledger:** 96 species, **413** citation-backed biological facts, **281** taxon-source links. Empty section counts: Identification **12**, Habitat **46**, Distribution **24**, Natural history **73**, Conservation **70**. All source records are in `rc21_diagnosis_conservation_batch_v1.json` and integrated into the existing `rc18_unified_96_priority_accounts_v1.json` and bibliography, with source type/date/location/claim scope.

## Q3 | Actual fieldwork and accessibility audit

**Direct review of current Java:**
- The live `MainActivity` pins a two-tier footer below the scrollable observation form. Save, Review and End Survey stay distinct. `End Survey` launches Complete, Incomplete and Cancel / Not Counted pathways. The `SurveyClosePolicy` retains partial effort and requires method-specific conditions to close complete. Zero-detection is allowed.
- `FieldLayoutPolicy` has width/large-text rules and a 48dp minimum action target. The existing 320dp screenshot verification does not prove large-text clarity, and a pinned End button is not automatically free from truncation at 200% font.
- The current GPS label changed dynamically but its previous accessibility description was constant. RC21 now speaks exactly the current status and still offers GPS details. The End control's accessibility description names its three deliberate outcomes.
- No schema, route, GIS location model, measurement provenance, method calculation, survey cancellation policy, or photo/audio record is changed.

**Added instrumentation:** `Rc10RecorderInstrumentedTest.largeTextKeepsEndSurveyAndGpsAccessible` sets Android font scale to 2.0 for a fresh disposable survey, checks End Survey displayed/active with at least a 48dp target, validates current spoken GPS text and captures a PNG. It restores the font setting in a finally block. Emulator script now expects 14 instrumented tests and collects the large-font PNG. Existing 96 species screens still capture at 320dp and the script still checks old signed-app in-place SQLite retention.

**Known gaps even after automated pass:** Full survey types (line transect, point count, VES, acoustic, traps) not each exercised under 200% font. Android TalkBack focus traversal, keyboard-overlap on physical devices, actual GNSS cold start, permission denial, 4-hour battery and recovery, offline image open and backup/restore with real owner records remain hands-on tests. Large text screenshot needs manual inspection; passing instrumentation alone is not visual proof.

## Required release evidence

1. Core Python and JVM regressions pass at code74.
2. New signed APK compiled under original pinned v2 certificate, package, version and SQLite schema.
3. Android 14 emulator 14/14 passes with 96 species screenshots, 200% recorder capture and byte-identical upgrade data.
4. Download the exact APK and evidence ZIP into the chat. Link real files directly only after verification.
5. Label RC21 **phone-test preview**, not stable or field-certified. Keep PR #38 draft and startup animation branch untouched until merge review.

## RC21B follow-up: accessibility screenshot revealed footer clipping

The 14-test RC21 Android emulator `40810458277` passed, but manual visual inspection of `recorder-200percent-font.png` found the End Survey row partially below the physical image frame at a 200% system font setting. Existing `isShown()` and 48dp minimum-height tests did not catch full-viewport clipping. This is a P1 usability defect, **not cleared by the original passing automation**.

RC21B source implements a conditional large-text layout. At system fontScale >=1.60 the verbose survey-status HUD moves inside the scrollable content so the permanent Save, Review and End Survey dock retains full screen space. Top-row visual navigation abbreviates labels with explicit spoken descriptions. A new instrumentation assertion requires the global visible rectangle to include the entire End Survey button height. This intentionally reduces fixed status detail only at large text; the complete GPS and session status remains accessible at the top of the scroll.

**RC21B candidate:** versionName `0.27.3-rc21b-accessible-recorder-preview`, versionCode 75. Preserve package, signing certificate, SQLite 27, survey lifecycle and source facts. Do not claim RC21B's corrected screenshot or 14/14 pass until its own Android emulator run and APK are independently retrieved.
