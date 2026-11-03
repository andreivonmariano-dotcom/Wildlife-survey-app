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

### 2026-10-28 | Owner-approved title-only LIKAS startup and cited offline library | APK preview delivered
The owner replaced previous mandatory animated forest splash with a minimal LIKAS wordmark on green, automatic brief accessible transition to Home, and preserved Home Resume action. Master U-03 v1.0.2 applies. Core branch PR #38 adds nine referenced habitat accounts and enriched source-checked narratives for eight wildlife species. Six photo metadata items have CC credits; five JPEG previews were built into successful APK CI 40664803299. Tarsier thumbnail unavailable from Commons 429. The direct 1,779,812-byte APK was delivered in this chat with verified RC4 signing. The old forest startup PR gate fails pending revision, so do not merge or label stable. Next chats should use core-branch docs and reverify any download link.

### 2026-10-28 | Screen-shot-based UX and source-checked species expansion | Preview delivered
The owner selected Q1/Q2/Q3 after providing phone screenshots. RC4 scientific core adds concise species dropdown/filter/search, ten detailed offline accounts including the Philippine Crocodile and Philippine Cockatoo, habitat guide/site tabs with group filtering and one-open-card reading, readable credited photos and a 230ms Home header transition following the minimal splash. Seven true CC JPEGs packaged. CI 40669068454 passed on code 97a05898... and direct 1.90MB APK was delivered with verified RC4 signer. Old forest-startup gate still fails so PR #38 stays draft. See screenshot review documentation on core branch.


### 2026-11-01 | Source-qualified Greater Mindanao PAIC distribution | Research dataset decision
Owner directed proceeding with a seven-core-island distribution layer for the 725-species reference. Maintain separate national/endemicity status, dated source-reported island mentions, infraspecific island names, historical mammal type localities and primary-verified contemporary occurrence. Camiguin and other adjacent islands remain individually tagged rather than silently assigned to the seven-island PAIC core. Release status is **research-only**. The 725-taxon workbook and audit statistics are recorded in `docs/LIKAS_PROJECT_STATE.md`; no app code, APK, SQLite schema or release changed.

### 2026-11-01 | Structured 725-taxon standardization v3.2 | Research data completed
Owner requested standardizing all checklist dimensions. Delivered an eight-sheet, 50-field canonical layer over the same 725 taxon IDs, with source-preserving archival evidence in a separate ZIP. Use separate controlled labels for PH presence, establishment, endemicity, seasonality, international IUCN risk, national DENR legal/source category, coarse habitat guide, ASL elevation guide, and seven-island PAIC occurrence evidence. Explicitly distinguish unknown/unrecorded status from IUCN NE, unknown legal listing from unlisted, unverified regional/infraspecific/type-locality records from species-level confirmed island occurrence. Record 199 targeted review issues for 168 taxa. No independent ecological/legal/source validation or Android release; see `docs/LIKAS_PROJECT_STATE.md`.

### 2026-11-01 | Mammal island-level distribution refinement v3.3 | Research artifact completed
Owner requested geographic specificity instead of the 102 mammal records displaying only "Philippines." Preserve MDD v2.5 country lists as original data and supplement with source-reported island evidence and named collection localities. The 725-species v3.3 workbook has 79 mammal taxa with named island evidence, 18 with type-locality-only named islands, and 5 country-level/unresolved, with citations and review flags. Current islands are distinct from Pleistocene faunal regions and historical type-locality records. Old genus names are cross-referenced, not treated as additional species. Research workbook delivered with direct file download; app import, external validation and APK remain pending. See `docs/LIKAS_PROJECT_STATE.md`.

### 2026-11-01 | Canonical standardized 725-taxon master v3.4 | Research data delivered
Owner requested merging v3.3 mammal island-distribution reviews into the single LIKAS master. The v3.4 workbook now provides one concise, filterable 725-species checklist and a 59-column canonical research master built from 50 unchanged v3.2 source fields plus nine shared geography/evidence columns. All 102 mammal source-limited summaries are integrated; 623 other taxa retain source-qualified island evidence, not independently upgraded ranges. Mammal original evidence, original seven-island matrix, field dictionary and all 237 previous issue entries remain traceable in 13 sheets. Source-reported island locations and historical type localities are separate. App integration and APK shipment were not requested or performed. Artifact identity and verification recorded in `docs/LIKAS_PROJECT_STATE.md`.

### 2026-11-01 | LIKAS distribution completeness v3.5 | Accepted research update
Owner requested a final standardized 725-species master with populated distribution displays. Created v3.5 containing source-qualified geographic text for all 725 records, retaining unresolved ranges as explicit evidence gaps. The 283 formerly blank named-island display fields have explanatory values. No changes to original conservation and habitat data or Android application. See docs/LIKAS_PROJECT_STATE.md for artifact details and QA.

### 2026-11-01 | Clean nine-column checklist v3.6 | Approved data presentation
Owner requested a final concise spreadsheet with order, family, species/common names, distribution status, IUCN and DENR, habitat, and elevational range only, without internal scientific-review phrases in displayed data. Created a one-sheet, 725-species alphabetical-by-order/family/name workbook with a controlled family-to-order crosswalk, simple distribution names, plain habitat labels, and metre-formatted elevations. Missing IUCN/DENR values remain dashes rather than fabricated listings. Research-source details remain in v3.5, which continues as the evidence master; v3.6 is a public-facing display derivative, not a new verified occurrence study or Android app update. Artifact and QA recorded in project state.

### 2026-11-01 | RC54 screen-by-screen survey chooser | Source change, unshipped
Owner chose to improve LIKAS one screen at a time, starting with survey option/method selection. Branch RC54 and draft PR #68 extend the approved RC53 Fieldwork visual system with two-line native radio choices, per-method descriptions, readable selected styling, and enlarged touch targets. Methods, data, GPS preflight, project and survey workflows remain unchanged. VersionCode 114 is source-only until verified signed APK delivery. 725-species v3.6 data is not imported in this screen-specific change. Refer to `docs/LIKAS_PROJECT_STATE.md` and `docs/LIKAS_RC54_SURVEY_METHOD_CHOOSER_QA.md` on the RC54 branch.


### 2026-11-03 | Minimum 100 substantiated species expansions per PHLibrary research pass | Owner-approved working rule
Owner explicitly required that the current and subsequent encyclopedia expansion passes each **examine at least 100 distinct species and expand their accounts**. The first pass applying this threshold produced Book 01 Greater Mindanao Research Edition v1.7 with 169 distinct accounts and 264 source-attributed, site-qualified additions from seven papers or institutional field reports. The 714-species scope and source-linked evidence log are preserved; unchanged placeholders and scientific uncertainties remain visible. This is a research-manuscript rule, not a release of the LIKAS app. See project-state entry dated 2026-11-03.


### 2026-11-04 | Reader-oriented encyclopedia prose | Accepted editorial direction
The owner requested that PHLibrary Book 01 read like a natural-history encyclopedia rather than a defensive research audit. Source context, taxonomic distinctions and real uncertainty remain necessary, but repetitive generic warnings should be consolidated in a short reading note or editorial appendix. Keep flowing, species-specific prose with accurate dates, localities, morphology and citations. Omit wholly empty sections instead of displaying repeated `No data` placeholders. The v1.14 Reader’s edit is an editorial derivative, not a new research pass or Android release. See the 2026-11-04 project-state entry.
