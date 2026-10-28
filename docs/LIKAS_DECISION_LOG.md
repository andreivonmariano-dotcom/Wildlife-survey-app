# LIKAS: Durable Decision Log

**Purpose:** Append-only record of accepted project-owner decisions and governing rules. This file is not proof that an item has been implemented or tested.

## Entries

### 2026-10-27 | Project vision | Accepted
LIKAS is intended as an all-in-one biodiversity platform for Philippine field biologists, not only a wildlife survey recorder. Include field surveys, species information, ecological/habitat information, research/conservation updates, and analysis/export. See `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`.

### 2026-10-27 | Survey lifecycle | Accepted; implementation needs verification
End Survey must always be accessible during an active survey. A completed survey satisfies its method requirements. Incomplete saves partial effort. Cancel / Not Counted is retained but omitted from default analytical counts. Zero-detection events remain valid evidence of surveyed effort.

### 2026-10-27 | Taxonomic source preferences | Accepted; ingestion needs verification
Use the owner-supplied WBCP birds 2025 and Emerson Sy amphibian 2025-1 / reptile 2026-1 checklists subject to provenance and redistribution review. Mammal Diversity Database v2.5 is documented for Philippine country-level mammal taxonomy. Checklist names do not establish province-level presence.

### 2026-10-27 | Visual design | Accepted; asset and device implementation need verification
Favor a minimalist pixelated frog with only the upper face visible above pond water and no lily pad. Forest startup art should be high-resolution, smooth, subtly animated, and responsive to device-local day/night time. Reduce screen clutter and redundant controls.

### 2026-10-27 | Direct downloads | Mandatory (DL-01)
Every presented APK, ZIP, image, and other deliverable must have a verified direct file-download link in the chat. A GitHub release/page link is not a substitute. See `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md`.

### 2026-10-27 | Cross-chat continuity | Mandatory (CC-01)
Persist the latest project state and accepted decisions in GitHub during each work session. Every new LIKAS development chat reads them first and continues from that state. Project memory supplements the written record. See `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`.

### 2026-10-28 | Remove old startup-scene assets | Accepted; previous scenery direction superseded
Remove the previous forest day/night WebP plates, unused `ForestBackdropView.java`, and the old startup-background guidance. Retain the static welcome screen, Android launcher icon, and shared LIKAS palette. This supersedes the 27 October request to refine or connect the old forest animation. A replacement scene has not been selected. Related requirements: G-07, U-03, U-04, U-06, E-01.

### 2026-10-28 | New LIKAS rainforest welcome design | Direction accepted; exact visual pending review
The owner asked to pursue a new Philippine rainforest scene, smooth local-time-adaptive ambient animation, and a cleaner startup layout together. A fresh visual and browser interaction prototype was produced in the chat. The previous retired art must not be reused. The new concept is **not** an approved final Android asset, merged implementation, completed APK, or measured 60 FPS native animation. Continue under U-03 review requirements and `docs/LIKAS_STARTUP_V2_CONCEPT.md`.

### 2026-10-28 | Rainforest parallax refined and test APK compiled | Preview built, release pending
The project owner requested a closer native match to the detailed forest concept and a real downloadable Android test APK. A protected preview branch and draft PR #37 now contain independent parallax motion and richer procedural forest detailing. GitHub Actions run `40630133535` compiled the app, passed automated tests, and produced versionCode 18; its APK was directly delivered and checked against the prior v0.15 signing certificate. **60 FPS, handset upgrade, record retention, and final visual approval remain unverified.** Main remains v0.15. See `docs/LIKAS_PROJECT_STATE.md` and draft PR #37.

### 2026-10-28 | Baseline correction: v0.27.3 RC4 current, v0.16 preview obsolete | Accepted correction
The owner identified the current working version as LIKAS v0.27.3 RC4, verified in `feature/v0.27.3-startup-living-forest` (`versionCode 49`, SQLite v27, PR #35), with a 5 FPS staged sprite player in `feature/v0.27.4-layered-sprite-player-stage` (draft PR #36). The separately developed `feature/rainforest-parallax-v2` was wrongly based on outdated `main` v0.15 and built as v0.16.0 (versionCode 18). **Do not merge or install that lower-version APK over RC4.** PR #37 is closed, unmerged. The owner-approved high-fidelity sprite composition and latest live branch take priority over any earlier generic 60 FPS Canvas prototype. Refer to the correction at the start of `docs/LIKAS_PROJECT_STATE.md`.

## How to append

Create a dated heading: `YYYY-MM-DD | Decision | Accepted / Replaced / Deferred`. State what changed, why it affects future work, source/reference, and any prior decision superseded. Do not erase historical entries.

### 2026-10-28 | Prototype D recovery, sprite-by-sprite QA, cross-branch PR checks | Pending original media and GitHub enforcement
The owner accepted continuing from v0.27.4 PR #36 and requested original Prototype D asset recovery, per-sprite inspection, and mandatory checks across the repository. Original approved-candidate ZIP is missing from accessible file sources and must not be substituted. A source/asset inspector with synthetic 28-layer tests passed CI. Trusted pull_request_target lineage checks were installed on main, RC4, and staged v0.27.4, with RC4 and PR #36 checks passing after a guard fix. Private GitHub plan/integration restrictions block actual repository-wide enforced rulesets/branch protection; report tests as running, not non-bypassable. Keep startup on RC4 until real pack approval and device testing. See `docs/LIKAS_PROJECT_STATE.md` and v0.27.4 audit.

### 2026-10-28 | Parallel workstream ownership: startup vs scientific core | Accepted
The owner directed all startup animation work to a separate conversation. This conversation must focus on survey/data/scientific features and non-startup UX using isolated RC4-derived `feature/likas-core-fieldwork-post-rc4`. The animation workstream remains separate. Do not modify launcher/startup forest assets in the core branch. Preserve application identity, database continuity, release provenance and explicit user approval of any version change. See core branch workstream brief.

### 2026-10-28 | Scientific core source integrity and field usability | Draft implementation; device gate pending
The owner requested a joint field-survey, species information and architecture audit, explicitly excluding startup animation. Draft PR #38 on the RC4-derived core branch corrects the `unreviewed` geographic evidence ranking/listing defect, adds source-attributed former-name lookup, corrects the Davao Occidental regional omission and no-area empty state, and moves read-only quality checks off the Android UI thread. Detailed scope and remaining risks are in `docs/QA_LIKAS_RC4_SCIENTIFIC_CORE_AUDIT.md` on the core branch. It leaves SQLite schema 27, app ID, versionCode 49, field survey data and startup code unchanged. This is not a new released APK and must pass mobile evidence/data regression and source tests before integration.

### 2026-10-28 | Redesign Home, Live Recorder, Species Library | Source implemented, review pending
The owner accepted Q1/Q2/Q3. A separate RC4-based core branch and draft PR #38 implement a visible Fieldwork/Workspace/Species Library Home structure, a recorder that groups low-frequency method evidence tools without hiding Mark/Save/End, and a cited-evidence-first species library. The species UI removes unsupported percentage/“Likely” prominence and score sorting, preserves raw sources, and states when diagnostic keys are not installed. All implementation source checks passed CI run `40652463202`; no startup assets, species science data, SQLite schema or APK version were changed. Full specification: `docs/LIKAS_BIODIVERSITY_WORKSPACE_V1.md` on core branch. On-device owner design review pending; formal field-test plans deferred.


### 2026-10-28 | Home/Recorder/Library visual consistency, quick evidence and cited comparison | Source complete in draft
The owner selected Q1 Q2 Q3. The RC4-derived core branch now uses shared minimalist section styling, exposes one existing method-specific evidence capture action directly, and offers two offline Philippine raptor comparison profiles with cited source characters. The diagnostic profiles are source-checked, not independently expert-reviewed. No startup animation, survey records, schema, versionCode or APK release changed. CI passed on implementation run 40654448251; on-phone visual review remains pending. The field test plan remains deferred.

### 2026-10-28 | QoL: scientific search dropdown, user data deletion and habitat preview | Source implemented, release gated
Owner approved all Q1 Q2 Q3 with usability priority. RC4-derived draft PR #38 implements bounded offline species name suggestions, typed-confirmation deletion of owner-created surveys, observations and linked evidence (and individual unused project/site/route/study items), and a read-only Habitat & Ecology Home entry. Full user data reset keeps the source catalog and default project; cleanup runs off the UI thread. See core-branch roadmap. No user data was deleted, no new APK released, and full current-version restore/phone verification is still mandatory before shipping.
