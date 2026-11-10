# LIKAS v0.24: coverboard, area-search and track-and-sign field protocols

Date: 27 October 2026
Base: `feature/v0.23-acoustic-analysis-and-trap-protocols`
Branch: `feature/v0.24-coverboard-area-sign-protocols`

## Included in this update

Three former **basic log** methods now have separate effort-and-evidence screens. Each is opened from the observation recorder's **Method Effort & Evidence** button during an active sampling visit. Archived surveys remain readable in History and in Home → History & tools → Coverboards, area search & signs. Recording an observation continues through the established LIKAS taxonomic and location workflow.

### Herpetofauna: coverboard inspection

- Record a permanent unique board ID for that survey, previous installation date in UTC, board material/dimensions, placement microhabitat and setup notes.
- Repeat inspection visits are linked to the same board, with observer, UTC timestamp, board condition, local substrate, air temperature, humidity, number of individuals, possible species and notes.
- A missing/uninspected board can be logged without falsely satisfying inspected effort. A missing board cannot be reported as holding observed individuals.
- Zero animals at a genuinely inspected board is a valid sampling result; no forced positive detection.
- Complete requires at least one registered board, with a valid inspection of every board. The regular sampling visit completion rule still applies.
- The separate main observation form handles taxon-level detections with its original photo, GPS and uncertainty fields. The board inspection itself is a board-level evidence record, not a substitute for a taxon-level observation.

### Birds: area search

- Record a stable area name/reference for the known search footprint, observer, actual start/end UTC, independently measured square meters, optional walked distance, habitat, weather, repeat-pass status and explicit movement/double-counting notes.
- A second pass with the same area reference must be flagged as repeated coverage, rather than silently treating it as new unique area.
- Pass area is **not summed** into the unique spatial footprint. The survey's existing required final measured searched area and active time remain separate completeness checks.
- Complete needs at least one valid timed measured pass plus the existing required total search-area, station and duration checks. A zero-detection search remains valid.
- The app does not claim every area has the same detectability; no automatic density or abundance estimator is produced.

### Mammals: track and sign

- Record measured search route or transect ID, observer, start/end UTC, surveyed length, substrate/track visibility, weather and search-bias notes.
- Attach multiple sign encounters to a route: track set, scat, scrapes, feeding sign, hair, den or other; age class, taxon or unidentified, confidence, substrate and notes.
- An optional unique encounter key cannot be repeated in one survey. This helps avoid counting the same track set twice.
- An effort segment with **zero signs** is a valid recorded search and can be Complete, subject to visit duration/other survey constraints.
- Track sets and sign evidence do not by themselves establish the number of individual mammals.

## New schema and exports

Additive SQLite database schema version **25** keeps existing observation, route, checklist, GPS, photo and acoustic records intact. Five new tables:

- `coverboard_units`
- `coverboard_checks`
- `area_search_passes`
- `track_sign_efforts`
- `track_sign_encounters`

Each exports as a correspondingly named CSV in the standard research ZIP. Keys link details to the original `surveys.id`, station, board or effort ID. The export includes zero-observation search segments and archived canceled/incomplete survey evidence. The app never infers missing GPS positions or replaces original human species identification.

## Survey outcome safety

The existing survey disposition actions remain accessible:

- **Finish Complete:** all original route/station/duration requirements and the new per-method required effort must be satisfied. Missing method evidence results in **Incomplete**.
- **End Incomplete:** retains collected data and documented effort.
- **Cancel Survey / Not Counted:** retains auditable records but excludes the survey from counted totals.

All new protocol inputs are restricted to an **active survey with an active station**. Historical records can be reviewed but not silently edited after closure.

## Methodological guidance

Field choices were informed by:
- Avian Knowledge Network, <https://www.dodakn.org/resources/dod-akn-standard-sampling-methods/>, for defined and timed area searches
- US National Park Service, <https://irmadev.nps.gov/DataStore/Reference/Profile/2256511>, for repeat coverboard sampling and local habitat measurements
- TERN signs-based fauna module, <https://www.tern.org.au/wp-content/uploads/Signs-based-Fauna-Module-v1_20230721.pdf>, for substrate and sign-effort metadata

The implementation is a **structured data collection module**, not a universal official sampling design. Observer teams must agree on deployment spacing, survey extent, measurement protocol, replication strategy and species-identification review before comparing campaigns. The app does not infer independence or habitat-adjusted abundance.

## Deliberately still tagged basic log

- **Spotlight survey:** requires method-specific beam/search width, travel effort, visibility and repeated-pass controls beyond current notes
- **Bat acoustic survey:** phone 44.1-kHz audio cannot replace a calibrated ultrasonic detector or manufacturer-specific recorder effort/call metadata

These remain explicitly labeled basic log. No false claims of calibrated bat detection or standardized spotlight effort.

## Tests

- `FieldProtocolPolicyTest`: zero-detection complete, missing board checks, measured interval/area/distance validity.
- `scripts/test_v024_field_protocols.py`: all five additive tables, Home/History/recorder integration, required pass/dedup fields, method flags and ZIP exports.
- Existing JUnit and Python GPS, survey-end, taxonomy, audio and camera/trap workflow checks remain in CI.
- Android `:app:testDebugUnitTest :app:assembleDebug` runs after the source tests.

## Physical-device acceptance still required

1. Herp visit: register three boards, inspect two with zero animals, attempt Complete (must become Incomplete). Inspect the third, finish all planned visits and test Complete.
2. Board installed in the past: confirm saved UTC installation date persists and inspection times are recorded separately.
3. Area search: record a 20-minute pass of known area, record zero birds, close with measured total area. Test Complete only after all visit criteria.
4. Record the same area reference a second time: require repeated-pass selection, preserve notes, never double the reported unique area.
5. Mammal track survey: one measured 200-m search without signs, then repeated sign encounters with identical key to verify duplicate-key rejection.
6. Record a track with unidentified species, unknown age and uncertain confidence. Confirm scientific exports make no guessed identification.
7. Reopen a finished and canceled survey through History, review archived board/check, area and sign records, and compare each CSV in the export.
8. Test portrait 320-dp phones, keyboard shown, 200% text scaling, GPS denied/returned, screen locks and database migration from v0.23.

Physical-device timing and ecological field acceptance have not yet been performed.
