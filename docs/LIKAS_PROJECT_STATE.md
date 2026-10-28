# LIKAS: Current Project State and Next-Chat Handoff

## 28 October 2026 | RC6 Q1/Q2/Q3 navigation, experimental recovery, Philippine species

- **Owner request:** Q1 unify standard navigation across LIKAS, Q2 verify RC4/RC5-to-current upgrade preservation and build restorable offline backup, Q3 expand endangered/endemic species across birds, amphibians, reptiles and mammals.
- **Source branch:** `feature/likas-core-fieldwork-post-rc4`, PR #38 still draft, based on RC4 lineage. RC6 source commit **`f4284d255248f7c76d35663a2823957ebddaa09e`**, subsequent documentation-only commit `109cf062e709f03a353a08613bd5c292b85d7159` (check newest HEAD at chat start). Package `org.phwildlife.survey.beta08`, versionName `0.27.3-rc6-recovery-library-preview`, versionCode **51**, SQLite **27**. There is **no schema migration** and no startup/sprite/icon modification.
- **Navigation implemented:** Shared `UiKit.navigationHeader` now appears on standard Dashboard, Survey History, Audit, Sites, Station Progress, Study/Field/Trap Protocols, Species Catalog/Picker/Detail/Regional Evidence, Habitat & Ecology, Data Management, old Import, Call Library, and Diagnostic Guide. Sound recording remains protected from accidental back navigation, and survey detail returns to survey list. Live recorder, camera, GPS use specialized controls. Needs actual 320dp/large-font screenshot and accessibility acceptance.
- **New recovery source:** `FieldBackupArchive.java`, `FieldBackupActivity.java`, Android manifest and Home link. Archive contains consistent SQLite online `VACUUM INTO` snapshot with `PRAGMA quick_check`, user record_media/acoustic, local XML settings, per-file SHA-256 manifest and safety size/path constraints. Refuses export with an active survey. Restore verifies ZIP/database and is restricted to an empty local workspace with no survey/site/project/route/template/favorites, original media or pending WAL. It creates a before-restore copy and requires restart. **This has only source/compile coverage, not completed device round-trip; not approved stable or guaranteed restorable.** SQLite versions older than 3.27 explicitly refuse the snapshot. ZIP is **unencrypted** and may contain sensitive coordinates or other private data. The legacy v0.7 CSV importer remains a separate path. See `docs/LIKAS_RC6_NAV_RECOVERY_SPECIES_QA.md`.
- **Scientific library:** Source-checked introductory species accounts increase from **13 to 17** by adding **`Dicaeum quadricolor`** (Cebu Flowerpecker, BirdLife/IUCN), **`Platymantis insulatus`** (Gigantes Limestone Frog, AMNH/ASG/FrogLog), **`Varanus olivaceus`** (Gray's Monitor, IUCN SSC/CITES) and **`Bubalus mindorensis`** (Tamaraw, DENR 2025 action plan/IUCN SSC/ZSL/MDD). All four groups represented. Unreviewed identification remains unresolved; global IUCN status distinct from DENR national status, habitat evidence distinct from occurrence. No new photo candidates were added for these four profiles.
- **CI QA:** Trusted PR lineage passed on the RC6 code commit `f4284d2...` (run `40674565850`). Python tests, bundled MDD source checks, licensed photo preparation, JUnit, Android compilation and APK artifact succeeded at the exact code SHA in push run **`40674561638`** and PR run **`40674570013`**. Source contract tests: `scripts/test_rc6_backup_and_navigation.py`.
- **Directly delivered APK in originating chat:** `/mnt/data/LIKAS-v0.27.3-RC6-recovery-library-preview-debug.apk`, **2,050,879 bytes**, SHA-256 **`43df7c311bda5d4a8ff915ad007dad4af15a9bc83e6b9f7ba794677722619ee3`**. ZIP integrity checked; embedded package and version strings verified. APK Signature Scheme v2 cert SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`** matches RC4/RC5. Session-local URL is not reusable in future chats.
- **Media regression:** The RC6 binary has **nine genuine offline reference JPEGs**, one less than RC5's ten. The RC5 tarsier JPEG (`species_tarsier.jpg`) was absent from this CI media retrieval, likely source-fetch variability; the manifest still credits its source and unavailable states are supported. Do **not** claim RC6 has ten images. Future work should make licensed media packaging deterministic and retry/restore the approved tarsier thumbnail.
- **Unverified:** No actual physical-device RC4→RC5→RC6 in-place upgrade or preservation comparison, no exported ZIP→fresh install→restored field survey+photos+audio round-trip, no corruption/rollback injection, no handset screenshot acceptance, no universal Android SQLite-version compatibility, no destructive control acceptance. Preview only. Separate sprite/animation branch remains untouched.
- **Next action:** Verify backup and restore on a sacrificial Android handset with realistic historic survey/media data and malformed archives, address missing photo reproducibility, review navigation on handset, then decide whether to promote or adjust. No stable release or PR merge claim.

---

## 28 October 2026 | RC5 Q1/Q2/Q3 field UX + cited offline species APK

- **Owner accepted:** Q1 consistent app navigation, Q2 shorter one-handed survey setup, Q3 larger source-checked Philippine wildlife library with licensed photographs. Work remains on `feature/likas-core-fieldwork-post-rc4` (draft PR #38). Current repository head at reporting: `334915ebe8886722610078c7cc1011194d2acf1f`. RC4 ancestry retained.
- **Android source implementation:** App-code commit `b39fefb5f51e984081b79bea8a41d881d3023b4f`. Shared `UiKit.navigationHeader` now applied to Dashboard and Survey History; observation detail can still return to All surveys. Survey setup retains method/project/checklist/GPS/route/effort controls and `startSelected()` validation, pins one Continue to GPS button beneath scroll, moves optional observer to additional details. Does not touch recorder, survey lifecycle, SQLite tables, media history, scientific export, startup sprite system or launcher art.
- **Version:** New installable **`0.27.3-rc5-field-ux-preview`**, versionCode **50**, package **`org.phwildlife.survey.beta08`**, SQLite **27**. Same APK v2 signing certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. No database migration introduced.
- **Field Guide:** `diagnostic_profiles_v1.json` contains **13 introductory source-linked accounts** (prior 10); new names `Manis culionensis`, `Sus cebifrons`, `Gallicolumba platenae`. Sources: IUCN SSC specialist groups, 2018/2019 global pangolin assessment, BirdLife historical account, and documented August 2026 Mindoro bleeding-heart rediscovery. The 2026 record does not establish a population or an island-wide site range. Global IUCN is not DENR national status. Still no specialist-approved ID keys/full national narrative coverage.
- **Actual APK media:** **10 offline JPEG thumbnails** verified inside binary, three more than RC4. New Commons media: Philippine pangolin (Shukran888, CC BY-SA 4.0), Visayan warty pig (Mhlayson, CC BY-SA 4.0) and **mounted museum specimen** of Mindoro bleeding-heart (Anand Krishnan, CC0). Sources/authors/licenses retained in manifest; do not treat reference photos as owner sightings.
- **QA:** Updated existing Python assertions for the RC5 scope while retaining actual method/migration/data contracts. Scientific-core Python/JVM/Android CI passed in runs `40672281957` and `40672289685` at test-inclusive SHA `21639ecd084e29506f49cc1d3720e7fb26f34fa1`. Latest trusted PR lineage gate passed run `40672337220` on docs-only HEAD. Trusted main gate now distinguishes voluntary sprite activation from ordinary field-preview version changes (main commit `826b06e62894fdb6165a9fe02e0bee8c751b6a19`), retaining mandatory approved sprite-media fingerprint for sprite activation.
- **Verified directly delivered APK:** `/mnt/data/LIKAS-v0.27.3-RC5-field-ux-preview-debug.apk`, **2,076,054 bytes**, SHA-256 **`7307fad1053bdd7a52d4eb8ffb3f856afc57aa476613164d0b0bb381ae079dc2`**. Binary ZIP structure passed, embedded manifest reads correct versionCode/package/name and v2 signer matches previous RC4. Delivered as a direct in-chat download in originating chat. Sandbox links are session-local.
- **Outstanding:** Draft PR #38 not merged, no RC4-to-RC5 physical phone upgrade/data preservation acceptance, narrow-screen keyboard/large-font visual check, scientific ZIP export/restore test, current-version full backup/recovery, protection of historic media after install, or destructive-data acceptance. Startup art/animation remains parallel separate branch. **Next:** phone screenshot and upgrade review, then broaden navigation and reliable field-data backup/restoration before promoting stable.
- **Design/QA record:** `docs/QA_v0_27_3_RC5_FIELD_UX.md` in PR #38.

---

## 28 October 2026 | Trusted RC4 PR check reconciled with minimal startup

- **Baseline inspected:** PR #38 head `42f493be7105fa937e1199c676536784f52894dc` on `feature/likas-core-fieldwork-post-rc4`, Android `0.27.3-rc4`, versionCode 49, SQLite 27. Last Android app-code commit remains `97a05898a0f1922eadd7dc05d93dbb43192b463e`.
- **Verified blocker:** Trusted default-branch `scripts/check_pr_against_rc4.py` failed the RC4 check at workflow run `40669557423` attempt 1 because it mandated an old splash tagline and a Resume control on `LikasLaunchActivity`. The owner's approved title-only splash now forwards to `WelcomeActivity`, where an active survey offers **Resume Survey**.
- **Remediation:** Trusted `main` gate updated at commit `5b99cc56f7ba6a357ecddbce4e6cd1c62d3f698e`. Its legacy check remains supported, and the minimal-startup path must verify LIKAS branding, navigation to Home, and Home's resume action.
- **Verification:** Run `40669557423` rerun attempt 2 **completed successfully** using the updated trusted gate. This resolves that identified PR compatibility failure, not all release conditions. No Android source, SQLite tables, signing setup, or APK changed in this correction.
- **Unverified and next:** PR #38 remains **draft**. Physical installed-device review of Field Guide UX, real current-version backup/restore and destructive-data recovery tests, migration/upgrade preservation, and cross-screen survey setup/dashboard consistency remain. Preserve separate startup/sprite branch boundaries. No new APK exists in this chat, and an originating-chat download link is not reusable.

---

## 28 October 2026 | Phone screenshots, field-guide UX and directly delivered APK

- **Owner request:** Q1 systematic source-checked endemic/threatened Philippine wildlife, Q2 illustrated habitat guides and field measurements, Q3 smoother minimal startup/Home transition. Owner supplied actual Android screenshots from installed preview showing oversized empty-query species dropdown, blank species discovery state, long habitat+site mixed scroll, excessive citation/photo text and inconsistent navigation across remaining screens.
- **Source built:** `feature/likas-core-fieldwork-post-rc4` app commit **`97a05898a0f1922eadd7dc05d93dbb43192b463e`**. Species search requires at least two letters, shows max eight compact suggestions, and filters both lookup and dropdown by selected wildlife group. The empty reference screen offers a browsable in-depth guide directory. Taxon pages use more readable 15sp text, 2-line reference summaries and a collapsed notes/source section.
- **Ecology UI:** Habitat & Ecology now separates **Habitat guide** and **My site records** into tabs; the guide filters Terrestrial/Edaphic/Coastal/Aquatic/Human-modified source types. Only one reference card expands at a time, and full text/photos load on first expansion. No field evidence is rewritten.
- **Brand continuity:** Owner-approved minimal splash continues to Home with a one-time 230ms Home-header appearance, respecting system animation settings.
- **Scientific data:** Source-checked introductory profiles expanded **8 to 10**, adding `Crocodylus mindorensis` (DENR 2005 recovery plan, University of Michigan ADW) and `Cacatua haematuropygia` (DENR WildALERT and Philippine Cockatoo Conservation Programme 2017). These are not specialist-validated keys or current population status. Nine original habitat descriptions remain sourced, plus a CC BY 4.0 Bacuit Bay limestone-karst photo by Vyacheslav Argenberg. User photos are evidence, not official species-distribution records.
- **Built APK delivered directly here:** `/mnt/data/LIKAS-v0.27.3-RC4-field-guide-UX-preview-2026-10-28.apk`, **1,902,816 bytes**, SHA-256 **`4efa428e41ce1b6a1e62fa2e3b40959e1ca734085e982171ca2a63eac7d17765`**, valid ZIP/APK with **7 real JPEGs** and all offline JSON packs. Android compiled manifest includes `org.phwildlife.survey.beta08`, `0.27.3-rc4`. APK Signing Block v2 certificate SHA-256 matches previous RC4: `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Successful Python/JVM/Android CI **`40669068454`** (push), **`40669076969`** (pull_request) at exact source SHA. Bundled species images: Eagle, tarsier, two frogs; habitat images: limestone-karst, mangrove, mossy forest.
- **Outstanding:** Trusted RC4 PR lineage gate **`40669071545`** remains failed, enforcing earlier forest startup despite explicit superseding owner decision. PR #38 must stay draft. No real-device visual acceptance after these changes, no full backup/restore or protected-data deletion device test. Survey setup and dashboard/navigation inconsistencies from screenshots remain separate next UI tasks. Full media for all species and habitats does not exist. No database migration, version bump or stable APK release.
- **Source documentation:** `docs/LIKAS_DEVICE_SCREENSHOT_UX_REFINEMENT_2026_10_28.md` and `docs/LIKAS_MINIMAL_STARTUP_OFFLINE_REFERENCE_V1.md`. Session-local APK link must be verified again in future chats. Mandatory direct APK with every app-code update remains in force.

---


## 28 October 2026 | Minimal startup and offline species/habitat reference APK

- **Latest owner decision:** Replace day/night animated forest splash with a clean LIKAS wordmark and subtle automatic transition into Home. Startup has a tap-to-skip action and honors reduced-motion settings. Resume Survey remains on Home. This supersedes previous animated startup plans. Master requirement U-03 updated in `main/docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` to v1.0.2.
- **Source branch:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38, Android `org.phwildlife.survey.beta08`, `0.27.3-rc4` versionCode 49, SQLite 27. Same debug signing identity. No survey table or application ID change. Latest source-code commit `dfb34c1b07e24211aa234150235161407677081d`, followed by documentation-only commit `ecd84d4713b5c75e66467bc564a66a8e38c183c8`.
- **Rich offline references:** `diagnostic_profiles_v1.json` has 8 source-checked species accounts with overview, ecological context, natural history, field notes, uncertainty and supporting citations. `offline_habitat_profiles_v1.json` has 9 detailed Philippine habitat accounts with field identifiers, substrates, field measurements, limits and primary/official source metadata. `OfflineFieldLibrary` and `HabitatRecordsActivity` display them without writing field records. Taxa without reviewed detailed accounts retain original named checklist records and explicit missing-detail messaging.
- **Actual licensed photos:** `offline_photo_manifest_v1.json`, `OfflinePhotoEvidence.java`, `scripts/prepare_offline_photos.py` and CI package Commons thumbnails at build time. APK contains **5 actual offline JPEG assets**: Philippine Eagle, Polypedates leucomystax, Kaloula pulchra, Palawan mangrove and Dulang-dulang mossy forest, with author/license/source credits shown in app. Philippine Tarsier photo did **not** bundle (Commons HTTP 429); the app displays the sourced attribution with an explicit unavailable state. Source photo count is 6 candidates, but **only 5 packaged**. No false claim of complete photographic coverage for thousands of species.
- **Build and direct link:** Full Python, JVM, Android assembly and licensed-photo checks passed GitHub Actions **40664803299** at `dfb34c1...`. Later documentation-only push run **40664897004** passed as well. Exact delivered APK path in this conversation: `/mnt/data/LIKAS-minimal-startup-offline-library-preview-2026-10-28.apk`, **1,779,812 bytes**, SHA-256 `6ffd7092e2ac411ff9f482486a471e7ff744d48fb7dfd84d59d500ef1518929b`. Inspected ZIP: 5 genuine JPEGs and all reference JSON assets. APK Signing Block v2 cert SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, matching RC4. **Direct APK link was given in this chat**. Previous chat URLs are session-local, so always reverify in new chats.
- **Blocking merge gate:** Separate trusted RC4 PR lineage job **40664902123 FAILED** because its base-branch `scripts/check_pr_against_rc4.py` still mandates forest animation. New owner U-03 supersedes that check, but the gate was not silently changed in the startup chat's branch. Keep PR #38 draft until trusted startup-base test policy is reconciled and physical phone UX/upgrade reviewed. Do not claim all checks pass.
- **Scientific limits:** National catalog coverage is names/source data; **only eight** detailed species accounts and **nine** habitat profiles presently. Reference photo availability is five. No copied proprietary guidebooks, no model-generated species traits, no automatic distribution, no full offline map or current-version full restore. No physical handset validation. Formal test-plan work remains deferred at owner's earlier direction.
- **Specification and handoff:** `docs/LIKAS_MINIMAL_STARTUP_OFFLINE_REFERENCE_V1.md` on core branch, v1.0.2 master spec on `main`. Next tasks: fix Trusted PR source gate for superseded forest design, expand curated licensed species and habitat sources, retry and validate missing tarsier photo, then release via signed version-bumped integration after data backup/restore and device review.

---


## 28 October 2026 | Mandatory APK link with every app update

- **Explicit standing owner instruction:** Whenever LIKAS application code is updated and an APK is built, provide a **verified direct download link to the actual newly built APK in the same response**, without waiting for a separate request. Never substitute a GitHub Actions page or older APK. If compilation, signature verification or direct delivery fails, state that no new downloadable APK is available rather than suggesting the update is installed.
- **Latest verified downloadable preview this conversation:** `LIKAS-scientific-core-QoL-2026-10-28-preview-debug.apk`, 1,426,823 bytes, SHA-256 `32b784b2a4d339d5b8a0b9679188d556bda202d39dc47914c56d5c01537e6f1a`. Built in successful core QA run `40660009190` from application code SHA `80e8240ac811e96682de3aa66069ded8c5559714`. AndroidManifest: `org.phwildlife.survey.beta08`, `0.27.3-rc4`, versionCode 49. V2 certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. It is a development preview only, not a stable field release. A sandbox file link is session-local and must be freshly verified in every new chat.
- **No app code changed in recording this instruction**. This is a documentation-only continuity update. Protect owner records and the startup animation workstream.


## 28 October 2026 | Follow-up usability corrections and current state

- Owner-requested Q1/Q2/Q3 core usability implementation remains under draft PR #38. **Last application source commit:** `80e8240ac811e96682de3aa66069ded8c5559714`, directly on RC4-based `feature/likas-core-fieldwork-post-rc4`.
- Individual unused project, station, route and study-template deletions are available from the Data Management category chooser, with explicit dependency checks and typed confirmation. All-survey, full-reset and project-reset controls are disabled when protected active/dependent data prevent them.
- Permanent data deletion is now performed on an independent SQLite connection in a dedicated background executor, with a non-cancelable progress dialog during each authorized transaction. The UI shows completion or failure afterward. Default project and stable references survive. Stored field drafts/resume and method quick-action preferences are removed at the appropriate reset scope. **No actual user records were deleted during development.**
- **Navigation:** Data management has one visible Workspace shortcut on Home, rather than a duplicate link under Field Tools. Species finder and Species Library arrow controls explicitly populate the bounded offline suggestion list before opening it.
- **Scientific roadmap:** `docs/LIKAS_CORE_USABILITY_SPECIES_HABITAT_DATA_ROADMAP.md` documents the verified/current limitations and next milestones for a comprehensive Species Library, Habitat Atlas and current-version backup/restore. The new Habitat & Ecology screen is read-only, not a finished spatial atlas.
- **QA:** Earlier application build/Python/JUnit CI `40659764742` succeeded on source commit `a988fd6906ae7686fb4db5c9ca8b32b653afc1ff`; more recent code (background deletion and Home link) was verified by successful full CI run 40660009190 on final code 80e8240ac811e96682de3aa66069ded8c5559714; RC4 lineage check 40660018098 passed. No new APK was delivered here. Do **not** merge/release destructive controls before a real current-version backup/recovery route, in-place upgrade/record preservation, media testing and owner visual review.
- App ID `org.phwildlife.survey.beta08`, SQLite 27, versionCode 49 and signing identity unchanged. Separate startup work remains untouched.

---


## 28 October 2026 | Q1/Q2/Q3, usability and safe local data controls

- **Owner direction:** Develop a more comprehensive Philippine Species Library, design Habitat and Ecology Atlas, review project/backup/scientific data organization, and make LIKAS easier to operate. Explicit requests include deleting all categories of saved data and adding a dropdown for species name search. Formal field test plans remain deferred, startup animation belongs to another chat.
- **Actual source changes (RC4 scientific-core branch, PR #38 draft):** New bounded `SpeciesReferenceSuggestionAdapter` and visible autocomplete dropdown on `TaxonDetailActivity` and `SpeciesPickerActivity`, covering common/scientific/known synonym names. The picker restricts suggestions to its current group and retains unidentified detections.
- **New local Data Management module:** `DataManagementActivity` and `FieldDataDeletion` implement archived survey deletion (single or all), per-item unused site/project/route/study-template deletion, project/station/route/study register reset, favorite cleanup and full user-created field-data reset. All destructive actions require typed confirmation; bulk delete is disabled with an active survey; project-register reset is disabled when survey history remains. SQLite changes are transactional and child records are deleted before parents. Safe local photo/WAV filenames only are removed after successful DB commits. Reset restores the built-in General field project and removes stale field draft/resume preferences; full reset clears method quick-action preferences. Installed source taxonomy and reference assets remain preserved. **No developer or tool executed a deletion against owner records.**
- **Habitat early module:** `HabitatRecordsActivity` is available on Home under Habitat & Ecology. It reads existing station habitats and per-visit environmental evidence, shows source/elevation/uncertainty or explicit not-assessed states. This is not a finished atlas, not an offline map, and not a habitat prediction.
- **Roadmap and scientific scope:** `docs/LIKAS_CORE_USABILITY_SPECIES_HABITAT_DATA_ROADMAP.md` gives detailed species-account, ecological measurement, offline map, data organization, current-version backup/restore, privacy and research-output milestones. Existing 8 introductory diagnostic profiles across Birds, Amphibians, Reptiles and Mammals remain source-checked, pending independent expert taxonomy review.
- **Critical release limitation:** Existing ZIP/CSV is scientific export, not a fully proven reinstallable backup; current `ImportActivity` targets v0.7 to empty v0.8 records. Owner-requested destructive tools remain **draft-development only** until current-version backup/restore, physical device QA, media integrity and in-place upgrade checks are completed. Do not use a previous APK link to claim these changes are installed. App ID, versionCode 49 and SQLite schema 27 remain unchanged, and startup/forest/icon source untouched.
- **Verification:** Source-level Python regression `scripts/test_core_usability_management.py` covers bounded dropdown and protected deletion graph with synthetic SQLite rows. Existing Python/JVM/Android CI has passed earlier implementation commits. Final exact HEAD build must be checked before release or merge. Branch remains draft PR #38 based on RC4, not historical `main`.

---


## 28 October 2026 | Direct RC4 scientific-core preview APK

- The owner requested a direct APK download after the method quick-action preferences and herpetofauna/mammal identification library work. A debug APK from CI run `40656109914`, source commit `09f1dbc4ef82de1dcce650d101155ed0689e71d8`, was downloaded and extracted into the current conversation as `LIKAS-v0.27.3-RC4-scientific-core-preview-debug.apk`.
- Verified actual AndroidManifest: app package `org.phwildlife.survey.beta08`, versionName `0.27.3-rc4`, versionCode 49. APK Signing Scheme v2 signer SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` matches the historic RC4 certificate. APK SHA-256 `24f91f1688a069b0d32c77a0fdc747b046199d68bbf9442c2612c102356c5883`, size 1,409,967 bytes. Artifact ZIP ID `12801697848`.
- The downloadable file is a source-tested **development preview retaining RC4's app version and code**, not a released 0.27.4, not the separate startup animation build, and not a physical-phone-tested in-place upgrade. It uses the same signing identity and SQLite schema 27, but local field-data backup is advisable before replacing an installed RC4 build.
- Full Python/JUnit/Android workflow `40656109914` succeeded. The previous Python-test failures from newly expanded source profiles were corrected before this CI run. No startup source changes in the core branch.


## 28 October 2026 | Source-complete Q1/Q2/Q3: minimalist UI, capture shortcuts, diagnostic comparisons

- Owner chose consistent visual styling across Home, Live Recorder and Species Library, fast per-method evidence actions, and cited identification comparisons. Startup animation and formal field-test planning remain in their separate workstreams.
- Shared `FieldVisuals` headings now cover core screens. `FieldQuickActions` routes one visible action to the existing call library, photo attachment, trap checks or method-evidence screen according to the survey method. No change to Save/Review/End Survey, scientific write paths or schema.
- New offline `diagnostic_profiles_v1.json` and `DiagnosticGuideActivity` compare two source-checked Philippine raptor profiles: `Pithecophaga jefferyi` and `Spilornis holospilus`. Every displayed trait has a publisher citation and checked date. Comparison is available in Species Library and matching species accounts. Text remains pending expert review; images, automated identification and unsupported species keys were not added.
- Android compilation, Python/JUnit regression checks and source QA passed GitHub Actions run `40654286500` for code commit `c9401cdb1d7b72258ff3a93824dd209017807bd0`. No installed-phone screen check or new APK release.
- Draft PR #38, RC4 ancestry, package ID, versionCode 49 and SQLite 27 remain unchanged. The startup branch was not edited. See `docs/LIKAS_BIODIVERSITY_WORKSPACE_V1.md`.

---


## 28 October 2026 | Owner selected Q1-Q3 Home, Recorder and Species Library redesign

- **Explicit selection:** Redesign the Home biodiversity workspace, simplify Live Recorder, and prioritize source-backed identification and ecological evidence in Species Library. Formal field-test plans remain deferred; startup animation stays in a separate chat and branch.
- **Implemented on core source branch:** `WelcomeActivity.home()` surfaces Fieldwork, Projects and Stations, recent-survey summary, Survey History, Dashboard, direct Species Library search, and collapsible field tools. **`LikasLaunchActivity` and forest/icon assets not edited.**
- **Live Recorder:** `MainActivity.draw()` groups secondary audio, device/capture and method-evidence actions behind an accessible disclosure. Mark now, Measure bearing, station navigation, fixed Save Observation, Review Records and End Survey stay accessible. Complete/Incomplete/Not Counted and all data writes unchanged.
- **Species presentation:** `CatalogActivity` now acts as a Species Library with direct name/synonym references and sources. Catalog totals are calculated from installed attributed taxa, original escaped-newline artifacts and hard-coded 1,503 denominator corrected. `TaxonDetailActivity` groups taxonomy, conservation, habitat/elevation and regional evidence; explicitly states that no vetted diagnostic key is installed. No synthetic traits or media created.
- **Scientific screening:** `SpeciesPickerActivity` no longer presents heuristic percentages or "Likely/Unlikely" to users. `HabitatLikelihood.Result` exposes separate text outcomes for source-derived habitat and elevation matching. Picker sort UI limits visible ordering to name/family/genus and resets legacy pseudo-probability filters. **Underlying heuristic source logic, SQL schema and historical observations remain unmodified.**
- **Documentation:** `docs/LIKAS_BIODIVERSITY_WORKSPACE_V1.md` records implemented screen hierarchy and limitations. Updated legacy UI source regression assertions in `scripts/test_v026_ui_audit.py` after new design superseded old labels.
- **Build status:** PR #38 is still a draft on RC4-based `feature/likas-core-fieldwork-post-rc4`. RC4 PR lineage check passed for code commit `d939960f8a7fc5e90f9ad90b1298209bbc6e1f0f`. Core QA run `40652463202` passed for that code revision; after documentation commit `edd64f93576edd105e3a3aa74cac5e9e35f259c1`, latest CI pending final confirmation. No APK/release version bump and no handset evaluation.
- **Limitations:** Species diagnostic characters, reference photographs/audio and genuine expert identification keys are not present in catalog sources. Their future import needs documented evidence and reuse permissions. Source-level UI restructuring does not prove on-device visual correctness.
- **Next:** Review visible Home, Recorder and Species Library flow on the actual phone, then reconcile with parallel startup changes through a deliberate RC4-based integration. Do not change schema 27 or app package identity without explicit release migration.

---


## 28 October 2026 | Product critique requested, test planning deferred

- Owner asked to **defer test plans** and critique the application itself, excluding startup animation owned by a different chat.
- Source-level product critique is recorded at `docs/LIKAS_PRODUCT_UX_CRITIQUE_RC4.md` on `feature/likas-core-fieldwork-post-rc4`. This review covers Home, survey setup, live recorder, Species Library, regional/catalog evidence, history/dashboard, non-startup UI, architecture and the all-in-one biodiversity scope.
- High-priority review finding: `HabitatLikelihood` displays an uncalibrated numerical screening index and “Likely” (e.g. 60/100 from a province source listing alone), which can be confused with a measured occurrence probability. Recommend source-first textual evidence, not an apparent probability score.
- UX findings: Home hides project and reporting tools under More tools, setup is long and method-heavy, recorder has many adjunct actions and a fixed two-row footer, reference lookup requires multiple screens, and scientific findings are spread across navigation destinations. Existing dedicated End Survey and Resume Survey actions are strengths to retain.
- Confirmed source issues awaiting owner-approved implementation: `CatalogActivity.refresh()` constructs visible-looking literal escaped newline separators and has a hard-coded denominator of 1,503 catalog entries. No application code was modified in this review action.
- Document is an **unaccepted design review**, not an owner decision, source patch or field-device observation. Do not begin test plans, new feature work or UI restructuring solely from this critique without the owner's next direction. Draft PR #38 retains earlier scientific source fixes and remains separate from startup development.

---

## 28 October 2026 | Scientific core Q1 Q2 Q3 audit and protected fixes

- **User chose all three:** audit live survey tools, develop scientifically reliable species information and identification, and review whole-app architecture/performance/non-startup user experience. Startup animation remains entirely owned by the separate chat.
- **Branch & PR:** `feature/likas-core-fieldwork-post-rc4`, draft PR **#38** targeting `feature/v0.27.3-startup-living-forest`; inherits RC4 versionName `0.27.3-rc4`, versionCode 49, package `org.phwildlife.survey.beta08` and SQLite schema 27. No schema, survey record, field method, launcher icon, startup background or animation source was changed.
- **Detailed audit:** `docs/QA_LIKAS_RC4_SCIENTIFIC_CORE_AUDIT.md`, source-verified three-part review and P0/P1/P2/P3 risk register. Field survey lifecycle, GPS provenance and scientific export filters largely present; physical upgrade, GPS endurance, acoustic/photo evidence and power remain unverified.
- **Critical source fix (scientific P0):** `SpeciesPickerActivity` now requires `review_status='source_listed'` for province/island evidence rank and local-only results; `RegionalChecklistActivity` also excludes `unreviewed` rows from its source-listed list. Unreviewed data remain retained, not silently erased or treated as absence.
- **Scientific identification:** `TaxonDetailActivity` and regional list lookup can resolve former scientific names via `taxon_synonyms` without rewriting historical field observations. Regional Davao Region includes all five PSA provinces (added Davao Occidental), with a note about separately unassessed Davao City. Picker local-source mode without a selected geographic scope now gives an explicit no-area message rather than a misleading no-species result.
- **Field UX:** Regional search has 180 ms input debounce, a truthful 1,000-row cap notice and visible catalog error state. `AuditActivity` now executes read-only scientific DB review on a background executor and renders results on UI with error reporting and lifecycle shutdown. Screen and data semantics remain unchanged.
- **Tests verified:** `scripts/test_core_species_evidence.py` reconstructs the Java species-detail SQL and checks synthetic SQLite synonym/reviewed/unreviewed cases; `scripts/test_core_audit_worker.py` asserts nonblocking, nonwriting audit contracts. `.github/workflows/core-science-qa.yml` runs Python, JVM and Android compilation with RC4 lineage and signature verification. GitHub Actions run **40646166557 PASSED** at code/documentation commit `ce3a6619fff9c9b284c8dbf99969502897b6a0a1`; RC4 PR lineage gate **40646169577 PASSED** at that commit. Later commits only update GitHub project state and decision notes. No installable post-RC4 version or new APK has been released.
- **Outstanding high-impact work:** MainActivity remains 2,413 lines with several responsibilities and main-thread SQL calls. DataAudit and Dashboard/History performance need handset measurements. GNSS write queue reliability, genuine database upgrades from old installs, private attached-media persistence, 320/360 dp + 200% text and TalkBack testing remain open. Automated camera/audio species identification and Research Watch are not implemented in this audit.
- **Next:** Review audit, verify latest CI, run handset QA, then decide safe incremental extraction of survey repositories, improved taxon evidence model and scientific export workflow. Do not relabel the RC4 app or issue an untested APK.

---


## 28 October 2026 | Scientific feature workstream separated from startup animation

- **Owner direction:** The startup animation and its image assets are being developed in a different conversation. This core-development conversation must not modify or assess the startup background, sprites, startup typography or app icon unless later explicitly requested.
- **Core-only branch:** `feature/likas-core-fieldwork-post-rc4`, derived directly from `feature/v0.27.3-startup-living-forest` (v0.27.3 RC4, code 49, SQLite schema 27). Parallel `feature/v0.27.4-layered-sprite-player-stage` remains for startup development.
- **Scope:** Field recording, survey lifecycle, species/checklists, habitat/ecological data, research/news, analyses, scientific exports, offline reliability, app usability outside startup, and database integrity.
- **Governance:** `docs/LIKAS_CORE_FIELDWORK_WORKSTREAM.md` documents scope exclusions, preserved app identity/data semantics, and potential scientific priorities.
- **No feature code or APK changed** in this scope-separation action. First target is pending user selection; protect other branch work from conflicting changes.

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