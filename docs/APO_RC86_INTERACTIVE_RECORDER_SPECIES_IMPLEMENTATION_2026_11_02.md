# APO / LIKAS RC86 | Interactive recorder and species picker

**Date:** 2 November 2026 (Asia/Manila)  
**Branch:** `feature/apo-recorder-picker-rc86-interactive-v146`  
**Draft PR:** #88 into the signed RC85 source, never historical `main`  
**Target:** `0.27.3-rc86-apo-interactive-recorder`, versionCode **146**, SQLite **29**  
**Status:** Implementation source committed. **Not an accepted APK release.** Signed CI, live device, migration and export round trip must be verified before direct APK delivery.

## Accepted user direction
Implement the RC85 recorder/picker audit and make fieldwork interactive without redundant controls, scientific lecture copy, invented taxa, fabricated habitat/gps evidence or changed survey outcome semantics. Keep the approved 714 PAIC reference, WorldCover data, mountain emblem, app package `org.phwildlife.survey.beta08` and historical survey records.

## Implemented source
1. **Species identity and migration:** additive SQLite 29 columns `field_records.taxon_key` and `field_records.taxon_source_version`, indexed by key; picker returns both. Recorder retains raw identification, resolves exact manually typed active-catalog names only when unique, stores keys/versions for newly saved observations, exports both in `observations.csv`; no historic species IDs are fabricated.
2. **Dual catalog scope:** when PAIC714 is active and the former pre-install Philippine tables were archived, Species Picker offers *Mindanao PAIC (714)* and *Philippine catalog*; both are searchable with corresponding synonyms/source-evidence tables. Does not delete archived references or permit hidden changes to source taxonomy. PAIC remains the first-view default; explicit user choices persist.
3. **One ordering control:** name, family, genus, or habitat-first (PAIC) in full-width stacked controls. Removed redundant Sort and duplicate Site-status click. Site context is read-only first, with an explicit Edit conditions action. Habitat/elevation overlap is no longer labeled a confirmed Site match. Card summaries omit dash-only IUCN, DENR, habitat, elevation and distribution placeholders.
4. **Interactive recorder:** immediate Mark detection next to observation actions; Pause effort remains direct in compact nonstation recorder, existing station effort controls stay intact; bounded inline autocomplete activates after two typed characters. ID confidence is visible and defaults to *Not assessed*, with explicit prior draft index conversion to retain the old selections when resuming. Research ZIP button is labeled *All data*; no false survey-only export claim.
5. **Clean observation transitions:** per-observation microhabitat, height, device and call-index fields now participate in unfinished-draft checks and are cleared after Save/Discard; station-level environment measurements remain. One *Attach photo* action replaces duplicate PHOTO quick shortcuts; pending photo says *Photo ready to attach*. Changing amphibian/reptile group explicitly clears the previous taxon after confirmation instead of retaining a mismatched key.
6. **Analysis behavior:** neutral *Not assessed* identifications no longer feed the derived species-level presence output. Save/Review/End and Complete/Incomplete/Not Counted are retained; raw observations and effort continue to be saved independently.

## Tests added and adapted
- Python source contracts `scripts/test_apo_rc86_interactive_v146.py` and updated RC85/RC84/older contract baselines for new code146/SQLite29.
- Revised native picker test requires actual finished species cards before screenshots and checks 714-species installation and 408/154/99/53 grouping through launcher test.
- New CI workflow `.github/workflows/apo-recorder-picker-v146-emulator-qa.yml` plus RC85-derived CI workflow configured for PR into RC85. Signed APK must verify app ID, original certificate, exact PAIC714/WorldCover bytes, locked artwork and 160% / 200% Android 14 UI tests.
- **Source check completed in this chat:** counted 48 insert fields, 48 SQL placeholders, 48 bound values for the new observation record. Database field creation is additive. Other unit tests and Android compilation have **not** been claimed to pass.

## Remaining release gates and interactive recommendations
- **P0:** Confirm GitHub CI actually passes. Inspect emulator cards/screenshots and verify selection->recorder->save includes selected taxon key/version and unchanged raw text. Test fresh install plus upgrade from populated SQLite28, including historical species records and export files.
- **P1:** Physical Android device test for GNSS, mark time, real camera/gallery, large type, TalkBack, interrupted app resumes, station transitions and end states. Check how the national archived catalog reacts to version changes and source aliases, including no-match/non-PAIC species.
- **P2:** Next usability refinements after reliable field QA: tap-to-reuse recent taxa (name only), tappable favorites, one-step *Undo last Save* as an audited edit (not silent deletion), haptic/tone confirmation as opt-in, explicit saved/sync/offline feedback, and expandable species comparison profiles. Do not copy biological measurements from prior specimens by default.
- **No new APK attached yet:** CI run and archive extraction must actually succeed. Earlier signed RC85 code145 is not relabeled as this build.

## Governing requirements
G-01, G-02, G-03, G-04, G-05, G-06, G-07, G-09, G-10, G-11; S-01 to S-06 (preserved); library taxonomy, identification/provenance, user-interface and export standards. This change does not authorize deletion of scientific records or disabling genuine data validation. User-visible caution text stays compact.
