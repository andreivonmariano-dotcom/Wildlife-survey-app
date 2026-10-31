# LIKAS RC26 | Real Android handset field-acceptance plan

Date: 29 October 2026, Asia/Manila. Scientific-core draft PR #38.
**NO PHYSICAL PHONE TEST PERFORMED.** This file describes acceptance work for the owner or field team, not completed test evidence.

Target current phone-test preview: v0.27.3 RC26C fieldwork readability, code84. RC26 code82 and RC26B code83 are superseded following 200% tab-clipping failures. Package `org.phwildlife.survey.beta08`, schema27, current debug signing certificate unchanged.

## Principle and protective setup

Do not wipe, uninstall, clear data, or change app ID/signing certificates on a handset holding historical field records. Save an independently accessible full LIKAS fieldwork backup, and separately verify the exported research ZIP of records and media. A research ZIP is not itself a proven in-place restoration archive. Test a spare handset or complete cloned and consented dataset before an owner's original phone.

## Planned device evidence (all PENDING)

- **Install / in-place upgrade:** Document manufacturer, device model, Android version, available storage, prior LIKAS versionCode and installed certificate fingerprint. Preserve older surveys, all original record IDs and timestamps, historical GPS values, species names, photographs, WAVs, and complete/incomplete/not-counted outcomes. Compare verified checksums/row counts before and after upgrade. Inspect links to existing media files. A disposable emulator fixture is not owner-data proof.
- **GNSS and elevation:** Outdoor cold and warm starts with time and location permission choices; log GNSS fix timestamp, horizontal accuracy, altitude availability, altitude reference, and vertical uncertainty. Confirm no coordinates or false ±0 m accuracy are silently invented. Confirm phone permission denial and stale fix remain visible missing-data states.
- **Complete / Incomplete / Not Counted:** Create a named test project and multiple independent surveys. A method-eligible run with fulfilled effort closes Complete. A deliberately partial run closes Incomplete and keeps observations. A canceled run closes Not Counted with an exclusion reason and remains in archive, but does not contribute to standard analytic counts. End Survey must be visible at all times.
- **Scientific zero detection:** Finish a legitimate effort visit with zero detections. Confirm one visit/effort export row and no fabricated species observation. Verify archive displays the valid zero-detection survey.
- **Photographs:** Capture and attach original photo(s) with permission allowed and denied states. Record file identifiers, timestamp, data export link, preview behavior, and SHA-256 before and after backup/upgrade.
- **WAV and acoustic evidence:** Capture a short labeled WAV and a longer field session. Retain original sample rate, duration, file bytes, species label confidence, recording provenance and unlinked/permission-denied states. Measure battery use and test a locked phone for field-session persistence.
- **History and corrections:** Search archived complete/incomplete/not-counted events; inspect the original taxon string and any later correction audit. Confirm deleted-record filters, unchanged historical specimen identifications, and linked evidence.
- **Export and restore:** Check CSV relationships across surveys, visits, effort, detections, GPS, site environmental records, and source media. Verify original image/WAV bytes and record IDs. Exercise full backup restore only on a disposable/clone device, then repeat export.
- **Large text / TalkBack:** Test 100%, 160% and 200% font on a narrow phone. Inspect Home, Survey Setup, active recorder, History, Species Library, Habitat, source dialogs, backup, and data management. Verify focus order, labels, touch height, action visibility and native Back navigation.
- **Power, resilience, and offline:** Airplane mode, screen lock, process interruption and battery saver tests. Record total observation counts before and after interruption and confirm no duplicate closures. Document device temperature, battery percentage and elapsed sampling time.

## Evidence register

| Test | Actual handset evidence | State |
|---|---|---|
| Historical owner dataset upgrade | Not available | PENDING |
| End Survey 3 outcomes / zero detection | Not available | PENDING |
| GNSS provenance and MSL uncertainty | Not available | PENDING |
| Photo and WAV long-session capture | Not available | PENDING |
| Full backup/restore + survey CSV/media links | Not available | PENDING |
| 200% large-text screenshots + TalkBack | Not available | PENDING |
| Power/offline/background reliability | Not available | PENDING |

The Android emulator check is a separate CI gate. A pass there does not complete any of the handset checks above.
