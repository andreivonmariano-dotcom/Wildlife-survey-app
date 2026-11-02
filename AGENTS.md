# APO development instructions (formerly LIKAS)

## Authoritative development baseline (2 November 2026)

The owner selected **APO** as the public-facing identity, based on Mount Apo, for a Philippines-first biodiversity companion serving wildlife biologists and ecologists. **Use `feature/apo-startup-screen-v1-design` as the current Android development source**, not the older default `main` or a historical LIKAS RC branch. Verify its HEAD on each new task. Last verified signed APK is **0.27.3-rc72-apo-gradient-startup**, versionCode **132** from app-code commit `4ce953bbea100c1c6a90231434591ffb6f99f788`, with SQLite **28** and Android app ID `org.phwildlife.survey.beta08`. The current branch also includes later documentation-only commits. The signed APK is a development preview, not physically field-validated.

## Preserve and verify

- Keep the approved, SHA-locked APO mountain emblem, separate borderless splash mark and native `#0B4B39` to `#021918` gradient. Do not reuse the retired frog as the launcher. Do not modify approved artwork without a new owner decision. See `docs/APO_OFFICIAL_MOUNTAIN_ICON_LOCK_2026_11_02.md` and `docs/APO_STARTUP_SCREEN_V1_DESIGN.md`.
- Preserve app package and signing continuity, SQLite28 migrations, historical records, 714-species PAIC reference, 2020 ESA WorldCover-derived reference, scientific provenance, and survey outcomes Complete / Incomplete / Not Counted with accessible End Survey.
- The current Home menu still contains legacy `LIKAS` wording, frog illustration, and Fieldwork/Library/Settings cards. The next redesign is **Home/Main Menu**, a separate scoped app-code change from this branch. The startup identity already uses APO. Audit `WelcomeActivity.java`, `LikasPortalUi.java`, `LikasDesignSystem.java` and on-device layout behavior before changing navigation.
- Follow product/scientific governance in `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` on `main` until its active-branch migration is reviewed. The latest explicit APO naming and branding decisions and the current source/QA records supersede outdated brand descriptions within that historical spec.
- Read `docs/LIKAS_PROJECT_STATE.md`, `docs/LIKAS_DECISION_LOG.md`, this branch's README and relevant QA before each task. Update project state and append the decision log when making accepted UI or code decisions; never erase history.
- Never merge this draft release lineage into default `main` solely to make GitHub look current. Verify ancestry, schema28 policy, QA and historical upgrade separately before any integration.
- A new APK must be actually compiled, signer/package/version verified, and attached via a **direct in-chat file download**, not a GitHub run/release page. Never present an older APK as the new version.

## Current state

Signed CI run `41332798258` passed for APO RC72. Physical upgrade testing with archived surveys, live GPS, field workflow and TalkBack remains unverified. Documentation updates alone are not a new app build.
