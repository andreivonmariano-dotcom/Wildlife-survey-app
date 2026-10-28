# LIKAS RC4 Scientific Core | Field Workflow, Species Evidence and Architecture Audit

**Date:** 28 October 2026, Asia/Manila  
**Branch:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38, based on `feature/v0.27.3-startup-living-forest`.  
**Release identity under review:** `0.27.3-rc4`, versionCode 49, `org.phwildlife.survey.beta08`, SQLite schema 27.  
**Scope:** Field survey, species taxonomy and evidence, ecological data, scientific exports, data quality, non-startup UX and app architecture. **Startup animation, background assets and launcher icon are excluded.**

## Method and claim levels

Reviewed actual Java source, SQL migrations, Python/JUnit source tests, README, the v0.27 integration screen matrix and fieldwork project notes. GitHub build compilation is distinct from physical phone tests or scientific validation. No device GNSS, spectrogram, survey-hour playback, screenshot, battery test, taxonomy revalidation against new external checklists, production data migration or user survey records were inspected for this report.

Priority labels: **P0** means scientifically misleading or data-threatening; **P1** means high-impact reliability or responsiveness; **P2** means correctness/completeness or usability; **P3** means later feature development. A source-backed *risk* is not a demonstrated field failure.

## Q1 | Field survey tools and scientific workflow

| Scope | Source-backed finding | Assessment / action |
|---|---|---|
| Survey lifecycle | `MainActivity.finishPrompt` exposes Finish Complete, End Incomplete and Cancel / Not Counted. `SurveyClosePolicy.resolve` checks effort, stations, route and area; `StudyProtocols.passes` and method policies supplement validation. Outcome and exclusion reason are stored with an outcome audit. | **Existing safeguard.** Run actual handset transitions with observations, zero-detection visits and paused effort. |
| Scientific zero detection | `ResearchExports.occupancyVisits` outputs completed visits separately from event detections; research queries omit Not Counted. | **Existing safeguard.** Verify zero-detection visit rows and incomplete/not-counted exclusion with actual sample exports. |
| GPS / elevation | `SurveyStartGpsPolicy`, `GpsFixPolicy`, survey-start status, station GPS and `GpsElevation` preserve accuracy/age and distinguish missing MSL measurements from verified height. | **Existing safeguard.** Under-10-m startup threshold is strict; verify GPS cold start, denied permissions, stale fixes, pause/restart and elevation datum on representative phones. |
| Long session | `TrackService` uses a single-threaded `trackWriter` queue and ordered SQL writes. `onLocationChanged` can enqueue accepted and rejected GNSS fixes, with writer shutdown during destruction. | **P1 risk, not measured:** queue is not visibly bounded, and recovery after sudden process death is unverified. Consider bounded queue and reliable stop-flush only with data-loss tests. |
| Read-only scientific audit | Previously `AuditActivity.scan()` synchronously called `DataAudit.run` on the UI thread. The audit contains many database queries and evidence-file checks. | **P1 corrected in draft:** run on a dedicated background worker with visible progress, lifecycle cleanup and explicit read-error state. Tests and device validation follow. |
| Database and archive | `FieldDb` uses additive migration through schema 27. Existing tests exercise migrations v23–v27 and SQLite copy/restore. Android manifest has `allowBackup=false`; research ZIP is an export, not a verified reinstallable backup. | **P1 release gate:** validate a real prior-installation database and archived media, in-place APK upgrade, ZIP completeness and documented recovery before stable release. |
| GPS, media and acoustics | Photos are stored in private files with metadata; protocol photos, WAV/call labels and field protocols have dedicated classes. | **Existing source routes.** Test lost/missing source file behavior, microphone/camera permission denial, long WAV, archive consistency and field capture after process interruption. |

## Q2 | Species information, identification and ecological validity

### Current baseline

- `BundledCatalog` expects **761 birds**, **116 amphibians**, **380 reptiles** and **363 regional source-evidence entries** from the installed private Philippine checklist asset. `ChecklistPackImporter` verifies four checksum-pinned CSVs with taxonomic source labels and a transactional import. The mammal importer uses attributed Mammal Diversity Database v2.5 material.
- `taxon_catalog`, `taxon_synonyms`, `taxon_occurrence_evidence` and `taxon_habitat_profiles` are separate concepts. This is important: a name appearing on a national checklist does **not** establish station or province-level occurrence.
- `TaxonDetailActivity` presents citations, synonyms, regional evidence, review status, conservation snapshot fields and habitat notes. `HabitatLikelihood` is a simple evidence-screening index, **not** validated probability of occurrence.
- A user's tentative or unidentified field record can remain verbatim. The catalog does not automatically rewrite historical identifications.

### Confirmed source defects fixed in draft PR #38

| Priority | Previously | New source change |
|---|---|---|
| **P0** | `SpeciesPickerActivity` counted any province/island occurrence row in its local evidence rank, including imported `review_status='unreviewed'`. This could display an unreviewed entry as documented positive local evidence. | Both province and island `EXISTS` queries require `review_status='source_listed'`. Unreviewed source rows stay in storage and reference details. |
| **P0/P2** | `RegionalChecklistActivity` showed all imported occurrence records as “source-listed taxa” without filtering review status. | Regional lists require `source_listed`. A failed database query now displays an error instead of falsely implying no species evidence. |
| **P2** | `TaxonDetailActivity.lookup` searched current names but not `taxon_synonyms`, despite existing former-name support in the survey suggestion adapter. | Species-reference search now resolves former scientific names to the accepted catalog record. The regional checklist name filter does the same. Original records are untouched. |
| **P2** | Davao Region picker listed only four provinces and omitted Davao Occidental. | Added all five provinces, plus individual Davao Occidental selection. A visible note states separate Davao City evidence is not assessed by this province-only view. Refer to the Philippine Statistics Authority's PSGC Region XI list: `https://psa.gov.ph/classification/psgc/provinces/1100000000`. |
| **P2** | Regional list refreshed SQL on every keystroke, silently capped results at 1,000 and swallowed read errors. | Added 180 ms debounce, 1,001-result truncation detection, visible cap warning and named error state. All matches still originate from installed sources. |
| **P2** | A user could select `Local source records` without choosing an island or province and receive an empty list that resembled species absence. | The picker now explicitly requests area selection and states that an empty locality scope does **not** imply species are absent. |

### Scientific library development, staged recommendations

1. **Identity and nomenclature.** Separate accepted taxon key, authored taxon concept, current/previous names, local common names, name-source version and identification confidence. Keep original observation's verbatim name immutable and allow a reviewed later determination record.
2. **Field identification.** Introduce explicitly cited diagnostic characters, side-by-side comparison, age/sex/life-stage keys and uncertainty flags. Do not treat external photographs or xeno-canto recordings as embedded licensed files, and do not claim machine-learning identification without independently evaluated model data.
3. **Geographic scope.** Expand the occurrence schema for **administrative regions and cities** as distinctly typed evidence categories only after reviewing source documents and provenance. Current import supports island/province, so the Davao City limitation is disclosed, not guessed away.
4. **Conservation status and ecology.** Store dated IUCN/Philippine/CITES source snapshots, reference page/date/edition, method of geographic inference, habitat citation and altitude/datum uncertainty. Keep missing assessments visibly missing, not scored as absence.
5. **Taxonomic reconciliation.** Review `BundledCatalog`, `ChecklistPackImporter`, starter `Taxa` and the independent `GeoRegions` examples for duplicate or contradictory scope. Do not silently merge national lists, starter examples or different taxonomy versions.

## Q3 | Architecture, responsiveness and field usability

| Priority | Finding / risk | Scoped action |
|---|---|---|
| **P1** | `MainActivity.java` is **2,413 lines** and mixes live UI, survey creation/close state, GPS, station effort, observation fields, database writes, photo links and research ZIP export. | Extract a testable survey session coordinator and observation repository incrementally. Preserve SQL semantics, single active-survey ownership, crash recovery and archived data. Do not rewrite all screens in one release. |
| **P1** | `AuditActivity` read checks were synchronous on UI, as above. `DashboardActivity` does multiple synchronous aggregate counts in `render()`, and `HistoryActivity` performs multiple inline queries. | Audit worker fixed first. Next measure query latency, then move heavy dashboard/history aggregation to a bounded executor with stale-result cancellation. |
| **P1** | Database integrity and original attached files are mission-critical, but source-level tests do not prove restore from a real v0.15/v0.27 installation. | Build repeatable anonymized upgrade/restore fixtures and verify media references, survey outcomes and audit trail on emulator and handset. |
| **P2** | Large result sets have search limits, and the regional checklist previously hid its cap. `SpeciesPickerActivity` already uses a worker, generation token and paginated cards. | Reuse the worker/pagination pattern for large read-only lists; validate 320/360 dp, 200% font, TalkBack and long names. |
| **P2** | Catalogs and geographic evidence are spread across `Taxa`, `GeoRegions`, `BundledCatalog`, `ChecklistPackImporter`, picker and detail views. | Introduce one read-only taxon/evidence repository API and one source-version policy. Validate names and geographic basis before changing the database schema. |
| **P3** | No implemented independent research/conservation news screen is established in the inspected Android activity tree. | Future Research Watch: dated source links, publisher, DOI/URL, retrieval time, offline cache and clear unavailable/offline state. Avoid presenting headlines as verified conservation assessments. |

## Next implementation order and acceptance

1. **Merge candidate after QA:** P0 geographic evidence-status fixes, former-name search, Davao source-area correction, regional search feedback and background read-only scientific audit. No schema migration or startup changes.
2. **Scientific regression:** Test `source_listed` versus `unreviewed` (national/island/province); approved former names; starter duplicates; unassessed ecology; zero detections; and empty/missing evidence. `scripts/test_core_species_evidence.py` uses synthetic SQLite fixtures and the real taxon-detail SQL extracted from Java.
3. **Fieldwork regression:** Complete, Incomplete, Not Counted, active resume, no-fix GPS, photo/WAV exports, old-data migration, evidence audit and canceled/incomplete research handling.
4. **Device performance:** 320dp/360dp and 200% font; 100 observations, 10 images, prolonged GPS, repeated list searches, full audit and export. Record responsiveness, PSS/heap, jank and battery before claiming improvement.
5. **Later architecture:** Extract read repositories, state transitions, export worker and GNSS logging in small steps with controlled fixtures and migration gates.

## Nonclaims

No APK has been installed or tested on a physical device as part of this scientific-core audit. No all-species ecology model, current conservation status update, automated photographic/audio species identifier or live research-news feed was delivered. Changes on the core branch are not to be merged blindly with the independent startup/sprite development branch. The stable/current RC4 candidate and startup assets remain unchanged.
