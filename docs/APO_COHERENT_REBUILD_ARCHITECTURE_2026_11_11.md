# APO coherent rebuild | architecture and integration baseline

**Working proposal, 11 November 2026 (Asia/Manila).** This is an implementation planning and audit document, **not** a change to the owner-approved APO Charter v1.2. It creates no release approval and does not claim that the UI rebuild, tests, APK, or phone acceptance are complete.

## Verified starting point

- Repository: `andreivonmariano-dotcom/Wildlife-survey-app`. Verified default branch `main` at `046bfa2dc4e37bc163bf2956e3472ccd0a9aeefa`, 11 November 2026.
- Android `versionCode 173`, `versionName 0.27.4-rc113-living-atlas-preview`; original application ID `org.phwildlife.survey.beta08`, original signing certificate, SQLite schema 29, and historic migration lineage.
- RC113 signed Android emulator and synthetic no-clear-data RC109 to RC113 upgrade passed in CI run 42941822102, as documented in `docs/APO_RC113_LIVING_ATLAS_EMULATOR_QA_2026_11_11.md`. Physical field-device testing and public licensing acceptance remain open.
- Offline Book 01 v1.82 has 715 accounts and a separate 1,159-entry bibliography. The 333-photo gallery remains subject to attribution/rights review. No approved ecological graph edges have been verified in the provisional Living Atlas data pack.
- `AGENTS.md` and `docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md` (content v1.2) govern development. Preserve the canonical Google Doc unchanged without separately approved amendment wording.

## Diagnosis and evidence boundaries

**Confirmed from current repository source:**

1. `MainActivity.java` is roughly 202 KB and combines live recording, local UI state, GPS, sensors, media and other actions. `WelcomeActivity.java` is roughly 74 KB and combines Home routing, nested section hubs, setup forms, active-survey resumption and GPS handling. These concentrations make a screen-only change hard to isolate.
2. The current source has parallel styling/color definitions in `UiKit.java`, `ApoHomeUi.java`, `PhLibrary2Ui.java` and related design classes. A theme-only fix will not produce one consistent interaction hierarchy.
3. `WelcomeActivity` routes internally among `home`, `biotools`, `fieldwork`, `records`, `analysis`, `utilities`, `phmaps`, `biodiscover` and `setup`; the Android manifest separately registers many Activity-level destinations. Each entry/return route needs a documented single owner.
4. RC113 introduced a native `LivingAtlasExploreActivity` on top of existing directory, discovery, reader, habitat and bibliography activities. The source is useful, but the experience requires navigation unification.

**Documented historical UX findings, to be rechecked against RC113 rather than assumed still present:** RC110 audits observed duplicate species/habitat entry paths, divergent visual systems, an overcrowded recorder, high-density reference screens, and placeholder Maps/Discovery hubs. RC111/112/113 subsequently addressed selected issues. The user reports the current app still feels visually conflicting. A complete *current* screenshot/usability audit and owner review are still needed before a visual defect is marked verified.

## Rebuild decision

Rebuild the **presentation and navigation architecture**, not the user-data foundation. Develop on this independent branch, leaving `main` and all older commits intact. Do not delete or recreate the Android app identity, user records, migrations, source catalog, original reference texts, approved artwork or signing files. Do not bump a version or publish an APK until implemented code and compatibility tests justify it.

### Proposed information architecture

Use the four canonical Charter destinations, with My Data as an accessible workspace:

- **Home:** primary Start Survey or Resume Survey, compact Atlas/Map/Research entry points, discreet featured species. No decorative element may displace an active survey control.
- **Fieldwork:** project → method → site/design → required parameters → live recorder → review → outcome. Keep existing validated method definitions and recording provenance.
- **Living Atlas:** one Book 01-first species and habitat search entry, one canonical species account route, scoped filters, visible source authority and the existing reviewed/provisional evidence distinction. Current family/habitat-mention previews should link into this route.
- **Map:** one honest offline map destination for verified regional boundaries and user-owned localities when functioning. Until built, demote placeholder routes rather than cloning other hubs.
- **Research Library:** one curated offline bibliographic reader and permitted full texts, no nonfunctional live-feed promises.
- **My Data:** project/site/route management, history, saved records, evidence, backup/restore and scientific exports. This is a workspace, not a second survey flow.

### Single-owner feature contract

Every user action has one canonical route and one persisted domain authority. Do not build a second species search, survey closer, backup system, map launcher or habitat inference screen to bypass an old screen. Navigation state may be restored without duplicating the saved data model. Historical class and preference names are migration-sensitive and may remain internally named LIKAS.

## Boundaries between modules

- **Preserved core:** `FieldDb`, schema/migrations, event/station/observation IDs, `SurveyClosePolicy`, method definitions, sampling effort rules, `TrackService`, GPS-fix provenance, media files, backup/export formats, packaged taxonomy, bibliographic source data, locked photo/art assets and owner-controlled rights metadata.
- **New presentation layer:** one tokenized visual theme (semantic surfaces, typography, spacing, states, controls and dark/night variants); reusable accessible navigation/header, survey status, data-entry and source-reference components; module-level route contracts; a single species-account UI.
- **Incremental adapters:** expose existing domain operations to small screen controllers/view models. Extract one feature at a time from the Home and recorder monoliths only after characterization tests establish equivalent outcomes. Do not combine an unverified persistence rewrite, a toolkit/framework migration and a UI redesign in one release.
- **Offline boundary:** none of the charter's required core workflows may require first-run imports, authentication, network or cloud.

## Rebuild acceptance journeys

1. **Open app offline:** Home shows Start/Resume, Atlas, available local map and permitted research material. Large-font 320 dp layouts remain usable. Unfinished modules have accurate status.
2. **Start survey:** choose method, define actual sampling effort, confirm GPS if available or record its absence, begin the active event once, restore after process death.
3. **Record observations:** save identified or unresolved taxon, including no-observation effort; retain timestamps, units, location accuracy, sources and optional evidence. One primary Save action and one accessible End Survey action.
4. **End survey:** Complete only when method conditions are met; Incomplete preserves early termination; Not Counted remains auditable and excluded from default analysis. Unsaved edits trigger clear review. Home/Back must not silently close a session.
5. **Return to data:** History, My Data, backups and scientific ZIP/CSV preserve original rows, media and status meaning. An in-place signed upgrade from the RC113 baseline must pass on seeded data without uninstall or clear.
6. **Read Atlas:** search a species, inspect the source-scoped account, family context, explicitly labeled habitat text mentions and references. Back returns through the actual visited route. Provisional ecological relations are not presented as verified edges.
7. **Use environment features:** GPS and elevation are identified by source/fix quality; unavailable measurements remain unknown. Map imagery and boundaries retain proper source scope.
8. **Accessible field testing:** Android 14 emulator at 360 dp / 100% and 320 dp / 200%, portrait, keyboard, dark system mode, Back/gesture, active survey and cold recovery. Later gate: TalkBack, physical handset, GNSS/audio/photo, sunlight/night and long-running recording.

## Suggested execution sequence

**Gate A: capture baseline.** Inventory every manifest screen, navigation entry and controller; compare current RC113 screenshots to the RC110 audit. Record feature owners, protected persistency surfaces, source and asset hashes, and a test-to-feature matrix. Capture an untouched install/upgrade reference.

**Gate B: design system and shell.** Implement one visual token system and a minimal functioning Home, core destinations and back stack. Remove competing entry points only after replacement paths are exercised. No scientific or survey persistence changes.

**Gate C: rebuild Fieldwork presentation.** Replace setup and recorder presentation behind existing survey operations. Gate all 17 registered methods, paused/recovered sessions, zero detections and three outcome states.

**Gate D: unify Atlas and My Data.** Route all discovery/context/reader screens through one canonical experience. Preserve source text and real data; make supplemental research/Map routes honest and nonduplicative.

**Gate E: acceptance and promotion.** Run science/SQLite/backup regression, exact identifier/signing/asset checks, no-clear-data in-place upgrade, native screenshot review and user acceptance. Promote only an explicitly accepted, verified branch. Produce a current-session directly downloadable signed APK if and only if the artifact has actually been retrieved and checked.

## Present state and next task

This document records the proposed rebuild's protected baseline and route contracts. **No application source, schema, survey data, or approved governing document has been changed here.** Next: a screen-by-screen RC113 route inventory and a design-system prototype on the isolated branch, with screenshot evidence before replacing legacy presentation.
