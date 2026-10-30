# LIKAS RC58 | Library and Discover screen visual audit
**Date:** 31 October 2026, Asia/Manila. **Status:** Source implementation; Android 14 emulator QA pending.

## Baseline and scope
- The owner requested moving directly to **Library and beyond**, rather than restarting Fieldwork. Audit the Library hub plus the Species and Habitats Discover screens first. Next pass covers full species accounts and individual habitat profiles.
- Branch `feature/likas-rc58-library-discover-visual` based on RC57 draft PR #54, code117, tested `13261f6f49a1a68c86b2fbf76f02a531c2256f4e` and documentation head `9b41bd368db016e7210848cd971eebb905422afd`. Integrated RC49 core unchanged, unmerged draft chain preserved.
- **Actual before evidence:** Android 14 visual navigation run **41038210535**, artifact **12968694224**, PNGs `05-library-hub.png`, `06-species-through-library.png`, `07-habitat-through-library.png` at 320dp/200% font. Inspected screenshot: Library hub theme is already consistent; Discover search hint is clipped and the shelf headings **Conservation** and **Terrestrial** wrap beside the counter and two 48dp navigation buttons.
- Reference integrity: 96 illustrated species accounts, 15 habitat profiles, source-linked library, explicit `Status unverified` for statuses not reviewed and dated. Do not conflate country checklist presence with local occurrence.

## Scoped layout implementation
- In `LikasDiscoveryActivity.shelf()`, stack the shelf title above a right-aligned counter+Previous/Next row only when fontScale >=1.60. For normal and 130% fonts retain compact one-row layout. The existing 48dp touch controls, card count/scroll logic, saved offsets and lazy hydration remain unchanged.
- Shorten the visible search hint to `Search` only at enlarged text sizes while retaining the full search accessibility description and existing filtering, 180ms debounce and saved search state.
- The Library hub itself requires no source change; preserve approved art, existing seven destinations and the unified `UiKit` theme. No new content or fabricated research modules.
- New preview `0.27.3-rc58-library-discover-preview`, versionCode **118**, original `org.phwildlife.survey.beta08`, signing identity and SQLite schema **27**. Requirements G/L/H/A/U/D/C. No change to the source reference pack, conservation codes, records, survey lifecycle, GPS or exports.

## Android and source acceptance
- New `Rc58LibraryVisualInstrumentedTest` produces actual Android screenshots of Library hub, Species top and scrolled, Habitats top and scrolled at **360dp/100%, 412dp/130%, 320dp/200%**, 15 PNG files. Checks active destinations, full search accessibility description and a one-line enlarged Conservation/Terrestrial header.
- New emulator script rechecks the actual scroll and executes existing RC36 library swipe and RC51 research ZIP instrumentation. CI checks full Python suite, JVM/Android compile and original pinned signer.
- Physical sunlight, TalkBack, in-place update with archived observations, backup restoration, data-integrity gates and complete individual detail-page audit remain outstanding.
