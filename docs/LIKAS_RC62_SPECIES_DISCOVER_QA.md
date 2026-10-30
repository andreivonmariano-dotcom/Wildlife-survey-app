# LIKAS RC62 | Species Discover visual audit and emulator acceptance

**Date:** 31 October 2026, Asia/Manila. **Scope:** Species Discover only. **Status:** Code/test implemented; real Android QA and signed build pending.

## Baseline verified against actual Android
- Source parent RC61 draft PR #58 from RC60 and original core 49; previous RC61 action `41101788907` compiled the APK and passed 354 Python tests but failed evidence collection because screenshot names still had RC60. Naming corrected at `0ebf16fcfeb174e3d090b90674b34ae86cee9476`; emulator rerun in progress when RC62 branched.
- Actual RC58 Android 14 Species PNGs inspected at **360dp/100%, 412dp/130%, 320dp/200%** (artifact `12993620333`). RC58 to RC61 retained the same Species Discover layout. At 320dp/200%, top navigation tabs, search, WILDLIFE kicker, duplicate Species title and offline field-guide subtitle fill much of the first viewport. Offscreen group chips are only partially visible. Some reference images are placeholder glyphs rather than licensed photographs.
- The reviewed status entries are separate from unknown statuses. IUCN and DENR must remain distinct; unsupported status cannot be inferred. No species names, statuses, references, licenses, actual photographs or country/province ranges may be invented.

## Scoped improvement
- At fontScale >=1.6 for **Species only**, remove redundant small WILDLIFE kicker and the generic offline guide subtitle; keep the Species heading at a still-prominent native 23sp, tabs, search, filtered count and reviewed-conservation shelf. At normal/130% font, preserve the original intro. Habitat Discover remains untouched.
- Add Android horizontal fading edge and accessible swipe description to **Species filter group rail only**, without shrinking touch targets, changing group selection semantics or adding unrelated controls.
- Give the glyph shown in image placeholders a modest **ILLUSTRATION** label behind the asynchronous licensed photo. When a licensed photograph arrives, the image covers the placeholder; licensing/attribution remains in the detail page. This does not declare a photo absent or create a species occurrence.
- Keep card reference, title/scientific name, statuses, slideshow navigation, all filter groups and search, including failed-query neutral "No matches" and the full checklist link. No method/SQLite/API changes.
- VersionCode **122**, `0.27.3-rc62-species-discover-preview`, existing package/signing/schema27 preserved.

## Android gates
- Capture actual Species top/scrolled/filtered screenshot at 360dp/100%, 412dp/130% and 320dp/200% (nine screenshots). Assert no wrapping of Conservation heading, accessible offscreen filter rail, illustrative placeholder label, text hierarchy and search hint behavior.
- Search exact scientific name of Philippine Eagle, then invalid token giving "No matches", and clear it. Do not treat no-result as regional absence. Rerun earlier main Library (RC60) and exact research ZIP instrumentation, all Python/Gradle tests and pinned signed artifact.
- Use screenshot evidence and explicit source revision when accepting. Historical on-device database upgrade, TalkBack reading order, physical outdoor readability, actual keyboard-overlay handling and field workflows remain outside this emulator screen pass.
