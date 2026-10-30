# LIKAS

LIKAS is an offline-first Android biodiversity fieldwork and reference tool for Philippine wildlife surveys.

**Active development branch:** `feature/likas-core-fieldwork-post-rc4`  
**Current development preview:** `0.27.3-rc43-approved-icons-preview` (versionCode **103**)  
**Application ID:** `org.phwildlife.survey.beta08`  
**SQLite schema:** **27**

**Build status (30 October 2026):** RC43 signed development APK from Scientific Core QA run **41003514753**, code commit `449af5fb2cb22a16c5b7333965e4b1cbab7b1789`, passed **287 Python tests**, Android unit tests, APK signing and resource checks. All **18** SHA-256-locked owner-approved green icons are bundled byte-for-byte. APK SHA-256: `50aa01fffca23a9516f01a36d2d7716971e2fb6831930d1bb9d83ca947872a4a`. Physical upgrade, archived-survey retention and recovery checks remain unverified. Follow direct-download policy for the actual signed file.

## What LIKAS currently provides

- **Fieldwork:** birds, amphibians/reptiles and mammals, project and station records, GPS/elevation, observations, effort, survey history and analysis. A survey can end as **Complete**, **Incomplete**, or **Not Counted**.
- **Library:** the full offline Philippine species checklist plus **96** curated illustrated research accounts, **15** habitat profiles, cited species/ecology references and approved offline photographs. Global IUCN and Philippine DENR statuses appear only when independently reviewed and dated. Unreviewed statuses stay neutral.
- **Settings:** backup/recovery, storage, import, integrity checks and Less motion.

The research/news feed and comprehensive ecological atlas remain planned features. Reference associations never create field observations.

## Development and QA

**Merged PR #40:** The Custom Survey creation UI is retired, while existing records and exports remain supported. All 18 approved green pixel icons are integrated in signed RC43. The active branch's successful CI run **41004124109** produced an APK byte-identical to the independently verified release candidate. See `docs/LIKAS_ICON_ASSET_LOCK_2026_10_30.md` and `docs/LIKAS_RC43_APPROVED_ICON_QA.md`; physical-device acceptance remains pending.


The current scientific build workflow is [`.github/workflows/core-science-qa.yml`](.github/workflows/core-science-qa.yml). It automatically runs on application/source changes and checks source contracts, evidence, database identity, pinned signing identity, photo rights, JVM unit tests, and APK compilation. Documentation-only changes do not trigger builds. Superseded runs are canceled.

Two slower device workflows are **manual acceptance checks**:
[`library-android-smoke.yml`](.github/workflows/library-android-smoke.yml) and
[`rc7-device-emulator-qa.yml`](.github/workflows/rc7-device-emulator-qa.yml). Their source and baseline requirements must be checked before running. An APK build alone does not establish handset compatibility.

Do not add APKs, keystores, local caches, emulator captures or generated photo-review packs to Git. These are covered by `.gitignore`. Signed APKs belong in short-lived CI artifacts and verified, direct conversation downloads.

## Current project records

- [Project state and next-chat handoff](docs/LIKAS_PROJECT_STATE.md)
- [Decision log](docs/LIKAS_DECISION_LOG.md)
- [RC43 signed icon-integrated preview QA](docs/LIKAS_RC43_APPROVED_ICON_QA.md)
- [Locked 18-icon owner artwork](docs/LIKAS_ICON_ASSET_LOCK_2026_10_30.md)
- [RC41 source and device QA checklist](docs/LIKAS_RC41_FIELD_READINESS_QA.md)
- [RC40 screenshot defect and UI audit](docs/LIKAS_RC40_SCREENSHOT_AUDIT.md)
- [RC39 full application audit](docs/LIKAS_RC39_APP_AUDIT_2026-10-29.md)
- [Taxonomic source register](docs/PHILIPPINE_TAXONOMY_SOURCES.md)
- [Master product/scientific specification (on `main`)](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/blob/main/docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md)

Historical release decisions, original source records, scientific review ledgers and approved imagery remain in `docs/`, `app/src/main/assets/` and Git history. The obsolete v0.15 installation instructions have been removed from this README, not from version history.
