# LIKAS: Current Project State and Next-Chat Handoff

**State revision:** 1.10  
**Recorded:** 28 October 2026, Asia/Manila  
**Repository:** `andreivonmariano-dotcom/Wildlife-survey-app` (private)  
**Branch checked:** `main`  
**Observed repository HEAD prior to startup-scenery cleanup:** `f1ef6583647d05ae3cbbb915b2f54370150be33c` on `main`. The cleanup commit follows this SHA. Read current `main` HEAD before further edits.  
**Latest app-feature code milestone observed:** `dc0362d23d0f74b6bca57c66da6e20c34adfd52d` (v0.15); later `c7c5742` and `44825d7` commits added LIKAS forest and brand files.  
**Evidence:** GitHub `README.md`, directory tree, selected source files, previous LIKAS project-chat decisions. No direct Android handset test was performed for this snapshot.

## Critical baseline correction | 28 October 2026, Asia/Manila

**Current app baseline is NOT the default branch's 0.15 beta.** The owner confirmed current LIKAS installation **v0.27.3 RC4**. GitHub branch `feature/v0.27.3-startup-living-forest` has `app/build.gradle` versionName `0.27.3-rc4`, versionCode **49**, app ID `org.phwildlife.survey.beta08`, and **SQLite schema 27**. Its `docs/QA_v0_27_3_LIVING_FOREST_STARTUP.md` records the built RC4 APK, verified signature continuity, and outstanding physical-device tests. PR #35 tracks this branch.

**Newest sprite-player continuation:** `feature/v0.27.4-layered-sprite-player-stage` (draft PR #36), derived from RC4. It contains `LikasSpriteForestView.java` and a 5 FPS separately animated sprite prototype direction. The actual binary sprite assets are not present in that branch's APK, and `app/build.gradle` is still RC4 versionCode 49. Do not claim a new v0.27.4 APK exists.

**Wrong-branch development incident:** An assistant used `main` v0.15.0-beta (versionCode 17, older database) instead of the newer release-candidate branch. It incorrectly built and delivered `0.16.0-parallax-preview` (versionCode 18) from `feature/rainforest-parallax-v2`, recorded in PR #37. That old-baseline preview **is NOT an update** to RC4. It omits changes and migrations present in v0.27.x. **Do not install its APK over RC4, do not uninstall RC4 to permit downgrading, and do not merge PR #37.** PR #37 was closed as obsolete, unmerged, on 28 October 2026. Previous v0.16 claims below are historical and superseded, not a current recommendation.

**Future work must follow the RC4 → 0.27.4 staged sprite-player lineage**, preserve accepted original artwork, screen semantics and 5 FPS individual sprite direction, and check real current branch code and artifacts before release claims. Do not select a build baseline solely from GitHub's default branch.

---

## 28 October 2026 | Q1/Q2/Q3 verified RC4 continuation, separate sprite QA and guarded pull requests

- **App baseline:** LIKAS v0.27.3 RC4 (versionCode 49, SQLite schema 27). Next source stage is draft PR #36, `feature/v0.27.4-layered-sprite-player-stage`, synchronized with updated RC4 via merge `93e705bdc168249eb2384d2ceacec287982c77ad`. Current source history is 0 commits behind RC4; `main` itself is still historical v0.15 code, and obsolete PR #37 is closed.
- **Missing original Prototype D ZIP:** Asset recovery searched GitHub, current accessible Project and Library files, local workdir and owner's connected Drive. Neither the recorded 9,350,494-byte `LIKAS_Layered_Sprites_5FPS_Prototype_D.zip` nor inner 1,488,772-byte `android/likas_sprite_assets.zip` was found. Inner ZIP recorded SHA-256 `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`. Do not fabricate a replacement or claim the source-stage APK already contains it.
- **Q2 per-component test tool:** New `scripts/inspect_likas_sprite_layers.py` checks 28 separately animated day/night components, ZIP/hash, alpha, frame counts, geometry, atlas and frame variation, and produces visual contact strips once original ZIP is available. `scripts/test_likas_sprite_layer_audit.py` uses synthetic test sprites. CI run `40634485653` passed this source-test stage. **No real Prototype D media or Android sprite playback has been tested.**
- **Q3 cross-branch PR checks:** Trusted `scripts/check_pr_against_rc4.py` and `.github/workflows/likas-pr-lineage.yml` added to `main`, RC4 and v0.27.4. RC4 and PR #36 subsequently passed lineage checks (e.g. 40634727804 and 40634775181). The first guard accidentally required the yet-unconnected sprite class in RC4; fixed and retested. The existing v0.27.4 CI still requires proper RC4 ancestry and matching debug signing key.
- **Hard limitation:** GitHub private-repo rulesets GET produced `403 Upgrade to GitHub Pro or make this repository public`. Branch-protection GET produced `403 Resource not accessible by integration`. **No mandatory, non-bypassable repository-wide merge rule has been configured.** Do not equate runnable CI workflows with enforced merge protection.
- **Safety:** Current RC4 startup and scientific survey data are unchanged. A source-build of the unconnected player is NOT a new v0.27.4 animation APK. Exact asset recovery, review, versionCode >=50, Android handset decoder/performance QA, data-preserving upgrade test and direct APK delivery remain pending.
- **Source audit:** `docs/QA_v0_27_4_ACTIVE_BRANCH_SPRITE_AUDIT.md` on v0.27.4 records the full audit and follow-on tests.

## 28 October 2026 | Owner partitions LIKAS development across chats

- **Startup animation:** Developed in a separate owner-directed conversation, associated with the existing RC4-derived `feature/v0.27.4-layered-sprite-player-stage` branch.
- **Core wildlife biology app features:** This conversation uses new isolated branch `feature/likas-core-fieldwork-post-rc4` based on actual `feature/v0.27.3-startup-living-forest` (RC4, code 49, SQLite 27), not historical `main`.
- **Do not edit startup assets or screen in core branch.** Core scope is field surveys, observations, species catalogs, habitat/ecological evidence, scientific reviews/exports, research information, offline reliability and non-startup UX. See core branch `docs/LIKAS_CORE_FIELDWORK_WORKSTREAM.md`.
- **Status:** Scope division and GitHub documentation only. No new application features or APK built; next core module awaits owner selection. Neither workstream should rebase from the older v0.15 default code.

## 28 October 2026 | Scientific core audit and P0 evidence-status source fixes

- **Separate core branch:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38, directly derived from the v0.27.3 RC4 base. Startup animation remains in a separate chat and feature branch, untouched.
- **Source audit:** `docs/QA_LIKAS_RC4_SCIENTIFIC_CORE_AUDIT.md` on PR #38 reviews actual survey lifecycle, GPS records, species taxa, occurrence and ecology evidence, research exports, old database migration, architecture and non-startup responsiveness. No device testing was performed.
- **Fixed scientific issue in draft:** Species picker source-location rankings and regional checklist previously treated imported `unreviewed` occurrence rows as source-listed positive local records. Now require `review_status='source_listed'`; raw unreviewed rows remain available for review, never turned into absence.
- **Taxonomy and local UX:** Species reference and regional name search now match `taxon_synonyms` former names. Davao Region filter adds the fifth PSA province Davao Occidental. Separate Davao City locality is disclosed as not independently assessed by the current province-only schema. Without selected island/province, the local-only picker now explains missing geographic scope instead of implying no species. Regional lists disclose 1,000 cap, read failures and debounce search 180 ms.
- **Scientific data review:** `AuditActivity` no longer runs multi-query read-only `DataAudit.run` synchronously on the UI thread; uses dedicated executor and lifecycle-safe result rendering without write access.
- **Testing and constraints:** New synthetic SQLite tests, source contract tests and `.github/workflows/core-science-qa.yml` compile/test code with signing and RC4 lineage checks, no published APK. CI run `40646116315` succeeded on application source commit `16ac4d01eb4dd441140ea667b93d8581ac9ae2c2`; later test/doc-only commits need freshest CI status checked. App ID, schema 27, code 49, survey records and startup assets unchanged. Real phone upgrade, survey, long-run GNSS, evidence attachment, exports and accessibility remain pending.
- **Architecture priorities:** The 2,413-line `MainActivity`, synchronous Dashboard/History DB reads, GNSS writer queue/failure recovery, data restore and separate taxonomy sources are logged as risks, not measured defects. Priority later: isolate survey session/repositories and independently review source-dated occurrence, ecological and conservation evidence.

## 28 October 2026 | Biodiversity workspace redesign in core branch, source verified

- **Owner-approved direction:** Q1 Home becomes a coordinated biodiversity workspace, Q2 live field recorder should be less crowded, Q3 species reference should foreground identification and source-backed ecology. Startup animation remains owned by another chat, and formal field testing remains deferred.
- **Source:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38, on the genuine RC4 lineage, not historical `main` or obsolete v0.16.
- **Home changed:** Visible fieldwork methods and Resume; Projects & stations, recent survey, History, Dashboard, direct Species Library search; secondary tools under disclosure.
- **Recorder changed:** Method-specific audio/device/evidence controls grouped behind disclosure. Mark now, Measure bearing, station navigation, Save Observation, Review Records and End Survey remain accessible, with unchanged Complete/Incomplete/Not Counted behaviors and no DB schema changes.
- **Species reference changed:** `CatalogActivity` is Species Library with live source-count summaries; `TaxonDetailActivity` separates taxonomy, conservation, habitat and regional references and marks diagnostic keys unavailable where not installed. `SpeciesPickerActivity` replaces uncalibrated `% Likely` cards and visible score sorts/filters with geography/habitat/elevation source descriptions and name/family/genus sorting.
- **Verification:** CI Python/JVM/Android build passed `40652463202` at app code commit `d939960f8a7fc5e90f9ad90b1298209bbc6e1f0f` and RC4 lineage gate passed `40652467141`. Remaining branch updates after that code are only project documentation. No release APK, versionCode change, schema migration, handset screen inspection, new taxonomic traits, or sprite media integration. See `docs/LIKAS_BIODIVERSITY_WORKSPACE_V1.md` on core branch.
- **Merge boundary:** Keep PR #38 draft until owner reviews real screens and confirms compatibility with the separate startup branch. Preserve existing field observations, signing identity and unfinished surveys.


## Mission

Build **LIKAS** as a Philippines-first, all-in-one biodiversity tool for wildlife biologists and ecologists. Five product pillars: (1) Field Survey Studio, (2) Philippine Species Library, (3) Habitat and Ecology Atlas, (4) Research and Conservation Watch, and (5) Analysis and Research Export. Scientific integrity, offline field use, data preservation, and clear interaction outrank decoration.

## Observed coded baseline (not a claim of physical-phone verification)

- Repository `README.md` identifies **Philippine Wildlife Survey Beta v0.15.0**, an offline Android field app for birds, herpetofauna, and mammals, without a map UI in that release.
- Version 0.15 introduced reusable bird point-count route templates, permanent station IDs, distinct visits, geotagging on station arrival, and data-quality review rules. The README describes SQLite schema version **11**, research ZIP exports, and scientific effort accounting that excludes travel from station sampling time.
- Existing modules/files include survey activity, history, taxa catalog, research export, scientific data audit, site management, camera/photo evidence, a GPS track service, point-count progression, and Android APK CI.
- The source catalog uses a small unreviewed starter list. The official Mammal Diversity Database v2.5 Philippine import is documented in the README. The Sy amphibian and reptile datasets are **not bundled** according to that README.
- Older brand commits added a retro forest backdrop class and day/night WebP art, but `WelcomeActivity` did not instantiate the view. On 28 October, those three startup-specific files and their dedicated instructions were removed. The adaptive launcher icon and shared LIKAS color resources remain.

## Confirmed owner preferences and decisions

- **Survey ending:** An End Survey action must remain accessible. Completed means protocol conditions were met. Incomplete retains real partial work. Cancel / Not Counted archives the event outside standard survey analysis.
- **Location:** Elevation should follow an acceptable recent GPS fix, with source, age, uncertainty, and reference tracked rather than filling in unknown values. Implementation status must be checked in code and on device.
- **Reference lists:** The owner previously supplied an amphibian checklist (Emerson Sy 2025-1), reptile checklist (Emerson Sy 2026-1), and WBCP bird checklist (2025). These must be verified as available in a current chat/Project source before claiming they are imported or bundled. The Mammal Diversity Database import uses a separate versioned source.
- **Visual direction:** Simplify busy screens, remove duplicate controls and text artifacts, and keep a minimalist field interface. Preferred launcher icon remains a pixel frog with its upper face emerging from water and no lily pad. The previous animated tropical forest direction is superseded by the 28 October decision to clear the old startup imagery and start fresh. A new four-lighting-state rainforest prototype exists for review, but no native replacement scene has been approved.
- **File handoff:** Every APK, ZIP, image, document, and other deliverable requires a verified direct in-chat file link; never replace that link with a GitHub page. See DL-01.
- **Persistent continuity:** The owner wants to start a new LIKAS chat without restating prior decisions when an earlier chat reaches its limit. See CC-01.

## Active requirements and likely next tasks

1. Read the user request, recent repository HEAD, this state file, decision log, master specification, and relevant code before implementing changes.
2. Audit current app workflows for duplicated controls, stale explanations, uneven design, and unfinished survey lifecycle paths.
3. Retain the current working static welcome screen. Develop a new startup-scene concept only after the scenery reset and separate visual approval. Review the frog launcher icon independently.
4. Verify and finish the requested checklist coverage for birds, amphibians, and reptiles. Review source provenance and redistribution rights, and avoid inventing island or province occurrences.
5. Check GPS-based elevation behavior with device-quality metadata and confirm accessibility of End Survey/Complete/Incomplete/Not Counted actions.
6. Carry out physical-phone verification, migration checks, data audit, and scientific export review before reporting a tested release.
7. On a new build, provide the **actual APK in the current chat as a direct download**, not just a GitHub URL.
8. Plan phased expansion into habitat/ecological information, wildlife research updates, and analysis modules. These are product goals, not currently verified shipped modules.

## 28 October 2026 | Startup-scenery asset reset

- **Requested:** Remove all previous startup-screen scenery assets before designing replacement imagery.
- **Repository edit:** Delete `app/src/main/assets/likas_forest/forest_day.webp`, `forest_night.webp`, `app/src/main/java/org/phwildlife/survey/ForestBackdropView.java`, and `docs/LIKAS_FOREST_BACKGROUND.md`. Amend brand guidance and master U-03. Keep the launcher icon (`likas_launcher_art.webp`, both adaptive-icon XMLs), shared brand colors, and static welcome screen.
- **Verification:** Inspected `WelcomeActivity`, `MainActivity`, `UiKit`, AndroidManifest, and Gradle configuration for references. No startup forest class or plate references were found in those app entry points. This documentation records a source change, not an APK release or physical-device test.
- **Next action:** Design a new startup scene from a blank slate. Do not reuse the retired artwork without a new explicit decision.

## 28 October 2026 | New rainforest and welcome-screen review prototype

- **Owner direction:** Explore all three together: a freshly designed Philippine rainforest scene, layered ambient motion using the phone's local time, and a cleaner LIKAS welcome experience. This does not approve the precise final artwork or authorize replacing the current release before review.
- **Created in the current conversation:** A downloadable browser-based interactive prototype, new morning/day/dusk/night concept plates derived from a common scene, daytime/night screenshots, and a 30 FPS accelerated movement study. The browser prototype was checked at 390 and 320 pixel widths, survey chooser shows Birds/Herpetofauna/Mammals/Custom, nighttime plate opacity reaches 1, reduced-motion mode was exercised, and no JavaScript page exceptions were seen.
- **Scope:** The generated artwork and ZIP exist as direct attachments in the originating chat, **not** in GitHub or the Android APK. The plate dimensions are 1224 x 1668 but come from an upscaled 612 x 834 crop. Native-detail art, compiled Android integration, device battery/performance tests, and a downloadable rebuilt APK are pending.
- **Durable brief:** `docs/LIKAS_STARTUP_V2_CONCEPT.md` records the candidate art direction, UI hierarchy, proposed local-time crossfades, performance requirements, and acceptance gate.
- **Next action:** Review the visual prototype with the owner, then implement a separate Android test branch with approved higher-native-resolution scenery. Preserve the live survey navigation and historical records. Test before release.

## 28 October 2026 | Native rainforest preview build and direct APK delivery

- **Latest explicit request:** Refine the native rainforest artwork toward the approved concept and complete a safe, downloadable Android test build (owner's Q1 and Q3).
- **Source branch:** `feature/rainforest-parallax-v2`, draft PR #37: https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/pull/37 . **Main app code remains v0.15.0-beta.** The parallax source was not merged and the new preview is not a production release.
- **Code implemented:** `RainforestBackdropView.java`, `RainforestMotion.java`, JUnit tests, welcome-screen hero integration, richer fixed canopy texture, ravine cliff detailing, waterfall spray, and ferns. Canvas has independently moving planes and a local-time morning/day/dusk/night palette.
- **Android CI verification:** Feature-branch code commit `23e1c8f5bb48fe790caabebec0931493531a8d25` passed GitHub Actions run `40630133535` for Java tests and debug APK build. The branch-only CI safely reuses the existing signing key and skips publication to the stable GitHub release.
- **Downloaded APK delivered in originating chat:** `LIKAS-v0.16.0-parallax-preview-debug.apk`, 280,733 bytes, SHA256 `9de10ad747a2b2b0454ecc295b7bf6d9d9bc3faee69921a0cd0f01e474b32653`. Parsed APK manifest confirms package `org.phwildlife.survey.beta08`, versionCode 18, versionName `0.16.0-parallax-preview`. APK v2 signing certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` **matches** v0.15 APK from main CI run `40627444544`. Download links from an earlier chat are not reusable without verifying the file in that session.
- **Not yet field verified:** On-device upgrade/data retention, actual Android frame-rate, battery and temperature, phone screenshot/visual parity, TalkBack, survey navigation and long-term endurance. The achieved 60 FPS value must not be claimed without handset measurement.
- **Next:** Use preview APK for real-device install/regression and FPS checks, obtain explicit final visual approval, and merge PR #37 only after acceptance. See branch file `docs/LIKAS_PARALLAX_ANDROID_QA.md`.

## Current blockers and nonclaims

- No APK was compiled or physically tested as part of the 28 October startup-scenery cleanup. CI build/test outcome must be checked separately.
- No complete migration audit, full UI audit, or taxonomic source-license review was performed in this continuity task.
- The three spreadsheets referenced in prior chats and earlier approved ZIP assets are not confirmed to be mounted in every future chat. A prior chat's sandbox path is not a durable download link.
- Project memory settings cannot be changed by a repository commit. The owner must set Project-only memory in the ChatGPT Project settings when supported.
- The recorded pre-cleanup HEAD above is a historical observation. Read the current HEAD at each new session.

## First task for a replacement chat

If the user says **"Continue LIKAS from the previous chat"**, read the canonical files named in `AGENTS.md`, check the latest code, and continue the latest explicit request. Provide a two- or three-sentence orientation only if helpful, then proceed to the task. Do not require the user to resupply decisions already recorded.

## State maintenance

Update after any important decision, code change, bug discovery, user-approved asset, release, or unresolved task. Identify what was **requested**, **implemented**, **tested**, and **released**. Update timestamps and source SHA whenever this snapshot changes. Append permanent product decisions to `docs/LIKAS_DECISION_LOG.md`.