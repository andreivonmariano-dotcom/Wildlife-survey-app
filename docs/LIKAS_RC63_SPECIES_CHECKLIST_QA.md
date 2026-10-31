# LIKAS RC63 | Full Philippine Species Checklist visual, keyboard, accessibility and historical upgrade QA

**Date:** 31 October 2026, Asia/Manila. **Status:** source patch and Android 14 acceptance staged, not yet passed.
**Parent baseline:** RC62 Species Discover code122, draft PR #59 and exact code/document HEAD cdc31b75933b21f25ee501f0cad3a34a45047c6b. Original integrated scientific core remains RC49; all stacked PRs draft.

## User request and scientific boundary
Focus the next Library pass on the complete source-linked Philippine Species Checklist, not on Habitats. Use an emulator to test keyboard-open layout, accessibility order/labels to the degree technically supported, and a signed in-place update containing historic survey records. Real outdoor readability and live physical TalkBack speech are **not** simulated by an AVD; do not claim them.

Source reviews: Rc7SpeciesLibraryActivity.java, FieldDb.java, UiKit.java, RC7 Species Library Master Specification, RC62 QA and original pinned signing/publication rules. Preserve scientific checklist source names, original counts, aliases, search policy, evidence tiers, photorights, IUCN/DENR limitations, raw field observations, Complete/Incomplete/Not Counted, and source-linked attribution. Changes affect design requirements G, L, T, D, U, C and E; no schema or library data migration.

## Source-level findings and scoped changes
- At enlarged text and with the soft keyboard open, the source banner plus two fixed filter menus and research toggle crowded out the results. Keep the banner in the scroll region at >=160% font; move both visibly labeled filter fields and research toggle into the scroll region **at all font sizes**. Leave Search, a compact evidence-count button and Show more accessible.
- Source-backed searchable checklist: retain two distinct filters with *visible labels* **Wildlife group** and **Reference coverage**, and native Spinner selected values. Keep group/source taxon search unchanged.
- Give keyboard-open Search an explicit resize policy (SOFT_INPUT_ADJUST_RESIZE), search IME action and shorter large-type hint. Native font scaling preserved. The test must show a **visibly open IME** rather than merely accepting key input.
- Give tappable evidence-status disclosure an accessible button role, retain complete evidence warning, and add a small right chevron on each focusable card. Prevent duplicate scientific name in spoken label when no common name is supplied. Keep all 24-per-page paging and original stable source keys.

## Android 14 test gates
- Full-checklist top/scrolled/IME-open images at 360dp/100%, 412dp/130%, and 320dp/200%. Native actual keyboard insets must indicate visible IME and at least 72dp visible results region, not a screenshot simulation. Android AccessibilityNodeInfo must give evidence limits button role and clickable action; group/scope labels and accessible row names must exist.
- Offline scientific-name lookup `Pithecophaga jefferyi` and no-match state. Prior RC62 Discover plus RC51 exact research ZIP instrumented regressions.
- In-place historical preservation test uses a **disposable** Android 14 emulator: build and install exact source RC62 code122 with original debug signer, run existing Rc51ResearchZipInstrumentedTest to seed complete, legacy-null, zero-detection, incomplete, not-counted, active and anomalous synthetic QA surveys; then `adb install -r` signed RC63 code123 **without clearing app data**. Verify original rows, outcomes, observations, zero-detection effort, project data and export Not Counted exclusion. This verifies same-schema in-place upgrade only (SQLite27 to SQLite27), **not** older schema migration or a real user's existing database backup/restore.
- Query presence of TalkBack package and dump Android accessibility hierarchy as evidence, but absence of a TalkBack service or automated node traversal cannot establish actual speech order/gestures. No physical-device test or outdoor glare claims.
- Independently validate signed APK identity, exact source SHA, emulator screenshots and logs. Release remains draft preview until real-device and broader upgrade gates pass.
