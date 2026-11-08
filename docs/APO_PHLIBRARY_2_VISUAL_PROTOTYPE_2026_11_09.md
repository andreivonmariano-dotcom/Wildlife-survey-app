# PHLibrary 2.0 | Three-screen interactive design concept

**Date:** 9 November 2026 (Asia/Manila)  
**Status:** Owner-directed visual **prototype delivered for review**, not an accepted final design or Android implementation. Based on the latest signed RC102 reference lineage. No production app code, sources, survey field records, APK, package identity, or signing changed in this design task.

## Objective and visual treatment

The existing PHLibrary is reliable and readable but visually repetitive: a text-first home with one cover and three text action rows; a 24-at-a-time name directory; a single undifferentiated article view; a separate 15-habitat spinner. The goal is a more engaging, recognizable natural-history atlas while keeping full scientific content, offline access and field-reading practicality.

**Concept 01:** An editorial-museum aesthetic with dark forest illustrated home cover, parchment/cream reading surfaces, classic serif headings, compact system-sans search controls, moss/ochre accent colors, independent Discovery Rarity and global IUCN status, and linked species navigation. No unverified animal imagery was inserted. Original SVG forest geometry is decorative, not a wildlife/species identification illustration.

## Three connected screens

1. **Home**: immersive original vector Greater Mindanao forest landscape, prominent *Wildlife of Greater Mindanao* hero, counts of 715 entries/four groups, one main search affordance, group browsing, and thematic habitat text search. The full future source-verified habitat atlas is outside this first prototype.
2. **Species Explorer**: live search over all 715 genuine Book 01 v1.82 entries (scientific/common name, family/order and source paragraph); exact-name ranking ahead of broader text hits; group chips; filter chooser for educational rarity and IUCN global status; source-order/common-name/scientific-name sorting; incremental, continuous-scroll record list. Searching article text is **not proof of regional presence/habitat occupancy**.
3. **Species Profile**: editorial dark forest masthead, taxonomic identity, clearly **separate** Discovery Rarity and IUCN labels, verbatim **single-paragraph canonical account**, text-size controls, original short in-account citations with explicit unverified full-reference resolution, same-family cross-links, session-only bookmarks and mobile back navigation.

## Data and fidelity

- Source: Canonical RC101 v1.82 JSON extracted from the approved Word manuscript and reused in RC102 app source, SHA-256 `a87021a615a93f64b56cb3c8974e57deaf69bdd7f0ca7478b70068bad533e885`.
- Entire **715-account payload embedded offline** within a self-contained HTML file; birds 409, amphibian entries 53, reptiles 154, mammals 99. Rare-label counts are still 124 Common, 336 Uncommon, 214 Rare, 37 Exceptionally rare, 3 Legendary, 1 Unranked.
- The provisional taxon `Oreophryne cf. nana` displays **Unranked**, `Ceyx melanurus` displays **Rare** and `Ceyx mindanensis` displays **Uncommon**. Historical taxonomic crosswalk and existing field observations are not edited.
- Individual source-paragraph text is displayed using `textContent`, not rewritten, and a Playwright test verified exact article fidelity for `Ceyx melanurus`. Book-level full citation matching is still unresolved and is not simulated as verified.
- Bookmarks exist only in current browser memory. No telemetry, network APIs, permanent storage or file observation mutation.

## Artifact manifest and QA

The original conversation produced these **chat-local** files:
- `PHLibrary_2_0_Interactive_Prototype.html`, **1,201,869 bytes**, SHA-256 `a0a642e2ab9a88a76d061b07f1404758a5cebe53634361885359293d7103b96e`.
- `PHLibrary_2_0_Three_Screens_Preview.png`, **460,386 bytes**, SHA-256 `c87aaac972cb56ae4ae8daa32365a655a2050c63605c0b715b0e7c870c95bee0`.
- `PHLibrary_2_0_Design_Prototype_Package.zip`, **1,227,517 bytes**, SHA-256 `75de1beaa0c266b03e2e026f4a8d764f032ebdbaf812e34ed55bb21691166ead`. Archive CRC verified; contains HTML, desktop/mobile images, README, manifest and test outcome.
- Browser tests used local Chromium + Playwright at desktop and mobile widths, including **320px** no-horizontal-overflow and mobile tab navigation. **715 records** loaded; filter tests returned **3 Legendary** and **7 IUCN CR**; exact taxon and article tests passed, with zero JS console page exceptions.
- These paths are not GitHub-hosted binaries. The reviewer must verify a file is available before reusing a sandbox link in a later conversation.

## Acceptance boundary and next implementation stage

The user approved proceeding with the **visual prototype**, not a specific new wildlife-photo or icon policy, a redesigned APK, or an encyclopedia data migration. Preserve the currently locked art/readability/content/certificate and scientific source policies until the owner reviews the concept. Future Android implementation can use native components (RecyclerView for explorer, accessible reader components, optional verified licensed photographs) but must pass actual phone/large-font/TalkBack/no-data-loss release tests. Do not claim that this HTML is the new installed app or that the illustrated forest is taxonomically accurate.
