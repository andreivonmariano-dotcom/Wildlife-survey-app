# LIKAS RC54 | Screen 3 Survey Setup visual audit
**Date:** 30 October 2026 (Asia/Manila). **Status:** Android 14 source, real screenshots and instrumentation verified; still isolated draft pending handset acceptance.

## Source and scope
- This is a scoped update to actual Android `WelcomeActivity.setup(String selected)`, used for **Birds, Herpetofauna and Mammals**. Draft PR #51 is based on RC53 draft PR #50, itself based on RC52 #49, RC51 #48 and RC50 #47. Active integrated core RC49 unchanged. Preview versionCode **114**, versionName `0.27.3-rc54-survey-setup-preview`, package `org.phwildlife.survey.beta08`, SQLite schema **27** unchanged.
- No methodology, validation rules, default numeric values, persisted setup preferences, geospatial lookup, route-template computation, specimen observation, survey lifecycle, export, original approved PNGs or launcher/splash artwork changed.
- Existing setup is intentionally long and scrollable, preserving method-specific controls. The GPS continuation is always the **single** bottom-docked button, outside the form scroll area. This screen must work offline without guessing effort, elevation or geographic region.

## Baseline problems from code/layout inspection
- Four numbered setup sections used filled green block labels, at odds with RC52/RC53 quiet section typography.
- The method radio buttons already had individual outlined surfaces but were enclosed in an additional outlined wrapper.
- Project and Manage only stacked at 160% text, allowing compressed selector text at smaller widths and medium-large fonts.
- Labels alternated between 13sp plain, 13sp bold, 14sp bold and 16sp bold; native spinners lacked standardized accessible descriptions and minimum heights.
- Birds line-transect bearing entry had only an EditText hint, without a separate persistent visible field label.
- Expanded Details and optional Study rules controls were clickable text, but lacked explicit stateful spoken descriptions. Native radio buttons and user-entered values must be preserved.

## Implemented presentation fixes
- A quieter four-step header using a small numbered leaf marker and shared section typography. The steps remain Survey method, Project and location, Sampling effort, Begin survey in the original order.
- Removed the unnecessary nested method wrapper border. The exact native RadioGroup and individual RadioButtons, button tint, selection updates, method descriptions and saved method remain intact.
- Added `setupLabel` to standardize Project, Checklist region, duration, radius, route length, spacing, repeat route and magnetic bearing labels; native widgets use 56dp+ selectable height and descriptive text through `setupSpinner`.
- Project selector + Manage button now reflow to one column under the existing `FieldLayoutPolicy.stackPortalPair` rules. This avoids narrow label space without altering the selected project.
- Visible Birds line-transect magnetic-bearing label shows/hides in lockstep with its numeric field; the existing degree range and native validation are unchanged.
- Additional details and optional study-rule text toggles now declare clickability and accessible expanded/collapsed descriptions, retaining the same actual content and persistence behavior.
- One `Continue to GPS` button remains outside the ScrollView and continues to call `startSelected()` with all original validation.

## Android QA planned
- Android 14 API 34 signed original-key workflow `.github/workflows/rc54-survey-setup-android.yml`.
- Actual screenshots: Birds, Herpetofauna, Mammals at 360dp/100% (top/middle/bottom); Birds at 412dp/130%; Birds 320dp/200% across six scroll positions. Check legibility, text clipping, method selection, field types and pinned GPS visibility.
- New `scripts/test_rc54_survey_setup.py` plus Android `Rc54SurveySetupInstrumentedTest.java` exercise all three taxon setups, 4 steps, method-specific controls, conditional bearing, disclosure toggles, and pinned GPS. Prior RC53 Home/Fieldwork and RC51 full ZIP tests should remain passing.
- Physical handset high-contrast sunlight/glove conditions, TalkBack traversal, 320dp/200% with real keyboard open, in-place upgrade from historical saved field data, and GPS fix accuracy/altitude remain unverified until separate tests.

## Actual verification, 30 October 2026
- **Final tested app source:** `ae5bda21cc2a4ba8a166659bf9ba9a82f070c3fa`; dedicated CI workflow **41058778483 SUCCESS** on Android 14 API34. Initial code build/screenshots CI **41057630176** also passed its original checks, but its 320dp/200% screenshot loop falsely swiped below the scroll viewport. This was detected by manually comparing PNGs. The final QA script now starts at y520 within ScrollView bounds and requires screenshot content change plus actual appearance of Survey method, Project and location, Sampling effort and Begin survey.
- **Test results:** 334 Python tests, signed Android compile/JVM suite, seven real Android Home/Fieldwork/Survey Setup instrumentation tests, plus one full research ZIP Android test, all green. Actual screenshot XML shows the single pinned Continue to GPS action at normal and enlarged text sizes. The reviewed 320dp/200% sequence visibly progresses from the first method choices through project/region and effort controls to the final optional study rules.
- **Actual device images:** Three taxa at 360dp/100% top, middle, bottom. Birds at 412dp/130%, and 320dp/200% across nine actual scroll positions. Final GitHub evidence artifact `12977234763`; directly attached evidence/screenshots in the implementing chat. Source-level scientific method and validation remain unchanged.
- **Signed build:** CI APK artifact `12979095081`. Independently extracted `LIKAS-v0.27.3-RC54-survey-setup-preview-debug.apk` is **9,784,156 bytes**, SHA256 **`21f3888a2676aadeac8407fb7ca4a0b3e70b2ceeeecbf38e4ec49cdff4e2fdcc`**. APK ZIP CRC passed and contains 18 approved PNG icons and 58 offline JPEGs. Android APK v2 signedData RSA-PKCS1-SHA256 signature independently cryptographically verified under the pinned original signer certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Original package `org.phwildlife.survey.beta08` and SQLite schema 27 retained.
- **Visual assessment:** The numbered step headers, native radio choices, and standard labels are aligned across all three taxa. At 320dp/200%, some long native-spinner selections are truncated visually until opened, and the saved-route name EditText scrolls horizontally as standard native single-line input behavior. All four stage headings and pinned GPS remain accessible. The screen remains intentionally long to preserve protocol inputs.

## Current limitations
Physical handset TalkBack/outdoor visibility/keyboard resize, historical data upgrade, process-death restore, export snapshot consistency and missing audio/protocol-photo file audit remain unverified. Keep RC54 and upstream staged previews **draft/unmerged**, not field-stable.
