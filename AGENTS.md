# APO development instructions (formerly LIKAS)

## Authoritative development baseline (2 November 2026)

The public-facing identity is **APO**. **Use `feature/apo-biotools-v134` as the latest Android development source**, verifying HEAD at every new task. The branch is a child of RC73 `feature/apo-text-home-v133` and is proposed in draft PR #76. Old `main` and historical LIKAS RC branches are not the current baseline.

Latest verified signed preview: **`0.27.3-rc74-apo-biotools`**, versionCode **134**, app/test source `f4f948c6533bb34ef55245aa2994c1ac5faf23a3`. Signed CI run `41375198786` passed **390 Python tests**, JVM/Android compilation, signature/package/artwork/scientific reference checks. Downloaded APK SHA-256 `09c96752003bda20660199a439485ba356558e740b59a37cffda0a2887ff0132`. AppId `org.phwildlife.survey.beta08`, original signer and SQLite **28** remain unchanged. This is a **development preview**, not a certified field-stable version. Details: `docs/APO_RC74_BIOTOOLS_SIGNED_QA_2026_11_02.md`.

Current Home modules are BioTools, PHLibrary, PHMaps, BioDiscover, plus text Settings and contextual Resume. BioTools has five text-only menus: Surveys, Projects & Stations, Field Records, Analysis & Exports, Field Utilities. Independent `ResearchExportActivity` calls the established ZIP archive writer but still requires on-device export review. PHMaps atlas and BioDiscover live feed remain incomplete.

## Preserve and verify

- Keep the approved, SHA-locked APO mountain emblem, separate borderless splash mark and native `#0B4B39` to `#021918` gradient. Do not reuse the retired frog as the launcher. Do not modify approved artwork without a new owner decision. See `docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md` and `docs/APO_STARTUP_SCREEN_V1_DESIGN.md`.
- Preserve app package and signing continuity, SQLite28 migrations, historical records, 714-species PAIC reference, 2020 ESA WorldCover-derived reference, scientific provenance, and survey outcomes Complete / Incomplete / Not Counted with accessible End Survey.
- Home and BioTools now use text-first navigation. Do not restore retired illustrated tiles or combine data/research outputs with device backup. Nested PHLibrary and Settings may retain legacy art for separate screen-by-screen review.
- Follow product/scientific governance in `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` on `main` until its active-branch migration is reviewed. The latest explicit APO naming and branding decisions and the current source/QA records supersede outdated brand descriptions within that historical spec.
- Read `docs/LIKAS_PROJECT_STATE.md`, `docs/LIKAS_DECISION_LOG.md`, this branch's README and relevant QA before each task. Update project state and append the decision log when making accepted UI or code decisions; never erase history.
- Never merge this draft release lineage into default `main` solely to make GitHub look current. Verify ancestry, schema28 policy, QA and historical upgrade separately before any integration.
- A new APK must be actually compiled, signer/package/version verified, and attached via a **direct in-chat file download**, not a GitHub run/release page. Never present an older APK as the new version.

## Current state

Signed CI run `41375198786` passed for APO RC74 (build code134). Physical device upgrade with archived surveys, research ZIP content, live GPS, field workflow and TalkBack remain unverified. Documentation updates alone do not constitute a newer APK.
