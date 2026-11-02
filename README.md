# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current APO development branch:** `feature/apo-biotools-v134` (draft PR #76 into RC73, not merged into older `main`)  
**Latest verified signed APK:** `0.27.3-rc74-apo-biotools`, versionCode **134**, source `f4f948c6533bb34ef55245aa2994c1ac5faf23a3`  
**Android ID:** `org.phwildlife.survey.beta08` (unchanged); original beta signer retained  
**SQLite schema:** **28** (data model unchanged; on-device historical upgrade test still pending)

**RC74 signed BioTools preview (2 November 2026):** The revised BioTools workspace has five text-only destinations: Surveys, Projects & Stations, Field Records, Analysis & Exports, and Field Utilities. Active survey Resume and the original Birds/Herpetofauna/Mammals setup remain, with no illustrated navigation or hidden More field tools dropdown. Existing records, analysis and utilities are directly navigable. A standalone research ZIP export now calls the exact same `MainActivity.writeResearchArchive` routine through Android's save-file picker, without starting a survey. It is separate from Settings' restorable backup.

**Verified build and direct delivery:** CI run `41375198786` passed **390 Python regressions**, JVM/Android build and AndroidTest compilation, package/signer checks, and byte-exact approved offline artwork and PAIC/WorldCover references. Downloaded APK `APO-v0.27.3-RC74-BioTools-code134.apk` is **11,747,380 bytes**, SHA-256 `09c96752003bda20660199a439485ba356558e740b59a37cffda0a2887ff0132`. Its ZIP integrity and embedded reference hashes were independently verified in the chat runtime. See [RC74 signed BioTools release QA](docs/APO_RC74_BIOTOOLS_SIGNED_QA_2026_11_02.md).

**Release boundary:** RC74 is a signed **development preview**, not stable/field certified. Existing survey record upgrades, real GPS, 200%-text / TalkBack on-device navigation, and research export end-to-end content still need device acceptance. RC73 and RC72 remain historical signed previews; do not offer them as the latest APK. Older default `main` is not the development baseline.

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
