# LIKAS active development instructions (RC4 lineage)

**Mandatory baseline:** LIKAS v0.27.3 RC4, versionCode 49, SQLite 27, package `org.phwildlife.survey.beta08`. The active development branch is `feature/v0.27.4-layered-sprite-player-stage`, stacked on `feature/v0.27.3-startup-living-forest`. Default `main` still holds historical v0.15 and must never be interpreted as the latest app source.

Before ANY code or APK work:
1. Verify GitHub branch HEAD, latest RC4 descendant, `docs/LIKAS_PROJECT_STATE.md` and append-only `docs/LIKAS_DECISION_LOG.md`.
2. Run `python3 scripts/check_likas_active_lineage.py --stage` with full Git history. Check the pinned RC4 ancestry and Android package, SQLite, versionCode and 5 FPS sprite contracts. For release, use `--release`.
3. Never build an APK intended as an upgrade from `main` v0.15 or from the obsolete `feature/rainforest-parallax-v2` (closed PR #37). Never downgrade or uninstall the existing field app to make an APK install.
4. Keep `LikasSpriteForestView.java` disconnected until the reviewed 1,488,772-byte Prototype D archive is available, verified, and included in the APK. A compile-successful player without media is not a working rainforest.
5. Preserve the approved wordmark-only landing, “Explore. Record. Conserve.”, top-right installed-version label, contextual Resume survey and Open LIKAS actions. Keep the pixel frog as the launcher icon, not a startup overlay.
6. Preserve user records, SQLite schema 27 migration history, field-workflow exit states, GPS provenance, photos, audio, and research exports. Test these before declaring release.
7. After each decision, source change, artwork, blocker or build, update `docs/LIKAS_PROJECT_STATE.md` and append a `docs/LIKAS_DECISION_LOG.md` entry. Do not overwrite previous outcomes.
8. For every APK/asset deliverable use a verified direct in-chat downloadable file. A GitHub link is supplemental, not the artifact.

**Source of truth:** The latest explicit owner decision, this branch's verified code, RC4 and Prototype D QA files, and `docs/LIKAS_RELEASE_LINEAGE_POLICY.md`. Concept files and earlier conversations are supporting records. No 60 FPS claim for the owner-selected 5 FPS individual sprite player.

**Release gate:** Increment versionCode above 49, validate exact approved sprite archive fingerprint, preserve established signing certificate, pass CI, perform physical Android install/upgrade and frame/energy checks, and obtain design approval. Until then keep PR #36 draft.
