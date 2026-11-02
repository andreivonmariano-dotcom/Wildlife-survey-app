# APO development instructions (formerly LIKAS)

## Authoritative development baseline (2 November 2026)

**APO** is the Philippine biodiversity toolkit, formerly LIKAS. **Current Android development branch:** `feature/apo-biotools-workflows-v136`, verify HEAD at every new task. It is a child of signed RC75 `feature/apo-biotools-destinations-v135` and forms draft PR #78. The historical default `main` is not the current development source.

**Latest verified and directly delivered signed APK:** `0.27.3-rc76-apo-biotools-screen-refinement`, versionCode **136**, app/test source `80ca725f3fca516678f973d10be18da9e48c1df1`, CI run `41378920723` (**SUCCESS**, 403 Python source/science regression tests and JVM/Android compilation). APK `APO-v0.27.3-RC76-BioTools-Screen-Refinement-code136.apk`, **11,752,828 bytes**, SHA-256 `b2ba708d064ade8a7afd4062aeaf76d268d39d7c094989d7929ea7d760679ba2`. Original Android ID `org.phwildlife.survey.beta08`, signer `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, SQLite **28** unchanged. QA and limitations: `docs/APO_RC76_BIOTOOLS_WORKFLOWS_SIGNED_QA_2026_11_02.md`. This is a **signed development preview** without actual field phone acceptance.

APO Home remains BioTools, PHLibrary, PHMaps and BioDiscover. BioTools remains Surveys, Projects & Stations, Field Records, Analysis & Exports, and Field Utilities. RC76 clarifies the four-stage survey setup, makes Survey History filters scrollable and its drilldown Back consistent, adds labeled and compact Study Templates, adds export scope/privacy preview, and scrolls Data Quality controls together with findings. Preserved scientific survey and export business logic. PHMaps comprehensive atlas and BioDiscover live feed are not yet implemented.

## Preserve and verify

- Keep the approved, SHA-locked APO mountain emblem, separate borderless splash mark and native `#0B4B39` to `#021918` gradient. Do not reuse the retired frog as the launcher. Do not modify approved artwork without a new owner decision. See `docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md` and `docs/APO_STARTUP_SCREEN_V1_DESIGN.md`.
- Preserve app package and signing continuity, SQLite28 migrations, historical records, 714-species PAIC reference, 2020 ESA WorldCover-derived reference, scientific provenance, and survey outcomes Complete / Incomplete / Not Counted with accessible End Survey.
- Home and BioTools now use text-first navigation. Do not restore retired illustrated tiles or combine data/research outputs with device backup. Nested PHLibrary and Settings may retain legacy art for separate screen-by-screen review.
- Follow product/scientific governance in `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` on `main` until its active-branch migration is reviewed. The latest explicit APO naming and branding decisions and the current source/QA records supersede outdated brand descriptions within that historical spec.
- Read `docs/LIKAS_PROJECT_STATE.md`, `docs/LIKAS_DECISION_LOG.md`, this branch's README and relevant QA before each task. Update project state and append the decision log when making accepted UI or code decisions; never erase history.
- Never merge this draft release lineage into default `main` solely to make GitHub look current. Verify ancestry, schema28 policy, QA and historical upgrade separately before any integration.
- A new APK must be actually compiled, signer/package/version verified, and attached via a **direct in-chat file download**, not a GitHub run/release page. Never present an older APK as the new version.

## Current state

Signed CI run `41378920723` passed for APO RC76 (code136). Physical device upgrade with archived surveys, on-device screen/large-text/TalkBack, GPS, fieldwork, template edits and research ZIP contents remain unverified. Documentation-only commits do not create a newer APK.
