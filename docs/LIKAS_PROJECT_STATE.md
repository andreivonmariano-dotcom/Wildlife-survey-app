# LIKAS: Current Project State and Next-Chat Handoff

**State revision:** 1.1  
**Recorded:** 28 October 2026, Asia/Manila  
**Repository:** `andreivonmariano-dotcom/Wildlife-survey-app` (private)  
**Branch checked:** `main`  
**Observed repository HEAD prior to startup-scenery cleanup:** `f1ef6583647d05ae3cbbb915b2f54370150be33c` on `main`. The cleanup commit follows this SHA. Read current `main` HEAD before further edits.  
**Latest app-feature code milestone observed:** `dc0362d23d0f74b6bca57c66da6e20c34adfd52d` (v0.15); later `c7c5742` and `44825d7` commits added LIKAS forest and brand files.  
**Evidence:** GitHub `README.md`, directory tree, selected source files, previous LIKAS project-chat decisions. No direct Android handset test was performed for this snapshot.

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
- **Visual direction:** Simplify busy screens, remove duplicate controls and text artifacts, and keep a minimalist field interface. Preferred launcher icon remains a pixel frog with its upper face emerging from water and no lily pad. The previous animated tropical forest direction is superseded by the 28 October decision to clear the old startup imagery and start fresh. No replacement scene has been selected.
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