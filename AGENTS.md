# LIKAS development instructions

**ACTIVE BRANCH OVERRIDE (28 October 2026):** The GitHub default `main` still contains historical v0.15.0 and must **not** be used as the baseline for LIKAS Android changes. The current user-confirmed app is **v0.27.3 RC4** (versionCode 49, SQLite 27) on `feature/v0.27.3-startup-living-forest`. The next development source is `feature/v0.27.4-layered-sprite-player-stage` (PR #36) and contains a staged 5 FPS sprite player but no integrated approved binary pack. Before any edit/build, examine active-branch state and CI lineage guard `scripts/check_likas_active_lineage.py` on that branch. The mistaken v0.16 preview branch (closed PR #37) is obsolete. Any new installable version must use versionCode at least 50, matching package/signing identity and verified media. Do not treat a default branch, filename or successful historical build as proof of latest release.


**Mandatory continuity (CC-01):** Before each new LIKAS development task, read `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`, `docs/LIKAS_PROJECT_STATE.md`, and `docs/LIKAS_DECISION_LOG.md`, then confirm current source HEAD. After every meaningful change or accepted decision, update the project-state file and append decisions. Never rely solely on previous chats or wait until a chat reaches its limit. Do not ask the owner to repeat already recorded decisions.

**Mandatory governance:** Follow `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` for product and scientific requirements. Follow `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md` for **every downloadable handoff**, including APKs, images and ZIPs. Deliver real attached files with direct chat download links, never GitHub navigation pages as substitutes.

LIKAS | STRICT PROJECT INSTRUCTIONS | Companion to v1.0 master specification

Project vision
Build LIKAS as an all-in-one, Philippines-first biodiversity tool for wildlife biologists and ecologists. The five pillars are Field Survey Studio, Philippine Species Library, Habitat and Ecology Atlas, Research and Conservation Watch, and Research Export/Analysis.

Governing document
Read LIKAS_MASTER_PROJECT_SPEC_v1.0.md before proposing or implementing changes. Treat requirement IDs G, S, L, T, H, R, D, A, U, C, and E as governing rules. Update the specification and its revision history whenever a decision changes one of those requirements. Verify claims against the GitHub repository and latest APK before saying a feature is shipped.

Non-negotiable order
1. Scientific integrity, evidence provenance, accurate methods, and privacy.
2. No data loss or broken historical survey records.
3. Offline and clear field-survey workflows.
4. Most recent explicit product-owner decision.
5. Consistent, minimal, usable UI and approved branding.
6. Future expansion, analytics, and decorative features.

Survey states
An active or paused survey must have an accessible End Survey control. Complete requires method-defined criteria. Incomplete saves real partial work. Cancel / Not Counted archives the event but excludes it from default analysis. Missing is never zero. Zero-detection effort is a valid survey event.

GPS and elevation
Use fresh fixes and preserve source, fix age, accuracy, altitude reference, and measurement time. Never infer an animal's exact location from the observer's GPS or turn planned route lengths into fabricated map geometries. Do not overwrite historic GPS/altitude readings silently.

Taxonomy and conservation
Use reviewed versioned Philippine bird, amphibian, reptile, and mammal sources with citations and licensing checks. Country-level occurrence does not establish provincial or island occurrence. Separate DENR national status from IUCN status. Preserve unresolved identifications.

Research/news
Use identifiable primary sources, publication dates, DOI/URLs, retrieval dates, and source-type labels. Live research and ecosystem modules are roadmap features until implemented and verified.

Engineering workflow
Read README, data dictionary, protocols, QA plan, and current code before changes. List affected requirement IDs. Preserve the app ID and signing continuity. Build, run tests, check migrations, verify real data export, and test on a physical Android phone where needed. State what shipped and what remains unverified. Avoid duplicated controls, placeholder strings, unreadable text, and jerky or low-resolution startup visuals.

Approved design direction
Modern field UI with restrained retro forest branding. Tropical time-adaptive forest startup art should be high-resolution and smooth. Preferred minimalist app icon: a pixel frog's upper face emerging from pond water, with no lily pad. Do not claim current repo assets are final just from filenames.


Direct download delivery | mandatory DL-01
Read and follow LIKAS_DIRECT_DOWNLOAD_POLICY.md for every APK, image, ZIP, PDF, CSV, animation, or other delivered file. Provide a verified direct sandbox file-download link in the chat, never a GitHub repository, release page, CI artifact listing, or external website as the primary download. Put the actual file in /mnt/data and verify it exists before linking. For each new LIKAS APK, link the newly built version, not an earlier APK. If no direct file is accessible, state that the file is not directly downloadable and do not invent a link or substitute an external redirect. GitHub source links are supplemental only.

Canonical master: LIKAS_MASTER_PROJECT_SPEC_v1.0.md, 27 October 2026.

Cross-chat continuity | mandatory CC-01
Read docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md for the complete protocol. Maintain docs/LIKAS_PROJECT_STATE.md and docs/LIKAS_DECISION_LOG.md as the persistent handoff after major decisions, code changes, assets, releases and blockers. A chat-limit event cannot be assumed to trigger an automatic final save. Follow the latest verified repository state and latest explicit owner decision. Project-only memory is supplemental and is controlled in ChatGPT Project settings. Never fabricate details from unseen chats.
