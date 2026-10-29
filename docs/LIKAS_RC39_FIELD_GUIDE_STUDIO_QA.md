# LIKAS RC39 | Field Guide Studio visual and navigation release

**Date:** 29 October 2026 Asia/Manila
**Branch:** `feature/likas-core-fieldwork-post-rc4`
**Version:** `0.27.3-rc39-field-guide-studio-preview`, versionCode **99**
**User direction:** make the design substantially more polished and useful, without losing field or science reliability.

## Scope delivered in source

1. **Searchable offline visual gallery:** Discover Species and Habitats now has a native, large-touch search bar, 180ms redraw coalescing, native-clear action, independent Species/Habitat query grouping, and explicit empty states. Search matches existing documented common/scientific names and group in 96 research accounts or habitat names/groups/overview in 15 approved profiles. This is not a full national checklist search, which remains in the existing `All species` catalog.
2. **One-tap taxon filters:** Wildlife groups (Birds, Amphibians, Reptiles, Mammals) and the five documented habitat groups are selectable as native 48dp chips. Active selections are labeled to accessibility services. Filtered cards are shown in a single Matches rail. Full browsing retains existing research shelves and conservation shelf. Search, tab, and group states survive Android activity recreation.
3. **Original local illustrations:** distinct vector fallback artwork for bird, frog, snake, mammal, forest, karst, wetland, coastal and modified landscapes, used only when an authorized offline image is absent. Wider visual cards, taller photo treatment and a visible Open guide affordance.
4. **Field desk refinement:** active session resume moves immediately below the home hero. It remains a read-only launcher and does not automatically create or end records. In Fieldwork, extra field utilities can be expanded or folded away with a saved app preference; projects, History and Analysis remain visible. Latest recorded survey becomes a readable tappable history card, not a dead text line.
5. **Scientific method setup:** native RadioGroup method choices have stronger active borders and background, plus a concise method description. Changing the method refreshes existing study-template choices. None of the original method codes, field data entry values, criteria, or survey outcomes change.

## Data boundaries

- Keep app ID `org.phwildlife.survey.beta08`, same pinned debug signing config, SQLite **27**.
- Preserve all original science facts (505 merged), 96 curated species research accounts, 15 habitat profiles, six independently verified dated IUCN/DENR status cards, and original 58 authorized/licensed JPEGs.
- Search is offline and read-only. Do not infer rarity, treat unreviewed as LC/DD, or combine national DENR status and global IUCN into a single claim.
- Protect live survey session clock, GPS/elevation provenance, observations, archive outcomes, export, route templates and database migration behavior.
- Startup scene and approved frog launcher identity remain unchanged.

## Acceptance gates

- Run scientific-core Python contracts, JVM policy tests, compile debug APK and Android instrumented test source.
- Run Android 14 emulator smoke for RC35/36/39 startup and search interactions. AndroidTest compilation by itself is not execution.
- Screenshot and inspect Home, Fieldwork, method setup, Species, Habitats and Settings at 320dp and 200% text. Screen reader, reduced motion, landscape, scrolling, swiping, search restoration and empty states require device review.
- In-place upgrade from signed RC38 versionCode98 with existing surveys; verify Complete, Incomplete, Not Counted, ZIP export, photos and raw GPS.
- Verify APK binary package/version and X509 signing certificate identity before making direct-chat link. Never use a GitHub artifact page as download.

## Known restrictions

Only the 96 research accounts have the illustrated gallery. `All species` remains the full separate offline taxon catalog. Photo rights and source links are unchanged. No new rarity reviews, live conservation feeds or media are claimed. Physical device testing is not complete simply from successful CI.

**Affected requirements:** U-04, U-07, U-09, U-10, U-11, G-01, G-03, S-01, S-09, L-02, L-04, H-02, C-01, E-01, E-04, E-05, DL-01, CC-01.
