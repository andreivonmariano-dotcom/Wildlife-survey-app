# LIKAS v0.19.3: field picker, habitat reference and state QA

## Changes
- Database schema 19 -> 20 adds source URL and source note columns to
  the separate taxon_habitat_profiles table. No field_record changes.
- Four vetted species habitat profiles are seeded using INSERT OR IGNORE.
  Existing researcher-added habitat profiles must not be overwritten.
- See HABITAT_ECOLOGY_COVERAGE_v0_19_3.md for all four source keys, habitat
  codes, elevation evidence and ecology references.
- 1499 of 1503 species remain unassessed for habitat/elevation; national,
  island, or provincial occurrence references are not habitat traits.
- The picker displays a 0-100 preliminary **screening index** color bar
  alongside text labels. A percentage is NOT a calibrated site occurrence
  probability. No score means no bar.
- A published habitat preference cannot be interpreted as a strict
  species-wide exclusion. All four seeded records have strict_habitat=0.

## State persistence test
1. Start a point count in Project A, survey ID A; open Identify Species.
2. Choose Genus sorting, Likely filter, Montane habitat, 1050 m elevation,
   and ±35 m uncertainty, then return to recording without selecting.
3. Reopen Identify Species. Every setting should retain its previous value.
4. Select a species. Verify its exact source name and group were returned,
   without altering frozen GPS mark, draft count, effort timers or station ID.
5. Open the species detail then return to the picker. Settings persist.
6. Move the app to Android Home and back; settings persist for survey A.
7. Finish survey A, start a different survey B. The site conditions and
   filters default for B, not leaked from A.
8. Reopen unfinished survey A from the launch screen. The controls should
   restore A's saved preferences.
9. Change to a different survey method and region in the same active survey,
   confirm only the geographic context changes, not the retained habitat
   or elevation measurement.

## Visual, reading and accessibility
- With a positive source record, show numeric screening index and text.
- Green Likely, red Unlikely, amber Insufficient Data, neutral Not scored.
  The meaning must remain readable without relying on color.
- Screen readers should announce the percent and that it is not a measured
  occurrence probability.
- Scores of 0, 60 and 100 are drawn within bounds and do not overflow.
- Sorting descending/ascending places unknown score cards last.
- Family and genus headers appear at group transitions, not for each row.
- Cards remain responsive with 1503 taxon catalog rows and 35/card page.
- Site conditions uses manually supplied elevation; no terrain or landcover
  map and no phone-GNSS altitude-based terrain height is claimed.
- Verify no duplicated taxonomy records from source starter lists.

## Scientific source review
- Philippine Eagle: primary/secondary forest, elevation 150–1450 m per
  Animal Diversity Web source, cited in the source ledger.
- Philippine Tarsier: peer-reviewed 2002 Bohol secondary lowland forest
  preference; no species-wide elevation extremes added.
- Barbourula busuangensis: forest streams, no elevation bounds.
- Ansonia mcgregori: lowland/lower montane forest streams, no measured
  range extremes in the checked reference.
- Published occurrence limits are observations, not hard physiological
  boundaries or absence assertions. All users retain freedom to record
  species outside screening scores.

## Release gate
- Android `:app:testDebugUnitTest :app:assembleDebug` must pass.
- Confirm Google/Android package ID and debug signing certificate match
  prior v0.19.2 in-place update.
- Back up surveys before installation. Test SQLite upgrade 19 -> 20.
- Verify unchanged field_records count, original taxonomy provenance,
  previous observations and ZIP exports after update.
- Full live-device field performance is not claimed from CI results.
