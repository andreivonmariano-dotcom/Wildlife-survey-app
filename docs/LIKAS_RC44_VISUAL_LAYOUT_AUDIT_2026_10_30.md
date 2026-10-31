# LIKAS RC44 | Visual and layout source audit

**Date:** 30 October 2026 (Asia/Manila)
**Inspected:** active RC44 `0.27.3-rc44-ui-harmony-preview`, versionCode 104, SQLite 27; source HEAD `80b784ef38a4a36ef47c7a1e359ba6b6fc6b4cf0` before this documentation entry.
**Scope:** source layout and existing RC39–RC44 QA. No current Android screenshots, emulator render, physical handset, data migration or frame timings measured.

## Findings, ranked by user impact

- **V01 High: width-aware layouts are inconsistent.** `FieldLayoutPolicy.stackSurveyTiles()` specifies stacked survey tiles below 390dp or fontScale 1.30+, yet `WelcomeActivity.fieldworkHub()`, `LikasLibraryActivity.toolPair()` and `LikasSettingsActivity.options()` use a fontScale 1.60-based `largeText()` condition for paired cards. At narrow widths labels can wrap excessively. Use shared width/font-scale rules.
- **V02 High: species picker search field is cramped.** `SpeciesPickerActivity` uses a 45dp dropdown and 122dp Unidentified button, plus 7dp gap, in the same horizontal row as search. After 32dp page padding, approximately 114dp remains for search on a 320dp screen. Give search a full-width row while preserving a clearly visible unknown-identification action.
- **V03 High: library gallery overreserves vertical space.** `LikasDiscoveryActivity.shelf()` slots have minimum heights 410dp (625dp large-text); internal cards declare 280/296dp (336/355dp large-text) minima. Adjust to measured card content without causing swipe/async-photo jumps.
- **V04 Medium-high: Home height and hero overlay risk.** Home has a fixed 152dp hero; optional 92dp Resume panel; `EXPLORE LIKAS`; three minimum 108dp tiles. On shorter devices, lower portals may require scrolling. `LikasPortalUi.hero()` overlays a 54dp frog with a 26sp title, without reserving horizontal text clearance. Reflow active-session Home and safe-inset the art. Visual clipping requires screenshot verification.
- **V05 Medium-high: recorder viewport density.** `MainActivity` has top Back/title/Export, HUD, scrollable form, and pinned Save/Review/End footer. The keyboard uses `adjustResize`, but narrow/large-font viewports may leave little form space. Condense secondary status while retaining both effort and fix provenance, plus all survey actions.
- **V06 Medium: small/fixed controls.** Recorder Pause/Station progress rows are 47dp and count controls 46dp; species dropdown 45dp wide. `UiKit.navigationHeader()` uses a 48dp Back box with 26sp arrow and single-line ellipsized title. Use at least 48dp tap targets and content-aware heights at 200% font.
- **V07 Medium: repeated headings.** Home hero + `EXPLORE LIKAS`; Library title + `BIODIVERSITY` + `Species & habitats` + `Offline field guides`; Discover's duplicated species/habitat headings. Shorten without removing biological evidence or methodological caveats.
- **V08 Medium: inconsistent navigation labels.** `TaxonDetailActivity` uses Back label `Back`, making the spoken action `Back to Back`. `AuditActivity` uses Home despite direct Settings entry. GPS and Camera use separate navigation header styles. Fix actual route semantics.
- **V09 Medium: GPS retry controls below 290dp sky view.** `SurveyGpsActivity` places satellite diagram before retry, location settings and cancel. Move essential acquisition actions above optional visual detail; preserve choice to start with GPS missing.
- **V10 Medium: components across reference, field, history and GPS remain visually divergent.** Create shared patterns for header, item, forms, status, empty/error and evidence. Do not weaken the functional Camera/Recorder differentiation.
- **V11 Low-medium: contrast checks.** `UiKit.BORDER` on warm background is ~1.23:1 and on white ~1.31:1; amber on warm background is ~4.35:1 (context-dependent). Check true foreground/background pairs and boundaries in sunlight.
- **V12 Low: unused design API inputs.** `LikasPortalUi.tile()` receives `tint` and `background` but ignores them in favor of title-based fill decisions. Remove unused inputs or implement explicit styles.

## Preserve and verify

Retain Home's Fieldwork / Library / Settings architecture, pinned End Survey with Complete / Incomplete / Not Counted, recorded missing GPS and elevation provenance, offline catalog, current app ID, schema and signing identity. Do not edit or regenerate the 18 hash-locked approved PNGs, the frog launcher or licensed wildlife photos.

## Acceptance order

1. Correct V01–V03/V06. Test 320/360/390/412dp widths and fontScale 1.0/1.3/1.6/2.0.
2. Correct V04–V09; inspect active survey, keyboard visible, GPS unavailable and verified, unknown species, and scrolling in each library shelf.
3. Align V10–V12 and inspect contrast, TalkBack labels, pixel icon scaling and reduced motion.
4. Execute Android instrumentation and take actual screenshots; run real handset and in-place upgrade tests before field acceptance. The RC44 build compiled AndroidTest but did not run it.

**Disposition:** Source-only audit, not visual device proof. No Android code, approved artwork, SQLite records or APK changed. The prior non-atomic restore and research-export historical outcome mismatch remain distinct high-priority safety findings.
