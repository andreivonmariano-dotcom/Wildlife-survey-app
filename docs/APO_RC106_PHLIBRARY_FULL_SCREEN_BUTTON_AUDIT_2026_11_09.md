# RC106 PHLibrary: complete screen and button consistency audit

**Date:** 9 November 2026 (Asia/Manila). **Scope:** owner-requested source/UI audit of the signed RC105 baseline and RC106 implementation. **Branch:** `feature/apo-rc106-phlibrary-ux-polish-v166`, draft PR #111, based directly on signed RC105 code165. **Version:** code166, `0.27.3-rc106-phlibrary-polish`, signing/app ID/SQLite29 unchanged. **Status at writing:** code staged, CI pending. This file does not assert phone QA or a signed RC106 deliverable.

## Review basis and integrity boundary

Reviewed ten RC105 Android 14 screenshots (Home, Explorer, Reader, Habitat Index and Habitat Detail at 360dp/100% and 320dp/200%), the native Java implementations for all PHLibrary screens and dialogs, Android instrumentation and release-source tests, and the exact signed RC105 APK data. No new taxonomic conclusions were drawn. Never turn Book 01 habitat-word matches into field occupancy or habitat selection. Keep the **715 canonical v1.82 narratives**, **1,159 original bibliography entries**, **15 habitat definitions**, **21 provisional evidence edges for 15 taxa across six habitats**, and Discovery Rarity v0.3.4 unchanged; no database migrations or rewrite of survey taxon IDs.

## Screen and button inventory, findings and RC106 action

### PHLibrary Home

- **Back:** returns to the preceding portal, not to fieldwork. Keep a readable large-text target.
- **Rotating owner photo hero:** four Mindanao photograph frames, ten-minute change; unchanged, decorative and offline.
- **Search species + Search button:** opens Explorer with query; keep both typed-search and IME Search behavior. Large-font search already stacks vertically.
- **Species Atlas, Habitat Atlas, References, Saved species:** four meaningful direct destinations, all retained.
- **Removed:** large `Find a species` and `Explore` section headings; the **Wildlife groups** secondary tile grid. It duplicated Explorer group chips and added scroll without new function. Discovery Rarity remains a filter/profile badge, not a dead Home tile.

### Species Atlas / Explorer

- **Back:** returns to prior page with list scroll state retained by Android; source saves search, filters, sort and list position on configuration changes.
- **Live search:** scientific/common name, order/family, and original narrative; results with article matches are explicitly called textual mentions, not site occurrences.
- **Group chips:** Birds, Amphibians, Reptiles, Mammals and All. Long strip horizontally scrolls without artificial `Show more` pagination.
- **Filters:** Family and Genus have searchable choices and Any reset, plus habitat-text terms, editorial rarity, dated-source IUCN code and saved only. Selection returns to the same virtualized ListView. `About habitat mentions` remains in an optional information dialog because the distinction is scientifically essential.
- **Sort:** source, common name A–Z, scientific A–Z; original source order is preserved unless explicitly changed.
- **Clear:** visible only when any search/group/filter/sort is active. The duplicate `Clear all filters` row inside the Filters dialog was deleted. Clear restores group, query, family, genus, habitat, rarity, IUCN, sort and saved state together.
- **Result cards:** common/scientific name, family/group and separately colored rarity/IUCN labels. Badge widths favor the longer IUCN label at enlarged font. Results count is shortened; empty states say `No matching accounts` or `No saved matches`. The duplicated large `Species atlas` heading was removed.
- **Known boundary:** the habitat filter is still a literal article-prose matcher, not a reviewed habitat selection model. Detailed filter combinations are pending native usability verification.

### Species Profile / Book 01 reader

- **Reader header:** retains common and italic scientific names, order/family and a source-specific contextual label; no duplicate Book 01 kicker.
- **Discovery Rarity and IUCN:** independently colored text + symbol badges; tapping either opens an educational/provenance explanation, not a computed risk score.
- **Save / Saved:** a short, readable control at 200% font, with an explicit accessibility description and live state change; device-local bookmarks never alter surveys.
- **A− / A+:** change only the local narrative reading size, save the user's preference, preserve one verbatim selectable v1.82 narrative; fixed 200%-font control padding.
- **Citations (N):** compact action after article opens the account short-reference list. Removed duplicate `Source notes` heading and `REFERENCES ... Open` wording.
- **Same family · [family]:** navigates directly to other family members. Removed the redundant explanatory paragraph because the heading is explicitly taxonomic.
- **Previous / Next species:** preserved, with Back now returning to the previously viewed internal species instead of exiting. The current species/history/vertical reading position is restored on activity recreation.
- **Source boundary:** neither family membership nor educational rarity proves habitat or encounter frequency. `Unranked` still explains provisional identity.

### Account-citation dialogs and the full Bibliography

- **Citations list:** retains original source short labels. Opening a label shows the supplied short/full text, never an invented citation.
- **Find in bibliography:** previously passed entire `Lastname & Coauthor, YYYY` as an exact literal search, frequently yielding zero results despite source records being present. The new action opens a distinct citation lookup, tries exact text first, then only **possible** records with matching first author token/year or an exact undated organization string. It always labels the result **Not verified**; this is search assistance, not a vetted citation crosswalk.
- **Bibliography Home:** Back, Search references, count and virtualized rows; redundant `Research sources`, `BOOK 01 / ORIGINAL BIBLIOGRAPHY` and long introductory paragraph removed.
- **Reference row:** capped preview of original text; tapping opens the complete, selectable, source-preserved record. Where a public HTTPS link is supplied, `Open source` invokes the external handler. An unavailable browser is handled without data mutation.
- **Outstanding scientific backlog:** genuine short-to-full author–year citation links are unresolved until reviewed. Do not label candidates as verified or invent DOIs.

### Habitat Atlas index and profile

- **Atlas index:** Back, grouped 15 landscapes, tap a landscape card. Cards without a literature link now have **no filler subtitle**. Source-linked cards report actual source-link count.
- **Landscape Back:** returns to the Atlas index at its previous scroll position, including configuration change restoration.
- **Profile:** original environment, field indicators, substrate, vegetation, field measurements and interpretive limits remain available in short, expandable headings; long uppercase heading labels removed.
- **Literature-linked species:** original 21 provisional edge records remain, with scientific name to read a species account, quoted evidence, original source title, and `Open source` URL.
- **About these links:** moves the important evidence/site-occupancy limitation to an optional dialog, instead of repeating a long paragraph on every page.
- **Search account text:** explicitly named search for literal habitat terms in Book 01 accounts. It is separate from the reviewed link set, and its accessibility description discloses that the matches are not verified local occurrences.
- **Missing association:** `No reviewed species links added yet` describes database link coverage, **not** biological absence.

## Testing contract for this branch

1. Python scientific-source and UI regression tests must pass without editing canonical data.
2. Android 14 native app must compile with original package/signing, 715 accounts, 1,159 references, untouched habitat source assets and SQLite29.
3. Native screenshot tests cover Home, continuous Explorer, Reader, Habitat index/detail, and the **new bibliography and citation-candidate screens**, at both 100% and 200% system fonts.
4. Native interactions test conditional Clear, source search, 200%-font controls, reader Next/Back history, and precise citation-candidate disclaimers.
5. Signed RC92 code152 → RC106 code166 no-clear-data upgrade must pass, preserving test records/media/export.
6. Physical owner-phone, TalkBack, low-end-memory photo crossfades, all filter combinations and genuine bibliographic reconciliation remain separate acceptance work.

No test failure should be repaired by deleting protective scientific assertions. Adapt obsolete visual-string expectations only where the owner explicitly requested removing the old wording or the same function has a new concise label.
