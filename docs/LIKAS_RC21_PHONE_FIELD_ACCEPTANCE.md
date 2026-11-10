# LIKAS RC21 | Phone field-test acceptance and regression log

**Candidate:** `0.27.3-rc21-endemic-fieldwork-preview` (versionCode 74, app ID `org.phwildlife.survey.beta08`, SQLite 27).  
**Date prepared:** 2026-10-29, Asia/Manila.  
**Status:** Phone-test specification. **No physical phone tests are claimed by this document.** Record exact build checksum, phone model, Android version, local time, storage and outcome on execution.

## Safety, update and evidence retention
- [ ] Before upgrading an existing field installation, create the full in-app research ZIP. Verify survey, observation, source media and effort CSVs exist and are nonempty where the old dataset has data. Store an external copy.
- [ ] Record the old package, versionCode and signer and compare the new APK's SHA-256 with the RC21 project-state record. Install via update, **never uninstall first**.
- [ ] Export after upgrade. Compare survey IDs, event outcomes, station IDs, linked original photographs, WAV recordings and the data-quality flag index. Classify every difference. A ZIP research export is not automatically a complete reinstalable private-DB backup.
- [ ] If upgrade fails, preserve the device state and log the error. Do not clear application data or perform a destructive reinstall.

## Survey lifecycle and zero-detection tests (all offline)
- [ ] Start Birds point-count with at least two planned stations; record a visit with zero detections. Verify station effort and the visit row persist without a fictitious animal observation.
- [ ] Start the next station, intentionally stop early. The footer End Survey must stay visible during long form scrolling and soft-keyboard entry. `End Incomplete` must preserve effort and observations.
- [ ] Repeat with all method-defined effort conditions met. `Finish Complete` must be available only if study/protocol validation passes. No planned station should become visited without an actual visit.
- [ ] Start a Birds line transect, mark an actual track, finish without measured route distance. Verify that the event is not treated as Complete. Planned distance must remain a design value.
- [ ] Start Herpetofauna VES and Mammal protocol encounters. Save partial notes and attach a method photo. Verify timestamp, evidence link, elapsed effort and method-specific fields after app restart.
- [ ] Cancel an active event through `Not Counted`. Confirm the archived event remains recoverable and is excluded from default analysis. Record a reason.
- [ ] Pause and resume a multi-station survey. Compare session elapsed duration with observer active effort and station effort; travel between stations must not silently become point-count effort.
- [ ] Force-stop the process after a saved observation and reopen. Verify active event recovery, saved media and no duplicate detection.

## GNSS and elevation truthfulness
- [ ] Cold-start outdoors, indoors, GPS permission denied, location disabled, stale fix, mock provider and restarted GNSS states. Log fix timestamp and reported accuracy for each.
- [ ] Speak and read the GPS status. The accessible description must match the live displayed status, including degraded accuracy or no fix, and GPS details must remain reachable.
- [ ] Save start location, station location and observation location only with the correct source and timestamp. Observer GPS must not be silently assigned as an animal's verified position.
- [ ] Verify altitude missing/unverified versus sourced MSL altitude; never coerce missing height to 0 m. Confirm no historical elevation overwrite during update.

## Species, research evidence and offline assets
- [ ] Open all four taxonomic groups without network. Search scientific, common and historical synonym names.
- [ ] Verify one newly added RC21 species diagnosis each for `Sus oliveri`, `Rusa marianna`, `Varanus samarensis` and `Crateromys schadenbergi`, with original source link and source type.
- [ ] Verify IUCN 2021 `Gallicolumba menagei`, 2021 `Anthracoceros marchei`, and Philippine national DAO 2019-09 `Crocodylus mindorensis` are labeled separately.
- [ ] Open source drawer and photo credits, then scroll to the final bibliography link. No fact should imply that a national checklist establishes provincial occurrence.
- [ ] Run the no-area and local-evidence filters. Confirm non-listed/unreviewed locality evidence cannot be reported as confirmed absence or confirmed presence.

## Accessibility and visual acceptance
- [ ] Use 320dp and a wider device at 100%, 150% and 200% system font. Capture Home, Survey Setup, Recorder, History, Species Library, sparse and rich Species Detail, source drawer and export dialogs.
- [ ] At 200% font and with soft keyboard open, End Survey remains operable with at least a 48dp target, and all three disposition choices are accessible without guessing.
- [ ] Run TalkBack focus through End Survey, Save, Review, GPS details, name suggestions, photo credit disclosure and citation filters. Each control must announce one concise, accurate purpose, state and action.
- [ ] Turn on reduced motion and battery saver. Verify no necessary text or action disappears.
- [ ] Test portrait rotation/resume, prolonged recording, phone calls and background/foreground recovery. Avoid duplicate survey starts.

## Power, backup, and QA disposition
- [ ] Record battery discharge and memory pressure during a four-hour continuous GNSS/recording session. Include exact device model, OS build, battery capacity and start/end battery percent, storage delta and thermal warnings.
- [ ] Confirm original photo and WAV bytes match exported copies. Verify missing/broken media references appear as flags, not invisible deletions.
- [ ] Tag each failure with: build SHA, phone model, Android version, exact reproduction steps, anonymized survey event ID, screenshot/log and severity P0/P1/P2/P3.
- [ ] Release gate: no P0 scientific truth/data-loss failure, no inaccessible End Survey, correct signed in-place upgrade, valid zero-detection effort and satisfactory source/license checks. A passing emulator suite does not clear phone-only failures.

**Data handling:** Use test projects or anonymized copies for screenshots, logs and GitHub artifacts. Do not commit field participants' private data or unreleased wildlife coordinates.
