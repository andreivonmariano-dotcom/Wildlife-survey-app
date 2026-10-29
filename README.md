# LIKAS v0.27.3 RC34 | Conservation cards and swipe habitat library

**Development candidate:** `0.27.3-rc34-conservation-cards-preview` (versionCode **94**). Same Android package `org.phwildlife.survey.beta08`, preserved signer and SQLite **27**.

The Species and Habitat Library now opens horizontally swipable, touch-friendly visual cards. The species collection shows an offline photograph where licensed, with an original fallback glyph otherwise. It features a **Conservation** shelf with six sourced entries, followed by the bird, amphibian, reptile and mammal shelves. Every displayed assessment retains **IUCN or DENR, category and dated year**. Three IUCN and three DENR status entries in the checked display index are supported by explicitly cited earlier research. **Other species have no unreviewed status badge**, and rarity is not automatically computed from threat, endemicity or checklist membership. The public Priority species and Priority research-gap labels have been withdrawn, retaining all original source records.

The habitat section offers side-scroll cards for five formation groups, and dedicated habitat profiles with field recognition, water, soil, vegetation, measurements, limits and original citations. Reduced-motion setting remains in force. `docs/LIKAS_RC34_CONSERVATION_CARD_DESIGN_SYSTEM.md` records the category and rarity rules, palette, screen geometry and outstanding full-catalog status audit.

**QA status:** Source-level science/navigation/UI regression checks passed on early RC34 source; latest exact source and new signed APK require verification independently before field release. Screen screenshots, 200% font, real-device upgrade and audio/GNSS acceptance remain open.

---

# LIKAS v0.27.3 RC33 | Portal, icon and visual refresh

**Android development preview:** `0.27.3-rc33-portal-ui-preview` (versionCode **93**), unchanged package `org.phwildlife.survey.beta08` and pinned signing identity, SQLite **27**.

The app now opens a quick LIKAS splash, then a **three-card portal** with short labels and original glyph art: **Fieldwork** for survey setup, active survey, archive, data and analysis; **Library** for Philippine species, habitats, original research, citations, comparisons and offline licensed photos; and **Settings** for backup, storage, import, app information and a Less motion control. A source-preserved, retro pixel-art *Staurois natator*-inspired frog emerges front-on from the pond in the launcher and home hero. This illustration is brand artwork, not a wildlife diagnostic photo. The old approved frog and forest images remain intact.

**Verified:** Scientific Core QA run **40848568986** completed successfully on source commit `33e1ef461739355185ad308f6803989b54188fa6`; exact signed APK package/version, 2,324-byte icon fingerprint, ZIP integrity and original signing certificate independently verified. **Not verified:** all RC33 Android emulator 200%-font screenshots, real-device upgrade over owner survey data, and remaining individual screen restyling.

See `docs/LIKAS_RC33_PORTAL_ICON_AESTHETIC_QA.md` for design system changes, tests, user-data protections, APK fingerprint and next work.

---

# LIKAS v0.27.3 RC32 | Source-scoped sparse priority accounts and diagnostic keys

**Build candidate:** `0.27.3-rc32-sparse-evidence-keys-preview` (versionCode **92**), Android package `org.phwildlife.survey.beta08`, same SQLite **27** and pinned signing setup. Core source checks, signed build and real-device verification are distinct QA gates.

RC32 adds 15 study-scoped research records for six sparse species: *Varanus bangonorum*, *Cyrtodactylus agusanensis*, *Cuora philippinensis*, *Ophiophagus salvatana*, *Crateromys australis*, *Dugong dugon*. The read-only research views now contain **505 citations over 96 species**, with **325/480 sections** populated and **18/96** species with at least one source in all five topics. The original 456-fact corpus and historical survey data remain unmodified. **23 taxa still have only one or two populated topics.** An account with five populated topics is not yet a complete field identification profile.

The source-linked reference catalog contains **33** exact-URL checked publication/agency/specialist entries. The dedicated 96-species status ledger separates dated, independently checked IUCN categories from the national DENR order; **three IUCN records and three DENR entries** have primary-level verification; a fourth dated IUCN status is tracked from a hornbill-specialist secondary summary from these review tranches. Historical research, group-summarized categories and unverified statuses are not silently treated as current. The two added specimen-scoped **Similar species** comparisons are *C. agusanensis* versus *C. gubaot*, and *O. salvatana* versus *O. hannah*.

The current 36/96 priority-species approved photo coverage remains unchanged. New third-party image candidates are **not** bundled without species identity, license, attribution and immutable-byte checks. No new private-authorized media rights have been presumed.

See `docs/LIKAS_RC32_SPARSE_STATUS_AND_DIAGNOSTIC_QA.md` and `docs/LIKAS_RC32_96_STATUS_REVIEW_LEDGER.json` for sourced claims, coverage thresholds, caveats and next QA tasks.

---

# LIKAS v0.27.3 RC31 | Priority diagnosis and dated conservation evidence

**Internal scientific candidate:** `0.27.3-rc31-priority-diagnostics-preview` (versionCode **91**), Android package `org.phwildlife.survey.beta08`, SQLite **27**, pinned debug signing. A compiled APK is not automatically a visually accepted emulator or field release.

RC31 targets the user's three priorities: source-backed additions for eight extremely sparse Philippine species, separate IUCN versus DENR status checks, and original-description/photographic diagnostic review. It adds **26 evidence notes** on top of the eight RC30 notes. The original 456-fact corpus remains unchanged, and the merged research screens now hold **490 facts, 316 of 480 topic slots and 17 species with at least one cited note in every section**. These are **content-coverage** counts only. Many taxa still lack original identification keys, life history, current legally verified status or licensed photographs.

The APA exact-link register now has **28 reviewed references**, including dates and authority for four rechecked IUCN global listings and two DENR 2019 national listings. The new `docs/LIKAS_RC31_96_SPECIES_STATUS_AND_ID_REVIEW_MATRIX.json` transparently records every unreviewed taxon and the photograph limitations. The 2016 *Dicaeum retrocinctum* IUCN assessment is **not** presented as current: IUCN's 2024-2 table reports its category changed to Near Threatened. *Dicaeum kampalili* has a verified 1953 name note, **not** a complete morphological key.

No new candidate photo is bundled without photographer/license verification, taxon/sex/stage review and approval. Survey setup, End Survey outcomes, active record data, SQLite schema, app ID, signing continuity and startup assets remain unchanged.

For exact species facts and their source locations, see [RC31 science QA](docs/LIKAS_RC31_SPARSE_DIAGNOSTIC_AND_CONSERVATION_AUDIT.md), [RC31 96-species status matrix](docs/LIKAS_RC31_96_SPECIES_STATUS_AND_ID_REVIEW_MATRIX.json) and [durable project state](docs/LIKAS_PROJECT_STATE.md).

---

# LIKAS v0.27.3 RC30 | Priority species evidence completion and APA research

**Science preview candidate:** `0.27.3-rc30-priority-evidence-preview` (versionCode **90**), package `org.phwildlife.survey.beta08`, SQLite **27** and existing signing identity. A successful build is not real-phone or field acceptance.

A source-level audit shows **96 priority species**, including only **9 species with at least one fact in all five research sections** at the original 456-fact baseline. The RC30 append-only reference pack adds **8 verifiable study-scoped facts for 6 species**, producing **464 cited facts and 294/480 populated topic slots** in the merged in-app research views. **13 of 96** species then have at least one fact in all five sections. These counts do **not** establish complete taxonomic diagnoses, range maps, modern IUCN/DENR assessments or photograph coverage. Missing: Identification 1, Habitat 37, Distribution 18, Natural history 61, Conservation 69.

`Rc30SpeciesEvidence.merge` adds these eight facts transactionally to read-only copies for species accounts, comparisons and conservation. Original `rc18` research JSON, taxonomic files, field observations and survey schema remain unchanged. APA 7 in-text and source entries now include **18 individually verified or explicitly attributed references** in the exact-link catalog, with no fabricated author names or document dates. The only undated specialist account is marked `n.d.`.

See [all 96 accounts and missing sections](docs/LIKAS_RC30_PRIORITY_COMPLETENESS_AUDIT.md), [current QA/status](docs/LIKAS_PROJECT_STATE.md), and [RC29 UI/readability audit](docs/LIKAS_RC29_UI_SCREEN_APA_FIELD_GUIDE_QA.md). Verify source QA, signed APK and Android emulator separately. In-place phone upgrade and owner survey backup/restore remain untested.

---

# LIKAS v0.27.3 RC29 | Offline research field guides and APA references

**Source candidate:** `0.27.3-rc29-research-field-guide-preview` (versionCode **89**), Android package `org.phwildlife.survey.beta08`, SQLite **27**, existing signing setup. A successful signed APK build and emulator acceptance are separate gates. Never treat this development branch as field-certified.

RC29 audits all 30 declared Android activities at source level, refines the 96 priority species field-guide layout, foregrounds existing licensed offline photos and photographer credits, trims technical phrasing, gives conservation evidence proper in-text citations, and shows source-linked references behind compact disclosures. **14 DOI-linked works have publisher/indexer-verified full APA 7 citation data.** The existing 306 species-reference links remain in the catalog, but the remaining incomplete metadata is not claimed to be APA-complete.

The complete 96-species / 456-fact source corpus, original observation/effort records, End Survey controls, photo rights, SQLite27, and approved startup assets are unchanged. See `docs/LIKAS_RC29_UI_SCREEN_APA_FIELD_GUIDE_QA.md` for the precise screen inventory, citation-coverage limits and acceptance gates.

---

# LIKAS v0.27.3 RC27B | Philippine reptile ecology and large-text field UI

**Source candidate:** `0.27.3-rc27b-keyboard-screen-qa-preview` (versionCode **86**), application ID `org.phwildlife.survey.beta08`, SQLite **27**, pinned Android signing certificate. This candidate has not yet passed new emulator or real-phone acceptance. The previous RC26C emulator had **15/16 tests**, with partial Habitat tab clipping at 200% font. RC27B hides inherited soft keyboards and tests the Habitat screen after layout; RC27 adjusted the compact navigation Back arrow and retains the existing 16-test screen suite.

Q1 adds four study-scoped facts across three reptile species, for a total **96 accounts, 456 facts and 306 links**. Q2 preserves RC26 Home, Setup and History large-text updates and targets the measured Habitat clipping. Q3 includes a **read-only** `scripts/rc27_phone_readonly_audit.sh` and fourteen handset acceptance checks, all pending actual hardware. No survey records, End Survey decisions, app package, signing key, database schema or startup artwork change.

See `docs/LIKAS_RC27_SCIENCE_AND_UI_QA.md`, `docs/LIKAS_RC27_PHONE_FIELD_QA.md` and `docs/LIKAS_PROJECT_STATE.md`. Do not mistake a successful debug build for a tested field release.

---

# LIKAS v0.27.3 RC26C | Primary ecology and field screen review

**Latest scientific-core debug preview on draft PR #38:** `0.27.3-rc26c-fieldwork-readability-preview`, versionCode **84**, application ID `org.phwildlife.survey.beta08`, SQLite **27**, existing pinned Android signing certificate. This branch's README history below contains older independent release candidates; do not mistake its RC4 startup heading for the current scientific-core code.

RC26 and RC26B add four source-typed, study-bounded ecological notes on `Limnonectes magnus` and `Baletemys kampalili`, bringing 96 priority species to **452 cited facts** and **303 species-reference links**. The full bibliography and original source links remain offline, with limitations shown per study. Missing notes remain missing and earthworm bait is never asserted as confirmed prey.

Home, Survey Setup and Survey History now use larger accessible action spaces at 200% Android text. RC26's first Android 14 emulator run had **14/16 tests passing**, with a clipped Habitat tab and a premature Setup measurement. RC26B passed 15/16 with a remaining Habitat tab row clip. RC26C explicitly matches tab-row and button heights; its separate 16-test emulator acceptance gate remains open. **Do not claim RC26C emulator success without its actual instrumentation archive.** The source/JVM build and APK signing continuity passed. Real historical owner records, GNSS, camera/WAV long-duration use, TalkBack and on-phone restoration are not field verified.

See [RC26 scientific and interface audit](docs/LIKAS_RC26_SCIENCE_FIELD_UI_AUDIT.md), [physical handset QA matrix](docs/LIKAS_RC26_PHONE_FIELD_QA.md) and [current handoff state](docs/LIKAS_PROJECT_STATE.md). Startup animation and frog icon development remain isolated.

---

# LIKAS v0.27.3 RC4 | Living forest startup

**Preview candidate:** `0.27.3-rc4` (versionCode 49). Startup-only changes: the approved tropical forest now has native Android animated ripples, waterfall streams, moving clouds, day birds, night fireflies, moving sun light and a halo following the painted moon. The startup uses a wordmark only with **Explore. Record. Conserve.**, and shows the actual installed version in the top-right corner. The previously approved pixel frog remains the Android launcher icon. Motion is disabled by Android reduced-motion preference and slows in battery saver mode.

The full local survey engine, SQLite schema 27, GPS/elevation records and scientific exports are unchanged. See [RC4 startup QA](docs/QA_v0_27_3_LIVING_FOREST_STARTUP.md) for device testing and performance limitations. Live handset testing remains open.

---

# LIKAS v0.27.2 RC3 | Startup menu refinement

**Review candidate:** `0.27.2-rc3` (versionCode 48), preserving app ID `org.phwildlife.survey.beta08` and SQLite schema 27. This is **part 1** of the screen-by-screen UI audit.

The startup landing uses the approved pond-frog logo, the full-resolution day/night forest, restrained contrast and a small action panel. **Open LIKAS** leads to Home; a separate **Resume survey** appears only for an unfinished event. Developer-style version text and misleading conditional labels are removed. An unreadable survey archive produces a warning, not a false empty state.

See [startup screen audit](docs/QA_v0_27_2_STARTUP_MENU.md). The Home menu and later screens are not part of this patch. Handset testing remains open.

---

# LIKAS v0.27.1 RC2 | Field screen polish and truthful GPS accuracy

**Development candidate**: `0.27.1-rc2` (versionCode 47). Retains app ID `org.phwildlife.survey.beta08` and SQLite schema 27.

This candidate refines Projects, Station Progress, Dashboard, Scientific Data Review and live recording headings. It keeps existing approved frog artwork and full-resolution day/night forest images. A missing station GPS accuracy now displays **accuracy unavailable**, never `±0 m`. The project-wide station register is distinguished from planned survey station visits.

Survey methods, observations, signed sound/photo evidence, Complete/Incomplete/Not Counted outcomes and archives are unchanged. See [RC2 field QA](docs/QA_v0_27_1_FIELD_POLISH_RC2.md). Android/JVM and Python automated checks run in CI. **Handset, signing continuity and backed-up upgrade testing remain open release gates.**

---

# LIKAS v0.27.0 RC1 | Philippine wildlife surveys

LIKAS is an offline-first Android field-research app for Philippine birds, reptiles, amphibians and mammals. It stores individual observations, sampling effort, GNSS quality, acoustic evidence, device histories and archived survey outcomes without requiring internet during fieldwork.

**Current build:** `0.27.0-rc1` (versionCode 46, Android app ID `org.phwildlife.survey.beta08`). This is a **release candidate**, not a certified stable field release. Physical-device, long-session power and old-installation restore tests are still required.

## Field workflow

1. On Home, select Birds, Herpetofauna, Mammals or Custom.
2. Choose the survey method, project, route and optional study-specific completion rules.
3. Use automatic verified GPS, or explicitly start without a fix and retain missing-location provenance.
4. Record a point/transect or structured visit, identify or mark unknown species, attach photographs or sound evidence, and record method-specific effort.
5. Select **End Survey** at any time. Finish **Complete** only when method and study criteria pass. **Incomplete** keeps partial work. **Not Counted** records the canceled event and reason.
6. Review the archived results and export a research ZIP with CSV data, original retained photographs, WAV files and evidence links.

A ZIP export is a **research-data export**, not a proven full reinstallable app backup. Copy it to a secure location before upgrading. Do not uninstall a field installation without checking your recovery route.

## Release-candidate changes

- Smaller phones and increased system font sizes receive stacked Home cards, larger readable actions, and a responsive observation footer.
- Station progress and GNSS path summary database queries run on a bounded background worker. Survey clock timing and GPS fix validation remain unchanged.
- ZIP export reads from its dedicated database connection.
- SQLite v27 adds wildlife-group scope to new study templates. Existing templates remain marked Shared legacy, and survey rule snapshots stay unchanged.
- CI validates real SQL migrations from v23 to v27, retained observation/trap/acoustic records, SQLite backup/restore roundtrips, and the Android APK/JVM tests.
- The release candidate combines the feature PR series in [integration PR #31](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/pull/31).

See [v0.27 screen and release QA](docs/QA_v0_27_INTEGRATION_SCREEN_MATRIX.md) for the screen-by-screen acceptance matrix, data-integrity tests, power measurements, limitations and launch gate.

## Older development history

The sections below document earlier feature milestones and their source limitations at the time. They are historical notes, not the current v0.27 specification.

---

# LIKAS v0.19.3: source-checked habitat and persistent species picker

- Introduces color-coded 0–100 screening index bars with Likely, Unlikely,
  Insufficient Data and Not Scored text labels; **not probability of occurrence**.
- Adds four source-checked habitat profiles, citing Animal Diversity Web,
  two amphibian references and a tarsier field study. Remaining 1,499 taxa
  are unassessed for published habitat/elevation. No invented values.
- SQLite schema 20 stores source URL and evidence notes beside each habitat
  profile, without altering historical observation data.
- Saves Sort & Filter (A–Z, family, genus, high/low index and status filter),
  selected habitat and measured elevation/uncertainty **per survey**.
- Species detail pages show source references and ecological evidence when
  they exist. Unassessed fields remain unfilled.
- See `docs/HABITAT_ECOLOGY_COVERAGE_v0_19_3.md` and
  `docs/QA_v0_19_3_HABITAT_PROFILES.md`.

This is a beta feature. Screenshots and physical-phone survey tests are
required before promotion to a field release.

# LIKAS | Philippine Wildlife Survey v0.18.3 Source Evidence Beta

## Species detail and regional evidence
- The Offline taxonomy screen opens read-only species reference pages.
  Search scientific or common names. The screen shows classification,
  source-dated IUCN and Philippine status, taxonomic authority, explicit
  island/province distribution text, aliases and original source links.
- In a survey recording screen, hold the current species field to inspect
  that species without changing the field's raw identification.
- SQLite schema 16 adds a separate `taxon_occurrence_evidence` table,
  preserving source/version, original distribution quote, province/island
  label and review state. Survey records and historical names are not migrated.
- Researchers can import attributed island/province occurrence CSVs only
  after installing the matching source taxon catalog. Unknown taxon keys
  cause the entire evidence import to roll back.
- The Offline taxonomy screen can browse Mindanao source-listed taxa and
  four Davao provinces independently, with a derived Davao Region union.
  **These are source listings, not sightings or asserted presence at a GPS
  station.** Missing location mentions are not absence records.
- The private user-supplied Emerson Sy 2025-1/2026-1 source-derived import
  bundle is delivered separately. No full original checklist has been
  committed to GitHub or packaged within the APK pending redistribution rights.

## Philippine birds: national occurrence crosswalk
- AviList v2026 is the global taxonomic authority, licensed CC BY 4.0.
- The WBCP Records Committee 2026 status report lists 765 Philippine bird
  species, but its complete 2026 checklist had not been published as of
  October 1, 2026. The public 2025 checklist is restricted to personal or
  scientific reference without broader publication rights.
- `scripts/reconcile_ph_birds_avilist.py` matches an authorized Philippine
  occurrence CSV to AviList accepted species names. It requires pinned file
  hashes and explicit source column names. Taxonomic split/conflict cases
  go to a review CSV and are never automatically guessed.
- **No comprehensive AviList-aligned Philippine bird list is yet bundled.**
  The script is a transparent preparation step, not a manufactured checklist.

See `docs/QA_v0_18_3_SPECIES_EVIDENCE.md` for scientific and device tests.

# LIKAS | Philippine Wildlife Survey v0.18.1 Auto GPS Beta

## Automatic field acquisition
- After the researcher finishes survey setup and selects Start Survey,
  the GNSS preflight automatically begins the survey on a **fresh
  GPS_PROVIDER fix with reported horizontal accuracy strictly below 10 m**.
  The second green Start control is no longer needed. A guarded launch
  prevents multiple survey activities from repeated location callbacks.
- Planned and custom station GPS acquisition follows the same automatic
  rule after selecting the sampling station. Missing-GPS override remains
  an explicit documented field decision.
- No automatic start occurs while the preflight screen is paused. Existing
  background GNSS operation and the ongoing location notification remain.
- Choosing **Choose checklist area by GPS** now automatically selects the
  unambiguous containing administrative province's coarse island checklist
  scope. Near-boundary and uncertain polygon matches leave the manual scope
  intact; the app does not guess the nearest checklist across an island boundary.
- National candidate taxa remain searchable, with separately reviewed local
  matches ranked higher. Selecting an area is **not** proof of a species'
  occurrence at the observer's location.

## Versioned taxonomic synonyms
- SQLite schema 14 adds `taxon_synonyms` linked by accepted catalog keys,
  leaving historical raw identifications unchanged.
- The offline taxonomy page accepts a versioned synonym CSV:
  `accepted_taxon_key,alias_name,source,source_version`.
- Import validation rejects unknown accepted keys and rolls back the entire
  transaction. Searching an alias suggests its current catalog name.
- No synonym data have been invented or bundled. Complete Emerson Sy reptile
  and amphibian records still need original permissioned source files.
  The comprehensive Philippines bird occurrence crosswalk to AviList 2026
  is still pending source reconciliation.

See `docs/QA_v0_18_1_AUTOMATIC_GPS.md` for real-handset acceptance checks.
This is a development beta, not a fully verified national wildlife checklist.

# LIKAS | Philippine Wildlife Survey v0.18.0 Taxonomy Beta (development)

## Taxonomy coverage status
- **Mammals:** same version-pinned Philippine country-filtered ASM MDD v2.5
  candidates that were bundled in v0.16/v0.17 build workflow.
- **Birds:** AviList v2026 is the intended taxonomy under CC BY 4.0.
  A vetted Philippines occurrence list is not yet bundled.
- **Amphibians:** Emerson Y. Sy 2025-1 source located. Complete verified
  author file and redistribution permission pending.
- **Reptiles:** Emerson Y. Sy 2026-1 source located. Complete verified
  author file and redistribution permission pending.

National membership is not evidence of local presence. Verbatim
identifications, unfamiliar species and unverified regional taxa remain
recordable.

### Catalog performance changes
- SQLite schema 13 adds scientific-name, common-name and source/version
  indexes. No observation table is dropped.
- Offline taxon autocomplete runs a bounded background query rather than
  materializing the entire candidate catalog as Android views.
- Search supports common and scientific names, literal punctuation,
  favorites and recent verbatim identifications.
- Offline checklist page shows installed counts by bird/amphibian/reptile/
  mammal and the actual installed source/version distribution.
- `scripts/validate_ph_taxa.py` validates a permissioned and reviewed
  source CSV with pinned SHA-256, prevents duplicate identifiers/names,
  and rejects regional evidence not separately vetted.

See `docs/PHILIPPINE_TAXONOMY_SOURCES.md` for source citations and
`docs/QA_v0_18_TAXONOMY.md` for acceptance checks.
No complete Sy checklist or global AviList taxonomy has been incorrectly
packaged as a verified Philippine or provincial occurrence list.

**Warning:** This remains a development beta. Export survey data before
upgrade, preserve the original signing certificate and validate performance
and taxonomy imports on a real Android phone.

# LIKAS | Philippine Wildlife Survey v0.17.1 Beta (UI review branch)

LIKAS is the working app identity. This development branch is a UI and stability
pass, not a change to the Android package or installed SQLite records.

## v0.17.1 visual and stability refinements
- Original Philippine forest scene with irregular tree crowns and tapered palms.
- Less dim daytime colors and a clearer disabled Continue Survey action.
- Home header alignment, lighter status bar, tighter survey-group cards.
- Recorder has fewer persistent instructions. Species filters, identification confidence
  and session/vibration controls are folded into short expandable sections.
  The Save Observation action remains pinned on screen.
- History and observations load 40 entries per batch. Generic checklist CSV
  parsing and insertion run on background threads, preserving transaction rollback.
- Notification Stop GPS choice survives Activity recreation and avoids unintended
  receiver restarts. Explicit GPS retry is available.
- Day/night rendering and Continue Survey display rules have new JUnit tests.

The current source is still a **development field-test candidate**, not a
scientifically validated release. See docs/QA_v0_17_1_LIKAS.md.

## v0.17 LIKAS launch and field usability
- Original leaf-and-compass vector mark and adaptive Android launcher icon.
- Startup screen with **Start New Survey**, **Continue Survey** (only active for
  an unfinished session) and the installed version.
- Original Canvas-animated Philippine lowland rainforest with emergent crowns,
  buttressed trunks, palms and mist. The palette changes at local day/night
  boundaries. Rendering stops when the launcher screen is hidden.
- A shorter field Home menu with management screens folded under **Data & Tools**.
- Cleaner survey setup wording, fewer permanently visible instructions and a
  collapsible taxonomy CSV guide.
- Reduced database polling from the recording clock and asynchronous draft
  preference writes. Large survey ZIP exports now run off the UI thread.
- Observation GPS is frozen at detection mark or camera capture, with invalid
  or missing fixes kept null. Taxonomy provenance is read from the catalog
  instead of guessed from the displayed label.

**Status:** CI and Java tests do not replace device tests. Preserve the installed
signing identity and back up local survey data before installing.
For acceptance criteria, read `docs/QA_v0_17_LIKAS.md`. The complete national
species catalog and additional background import work remain future stages.

Offline Android wildlife field application for Birds, Herpetofauna and Mammals. Independent of SMART and KALUMBATA. No map UI in this release.

## v0.16.1 GNSS field corrections

Live GPS acceptance now checks Android elapsed realtime. The phone's wall
clock no longer rejects an otherwise valid fix whose satellite timestamp
appears slightly ahead. The continuous location service stays active after
pressing Home or locking the screen. Android shows a foreground GPS
notification, subject to notification permissions. The service may stop after
force-stop, reboot, permission changes or device power restrictions.

Sampling stations use the current live receiver fix. Valid readings under
10 m start a station immediately. A compact GPS screen appears only when
the current fix is insufficient. The full satellite sky map remains on the
initial survey GPS screen. An explicit missing-GPS station start records a
quality flag and no fabricated coordinates.

## v0.16 survey-start GNSS accuracy gate (in development)
All newly configured surveys pass through `SurveyGpsActivity` before `MainActivity`
creates a survey row or begins the clock. An actual live GPS_PROVIDER fix must have
device-reported horizontal accuracy **strictly better than 10 m**, be at most
10 seconds old, contain valid coordinates, and not be marked as a mock fix.
The GPS screen shows current reported accuracy, retains neither a stale fix
nor a previous station coordinate, and permits explicit retry and cancellation.
GPS problems can block survey launch in dense forest. This policy does not
guarantee true horizontal position error better than 10 m.

Survey-start coordinates, accuracy, and fix UTC timestamp are retained on
the survey event in SQLite schema 12, and exported in `surveys.csv`. The
migration adds columns without deleting historical records. Older events
keep these fields null.

## GNSS sky map and station-level acquisition (v0.16)
The GPS preflight includes a north-up satellite sky plot with cardinal directions,
30° and 60° elevation rings, color-coded satellite constellations, readable
satellite identifiers and distinct rings around satellites contributing to the
fix. Marker size indicates GNSS signal C/N0. Satellite metrics are diagnostic
and do not independently prove horizontal position accuracy.

Before each planned point-count visit, manually selected bird station visit,
or structured herpetofauna/mammal station, the same preflight screen acquires
a fresh GPS_PROVIDER fix. The station clock starts after the observer accepts
a valid fix with reported horizontal accuracy **strictly below 10 m** and
age no greater than 10 seconds, or explicitly selects a missing-GPS override.
The latter leaves station coordinates NULL and records a GPS-quality warning.
Survey-start GPS is never reused as the next station's location. Existing
route station IDs and historic coordinates remain intact on repeat visits.
Prior v0.15 visits can retain older, less precise historical measurements.

Physical-handset testing under canopy and in open sky remains required,
including checking permission prompts, GNSS diagnostics and explicit
missing-GPS override. A phone receiver cannot guarantee actual 10 m error.

## Taxonomy status and data sourcing
The original starter taxa are incomplete for birds and herpetofauna.
GitHub Actions downloads the **official MDD v2.5** CSV from Zenodo, verifies
its published MD5 checksum, then filters extant non-domestic species with
Philippine country membership. The CI build bundles this subset into the APK.
On a fresh install or upgrade to schema 12, the app seeds the attributed
offline mammal catalog automatically without replacing user-imported names.
The current checksum-pinned build produced **246 Philippine mammal candidates**, including two entries with uncertain country membership flagged for review.
The existing manual MDD CSV importer remains available. Local source builds
that skip the build-time preparation script do not bundle this asset.

MDD `sciName` is a genus-epithet key separated by an underscore. Displayed
scientific names are converted to spaces; pre-existing MDD catalog names are
normalized during migration. MDD's uncertain national membership and flagged
taxa stay **unreviewed**. No Philippines-wide list establishes occurrence on
an individual island, province, or sampling site.

Emerson Y. Sy amphibian v2025-1 and reptile v2026-1 are selected references,
but complete machine-readable files and reuse permissions must be verified
before bundling. A structured, attributed CSV import path is already present.
AviList v2026 is the selected bird taxonomic backbone; locality assessment is
a separate evidence layer. The complete national species datasets are **not
yet bundled** in this branch.

See `docs/QA_v0_16.md` for GPS and taxonomy acceptance tests.

## Q2: repeatable routes with permanent stations
For **bird point-count** monitoring, survey setup now offers:
- **New route: generate unique stations**, with optional (on by default) **Save this route for future monitoring**, plus a route name.
- **Existing saved routes in the selected project**. Selecting one loads its recorded planned length, spacing and number of stations.

An initial 2 km route with 250 m spacing creates nine permanent IDs at chainages 0, 250, …, 2000 m. Saving a route template registers those same station IDs in `route_template_stations`. A subsequent survey using the same template starts fresh point-count visits at those *same station IDs*, not new sites. Each visit gets its own timestamp, point-count ID, GPS/accuracy and detection records. Historic station coordinates remain unchanged, and GPS deviation is flagged for review.

Only **point-count routes** are reusable as fixed monitoring stations. Bird line-transect and herpetofauna VES route lengths remain *design values only*. Their GIS tracks are captured per field event, not silently reused as geographic waypoints.

The database migration to schema 11 adds `route_templates`, `route_template_stations`, and `surveys.route_template_id`. Original surveys, station IDs, observation data and archived photographs are not discarded. ZIP export now includes `route_templates.csv` and `route_template_stations.csv`.

## Q3: scientific data quality and stability audit
Home now has **Scientific data review**, a read-only checker of:
- SQLite quick_check and orphaned survey, observation and unit links
- Invalid or missing station coordinates and absent GPS accuracy
- Missing, cross-project or incomplete reusable route station records
- Survey planned/actual station count mismatches
- Point-count completion that falls short of actual active station duration
- Survey completion despite unvisited stations
- Duplicate use of unrelated site identity, invalid detection counts and contradictory unknown taxa
- Large differences between repeat-visit GPS positions and stored station reference positions, considering uncertainty
- Missing photo files referenced by the observation evidence index

The audit never deletes or changes source records. **`data_quality_flags.csv`** is included in the ZIP export. A clean automated report is not proof of ecological validity. Direct handset GPS, camera behavior, migration integrity, field methods and taxonomic identification still require field review.

**Critical scientific correction:** for station-based monitoring, `observer_effort_minutes` is calculated from completed sampling-unit intervals with documented pauses removed. Travel between stations is not counted as stationary effort. The whole-session timer keeps recording actual elapsed time, and the second session clock is explicitly labeled *Session excluding pauses*, not sampling effort.

Automated Java tests check route planning arithmetic, including 2 km / 250 m = 9 stations, nondivisible route distances, zero spacing and numeric overflow. GitHub Actions runs unit tests before assembling the APK.

## Species catalog and sources

The app's existing built-in taxonomy is still a **small, unreviewed starter list**, not an exhaustive official Philippine species list.

### Mammals
Use the **American Society of Mammalogists Mammal Diversity Database (MDD) v2.5**, July 28, 2026, for up-to-date mammalian scientific names and *country-level* distributions.

In Home > Offline species checklist, choose **Import Philippine mammals from MDD v2.5 CSV**. Select the exact official source file `MDD_v2.5_6904species.csv` from the [MDD Zenodo record](https://zenodo.org/records/21654811). The importer verifies fields, selects records where `countryDistribution` explicitly includes the Philippines, and excludes taxa marked recently extinct or domestic. It imports taxon ID, scientific/common name, family, provenance and version into the local catalog. No province-level or faunal-region association is invented. Synonyms and reviewed locality evidence can be appended in later versions.

MDD v2.5 source: [mammaldiversity.org](https://www.mammaldiversity.org/) and [Zenodo 21654811](https://zenodo.org/records/21654811).

For Philippine and island-specific occurrence verification, consult [Field Museum Synopsis of Philippine Mammals](https://www.fieldmuseum.org/project-narrative/science/synopsis-of-philippine-mammals) and [Philippine Mammal Project publications](https://www.philippinemammalproject.com/publications.html). The Field Museum site is not a replacement for MDD v2.5 taxonomy updates.

### Amphibians and reptiles
Emerson Y. Sy maintains:
- **Checklist of amphibians of the Philippines**, version **2025-1**, updated February 25, 2025
- **Checklist of reptiles of the Philippines**, version **2026-1**, updated February 28, 2026

Those versions are documented on [Emerson Sy's research profile](https://www.researchgate.net/profile/Emerson-Sy). The app's attributed CSV importer is prepared to receive curated entries from the checklists. **Their full datasets are NOT bundled in this release.** Distribution, synonymy and redistributable source licensing need separate review. The [Reptile Database](https://www.reptile-database.org/data/index.html) provides a useful independent taxonomic cross-check. Avoid conflating lists of invasive/exotic pet trade species with native species inventories.

### Birds
AviList v2026 provides an attributed global avian taxonomic backbone under CC BY 4.0, but its global names alone cannot establish occurrence in Mindanao or other Philippine regions. Source: https://www.avilist.org/checklist/v2026/

Regional suggestions in the UI are **positive evidence only**. Any unassessed name remains available with the regional suggestion filter off.

## Field installation
v0.15 keeps Android application ID `org.phwildlife.survey.beta08` and the established persistent signing key. **Export your existing ZIP before updating in place**. Do not uninstall the older version. New SQLite version 11 keeps previous observations.

See `docs/QA_v0_15.md` for migration, saved routes, data quality, scientific methods and import checks. CI unit tests and APK compilation are not substitutes for physical-phone testing.
