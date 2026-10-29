# LIKAS

LIKAS is an offline-first Android biodiversity fieldwork and reference tool for Philippine wildlife surveys.

**Active development branch:** `feature/likas-core-fieldwork-post-rc4`  
**Current source candidate:** `0.27.3-rc41-field-readiness-preview` (versionCode **101**)  
**Application ID:** `org.phwildlife.survey.beta08`  
**SQLite schema:** **27**

**Release gate:** RC41 is source code until its GitHub Actions scientific QA succeeds and the exact signed APK is inspected. Do not treat an older APK or a queued build as the current release. The most recently verified signed preview is RC39 (versionCode 99); physical upgrade testing is still required.

## What LIKAS currently provides

- **Fieldwork:** birds, amphibians/reptiles, mammals and custom protocols, project and station records, GPS/elevation, observations, effort, survey history and analysis. A survey can end as **Complete**, **Incomplete**, or **Not Counted**.
- **Library:** the full offline Philippine species checklist plus **96** curated illustrated research accounts, **15** habitat profiles, cited species/ecology references and approved offline photographs. Global IUCN and Philippine DENR statuses appear only when independently reviewed and dated. Unreviewed statuses stay neutral.
- **Settings:** backup/recovery, storage, import, integrity checks and Less motion.

The research/news feed and comprehensive ecological atlas remain planned features. Reference associations never create field observations.

## Development and QA

The current scientific build workflow is [`.github/workflows/core-science-qa.yml`](.github/workflows/core-science-qa.yml). It automatically runs on application/source changes and checks source contracts, evidence, database identity, pinned signing identity, photo rights, JVM unit tests, and APK compilation. Documentation-only changes do not trigger builds. Superseded runs are canceled.

Two slower device workflows are **manual acceptance checks**:
[`library-android-smoke.yml`](.github/workflows/library-android-smoke.yml) and
[`rc7-device-emulator-qa.yml`](.github/workflows/rc7-device-emulator-qa.yml). Their source and baseline requirements must be checked before running. An APK build alone does not establish handset compatibility.

Do not add APKs, keystores, local caches, emulator captures or generated photo-review packs to Git. These are covered by `.gitignore`. Signed APKs belong in short-lived CI artifacts and verified, direct conversation downloads.

## Current project records

- [Project state and next-chat handoff](docs/LIKAS_PROJECT_STATE.md)
- [Decision log](docs/LIKAS_DECISION_LOG.md)
- [RC41 source and device QA checklist](docs/LIKAS_RC41_FIELD_READINESS_QA.md)
- [RC40 screenshot defect and UI audit](docs/LIKAS_RC40_SCREENSHOT_AUDIT.md)
- [RC39 full application audit](docs/LIKAS_RC39_APP_AUDIT_2026-10-29.md)
- [Taxonomic source register](docs/PHILIPPINE_TAXONOMY_SOURCES.md)
- [Master product/scientific specification (on `main`)](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/blob/main/docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md)

Historical release decisions, original source records, scientific review ledgers and approved imagery remain in `docs/`, `app/src/main/assets/` and Git history. The obsolete v0.15 installation instructions have been removed from this README, not from version history.
