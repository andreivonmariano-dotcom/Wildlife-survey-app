# LIKAS RC39 | Application audit and review

**Audit date:** 29 October 2026, Asia/Manila  
**Source baseline:** `feature/likas-core-fieldwork-post-rc4`, app build `0.27.3-rc39-field-guide-studio-preview`, versionCode **99**, SQLite **27**  
**Confirmed QA:** Scientific Core QA run **40875251725** succeeded. Android unit/instrumented application code compiled. On-device test execution has **not** been verified.  
**APK inspected:** `LIKAS-v0.27.3-RC39-field-guide-studio-debug.apk`, **9,670,010 bytes**, SHA-256 `ecd6c43f617e18008b497a4c396dcc502e93e71a1a1ee023e4462ba64a94e16e`. ZIP contents and manifest presence checked, without installing the APK.  
**Scope:** static Android/Java source review, project product/scientific specification, build evidence, and APK archive inspection. Not a screenshot audit, stress test, penetration test, or field-device acceptance.

## Executive judgment

RC39 has a coherent offline fieldwork foundation and preserves several non-negotiable survey controls. The latest core QA passed, and the exact APK contains the 96 curated research accounts, 15 habitat profiles, six individually reviewed dated conservation records, and 58 authorized offline JPEGs. The newest illustrated gallery serves only the 96 curated accounts, not the larger searchable national taxon catalog. The product must still be treated as a **development preview**, primarily for the following evidence-backed reasons.

## Findings and recommendations

### A01 | High | Dashboard summary disagrees with legacy survey interpretation (confirmed source inconsistency)

**Evidence:** `HistoryActivity.OUTCOME_SQL` derives a complete/incomplete outcome for older survey rows whose `outcome` is NULL. `DashboardActivity.render()` counts `WHERE outcome='complete'`, `WHERE outcome='incomplete'` and `WHERE outcome='not_counted'` literally. Its headline `SELECT COUNT(*) FROM surveys` is labeled **Surveys in archive**, although the same count includes active surveys.

**Impact:** Older complete/incomplete survey records can appear in History but be excluded from the Dashboard's completed-visit, detection and group summaries. An active event is included under a label implying archiving. This is a scientific reporting correctness issue. Whether specific installed data contain NULL outcomes requires a database fixture or field-device verification.

**Fix:** Use one shared, tested read-only outcome expression for History, Dashboard and exports. Do not rewrite historic outcomes silently. Reconcile reported archive/active/excluded totals and add fixtures for legacy NULL outcomes, complete, incomplete, not counted and zero-detection sessions.

### A02 | High | Gallery rebuild and photo decoding on UI thread (source-confirmed pattern, runtime impact unmeasured)

**Evidence:** `LikasDiscoveryActivity.render()` clears all contents and constructs a fresh horizontal rail for each category. Unfiltered Species builds 96 group cards plus the separately presented conservation shelf. Search calls `render(false)` after each 180 ms delay. `shelf()` builds each card synchronously, and `OfflinePhotoEvidence.thumbnail()` uses `BitmapFactory.decodeStream` directly during card construction. The 4 MiB LRU cache is bounded, but displayed ImageViews continue holding bitmap references.

**Impact:** Slow first gallery entry, keyboard lag during filtering, transient memory pressure and scroll stutter are plausible on midrange field phones. These effects are not established by a measured frame trace.

**Fix:** Render only the visible cards, recycle views, precompute name/index filtering and load/downsample licensed thumbnails off the UI thread. Keep offline-only operation, photo provenance and reduced motion. Capture benchmark frame times, heap usage and cold-start timings on 320dp and midrange devices.

### A03 | Medium | Less motion does not cover the survey timer (confirmed source defect)

**Evidence:** `MainActivity.status()` sets `eventView.setAlpha(!surveyPaused&&near?(now/650%2==0?1f:0.45f):1f)` for the last 30 seconds. This ignores `likas_ui.reduced_motion`; `UiMotion` and `LikasDiscoveryActivity.moveCard()` do honor that preference.

**Impact:** A user selecting Less motion still gets a repeating flashing field status.

**Fix:** Keep the text and color warning but set opacity to 1 when reduced motion is selected. Provide a nonanimated final-30-seconds alert and a source regression test.

### A04 | Medium | Search and category usability need actual device proof (mixed confirmed and unverified)

**Evidence:** `LikasDiscoveryActivity.shelf()` gives Previous/Next arrows **46dp** width, below a recommended 48dp touch target. Its search bar and card shelves are freshly rebuilt during filtering. Tabs visually use disabled states and an appended bullet for the selected tab rather than explicit selected-state semantics.

**Impact:** Small tap targets and ambiguous selected states can affect outdoor gloves, accessibility services and large-font users. Actual clipping, hit-target size and TalkBack behavior still need device screenshots and execution.

**Fix:** Increase arrow target width to at least 48dp, label controls by selected role/state, test 320dp and 200% font and run TalkBack navigation from Library through a filtered species account.

### A05 | Medium | Home hierarchy and navigation labels remain inconsistent (source-observed)

**Evidence:** `LikasPortalUi.hero()` occupies approximately 180dp, followed by three destination panels with 136dp minimum height each. On small screens the Settings entrance will likely require scrolling. `WelcomeActivity.addHomeRecentSurvey()` labels a card **Latest record**, but clicking it opens unfiltered `HistoryActivity`. `FieldBackupActivity` and `DataManagementActivity` announce **Back to Workspace**, despite entry from Settings. `Rc7SpeciesLibraryActivity` announces **Back to Home**, despite entry via Library and Discover.

**Impact:** The three main spaces compete for the first screen, and spoken Back directions misstate the immediate destination.

**Fix:** Test the first viewport on real devices, condense the hero for repeat sessions or keep all three destinations visible, deep-link Latest record to its event, and derive accessible Back labels from actual navigation.

### A06 | Medium | In-place upgrade and executed Android acceptance still absent (confirmed QA gap)

**Evidence:** The successful Scientific Core QA run includes Python tests, JVM tests, Android compilation and AndroidTest APK compilation. No current Android 14 emulator execution, 320dp/200%-font screenshot review, TalkBack pass, or physical RC38-to-RC39 upgrade with real existing survey data has been recorded.

**Impact:** A compiling signed APK does not prove that navigation, permission prompts, GPS, backups, Android foreground services, historical evidence files or small-screen text work correctly on a phone.

**Fix:** Install over a backed-up RC38 database with real surveys, run zero-detection/complete/incomplete/not-counted cases, field GPS, photo/audio evidence, restore and export; execute existing RC35/RC36/RC39 instrumented tests and attach screenshots.

### A07 | Medium | Sensitive-coordinate backup handling requires product policy review (confirmed design trade-off)

**Evidence:** `FieldBackupArchive` creates an ordinary ZIP with a database snapshot, media and preferences. `FieldBackupActivity` expressly warns that the file contains exact field locations and potentially personal data. The app requires a trusted local storage destination. No password-based ZIP encryption is implemented.

**Impact:** Anyone given a copied backup can potentially access sensitive locations and personal field records. The source is transparent about the risk, so this is a protection and sharing-policy decision, not a claim of a hidden breach.

**Fix:** Keep the original full private recovery archive and develop an explicitly separate shareable export with coordinate suppression/generalization and, if adopted, user-controlled encrypted backups.

### A08 | Low | Historical forest assets and release naming create maintenance ambiguity (confirmed)

**Evidence:** The APK retains four `assets/likas_forest/*.webp` files totaling approximately **894 kB** and compiled `LikasForestView` animation code. The current `LikasLaunchActivity` displays a text-only launch. The successful RC39 GitHub artifact still uses the stale label `LIKAS-RC36-library-polish-debug`, even though the APK manifest and version are RC39.

**Impact:** Maintainers may misinterpret which startup design is active, or select the wrong build by filename. Whether other screens instantiate the forest view needs a reference audit before deletion.

**Fix:** Audit actual view references and remove only verified dead assets under owner approval. Name CI artifacts from `versionName`/versionCode, and keep the original frog icon and scientific photos intact.

### A09 | Product roadmap | All-in-one coverage remains incomplete (not a regression)

**Evidence:** The master specification lists Research and Conservation Watch, Habitat and Ecology Atlas, and advanced research export as target pillars. The confirmed RC39 app provides meaningful Fieldwork and offline species/habitat references, but no verified live, source-labeled wildlife research/news feed or complete offline ecological atlas was established during this audit.

**Action:** After field-core acceptance, implement verified dated primary-source updates, source metadata/citations and offline read states in separate evidence-controlled modules. Do not present planned features as shipped.

## Strengths directly established

- **Survey states:** `MainActivity.finishPrompt()` exposes Finish Complete, End Incomplete and Cancel Survey / Not Counted; `SurveyClosePolicy` applies effort and method-defined completeness rules.
- **Offline data:** encrypted catalog contents and research assets are bundled; data stays in SQLite with schema 27. **No claim of end-to-end encryption of local field records is made.**
- **Scientific labeling:** the 96-card research subset has only six source-reviewed conservation entries. Unreviewed species show neutral **Status unverified**, without invented rarity. DENR and IUCN are separately labeled.
- **Privacy controls:** automatic Android backup is disabled, optional field backup exists, and restore refuses a nonempty workspace.
- **Release identity:** package `org.phwildlife.survey.beta08` and the previously pinned signing identity are maintained by the verified build record. Latest preview version is 99.

## Recommended sequence

1. **Accuracy first:** fix A01 and test legacy/zero-detection outcome arithmetic against database fixtures.
2. **Safety and accessibility:** fix A03 and A04, align navigation labels and make the active-session flows pass large-font TalkBack tests.
3. **Performance:** refactor A02 into lazily rendered/off-main-thread offline cards and measure memory, latency and frame time.
4. **Release gate:** run Android emulator and on-device upgrade tests from A06, then publish a separately verified APK with a clear versioned CI artifact name.
5. **Broader product:** address protected exports and source-verified Research Watch after field-core acceptance.

**Audit disposition:** source reviewed and APK archive inspected. **No app code, database schema, research facts, status records, surveys, or approved assets were changed by this audit.** All issue priorities are review recommendations, not a declaration of demonstrated crashes or measured performance regressions.
