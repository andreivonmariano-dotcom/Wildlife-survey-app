# LIKAS RC25 | Primary ecology evidence and field-screen accessibility review

Date: 29 October 2026, Asia/Manila. Branch: `feature/likas-core-fieldwork-post-rc4` (draft PR #38). Governing scientific requirements: G-01, G-03, G-04, G-05, G-07, G-11, G-12; L/T/H source attribution; U accessibility; E validation. Startup art and native app identity are outside scope.

## Q2 | Seven observations from three published field studies

The research accounts are **source snapshots**, not inferred current occupancy, definitive identifications, complete ecological profiles or population estimates. Only original study abstracts or full study records support the exact facts below. Original journal full text is not bundled, and screenshots from third-party papers are not redistributed.

- **Philippine Eagle (`Pithecophaga jefferyi`):** two Natural history facts from Ibañez, Miranda, Balaquit-Ibañez, Afan & Kennedy (2003), *Wilson Bulletin* 115:333–336, DOI https://doi.org/10.1676/01-054. A single central Mindanao nest pair had an observed 58-day incubation period and 74% female attendance. Seventeen identified prey items included civets and Philippine colugos. The source cannot establish Philippine-wide average nest timing or prey proportions. Institutional author record: https://digitalscholarship.tsu.edu/honors_facpubs/7/
- **Mindanao litter frog (`Leptobrachium lumadorum`):** two Habitat facts from Sanguila et al. (2016), *ZooKeys* 624:1–132, DOI https://doi.org/10.3897/zookeys.624.9814. Regional survey encounters include agriculture bordering second growth, tree nurseries and riparian forest, with records up to 1,500 m at several northeast Mindanao mountains. These encounters are not preferred habitat rankings or island-wide limits. A 2026 published Samar record in the existing account is retained; the 2016 paper's historical absence wording is not reused as a present-day absence claim.
- **Philippine colugo (`Cynocephalus volans`):** one tree-use Habitat fact and two Natural history facts from Wischusen & Richmond (1998), *Journal of Mammalogy* 79:1288–1295, DOI https://doi.org/10.2307/1383020. Six observed colugos foraged among 35 of 76 enumerated tree species and made repeated brief feeding trips at night. Sampling at one study area cannot establish a universal diet or regional range. Institutional abstract: https://repository.lsu.edu/biosci_pubs/3421/

The unchanged 96-species roster now includes **448 facts and 302 species/reference links**. Missing sections: Identification **1**, Habitat **40**, Distribution **23**, Natural history **67**, Conservation **69**. A missing section remains missing rather than filled with generic claims. `Dicaeum kampalili` Identification stays empty without inspection of its 1952 species description. All seven new claims have original source type, title, URL, year, source location, time/study context and limits in `rc25_primary_ecology_v1.json` and the unified 96-species corpus. Bibliography links were added without replacing older citations. No conservation statuses, station coordinates or survey observations were changed.

## Q3 | Non-startup screen-by-screen review

| Screen | Source-level finding | RC25 change or open gate |
|---|---|---|
| Home & survey setup (`WelcomeActivity`) | Multiple workflows and a persistent active-survey resume; numerous fixed-size shortcuts require 200% font inspection. | Source reviewed, **no RC25 change**. Device screenshot/focus audit open. |
| Live Recorder (`MainActivity`) | Persistent End Survey and Save dock already instrumented at 200% in RC21B–RC24C. | Retain verified code. Real field session and screen-reader test open. |
| Species Library (`Rc7SpeciesLibraryActivity`) | Fixed-height compact filters and research panel above result viewport crowd listings under large system type. | Responsive short title, short visible source summary with full spoken provenance, taller vertical filters, scrolling research panel and expandable full evidence-limit text. New 200% test and screenshot. |
| Individual Species (`TaxonDetailActivity` / `Rc11PrioritySpeciesView`) | 96 documented species screenshot loop covers initial view and source captions. Some bibliography source lines are visually shortened but retain full content description. | New ecology evidence appears automatically, no new duplicate cards. Full TalkBack and below-fold review open. |
| Habitats (`HabitatRecordsActivity`) | Large titles and two tab labels compete for horizontal space. Opening a fixed research panel can crowd the records list. | Compact 200% labels with full accessible meanings; research controls move into scroll area. New 200% screenshot, visible Guide/My records/Research tools assertions. |
| Research comparison & conservation | RC24C already fixes source filters and two 200% headings. | Source kept, regression preserved. |
| Archive (`HistoryActivity`) | Multiple archival views and forms remain, not instrumented for 200% text in this batch. | Source reviewed only; status and actions need real view-by-view device assessment. |
| Data deletion (`DataManagementActivity`) | Typed confirmation for destructive actions exists; real archive and media restoration remains a release gate. | No deletion logic changes. |
| Backup/restore (`FieldBackupActivity`) | Scrollable backup UI exists, research ZIP is not a tested reinstallable backup. | No persistence or restore changes. Production archive upgrade test open. |
| GPS/station context (`SurveyGpsActivity`, `StationProgressActivity`) | Scrollable GPS detail and station progress screens exist, but high-contrast sunlight and fresh GNSS field measurements are unverified. | Source reviewed only; device and real-location tests open. |

## Build and acceptance

App candidate `0.27.3-rc25-primary-ecology-accessibility-preview` **versionCode 81**, package `org.phwildlife.survey.beta08`, SQLite **27**. Signed debug build uses existing pinned certificate. Do not label RC25 shipped until independent CI APK inspection and direct attachment. The emulator runner now expects **15 tests** including large-font Species and Habitats screenshots, 96 original account renders, recorder/compare/conservation accessibility, media fixture and byte-identical disposable SQLite upgrade. The new source suite `scripts/test_rc25_primary_ecology_and_screen.py` asserts exact authorship, source linkage, scoped claims and UI code paths.

Outstanding: new emulator 15/15 result, actual screenshots for both newly audited pages, APK signer/hash, owner phone in-place upgrade, field microphone/camera/GNSS and TalkBack focus order. No claim of a stable release or independently expert-certified field diagnostics.
