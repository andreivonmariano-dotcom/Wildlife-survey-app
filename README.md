# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current Home development source:** `feature/apo-text-home-v133` (child of the APO RC72 startup branch; not merged into `main`)
**Last verified signed APK before RC73 build:** `0.27.3-rc72-apo-gradient-startup`, versionCode **132**  
**Latest verified signed APK:** `0.27.3-rc72-apo-gradient-startup`, versionCode **132** (built from `4ce953bbea100c1c6a90231434591ffb6f99f788`)  
**Latest checked APO branch HEAD before this documentation update:** `2fc989cae924fc8c652d7b5c3bb6d578dc07d721`  
**Android application ID:** `org.phwildlife.survey.beta08` (unchanged)  
**SQLite schema:** **28** (historical field records retained by design; real-device upgrade not yet validated)

**APO signed preview, 2 November 2026:** The owner-approved textless mountain-ribbon launcher icon and a separate transparent logo on a static forest-to-dark-green splash gradient are in the signed code132 APK. The startup has gentle one-time entrance/exit animations and a reduced-motion path. The 714-species Mindanao PAIC checklist and a sourced ESA WorldCover 2020 reference are installed automatically for offline access. GitHub Actions run `41332798258` passed 380 Python regressions, JVM/Android compilation, signing/identity checks, and packaged reference/artwork SHA checks. APK SHA-256: `4a4210ff77aa40cf8c3f7c0741ddad5baeabe17d54ac93dbfaa62810e78cdfeb`.

**Release boundary:** RC72 is a **signed development preview**, not a validated stable field release. Physical-phone upgrades with archived surveys, GPS and field workflows, accessibility and on-device menu layout remain unverified. Four later commits on this branch only updated APO startup documentation. They do not constitute a newer APK. Older `main` is not a suitable Android development baseline. Do not merge draft ancestry or reset the working branch without reviewing SQLite28 migration and original-signature tests.

**RC73 Home implementation:** Four text-only top-level modules are coded: BioTools, PHLibrary, PHMaps and BioDiscover, with a small Settings header action. The legacy illustrated Home hero/tiles are no longer called; active survey Resume remains. PHMaps exposes existing field-site and habitat records while independent atlas features are not complete; BioDiscover is clearly identified as under development, not as a live news feed. Source is staged for signed build/QA, not yet physically field-validated.

See [APO code132 signed APK and release QA](docs/APO_V132_SIGNED_APK_RELEASE_QA_2026_11_02.md), [approved official emblem](docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md), [project state](docs/LIKAS_PROJECT_STATE.md), and [decision log](docs/LIKAS_DECISION_LOG.md).

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
