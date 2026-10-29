# LIKAS RC18 | Unified research corpus for all 96 priority species

**Research snapshot:** 29 October 2026 (Asia/Manila)  
**Target Android version:** `0.27.3-rc18-unified-priority-accounts-preview`, versionCode **70**  
**Package:** `org.phwildlife.survey.beta08`  
**SQLite:** version 27, unchanged  
**Requirements:** G-01, G-02, G-03, G-04, G-05, L, U, C, E

## What the app now implements

`Rc11PrioritySpeciesView.java` reads **one 96-account research source** `app/src/main/assets/rc18_unified_96_priority_accounts_v1.json` instead of repeating content from old research batches on screen. Original RC8/RC10/RC12–RC17 source assets remain preserved and are available as fallback. The unified source contains 349 non-duplicated, source-linked biological facts from those research batches and a new original source check. It is not 349 new peer-reviewed claims. It includes trusted taxonomic and conservation databases, peer-reviewed papers, dated official assessments, and other named source records, each retaining its `sourceType` and source asset.

Every one of the 96 account records has a scientific name, checklist identity, family, group, at least one species-specific biological fact, topic-organized information, source reference URLs, and a published-evidence coverage ledger. All 96 open in the same five compact topic cards: **Identification, Habitat, Range, Natural history, Conservation**. The **Range** card correctly uses the unified corpus's internal `Distribution` key. Empty sections remain invisible.

The prior two species with **no specific biological facts** are filled with source-based evidence:
- *Dicaeum anthonyi*: Northern Luzon/Ifugao type locality, current separation from Mindanao *D. kampalili*, and montane forest context, from current Avibase taxonomic concept citing McGregor (1914) *Philippine Journal of Science*. This is clearly tagged as a curated taxonomic reference, not falsely labeled a modern peer-reviewed field survey.
- *Desmalopex microleucoptera*: original 2008 *Journal of Mammalogy* description by Esselstyn et al., DOI `10.1644/07-MAMM-A-285.1`, includes morphological and phylogenetic comparisons, Mt. Siburan and Mt. Halcon sampling and forest habitat; MDD 2.5 cited accepted spelling; IUCN assessment dated 31 Aug 2018/published 2019 gives global Endangered status and declining trend. This IUCN statement is not the Philippine DENR national legal category.

The original taxonomic and cited differential compare controls continue from RC17. The bibliography lists 252 distinct *species–source links*, not 252 original scientific papers. Original owner records and media rights were not rewritten.

## Scientific evidence still missing

The 96 account **layouts and existing source corpus are consolidated**, but publication-level scientific completeness is **not achieved**. A user asking for every diagnosis, island distribution, behavior, conservation assessment and habitat cannot be told all facts are known. The audited, source-backed coverage is:

| Topic | Accounts with one or more source-backed statements | Accounts lacking this type of evidence |
|---|---:|---:|
| Identification | 60 | 36 |
| Habitat | 42 | 54 |
| Distribution | 71 | 25 |
| Natural history | 17 | 79 |
| Conservation | 17 | 79 |

These are topic presence counts, not evidence of independent peer review or specialist certification. Some single published references describe only a locality or original type sample. No AI-generated species descriptions, guessed habitats, presumed local occurrence, fabricated island ranges or invented conservation categories are added to fill blanks. Bibliography coverage for all 96 names is broader than diagnostic and ecological completeness.

**Remaining literature work:** the 36 diagnosis gaps and missing topic fields need original species descriptions, revised taxonomy, recent local studies and dated official status assessments. Original specimen measurements cannot be universal field thresholds. Every new source requires a traceable section or page citation, publication year and short objective claim.

## Engineering gates and acceptance

- `scripts/test_rc18_all_priority_accounts.py` validates 96 unique existing source keys, 349 cited facts, citation URLs, coverage consistency, the two formerly blank taxa, Range-to-Distribution Android mapping and no fabricated section completion.
- Android instrumentation checks that both formerly sparse species now show cited text, and retains the existing cited lookalike comparisons plus End Survey / backup tests. Emulator suite target: **12 tests**.
- CI must verify Python source tests, Android compilation, fixed same-certificate debug APK, previous installation upgrade retaining exact SQLite DB content, and absence of missing media/photo rights.
- Do not describe source compilation or a submitted GitHub commit as device-tested until a real signed binary and emulator log are checked. Physical handset remains untested.

## Source provenance decision

Current owner's explicit instruction: publish biologically factual, concise field-guide content supported by peer-reviewed publications or clearly labeled authoritative databases. Do **not** add a specialist approval/signoff workflow. Do **not** present methodology disclaimers at the end of public card descriptions. All scientific exceptions stay attached to the exact source in **Sources & details**.

The Android taxonomy has 1,503 base checklist names, but only the 96 owner-priority accounts are consolidated here. Future full-catalog expansion must use the same source standard.
