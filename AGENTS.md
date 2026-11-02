# APO development instructions (formerly LIKAS)

## Authoritative development baseline (2 November 2026)

**APO** is the public name of the offline-first Philippine biodiversity app. **Current Android development branch: `feature/apo-biotools-destinations-v135`**, verify HEAD before every new task. This branches from RC74 `feature/apo-biotools-v134`, draft PR #77, not merged to legacy `main`.

**Latest verified signed and directly delivered APK:** `0.27.3-rc75-apo-biotools-destinations`, code **135**, built app source `55e2864d4508bc512eafbdd9eb78eac4093030cc`, CI run `41376495935` (**SUCCESS**, 397 Python regression tests and JVM/Android build). Exact APK `APO-v0.27.3-RC75-BioTools-Destinations-code135.apk`, 11,751,480 bytes, SHA-256 `2083c18d98c2588c0d0382e7d6aaf0aa2885c890df91e78483c4ea710219263d`. AppId `org.phwildlife.survey.beta08`, original signer SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, SQLite schema **28** unchanged. Details: `docs/APO_RC75_BIOTOOLS_DESTINATIONS_SIGNED_QA_2026_11_02.md`. The binary is **not field-certified**; upgrade with existing records, GNSS, 320dp/200% font layout, TalkBack and ZIP contents remain unverified on physical phones.

Home has BioTools, PHLibrary, PHMaps and BioDiscover, with text-only Settings and contextual Resume. BioTools has Surveys, Projects & Stations, Field Records, Analysis & Exports, and Field Utilities. RC75 improves all five destinations using compact text-first rows, station search/coordinate validation, outcome summaries, careful scientific metric labels, and scoped utility explanations. Independent research export reuses the existing writer. PHMaps full atlas and BioDiscover research feed remain unimplemented.

## Preserve and verify

- Keep the approved, SHA-locked APO mountain emblem, separate borderless splash mark and native `#0B4B39` to `#021918` gradient. Do not reuse the retired frog as the launcher. Do not modify approved artwork without a new owner decision. See `docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md` and `docs/APO_STARTUP_SCREEN_V1_DESIGN.md`.
- Preserve app package and signing continuity, SQLite28 migrations, historical records, 714-species PAIC reference, 2020 ESA WorldCover-derived reference, scientific provenance, and survey outcomes Complete / Incomplete / Not Counted with accessible End Survey.
- Home and BioTools now use text-first navigation. Do not restore retired illustrated tiles or combine data/research outputs with device backup. Nested PHLibrary and Settings may retain legacy art for separate screen-by-screen review.
- Follow product/scientific governance in `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` on `main` until its active-branch migration is reviewed. The latest explicit APO naming and branding decisions and the current source/QA records supersede outdated brand descriptions within that historical spec.
- Read `docs/LIKAS_PROJECT_STATE.md`, `docs/LIKAS_DECISION_LOG.md`, this branch's README and relevant QA before each task. Update project state and append the decision log when making accepted UI or code decisions; never erase history.
- Never merge this draft release lineage into default `main` solely to make GitHub look current. Verify ancestry, schema28 policy, QA and historical upgrade separately before any integration.
- A new APK must be actually compiled, signer/package/version verified, and attached via a **direct in-chat file download**, not a GitHub run/release page. Never present an older APK as the new version.

## Current state

Signed CI run `41376495935` passed for APO RC75 (code135). Physical-device installation/upgrade with archived surveys, station edits, ZIP contents, GPS, fieldwork and TalkBack remain unverified. Documentation-only commits do not create a newer APK.
