# APO PHLibrary | Current-state audit and major redesign proposal

**Audit date:** 8 November 2026, Asia/Manila  
**Scope:** Read-only code and repository audit plus inspection of the owner-locked Word manuscript; no Android source edits, APK installation, or owner handset inspection.  
**Current development HEAD examined:** `feature/apo-rc100-phlibrary-integrated-habitats-v160`, `7d4421993fd1af496237946fdfafb47fb94f458d`, draft open PR #105, based on RC99 code159 (itself from RC98/RC97/RC96/RC95 lineage).  
**Latest source QA checked:** GitHub Actions run **42468208722** on exact head SHA, **success**, with build, original-signature verification, emulator reader/UI tests, and synthetic signed RC92→RC100 no-clear-data upgrade completed. Signed APK artifact **13612064927**, screenshots **13611903674**, upgrade evidence **13613000488** exist as workflow artifacts. **The artifact bytes and screenshots were not downloaded/visually rechecked in this audit, and the owner's physical handset was not tested.** PR remains draft/unmerged.
**Owner-locked reference:** `docs/PHLIBRARY_CANONICAL_BOOK_V182_REFERENCE_LOCK_2026_11_08.md`, Book v1.82, SHA-256 `a35f0a53e0586a071c1bd9ff807150643313982503693cd06ea8905f7aeaa279`; companion rarity v0.3.4. The Word/XLSX bytes are retained in the user's persistent Library, not yet in app assets.

## Findings

### Critical: content and taxon concepts are outdated

**Observed in code:** `app/src/main/java/org/phwildlife/survey/PhLibraryBook01.java` still loads 8 `phlibrary_book01_v175_XX.json` files with 714 records, checks `rarityModel == 0.3.1`, then applies overlays `v176`, `v178`, `v177`, `v179` and `v180`. `ready()` checks for 714; reader footer says `Text v1.80 · Rarity v0.3.1`. The directory header and featured cover both hardcode **714 species**. Source JSON inspection confirms the pre-split `Ceyx melanurus` and `Aphantophryne nana` accounts but not `Ceyx mindanensis` or `Oreophryne cf. nana`.

**Approved target:** Book v1.82 has **715 entries** (409 birds, 53 amphibian entries including 1 provisional `cf.`, 154 reptiles, 99 mammals). Rarity v0.3.4: 124/336/214/37/3 plus 1 Unranked. `Ceyx melanurus` and `C. mindanensis` are separately treated. `Oreophryne cf. nana` remains Unranked. The editorial text now covers all 715 accounts, while the RC100 branch describes 100 individually reviewed habitat cues on an older 714-entry dataset.

**Implication:** Current RC100 cannot be used as the canonical PHLibrary reader content, regardless of UI test pass. Historical record IDs and aliases must survive conversion; a one-to-many dwarf-kingfisher split cannot be silently applied to old observations.

### High: complex runtime text-patch chain creates unnecessary fragility

Five exact-string overlay procedures manipulate complete article text during the app's first library load. The loader sets `initialized = true` **before** reading/verifying its assets, so a single unexpected exact-text patch or damaged source pack leaves `ENTRIES` empty for the process lifetime. The displayed fallback is generic and cannot retry without restarting the process. The one-shot loader and filter execute synchronously when the directory first calls `ready()`; no measured delay is available.

**Recommendation:** Replace the runtime overlay stack with one deterministic, prebuilt `phlibrary_book01_v182.json` plus separate versioned alias/provenance data, produced reproducibly from the locked documents. Keep the historical overlays and v1.75 source in the repository for audit, but make the app's packaged reader input atomic, validated, recoverable and cacheable.

### High: discovery search is shallow

`PhLibraryDirectoryActivity.filter()` searches only the `s` (scientific), `c` (common), `o` (order), and `f` (family) text fields; filters support one wildlife group and one rarity tier only. No historical names, island/geographic evidence, habitat, ecology, status, origin, source, research keyword or article full-text query is included. Rarity choices do not include **Unranked**. The directory uses a `ScrollView`, `LinearLayout` and manual **24-record “Show more”** pagination, with filtering rebuilding views and returning scroll to the top. Font resizing and search state are partially covered, but scroll/read position, query suggestions and sorted results are not integrated with the dataset.

**Recommendation:** A single search-and-discovery surface supporting names/aliases, taxonomic group, habitat descriptors, island evidence, educational rarity, independently sourced IUCN/DENR labels and source keywords. Rank exact accepted names first, then aliases/common names, then broader matched habitat keywords. Use a virtualized list, preserve scroll/filter state, and present 715 as a live catalog value.

### High: habitat module is separate from the expanded encyclopedia

`LikasLibraryActivity` opens `HabitatSpeciesEvidenceActivity` as a third row. That view uses **15** habitat profiles from `offline_habitat_profiles_v1.json` and an older edge list from `rc8_species_habitat_links_v1.json`, displayed via a spinner and standalone source cards. Its “Open species” sends a legacy `sourceTaxonKey` to `TaxonDetailActivity`, which conditionally routes to Book 01 when it can match a current scientific name. The older evidence graph is not automatically synchronized with the new v1.82 long habitat accounts or revised name concepts. No code-level guarantee was found that every old evidence edge opens the corresponding new book account.

**Recommendation:** Build a versioned habitat facet index linked to the 715 canonical taxa. Organize browsing around forest type/structure, wetland and river settings, coastal/intertidal and marine habitats, geology and substrate, habitat disturbance, and named regional context **only where traceable in the account**. Let users browse a habitat and open its linked book species, or navigate from a book account back to the relevant habitat definitions. Keep true field habitat observations separate from educational reference associations. Retain existing evidence IDs as a historical layer, with a transparent crosswalk.

### High: references are present but not consistently resolvable

`PhLibraryBook01.render()` shows one uninterrupted narrative with author-date citations, then a small combined short-reference label. A tap opens a list and a dialog for a single reference. When the source's `full` field is blank, the dialog repeats the short author-date string, without a DOI/link. RC100's QA notes **2,572 of 2,765 citation entries** currently only have short citations in the bundled reader data. This does **not** mean that every underlying publication is unavailable or that the original book has no reference; it means the app's per-citation metadata mapping is incomplete.

**Recommendation:** Build a shared bibliography index from the canonical manuscript, source lists, existing institutional records and verified DOI/URL metadata. Keep unresolved citations without inventing bibliographic details. Give each citation its own text-only tappable target and a concise reference sheet with available title/year/journal/DOI or URL. Retain in-text author–date source text and original source scope. Prioritize frequently reused references and those attached to new habitat or status content.

### Medium: reading is clean, but lacks reader-level navigation and customization

The current UI intentionally remains **text-only, icon-free and photo-free** and displays the exact narrative as one `TextView`, supported by plain status labels and a reference footer. This is consistent with the owner's latest visual preference and is worth retaining. It offers previous/next species and scroll-to-top on each newly selected species. It does not expose a dedicated “find in this article,” reading position/bookmark, source-copy/share option, or contextual text-only related-species links. The page is nested within `TaxonDetailActivity`, which continues to implement a second older species-profile/search experience for unmatched records. This creates potential back-stack and interaction inconsistency.

**Recommendation:** Create a dedicated, lightweight PHLibrary reader with one long body paragraph, stable Back and clear identity, a concise metadata strip, separate source actions, reading-position restore, a simple within-article Find, and optional reader text-size control. Put related species or documented taxonomic cross-references at the bottom, not as a duplicate home menu or unsupported “similar species” diagnosis. Keep the legacy scientific data views accessible through explicit “Source record” routes rather than blending their controls into the reader.

### Medium: no user-personal reading tools

No bookmark, recently viewed list, saved research citation, notes or later-reading feature was found in the audited PHLibrary classes. This is **not** a field recording deficiency. A local read-only personal reading layer could help field researchers use the library offline without modifying canonical taxon records. Such state requires its own storage and migration strategy; it must never overwrite survey observations or source metadata.

### QA and release posture

**Verified:** On exact RC100 head `7d442199`, Actions run **42468208722** finished successfully. The job logs summary reports completed source regression, Android compilation, signed APK check, Android 14 emulator, and synthetic RC92→RC100 upgrade. A signed preview artifact was published. Existing UI/tests cover enlarged text and previous/next navigation. The repository's older RC100 QA Markdown still says “pending CI,” so it is stale relative to the latest Actions result.

**Not verified here:** APK binary SHA/certificate independently downloaded by this chat, screenshot appearance of current RC100, actual phone upgrade, daylight or TalkBack usability, source-level quality of all 715 v1.82 ecological assumptions, or a v1.82-imported Android build. Draft PR #105 has not merged. Do not call v1.82 “installed” or cite the successful v1.80 tests as proof of a v1.82 implementation.

## Proposed major redesign

**Design thesis:** PHLibrary becomes one authoritative offline **wildlife encyclopedia** with integrated **species discovery**, **habitat exploration**, and **source lookup**, while preserving the user's approved restrained, text-only editorial style. Avoid turning its home screen into a multi-tool dashboard.

### Phase 0 | Canonical data migration (must precede interface overhaul)

Generate exactly **715** offline records from the locked Book v1.82 and rarity companion v0.3.4. Keep the complete source paragraph, accepted identity, group, order, family, national/global status, Discovery Rarity tier, island-evidence metadata, citations and historical name aliases as separate typed fields. Add a sidecar review file for habitat tags and evidence references, not silently inferred “verified occurrences.” Introduce stable source/taxon-concept keys independent of visible name spelling. Package one immutable content manifest with version, counts, file hashes and reader compatibility. Keep legacy RC100 inputs untouched as rollback snapshots.

**Release gate:** Exact 715 rows, 409/53/154/99 grouping, 124/336/214/37/3/1 tier distribution, exact narrative fidelity, 0 empty headings/body, 0 duplicate current keys, test both `Ceyx` species and `Oreophryne cf. nana`, all prior checklist and survey observations preserved after signed upgrade. Do not treat book inclusion as a new GPS observation.

### Phase 1 | Single PHLibrary discovery flow

Use one prominent “Wildlife of Greater Mindanao” entrance and a **single global search field** integrated with group, rarity and habitat access. Preserve the quiet non-photographic style. Collapse home-page actions that merely reopen the same book with a filter; present discovery suggestions and filter chips inside the catalog. Keep habitat exploration accessible as a distinct evidence-led view and not a second catalog with a different taxonomy.

**Acceptance:** Search name, valid alias, family, habitat phrase, island evidence and status; show active filters, clear all, robust empty states; restore query/scroll after returning from species. Dynamic counts always reflect the loaded edition.

### Phase 2 | Research-grade reading with low visual clutter

Maintain **one flowing paragraph** and small, separate citations at the bottom. Add in-page Find, text-size control, reliable previous/next/back, optional reading-position restore, and an unobtrusive “References” line where each source opens the best available citation. A small “Why this rarity?” sheet can explain the *educational basis* in natural prose without repeating methodological defenses in every profile. Keep IUCN global, DENR national and Discovery Rarity distinct; show Unranked clearly without creating a sixth rarity tier.

**Acceptance:** 200% font, TalkBack, keyboard focus, 320dp widths, orientation, long articles, source dialogs and same-species URL/access state are tested on Android emulator and device.

### Phase 3 | Habitat explorer as a real ecological index

Introduce reviewed tags and source-linked definitions for Philippine lowland/montane/mossy/dipterocarp forests, riparian/marsh/karst/cave systems, mangroves/coastal and marine habitats, open/modified lands and applicable microhabitats. Each facet opens species accounts and cites the original evidence where possible. Species pages link back to applicable habitats. Do **not** derive habitat occupancy, island presence, or forest type from a guessed elevation or generic family behavior.

**Acceptance:** 715 species can be browsed, every facet linked by stable key; empty/uncertain habitat codes are distinguishable from surveyed absence; source citations and name-split cases remain correct.

### Phase 4 | Personal research utility and sustainable architecture

Optional offline bookmarks, recent reading, shareable full species citation, and personal notes should live in a user-owned layer separate from immutable reference content and survey events. Migrate the old catalog only through explicit, tested crosswalks. Add content load retry, better error reporting and cancellation, and data-quality checks that run before publishing an APK. Keep the full text-only UI free of redundant action cards.

**Acceptance:** Cold startup/first opening performance measured, no hangs on missing/corrupt content, failure recovery without restart, verified survey/notes preservation in signed upgrade, no source mutation, keyboard/TalkBack/large-type acceptance.

## Recommendation and decision status

**Recommend Phase 0 first**; it fixes the principal data contradiction and removes overlay fragility before visual implementation. Then implement Phases 1–3 as a cohesive PHLibrary redesign and release candidate, retaining original source provenance. Phase 4 may follow in a subsequent iteration.

The **Book v1.82 reference lock is accepted**. All other redesign ideas in this document are **proposed for owner review and have not been coded, adopted, merged, built into an APK or tested on a device**. A future implementation should branch from the latest verified RC100/RC99 lineage, not the historical default-main Android code.
