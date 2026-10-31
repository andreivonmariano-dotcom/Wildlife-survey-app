# LIKAS RC65 | Main Library icon optical crop and framing

**Date:** 31 October 2026, Asia/Manila. **Scope:** four Main Library display icons only. **Status:** implementation and Android QA staged; acceptance pending.
**Parent:** RC64 draft PR #61, tested original source SHA ae11ac6d0f71ca82d7b56b551c17bd6f0a7f3bca; latest docs HEAD 06ab681ab9b236dba251ce2c2a3b329a5c7b407b. Earlier scientific core RC49 remains integrated; no PR merged.

## Input and findings
- Owner supplied a physical-phone Library screenshot and still found the four icons visually mismatched after RC64. Existing **Species/Habitats/Similar species** images are the visual scale references, not master assets to modify.
- Audited the actual image bytes extracted from the independently verified RC64 APK. The 18 owner-approved images are 512×512; Photos and Sources are full-bleed 8-color RGB PNGs with a visible frame. Generated Conservation and Habitat links are 64×64 palette PNGs with 4px transparent outer margins. Source hashes are pinned in the build script.
- Selected faithful optical trims after side-by-side study: **4px from each side** of the 64px Conservation/Habitat links art; **24px from each side** of 512px Photos/Sources art. Resulting 256×256 **Library-only** display PNGs preserve source pixel colors and nearest-neighbor pixel shapes. No new object, recoloring, interpolation, licensable image or taxonomy modification.
- Reframing uses the existing 50dp non-cropping FIT_CENTER image plate, corner clipping and accessible card navigation. Original 18 approved icons and 2 generated icons are not replaced, and their exact bytes stay unchanged. Photos/Reports usages elsewhere still point at original source PNGs.

## Reproducible build
- `scripts/prepare_rc65_library_icons.py` uses standard Python only, parses/validates PNG CRCs, reconstructs original PNG row filters, crops and resamples exactly, checks pinned SHA256 source hashes, then creates four PNGs in `app/build/generated/rc65/res/drawable-nodpi`. Gradle task `prepareRc65LibraryIcons` runs before Android resource merge. No manually copied generated asset can become a hidden source of truth.
- App versionCode **125**, versionName `0.27.3-rc65-library-optical-icons-preview`, appId `org.phwildlife.survey.beta08`, original signing identity and SQLite schema27 unchanged.

## Acceptance
- Python tests assert locked original source hashes, exact cropped source pixel samples, 256px PNGs, route preservation, and Gradle build integration, in addition to entire existing scientific regression suite.
- Android14 test screenshots at 360dp/160dpi, 360dp/**440dpi**, 320dp/440dpi/**200% text**. Test seven accessible destinations, full 50dp image plates, 256px derivative intrinsic size, alpha footprint >=85% and last action scroll reachability; rerun RC60 Main Library and RC51 research ZIP tests.
- Review actual emulator before/after images and separately label original owner phone image, which is *not* evidence of updated RC65 phone installation.
- Physical outdoor readability, genuine TalkBack speech/gesture order, older-schema database upgrade and real-historical backup/restore remain unverified.

**Delivery:** Once successful, provide verified directly attached signed RC65 APK, actual screenshots/evidence and variant assets ZIP. Draft PR, not field-stable.
