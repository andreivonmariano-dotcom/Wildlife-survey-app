# LIKAS RC59 | Main Library screen detailed visual audit
**Date:** 31 October 2026 (Asia/Manila). **Scope:** ONLY main Library hub, not Discover or individual field guides. **Status:** App changes and real emulator gate staged; acceptance pending.

## Baseline and evidence
- Latest verified parent RC58 draft PR #55, branch `feature/likas-rc58-library-discover-visual` at `bd7b5c5c364ef38c4b98069db4ce529a59c9c775`. Latest tested code `d3412aa8f1e2fc85c079f6185461feef17a51107`, Android 14 run `41087810776` passed. Parent chain remains draft and the integrated core is RC49.
- Direct visual inspection of the real RC58 Android Activity PNGs: `rc58-p320f200-library.png`, `rc58-p360f100-library.png`, `rc58-p412f130-library.png`, artifact `12993620333`. All display two pale feature cards and five lower white reference actions, the same forest color system and approved pixel icons. The top card content is accessible at 200% font and the remaining reference tools require vertical scrolling.
- Independently inspected all **18** original packaged approved icon PNGs from verified RC58 APK. Every asset is 512×512. Distinctive photographic/pixel details are legible at 50–56dp, but reusing the exact Species book icon for Similar species weakens orientation. Conservation uses a vector shield because no approved conservation PNG exists. Do not claim all destination icons are pixel art.
- Measured standard color contrast on the existing palette: muted text `#516A57` on `#F7F8F2` = **5.54:1**; primary forest `#215335` on same background = **8.36:1**; dark ink `#133326` on pale leaf `#EDF6E7` = **12.38:1**; functional outline `#758F7B` on background = **3.29:1**. Calculated WCAG 2.1 contrast ratios with sRGB linearization, not measured outdoor glare.
- Source reviewed: `LikasLibraryActivity`, `LikasPortalUi.tile/action`, `UiKit.page/navigationHeader/sectionTitle`, `FieldVisuals.section` and current reference routing.

## Prioritized visual findings
1. **P1 scrollbar occlusion:** Android native scrollbar paints over the right card border/arrows. Seen at 360/412/320 screenshots and repeatable after scrolling. Fix by giving this scroll container an 8dp right safe strip, with native bar outside the content area. Do not remove the scroll indicator or alter shared portal pages.
2. **P2 confusing icon duplication:** the Species destination and Similar species action show the same approved book icon. Switch Similar species to existing approved research (document+magnifier) art, leaving original bytes and source mapping intact.
3. **P2 inconsistent card rhythm on width transitions:** reference tools switch to side-by-side mini cards at 390dp/normal text, but stay full-width cards at 130% type. Use full-width list rows on phone-sized screens (<600dp) and paired minis only on larger tablets, so the destination hierarchy remains stable.
4. **P2 generic secondary labels:** replace `Browse wildlife` with `Illustrated accounts`, `Explore ecosystems` with `Habitat profiles`, `Field traits` with `Compare traits`, `IUCN · DENR` with `Dated assessments`, and `Habitat evidence` with `Cited associations`. The new text reflects actual destination content while avoiding unsourced local occurrence or universal conservation coverage.
5. **P3 vector conservation shield differs from pixel art:** no owner-approved counterpart in the 18-icon pack. Retain meaningful vector for now rather than replacing it with a misleading category icon or synthesizing an unapproved pixel PNG.
6. **P3 long-page discoverability:** the last card is below the fold at 200% font. Preserve native scrolling; add actual Android reachability checks rather than forcing unsafe text shrinkage or collapsing reference tools.

## Changed source and invariants
- `LikasLibraryActivity.java` only for app UI, plus versionCode119 (`0.27.3-rc59-library-hub-preview`) and dedicated QA/test files. No shared palette, drawable assets, taxon/range/status data, schema, GPS, research export, survey outcomes, photo licensing or routing changes.
- Maintain existing app ID `org.phwildlife.survey.beta08`, SQLite schema 27, exact debug signer `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` and 18 approved PNG source hashes.
- Test main Library at 320dp/100%, 360dp/100%, 412dp/100%, 412dp/130%, 320dp/200%. Capture actual top/bottom emulator screenshots and verify all seven destination controls exist, are focusable, at least 48dp high, not horizontally clipped, have non-ellipsized labels, and the final Habitat links card is fully reachable by scrolling. Verify icons actually load and keep accessibility back route.
- Rerun the three RC58 Library/Discover Android tests, one scientific research ZIP test, Python suite, JVM test APK compilation, signed APK checksum and v2 signature. Field device/TalkBack/outdoor light and upgrade with historical survey records remain unverified.

## Next screen
After RC59 acceptance, take a fresh actual-emulator baseline of the Species Discover screen for the separate next pass. Do not mix unrelated screens or claim a stable field release.
