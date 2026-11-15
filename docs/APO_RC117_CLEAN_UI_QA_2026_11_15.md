# APO RC117 clean interface and motion QA | 15 November 2026

## Disposition
**Verified signed owner preview. Not approved for unrestricted public release or field certification.**

- Development source: draft [PR #126](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/pull/126), branch `refactor/apo-rc117-clean-ui-motion-20261115`, exact tested **code HEAD `92dbcad6b9f11402e87b5eba7f012af8495170cc`**.
- Exact-head green gate: [GitHub Actions run 43704750113](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/43704750113), job 132783321226, **SUCCESS**. Full pipeline completed with no failed step.
- Android version: `0.27.4-rc117-clean-ui-motion`, `versionCode 177`; application ID `org.phwildlife.survey.beta08`, SQLite29 unchanged.
- Original signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, confirmed by APK signer and independently checked CI signer restoration.
- Actual signed artifact: GitHub Actions artifact ID **14158206861**; extracted final `app-debug.apk` **142,455,572 bytes**; SHA-256 `9ff4a1f6c35db4e454eee26d3e5789af04587e0688bf9ce7e82e07c428e2e69c`. Downloaded to the owner conversation under `/mnt/data/APO-v0.27.4-RC117-code177-clean-ui-motion-verified.apk`; exact path is conversation-local and must be reverified in a later chat. Independent APK ZIP CRC test passed (523 ZIP entries). CI reported the same APK SHA-256.

## Functional changes verified in source
- Home, Fieldwork and My Data use restrained destination cards without decorative item numbering, duplicate section labels or lengthy instructional subtitles. Atlas retains one primary species-browse route and canonical account access.
- Live recorder retains a visible Save Observation, Review and End Survey dock; Complete, Incomplete and Not Counted outcomes and method-specific criteria remain unchanged. Empty unsaved-draft messages are hidden, method focus is no longer repeated, and opportunistic sessions avoid a redundant second timer.
- The Research Library consistently names its landing page and corrects singular reference count; Home's daily wildlife teaser uses `TODAY IN ATLAS` and excludes a visibly truncated account excerpt. Full source article remains accessible inside the species reader.
- New shared native Activity open/close animations use a short **155 ms** fade and subtle lateral movement, with system animator and `likas_ui.reduced_motion` checks. Existing app launch animation remains distinct. No database, reference taxonomy, image, photo, survey, GPS provenance, or conservation status was edited.

## Green acceptance evidence
- **566 Python regression tests** passed, including source/survey and the RC117 copy/motion contracts.
- Gradle JVM tests, debug app and AndroidTest builds passed. CI verified exact package ID, code177, versionName, original signing certificate, pinned official artwork, PAIC714 and WorldCover hashes, locked media assets and APK ZIP integrity.
- Android 14 native shell, map, Atlas, bibliography, gallery/media and inherited recorder acceptance tests passed with **360×800 standard** and **320×720 at 200% text size** captures. The latest checked native screenshots include the new Research Library title, correct saved-reference grammar and clean Home teaser.
- **Signed in-place RC109 code169 → RC117 code177 upgrade passed** with `adb install -r` and no app-data clear. Test retained the synthetic active, Complete and Not Counted survey outcomes, an observation, point count, WAV bytes, audit record and scientific research exports.
- Direct owner APK and native screenshot pack were downloaded in the same conversation. Rendering screenshots and archive integrity were independently inspected.

## Explicit limits
- Draft PRs #122–#126 are not merged into `main`. Main remains RC113 code173.
- No physical handset or TalkBack acceptance was performed. Emulator tests cannot establish real field GNSS, microphone, camera, battery or all-method reliability.
- Public release still requires image/source rights clearance and comprehensive line-by-line scientific copy review; the app still lacks accepted PAIC map boundaries, full national terrain coverage, licensed offline full-text papers and fully reviewed ecological relationships. Accurate scientific uncertainty, missing full text and GPS provenance are **not** to be concealed merely to simplify copy.
- Shared 155 ms transitions and reduced-motion paths were inspected and source-tested, but their subjective smoothness has not been measured on multiple physical devices. No governing charter amendment was made.

## Continuation
Preserve RC117 signed code177 as an owner-preview baseline. Review on a real phone, complete charter release blockers and later reconcile the stacked development PRs into the canonical branch with a controlled merged build and no-clear-data upgrade. Never label a subsequent APK as RC117 unless its exact checksum matches this verified artifact.
