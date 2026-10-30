# LIKAS RC60 | Resolve three Library visual decisions

**Date:** 31 October 2026 (Asia/Manila). **Status:** Scoped implementation; Android emulator acceptance pending. **Parent:** RC59 main Library draft PR #56, code119, tested source `48df5c1c3e6660939f6f9f00f17f92a8d354d191`. Integrated scientific core remains RC49; no draft merged.

## Explicit owner request
Resolve the three open main Library screen decisions: (1) Conservation shield stylistic mismatch, (2) page-title/section hierarchy at large font, and (3) inconsistent opening affordance of featured Species/Habitats vs other Library rows. Keep current screen as sole UI scope.

## Chosen implementation
1. **Conservation icon:** Use a new original 16×16 pixel-grid shield containing a green leaf, rendered as a Java View using discrete non-antialiased squares. The `conservation-pixel-shield` symbol is only used by this Library row and remains an accessible, semantic shield. It does NOT modify, repurpose, synthesize or falsely designate an owner-approved PNG. The existing original 18 approved PNGs stay locked; other usages of the generic vector shield are unaffected.
2. **Library typography hierarchy:** Retain `UiKit.page` and its Back/Library title. Make only the main Library's two section headings 14sp, medium-weight, restrained letter spacing and shared forest color. Leave card titles and scientific details untouched. Native font scaling remains active; no fixed text heights.
3. **Consistent open action:** A new reusable `LikasPortalUi.libraryFeature()` reuses the existing accessible icon-left/text/right-chevron `action()` renderer, then applies the pale featured background and 100dp intrinsic minimum height. Species and Habitats now use the same chevron as five white reference rows. Other screens' Home tiles and their approved icons are unaffected.
4. **Identity:** Version `0.27.3-rc60-library-visual-preview`, code120, unchanged appId `org.phwildlife.survey.beta08`, SQLite27 and original signing certificate. No surveys, GPS, offline scientific references, statuses, source citations, licensed media, or data formats changed.

## Acceptance gate
- Android 14 visual screenshots of Library top and bottom at **320dp/100%**, **360dp/100%**, **412dp/100%**, **412dp/130%**, and **320dp/200%**. Test all seven clickable, accessible, >=48dp controls, preserved approved art, and last-row reachability.
- Instrumented assertions for pixel shield rendered under Conservation, page title at least 16% larger than section headings, medium nonbold section titles, matching icon-left / chevron-right featured rows.
- Rerun RC59 main Library, RC58 Discover and RC51 exact research ZIP tests, all Python scientific regression and pinned-signer Android build. Reject clipped / unreachable controls and visual anomalies on actual screenshots.
- Full phone/TalkBack/outdoor and in-place historic-record upgrade remain unverified even if emulator green.

## Visual limitation
Pixel shield is **source-drawn** iconography, not a new owner-approved PNG. It can be independently reviewed and replaced later with owner-approved art without changing destination semantics.
