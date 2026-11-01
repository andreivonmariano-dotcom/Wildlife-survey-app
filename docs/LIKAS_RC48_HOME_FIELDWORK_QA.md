# LIKAS RC48 | Home and Fieldwork design-system integration

**Date:** 30 October 2026 (Asia/Manila)  
**Status:** Implemented on an isolated source branch, pending scientific CI and signed-build acceptance.  
**App:** `0.27.3-rc48-home-fieldwork-preview`, versionCode **108**, same app ID `org.phwildlife.survey.beta08`, SQLite **27**.

## Scope and governing requirements

This is **Part 2** of the accepted staged LIKAS refinement. It extends the RC47 shared component system to Home, Fieldwork survey selection, survey setup and the live recorder's field inputs and quick actions. Requirements **G-07, S-01 through S-06, U-01, U-04 through U-06, U-08, U-10** apply. No biological evidence, geographic classification, records, schema, permissions or export logic changes.

## Implementation

- **Home:** Uses the RC47 page-gutter and spacing tokens around the existing primary portal cards and survey resume action. Existing three top-level destinations, approved pixel artwork and startup remain.
- **Fieldwork:** A currently active survey now receives the **Current survey / Continue field recording** heading. The **New survey / Choose wildlife group** heading appears only when starting a new survey is available. The existing Resume Survey action, Birds/Herpetofauna/Mammals entry points, data tools and History/Analysis remain. On narrow or enlarged-text screens, History/Analysis use compact horizontal actions instead of tall mini tiles.
- **Setup:** Reuses the shared `UiKit.navigationHeader` Back behavior, consistent native `UiKit.fieldInput` surfaces, design-system typography/spacing, and high-contrast radio-control outlines. Existing numeric entry types, validation, route templates, project/station selection, optional studies, and the single pinned Continue to GPS action are unchanged.
- **Recorder:** Standard text entry and offline species autocomplete share the setup field treatment. The two detection actions and method-specific quick capture/change actions stack to full width at under 390dp or system font >=130%. Existing user-configured quick actions, camera/calls/station methods and measurement paths remain. Essential GPS disclosure and Clear mark controls meet the shared 48dp minimum; method-unit action and Add photo scale with the touch-height policy.
- **Protected:** End Survey remains pinned alongside Save Observation and Review Records, with the three explicit Complete, Incomplete and Not Counted outcomes untouched. No change to underlying effort timers, observer GPS/elevation evidence, uncertain identifications, archived survey data, SQLite schema, app identity, signing configuration, checklist photos, 18 locked pixel icons or original pond-frog launcher.

## QA and acceptance boundaries

Added `scripts/test_rc48_home_fieldwork.py` for Home hierarchy, active Fieldwork wording, responsive capture tools, shared field-input presentation and survey lifecycle invariants. Existing CI Python/JVM, APK build and pinned-signature checks must pass before merge. This **does not** count as in-field validation: 320/360/390/412dp and 100/130/160/200% fonts, IME focus and dock visibility, TalkBack, actual GPS denied/stale fixes, photo/camera flow, landscape/insets and RC47-to-RC48 upgrade with real data still require direct device/emulator acceptance.

**Known separate release blockers:** Interrupted backup restore can leave partial media changes; historical NULL survey outcomes may be represented inconsistently in some derived exports. RC48 does not resolve these.

## Verified merged-build results, 30 October 2026

- **PR QA:** Scientific Core QA **41025648619** passed **310 Python tests**, JVM unit tests and Android `:app:assembleDebug` plus AndroidTest APK compilation.
- **Active-branch signed CI:** workflow **41025896914** passed on the exact merge commit **`41828e20ba6a8226b592369ee751797516bcdaf9`**, producing artifact **12963711954**, `LIKAS-0.27.3-rc48-home-fieldwork-preview-code108-debug`.
- **Direct APK delivery:** Extracted from the merged-workflow artifact as `LIKAS-v0.27.3-RC48-home-fieldwork-debug.apk` in the active conversation. This path is not guaranteed in later chats. **9,779,924 bytes**, SHA-256 **`226df5e5b751d0dba5aa8a739b9eeeab5920e69fc358f5362968e8833d68bda3`**.
- **Independent binary verification:** APK ZIP CRCs passed. Binary Android manifest parsed to package **`org.phwildlife.survey.beta08`**, versionCode **108**, versionName **`0.27.3-rc48-home-fieldwork-preview`**. APK Signature Scheme v2 cryptographic signature and signed-content digest passed against the unchanged original certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**. The APK includes 18 approved pixel PNGs, 58 offline reference JPGs and compiled Android DEX.
- **Not tested on hardware:** No handset screenshot or physical-installed migration, TalkBack, daylight contrast, IME/dock overlap or GPS receiver trial was executed. Preserve the known restore and export blockers.
