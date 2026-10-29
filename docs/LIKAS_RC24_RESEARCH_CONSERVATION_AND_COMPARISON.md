# LIKAS RC24 | Original taxonomy, conservation evidence and current comparisons

Date: 29 October 2026, Asia/Manila. Source: scientific-core draft PR #38. Target app: 0.27.3-rc24-conservation-comparison-preview, code78. SQLite 27, app ID, signer, field-survey records and startup branch unchanged.

## Q1. Original taxonomic descriptions

Read Brown, Brown and Alcala (1997), Proceedings of the California Academy of Sciences 49:405–421, original searchable archival text, https://archive.org/download/biostor-3273/biostor-3273.pdf . PDF screenshot lookup returned an archive cache error, so do not claim page-image inspection. Source-specific traits from the printed text are recorded as six claims in rc24_original_hazelae_isarog_v1.json.

- Platymantis hazelae: original key pp.407 and description/comparisons pp.409–410 distinguish pointed snout and absent yellow/brown areolations at groin/thighs from other related forms, with cautions for polilloensis and provisional rivularis synonym. Arboreal fern and Pandanus records, clutches of 5–9 and direct development are sourced to the 1997 revision, which cites Alcala 1962.
- Platymantis isarog: original description called Platymantis reticulatus Brown, Brown and Alcala 1997 (pp.416–418), replacement name isarog in Herpetological Review 28:131; distinct from the older preoccupied reticulatus. Extensive pale brown-bordered areolations on lower legs, a frequently reticulate brown ventrum, plus field observations from Mount Isarog and unpigmented ovarian eggs have narrow specimen limits.
- Dicaeum kampalili: one remaining identification gap. Manuel/Gilliard's detailed 1952 account (under preoccupied rubricapilla) has been located in museum bibliography; Cornell 2023 confirms modern Mindanao vs Luzon split. No full species-level plumage description was available for direct character verification. Do not fill from common name or locality alone.

## Q2. Typed conservation dashboard

New Rc24ConservationDashboardActivity reads the exact current 96 species unified corpus, filters by species group, scientific/common name and four scopes: all cited conservation facts, dated IUCN global, Philippine national government records, or other studies and threats. Only sourceType=iucn_dated_assessment belongs under the global scope; sourceType=government_national_conservation_record belongs under national. Other accounts do not inherit any IUCN/DENR category. Seven global fact records and one national fact record are present in this source snapshot; they do not represent complete Philippine status coverage.

Each entry presents dated source text, year when recorded, study limit, link to the original source and full species account. A per-page limit of 12 species keeps offline browsing responsive. Number without documented conservation evidence is explicit, not a Least Concern count. Rare-species exact coordinates and live user surveys are never displayed.

The dashboard is accessible in Species Library via the Conservation research control; it is not a live conservation news feed and does not fabricate population trends.

## Q3. Current comparison and 200% font

Rc8TraitComparisonActivity now reads the latest 96-account unified corpus, mapping Identification, Habitat, Distribution, Natural history and Conservation into comparable cited facts. The separate set of 15 habitat reference cards remains tagged as the earlier RC8 pack. Every displayed record includes the source type, year and limits, plus a link to the current full species page. The four selectors and results live in one vertically scrollable page for 200% font users. The species account action is labeled Compare current cited traits.

The existing large-font Android instrumentation test now opens both screens at 200%, checks navigation/source messages, and captures rc24_compare_200percent.png and rc24_conservation_200percent.png. The emulator runner collects both PNGs, retains all 96 species screenshots and checks the exact old/new SQLite upgrade bytes.

## Scientific coverage and release gates

96 priority taxa, 441 cited facts, 299 species-source links. Missing sections: Identification 1, Habitat 42, Distribution 23, Natural history 69, Conservation 69. Missing is never an inferred safe status or zero population.

Source/QA checks: scripts/test_rc24_conservation_comparison.py, all older evidence suites, app code78, original pinned APK signing cert, Android14 14/14 instrumentation, 96 screenshots, 200% comparison/dashboard/recorder screenshots, unchanged SQLite fixture.

The APK is a signed **phone-test debug preview**, not a stable field release. Physical handset, actual owner database migrations, TalkBack focus and offline/long-duration recording remain unverified. No master project scientific rule was changed.

## RC24B usability correction and binary evidence, 29 October 2026

The RC24 code78 emulator run **40817617391** passed 14/14 actual Android tests, all 96 screenshot captures, and byte-identical old/after SQLite (1,613,824 bytes, SHA256 `21de1278e032a1b3d481ded3abc40ff441f69a6dcdc0e12d2da046c0481b721c`). Archive artifact **12872544193** was retrieved and reviewed. Three 200%-font screenshots were present. The comparison and conservation screenshots revealed long opening paragraphs dominating the first viewport, so initially useful filters sat lower than expected. A passing unit test for visible text and scrollability did not reveal this usability weakness.

**RC24B code79** shortens the titles and opening text at Android fontScale >= 1.60. Full disclosure remains accessible through the opening text's content description and a tap-to-open explanation dialog. The short dashboard search hint reads `Search species`, with its complete spoken description preserved. The improved Android test now requires the first filter on both screens to be entirely visible within the display. This changes no biology, source classification, survey record, taxon key, database schema or signer.

Signed RC24B source build **40818503446** passed. Artifact **12873700952**, verified actual APK 9,591,832 bytes, SHA256 `4d9d755269143930b6ea6e56b2898a60c437975a9a47d9b714887ad34230cc21`, Android v2 certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, package `org.phwildlife.survey.beta08`, code79. This is a debug **phone-test preview**. New Android emulator run **40818496143** still requires independent 14/14, 96 screenshot, 200% filter and preserved SQLite checks. Real field phone data remains untested.

### Unresolved flowerpecker original paper located, not converted into identification facts

The digitized 1952 Manuel and Gilliard original *Undescribed and newly recorded Philippine birds*, *American Museum Novitates* 1545:1–9, is now cataloged at [Biodiversity Heritage Library item 331849](https://www.biodiversitylibrary.org/item/331849) and AMNH institutional handle 2246/4247. The *Auk* 1953 replacement name is documented at [Avibase taxonomic history](https://avibase.bsc-eoc.org/species.jsp?avibaseid=CC977B5F616118B7). The original 1952 description was under the preoccupied name *Dicaeum rubricapilla* Manuel & Gilliard, not the current *D. kampalili*. Web item and AMNH full text fetches returned a cache miss or inaccessible repository; the actual 1952 plumage description was not inspected. Identification remains unfilled in the app. A page-level check against the original type series, and its distinction from the old *D. anthonyi* complex, is required before claiming definitive characters. The BHL scan remains in copyright; only original citations and brief paraphrase can be embedded after validation.

## RC24C header QA candidate, 29 October 2026

The Android 14 320dp emulator screenshot from RC24B at 200% system text exposed a Conservation page heading broken mid-word. Source review traced this to `UiKit.navigationHeader`: a 120dp Back button and 20sp title competed for one row. RC24C uses a compact arrow control at >=160% font, with its complete destination in the accessibility label, and a 16sp single-line heading with spoken full title. The updated Android instrumentation requires the Compare and Conservation headings to render completely on one line without ellipsizing, alongside the existing first-filter visibility assertion. A smaller phone can still apply an accessible end truncation rather than word fragmentation. RC24C advances app versionCode to 80 without changing survey records, scientific sources, launch artwork, package, signer or SQLite 27. This is a **source candidate** until build and new 200% screenshot inspection are complete.

The only missing Identification account is Dicaeum kampalili. The 1952 Manuel/Gilliard original remains linked, but source-level plume characterization is not entered until its actual paragraphs are read and compared with the current nomenclature.
