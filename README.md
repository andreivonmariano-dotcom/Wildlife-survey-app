# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current APO Home development source:** `feature/apo-text-home-v133` (draft PR #75, branched from the latest APO startup; not merged into `main`)  
**Latest verified signed APK:** `0.27.3-rc73-apo-text-home`, versionCode **133**, source `ac75b6abe1af2b5e3e5c758a1bdec86f075fc9c1`  
**Android application ID:** `org.phwildlife.survey.beta08` (unchanged)  
**SQLite schema:** **28** (data model unchanged; real-device upgrade not yet validated)

**APO RC73 signed preview, 2 November 2026:** The text-first Home presents **BioTools**, **PHLibrary**, **PHMaps**, and **BioDiscover** with Settings in the header and a contextual Resume shortcut. The former illustrated LIKAS Home hero and icon tiles are no longer used there. The 220ms reduced-motion-safe Home entrance preserves the owner-approved mountain launcher and gradient splash. PHMaps currently links existing site and habitat records, while a complete atlas remains unimplemented. BioDiscover is explicitly marked as in development because no live research feed is shipped.

**CI and downloadable build:** GitHub Actions run `41373151540` passed **385 Python regressions**, JVM/Android tests and build, original signer/Android ID and exact embedded-artwork and data checks. File `APO-v0.27.3-RC73-text-home-code133.apk` is **11,744,944 bytes**, SHA-256 `c21fbc5ba9d9b4bbc9e455148d680609f21974d2f53523b35ddbbb4a9eb2125d`. The APK was downloaded and delivered directly in the active user chat. See [RC73 signed Home preview QA](docs/APO_RC73_TEXT_HOME_SIGNED_QA_2026_11_02.md).

**Release boundary:** RC73 is a **signed development preview**, not a stable field-certified release. Physical handset upgrade and saved records, GPS/field recording, screen accessibility and large-font navigation require device tests. The prior code132/RC72 startup build (SHA-256 `4a4210ff77aa40cf8c3f7c0741ddad5baeabe17d54ac93dbfaa62810e78cdfeb`) is historical and should not be presented as the current version. Old default `main` is not a suitable Android development baseline.

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
