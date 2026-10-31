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


## Final emulator acceptance, signed preview and observed limits | 31 October 2026

- **Exact tested application code/test HEAD:** `bbc9b24be05bb55b365cae22a052a23b59da3057`. GitHub Actions Android14 workflow **41156417761 SUCCESS**, job **124404095159**, **370 Python unittest executions** (0 failures), Gradle signed Android/JVM build, and **nine Android instrumentation executions** (2 individual account tests repeated at 360dp/160dpi 100%, 360dp/440dpi 100%, 320dp/440dpi 200%; then 3 checklist and exact research ZIP regression tests). Actual screenshot/Android logs collected and reviewed, not inferred from source alone.
- **Screen review:** Real full-system Android14 PNG screenshots at all three settings, account top/bottom and search (9 PNGs). Individual Philippine Eagle page shows name, family/group, cited sections, approved offline image with credit, accessible Search another, jump sections, sources and references with scrolling. Original global Conservation research shortcut is hidden in the loaded account and visible again in search mode. No horizontal clip seen in selected account/detail captures; 200%-font UI naturally requires vertical scrolling. Original screenshot revealed clipped `Common, scientific, or former name` search hint at 320dp/200%; fixed it to short `Search species` and re-ran the entire acceptance. Verified corrected actual 320dp/200% PNG text fits; full search semantics retain accessible content description, aliases and autocomplete.
- **Source regression maintenance:** Initial CI failed 15 stale Python assertions hardcoding code125 and RC65 preview label after intentional code126 update. Replaced fixed version assertions in legacy RC50–RC65 tests with a **minimum allowed versionCode 125** and `versionName '0.27.3-` family; their appId/schema/GPS/export/asset assertions remain in effect. A second CI attempt exposed 13 remaining old versionName assertions; those were similarly corrected, and final green suite passed. This was test-maintenance, not a change in survey semantics.
- **Signed APK:** GitHub artifact ID **13025725422**, extracted `LIKAS-v0.27.3-RC66-species-account-preview-debug.apk` **9,805,679 bytes**, SHA256 `5cb5cb7b04033d43e4a542e8ae02a772639a63088da0b9784b42571bfcf800e5`. Independent ZIP CRC pass (145 entries), binary manifest confirms `org.phwildlife.survey.beta08`, versionCode126, `0.27.3-rc66-species-account-preview`; complete Android APK Signature Scheme v2 RSA-PKCS1-SHA256 signature and **12-chunk content digest verified**; certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` unchanged. All **109 packaged assets/** files byte-for-byte equal to verified RC65 APK.
- **Screenshot/Android logs evidence:** GitHub workflow artifact **13025960157**, downloaded and inspected `LIKAS-RC66-final-Android-evidence.zip`, ZIP CRC valid with 39 entries including nine screenshots and test logs. Source tests, actual screenshots and signed APK were obtained from the same tested commit.
- **Direct delivery:** APK and emulator evidence attached as verified direct chat files in this implementing conversation, not GitHub-only links; future chats must reverify sandbox paths before reuse.
- **Release boundary:** Accepted as an **Android emulator-tested draft preview**, not merged or field certified. Physical phone visual readability, TalkBack spoken traversal, historical owner-data and older-schema upgrade, media/backup restore, process-death and live field GNSS remain unverified.
- **Next scoped work:** continue one-screen-at-a-time Species Library details or individual account observations only after owner phone review; otherwise move to Habitats Discover screen. Do not alter cited biology or make conservation reclassification without new evidence.
