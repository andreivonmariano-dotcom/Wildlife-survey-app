# LIKAS RC4 -> v0.27.4 | Startup and Sprite-Player Audit

**Date:** 28 October 2026 (Asia/Manila)
**Scope:** Q1 continue sprite work from actual v0.27.4 branch; Q2 audit current RC4 startup; Q3 prevent wrong-baseline builds.
**Branch lineage:** `feature/v0.27.3-startup-living-forest` at `458e786827ba73bebabf5c373e2080015c5fd38c` → `feature/v0.27.4-layered-sprite-player-stage` (draft PR #36). GitHub compare confirms the stage is ahead of its RC4 parent, not behind it.

## Source audit of RC4 landing

| Property | Actual current code | Required direction | Result |
| --- | --- | --- | --- |
| Android identity | `org.phwildlife.survey.beta08`, version 0.27.3-rc4, code 49 | Preserve identity and advance code for the next installable release | PASS baseline |
| Field DB | SQLite 27 and migration `upgradeTwentySeven` | No records or schema overwritten for art | PASS source |
| True startup entry | `LikasLaunchActivity` is launcher | Keep Home/Resume in native controls | PASS source |
| Branding | LIKAS wordmark, Explore. Record. Conserve., top-right PackageManager version | No frog overlay, no hardcoded build badge | PASS source |
| Actions | Open LIKAS opens `WelcomeActivity`; Resume survey checks live event with a read-only query | Protect unfinished events | PASS source, phone pending |
| Forest at runtime | `new LikasForestView(this)` | New independent sprite-frame animation | NOT SWITCHED |
| Approved artwork | Original HD day/night WebP files present | Preserve reviewed source compositions, clean duplicate birds and clouds | PARTIAL cleanup documented |
| Component player | `LikasSpriteForestView` compiled but unused | 28 sprite groups at 200ms (5 FPS) | STAGED |
| Media archive | `likas_sprite_assets.zip` absent from GitHub stage | Import exact reviewed Prototype D pack, verify SHA | BLOCKED |
| Crossfade | New player has 05:30–06:30 dawn and 17:30–18:30 dusk blending | Phone-local gradual transition | CODED, not runtime verified |
| Device performance | No installed Prototype D player tested | Screen-size, jank, memory, battery, reduced motion | NOT VERIFIED |

## Renderer defects found and corrected in source stage

- A missing/invalid binary archive previously triggered repeated failed load attempts on every sprite tick. The player now records the load failure and logs it, rather than retrying indefinitely.
- A detached view could clear its handler during an active background tick. The tick now uses a captured handler and verifies `alive` before drawing and rescheduling.
- Window visibility events could create overlapping scheduled animation loops. The player now uses a single queued sprite-tick gate (`AtomicBoolean`) with controlled replacement on visibility restoration.
- Any in-flight bitmap decode and view detachment still require handset stress tests. WebP `BitmapRegionDecoder` behavior on API 26/29/34 remains unverified.

## Release-lineage protection

- `config/likas_active_lineage.json` pins the RC4 ancestor, version minimum 49, schema 27, package ID, 5 FPS clock, approved Prototype D asset hash and first future release code 50.
- `scripts/check_likas_active_lineage.py` fails if a branch lacks RC4 ancestry or uses code 18, old schema, wrong app ID, changed startup controls, or missing asset on an activated player.
- The existing CI has `fetch-depth: 0` and executes the guard before JVM/Python/Android builds.
- The CI tests verify rejections of the obsolete v0.16 code, removed schema, wrong package, missing archive and premature sprite activation.
- Feature PR builds require the old signing certificate SHA-256 and never generate a new keystore. A feature-branch push does not replace a stable release asset.
- **Scope of enforcement:** A workflow guard only applies to branches and builds that use it. Organization-level required status checks, merge rules and signing reviews are still needed for protection across unrelated branches.

## Remaining prerequisites before actual v0.27.4 APK

1. Obtain the exact **Prototype D** binary `android/likas_sprite_assets.zip` (recorded SHA-256 `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`). It is absent from GitHub and not available among accessible current Project files.
2. Inspect individual static plates and moving sheets, including baked-in distant clouds, bird silhouettes and waterfall body motion. The user has not yet approved a final pack.
3. Import approved archive, run ZIP integrity and manifest checks, connect `LikasSpriteForestView`, version bump above code 49, and perform new CI tests.
4. On real Android devices inspect WebP region decode and alpha occlusion, splash/ripple loops, twilight crossfade, animation 5 FPS pacing, heat and memory.
5. Back up RC4 survey records, test in-place upgrade with original signing key, End Survey and Resume, GNSS provenance, audio/photos and scientific ZIP export.
6. Deliver a **newly built verified APK** as a direct chat download, not a previous release or GitHub-only link.

**No new v0.27.4 APK was created in this audit.** A successful source build is not a successful 5 FPS sprite playback test.

## Follow-on Q1/Q2/Q3 | 28 October 2026 | Asset recovery and merge checks

**Asset recovery:** Searched all accessible conversation/Project files, the personal Library, the local working filesystem, the private GitHub repository tree and the owner's connected Google Drive. The exact `LIKAS_Layered_Sprites_5FPS_Prototype_D.zip` or its nested `android/likas_sprite_assets.zip` is not available in these sources. The recorded 1,488,772-byte ZIP and SHA256 are provenance identifiers, not evidence of locally available binary. Do not silently substitute the separate 7.5 MB parallax/browser prototype or new procedural art. No asset was committed or runtime sprite connection performed. The Prototype D art is a **review candidate**, not a finally accepted image pack.

**Per-layer tester:** `scripts/inspect_likas_sprite_layers.py` can audit all 28 independent layers across day/night, preserving component kind and atlas identity. It checks ZIP CRC, recorded SHA256, all fixed/rocks plates, 4 waterfalls and river per mode, expected 20/40-frame lengths for 5 FPS motion, geometry and sheet coverage. With Pillow it compares *every RGBA frame* for movement and produces five-frame review strips for each component. `scripts/test_likas_sprite_layer_audit.py` provides deliberately **synthetic** 28-layer fixtures that exercise missing-layer and unapproved-hash rejection. These passing test fixtures are **not tests of real Prototype D pixel art**. Once the actual archive is recovered, run:

```bash
python3 -m pip install Pillow
python3 scripts/inspect_likas_sprite_layers.py --archive /path/to/likas_sprite_assets.zip --output /path/to/review_strips
```

Do not use `--skip-hash-check` for owner art.

**Trusted PR gate:** `scripts/check_pr_against_rc4.py` and `.github/workflows/likas-pr-lineage.yml` are staged in `main`, RC4 and v0.27.4 branches. The new `pull_request_target` checks read **trusted base-branch code only** and inspect PR-head files through Git objects. They never execute PR-supplied scripts. A first iteration incorrectly expected a sprite player in RC4 itself, and was corrected. PR #35 (RC4) and #36 (v0.27.4) subsequently passed source-lineage checks; latest run details are in GitHub Actions.

**Limits:** GitHub returned `403 Upgrade to GitHub Pro or make this repository public` for private repository rulesets and `403 Resource not accessible by integration` for existing branch protection. These checks run but are **not verified as required merge checks across all branches**. The account's GitHub plan and repository settings must permit protection before claiming a non-bypassable rule. Existing historical PRs targeting other old branches do not all inherit the new workflow.

**Branch synchronization:** The RC4 branch received the trusted read-only PR checks. The v0.27.4 branch was merged with that updated RC4 history through commit `93e705bdc168249eb2384d2ceacec287982c77ad`; GitHub compare again reported **0 commits behind RC4**. No Android app entrypoint, database, species catalog or exports were modified by this merge.

**Release remains blocked:** No user-approved binary pack exists in this session. Native sprite atlas playback and visual quality are unverified on Android. No newly versioned v0.27.4 APK was produced or distributed.
