# LIKAS RC66 | Individual species-account screen and cited reference layout

**31 October 2026, Asia/Manila. Status: draft implementation; Android evidence pending.**

## Starting point
- Parent: RC65 `feature/likas-rc65-library-icon-optical-crops` (draft PR #62; tested `67883f443b968a74b8c33c59b39b40af42ee8261`).
- This branch: `feature/likas-rc66-species-account-screen`, code **126**, `0.27.3-rc66-species-account-preview`. Original application ID `org.phwildlife.survey.beta08`, SQLite 27 and pinned signing certificate remain unchanged.
- Changes only affect `TaxonDetailActivity`, `Rc11PrioritySpeciesView`, and tests/build/QA. No approved icons, images, taxonomic JSON, source references, conservation labels, survey data or survey workflows are edited.

## Observed source-level findings
1. The all-species `Conservation research · 96 species` shortcut stayed fixed above a loaded individual species account after search and group controls were hidden. It consumes reading space and competes with the record heading.
2. At widths below 350dp or fontScale >=1.60, the common/scientific-name search shared one fixed-height row with a 48dp dropdown and 80dp Find button. The remaining text field becomes cramped.
3. A research-card title used MATCH_PARENT inside a horizontal icon+title row, risking horizontal overflow after a 36dp icon and 11dp margin. Fixed 45dp/50dp expand buttons could constrain scaled text. Section jump chips had no fading edge or explicit horizontal-swipe description.

## Scoped code changes
- Hide the global conservation shortcut only while an individual record is open; restore it on returning to general search. The 96-species destination remains available in search mode and from Library, without modifying underlying conservation claims.
- Reuse the same search EditText, autocomplete adapter, dropdown and Find listeners. Reflow search across two rows only below 350dp or at >=160% font. Use Android IME resize without altering typed queries.
- Change research-card header text to weighted remaining width, let reference/expand controls adopt their own measured minimum touch sizes, and give jump chips a horizontal fade plus accessibility description. Existing research text, APA in-text citations, source drawer and missingness remain untouched.

## Regression and acceptance scope
- Python source tests: `scripts/test_rc66_species_account_ui.py` plus prior `test_*.py` suite.
- Android instrumentation: `Rc66SpeciesAccountInstrumentedTest` checks a real catalog-backed Philippine Eagle detail, hidden global shortcut, retained search path, visible source heading and screenshots of top/bottom. It also checks the generic search state, using disposable emulator data only.
- Workflow: `.github/workflows/rc66-species-account-android.yml`, `scripts/rc66_species_account_emulator_acceptance.sh`. Screenshots planned at 360dp/160dpi 100%, 360dp/440dpi 100%, 320dp/440dpi 200%. Follow with RC63 checklist and RC51 exact research ZIP regression. The workflow builds a signed code126 preview and preserves test evidence.
- **Do not report Android acceptance until the workflow succeeds and screenshots are inspected.** Screenshot diff alone is not evidence of readability; inspect clipping, source attribution and touch targets.
- Real TalkBack speech, historic older-schema and owner-data upgrades, recovery/backup, physical daylight and hardware GNSS field tests remain release gates. Do not merge the draft chain or describe RC66 as stable.

## Next after passing
Independently review the rendered species account with source-backed facts and photo credit. Only then continue one screen at a time to Habitats. If a real phone screenshot shows a mismatch, apply a focused correction to the same screen first.
