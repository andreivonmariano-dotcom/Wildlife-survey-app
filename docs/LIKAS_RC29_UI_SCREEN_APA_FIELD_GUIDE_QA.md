# LIKAS RC29 | Scientific Field Guide UI, APA 7 references and screen inventory

Date: 29 October 2026 (Asia/Manila). Development branch: `feature/likas-core-fieldwork-post-rc4`. App versionCode 89, `0.27.3-rc29-research-field-guide-preview`. Draft PR #38. This is not field-certified.

## Scope and safety
The owner approved all three: screen-by-screen visual consistency, complete APA-style research references, and illustrated offline wildlife field-guide accounts. This release is a **verified metadata tranche** and an implementation of a common reading layout, not a claim that all 306 source-index entries have full APA metadata. The original taxon facts and source provenance remain untouched. No field-history, SQLite 27, survey state, package ID, signing identity or startup art change is in scope.

### Q1: All declared Android screens, source-level audit
The manifest declares **30 activities**. We inspected the Java sources of all 30 on this candidate branch. The screen inventory is:

- **Entry and preparation:** LikasLaunchActivity, WelcomeActivity, SurveyGpsActivity, SitesActivity, StudyProtocolsActivity.
- **Survey and evidence capture:** MainActivity, CameraActivity, SpeciesPickerActivity, CallLibraryActivity, TrapProtocolActivity, FieldProtocolsActivity, StationProgressActivity.
- **Archive and data management:** HistoryActivity, DashboardActivity, DataManagementActivity, FieldBackupActivity, AuditActivity, ImportActivity.
- **Species and references:** Rc7SpeciesLibraryActivity, TaxonDetailActivity, CatalogActivity, RegionalChecklistActivity, DiagnosticGuideActivity, Rc8TraitComparisonActivity, Rc8PhotoComparisonActivity, Rc9DifferentialActivity, Rc9PhotoAnnotationReviewActivity.
- **Habitat and conservation:** HabitatRecordsActivity, HabitatSpeciesEvidenceActivity, Rc24ConservationDashboardActivity.

**Source-level findings:** Most reference and archive activities use the shared `UiKit` palette, the common navigation header, and a scrolling content container. CameraActivity and LikasLaunchActivity do not have a document-style scroll, which is consistent with camera and startup surfaces. The Field Survey recorder preserves its fixed Save and End Survey controls. Many secondary screens still contain fixed-height buttons with substantial text at 200% font size and require instrumented layout checks, not just code search. The Species Library had a technical checklist-edition subtitle. The Conservation dashboard repeated source labels under each study note. Species cards emphasized publishers and their sources instead of organism characters. The photo preview was small and visually secondary. These issues received targeted changes in this tranche.

**Changes:** Larger non-cropped photo plates with photographer, license and caption visible. Fewer open facts per card, with research sections expanded on demand. Concise visible Species Library heading and an accessible collapsed research-tools area. Conservation facts now receive APA author–date citations with one compact expandable reference list per card. Shared green/ivory style and optional reduced-motion-aware disclosure animations remain, with the recorder and end-state buttons untouched.

**Remaining screen-level QA:** A source audit does not prove all 30 real screens render without clipping. Run current-head 16-test Android emulator, review all 96 priority species snapshots, compare screens at normal and 200% font, inspect TalkBack traversal, verify stationary HUD/buttons and Android system bar overlap. Real-device review remains open.

### Q2: Citation audit and verified metadata
`rc14_96_species_literature_index_v1.json` has **306 taxon-publication links** from **199 distinct URLs**. The existing source metadata includes only 12 DOI-populated index entries (some share one DOI), 98 entries with a publication-year value, 15 entries with an authors label and 6 entries with a structured article title. Of those, none held both a fully usable author list and title before verification. The 96 priority species account corpus holds **456 attributed claims**, including 246 with no explicit year field. These counts are properties of the existing source index, not the completeness of world literature.

`VerifiedApaCatalog` now holds **14 individually checked full APA 7 research references**, including the entire DOI-populated index cohort with valid publisher/indexing metadata and three additional claim-linked DOI works. For this identified set, claims and bibliographies use verified author identity and publication date rather than interpreting abbreviated source strings. Especially, the Visayan hornbill article DOI `10.11609/jott.7261.14.3.20713-20720` lists **Reintar et al. (2022)**, not `Warguez et al.` Warguez is a coauthor. The verified original publication record overrides the older abbreviated label in the reading UI. Its stored historical source text is not deleted.

Works independently checked from official publisher, publisher-hosted article, PubMed or major research-library metadata:
- Sutton et al. (2026), `10.1017/S0959270926100586`, Bird Conservation International.
- Ibañez et al. (2003), `10.1676/01-054`, Wilson Bulletin.
- Miñarro et al. (2025), `10.7717/peerj.18694`, PeerJ.
- Reintar et al. (2022), `10.11609/jott.7261.14.3.20713-20720`, Journal of Threatened Taxa.
- Sanguila et al. (2016), `10.3897/zookeys.624.9814`, ZooKeys.
- Sanguila et al. (2026), `10.3897/zookeys.1272.167304`, ZooKeys.
- Pascual et al. (2026), `10.1038/s41598-026-76205-5`, Scientific Reports.
- Mynott et al. (2021), `10.1186/s40657-021-00303-3`, Avian Research.
- Welton et al. (2010), `10.1098/rsbl.2010.0119`, Biology Letters.
- Ramos and Nemenzo-Calica (2025), `10.59120/drj.v16i2.389`, Davao Research Journal.
- Bejar et al. (2026), `10.1163/14219980-bja10081`, Folia Primatologica.
- Meneses et al. (2026), `10.26107/RBZ-2026-0015`, Raffles Bulletin of Zoology.
- Koch et al. (2010), `10.11646/zootaxa.2446.1.1`, Zootaxa.
- Yangurin et al. (2025), `10.11609/jott.10046.17.10.27551-27562`, Journal of Threatened Taxa.

**Do not interpret this 14-entry result as 306 completed APA citations.** Each remaining catalog web page, checklist, government dataset and publication must have its bibliographic year, title, institutional/personal author and source type verified individually. Do not infer years from access dates or insert fabricated bibliographic metadata. The UI marks missing dates `n.d.` and retains the exact link.

### Q3: Illustrated, research-first field-guide accounts
The existing certified offline photo process remains the only media source. The repository tracks 10 core photo-license records, and its separate CI photo process installs the independently approved and checksum-checked iNaturalist research media. The new guide displays a full-width, uncropped plate before biological evidence, a short caption, photographer and recorded license. The original image source/license drawer remains available on demand. No newly scraped photo or speculative species occurrence was added.

Each priority account keeps:
1. Taxon/common name and family, with restrained metadata.
2. The licensed image plate if an actual approved image exists.
3. Identification first, then Habitat, Range, Natural history, Conservation.
4. Concise biological claims with APA author–date citations.
5. A single `References` drawer, with an alphabetized, deduplicated direct-reference list, further reading and study scope.

The disclosure animation runs only when system animations are active and font scale is below 160%. No field recorder animation or mandatory survey action is changed. Android's own orientation remains portrait.

## Test gates
Required: all Python source tests, new RC29 field-guide regression tests, JUnit formatter/DOI cases, JVM/Android compilation, checked SHA/manifest/certificate of exact signed APK, 16-test Android14 emulator and screenshot review, disposable old/new SQLite equality. **No real-owner phone with historic surveys has been accessed.** If any gate fails, keep RC29 labeled an internal debug preview and record the failing test and artifact.

## Explicit next work
Continue completing incomplete bibliography metadata for the other URLs through verified original-page and database records. Review the nonpriority long-tail species catalog for the old source-label style. Test real UI at normal and 200% text, GPS, camera, WAV, backup/restore and survey-ending semantics on actual hardware. Do not expand unsourced taxa with inferred facts or unlicensed imagery.
