# LIKAS RC38 | Illustrated three-space UI and biodiversity card refinement

**29 October 2026, Asia/Manila.** Workstream: `feature/likas-core-fieldwork-post-rc4`. **Preview:** `0.27.3-rc38-illustrated-ui-preview`, versionCode 98.

## Scope and design rationale

The owner requested a less spreadsheet-like LIKAS experience, three clear destinations and illustrated wildlife/habitat discovery cards. RC33 already implemented **Fieldwork, Library, Settings** after the quick title-only startup. RC34 to RC37 added card discovery, neutral unknown-status handling and card navigation. This pass builds on those decisions rather than replacing their science model.

- **Three-space home:** large illustrated portal tiles use contrasting field-canopy, soft foliage and water palettes, with original line-contour decoration, large icon plates and short captions. Short startup and frog art are unchanged.
- **Fieldwork:** Bird, Herpetofauna, Mammal and Custom start actions form a compact, illustrated two-column grid. At enlarged text they stack vertically. History and Analysis have separate illustrated quick-action tiles. Existing active survey resume, setup validation, end-state controls, saved records, GPS, station and archive functions stay unchanged.
- **Library:** Species and Habitats become dominant illustrated entry panels, with smaller two-column reference tiles for Similar species, Photos, Conservation and Sources. Habitat links remain. At enlarged text the reference actions stack.
- **Settings:** Backup, Storage, Import and Data check now use an illustrated two-column grid that stacks at 160%+ font scale; Less motion and About remain separate.
- **Discovery cards:** native rounded gradient surfaces and a restrained severity-color accent make IUCN/DENR-reviewed CR/EN/VU cards visually distinct. Habitat cards gain a soft paper treatment. No game-like rarity classification or points are created.
- **Science:** six independently checked dated IUCN/DENR display records remain the only reviewed status examples among the 96 research accounts. The remaining 90 have neutral styling, without guessed locality or status. Status is not rarity. No new wildlife pictures, claims, laws or library taxonomy are added.
- **Runtime behavior:** native touch controls, local artwork and existing licensed offline photos. No network resources, synthetic GIS data or edits to survey SQLite are introduced.

## QA gates

- Run all Python science, UI and database contract tests, JVM tests, Android debug compilation and AndroidTest compile.
- Verify application identity, version 98, retained signer fingerprint and SQLite schema 27.
- Inspect small-screen 320dp layout, Android 14 emulator launch, 200% font and TalkBack; test return/back navigation and independent shelf positions.
- Install over versionCode 96/97 with survey history, verify export, end-state behavior, GNSS and media attachments. The startup-animation workstream remains excluded.
- A passing CI build is a development preview only, not proof of physical field readiness.

**Affected requirements:** G-01, G-02, G-03, G-05, G-07, G-10, G-11, L-02, L-04, U-07, U-09, U-10, U-11, DL-01, CC-01.

## Verified development build, 29 October 2026

Source fix HEAD `890690329d784156a4c554b8dd014713df5f1558`. The Scientific Core QA job **40868130998** completed successfully, producing artifact **12894089473**. CI currently retains an obsolete RC36 artifact label; the binary was decoded and verified as **RC38**, versionCode **98**, package `org.phwildlife.survey.beta08`, original v2 signer SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Extracted APK **9,663,970 bytes**, SHA-256 `151ac50869b758d9b44c3a149ac7de7ab4987651340b9de3a38025607bd7a6f7`. Direct chat download of `LIKAS-v0.27.3-RC38-illustrated-ui-debug.apk` provided after verification. The baseline Android emulator, real handset, 200% font, TalkBack, file migration and GPS/audio use are still unverified.
