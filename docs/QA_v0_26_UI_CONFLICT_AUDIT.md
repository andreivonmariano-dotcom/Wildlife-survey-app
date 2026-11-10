# LIKAS v0.26: interface declutter, conflicting labels and data integrity audit

Date: 27 October 2026
Parent: `feature/v0.25-spotlight-evidence-study-protocols`
Branch: `feature/v0.26-screen-declutter-conflict-audit`

## Audit scope

Source review of `MainActivity`, `WelcomeActivity`, `HistoryActivity`, `SpeciesPickerActivity`, `StudyProtocolsActivity`, `CallLibraryActivity`, `SurveyMethods`, `PhotoEvidence`, and shared `UiKit`. This is a focused UI and behavior cleanup, not a comprehensive architectural rewrite. No screenshots or instrumented device test results were available in this pass.

## Changes made

| Finding | Risk to usability or research | v0.26 change |
| --- | --- | --- |
| Recorder showed both a full-width **Find species** button and a separate direct-entry field | Dense form, little name-entry space at 320dp | Direct-entry and smaller Browse action share a row; separate quiet Unidentified/favorite controls remain |
| Repeated decorative recorder headings and empty status rows | Excess scrolling, visual noise | Removed unnecessary Mark detection/Species headers, hides blank progress/pause/photo status, removes duplicate generic target-time line |
| Camera/bearing control had ambiguous **Open camera** label | Confusion with later **Add photo** action | Renamed **Measure bearing** and clarified the sighting-camera action |
| Home buried common navigation in a list of 10 links | Common History and checklist require expanding a large menu | History and checklist placed as direct shortcuts, remaining tools grouped under Field evidence and Projects/reporting |
| Home contained an unused recent-survey renderer alongside live active-survey Home card | Dead implementation and conflicting page layout paths | Removed the unused method; preserved active survey Resume card |
| Study rule picker added a permanent second setup section | Long setup screen | Optional study settings now collapse behind one row; custom template name stays visible when collapsed |
| Returning from GPS could lose the study template displayed in setup | Potential wrong completion threshold selected without obvious feedback | Persists and restores selected template ID; refreshes choices for selected project and method |
| Survey History presented Station, Audio, Trap and Other method actions for every survey | Irrelevant buttons lead to empty screens | Displays audio only for Birds/Herpetofauna, device module for supported trap methods, other field module for relevant methods |
| Survey History queried SQLite for every search keystroke | Repeated UI-thread work and slow keyboard response | Debounces queries by 220ms and cancels outdated searches after navigation/destruction |
| Older closed surveys with NULL `outcome` treated as Active by filter | Misleading research status and counts | Historical outcome mapping uses end time and complete flag. No original outcome record rewritten |
| Unknown legacy methods silently shown as first modern method's label | Scientific protocol misidentification | New `SurveyMethods.displayLabel()` retains the unrecognized method code rather than inventing another method |
| History repeated date, empty `Station` labels and full closure audit text | Clutter and duplicate cancellation reasons | Hides missing station label; complete audit stays available behind Closure details |
| Species picker showed site evidence twice: always visible and in context dialog | Header consumed vertical space | Keeps geographic and model details in tap-to-open info while reducing permanent header text |
| Archived observation photos silently skipped if originals missing | A ZIP could look complete without its referenced image evidence | Missing or unsafe observation-photo paths now cause a clear export error rather than omitting files silently |
| Shared text-based controls lacked focus targets | Accessibility and keyboard interaction | Adds focus, minimum size and useful descriptions to Home tools, GPS details and checklist-context disclosure |

## Audit decisions: controls intentionally retained

- **Save Observation**, **Review Records**, and **End Survey** remain permanently docked, including with a long record form.
- Survey termination preserves three outcomes: Complete, Incomplete, and Not Counted. No database fields or complete-status rules were loosened.
- Bird, herpetofauna and mammal evidence fields, station progress, GPS start provenance and typed unidentified species remain.
- **Measure bearing** is not a duplicate of **Add photo**. The first marks camera/compass-based detection direction, the second attaches an original observation image.
- Detailed ecological evidence, recorder WAVs, time-coded labels, individual trap audits, study protocol snapshots and export CSVs remain accessible on their own pages.
- The previous LIKAS frog icon, HD forest art and forest/pond/cream palette are preserved. No unapproved new imagery has been substituted.

## Unresolved and prioritized follow-up findings

### High priority
1. **Device QA and visual verification.** The most important unknown is layout at 320dp, 200% font scale, system navigation and keyboard overlay. CI compilation does not verify usability or accessible reading order.
2. **Large activities.** `MainActivity` is over 2,300 lines and mixes drawing, GPS, drafts, SQLite, scientific validation and export. `WelcomeActivity` still manages several setup-state paths. Splitting these without loss of recorded context requires dedicated tests.
3. **Branch integration.** v0.26 stacks on v0.25, v0.24, v0.23 and earlier feature branches. The repository default `main` does not yet include this series. Never present the default-branch release as v0.26 until the migration and PR chain are integrated and tested.

### Medium priority
4. **UI-thread database work.** Survey HUD progress refresh performs periodic SQLite reads, and History list/search queries still run on the UI thread, now delayed but not backgrounded. Move to one serial database worker with a lifecycle-aware state update.
5. **Live GPS and survey preflight overlap.** Survey start and station acquisition have separate GPS checks. Both require coherent age, provider and permission handling on Android hardware.
6. **Cross-method equipment records.** Trap deployments and board inventories are currently scoped to a survey. True multi-day projects would benefit from reusable site-scoped installations with linked sessions, without duplicate equipment IDs.
7. **Media archival verification.** Research ZIP should eventually include checksums of every WAV and photo and a manifest of missing references. The latest change rejects missing observation photos, but is not a full backup-restore integrity test.
8. **Offline taxonomy build reliability.** External MDD v2.5 source sometimes returns HTTP 504. CI now retries source retrieval three times and still fails rather than silently publishing an incomplete mammal checklist.

## Test matrix

- Open Home with and without an active survey; verify Resume and the two shortcuts. Expand More tools; verify no duplicate History/checklist routes.
- Start Birds point count, Herpetofauna calling, Mammals spotlight, camera trap and Custom survey. Only method-related tools should appear.
- Enter and pick a species; change to Unidentified; favorite a record; attach a photo; save; review and end. Check no loss of direct-entry identification.
- Cancel GPS preflight and return to setup with a named study protocol. Confirm the same selected ID and visible collapsed study title, including after changing project/method.
- Open History filters with many surveys and type quickly. Verify no stale results appear on observation pages.
- Create a legacy completed survey with NULL outcome in a migration test dataset. Verify Active filter excludes it and Complete filter includes it. No old status should be rewritten.
- Open incomplete and Not Counted History records, review Closure details, and verify original reasons and record counts are available.
- Export observations containing a missing referenced photo file. Export must fail with the precise filename rather than omit the file. Real photos, WAVs and five method-evidence tables must remain in valid ZIP exports.
- Check 320dp, 360dp and larger widths, 100%, 150%, 200% font scales, TalkBack and Bluetooth keyboard.
- Verify the built APK on a physical Android handset across screen lock, long survey, no GPS, photos, WAV playback, low storage and offline start.

## Automated coverage added

- `SurveyMethodsDisplayTest`: known labels and true unknown legacy method labels.
- `scripts/test_v026_ui_audit.py`: Home shortcuts, fixed survey actions, study choice persistence, contextual History tools, correct legacy outcome SQL, photo export completeness and species UI.
- All earlier Java JUnit, taxonomy, method-completion, GPS, photos, acoustic and other Python source checks remain part of CI.

This is a source-level audit and focused refinement. It does not substitute for a complete device UX test and is not a guarantee that every method-specific interface has zero redundant taps.
