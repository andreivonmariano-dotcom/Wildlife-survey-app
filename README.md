# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current Android development branch:** `feature/apo-biotools-workflows-v136` (draft PR #78 into RC75, not merged into historical default `main`)  
**Latest verified signed APK:** `0.27.3-rc76-apo-biotools-screen-refinement`, versionCode **136**, built source `80ca725f3fca516678f973d10be18da9e48c1df1`  
**Android ID:** `org.phwildlife.survey.beta08` (unchanged); signer certificate unchanged  
**SQLite schema:** **28** (unchanged)

**APO RC76 signed BioTools workflow preview, 2 November 2026:** Builds on the four-module APO Home and five-part BioTools menu. Survey setup now clarifies method, project/region, sampling effort and GPS-preflight stages. Survey History places outcome/search controls in the same scroll as its records and honors hardware Back from observation detail. Study Templates presents field labels, grouped scientific requirements and compact editable entries without changing validation or historical templates. Research ZIP export now shows local archive scope, all-outcome privacy information and clearer status messaging while calling the existing archive writer. Data Quality Review places controls and findings in one scrolling page for small screens and enlarged text.

**Signed preview and verification:** CI run `41378920723` passed **403 Python scientific/source tests**, JVM/Android compilation and AndroidTest APK, original signing certificate, package/version and embedded artwork/reference hashes. Direct-download APK `APO-v0.27.3-RC76-BioTools-Screen-Refinement-code136.apk` has **11,752,828 bytes**, SHA-256 `b2ba708d064ade8a7afd4062aeaf76d268d39d7c094989d7929ea7d760679ba2`. Exact file was downloaded from the CI artifact, checked for full archive integrity and rehashed in the user chat. See [RC76 BioTools workflow signed QA](docs/APO_RC76_BIOTOOLS_WORKFLOWS_SIGNED_QA_2026_11_02.md).

**Release boundary:** RC76 is a signed **development preview**, not a validated stable field release. In-place upgrades with real archived surveys, long GPS field recordings, research ZIP contents, large-text/TalkBack, study-template editing on-device, and backup/restore remain unverified. Older RC75/RC74 builds are historical, not the latest development baseline.

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
