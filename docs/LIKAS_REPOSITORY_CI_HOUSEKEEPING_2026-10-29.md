# LIKAS repository and Actions cleanup

**Date:** 29 October 2026 (Asia/Manila)  
**Branch:** `feature/likas-core-fieldwork-post-rc4`  
**Current app:** RC41 candidate, `0.27.3-rc41-field-readiness-preview`, versionCode 101  
**Scope:** GitHub Actions workflow triggers, tracked text/workflow clutter, generated-file prevention. No biological data, approved assets, application code, app identity, SQLite schema, or signing material changed.

## Queue investigation

GitHub's API shows the latest LIKAS Scientific Core QA jobs staying in **queued** state, without a reported runner assignment. At the initial inspection, RC41 run `40888849314` was queued. The workflow run counter had exceeded **2,200** for this one QA workflow. GitHub reports approximately **4,134 KiB** repository size and the feature branch is approximately **1,782 commits ahead** of the divergent default `main`. These figures show a large volume of development pushes, but do not independently prove an Actions quota or runner shortage.

The active-branch workflows showed three costly automatic jobs for ordinary application edits:
- `core-science-qa.yml`: scientific-source tests, 17+ Android preparation/build steps, signed APK artifact, originally on every branch push including documentation-only pushes.
- `library-android-smoke.yml`: Android emulator smoke, 40-minute timeout, originally on every branch push.
- `rc7-device-emulator-qa.yml`: Android emulator upgrade/backup tests, 50-minute timeout, originally on application/test/asset pushes and depending on an old pinned RC7C APK artifact.

GitHub's public status page reported Actions **operational** when checked on 29 October 2026. Older Actions incidents from October 2026 were resolved. No accessible repository tool exposes account billing/minutes, runner inventory or the server-side reason for a queued run. The exact account-level queue cause remains unverified. A queued job without a runner cannot generate an APK.

## Changes made

| Path | Action |
|---|---|
| `.github/workflows/core-science-qa.yml` | Added cancel-in-progress per ref, excluded docs-only/README/.gitignore pushes, and manual dispatch. Renamed future APK artifacts dynamically from Gradle version and code. Reduced new APK artifact retention from 14 to **5 days**. |
| `.github/workflows/library-android-smoke.yml` | Removed automatic pushes, retained manual `workflow_dispatch` and concurrency guard. |
| `.github/workflows/rc7-device-emulator-qa.yml` | Removed automatic pushes, retained manual `workflow_dispatch`. The old pinned RC7C artifact must be reviewed before invoking this test. |
| `.github/workflows/rc7-inat-diagnose.yml` | Deleted obsolete one-off RC7 API diagnostic workflow. Source diagnostic script remains. |
| `.github/workflows/rc7-license-candidates.yml` | Deleted superseded old RC7 candidate collector workflow. Source collector script remains. |
| `.github/workflows/rc7-photo-review.yml` | Deleted one-off RC7 contact-sheet workflow. Source scripts and rights approval lockfiles remain. |
| `.gitignore` | Added APK/AAB/APKS, keystore, cache, emulator-capture, image-review and temporary-log exclusions to reduce accidental future repository bloat and signing-key exposure. |
| `README.md` | Replaced **48,364** bytes / 533 lines of appended historical releases and outdated v0.15 / SQLite 11 installation notes with a **3,368**-byte current RC41 overview, accurate source status, and canonical documentation links. Original text is retained in Git history. |

The scientific-core QA still verifies pinned taxonomy, the 49 approved iNaturalist images and other licensed/authorized offline media, Android package/signing identity, Python science/source contracts, JVM tests and APK compilation. Manual emulator acceptance remains mandatory before declaring field readiness.

## Storage and preservation

GitHub repository size was approximately **4 MB** when checked, not tens or hundreds of megabytes. Large APKs in GitHub Actions are **artifacts**, not necessarily tracked repository files. Setting a five-day retention for future artifacts reduces new artifact storage but does **not** delete existing artifacts. The available GitHub integration has no workflow-run cancellation or artifact deletion action.

Deleting tracked files removes them from the current branch snapshot, not historical commits. The goal here is a smaller current checkout and cleaner workflows, not a dangerous history rewrite. No Git force-push, branch deletion, artifact cleanup, or scientific content deletion was performed. Existing signed research APKs, photo rights, old surveys, source packs, status review ledgers, approved forest and frog artwork are preserved.

## Remaining checks

1. Confirm the **latest** scientific-core workflow is picked up by a runner and builds the versionCode101 APK with the dynamic artifact name.
2. If new runs remain queued without a runner, inspect GitHub repository **Actions** settings, account billing/usage, runners, and the workflow run UI for a specific message. Distinguish a queue problem from failed science tests after a runner starts.
3. Run the deliberate Android 14 emulator suites after checking their baseline compatibility and ensure the RC39-to-RC41 upgrade succeeds with backed-up field data.
4. Check future GitHub Actions artifact storage and retention in repository settings. Do not indiscriminately delete signed reference baseline artifacts needed for migration tests.

**Result at report creation:** repository organization and redundant workflow cleanup committed; signed RC41 APK not yet verified.
