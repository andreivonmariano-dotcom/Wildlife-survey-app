# LIKAS RC54 | Screen 3: Survey method chooser

**1 November 2026, Asia/Manila.** Isolated draft PR #68 into RC53 Fieldwork draft PR #50. The active integrated RC49 application is unchanged.

## User intent
Continue screen-by-screen Android usability and aesthetic review following RC52 Home and RC53 Fieldwork. This pass concerns the method chooser at the top of Birds, Herpetofauna and Mammals setup in `WelcomeActivity.setup`. The scientific v3.6 master checklist is **not** imported in this UI-only change.

## Source implementation
- Every survey method keeps the original `SurveyMethods.Method.code`, label, description and `basicLog` flag. Bird 4, herpetofauna 6 and mammal 7 method definitions are unchanged.
- The same native direct-child `RadioButton`s under `RadioGroup` now show an individually styled method name and a smaller muted description **in the same selectable card**. Screen readers receive the full description and native selected state.
- Selected cards use a contrasting pale-leaf fill and visible pond-green outline, with ripple feedback; default/restore selection remains based on ids 1000+n.
- 80dp minimum option height, intrinsic text wrapping and no fixed truncated label height. Remove an unnecessary nested card and the single below-list hint which used to change when switching methods.
- Keep the familiar `01 Choose a survey method`, `02 Project and location`, `03 Sampling effort`, `04 Begin survey` sequence and the one pinned `Continue to GPS` control.
- Preserve selection-dependent method forms, project/region/GPS boundary suggestion, route templates, optional study rules, form values, validations, active survey lifecycle and historical data.

## Safety and provenance
Application id `org.phwildlife.survey.beta08` unchanged; SQLite schema 27; RC54 preview versionCode 114. Original locked startup branding, illustrations, taxonomic source data, and user-generated surveys are untouched. Never install an older lower-versionCode release over this preview.

## Verification
- Python source assertions: `scripts/test_rc54_survey_method_chooser.py`.
- Android instrumentation: `Rc54SurveyMethodChooserInstrumentedTest` navigates Home -> Fieldwork -> each taxon, checks option labels, descriptions, checked state, 80dp touch height and GPS continuation.
- CI workflow for this branch: `.github/workflows/rc53-fieldwork-theme-android.yml` (name updated to RC54 on this branch). Android emulator capture script `scripts/rc54_survey_chooser_emulator_acceptance.sh` covers 360dp/100%, 412dp/130%, 320dp/200% and science ZIP regression.
- **Unverified until the workflow completes:** Android compile, instrumented acceptance, screenshots, original signer on compiled APK, direct APK delivery and physical handset testing. The GitHub source commit or draft PR is not proof of a release.
