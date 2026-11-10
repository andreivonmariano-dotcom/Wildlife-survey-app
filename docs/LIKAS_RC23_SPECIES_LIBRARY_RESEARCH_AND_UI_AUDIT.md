# LIKAS RC23 | Original diagnostic sources, threatened endemic research and field library audit

**Date:** 29 October 2026 (Asia/Manila).  
**Workstream:** Q1 original diagnoses, Q2 population/habitat/threat evidence, Q3 field search, comparison and source readability.  
**Code:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38.  
**Target preview:** `0.27.3-rc23-species-library-preview` / versionCode **77**.  
**Preservation:** original package `org.phwildlife.survey.beta08`, v2 signing certificate fingerprint, SQLite schema 27, offline records, field survey close controls and startup animation remain unchanged.

## Q1. Original and modern comparative identification evidence

Four of RC22's seven unfilled identification topics now have bounded evidence:
- **Pelophryne albotaeniata.** Das 2008, *Raffles Bulletin of Zoology* 56(2):435–443. The paper's comparative Table 3 on printed p. 441 includes inspected Philippine specimens in its appendix and records a vertical snout, exposed tympanum and maximum male SVL 19.9 mm. This table was extracted from a journal-hosted PDF. PDF screenshot retrieval failed, so no finer row/column character claim is imported. Pale dorsolateral bands remain variable in other field reports. Do not identify an animal from size alone.
- **Limnonectes acanthi.** Herr et al. 2021, *Ichthyology & Herpetology*, DOI `10.1643/h2020095`: four examined Palawan males have advertisement-call notes with two pulses versus the single pulse of Mindoro/Semirara sister taxon *L. beloncioi*. The two are externally cryptic. Do not use province or photographed phenotype as a conclusive differential.
- **Platymantis negrosensis.** Brown et al. 1997 original guentheri-group description, Table 4: recorded Kwek-Kwek calls, sometimes one and sometimes two to five notes; source sample n=2. This is original acoustic context, not an automated classifier.
- **Platymantis banahao.** Same 1997 original description Table 4: a repeated Tut-Tut-Tut advertisement call with six to twenty-four notes in the small recorded series (n=4). Scientific statement is an acoustic character only. Do not substitute a repeated human-language transcription for call-frequency validation.

The remaining three **Identification** sections are *Platymantis hazelae*, *P. isarog*, and *Dicaeum kampalili*. The older Philippine Red List treats the Flame-crowned/Yellow-crowned flowerpecker as a combined `Dicaeum anthonyi` complex and distinguishes Mindanao and Luzon crowns. The current source checklist separates `D. kampalili`. Crosswalking the historical mixed account directly into a species diagnosis is unsafe without reviewing Manuel and Gilliard's 1952 original morphological description under the preoccupied name Dicaeum rubricapilla (American Museum Novitates 1545:5). The 1953 Auk paper supplied the replacement name Dicaeum kampalili; it is not the original plumage description. Neither the list, type locality nor a group-wide frog trait is accepted as a unique diagnosis.

## Q2. Philippine threatened endemic habitat and threats

The RC23 batch adds:
- **Mindoro Bleeding-heart (*Gallicolumba platenae*).** The project team announced a photographed bird above 900 m on 1 August 2026 from an Indigenous-led camera-trap survey. This is one confirmed record at one upland forest site. It cannot support a new population estimate, nationwide elevational range or public release of precise camera locations. The team separately called for remaining-forest protection.
- **Palawan Pangolin (*Manis culionensis*).** Marler 2016, *Journal of Threatened Taxa* 8:9443–9448, DOI `10.11609/jott.2867.8.12.9443-9448`: three baited camera photographs at two Palawan localities in mangrove, lowland and riverine forests. Photos are not independent counted pangolins. Baiting constrains any habitat-preference claim.
- **Visayan Warty Pig (*Sus cebifrons*).** The IUCN SSC Wild Pig Specialist Group summarizes contraction of former forest habitat and continued hunting, habitat loss and domestic-pig hybridization threats. Its undated page is labeled a specialist account, not converted into a fresh 2026 IUCN assessment or exact current population number. Reported degraded-habitat animals may include hybrids.

No occurrence record, IUCN/DENR legal status or GPS value was changed, and no copyrighted photograph was copied.

## Q3. Field identification, comparison and source presentation audit

**Found and corrected:**
1. The priority species account formerly showed fact text without immediate on-card citation. `Rc11PrioritySpeciesView.addFact` now displays a compact source line below each visible note, with a full spoken source label. Full URLs and research scope remain in **Sources & details**. This adds no duplicate primary action.
2. The bibliography's offline JSON read limit was only 160,000 bytes, near the growing 96-account index. `Rc14Bibliography` now allows up to 300,000 bytes. It labels a reference's stored `sourceType` and publication year (or the absence of a year) without silently assuming all sources are peer-reviewed.
3. The separate **Compare species traits** screen still reads the older RC8 trait pack rather than the latest RC23 unified corpus. Its entry label is now **Compare evidence snapshots**. The screen explains its age and warns that the current species account may contain newer research. This avoids presenting old comparison entries as a current species diagnosis.
4. Catalog and field picker searches retain offline common, scientific and synonym matching; the search controls now have explicit accessibility descriptions naming all three search fields.

**Audited without changing:** production synonym SQL is parameterized and escapes LIKE wildcards; catalog results prefer official records above starter duplicates; geographic source filters remain distinct from confirmed presence/absence; the differential screen displays only existing curated lookalike comparisons. No automatic identification from habitat score was introduced.

**Remaining audit risks:** older RC8 compare facts need eventual reconciliation with the latest 96-source corpus. Some longer source lines and 200% text on species detail require emulator screenshots and physical phone/TalkBack checks. This release does not certify identification accuracy or all older checklist names.

## Code and acceptance evidence

Versioned batch: `app/src/main/assets/rc23_original_id_and_threatened_habitat_v1.json`; facts integrated into the existing 96 unified accounts and literature index with source title, URL, source type, year, section, location and study limitation.

Source-ledger totals:
| Category | RC22 | RC23 |
|---|---:|---:|
| Priority species | 96 | 96 |
| Cited biological facts | 426 | 435 |
| Taxon-source links | 290 | 296 |
| Identification gaps | 7 | 3 |
| Habitat gaps | 46 | 44 |
| Distribution gaps | 23 | 23 |
| Natural-history gaps | 71 | 71 |
| Conservation gaps | 70 | 69 |

Tests: `scripts/test_rc23_species_research_and_ui.py` checks all nine exact claim matches and bibliography URLs, three honest gaps, dated records, source and UI contracts. Earlier 96-account scripts and the Android 96 screenshot instrumentation now assert inline attribution. CI builds Android versionCode77. Android 14 emulator suite must pass 14 instrumented tests, all 96 screenshots, 200%-font End Survey and the SQLite upgrade comparison. No field-phone certification is implied.

**Governing requirements:** G-01, G-02, G-03, G-04, G-05, G-08, G-09, G-10, G-11, G-12, LIKAS species-library (L), taxonomic and ecological source (T/H), and UI (U) sections of the master charter. No product-rule amendment is introduced.

## Verified CI, signed APK and Android 14 emulator evidence, 29 October 2026

- Scientific Core QA succeeded after cumulative source regression correction, including successful GitHub Actions runs **40815210413**, **40815309027**, **40815315494**. Signed RC23 APK artifact **12871439201** extracted at **9,579,979 bytes**, SHA-256 `07e4e1a6c286e8abc7f093ffb1bd6569593fc8d5b8afd3d42af768e7f70702d8`; ZIP integrity and embedded 96 species/435 facts/296 source links/nine RC23 facts verified. Binary Android v2 signer SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` matches original signed LIKAS lineage. App package `org.phwildlife.survey.beta08`, versionName `0.27.3-rc23-species-library-preview`, Gradle/Android versionCode 77.
- Android emulator workflow run **40815167061** completed **success** against RC23 production source and instrumented 96-account tests, artifact **12872058372**, retrieved as `LIKAS-RC23-Android14-emulator-QA.zip` (4,722,285 bytes; SHA-256 `8e80b25fcf9209b8d6790018ad4d528891a571fd1298579fb344188eb4279c2f`). Actual `instrumentation.txt` ends with **OK (14 tests)**, including all 96 accounts and a full End Survey rectangle at 200% font.
- **Screen review:** 96 distinct 480x960 JPEG screenshots, exactly 24 species in each wildlife group, every screenshot byte length matching the 96-row CSV. Four 24-screen contact sheets were inspected. New compact inline `Source:` labels render under research statements. The comparison action now displays `Compare evidence snapshots` without claiming a current identification. No severe first-viewport heading/card overlap was observed. Below-fold pages and broad accessibility settings remain a separate test.
- **Fieldwork data retained:** old and upgraded emulator SQLite fixtures are byte-identical (1,613,824 bytes; SHA-256 `5c90b4c8902758255982ac0dd213ca071eb480a31257aa0b8b6c127c94d574eb`). This is not physical phone or real survey-owner backup migration.
- **Development status:** Core scientific research, Android build, and current source emulator acceptance passed. PR #38 is still draft; neither stable field release nor scientific completeness is claimed. Three unverified species-level identifications remain, plus the old RC8 trait comparison pack's lack of latest RC23 facts.
