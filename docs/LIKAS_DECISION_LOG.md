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

## How to append

Create a dated heading: `YYYY-MM-DD | Decision | Accepted / Replaced / Deferred`. State what changed, why it affects future work, source/reference, and any prior decision superseded. Do not erase historical entries.