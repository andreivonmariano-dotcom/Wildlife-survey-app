# LIKAS | Greater Mindanao mammal evidence register v3.6.0

**Curated:** 31 October 2026. **Use:** owner-only private research. **Public repository:** source citations and decision metadata only, **no row-level private source-derived WBCP/Sy/mammal checklist**.

## Implemented, direct owner-private artifacts
- Owner-private `LIKAS_Greater_Mindanao_PAIC_Mammals_v3.6.0_2026-10-31_PRIVATE.xlsx`, SHA-256 `e1cb459b47e7ed7511bbd7a011c80f602689e6da1a4852df39f47550353741c2`; preserved separate v3.5 mammal register and full v3.4 four-vertebrate group workbook unmodified.
- Owner-private ZIP `LIKAS_Greater_Mindanao_PAIC_Mammals_v3.6.0_2026-10-31_PRIVATE_RESEARCH_PACK.zip`, SHA-256 `a724105634efbb5093f4d29ba8b38a17b00e1e5ff645fc4a33e2b9cd69027ec0`: workbook; reference-staging JSON; mammal candidate, evidence and MDD/conservation crosswalk CSV; preexisting 29-publication literature register; QA report.
- **101 unique included mammal binomials** (previous v3.5: 100; exactly one newly included species). All family fields nonempty; only **four** exact MDD species web profiles individually checked and **97** taxonomic/family rows still pending original v2.5 CSV reconcilation.
- Evidence ledger **305 source-to-species assertion rows**: 101 inherited/current master source summaries; 100 original v3.4 mammal island source assertions; 99 original v3.4 mammal source assertions; two original *Podogymnura minima* specimen evidence links; three original 2021/2023 named island/site mammal field-study evidence links. Rows are **not** independent observations or IUCN evaluations.
- Original 29-entry Mindanao biodiversity literature register retained. Offline JSON is **research staging only**, not compatible with the hash-pinned Android national-checklist ZIP importer.

## Verified new species concept
- *Podogymnura minima* **Sanborn 1953**, family **Erinaceidae**, Mt Kitanglad/Bukidnon historical specimens, was originally *Podogymnura truei minima*. Balete et al. (2023) recognized *P. minima* separately in Zootaxa 5228(3):244–266, DOI https://doi.org/10.11646/zootaxa.5228.3.2. Digitized specimen treatment https://zenodo.org/records/7542097 lists holotype **ZMUC 1311** from Mt Kitanglad, **1,600 m** and additional FMNH specimens from Mt Kitanglad, Mt Imbayao, Mt Nangkabulos and Mt Dulang-dulang (Bukidnon). MDD v2.5 https://www.mammaldiversity.org/taxon/1006726/ independently recognizes it. *P. truei* and *P. intermedia* already listed, not double-counted.
- Exact MDD web profiles individually checked for *Acerodon jubatus*, *Alionoctula javanica*, *Desmalopex leucoptera* (previous v3.5 work) and *P. minima*; 97 existing species have inherited curated genus/family mappings, **not a complete species-level MDD validation**.
- Do **not** transfer old broad *P. truei* species-wide literature dates, old specimens, IUCN category or DENR national category to *P. minima* without taxonomic concept and original assessment reconciliation.
- *P. minima* IUCN: **NOT VERIFIED** (MDD blank field is not proof of the formal NE category); DENR 2019: **NOT VERIFIED**, original legal taxon assessment not established.

## QA and next checks
- Workbook exported and re-imported using artifact_tool; 101 unique scientific names and taxon IDs, one *P. minima*, valid archive and 305 valid source-to-taxon links. Private ZIP CRC integrity checked.
- Official MDD v2.5 original CSV is at https://zenodo.org/records/21654811 (file MDD_v2.5_6904species.csv; expected MD5 `533e662fd5b8f66a5f56c191e7efac44`). Network download failed in this runtime, so original CSV **was not obtained, verified or matched against the 101 candidate list**.
- 29 bibliography leads are not an exhaustive literature review. *P. minima* historical specimen sites do not establish contemporary local occupancy, habitat selection or regional abundance.
- **No Android source, SQLite, field observations, app data, signing key, APK or on-device species library altered.** Full four-group v3.4 owner-private master is not replaced by this independent 101-mammal edition. Do not advertise 723 as a newly merged/app-installed total.
