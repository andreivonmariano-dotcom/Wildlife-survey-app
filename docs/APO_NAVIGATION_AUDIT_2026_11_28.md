# APO navigation audit — 28 November 2026

Status: development candidate; not a release. Source pin: 8c0dabd2c0210827e0007793f07beb9475758402. The app-code changes are on draft PR #127. No charter change or production signing.

## Navigation contract

- Home is the launch workspace and has Start/Resume Survey. Five peer destinations (Fieldwork, Atlas, Map, Research, My Data) use a shared shell, with compact two-row treatment at narrow widths/enlarged text. Home is a dedicated header action at peer roots.
- One canonical workspace gateway uses explicit destinations and CLEAR_TOP/SINGLE_TOP. Root switches return via the Home workspace and selected-tab re-taps do not create another activity. Contextual bibliography, account and observation detail routes retain Back to their initiating activity. The in-app and Android Back actions must agree where an activity manages its own stack.
- Survey setup and recorder remain task-focused without peer tabs. A Back from setup retains the most recent same-group setup draft within that workspace (including Activity recreation); switching wildlife groups starts that group's setup. GPS cancellation returns to the setup. Recorder navigation and all survey outcomes are protected.
- History entered from the Map returns to the Map. History list/detail and recorder review preserve their distinct paths. History recreates its selected event and filters. Backup header and system Back share the busy-operation guard.
- At normal text, five peer tabs share a single row. At compact/large text, they reflow into two rows. Full visible labels, selected states, 48 dp targets and adequate map canvas must be measured on Android.

## Static inventory

Reviewed all 40 Activity declarations and their explicit Java Intent edges (source snapshot 55590e3, before the latest fixes). Explicit-intent scans cannot resolve every dynamic Class target, dialog choice, system picker, hardware or permission branch. The new automated manifest graph checks duplicate declarations, Java class existence, exported identity and declared explicit targets. Retain legacy activities until dynamic entry paths are reviewed.

| Screen group | Declared activities | Intended navigation |
|---|---|---|
| Launcher and workspace | LikasLaunchActivity, WelcomeActivity, LikasLibraryActivity, ApoOfflineMapActivity, PhLibraryBibliographyActivity, LikasSettingsActivity | Launch Home; peer switches via shared gateway; contextual research Back returns to caller |
| Survey capture and preflight | SurveyGpsActivity, HabitatsSpeciesActivity, MainActivity, CameraActivity, SpeciesPickerActivity, StationProgressActivity | Setup → GPS → optional checklist → recorder; child results and End Survey protected |
| Data workspace and tools | HistoryActivity, SitesActivity, HabitatRecordsActivity, FieldBackupActivity, DataManagementActivity, AuditActivity, ResearchExportActivity, DashboardActivity, ImportActivity, CallLibraryActivity, TrapProtocolActivity, FieldProtocolsActivity, StudyProtocolsActivity | Back to initiating workspace or live recorder/history; preserve transactions, backups and confirmation rules |
| Atlas and scientific detail | PhLibraryDirectoryActivity, TaxonDetailActivity, LivingAtlasExploreActivity, HabitatSpeciesEvidenceActivity, CatalogActivity, DiagnosticGuideActivity, Rc7SpeciesLibraryActivity, RegionalChecklistActivity, Rc8TraitComparisonActivity, Rc24ConservationDashboardActivity, Rc8PhotoComparisonActivity, Rc9DifferentialActivity, Rc9PhotoAnnotationReviewActivity, LikasDiscoveryActivity, LikasHabitatProfileActivity | Contextual Back and internal reader history; legacy destinations remain available where existing paths require them |

## Specific inconsistencies corrected in source

- Map/Research previously lacked the primary shared shell; Atlas-to-map and bibliography used direct separate activity launches. Five peer tabs now share routes and selection behavior.
- Workspace recreation could replay an incoming route or reset the screen. Consume route extra and save the displayed workspace; preserve contextual distinction.
- Map-linked observation detail previously used the archived-history Back behavior. Explicit origin map and caller-aware Back now restore Map. Save history filters and event across recreation.
- Setup Back previously rebuilt Fieldwork without retaining the entered draft. Same-group setup now restores the saved draft. Both header and Android Back use one action.
- Backup and audit headers incorrectly implied Settings or Analysis as universal parents; backup and audit use generic Back. Projects & sites label aligned with its card; backup uses Backup & restore. In-flight backup operation blocks Back.
- Navigation tests had four-tab and direct-launch assumptions. Updated only affected UI assertions; scientific, data, identity and export guards remain. Added actual root transitions, contextual Back, My Data children, three group setup and draft preservation, map source dialog and manifest graph tests.

## Validation ledger

- Previous candidate 37869e5: private QA 46151000996 passed source regression, media verification, Gradle compile, exact package/identity/signature and artwork exclusion gates. Emulator step still in progress at check.
- First navigation candidate 55590e3: run 46151688629 failed five obsolete source predicates (578/583 passed); no Android build or native tests executed.
- Latest candidate 8c0dabd: private workflow commit d00f5553d62fc971a7823f01dca292bb8f8cdd49 dispatched run 46152098370. Two independent Android 14 emulator jobs, 360x800 normal text and 320x720 200% text, offline after boot. Each plans 19 navigation/context/map/research/Atlas tests and two isolated recorder/closure tests. The runner is configured to capture private synthetic screenshots and logs. No complete result yet. Tests use only disposable QA package org.phwildlife.survey.apo, synthetic records and throwaway signer.
- Remaining: inspect both job results and screenshots, correct failures before merging; expand route coverage for every method, camera/audio permissions and activity result flows, deep detail/legacy routes, device/keyboard/system gesture/process-death transitions, map filters and root-state restoration, interruption and upgrade. Static inventory is not proof of all 40 screen routes or 100% reliability. No physical device validation, production signer, released APK or direct download.

## QA retry update

Both jobs in run 46152098370 failed the source gate: 582/585 tests passed, including both new manifest/Back tests; three exact historical setup assertions still expected the old Surveys header and direct setup calls. Android builds and native stages were skipped. Corrected only those predicates (589bba1, 05ef1d7, 0c1fb6c); app Java and instrumentation unchanged. Latest QA pin 0c1fb6cf2d9354cb8a1c81a79f59f7edad8c162b; workflow efb297a03b572eca8fc4adaca0058037ef28dfa1; retry run 46152239547, jobs 141001188363 and 141001188597. Setup underway at check. The earlier copy-cleanup emulator step remained in progress. No full native result or APK.
