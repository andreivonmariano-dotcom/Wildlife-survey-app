# LIKAS v0.21: field-first layout, previews and validation

Date: 2026-10-27
Base: `feature/v0.20-optional-survey-start-gps`

## Implemented screen changes

### Field recording, Birds / Herpetofauna / Mammals
- A fixed, compact survey HUD now displays total and active effort time, method/station timer, station progress, GPS health, pause state, and the independent missing-start-GPS warning. Fields below remain scrollable.
- Fixed bottom dock: full-width **Save Observation** primary action, plus **Review Records** and persistent outlined **End Survey** controls. A higher visual priority for Save does **not** hide survey completion, early end or cancellation.
- Transect magnetic bearing moves to its own row only on applicable methods, reducing clipping on 320 dp screens.
- Existing group panels, photographic evidence, taxonomic review, scientific distances, station/point navigation, survey outcomes, data export, and GNSS validation are unchanged.
- Android now uses `adjustResize` for the recorder so the keyboard does not automatically pan the action dock out of view.

### Home
- LIKAS frog icon and wordmark remain compact.
- During active work, Home prioritizes one Resume Survey card. Where available, it shows profile, method, and project; it keeps the requirement to close an existing survey before a new one starts.
- Otherwise, Home shows Birds, Herpetofauna, Mammals and Custom in a 2×2 grid with descriptive captions.
- Existing history, checklist, projects, dashboards, data audit and import features stay under the folded History & tools menu.

### Survey setup
- Small Back control and compact heading.
- Section guide: 01 Survey method, 02 Project and location, 03 Observer and effort, 04 Begin survey.
- Method radio controls are grouped in one rounded surface with a full-size selectable row and pond-teal selection mark.
- Unused method controls retain their existing visibility logic, and supplementary regional settings remain collapsed.
- Start action says **Continue to GPS**, reflecting that the next screen handles verified GPS or the observer's explicit Start Without GPS confirmation.

### Shared visual style
- Cream background `#F5F7F0`, dark forest text `#153629`, main actions `#236447`, pond teal `#337E73`, warm GPS/pause warnings `#A4662C`.
- Shared rounded surfaces, 48 dp minimum buttons, 104 dp Home grid cards.
- The forest imagery stays on the startup screen, not under data-entry forms.

## Preview set

Created a ten-screen conceptual preview storyboard in this conversation. It covers Startup, Home, Bird Setup, GNSS start (including missing-GPS choice), Bird Recorder, Species Search, Herpetofauna Recorder, Mammal Recorder, Progress, End Survey Outcomes, and History. Previews are **visual concepts**, not automated emulator screenshots or proof that every secondary screen received the new layout. In v0.21, the new layout is implemented for Recorder, Home and Survey Setup only.

## Data-integrity safeguards

This version makes no schema migration, species checklist edits, scoring changes, GPS policy changes, effort rule changes, survey end disposition changes, or export format changes. It inherits the additive version-22 GPS provenance field and the Complete / Incomplete / Not Counted rules from v0.20.

## CI checks

- `scripts/test_field_layout_contract.py` asserts fixed HUD and dock order, persistent End Survey action, 2×2 home tiles, setup sections, and continued GPS status/export wiring.
- Existing `test_likas_branding.py` checks the revised dock structure and startup asset fingerprints.
- All existing source-processing, GPS decision, route, sampling, and survey-close tests remain enabled in GitHub Actions, followed by `app:assembleDebug`.

## Handset acceptance cases not covered by source tests

1. 320 dp width / 200% font, portrait, with soft keyboard: Save and End Survey remain accessible; no text overlaps.
2. Bird count at station 3 of 8, including pause, GPS signal loss, restart, unknown species, observations and evidence.
3. Herpetofauna amphibian call survey and reptile encounter: method-specific fields stay visible in the right context.
4. Mammal track/sign station visit: device and effort fields remain accessible without bird-only measurement controls.
5. No GPS at initial survey start: persistent amber warning, verified later observations, and missing start coordinates in exported CSV.
6. Survey end Complete, End Incomplete and Cancel / Not Counted: all three retain the existing audit behavior.
7. Landscape not supported by this portrait-first beta; verify small devices and large display scaling.
8. Check TalkBack names, focus order, 48 dp controls, gesture back, keyboard layout, and screen scroll restoration.

## Next refinement

Review handset screenshots before widening the four-step setup concept into a multi-page wizard. That change would carry persistence and navigation risk, so the current four sections deliberately stay on one screen.
