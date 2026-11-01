# LIKAS design system | Foundations (RC47)

**Status:** Source implementation pending complete signed-build and physical-device acceptance.  
**Date:** 30 October 2026 (Asia/Manila)  
**Scope:** Part 1 of the approved staged interface unification. Applies chiefly to shared UI helpers, Library and Settings. Not a redesign of the field recorder or scientific reference content.  
**Requirements:** G-02, G-03, G-07, U-01, U-04, U-05, U-06, U-08, U-10 and C-06.

## Interface principles

1. **Field-first:** Record and end a survey safely offline, with an always-available End Survey control and explicit Complete, Incomplete and Not Counted outcomes. Decoration cannot displace these actions.
2. **Evidence-first:** Species and habitat views preserve citations, geographic scope, uncertainty, rights and distinct IUCN/DENR status evidence. UI simplification does not alter biological claims.
3. **Coherent structure:** Each hub has one Back/navigation header, one scrollable content region, short section headings and clear groups of actions. No repeated publisher disclaimers or developer artifacts.
4. **Accessible by default:** Text uses Android sp scaling. Interactive areas meet a minimum 48dp touch target. Buttons can grow for long labels and enlarged text. Use the existing Less motion preference.
5. **Locked brand assets:** Reuse the exact 18 approved pixel-art PNG resources and original pond-frog launcher. No image files, icon mappings, licenses or hashes are changed by this design pass.

## Implementation vocabulary

`LikasDesignSystem.java` stores reusable measurements without Android dependencies.

| Role | Value | Usage |
| --- | --- | --- |
| Spacing rhythm | 4, 8, 12, 16, 24, 32dp | Layout and section spacing |
| Page gutters | 16dp | Library/Settings page shell |
| Corners | 14dp input, 16dp button, 19dp card, 26dp hero | Consistent surface shape |
| Type hierarchy | 23sp page, 17sp section, 16sp card, 15sp body, 13sp caption, 14sp controls | Android scaling retained |
| Minimum touch area | 48dp | Controls, back navigation |
| Paired cards | Stack below 390dp or at >=130% font | Existing RC45 adaptive policy; now one rule |

`UiKit` now owns `page(...)` (Back header + scrolling content), `sectionTitle(...)`, `caption(...)`, shared buttons and surfaces. `FieldVisuals.section(...)` uses those same typography primitives. `LikasPortalUi` retains exact image-resource mappings but now uses its supplied card color rather than title-based override and applies the common accessible card outline.

**Visual contrast:** Functional control/card borders use an opaque deep moss outline (RGB 117,143,123), estimated 3.29:1 against the warm background and 3.52:1 against white. Decorative surface hairlines retain a lighter color. Warning text uses amber RGB 145,85,20, estimated 5.60:1 against the warm background. These are calculated sRGB ratios, not measurements on a phone display; outdoor and high-contrast checks remain required.

## First migrated screens

- **Library:** shared page structure; one heading with an offline field-guide subtitle; consistent reference-tools grouping. All Species, Habitats, Similar species, Photos, Conservation, Sources and Habitat links actions remain.
- **Settings:** same shared page structure and title hierarchy; Less motion switch can grow vertically at larger type. Backup, Storage, Import, Data check and About remain.
- **Fieldwork helper:** section headings and contextual notes now share typography with Library and Settings. Recorded field values and survey state logic are unchanged.
- **Portal cards:** common action outlines, card text size and accessible descriptions, preserving approved pixel art and existing destinations.
- **Shared breakpoint:** `FieldLayoutPolicy.stackPortalPair` delegates to the design system's single-column rule.

## Explicit limits of Part 1

This is **not** full application screen migration. Home and the live recorder retain their established layouts, subject to later dedicated usability work. Species detail, GIS/GPS, History, Camera, the gallery and remaining tools still have local styles. No new dark mode, app-wide navigation pattern, scientific models, reference data or database migration is claimed.

Source contracts and JVM unit tests check the tokens, preserved routes, responsive policy and critical exit controls. A signed APK build is a source/integrity gate; it does not prove layout acceptability on real devices. Test 320/360/390/412dp screens at 100/130/160/200% system type, TalkBack, keyboard state, direct access to End Survey, contrast in daylight and installed upgrade retaining survey records.

**Separate release blockers:** A backup restore may partially modify the app if interrupted, and derived research exports may handle historical NULL survey outcomes differently from History. This UI pass does not fix either issue.

## Merged-branch signed artifact verification

- PR #44 merged at `cbe828de29cdd8047fcdfe54e3116ae0a4ebefbd`; active-branch GitHub Actions run **41024068239** passed, artifact **12962428249**.
- Downloaded the workflow artifact ZIP, verified ZIP CRCs and extracted the exact `app-debug.apk` as `LIKAS-v0.27.3-RC47-design-system-debug.apk` in the active chat.
- APK size: **9,779,900 bytes**. SHA-256: `d18d32b3df3b3fedd9d411e322616c0a484b57fcbb06ac4eef91add8ce84d439`.
- Independently parsed binary Android manifest: package `org.phwildlife.survey.beta08`, **versionCode 107**, versionName `0.27.3-rc47-design-system-preview`. Verified APK Signature Scheme v2 signature and signed-content digest against the **original certificate** SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`.
- APK contains all **18** approved pixel PNG resources, **58** offline reference JPGs, compiled DEX and the packaged mammal checklist. CI icon-hash tests also passed. Existing survey methods, data model and active survey controls were not changed.
- **No phone acceptance:** Installed upgrade, TalkBack, 320dp/200%-font screenshots, sunlight readability and actual GPS/IME behavior were not run. This is still a preview, not a field-accepted release.
