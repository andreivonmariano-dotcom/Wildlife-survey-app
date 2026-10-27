# LIKAS v0.20: field layout guidance and optional survey-start GPS

Date: 2026-10-27
Base: `feature/v0.19.9-frog-theme-smooth-forest`

## Delivered: start survey with GPS missing

The initial survey GNSS screen presents a secondary **Start without GPS · mark start missing** action above the satellite chart. A confirmation explains the missing coordinate and later capture behavior. Normal auto-start after an acceptable GPS fix stays active. The existing 10 m/10 s/non-mock fix policy remains unchanged.

A deliberate skip:
- starts the survey timer and permits field observations
- stores SQL NULL for all four start-location fields, never 0/0, fabricated accuracy or later backfill
- stores `surveys.start_gps_status='missing_at_survey_start'`
- shows an amber reminder during the active recording session, including after app relaunch
- exports `start_gps_status` as a new final column of `surveys.csv`
- permits GPS to be started or restarted from the recording screen; later detection/station readings are independent, verified GNSS fixes

A verified start stores `verified_start_fix_under_10m`. Existing survey rows gain `legacy_start_fix_present` or `legacy_start_location_unrecorded` during one-way, non-destructive SQLite schema migration 21 to 22. Historical positions are unchanged.

Starting without GPS does not imply complete sampling. Point, station, transect, area, and survey outcome requirements still govern Complete, Incomplete and Not Counted.

## Proposed layout and appearance design (not implemented in this PR)

### Home
- Keep the approved small frog icon next to the LIKAS wordmark rather than an oversized hero on the field setup screen
- Show **Resume survey** as the first and strongest card when a session is active. Add the project, protocol, active/paused badge, duration, and last station
- Without an active session, show four large category targets: Birds, Herpetofauna, Mammals and Custom
- Move data management, exports, checklists and settings into one expandable Tools group
- Avoid repeated explanations and repeated Home buttons

### Survey recorder
- Keep a compact fixed header displaying survey group, current method and an always-readable total / active effort timer
- Add a small GNSS pill with text: Verified ±6 m / Searching / Stopped; the missing-start amber marker is independent and remains visible when later GPS becomes available
- Place the sampling station or point ID with its target countdown and completed/target progress immediately below the timer
- Make species search, group selection, quantity, detection mode and Save Observation the dominant interaction area
- Keep station transition, pause and End Survey available at all times with clear separation between normal and destructive actions
- Put habitat, elevation, noise, ID certainty and equipment in a collapsed, method-specific Advanced group; preserve their export fields
- Avoid stacking 3–4 full-width buttons if one action row can express the same intent

### Identify species
- Keep a full-width search field, sticky group chips, and only a small evidence/score hint in each result
- Put source, elevation confidence and detailed citation in the Details screen
- Make **Unidentified** readily available without requiring extra navigation, but never auto-infer a species

### Survey completion
- End Survey always available; end dialog clearly separates:
  1. **Finish Complete** (only if all method rules are satisfied)
  2. **End Incomplete** (preserve data and mark incomplete)
  3. **Cancel / Not Counted** (preserve auditable record, exclude from counts)
- Explain unmet criteria only when observer requests Complete

### Design tokens
| Token | Proposed specification |
| --- | --- |
| Background | Light cream `#F5F7F0` |
| Primary text | Dark forest `#153629` |
| Primary action | Forest `#236447` |
| Accent / confirmed GNSS | Pond green `#337E73` |
| Unverified GPS / pause | Warm amber `#A4662C` |
| Card corner radius | 12–16 dp |
| Body text | 15–16 sp |
| Section title | 17–20 sp |
| Touch target minimum | 48 dp |
| Form spacing | 8 dp inside groups, 16 dp between groups |
| Small-device target | 320 dp width; test 200% font, keyboard, night and sunlight |

A single font family (Android sans-serif) and simple outline icons are preferred. Day/night animated forest imagery belongs on the startup screen, not behind editable field forms.

## Test matrix

- Fresh GNSS fix under 10 m starts automatically without a second tap and stores valid nonnull coordinates
- No GPS, weak signal, disabled receiver, denied permission, or stale/mock fix: the observer may explicitly choose Start without GPS
- Cancel the confirmation: no survey exists, no effort clock runs
- Confirm skip: one survey exists with time, status missing and all four nullable start-GPS columns empty
- Restore active survey: amber start warning persists despite later GNSS improvement
- Acquire a good later fix: new observation or station gets its own independently verified coordinates; start GPS fields remain NULL
- ZIP surveys CSV includes new provenance column; old export columns and existing rows remain unchanged
- Verify Complete/Incomplete/Not Counted outcome rule and station completion still behave identically
- Test with airplane mode, GPS disabled, permission denied, and permission regranted; no crash or fake coordinate
- Device UI: 320 dp phone, large font, access to skip without scrolling past the GNSS sky chart

## Scope / risks

This version implements the explicit GPS-start bypass and a durable source-verified flag. Wider recorder visual redesign remains a proposal for a separate PR. MainActivity is a large Android Activity, so broad refactoring should be staged behind device screenshots and replayable instrumentation tests. The option is useful for notes in poor GNSS conditions, but the start location is scientifically missing and remains missing after later signal recovery.
