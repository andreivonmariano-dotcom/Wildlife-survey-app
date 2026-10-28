# LIKAS | Minimal Title Startup and Offline Reference Library

**Date:** 28 October 2026 (Asia/Manila)
**Owner decision:** Replace the time-based animated forest startup with a minimalist LIKAS wordmark and a restrained transition into Home. Expand offline biodiversity species and habitat reference content, including properly credited photographs.
**Working branch:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38. Android package identity, SQLite schema 27, versionName 0.27.3-rc4, versionCode 49, and survey record structures remain unchanged.

## Startup design

- `LikasLaunchActivity` renders a single **LIKAS** title over deep forest green. No forest view, bitmap, decorative icon, GIF, weather context, GPS access, database write or blocking full-screen menu.
- A 460ms title fade/scale and a 210ms fade-up lead automatically into `WelcomeActivity`. A user tap skips motion. Android's `ValueAnimator.areAnimatorsEnabled()` disables motion where configured, so Home appears immediately.
- Home retains **Resume Survey** and all fieldwork/project navigation. The startup transition itself never starts a new survey.
- Older bundled forest art remains as historical assets, not referenced by the new launcher. Removal can be coordinated separately to reduce APK size.
- This owner decision supersedes previous approval of the animated day/night startup. The **old PR lineage gate** is based on the RC4 forest policy and may fail until the trusted base workflow is updated to accept the new decision. A failed gate is not hidden or described as passed.

## Offline content included in source

### Installed national checklists
- Already-bundled birds, amphibians, reptiles and mammals remain available as scientific **taxon names and attributed source records**. Do not confuse taxonomic catalog coverage with complete diagnostic accounts.
- Checklist/import sources and review status remain separate from local occurrence evidence. Unreviewed records never count as confirmed station presence.

### Detailed species accounts
- Eight source-checked accounts in `diagnostic_profiles_v1.json` are enriched with five narrative sections: morphology/overview, habitat/microhabitat, natural history, field documentation guidance and identification cautions.
- Subjects: Philippine Eagle, Philippine Serpent-Eagle, Four-lined Tree Frog, Asian Painted/Banded Bullfrog, Philippine Sailfin Lizard, Cuming's Water Monitor, Philippine Tarsier and Philippine Colugo.
- Each account contains source IDs mapped to the already installed citation register. Morphology and ecology remain source-checked, **not independent Philippine taxonomic specialist approvals**. Unknown traits stay unassessed.
- `TaxonDetailActivity` displays these descriptions when a source-matched entry exists; other checklist taxa retain original checklist information and an explicit missing detailed-account message. Comparative morphology remains available in the `DiagnosticGuideActivity`.

### Habitat and Ecology Atlas
- `offline_habitat_profiles_v1.json` carries nine distinct cited habitat profiles: lowland, montane, mossy/upper montane, limestone/karst, ultramafic, mangrove, freshwater wetland, riparian and secondary/disturbed forest mosaic.
- Each habitat includes overview, field indicators, hydrology and substrate, vegetation/wildlife context, recommended measured variables, interpretation cautions and citation IDs.
- Reference sources: Field Museum Philippines Habitats, PSA forest formations/mossy forest and mangrove definitions, DENR Mangrove Management Handbook.
- `HabitatRecordsActivity` presents compact expandable reference accounts above actual station habitat and per-visit environmental records. Reference content never rewrites station conditions or infers habitat/species presence.

### Licensed offline photo workflow
- `offline_photo_manifest_v1.json` contains six independently selected Wikimedia Commons files with image authors, file descriptions, source URLs, Creative Commons reuse licenses, offline asset paths and resizing notice.
- Examples: Pithecophaga jefferyi (captive Davao), Carlito syrichta (Bohol), Polypedates leucomystax (Philippines), Kaloula pulchra (Thailand, clearly marked as a reference), Palawan mangrove forest and Mount Dulang-dulang mossy forest.
- `scripts/prepare_offline_photos.py` retrieves a small Commons thumbnail per file **at build time** and copies them into `app/src/main/assets/offline_photos/`. The Android APK must actually include these bytes to qualify as a photographic offline build.
- `OfflinePhotoEvidence` displays only bundled photos and always shows author, CC license, publisher file page, photograph context and license URL. If an expected photo is not packaged, the app labels it missing rather than claiming offline availability.
- Original photographs are not redistributed at full resolution. The workflow creates a small resized preview and preserves license attribution. Image location may differ from the distribution of a study population; the image is an **identification illustration**, not station occurrence evidence.
- This media subset is intentionally curated, not photos for every Philippine taxon. A complete licensed media library would require additional verified source/photo identifiers, legal review and larger storage planning.

## Quality gates

1. Python source/schema checks for 8 species account references, 9 habitat profiles, all six CC photo records, and minimal startup/resume.
2. GitHub workflow downloads and verifies at least three JPEG thumbnails before Android Gradle compiles them into the app.
3. JVM and Android source compilation, app identity and RC4 signing certificate verification.
4. Inspect actual APK asset paths and signing metadata before direct-link delivery. Do not describe a CI artifact as downloadable until copied into this conversation and checked.
5. User device visual review, startup timing, first-launch/relaunch behavior and in-place upgrade with saved surveys remain pending; this is a **development preview only**.

## Scientific limitations and next stages

- The **full** species/habitat library is not complete: eight detailed taxon accounts and nine habitat accounts are a grounded starting set. Hundreds of checklist names have no reviewed diagnostic descriptions, licensed photographs or audio. Do not auto-generate species-specific traits.
- Links to original source pages remain offline-readable as text but clicking them requires network connectivity; the reference account text and bundled preview photographs work without network.
- No validated species distribution modeling, live IUCN lookups, full ecological measurement entry form, or licensed offline basemap was added.
- Full restorable backup and physical survey-data preservation review remain necessary before promoting the draft branch to field release.
