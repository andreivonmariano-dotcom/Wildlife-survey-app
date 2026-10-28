# LIKAS: Current Project State and Next-Chat Handoff

## 28 October 2026 | Q1 Q2 Q3: asset recovery, layer-by-layer testing and cross-branch PR gate

- **Source baseline:** `feature/v0.27.4-layered-sprite-player-stage`, descended from RC4 versionCode 49, SQLite 27. Following trusted workflow additions on RC4, synchronized histories using merge `93e705bdc168249eb2384d2ceacec287982c77ad`, with 0 commits behind RC4.
- **Q1 recovery:** Exact Prototype D archive `android/likas_sprite_assets.zip` (recorded 1,488,772 bytes; SHA256 `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`) was **not found** in accessible Project/conversation files, personal Library, local files, GitHub tree, or connected owner's Google Drive. The asset is a recorded review candidate, not final owner-approved artwork. Runtime player remains disconnected. No fabricated/replacement art and no v0.27.4 APK.
- **Q2 sprite audit:** Added `scripts/inspect_likas_sprite_layers.py` to inspect every individual component, all 28 sheets, RGBA frame variation, expected 5 FPS 20/40-frame counts, static/rocks geometry, mask/alpha and atlas bounds; it emits review strips per sprite if the real ZIP is supplied. Added `scripts/test_likas_sprite_layer_audit.py` with synthetic 28-layer fixtures. CI source/regression checks passed on commit `2ebbeeb16238f8be72ac9f652e36e759a575316c` (run `40634485653`). The actual source art has **not** been pixel-tested.
- **Q3 controls:** Created trusted PR-head Git-object reader `scripts/check_pr_against_rc4.py` and `.github/workflows/likas-pr-lineage.yml` on `main`, RC4, and v0.27.4. Branch checks pass on the current RC4 and v0.27.4 PRs. First iteration rejected RC4 itself for lacking an intentionally unconnected sprite class; corrected on all three branches. The feature branch also retains its existing full-history lineage and signing checks in Android CI.
- **Enforcement limit:** GitHub returned private repository rulesets `403 Upgrade to GitHub Pro or make this repository public` and branch protection `403 Resource not accessible by integration`. The workflow checks execute, but **repository-wide mandatory branch/merge protection was not enabled**. Do not claim non-bypassable checks until settings and entitlement permit them.
- **Verification:** GitHub Actions run `40634775181` passed trusted PR gate on synchronized stage head `93e705bd...`. CI build on merge commit was in progress during this note; check latest run before claiming final status. No physical handset sprite, FPS, power, TalkBack or database upgrade test was performed.
- **Next:** Recover the *exact* Prototype D source ZIP, inspect each real day/night sprite and approve the corrected appearance; import with checksum, connect player, increase versionCode >= 50, pass CI plus physical phone QA, and provide an actual new APK directly in chat. PR #36 stays draft. Details: `docs/QA_v0_27_4_ACTIVE_BRANCH_SPRITE_AUDIT.md`.

---


## 28 October 2026 | Verified RC4 lineage, active-branch safeguards and Prototype D audit
- **Owner confirmed Q1/Q2/Q3:** Continue from the real `feature/v0.27.4-layered-sprite-player-stage` branch, audit the RC4 startup, and institute strict version-control gates after the obsolete v0.16 incident.
- **Verified ancestry:** `feature/v0.27.4-layered-sprite-player-stage` descends from RC4 commit `458e786827ba73bebabf5c373e2080015c5fd38c`; it was ahead by 12 commits and behind by 0 at the start of this task. The default `main` is historical v0.15 and is not the active development baseline. Closed PR #37 remains obsolete.
- **Source audit:** `LikasLaunchActivity` correctly preserves the LIKAS wordmark, Explore. Record. Conserve. tagline, dynamic top-right version, contextual Resume survey and Open LIKAS action. RC4 still instantiates `LikasForestView`. The staged `LikasSpriteForestView` is intentionally disconnected. No Prototype D atlas ZIP is bundled, so **no v0.27.4 APK with layered sprites exists**.
- **Version-control additions:** Added `AGENTS.md`, `docs/LIKAS_RELEASE_LINEAGE_POLICY.md`, `config/likas_active_lineage.json`, `scripts/check_likas_active_lineage.py`, and `scripts/test_likas_active_lineage.py`. This pins RC4 ancestry, versionCode at least 49, app ID, schema 27, accepted 5 FPS per-sprite timing and expected Prototype D pack checksum; release activation requires versionCode at least 50 and matching ZIP.
- **CI:** Updated existing branch workflow to fetch full Git history, reject wrong release ancestry before build, and reject PR builds signed by a different certificate. No new signing key is generated on PR runs. The initial Python test syntax error was corrected. **GitHub Actions run `40631698579` completed successfully** for branch commit `7e94907ecc706e80173851837ef4f954f8e4ca2f` (source checks, Python/JUnit and Android build). This verifies code compilation, not runtime sprite animation. The current app remains `0.27.3-rc4` and the staged sprite player is unconnected.
- **Renderer reliability:** Changed native 5 FPS source to fail once and log missing/invalid sprite assets, avoid handler teardown races, and serialize visibility-triggered frame scheduling via `AtomicBoolean`. No active startup class or survey/SQLite code changed.
- **Detailed review:** `docs/QA_v0_27_4_ACTIVE_BRANCH_SPRITE_AUDIT.md` describes the code-vs-design audit and remaining image artifacts. Physical Android WebP atlas decode, battery, frame cadence, accessibility, in-place upgrade and field survey regression are not verified.
- **Blocker:** Exact Prototype D `android/likas_sprite_assets.zip` is absent from repository and current accessible Project files. Recorded SHA-256 `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`. Do not substitute generated art or activate the sprite class without it.
- **Next:** Verify final CI run and pin its commit; retrieve/import and inspect the exact approved sprite ZIP, connect it only with valid media, increment versionCode >49, run full CI and on-device research-data regression, then provide a newly built direct downloadable APK. PR #36 remains draft.

---


## Latest development stage | 28 October 2026, Asia/Manila | Prototype D waterfall refine + Android sprite source
- Owner requested Q1/Q2/Q3 together: refine each waterfall sheet, clean duplicated static detail, and build an Android sprite player from separate 5 FPS parts with phone-local day/night selection and battery-aware playback.
- **Local Prototype D created:** `LIKAS_Layered_Sprites_5FPS_Prototype_D.zip`, 9,350,494 bytes, SHA-256 `3eaf56a026a070eccf498857b1510171c5e892dee22f3932f663667b36a93aab`. Its `android/likas_sprite_assets.zip` is 1,488,772 bytes, SHA-256 `def85e227587fdadee75956b6f105b57feb38f2bff27c6889c1f1edbfc042498`. Complete with individual transparent PNG frames, 28 component sprite sheets, day/night stills, frame editor/repack scripts, Android Java player, offline layer viewer and 5 FPS day/night MP4 previews.
- **Q1:** four waterfalls each retain original water contours and textured bodies with distinct 5 FPS rising/splash keyframe edits and independently traveling pixel strands. The script-generated editing is not equivalent to manual frame-by-frame professional animation.
- **Q2:** localized static sky-bird cleanup completed. Remaining distant background mist and embedded deep-canopy detail are intentionally untouched because erasing entire parts would deform the source composition. This is a **partial** cleanup.
- **Q3 staged in GitHub:** new `app/src/main/java/org/phwildlife/survey/LikasSpriteForestView.java` on branch `feature/v0.27.4-layered-sprite-player-stage`, derived from `feature/v0.27.3-startup-living-forest`. The class uses compressed WebP sprite atlases, background worker decoding, 5 FPS frame intervals, native nearest-neighbor alpha compositing, device-local 05:30–06:30 dawn and 17:30–18:30 dusk crossfades, plus reduced-motion, battery-saver and hidden-screen pause.
- Regression source check: `scripts/test_staged_pixel_sprite_player.py`; QA: `docs/QA_LIKAS_PROTOTYPE_D_5FPS.md`. GitHub CI run 40622163888 passed after fixing the Android View.animate() naming collision. The following source change ad2402d5e76e92478b4b0a5ba71e88389cc239d4 corrects twilight compositing; CI validation for that change is tracked on PR #36. Source class remains deliberately **unconnected**, since GitHub does not yet contain the 1.5 MB binary pack. No new Android APK includes it. Existing `v0.27.3-rc4` APK still has the rejected old scene and must not be relabeled.
- **Next release gate:** import Android sprite binary archive, connect the new view, advance versionCode, run CI and test actual Android decoder/animation, signing identity, database continuity and research exports. Then provide a new direct APK only after binary verification.
- The startup frog stays the installed icon, not a backdrop overlay; native LIKAS title, tagline, version and Resume/Home buttons stay unchanged.

---

## Latest sprite-art milestone | 28 October 2026, Asia/Manila | 5 FPS independent pixel elements
- Owner explicitly requested Q1 Q2 Q3: refine waterfall flow, give river its own continuously looping current sprites, and isolate more cloud/foreground foliage motion. The **accepted target frame timing is 5 FPS per animated portion (200ms)**, not 16/24 FPS whole-scene warping.
- Current conversation generated **Prototype C**: `LIKAS_Individual_Rainforest_Sprites_5FPS_Prototype_C.zip`, size **14,972,272 bytes**, SHA-256 `43a024066310dcf96c000062a0d13d762c653379598a7a98cd1f8846297f7c48`. This is a separate editable asset pack, not a built APK or approved final artwork.
- Manifest declares **28 separate component instances** across matched day/night scenes, **760 transparent PNG keyframes**, per-component XY positions, and sprite sheets: four waterfalls per state (20 frames each, 4-second loops), one river (40 frames, 8-second loop), three drifting clouds (40 frames each), three canopy cutouts (20 frames each), **two additional pixel-painted foreground fern clusters** (20 frames each), and daytime birds / nighttime fireflies (40 frames).
- Fixed mountains, cliffs, trunks, rock foreground and textured waterbed remain stationary. Animated waterfall streams, river reflections/foam, cloud positions, foliage sway and wildlife have their own frames; the full scene is never geometrically warped.
- Included self-contained `reference/` source assets, `build_assets.py`, `manifest.json`, 28 sheet PNGs, individual frame folders, `index.html` offline layer inspector, scene PNGs, day/night 5 FPS MP4 previews and source review notes. Archive tested using Python ZipFile `testzip()`; all 28 component groups passed sampled frame uniqueness and transparency checks.
- Quantitative loop validation on the 8-second previews: mean 5 FPS adjacent-frame absolute pixel differences day **0.881** vs wrap **0.928** (1.053×); night **0.349** vs wrap **0.408** (1.170×). Visual motion and actual on-device performance remain unverified.
- **Art limitations:** waterfall flow and river bands are procedurally drawn inside source-image silhouettes, not individually hand-painted by an animator; source waterbed remains painted beneath independent current sprites; the former B base still has some baked-in distant cloud/bird and canopy marks. This is an inspectable asset specification, not a claim of photorealistic fluid motion. It is not yet committed as Android app binaries.
- Owner's previous rejected deformation and overlay-only previews remain **superseded**. Existing Android `v0.27.3-rc4` still uses the old Canvas render and **does not contain these assets**.
- **Next step:** review isolated asset strips and 5 FPS day/night videos for quality and layering; refine remaining static distant cloud/bird artifacts and waterfall keyframes. After owner approval, import the asset pack into an Android sprite player, test battery saver/reduced motion/time-based day-night crossfades and build a new APK with signing continuity. Do not reuse an earlier chat's file link without verifying availability.

---

## Latest visual implementation | 28 October 2026, Asia/Manila | Prototype B: waterfall, river, cloud and canopy pass
- The owner selected Q1/Q2/Q3: distinct waterfall flow frames, independent river currents and foam, and independently movable canopy/cloud parts. The previous Prototype A failed visual standards because too much water remained static and clouds/birds overlapped baked background.
- Created `LIKAS_Rainforest_Layered_Prototype_B.zip` in the current chat, SHA-256 `0c14d4bcfa37099c552e283ac96cfc2f1c9604427de46bea33ffdca0689d8794`, 7,249,181 bytes. The archive includes editable and cropped transparent waterfall/river PNG frames, separate cloud and canopy PNGs, static scenery/rocks, source stills, an offline time-aware browser preview and Python build scripts.
- New frame details: 4 separate waterfalls with 16 distinct procedural pixel-painting frames each per day/night state, river with 32 distinct moving-current/foam frame images per lighting state, 3 independent cloud sprites and 3 independent foreground canopy cutouts per state. Two 432x756 16fps four-second review videos were rendered and validated, including waterfall and river detail clips and a comparison contact sheet.
- Important provenance: the waterfall contours are grounded in source-image masks; each waterfall's changing color bands and strands were generated with pixel-art drawing code, **not manually hand-painted by a specialist**. The river retains the approved original dark-water base with separate moving current/reflection sprites, not a fully physically simulated water surface. Some far clouds and leaves remain baked into the source. Four-second loop resets and low-end device motion remain unverified.
- Validation: ZIP integrity check passed, both MP4s playable with four-second duration, source frames differ over time. **No Android APK was produced, no binary art committed to GitHub and no phone QA occurred.** Do not claim shipped animation or finished fully separated forest.
- Next action: review Prototype B waterfall and river closeups, improve loop boundary and preserve masked edge quality, extract/move remaining canopy/cloud groups. Once approved, migrate asset and playback source into a new Android branch, use time-of-day crossfade with a static reduced-motion fallback, run CI/device QA and deliver a newly verified signed APK.
- Source branch where project decisions live: `feature/v0.27.3-startup-living-forest`. Default `main` was observed at `f1ef6583647d05ae3cbbb915b2f54370150be33c` this session.

---


## Current direction | 2026-10-28, Asia/Manila | Separate static and moving sprite layers
- Owner rejected the prior pixel displacement/warping animations as visually unacceptable. **Do not reuse** image-warp, canvas-only simulated ripple or generalized overlay techniques as the final rainforest animation.
- New design is a **true layered pixel scene**: immutable terrain, mountains, trunks and foreground rocks, plus transparent independent sprite-frame images for river, waterfalls, clouds and wildlife, with separate swaying leaf-cluster sprites. Match the approved day/night Philippine rainforest composition and retain the no-logo-in-background rule.
- A proof-of-concept asset pack was generated in this conversation: `LIKAS_layered_rainforest_prototype_A.zip` (432×756 frames, 48 independent frames per lighting mode, 12 sprite frames per second), with offline `index.html` layer toggles, `build_scene.py`, source stills, sprite frame PNGs, and day/night video previews. Dawn/dusk uses local device time in the preview; a manual override is strictly for visual review.
- **Visual QA is not complete.** Waterfall and lake sprites are cut from the original art with changing native detail frames, not fully redrawn by a human animator. Some clouds and birds are still embedded in the static source, and mask boundaries and canopy sprite pivots need art-direction review. Source stills and independent dynamic assets have not been pushed as binaries to GitHub.
- **Not integrated into APK.** Latest v0.27.3 RC4 test APK continues to use the rejected prior runtime and must not be advertised as containing this layered design. No new APK compiled or device tested for Prototype A.
- Immediate next: review the new layers/videos for quality; refine waterfall keyframes, remove fixed cloud/bird remnants, and polish masked rock edges. After owner approval, import sprite assets and timeline logic into Android, implement reduced-motion and app pause/resume, test the signed APK on devices, and provide a fresh direct downloadable APK.
- Source of truth for branch status is `feature/v0.27.3-startup-living-forest`; default `main` verified as `f1ef6583647d05ae3cbbb915b2f54370150be33c` in this session. Archive files exist only in the current conversation and are not safely transferable via historical sandbox links.

---


## Latest visual direction | 28 October 2026, Asia/Manila | Pixel rainforest concept and frame-loop prototype
- Owner approved the new Philippine rainforest visual concept and requested a coherent **retro pixel-art transformation**, actual animated water, waterfall streams, rustling foliage and travelling clouds, plus automatic day/night selection based on the phone's clock.
- The preceding `v0.27.3-rc4` overlay-only renderer does not meet that request. Its static WebP layers with generated Canvas overlays are **superseded** as the target design. Do not describe RC4 as the new pixel-animation build.
- The current conversation produced a **word-free pixel-art diptych** with matched day/night scenery, a pair of silent H.264 MP4 loops (432×756, 24 fps, 144 frames, six seconds), PNG static fallbacks, and an offline HTML concept preview. Animated frames are generated by pixel remapping of *actual scene areas* (clouds, foliage, waterfalls, river), not Canvas doodles over an unmoving background.
- Night lighting: 18:30–05:30, daytime: 06:30–17:30, with 1-hour dawn/dusk crossfades. Phone local time controls selection. Preview-only manual lighting control is NOT a production requirement.
- An independent `LikasPixelMotionView.java` (TextureView + MediaPlayer, saved clock selection, pause/offscreen handling, Android reduce-motion/power-saver fallback) and `install_into_likas.py` helper were packaged with the assets. The complete pack is `LIKAS_living_pixel_forest_animation_pack_24fps.zip` generated and verified in the current chat.
- **Status separation:** Art, video loops, source, preview and ZIP **created and locally verified**. These new binary files are **not yet in GitHub**, the Android integration code has **not been compiled on CI**, and **no new APK** containing this pixel animation has been built. Existing RC4 APK still uses the older forest and Canvas overlays.
- **Immediate next action:** Import the ZIP's day/night MP4 and PNG assets into `app/src/main/assets/likas_pixel/`, add `LikasPixelMotionView.java`, change `LikasLaunchActivity` to instantiate it, keep native LIKAS wordmark/tagline/version/controls, increment versionCode, build with the established signing certificate, run tests, and provide a **fresh directly downloadable APK** after verifying animated playback on a real phone. Inspect river cloud and leaf motion for visual stability; do not claim finished app integration until it is real.
- The user's approved pixel-frog icon remains an Android launcher icon only. Retain species, effort, GPS/altitude, audio, photos, survey lifecycle and exports unchanged.

---


## Latest approved direction | 2026-10-27, Asia/Manila (RC4 living forest startup)
- Current working branch: `feature/v0.27.3-startup-living-forest`, based on startup-only PR #34 `ui/likas-startup-screen-r1-20261027`. Older `main` remains at `f1ef6583647d05ae3cbbb915b2f54370150be33c` (historical app baseline). Version `0.27.3-rc4`, versionCode 49, Android ID `org.phwildlife.survey.beta08`, schema 27 unchanged.
- Owner superseded RC3 branding placement: **Remove frog from startup only**, retain exact approved frog as launcher icon; display **LIKAS** with **"Explore. Record. Conserve."**; show the dynamically detected installed version in a compact **top-right** chip.
- Owner explicitly requested genuine animated water/lake, falling waterfalls, travelling clouds, daytime birds, nighttime fireflies, moving glowing sun/moon. Implemented in `LikasForestView.java` as new Canvas motion layers anchored to the approved high-resolution panoramas, with dawn/dusk and battery saver/reduced-motion handling. Source imagery remains untouched.
- `Open LIKAS` and contextual `Resume survey` remain the only startup actions. Survey data, taxonomy, GPS, elevation, photos/audio and scientific exports remain untouched.
- Status: **coded and Android CI verified, not yet field-tested**. PR #35 was built at source commit `775b9c822d178ef2634f9a5a646bb729563bc1c0` with GitHub Actions run `40537117613`, conclusion success. Python/JUnit/build passed. Verified in-chat downloadable APK `/mnt/data/LIKAS-v0.27.3-RC4-living-forest-debug.apk`, 1,392,591 bytes, SHA-256 `9a51219aacc8d841ef105ea853fa7a10f9005029b94765df0794876841479fe3`. Both HD assets match approved SHA-256 values. Package `org.phwildlife.survey.beta08`, versionName `0.27.3-rc4`; code 49 from `app/build.gradle`. APK Signature Scheme v2 cert SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, identical to the RC3 review APK. No physical Android animation, battery or historic DB upgrade tests have run.
- Immediate next: review the RC4 startup motion and wordmark on a real phone, including day/night and accessibility; record any visual or performance bugs. Once accepted, move to the Welcome/Home menu as a separate progressive declutter stage. Do not reuse the prior chat's APK link without rechecking the actual file.

---

**Update: 2026-10-27, Asia/Manila | Startup UI audit, part 1**
- Connected GitHub repository `andreivonmariano-dotcom/Wildlife-survey-app`. Default `main` observed at `f1ef6583647d05ae3cbbb915b2f54370150be33c`, still containing legacy v0.15 app source. Previous details beneath this update document an *earlier* snapshot and are not the latest feature baseline.
- Working baseline: PR #31 `release/v0.27-integrated-ux-performance-qa` (v0.27.0 RC1), followed by PR #33 `ui/v0.27.1-field-polish-quality-labels` (v0.27.1 RC2). Neither is merged to main. RC2 Python/JUnit/Android CI passed, but real-handset testing remains open.
- New branch: `ui/likas-startup-screen-r1-20261027`, based on RC2, version `0.27.2-rc3` (versionCode 48), Android ID `org.phwildlife.survey.beta08`, SQLite schema 27 unchanged. Latest startup app-and-test commit `718cb671a94410cf98f0f539ec2adb517f25a37d`.
- Latest owner instruction: declutter and improve LIKAS **one screen at a time**. Work on **startup landing** now, then separately review Welcome/Home, survey setup, live recorder, History and other screens.
- Coded for startup Part 1: smaller approved frog/wordmark, clearer native controls, lighter forest visibility and stable `Open LIKAS` Home destination. Show separate `Resume survey` only with a valid unfinished survey. Remove on-screen build-version artifact. An unreadable survey database now displays a warning instead of silently being treated as empty.
- Approved artwork carried forward unchanged: `likas_frog_icon.webp`, `forest_day_hd.webp`, `forest_night_hd.webp`. Time-of-day renderer unchanged.
- Automated source contract: `scripts/test_v0272_startup_menu.py`; review plan: `docs/QA_v0_27_2_STARTUP_MENU.md`. GitHub CI run `40533599624` completed successfully for source commit `718cb671a94410cf98f0f539ec2adb517f25a37d`, including Python source tests, Android JVM tests and a debug APK build. Verified review APK: `LIKAS-v0.27.2-RC3-startup-review.apk` (1,389,359 bytes, SHA-256 `5a38eb14aef0ac5c281248f63c41cc34ae202484ba0fbeb0c8921db95c3e3abf`). APK contains the approved frog and HD forests, declares package ID `org.phwildlife.survey.beta08`, and has the same v2 signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` as RC2. A direct APK link was provided in the current chat. No handset checks are claimed.
- All survey persistence, outcomes (Complete / Incomplete / Not Counted), taxa, MSL elevation metadata, WAV/photo evidence and scientific exports remain untouched by this screen patch. No DB migration.
- Physical handset layout at 320dp/200% fonts, animation frame pacing, database error flow, and real older data/signing installation are outstanding release gates.
- **Next step:** Review the startup menu on a phone, especially minimum display sizes, font scaling and Resume behavior. Only then audit the **Welcome/Home menu** (wildlife-group tiles, duplicated Resume, shortcuts and More tools) in a separate change. A prior chat's APK link is not reusable without a new file check. A prior-chat APK link is not a current deliverable unless reverified.

---

## Earlier historical state snapshot (not the latest feature baseline)

**State revision:** 1.0  
**Recorded:** 27 October 2026, Asia/Manila  
**Repository:** `andreivonmariano-dotcom/Wildlife-survey-app` (private)  
**Branch checked:** `main`  
**Observed repository HEAD when baseline was checked:** `6660e6ea101007f85dba315bb6688621c31bdf3f` (documentation change). This value becomes stale after any further commit.  
**Latest app-feature code milestone observed:** `dc0362d23d0f74b6bca57c66da6e20c34adfd52d` (v0.15); later `c7c5742` and `44825d7` commits added LIKAS forest and brand files.  
**Evidence:** GitHub `README.md`, directory tree, selected source files, previous LIKAS project-chat decisions. No direct Android handset test was performed for this snapshot.

## Mission

Build **LIKAS** as a Philippines-first, all-in-one biodiversity tool for wildlife biologists and ecologists. Five product pillars: (1) Field Survey Studio, (2) Philippine Species Library, (3) Habitat and Ecology Atlas, (4) Research and Conservation Watch, and (5) Analysis and Research Export. Scientific integrity, offline field use, data preservation, and clear interaction outrank decoration.

## Observed coded baseline (not a claim of physical-phone verification)

- Repository `README.md` identifies **Philippine Wildlife Survey Beta v0.15.0**, an offline Android field app for birds, herpetofauna, and mammals, without a map UI in that release.
- Version 0.15 introduced reusable bird point-count route templates, permanent station IDs, distinct visits, geotagging on station arrival, and data-quality review rules. The README describes SQLite schema version **11**, research ZIP exports, and scientific effort accounting that excludes travel from station sampling time.
- Existing modules/files include survey activity, history, taxa catalog, research export, scientific data audit, site management, camera/photo evidence, a GPS track service, point-count progression, and Android APK CI.
- The source catalog uses a small unreviewed starter list. The official Mammal Diversity Database v2.5 Philippine import is documented in the README. The Sy amphibian and reptile datasets are **not bundled** according to that README.
- Brand-related commits introduced a retro forest backdrop component, day/night WebP art, brand color definitions, and an adaptive icon. The brand document says the animated backdrop is **not yet attached to the activity** at its documented stage. Inspect current source and actual handset behavior before changing this status.

## Confirmed owner preferences and decisions

- **Survey ending:** An End Survey action must remain accessible. Completed means protocol conditions were met. Incomplete retains real partial work. Cancel / Not Counted archives the event outside standard survey analysis.
- **Location:** Elevation should follow an acceptable recent GPS fix, with source, age, uncertainty, and reference tracked rather than filling in unknown values. Implementation status must be checked in code and on device.
- **Reference lists:** The owner previously supplied an amphibian checklist (Emerson Sy 2025-1), reptile checklist (Emerson Sy 2026-1), and WBCP bird checklist (2025). These must be verified as available in a current chat/Project source before claiming they are imported or bundled. The Mammal Diversity Database import uses a separate versioned source.
- **Visual direction:** Simplify busy screens, remove duplicate controls and text artifacts, and keep a pleasing minimalist field interface. Approved direction: pixel-frog launcher icon with upper face emerging from pond water, no lily pad. Animated tropical forest should be sharper and smoother than the low-resolution initial version, with subtle movement and device-time day/night appearance. Verify approved visual files and actual implementation, not only filenames.
- **File handoff:** Every APK, ZIP, image, document, and other deliverable requires a verified direct in-chat file link; never replace that link with a GitHub page. See DL-01.
- **Persistent continuity:** The owner wants to start a new LIKAS chat without restating prior decisions when an earlier chat reaches its limit. See CC-01.

## Active requirements and likely next tasks

1. Read the user request, recent repository HEAD, this state file, decision log, master specification, and relevant code before implementing changes.
2. Audit current app workflows for duplicated controls, stale explanations, uneven design, and unfinished survey lifecycle paths.
3. Validate the approved frog icon and sharper/smoother forest art against actual repo assets, then make UI changes only using verified materials.
4. Verify and finish the requested checklist coverage for birds, amphibians, and reptiles. Review source provenance and redistribution rights, and avoid inventing island or province occurrences.
5. Check GPS-based elevation behavior with device-quality metadata and confirm accessibility of End Survey/Complete/Incomplete/Not Counted actions.
6. Carry out physical-phone verification, migration checks, data audit, and scientific export review before reporting a tested release.
7. On a new build, provide the **actual APK in the current chat as a direct download**, not just a GitHub URL.
8. Plan phased expansion into habitat/ecological information, wildlife research updates, and analysis modules. These are product goals, not currently verified shipped modules.

## Current blockers and nonclaims

- No new APK was compiled or physically tested during creation of this snapshot.
- No complete migration audit, full UI audit, or taxonomic source-license review was performed in this continuity task.
- The three spreadsheets referenced in prior chats and earlier approved ZIP assets are not confirmed to be mounted in every future chat. A prior chat's sandbox path is not a durable download link.
- Project memory settings cannot be changed by a repository commit. The owner must set Project-only memory in the ChatGPT Project settings when supported.
- The repository HEAD above is a historical observation. Read the current HEAD at each new session.

## First task for a replacement chat

If the user says **"Continue LIKAS from the previous chat"**, read the canonical files named in `AGENTS.md`, check the latest code, and continue the latest explicit request. Provide a two- or three-sentence orientation only if helpful, then proceed to the task. Do not require the user to resupply decisions already recorded.

## State maintenance

Update after any important decision, code change, bug discovery, user-approved asset, release, or unresolved task. Identify what was **requested**, **implemented**, **tested**, and **released**. Update timestamps and source SHA whenever this snapshot changes. Append permanent product decisions to `docs/LIKAS_DECISION_LOG.md`.