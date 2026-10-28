# LIKAS | Active release lineage and version-control policy

**Policy ID:** VC-01
**Recorded:** 28 October 2026, Asia/Manila
**Status:** Active for RC4 descendants, including draft PR #36.
**Motivation:** A v0.16 preview was mistakenly compiled from old `main` (v0.15) after RC4 (v0.27.3) already existed. PR #37 was closed, unmerged.

## Canonical branch and release baseline

- Actual current installed candidate: `feature/v0.27.3-startup-living-forest`, commit `458e786827ba73bebabf5c373e2080015c5fd38c`, app `0.27.3-rc4`, versionCode **49**, SQLite **27**.
- Next source stage: `feature/v0.27.4-layered-sprite-player-stage`, descendant of RC4, **not yet a new installable release**.
- Historical default `main`: v0.15.0-beta. It is NOT the highest-version active Android branch. Do not infer release status from a default-branch label.
- Package ID remains `org.phwildlife.survey.beta08`. The existing signing certificate must match any in-place APK upgrade.
- Approved sprite target: 5 FPS per independent component (200 ms), no full-frame displacement or artificial Canvas-only scenery replacement. Original time-coherent day/night concept, fixed terrain, moving sprites.

## Mandatory pre-build checks

`config/likas_active_lineage.json` pins the base commit, app ID, schema, minimum version, approved 5 FPS timing and Prototype D fingerprint. `scripts/check_likas_active_lineage.py` verifies the live source and uses `git merge-base --is-ancestor` with full Git history. Every current CI build runs the check before tests and assembly. An unrelated/older branch fails, including the obsolete v0.16. This guarantee applies only to workflows that execute the guard.

Staged source may retain versionCode 49 **only if the new player is not connected**. An installable v0.27.4 sprite release requires all of:

1. Ancestor RC4 commit present in source history, no unexplained regressions, and versionCode **at least 50** with a new versionName.
2. SQLite schema 27, all previous upgrade paths and app package ID preserved.
3. Native player connected only after the full approved sprite asset ZIP is checked into `app/src/main/assets/likas_sprite_assets.zip`.
4. SHA-256 of that asset equals `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`. Any revised artwork requires fresh owner review and a deliberate fingerprint update.
5. Manifest has 28 independent moving components, day/night fixed plates, foreground rock occlusion, valid atlases and frame geometry. Check every binary with local validation and on a real Android decoder.
6. CI Python/JVM tests and APK assembly pass, using the established signing key. No new keystore for an upgrade.
7. Real handset installation over RC4, retained offline surveys and archives, screen navigation, reduced motion, GPS, media and exports checked.
8. Direct actual APK file delivered in chat. No GitHub-only download as the primary delivery.

## Startup identity

`LikasLaunchActivity` remains the true launcher. Show wordmark **LIKAS**, subtitle **Explore. Record. Conserve.**, dynamic top-right version, **Open LIKAS** to Home, and conditional **Resume survey**. The approved frog is the launcher icon, not a second startup illustration. Never allow a decorative renderer to create, delete or modify survey records.

## CI and release restrictions

- On the active feature branch, the existing GitHub Actions workflow now checks complete Git ancestry, source contract and sprite fingerprint before the remaining Python/JVM/Android build.
- PR builds require the established debug signing certificate fingerprint `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Do not quietly generate a replacement.
- Automatic publication on every `main` push is suppressed in this feature branch's workflow. A release requires deliberate approval and the `--release` guard.
- Branch protection, required status checks and owner reviews must be configured in repository settings for rules to be truly non-bypassable across other independent branches. Source-controlled CI alone cannot police a branch that never adopts the workflow.

## Recordkeeping

Record every new art pack, selected release commit, actual APK checksum and signing digest, CI run, field test and approval in `docs/LIKAS_PROJECT_STATE.md` and append only to `docs/LIKAS_DECISION_LOG.md`. If the latest available branch differs from these pinned assumptions, stop release work and reconcile the project record first.

## Release acceptance status

Current status is **source staged, not APK shipped**. `LikasSpriteForestView.java` exists, but binary Prototype D media is unavailable in the current repository and accessible conversation files. RC4's Canvas renderer remains active until the binary package and physical verification pass.
