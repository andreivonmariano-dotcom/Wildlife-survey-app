# LIKAS RC41 | Field readiness and remaining issue review

**Date:** 29 October 2026, Asia/Manila  
**Candidate:** `0.27.3-rc41-field-readiness-preview` · versionCode **101**  
**Branch:** `feature/likas-core-fieldwork-post-rc4`

## Fixed in source

- **RC39 broken Home:** Retains the RC40 content-sized navigation tile repair, concise Home, Fieldwork, Library and Settings.
- **Gallery performance:** RC41 constructs card slots for each group but only populates cards within two positions of the visible card, with a maximum of five active cards per shelf. Offscreen cards are removed. The position and category state persist after a search or rotation.
- **Offline photos:** Only approved licensed JPG assets are used. Gallery media loading and 4x-sampled RGB565 decode now happen on a two-worker executor; vector art remains visible until the image is ready. Full credit/license material remains on linked species and habitat details. Main-thread thumbnail decoding still exists in some full detail views and requires measurement.
- **Library clarity:** The collectible 96-account species guides link to the **Full Philippine species checklist**, preserving the larger catalog and its taxonomy. This is not a claim that only 96 species occur in the Philippines.
- **Accessibility/navigation:** Real selected state is set on category chips and gallery tabs. The species checklist Back label says Library, and Backup/Storage back labels say Settings.
- **RC40 correctness retained:** Visible numeric units, shared legacy outcome classification, reduced-motion countdown, 48dp gallery arrows, source-cited concise species/habitat prose, no changed survey-outcome controls.

## Identity and evidence boundaries

- Android ID `org.phwildlife.survey.beta08`, signed identity from previously verified RC39, SQLite schema **27**.
- Keep six independently reviewed dated IUCN/DENR assessments, 96 source-curated accounts, 15 offline habitats and 58 approved offline JPEGs.
- The 96-account source JSON still contains 456 original structured facts after the RC40 direct-prose edit. No source URL, scientific identity, legal status or distribution claim has been added.
- No Android database migration, survey completion-rule change, field recording update or replacement of approved startup/frog artwork was made.

## Verification gates

1. All Python source/scientific checks including `test_rc40_screenshot_audit.py` and `test_rc41_gallery_performance.py`, Java unit tests, Android debug app and AndroidTest APK compile.
2. Fetch current GitHub artifact, verify ZIP CRC, APK manifest package `org.phwildlife.survey.beta08`, versionCode **101**, correct versionName and same signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, then direct conversation file.
3. Uncompleted in CI alone: execute device instrumentation `Rc39DiscoverySearchInstrumentedTest` and `Rc40HomeLayoutInstrumentedTest`; inspect actual first-screen Home at 320dp and 200% font; TalkBack and search/swipe photo attribution; install versionCode 101 over 99 with real, backed-up active/complete/incomplete/not-counted surveys.
4. Outdoor field checks: GNSS and GPS/elevation provenance, long survey timer, photo/audio, backup restore and zero-detection surveys.

## Further audit findings

- Gallery now avoids mass synchronous thumbnail decoding, but actual performance improvement needs frame-time and memory measurements on a phone.
- Remaining larger detail pages may decode photos synchronously; avoid broad rewrite without measuring.
- An ordinary recovery ZIP retains exact field locations and other sensitive records, so users must keep it private. Encrypted backup or an explicitly share-safe redacted export should be a separate consented feature, not silently added to a field data workflow.
- `FieldBackupArchive.restoreIntoEmpty` installs the validated database then copies media and preferences; an I/O failure during the media copy could leave a partial restoration. A transaction-safe staged restore and rollback test is recommended as a separate high-priority data-recovery issue before production deployment. No destructive restore change was made in RC41 without a fixture.
- GitHub CI’s historical artifact label may still contain an older RC number. Trust only verified binary metadata.
- No real wildlife research/news feed has been shipped. Keep the all-in-one research module in the roadmap, not a promotional claim.

**Release status at authoring:** compiled signed RC41 APK remains unverified until the exact GitHub QA run has succeeded and binary is inspected.

## Independent release check | 29 October 2026

Checked source commit `0841d5f9441734accfee1a8b3a1131dfd19741af` on isolated `qa/rc41-release-check`. GitHub Scientific Core QA `40888849314` failed at Python tests, before media preparation or signing, with no artifact. Two obsolete UI source assertions expected the prior checklist title and synchronous gallery thumbnail. Updated them to check the RC41 checklist/Library navigation and asynchronous gallery call; the scientific corpus, facts, source licenses and scientific test assertions were retained.

| Check | Result |
| --- | --- |
| `python3 -m unittest discover -s scripts -p 'test_*.py'` | 280 pass, 0 fail |
| Gradle `:app:testDebugUnitTest` | 95 pass, 0 fail |
| Gradle `:app:compileDebugAndroidTestJavaWithJavac` | Pass; Android instrumentation tests compiled but not executed |
| Normal `:app:assembleDebug` | Fails at `validateSigningDebug`: original `.signing/wildlife-beta08.keystore` absent |
| Temporary unsigned `:app:assembleDebug` | Pass; manifest retains package `org.phwildlife.survey.beta08`, versionCode 101 and RC41 versionName |

The unsigned proof APK is not installable as an update to the original signed app. It lacks the CI-prepared approved JPEGs and generated MDD catalog, and was not delivered as a release artifact. No replacement key was created. Source inspection supports the Home/recorder/library/navigation contracts; 320dp/200%-font rendering, gallery timing, device instrumentation, GPS/media, record-preserving upgrade and recovery fault injection still require device or dedicated fixture QA. The existing partial-restore risk remains a release concern; no destructive recovery changes were made without rollback coverage.


## Signed RC41 GitHub acceptance update | 30 October 2026

- **Code and CI:** Active app-source commit `8d2918665eccd8d6a037d616cd58a9a9dde6cf96` (merged PR #39, corrected the two obsolete UI assertions). GitHub Scientific Core QA run **40908133636** completed **success**, with artifact **12912227441** (`LIKAS-0.27.3-rc41-field-readiness-preview-code101-debug`). This audit did not change Android source, scientific records, survey data or signing settings.
- **Binary inspected:** Downloaded the exact CI artifact and extracted `app-debug.apk`. APK SHA-256 **`53be72c0c020884091f35d6dd1e4b8a06733da736083bcc5c365edad3fee9f34`**, **9,670,326 bytes**. ZIP integrity passed. Binary Android manifest confirms package `org.phwildlife.survey.beta08`, versionCode **101**, versionName `0.27.3-rc41-field-readiness-preview`. APK Signature Scheme v2 cryptographic signature **and the full APK signed-content SHA-256 digest** verified, signer certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**, unchanged from RC39. APK includes **58** authorized offline JPGs and `ph_mammals_mdd_v2_5.json`.
- **Delivery:** Verified signed development preview copied into this ChatGPT conversation's `/mnt/data/LIKAS-v0.27.3-RC41-field-readiness-debug.apk`, and a direct file link delivered. This is an RC41 *development preview*, not production field acceptance.
- **Unverified:** physical Android install/upgrade from RC39 with backed-up records, emulator instrumentation execution, 320dp/200%-font/accessible navigation, real GPS/elevation and survey workflows, backup restore and partial-restore rollback, long-session battery/performance and external photo rights/device behaviour. Keep the original SQLite **27**, original signing identity and all survey outcomes. Do not call RC41 production-ready.
- **Next:** independent real-device upgrade and data-preservation tests; staged fault-injected recovery fix; new version only after acceptance and verified APK. The QA artifact retention is five days; archived copies outside this chat need independent verification.

---

