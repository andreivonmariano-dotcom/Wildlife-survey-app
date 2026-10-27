# LIKAS v0.27.1 RC2 | Field usability and GPS display review
Date: 27 October 2026

Base: `release/v0.27-integrated-ux-performance-qa` at `401dcfe7a911fc593661edf4fba0827a1500e4f0`.
Version: `0.27.1-rc2`, versionCode 47, package `org.phwildlife.survey.beta08`.
Scope: follow-up to integration PR #31. This release candidate remains unverified on physical Android devices.

## Governing requirements
- G-01, G-05, G-07, G-10, G-11: truthful measurements, clear controls and old-record continuity.
- S-02 and S-06: always-visible End Survey, repeat-visit and interruption consistency.
- U-01, U-04, U-05, U-06: LIKAS branding, accessibility and concise screens.
- E-03, E-04, E-05: migration, regression tests and transparent release status.
- DL-01: direct in-chat file delivery after build verification. A GitHub action page is not an APK download.

## Work completed in this candidate
- **Station visits:** never render a missing GPS horizontal accuracy as `±0 m`. The UI now says `accuracy unavailable`, preserving the stored NULL.
- **Survey vs project scope:** project stations without a visit in this survey are identified as project-wide reference stations, not automatically classified as missed planned stops.
- **Historical state display:** legacy survey outcome derives from end timestamp and complete flag if outcome is NULL; no database mutation.
- **Method name:** use the recorded survey group and truthful historical method label, including unknown methods.
- **Project management:** move Home first, shorten the introductory copy, put station-ID explanation behind on-demand help, and display project names without repeated opaque ID strings in the dropdown. The permanent ID still appears in station edit dialogs.
- **Shared presentation:** bounded ripple response and common forest-green section headers, aligned Dashboard, Audit, station progress, and recording headings.
- **Scientific review:** clearer read-only warning and short introduction. Existing audit and export behavior stay intact.
- **Performance:** avoid duplicate initial station-progress rendering; onResume remains the sole initial draw.

## Unchanged scientific work
- Survey lifecycle: Finish Complete, End Incomplete and deliberate Not Counted.
- Project/station IDs, protocol durations, counts, route templates, GPS recording, MSL elevation provenance, taxonomy, photographic evidence, sound recordings, device checks and ZIP exports.
- SQLite schema 27, Android app ID, local storage and legacy survey records.
- Existing approved day/night HD panorama files and original minimalist frog launcher art. No regenerated substitutes.

## Automated verification
- `python3 -m unittest discover -s scripts -p 'test_*.py'` includes `test_v0271_field_polish.py`.
- `:app:testDebugUnitTest` and `:app:assembleDebug` build the full code and run JVM tests in CI.
- PR build remains a review build. GitHub Actions signs according to its restored signing key. Do not claim in-place upgrade capability until the certificate fingerprint is checked against an installed LIKAS binary.

## Remaining field acceptance tests
1. Use 320dp/360dp phones at normal font size and 200% text. Check screen overflow, touch targets, TalkBack, and one-handed Save/Review/End.
2. Visit a station with coordinates but NULL accuracy. Verify the screen explicitly reports unavailable accuracy, never `±0 m`.
3. Check project-wide sites with no visit in a given survey. Confirm they are not represented as missed planned survey stations.
4. Reopen archived Complete, Incomplete, Not Counted, and legacy NULL-outcome events; verify summary and history agree.
5. Run full Birds, Herpetofauna, Mammals and Custom workflows; preserve zero-detection sampling, pauses, resume and End Survey actions.
6. Test GNSS precision/no-fix, battery draw, reduced-motion time-adaptive forest rendering, camera/sound evidence, and low-end device performance.
7. Back up existing v0.15/v0.26 installation and test v27 migration and restoration using original media and acoustic records.
8. Compare Android package, signing certificate and versionCode with the previous installed binary before replacing it. Do not uninstall the working field app as an upgrade shortcut.

## Release gate
Passing compilation and source checks does **not** certify real-device usability, restoration, accurate ecological sampling, signing continuity, or private checklist redistribution rights. Keep this as a release candidate pending acceptance testing.
