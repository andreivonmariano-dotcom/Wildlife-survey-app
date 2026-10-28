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

## How to append

Create a dated heading: `YYYY-MM-DD | Decision | Accepted / Replaced / Deferred`. State what changed, why it affects future work, source/reference, and any prior decision superseded. Do not erase historical entries.
### 2026-10-27 | Screen-by-screen UI declutter | Accepted, startup first
The owner requested a deliberate one-screen-at-a-time audit. First refine the startup landing, remove redundant/misleading labels and layout clutter, preserve the exact frog/forest art, keep active-survey resume accessible, and do not change survey records. Then inspect Welcome/Home separately. Startup code: `ui/likas-startup-screen-r1-20261027`. Scope and unverified tests: `docs/QA_v0_27_2_STARTUP_MENU.md`.

### 2026-10-27 | Animated startup and typography | Accepted (supersedes startup icon/version placement)
The owner requests genuine lake/waterfall/cloud movement, daytime birds, night fireflies and living sun/moon light, with smooth phone-time appearance. Remove the frog *only from the startup landing*, not the installed app icon. Show only the LIKAS wordmark, the tagline **Explore. Record. Conserve.**, and a small installed version at the top right. This supersedes the earlier RC3 decision to omit the version label entirely and to place a frog image above the startup wordmark. Keep field decisions accessible and animation battery-aware. Implementation: `feature/v0.27.3-startup-living-forest`, `docs/QA_v0_27_3_LIVING_FOREST_STARTUP.md`.

### 2026-10-28 | Original rainforest scene, retro pixel conversion and moving pixel-frame background | Accepted; app integration pending
The owner preferred the newly generated Philippine rainforest concept over the previous random-looking forest artwork and expressly rejected a static scene with artificial animated Canvas overlays. Use coherent retro pixel-art scenes with independently generated moving frame sequences derived from the scenery. Keep daytime and nighttime loops selected from the phone's local time, allowing gradual dawn/dusk blending. The approved frog stays the installed app icon but not inside the startup backdrop. A local animation pack was generated and delivered as `LIKAS_living_pixel_forest_animation_pack_24fps.zip`. It has **not** been committed as APK media assets or built as a new APK. Do not represent RC4 as already shipping the new pixel art motion.

### 2026-10-28 | Layered pixel-art animation instead of distortion | Accepted; prototype only
The owner explicitly rejected the previous 24 FPS pixel distortion loops for poor visual quality. The replacement direction is independently authored and composited **static scenery + genuinely separate animation sprite parts**, including water body, waterfalls, moving clouds, swaying leaves, wildlife and lighting modes. Local prototype `LIKAS_layered_rainforest_prototype_A.zip` contains 48-frame sprite sequences, separate transparent PNG assets and an offline layer inspector. The work is **not shipped or installed as an APK**, and its quality remains subject to owner review. Fix residual baked-in birds/clouds and refine waterfall body-frame art before Android integration.

### 2026-10-28 | Follow-on Q1 Q2 Q3: refine waterfall, river and movable foliage/clouds | Accepted; Prototype B delivered
Owner requests independent waterfall keyframes, distinct river current and foam animation, and individually movable canopy and cloud layers. Prototype B was created as local visual assets and source code. It includes 16 unique programmed waterfall flow frames per fall, 32 river reflection/foam keyframes, three cloud sprites and three canopy sprites per day/night mode. Source water contours were reused, but warped full-frame movies were not used. The user has not yet approved Prototype B's quality, the loop seams and remaining baked-in far scenery need improvements, and **no Android integration or signed APK** has occurred. Deliverable recorded: `LIKAS_Rainforest_Layered_Prototype_B.zip`, SHA-256 `0c14d4bcfa37099c552e283ac96cfc2f1c9604427de46bea33ffdca0689d8794`.

### 2026-10-28 | Independent 5 FPS sprite assets (Prototype C) | Owner-directed; visual review pending
Owner selected Q1/Q2/Q3 and requested 5 FPS per independently animated rainforest portion. Build each waterfall, the river, clouds, near and far canopy, birds and fireflies as distinct transparent frame assets, preserving fixed terrain and rock occlusion. The resulting local Prototype C pack contains 28 component instances and 760 PNG keyframes plus sprite sheets, manifest, rebuild script and time-aware layer inspector. Pack checksum: `43a024066310dcf96c000062a0d13d762c653379598a7a98cd1f8846297f7c48`. This is **not an APK release** and has not passed owner's art review; do not represent it as finished or shipped. Physical-device QA and Android integration remain pending.

### 2026-10-28 | Core scientific development split from startup animation | Accepted
The owner is developing startup animation in a separate chat and requested that this conversation focus on other LIKAS work. A new RC4-based branch `feature/likas-core-fieldwork-post-rc4` records work on field survey workflows, taxonomy/species information, habitat/ecology, analysis/export, research updates and non-startup usability. Do not edit forest/sprite/background/startup/icon code or art in this branch. The separate sprite branch remains independent. Preserve versionCode 49 and SQLite 27 until a new feature release is deliberately approved. Scope brief: `docs/LIKAS_CORE_FIELDWORK_WORKSTREAM.md`.

### 2026-10-28 | Scientific core evidence integrity and field responsiveness | Experimental source fixes; QA pending
The owner selected audit of survey lifecycle, species taxonomy/evidence quality and whole-app non-startup architecture. On the isolated RC4-derived core branch, positive island/province matches now require explicit `source_listed` status, former names are searchable in species reference and regional lists, Davao Occidental is included in the five-province Davao area filter, and an unset local scope is not displayed as species absence. Regional list truncation and failures are disclosed. The read-only scientific audit was moved off the UI thread without changing scientific records. New source-grounded SQLite tests, a core-only guarded CI workflow and `docs/QA_LIKAS_RC4_SCIENTIFIC_CORE_AUDIT.md` document evidence and risks. No schema migration, current RC4 APK replacement, physical handset validation or startup animation edit has occurred. Do not merge draft PR #38 before final CI and device acceptance.
