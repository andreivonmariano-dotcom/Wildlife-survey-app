# LIKAS RC54 | Screen 3 Survey Setup visual audit
**Date:** 30 October 2026 (Asia/Manila). **Status:** isolated draft awaiting complete Android test and screenshot inspection.

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

## Current limitations
No visual result, APK build or instrumentation pass should be claimed solely from this draft description. Record actual run IDs, test counts, screenshot inspection findings, signing identity and exact APK checksum after success.
