# Wildlife Survey v0.7: Field Data Dictionary

**Scope:** standalone offline Android Beta. The table states the actual recorded fields and planned additions distinctly. **M** mandatory, **A** automatic, **C** conditional, **O** optional.

## Entity and relationship plan
`Project 1 → many Sites 1 → many Survey Events 1 → many Observations 1 → many Media/Measurements`. Audit edits belong to observations and are append-only. Site and project boundaries are separate from individual GPS fixes. A repeated visit creates a new event, never overwrites the station.

## Actual v0.7 field storage

| Entity / variable | Database field | Units or values | Source | Priority |
|---|---|---|---|---|
| Project stable identifier | `projects.id` | Unique text ID | User | M |
| Project title | `projects.name` | Text | User | M |
| Project notes | `projects.description` | Text | User | O |
| Site ID | `sites.id` | Unique text ID | User | M for standardized events |
| Project link | `sites.project_id` | `projects.id` | User | M |
| Station label | `sites.name` | Text | User | M |
| Station habitat | `sites.habitat` | Text, not yet controlled | User | O |
| Station position | `sites.latitude`, `longitude` | Decimal degrees, WGS84 intended | User | O in Beta |
| Station notes | `sites.notes` | Text | User | O |
| Survey event ID | `surveys.id` | Unique immutable text | App | A |
| Survey profile | `surveys.survey_profile` | Birds, Herpetofauna, Mammals | User | M |
| Field method | `surveys.method` | SurveyMethods method code | User | M |
| Start / finish time | `surveys.started`, `ended` | UTC ISO 8601 | App | A |
| Complete indicator | `surveys.complete` | 1, 0 or null while active | User | M at finish |
| Station | `surveys.station_id` | Stable site ID | User | C |
| Project | `surveys.project_id` | Project ID | User | M |
| Survey observer | `surveys.observer_name` | Text | User | O in Beta |
| Bird point-count target | `surveys.point_target_s` | Seconds | User | M for point count |
| Transect bearing | `surveys.transect_az` | Magnetic degrees [0,360) | User | C for bird line |
| Actual transect length | `surveys.effort_length_m` | Meters | User at finish | C |
| Stratum ID | `surveys.region_label` | Text | User | O |
| Stratum area | `surveys.area_km2` | Square kilometres | User | O |
| Protocol scope | `surveys.protocol_status` | beta_standardized, basic_log_only, legacy | App | A |
| Bird count number | `field_records.individual_count` | Individuals or cluster size | User | M |
| Taxon label | `field_records.raw_identification` | Text, tentative names allowed | User | M |
| Scientific / common name | `scientific_name`, `common_name` | Text, starter checklist unvetted | App/user | C |
| Observation timestamp | `field_records.detected_at` | UTC ISO 8601 | App | A |
| Detection modality | `field_records.detection_method` | Visual, Auditory, Both, Sign, Camera | User | M |
| Horizontal bird distance | `field_records.horizontal_m` | Meters, not slant | User | C |
| Bird distance source | `field_records.distance_method` | Unknown, Estimated, Measured | User | C |
| Initial position assessed | `field_records.initial_position` | Unknown, Yes, No | User | C |
| Movement status | `field_records.movement_status` | Unknown, Stationary, Moving, Flyover, Entering after start | User | C |
| Bearing | `field_records.bearing_magnetic` | Degrees magnetic from phone | App | O |
| Inclination | `field_records.pitch_deg` | Degrees above or below horizon | App | O |
| Perpendicular distance | `field_records.perpendicular_m` | Meters | Calculated | C, line only |
| Observer GPS | `field_records.observer_lat`, `observer_lon` | WGS84 degrees, phone GPS | App | A if available |
| GPS reported accuracy | `field_records.gps_accuracy_m` | Meters | App | A if available |
| Behavior, habitat, notes | `field_records.behavior`, `habitat`, `notes` | Free text | User | O |
| Candidate indicator | `field_records.distance_eligible` | 0/1, **not an analysis-ready claim** | App | A |
| Soft delete | `field_records.deleted_at` | UTC timestamp | App | A when deleted |
| Edit log | `record_edits.*` | Before/after JSON and reason | App/user | A |

## Classification of missing information
- Null means not recorded or unknown, **never zero**.
- Survey with zero observations is retained in `surveys.csv`, not fabricated as a species absence.
- `observer_lat/lon` is the observer's location; do not present it as the bird's exact position.
- Magnetic azimuth is not true-north azimuth. Store source and magnetic reference. No assumed declination.
- The app doesn't measure bird distance or height by camera. Radial distances are entered manually.
- The old `sightings` records are copied to `field_records` once at the v4 schema migration and retain `legacy_source_id`; missing historical detail remains null.

## Future schema (not yet implemented)
- `site_visits` or dedicated hierarchical `sampling_units`, visits and sampling strata.
- Repeated count intervals, settling time, effective count duration after interruptions, observer-count, weather at observation, playback protocol, and GPS-fix age.
- `media` table: photo/audio file references, capture time, hash, target record, metadata.
- Structured habitat and vertical stratum tables, elevation source and uncertainty, local ground height and slope.
- `taxa` table: globally stable taxon IDs, checklist authority/version, accepted/synonym names, identification reviewer and verification.
- Sampling-design unit selection probability, stratification polygon area and effort covariates.
- Camera deployments, trap-hours/nights, acoustic recorder deployments, bat passes, amphibian call scale.
- Standardized export mapping to Darwin Core Event Core and Humboldt Extension, alongside distance-analysis flatfiles.

## Export definitions
`projects.csv`, `sites.csv`, `surveys.csv`, `observations.csv`, `distance_review.csv`, `edit_history.csv`. Every table uses stable IDs to link data. Soft-deleted observations are excluded from `observations.csv` but remain visible in audit data.
