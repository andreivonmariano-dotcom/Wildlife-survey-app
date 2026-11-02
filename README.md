# APO

APO is an offline-first, Philippines-focused Android biodiversity companion for biologists, with field surveys, species references, habitat context, and research-export tools. The name references Mount Apo and supersedes LIKAS as the public-facing product identity.

**Current Android development branch:** `feature/apo-biotools-all-screens-v137` (draft PR #79 into RC76, not merged to historical `main`)  
**Latest verified signed APK:** `0.27.3-rc77-apo-biotools-all-screens`, versionCode **137**, tested source `0075ead49c4e60cd1a69a6dc29be3520b0a26ddd`  
**Android ID:** `org.phwildlife.survey.beta08` (unchanged); original beta signing certificate retained  
**SQLite schema:** **28** (unchanged)

**APO RC77 BioTools cross-screen polish, 2 November 2026:** Extends RC76 and audits all five BioTools modules plus their nested workflows. From Field Records, site habitats now opens *recorded field evidence*, not the reference guide; the guide remains a distinct tab. Acoustic history/review includes clearer offline/survey state and reflowing controls. Trap/camera and field-protocol archives now have text-first survey rows, honest empty states, preserved active-survey recording gates, accessible Back navigation and fewer fixed-height controls. Long project, study-template, trap and audio annotation dialogs scroll. Existing descriptive analysis, export, quality review and four-stage survey setup remain intact.

**Verified RC77 signed preview:** `APO-v0.27.3-RC77-All-BioTools-code137.apk`, **11,754,140 bytes**, SHA-256 `99b84a1dcebe8ffdce3c2e35aa26780585de4d03b7980b7b493318fe62a4815d`. GitHub Actions `41380263162` passed **410 Python regressions**, JVM/Android compilation and AndroidTest APK, exact package/signer verification and locked art/PAIC714/WorldCover hashes. The CI artifact was copied to a direct in-chat APK and independently ZIP/asset/SHA checked. See [RC77 all BioTools screens signed QA](docs/APO_RC77_ALL_BIOTOOLS_SIGNED_QA_2026_11_02.md).

**Release boundary:** RC77 is a signed **development preview**, not a stable field-certified release. In-place upgrade retention of old field data, GNSS, research ZIP contents, audio capture, study-template changes, 320dp/200%-font screenshots and TalkBack still need on-device verification. Historical RC76/RC75/RC74 builds are superseded, not renamed or relabeled.

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
