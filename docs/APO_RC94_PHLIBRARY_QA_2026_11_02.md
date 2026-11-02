# APO / PHLibrary RC94 | Screen and data-preservation acceptance

**Recorded:** 2 November 2026, Asia/Manila  
**Branch:** `feature/apo-rc94-phlibrary-overhaul-v154`  
**Preview:** `0.27.3-rc94-phlibrary-preview`, versionCode **154**, package `org.phwildlife.survey.beta08`, SQLite29. Draft only until signed artifact and evidence pass.

## Reason for this screen pass

The PHLibrary hub previously opened only the smaller illustrated species collection from its first Species card; the broader source-linked 714-name Mindanao PAIC checklist was an extra navigation step. The Discovery page showed a generic "Full Philippine species checklist" label, even when the installed catalog was PAIC-limited, and the displayed conservation carousel used a blended maximum severity across IUCN and DENR. Tab switching retained one shared search phrase, which could cause irrelevant empty states.

## Implemented, presentation-only

- Hub opens **Search species checklist** directly into `Rc7SpeciesLibraryActivity` before the selected illustrated wildlife and habitat guides.
- Clearly separate larger Mindanao PAIC source-name index from the selected illustrated accounts. Neither list implies a confirmed survey-site occurrence.
- Keep wildlife and habitat field guides, conservation evidence, comparisons, photos, source checklists and cited habitat associations reachable in coherent sections without duplicate buttons.
- Discovery species/habitat tabs show explicit selected styling and accessible descriptions; each tab saves and restores its own search text across rotation and tab changes.
- Matching sets of at most eight accounts render as vertical cards instead of mandatory sideways rails. Larger browse shelves remain lazy.
- Remove pooled IUCN/DENR card severity and inferred rank. Source-specific status labels remain displayed independently, and the cited-conservation shelf is alphabetically ordered.
- Move direct complete-checklist and habitat-index actions above the illustrated guide shelves.
- No changes to app signer, launcher art, approved offline photos, PAIC714/WorldCover data, sampling protocol, SQLite tables, survey outcomes, GPS, acoustic evidence or research export logic.

## Automated acceptance

`.github/workflows/apo-rc94-phlibrary-v154-qa.yml` runs on the RC94 branch and draft PR. It checks all Python scientific/source regressions, JVM and Android compilation, exact app package/code/name and original certificate, approved art, PAIC714 and WorldCover hashes. Existing Android14 GPS cancel, missing-fix, microphone RIFF, restart, SQLite29 backup/rollback tests run before two new PHLibrary UI tests at **360dp / 160% font** and **320dp / 200% font**. Four genuine screen PNGs and runtime error log are saved as evidence.

A second Android14 run first installs the exact signed **RC92 code152** predecessor artifact `13139140344` (independently extracted SHA-256 `c88f641e73516b7e9916e34ec31a511c06b2f4042ffde0b559471aff91fabb06`). Synthetic incomplete/not-counted/active events, a zero-detection completed point count, outcome audit, original identification text and WAV bytes are seeded; code154 is installed through `adb install -r`, without clearing data. The test asserts SQLite29, original rows and audio SHA, and complete-only research export eligibility.

**Verification status at creation:** Source files and tests committed; CI success, signed 154 binary, screenshots, genuine signed upgrade and real-handset behavior **not independently verified**. Do not call RC93 or RC94 released based on a successful source edit or an earlier RC92 run. Never attach RC92 as a newer app. Field certification additionally requires populated real-phone upgrade, GNSS, microphone, accessibility and media/ZIP review.

## Affected governing requirements

`G-01`, `G-03`, `G-04`, `G-05`, `G-07`, `G-10`, `G-11`, `L` species reference, `T` provenance, `U` accessible UI and `E` export integrity. No amendment to the master scientific requirements is proposed.

## Remaining work

Review signed build/run reports and visual screenshots, correct any test or UI defects, independently retrieve and checksum the final RC94 APK, verify manifest and signer, and provide a direct downloadable file in chat. Do not merge draft into older default main or pretend the unreviewed Android package is field-approved.
