# LIKAS

LIKAS is an offline-first Android biodiversity fieldwork and reference tool for Philippine wildlife surveys.

**Active development branch:** `feature/likas-core-fieldwork-post-rc4`  
**Current development preview:** `0.27.3-rc47-design-system-preview` (versionCode **107**)  
**Application ID:** `org.phwildlife.survey.beta08`  
**SQLite schema:** **27**

**Build status (30 October 2026):** RC47 source merged in PR #44, code merge `cbe828de29cdd8047fcdfe54e3116ae0a4ebefbd`. Scientific Core QA **41024068239** passed on the active branch, including **305 Python tests**, JVM tests, signed APK and AndroidTest APK compilation. The signed code107 APK has SHA-256 `d18d32b3df3b3fedd9d411e322616c0a484b57fcbb06ac4eef91add8ce84d439`; APK v2 signed-content and original signing-certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` were independently verified. This is a development preview without physical-device/upgrade acceptance.

## What LIKAS currently provides

- **Fieldwork:** birds, amphibians/reptiles and mammals, project and station records, GPS/elevation, observations, effort, survey history and analysis. A survey can end as **Complete**, **Incomplete**, or **Not Counted**.
- **Library:** the full offline Philippine species checklist plus **96** curated illustrated research accounts, **15** habitat profiles, cited species/ecology references and approved offline photographs. Global IUCN and Philippine DENR statuses appear only when independently reviewed and dated. Unreviewed statuses stay neutral.
- **Settings:** backup/recovery, storage, import, integrity checks and Less motion.

The research/news feed and comprehensive ecological atlas remain planned features. Reference associations never create field observations.

## Development and QA

**RC47 design-system foundation:** Shared spacing, font, corner, touch and responsive rules now serve Library and Settings through one page scaffold; Fieldwork uses common section typography and portal cards share accessible interaction styling. Prior RC44 approved art and startup remain unchanged. See [RC47 design system](docs/LIKAS_RC47_DESIGN_SYSTEM_FOUNDATIONS.md) and [locked icon record](docs/LIKAS_ICON_ASSET_LOCK_2026_10_30.md).


The current scientific build workflow is [`.github/workflows/core-science-qa.yml`](.github/workflows/core-science-qa.yml). It automatically runs on application/source changes and checks source contracts, evidence, database identity, pinned signing identity, photo rights, JVM unit tests, and APK compilation. Documentation-only changes do not trigger builds. Superseded runs are canceled.

Two slower device workflows are **manual acceptance checks**:
[`library-android-smoke.yml`](.github/workflows/library-android-smoke.yml) and
[`rc7-device-emulator-qa.yml`](.github/workflows/rc7-device-emulator-qa.yml). Their source and baseline requirements must be checked before running. An APK build alone does not establish handset compatibility.

Do not add APKs, keystores, local caches, emulator captures or generated photo-review packs to Git. These are covered by `.gitignore`. Signed APKs belong in short-lived CI artifacts and verified, direct conversation downloads.

## Current project records

- [Project state and next-chat handoff](docs/LIKAS_PROJECT_STATE.md)
- [Decision log](docs/LIKAS_DECISION_LOG.md)
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
