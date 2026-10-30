# LIKAS visual system and screen-by-screen Android audit | Screen 1: Home
**Date:** 30 October 2026, Asia/Manila. **Status:** RC52 isolated draft, Android 14 visual/layout/instrumentation acceptance passed. Not a field-stable release.
**App baseline:** RC51 source `a2d3277897390948225229cd0bdf8a9d6d838f2c`, versionCode 111; active integrated core remains RC49 at `f108cd150804fcf463292a974ff43bf43378a909`. This screen change is PR #49 (RC52, versionCode 112) based on draft RC51 PR #48, itself based on RC50 PR #47. The three PRs are intentionally unmerged.

## Scope and audit method
This is **actual source/Android layout work**, not image mockups. Existing Android 14 screenshots reviewed before edits: RC51 Home 360dp/100% (artifact 12972524169), RC50 Home 320dp/200% (run 41042252175), and RC49 actual navigation screens (prior acceptance). Screen 1 concerns the real `WelcomeActivity.home()`, `LikasPortalUi.hero/tile/session()` and shared layout tokens. No approved pixel-art image or startup sequence is altered. No survey lifecycle, biology, SQL, media, GPS, export or settings changes.

## Design system to apply consistently as we inspect each screen
- **Color roles:** `UiKit.BG` warm off-white background, `INK/DEEP` dark forest primary text and dark Hero, `FOREST/POND` actionable emphasis, `MUTED` secondary text, `LEAF_LIGHT` consistent secondary navigation surface, `CONTROL_BORDER` functional outlines. Diagnostic warnings and errors retain distinct semantic colors. Favor sufficient readability on pale backgrounds; use one contrast check for every new palette token.
- **Typography:** reusable `LikasDesignSystem` page / section / card / caption / control sizes; actual device font scaling honored with intrinsic heights. Avoid fixed-height text clipping. Preserve taxon scientific names, units, references and location uncertainty exactly.
- **Geometry:** 4dp spacing rhythm; screen gutter 16dp, card radius 19dp, hero 26dp, minimum actionable target at least 48dp; new Home navigation icon 56dp and min card 100dp. Shared 16dp hero-to-actions gap, 8dp tile gaps.
- **Hierarchy:** one dominant action per stage; strong Fieldwork destination, equivalent Library and Settings surfaces, active Resume always before ordinary actions. No duplicate logos, status headings or nonfunctional ornament. Do not invent modules or imply Research Watch is shipped.
- **Behavior:** preserve all three Home destinations, accessible labels, fresh state after Resume, real scrollability at 320dp/200%, Android Back semantics, and minimal motion respecting the Less motion preference.

## Screen 1: observed baseline and proposed correction
- **Baseline Android screenshot:** Home Hero + `EXPLORE LIKAS` kicker duplicated product branding. Three Home tiles showed Fieldwork dark, Library light, Settings medium green without corresponding functional priority; Home sublabels were more verbose than needed. At 320dp/200% font, titles/captions wrapped and Settings moved below fold though the page was scrollable. A prior RC49 360dp measurement bug was fixed in RC50 and must not regress.
- **RC52 code changes:** retain green branded Hero with the exact approved tagline `Explore. Record. Conserve.`; remove decorative `EXPLORE LIKAS` heading; Fieldwork stays the primary dark card; Library and Settings use the same pale `LEAF_LIGHT` card and `LEAF_MID` icon holder. Tighten descriptive labels to `Surveys & history`, `Species & habitats`, `Backups & preferences`. Reduce icon to 56dp and card min to 100dp with readable scaled text. Keep approved illustrations and original frog unmodified.
- **No data changes:** app ID `org.phwildlife.survey.beta08`, schema 27, signer and photo taxonomy assets unchanged. VersionCode advanced from 111 to 112 for non-downgrade updates.
- **Current code:** `WelcomeActivity.java`, `LikasPortalUi.java`, `LikasDesignSystem.java`, existing instrumentation contract updated for visible/accessibility labels. Added `scripts/test_rc52_home_theme.py` for source regression.

## Tested and visually inspected (30 October 2026)
- **Android 14/API34 CI:** workflow run `41050079116` completed **success** at source SHA `28cf4e17a2988b5e5cc94ff429212f607957f925`. Signer pin and build passed; **327 Python source/SQLite checks** ran, Gradle app/JVM/test APK compiled, and **3 Android instrumentation tests passed with zero failures/errors**. These included Home layout, Birds setup control labels, and RC51 exact research ZIP fixture.
- **Real screenshots reviewed:** `rc52-home-360-normal.png` (360dp/100%), `rc52-home-412-font130.png` (412dp/130%), `rc52-home-320-font200.png` and `rc52-home-320-font200-scrolled.png` (320dp/200%). Home menu is wholly visible without scrolling at 360/412; at 320/200 Settings remains reachable by ordinary scroll, all card captions remain legible, no horizontal clipping observed. A terminal/QA XML check confirmed app's Fieldwork, Library, Settings accessibility names. The latter screenshot is scrolled and deliberately crops the top of Hero.
- **Text/contrast checks:** computed WCAG contrast ratios for token roles: `INK` on `LEAF_LIGHT` 12.38:1; `MUTED` on `LEAF_LIGHT` 5.34:1; `CREAM` on `DEEP` 14.99:1; `CONTROL_BORDER` on `LEAF_LIGHT` 3.17:1. These are palette ratios, not a sunlight or TalkBack test.
- **Signed APK handoff:** exact code112 APK extracted from GitHub artifact `12974098120`, **9,783,772 bytes**, SHA-256 `cfb17be8d4748ef227914a4d9faa2ae25b24e0a39ac039366c3aaeaeef009c43`. ZIP CRC verified, 18 owner-approved icons and 58 offline JPGs packaged. APK v2 signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` matches pinned identity and signedData RSA/PKCS1 SHA256 signature verified. Emulator evidence artifact `12974502263`. Direct APK/screenshots attached only to original conversation; any future chat must verify their actual local paths before linking.
- **Existing design choices protected:** start splash/animation, approved artwork PNGs, data schema, survey lifecycle including End Survey, and derived research ZIP code (from RC51) unchanged in RC52.
- **Remaining acceptance:** real handset 320/360/412dp, battery/motion, outdoor glare, TalkBack focus order, and in-place historical records upgrade are still not verified.

## Verification targets
1. 360dp/100%: complete visible Home menu; no oversized Hero; one clear label hierarchy.
2. 412dp/130%: labels legible and action cards not clipped.
3. 320dp/200%: all action destinations reachable by ordinary vertical scrolling, readable without horizontal clipping, no compromised Resume behavior.
4. Home + Birds setup instrumentation continues to navigate; RC51 full research ZIP passes its previously established scientific fixture.
5. Signed APK identity and 18 approved PNG artwork present. Inspect actual screenshots before marking visually accepted. Device screenshot checks are layout acceptance, not a physical sunlight/glove/TalkBack trial.

## Subsequent screens, each separate scoped revision
- **Screen 2, Fieldwork hub:** simplify New survey, Current survey, History/Analysis and Field tools grouping. Check 320dp/200% cards, user-selected survey state, 48dp targets.
- **Screen 3, Survey setup:** Birds, Herpetofauna, Mammals method selection, field hierarchy, contextual controls, keyboard, pinned Continue to GPS, units and study requirements.
- **Screen 4, GPS acquisition:** fresh fix/accuracy/altitude messaging and permission/timeout states, distinct from sampled station GPS and habitat MSL elevation.
- **Screen 5, Live recorder:** input and capture priority, pinned End Survey, pause/resume, Complete/Incomplete/Not Counted and zero-detection behavior, screen orientation/system UI.
- **Screen 6, History and observation review:** list search and Clear wrapping, edit history, original/edited record visibility, action confirmation.
- **Screen 7, Library/Species/Habitats:** discover filters, taxonomy account cards, conservation heading wrapping and evidence labels, cited locality caveats.
- **Screen 8, Settings/data tools:** backup/restore, deletion confirmations, export status, preferences and Less motion. Future Research Watch remains labeled planned until implemented.
Review inherited shared components when first encountered rather than replacing all UI code at once.

## Release limitations
The new RC52 layout passed the scoped Android emulator review but still awaits real handset testing. RC50/51 remain draft. Unresolved scientific safety gates include historical in-place upgrade, process-death recovery, consistent research export snapshot during concurrent writes, and missing protocol-photo/WAV file auditing. UI work must not mask or override these release requirements.
