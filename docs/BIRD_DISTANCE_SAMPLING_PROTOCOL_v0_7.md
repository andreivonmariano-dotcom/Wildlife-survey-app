# Bird distance-sampling specification: field workflow and validation

## Observation workflow in v0.7
1. From Home choose Bird → Point Count or Line Transect.
2. Select project, **registered station ID**, observer and duration (point count). For bird line transect, enter magnetic line bearing for the straight surveyed segment.
3. Upon first encounter use **Mark now**, then capture camera bearing only if needed. The camera never estimates distance.
4. Record identification, cluster size, horizontal radial distance from the point or observer, whether distance was estimated or measured, whether measurement represents initial detected location, and the movement category.
5. Record detection modality. Save even if species or distance cannot be resolved.
6. Finish survey with complete/incomplete status and actual line length when applicable. Zero-detection events stay in the database.

## Point counts
- Horizontal **radial** distance is distance from point center to cluster center at initial detection, not vertical slant distance.
- Set a fixed timed protocol, optionally a fixed sampling radius, settling time and distance truncation policy. Duration and radius must stay consistent within comparable surveys.
- First-detection time should reflect the encounter, not the time needed to type or photograph the bird.
- Count flock size only when individuals are distinguishable. Uncertain counts require a separate uncertainty code in a future version.
- Flyovers, entrants and birds moving after first detection require flags for selective analysis.
- Point counts use number of visits for `Effort` in common R Distance flatfile constructions. Survey duration remains an independent effort covariate.

## Line transects
- Walk a measured line or documented segment. Record actual surveyed length and an azimuth for the active straight segment.
- The recorded radial distance to the target should be horizontal, and direction should be measured relative to same **magnetic** azimuth reference.
- `perpendicular_m = horizontal_m * abs(sin(target_mag - transect_mag))`.
- Do not use a single line bearing for a curved or changing-heading transect. **Future upgrade:** GPS line geometry and segment-level azimuth.
- The target is the initial position of the bird or flock center, not its location after escape/observer movement.
- Area extrapolation requires a known study stratum and stratum area. The Beta captures optional label and area, not sampling selection probabilities.

## Candidate status vs scientific validity
`distance_eligible=1` is a preliminary *candidate* flag only. It requires bird group, known horizontal distance, known source (estimated or measured), first-position=Yes, non-flyover/non-entrant, and a distance-sampling method. A line encounter needs both transect and target bearings to derive perpendicular distance. **It does not prove** that distances, species identity, sampling design, independence, g(0), or observer assumptions satisfy model requirements. A specialist must review all these conditions.

Missing `distance_eligible` values and earlier Beta records stay review-only. The `distance_review.csv` export lists method, observed distance, cluster size, effort, stratum metadata and the candidate flag so records can be inspected without forcing model assumptions.

## Future mandatory enhancements for rigorous density inference
- Define station geometry, count time, distance limits, forest type, observer, visibility and noise.
- Record accurate initial distances, count intervals and detection process, including auditory cue differentiation.
- Record study-area frame/strata, sampling unit selection, adequate replication and encounter sample sizes.
- Distinguish radial versus slant measurements, and instrument/estimate calibration.
- Separate species-level analyses and site effort; explicitly export **zero-detection samples** for fitted species.
- Add R Distance-compatible flatfiles containing `Region.Label`, `Area`, `Sample.Label`, `Effort`, `distance`, `object`, `size`. Do not invent unknown study area or effort.

## Literature and standards
- R Distance flatfile: https://distancesampling.org/Distance/reference/flatfile.html
- General Distance resources: https://distancesampling.org/resources/whatisds
- GBIF sampling event quality: https://www.gbif.org/data-quality-requirements-sampling-events
- TDWG Darwin Core: https://dwc.tdwg.org/
