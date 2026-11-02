# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current survey UI development branch:** `feature/apo-survey-flow-v139` (RC79 draft based directly on RC78; not merged into historical default `main`)  
**Latest verified signed APK:** `0.27.3-rc79-apo-survey-flow`, versionCode **139**, exact signed/tested source `bdc8f067e00740b89adab893ae65f97c07d0422c`  
**Android ID:** `org.phwildlife.survey.beta08` (unchanged); original signer retained  
**SQLite schema:** **28** (unchanged)

**APO RC78 live survey recorder preview, 2 November 2026:** Continued the BioTools overhaul in the most time-sensitive field screen. The recorder now has clearer Sampling Session/Detection Tools sections, accessible reasons when Save Observation is disabled for paused or unopened sampling units, and scalable persistent Save/Review/End Survey controls. Ending a survey warns before discarding incomplete drafts containing notes, distance, images, marked time, counts or other identification evidence, not just species text. Station progress distinguishes recorded survey visits from registered but unvisited project stations. Habitat preparation controls reflow instead of clipping text.

**Verified signed APK and emulator:** `APO-v0.27.3-RC78-Recorder-Emulator-code138.apk`, **11,755,020 bytes**, SHA-256 `8485de0a265490aea115e55b7510c3a381accceec9585d7daf0afed82f907b39`. CI run `41383237787` passed **415 Python scientific/UI regressions**, Android JVM/app/AndroidTest compilation, original package/signer/art/reference checks, and an **actual Android 14 emulator (API34) instrumented recorder test at 160% text size**. Emulator screenshot (native Android activity view, not unrelated system overlays): `APO-RC78-Live-Recorder-Emulator-160pct-Clean.png` (1080×2280, SHA-256 `740efe4692f055a2f0a9e2aa44c8673b317e479b98e30603695c1d1fe748665a`). See [RC78 recorder emulator QA](docs/APO_RC78_SURVEY_RECORDER_EMULATOR_QA_2026_11_02.md).

**Historical RC78 release boundary:** RC78 was a **signed development preview**, not production field-certified. The emulator confirms key recorder controls fit under enlarged text but the observation form begins below the fold, and no full on-device survey lifecycle, GNSS, backup or archived-data upgrade tests were performed. The screenshot is an app-view capture; one earlier emulator run displayed an unrelated Pixel Launcher ANR dialog in a raw screenshot. The full design and field acceptance gate remains open. Prior RC77/RC76 builds are historical.

**RC79 survey-flow changes, signed development preview:** The new code139 draft fits Review and End Survey side-by-side at 160% font when width permits, prioritizes the observation form in continuous non-transect methods, expands the unsaved-entry guard to additional evidence selections, and displays completion criteria when ending a survey. Signed GitHub Actions run `41384887314` passed 419 Python tests, Android/JVM compilation and one Android 14 emulator test at 160% text. The signed 11,755,588-byte code139 APK (SHA-256 `a45503c9facdf9f4d1a3dfd631c943ec6ef40da007d60086811f0d1a5d5daf6e`) was directly delivered in the development chat. The observation form enters the initial viewport, but the status HUD still occupies considerable height. Physical-device survey QA remains unverified. [RC79 survey QA](docs/APO_RC79_SURVEY_FLOW_QA_2026_11_02.md).

## What APO currently provides

- **Fieldwork:** birds, amphibians/reptiles and mammals, project and station records, GPS/elevation, observations, effort, survey history and analysis. A survey can end as **Complete**, **Incomplete**, or **Not Counted**.
- **Library:** the owner-authorized offline **714-species Mindanao PAIC** master catalog activates automatically (408 birds, 53 amphibians, 154 reptiles, 99 mammals) with the supplied habitat and elevation guides. Legacy illustrated references, cited habitat profiles and approved photos remain separate reference assets, not proof of a local species record. Global IUCN and Philippine DENR fields remain distinct; missing or unverified statuses are not automatically certified.
- **Settings:** backup/recovery, storage, import, integrity checks and Less motion.

The research/news feed and comprehensive ecological atlas remain planned features. Reference associations never create field observations.

## Development and QA

**RC48 Home and Fieldwork integration:** The RC47 shared components now serve survey setup and observation inputs. Active surveys have a dedicated Current survey section; narrow-phone capture actions reflow without removing the pinned End Survey dock. See [RC48 field UI QA](docs/LIKAS_RC48_HOME_FIELDWORK_QA.md) and [RC47 design foundations](docs/LIKAS_RC47_DESIGN_SYSTEM_FOUNDATIONS.md).


The current scientific build workflow is [`.github/workflows/core-science-qa.yml`](.github/workflows/core-science-qa.yml). It automatically runs on application/source changes and checks source contracts, evidence, database identity, pinned signing identity, photo rights, JVM unit tests, and APK compilation. Documentation-only changes do not trigger builds. Superseded runs are canceled.

Two slower device workflows are **manual acceptance checks**:
[`library-android-smoke.yml`](.github/workflows/library-android-smoke.yml) and
[`rc7-device-emulator-qa.yml`](.github/workflows/rc7-device-emulator-qa.yml). Their source and baseline requirements must be checked before running. An APK build alone does not establish handset compatibility.

Do not add APKs, keystores, local caches, emulator captures or generated photo-review packs to Git. These are covered by `.gitignore`. Signed APKs belong in short-lived CI artifacts and verified, direct conversation downloads.

## Current project records

- [RC49 History review and signed QA](docs/LIKAS_RC49_HISTORY_REVIEW_QA.md)

- [Project state and next-chat handoff](docs/LIKAS_PROJECT_STATE.md)
- [Decision log](docs/LIKAS_DECISION_LOG.md)
- [RC48 Home and Fieldwork integration](docs/LIKAS_RC48_HOME_FIELDWORK_QA.md)
- [RC47 shared design-system foundation](docs/LIKAS_RC47_DESIGN_SYSTEM_FOUNDATIONS.md)
- [RC46 field-layout hierarchy](docs/LIKAS_RC46_FIELD_LAYOUT_HIERARCHY_QA.md)
- [RC44 signed UI harmony QA](docs/LIKAS_RC44_UI_HARMONY_QA.md)
- [RC43 signed icon-integrated preview QA](docs/LIKAS_RC43_APPROVED_ICON_QA.md)
- [Locked 18-icon owner artwork](docs/LIKAS_ICON_ASSET_LOCK_2026_10_30.md)
- [RC41 source and device QA checklist](docs/LIKAS_RC41_FIELD_READINESS_QA.md)
- [RC40 screenshot defect and UI audit](docs/LIKAS_RC40_SCREENSHOT_AUDIT.md)
- [RC39 full application audit](docs/LIKAS_RC39_APP_AUDIT_2026-10-29.md)
- [Taxonomic source register](docs/PHILIPPINE_TAXONOMY_SOURCES.md)
- [Master product/scientific specification (on `main`)](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/blob/main/docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md)

Historical release decisions, original source records, scientific review ledgers and approved imagery remain in `docs/`, `app/src/main/assets/` and Git history. The obsolete v0.15 installation instructions have been removed from this README, not from version history.
