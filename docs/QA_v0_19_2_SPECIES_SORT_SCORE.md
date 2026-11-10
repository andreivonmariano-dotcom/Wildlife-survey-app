# LIKAS v0.19.2: species sort/filter and percentage screening index

## Intended behavior
- Active recorder > Identify Species > **Sort & Filter**.
- Order by common-name A–Z, family, genus, preliminary screening score
  high-to-low, or score low-to-high.
- Filter separately by All, Likely, Unlikely, Insufficient Data.
- Change group and documented-region filters without losing the survey clock.
- Matching species remain selectable at every screening score.
- Missing local distribution or unreviewed species habitat is never evidence
  of absence. Null percentages sort after scored species in both directions.
- The picker loads the bounded 5000-query rows on a background worker, sorts
  once in memory, and adds only 35 views per displayed page. A catalog
  above that bound requires a narrower search for full coverage.

## Preliminary score definition
This is a 0–100 **screening index**, NOT an occurrence probability:
- A published province occurrence record contributes 60 points.
- A published island record contributes 45 points.
- A match to a separately reviewed species habitat profile adds 25.
- A match to a separately reviewed species elevation range adds 15.
- A strict habitat mismatch backed by reviewed source evidence subtracts 25.
- Elevation conclusively outside reviewed bounds subtracts 25, retaining
  at least 100 meters or twice the entered elevation uncertainty as a margin.
- Bounds clamp the index to 0–100. No score is assigned to taxa with no
  positive geographic evidence and no independently reviewed combined
  habitat/elevation evidence.
- The categorization rule separately requires two reviewed mismatches for
  **Unlikely**. Province-cited taxa with no other evidence are provisionally
  **Likely**; island-cited taxa need additional positive evidence to
  cross the Likely rule. This is not a calibrated scientific threshold.
- Missing data never subtracts points. A species being absent from a
  checklist never means it is absent in the wild.
- The screening index is not a percentage of records, an occupancy
  probability, or a validated ecological prediction.

## Manual site conditions
- The user can select habitat category and manually enter site elevation
  and optional elevation uncertainty. No automatic terrain or landcover
  inference is claimed.
- On launch, habitat may be interpreted from existing observer habitat
  notes (mossy, montane, dipterocarp/lowland, secondary, grassland,
  riparian/stream, wetland, mangrove/coastal, agriculture, urban).
- Reviewed ecological species profiles live in
  `taxon_habitat_profiles` (SQLite schema 19). The table is initially
  empty. They require credible attributed habitat/elevation references
  before assessment can use them.
- Example: province-listed species gives 60 screening points, not a
  60% measured probability. Without a local record or reviewed habitat
  data, display **Insufficient Data**, no percentage.

## Regression
1. Alphabetical sorting: A–Z common names, deterministic scientific tie.
2. Family sorting: family then common/scientific name.
3. Genus sorting: first scientific-name token, including genus-only taxa.
4. High-to-low / low-to-high score: sorted scored names, blanks last.
5. Likely/Unlikely/Insufficient filters: correct status selection even
   if the latter two groups currently contain no reviewed ecological data.
6. Search common name, Latin binomial and synonym, then sort within results.
7. Switch Birds/Amphibians/Reptiles, scoped local evidence and All Species.
8. Open a selected species; return to recorder preserving the station,
   detection timestamp, notes and ongoing GNSS.
9. Large checklist typing and scrolling at low RAM. Refresh should not
   inflate 1503 view cards simultaneously.
10. Run Android build and unit tests, in-place install with original
    signing certificate, backup survey export and device field test.

## Future scientifically calibrated predictions
Needs measured habitat covariates, local historical records and
non-detections, seasonal effects, spatial blocking, calibration, and
uncertainty analysis. Never call a heuristic percentage a real
probability of a species occurring in the area.
