# APO / LIKAS RC110 | Native emulator visual and usability audit

**Audit date:** 10 November 2026 (Asia/Manila)  
**Audited production baseline:** RC110 `0.27.3-rc110-highres-photo-galleries`, versionCode 170, package `org.phwildlife.survey.beta08`, SQLite29. App feature code was unchanged in this audit.  
**Audit-only source:** `d53e024aeb64bc8050b03c47b8b53880a3c5f332`.  
**Emulator evidence:** [GitHub Actions run 42757693097](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42757693097), passed; native screen artifact 13736173698 contains **68 native Android PNG screenshots**, 34 each at 360 × 800 dp / 100% text and 320 × 720 dp / 200% text. The separate RC110 signed build run 42757692986 also succeeded. The parallel pull-request run 42757698106 failed in an intermittent inherited Espresso saved-species window-focus test before new audit captures, not in code compilation.
**Methods:** Live Android API34 emulator; screenshots from actual Activity view trees; no synthesized UI screenshots. Inspected `WelcomeActivity`, `MainActivity`, `LikasSettingsActivity`, `PhLibraryDirectoryActivity`, `PhLibraryBook01`, `LikasDiscoveryActivity`, `PhLibraryDriveGallery`, `PhLibraryStatusBadge`, `PhLibraryHabitatFilter`, design systems and owner-approved APO icon lock. Captured Home, BioTools, Surveys, Field Records, Analysis, Utilities, PHMaps, BioDiscover, Settings, three group setup forms and three group recorders. Existing PHLibrary captures were also reviewed.

## Priority findings

### V01 — High | Field entry is buried below a large editorial Home card
**Observed:** APO Home opens with `ApoSpeciesOfDay` before the four BioTools/PHLibrary/PHMaps/BioDiscover modules. On the 360×800 and especially the 320×720/200% screenshot, the first screen shows almost exclusively the daily species feature. BioTools requires scrolling. **Exception:** when a survey is already active, Home puts Resume first, which is correct.
**Revamp:** Field-first header with a prominent Start/Resume Survey action and a concise PHLibrary shortcut. Convert daily wildlife feature into compact 120–180 dp thumbnail/teaser that opens the full account. Do not remove the scientifically sourced content or approved APO emblem.

### V02 — High | Critical selection labels truncate under 200% system text
**Observed:** On 320 dp survey setup, native method spinner is cropped, e.g. `Visual encounter (V…` and region `All Philippines / no…`. The very controls that determine method and geographic checklist scope do not expose their full chosen values. 200% recorder labels also truncate `Horizontal distance (…` and `Life stage not rec…`. Group chip `Amphibians` breaks into `Amphibia / ns`.
**Revamp:** Use two-line accessible selection cards for method and region, an explicit full-label selected-value summary, and flexible full-width vertically stacked taxon-group segmented choices at 200% text. Allow longer text/hints to wrap; do not silently shrink real system-scaled type. Keep survey codes and region semantics unchanged.

### V03 — High | Two visual systems look like different products
**Observed:** APO Home and BioTools use plain editorial type and thin rules; PHLibrary uses separate beige illustrated cards and green badges; Settings abruptly changes to thick outlined pill cards with pixelated green icons. `UiKit.BG` is RGB(247,248,242), `PhLibrary2Ui.PAPER` RGB(245,242,233), and `ApoHomeUi` defines another ink/muted palette. Activity status and bottom navigation bars are not always visually integrated, giving a thick black footbar on some screens.
**Revamp:** Consolidate tokens for surface, ink, muted text, radii, buttons, body type and bottom/system bars. Restrict pixel art to approved brand artwork where appropriate. Use one icon style for navigation and tools. Keep the SHA-locked mountain ribbon untouched.

### V04 — High | PHMaps and BioDiscover are prominent unfinished dead ends
**Observed/source:** Home gives equal prominence to `PHMaps` and `BioDiscover`. PHMaps only links to the same Projects & Stations and Site Habitats screens accessible via BioTools/Field Records; full map layers are in development. BioDiscover contains almost entirely a paragraph saying the feed is unavailable. Both screens leave large empty areas in the emulator.
**Revamp:** Retain roadmap honesty, but demote incomplete modules to a visible `Coming later`/preview area until they perform a distinct useful task. Expose existing sites/habitats through their established BioTools routes, not a duplicate nominal map hub.

### V05 — Medium/high | Too many parallel ways to reach the same species and habitat information
**Observed/source:** PHLibrary Home presents a search field; the Species Atlas opens another search/filter screen; the full checklist and separate field guides use other paths. Site habitats and Projects & Stations appear in multiple hubs. Distinct catalog scopes matter but are not obvious from short labels.
**Revamp:** One primary species search entry and one consistent account page, with scoped tabs/filters for Book 01, broader national checklists and field-guide features. Cross-links should open the same record with preserved search context, not duplicate browse workflows. Keep different source authorities and archived regional scopes visibly distinct.

### V06 — Medium/high | Dense scientific text is visually unscannable
**Observed:** Habitat account screenshot presents a large nearly continuous paragraph with little hierarchy. At 200% text it requires prolonged scrolling, and References displays raw long citations with clipped lines/links. Atlas result cards stack two badges vertically at 200%, occupying most of screen.
**Revamp:** Display verified concise summary, structured labeled sections (identification, habitat, distribution, sources), followed by an expandable `Full source account` preserving original text. For citations, separate citation metadata, stable link, and unverified metadata badges, without inventing or silently rewriting references.

### V07 — Medium/high | Some presentation wording can misstate scientific meaning
**Confirmed source:** Atlas puts book rarity and IUCN on adjacent unlabeled badges; identity is only explicit in accessibility description/tap. `PhLibraryHabitatFilter` matches terms inside narrative text, but UI presents group labels such as `Riparian · 54 species`. A narrative mention is not a verified species–habitat relation.
**Revamp:** Use visible labels `Book rarity`, `IUCN global` and a clear caveat `accounts mentioning this habitat`. Never state habitat occupancy, habitat selection or local presence from these search results.

### V08 — Medium | Main-thread 1920px JPEG decoding in offline species gallery
**Source-confirmed, runtime not benchmarked:** `PhLibraryDriveGallery.decode()` uses full-size ARGB_8888 `BitmapFactory.decodeStream` synchronously, including on gallery swipe and cover rendering, with a 24 MB cache. The 333 JPEG assets are 127,507,271 bytes packaged and APK ~141 MB.
**Revamp:** Asynchronously decode/downsample thumbnails and prefetch only adjacent frames, bound memory by device class and collect cold-open, scroll frame-time and peak-heap metrics on a representative field handset. Do not claim jank was measured in this audit.

### V09 — Medium | Field recorder still spends too much vertical space on UI scaffolding
**Observed:** The recorder at 360dp shows separate Session/Active/Elapsed, two near-duplicate missing-GPS status phrases, a large observation form and a persistent multi-row footer. On 320dp/200% the footer consumes a substantial fraction of the limited viewport, pushing species/count and evidence entry behind scroll. A wide `ZIP` or `All data` export button is prominent in primary recorder top bar while field detection is the main task.
**Revamp:** Keep Save Observation, Review and End Survey available at every stage and retain active effort/GPS warning. Compact status to one readable line plus expandable details, distinguish unavailable start GPS from real fix, reduce redundant narrative microcopy and move export to research tools or secondary menu. Test one-handed/gloved 320dp use with actual GNSS.

### V10 — Medium | App identity inconsistency and underdeveloped empty states
**Confirmed:** Android package label is `Apo` and startup uses owner-approved APO mountain, but Settings still says `About LIKAS` and dialog `LIKAS`. Field Records shows `0 saved surveys (all outcomes) · 0 observations (excluding deleted records)`; empty BioDiscover, Maps and Saved Species pages have dead space and generic copy.
**Revamp:** Update user-facing branding strings consistently to `APO` while preserving package, signer, record IDs and archival provenance. Convert developer-centric count qualifiers to concise status with `Details` help, and offer a meaningful route from an empty saved collection.

### V11 — Medium | No proven low-glare/night field mode
**Confirmed source:** Android manifest sets a Material **Light** no-action-bar theme, most screens use hard-coded light surfaces and no visible dark mode control in Settings. No actual dark-mode or TalkBack validation in this audit.
**Revamp:** Add a restrained accessible night theme for nocturnal herpetofauna/bat field use and explicit large-text/TalkBack checks, without inverting approved media incorrectly or weakening contrast.

### V12 — Low/medium | Old repository and help terminology can mislead developers
**Source:** RC110 active branch `README.md` still leads with RC84 and stale RC85 development state, `AGENTS.md` leads with RC86. Several technical `LIKAS` class names are historical and should not be blindly renamed; user-facing `About LIKAS` is a genuine defect.
**Revamp:** Keep a short authoritative current-release section at top of the technical docs, preserve historical entries below, and distinguish code-internal historical naming from user-visible labels. Avoid broad refactoring of scientific functions just for naming.

## Safeguards and limits
No production Java sources, database schema, official artwork, survey rules, user photographs or signer were modified. The audit added only test instrumentation and temporary screenshot artifacts. Results reflect two API34 emulator configurations and synthetic records, not hardware GNSS, camera/audio, high-contrast sunlight, battery/thermal conditions or accessibility-service use. The 333 owner photos' species identifications, photographer rights and redistribution approvals remain unresolved. The approved gray `?` placeholder must stay neutral where no qualified photograph exists, but can be visually smaller.

**Recommended order:** (1) Home action/primary navigation; (2) 320dp/200% form truncations; (3) cohesive design tokens and Settings; (4) scientific labels/reference readability; (5) gallery performance and low-light field ergonomics; (6) consolidate duplicate/in-development hubs. Screen-by-screen redesign should be reviewed before changing production code; preserve RC109→RC110 update and no-data-loss test coverage.
