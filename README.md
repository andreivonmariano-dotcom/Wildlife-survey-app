# LIKAS

LIKAS is an offline-first Android biodiversity fieldwork and reference tool for Philippine wildlife surveys.

**Active development branch:** `feature/likas-rc71-public-preloaded-paic-references`  
**Current signed development APK:** `0.27.3-rc71-preloaded-paic714-worldcover` (versionCode **131**)  
**Application ID:** `org.phwildlife.survey.beta08` (original signer retained)  
**SQLite schema:** **28** (upgrade from v27; no survey-record deletion)

**Current signed build status (1 November 2026):** The owner authorized distributing their harmonized 714-species Mindanao PAIC checklist publicly in the LIKAS repository and APK. The 16-tile derived ESA WorldCover 2020 land-cover reference is shipped under CC BY 4.0 with full attribution and modification notice. Both source files are committed in `app/src/main/assets/` and activate automatically at first launch, with no manual import step. RC71 CI **41322915705** passed 377 Python source/science tests, JVM tests, Android APK + AndroidTest builds, signer verification, and independent SHA256 checks of the exact files *inside the signed APK*. Direct owner APK: `LIKAS-v0.27.3-RC71-PAIC714-WorldCover-code131.apk`, SHA256 `76620a2ae6d102012fc7e08740049e2e562f262e01f462bf63dd46e023ccbe6d`. This is a **development preview** until physical-phone startup, historical migration, GPS and UI acceptance are verified. Public source is staged in draft PR #72, inheriting PRs #70–71. The old trusted PR lineage gate requires literal SQLite27 and must be reviewed for current schema28.

See [RC71 reference packaging and QA](docs/LIKAS_RC71_PUBLIC_REFERENCE_EMBED_QA.md) and [latest project state](docs/LIKAS_PROJECT_STATE.md).

## What LIKAS currently provides

- **Fieldwork:** birds, amphibians/reptiles and mammals, project and station records, GPS/elevation, observations, effort, survey history and analysis. A survey can end as **Complete**, **Incomplete**, or **Not Counted**.
- **Library:** the full offline Philippine species checklist plus **96** curated illustrated research accounts, **15** habitat profiles, cited species/ecology references and approved offline photographs. Global IUCN and Philippine DENR statuses appear only when independently reviewed and dated. Unreviewed statuses stay neutral.
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
