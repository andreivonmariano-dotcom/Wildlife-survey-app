# LIKAS | Original-paper tables and iNaturalist Greater Mindanao project audit (31 October 2026)

**Status:** research checkpoint; **not** an accepted checklist release or an Android app change. The user's owner-derived private source tables and sensitive locations have **not** been uploaded to public GitHub.

## What was extracted privately and verified
- **Hijo Forest, Tagum City**: 122 original scientific-name rows directly transcribed from Table 1 of Gamalo et al. (2023), doi https://doi.org/10.13057/biodiv/d240250 (accessible full PDF https://smujo.id/biodiv/article/download/12450/6560/1075415). Group counts: **17 mammal entries, 11 amphibian, 22 reptile, 72 bird = 122**. The PDF text reports **122** total whereas a publisher article summary reports **125**. Individual rows mix modern survey, 2010 Montero report, and community reports; the table's star indicates a 2010 and/or locally reported entry, **not** independently verified presence in 2021. Collected vouchers stated deposited in UP Mindanao DBSES Wildlife Collection but Table 1 has no individual institutional accession IDs.
- **Tinuy-an Falls, Surigao del Sur**: all **20** original species names from Plaza et al. (2025), DOI https://doi.org/10.3724/ahr.2095-0357.2024.0025 (10 amphibians, four skinks, six snakes; **2022 actual fieldwork**). **18** named JLP field catalog/photo entries copied, two IDs unextracted rather than invented. JLP field IDs are not established as repository museum accessions. *Platymantis dorsalis* uses historical broad taxon concept; *Boiga dendrophila latifasciata* is a source trinomial.
- **142 source rows** compared using owner's four-group v3.4 accepted names plus independent v3.6 101-mammal accepted-name snapshot. Results: **115 exact accepted names; 1 indexed historical-name match; 4 manual crosswalk candidates; 5 unresolved cf./sp. identifications; 17 non-exact names**. The 27 non-exact/complex rows are held for taxon and source reidentification, **zero proposed additions approved**.
- The directly delivered private XLSX has 142 crosswalk rows, a 27-row review sheet, a 20-row Tinuy-an voucher index, project audit, and pending-source list; workbook export/import and archive integrity verified. Companion original-name CSV, QA markdown and a network-ready iNaturalist exporter Python script were delivered as a ZIP.
- Private bundle `LIKAS_Mindanao_Primary_Papers_iNaturalist_Audit_Pack_2026-10-31.zip` SHA-256 `1e71e82febeb07b979c423854997900191f130c93dd95afa53aa546244ee6264`. XLSX SHA-256 `f184ba0352f4995073d1015ae79479862a31ea0a4959f563892528be84f2f757`.

## iNaturalist project result and blocker
Owner provided the exact projects:
- https://www.inaturalist.org/projects/birds-of-greater-mindanao
- https://www.inaturalist.org/projects/reptiles-and-amphibians-of-greater-mindanao
- https://www.inaturalist.org/projects/mammals-of-greater-mindanao

All three are visibly indexed on the iNaturalist Davao Region **Projects** page https://www.inaturalist.org/places/davao-region. The provided mammal URL contained a trailing comma in the original markdown target; the valid slug has no comma.

**No actual research-grade observation records were downloaded.** The pages supplied incomplete or inaccessible project HTML; live JSON/CSV observation API endpoint reads were blocked, and container internet DNS unavailable. No observation or species counts are asserted. A standalone **network-ready and offline mocked-tested Python script** resolves project IDs then queries `/v1/observations?project_id=<id>&quality_grade=research&per_page=200&order_by=id&order=asc&id_above=<cursor>`. It saves observation ID, source scientific name, date, taxon ID, quality grade, project, public observation URL, license and geoprivacy flag, plus source taxon counts. No coordinates, hidden localities, photos or observer identities extracted. Script was **not** live tested because network unavailable. RG is a community consensus/quality label and can apply above species, not a specimen-level determination: https://help.inaturalist.org/en/support/solutions/articles/151000169936. Collection projects are filtered searches, not exhaustive PAIC faunal censuses: https://help.inaturalist.org/en/support/solutions/articles/151000176472.

## Scientific precautions and pending source extractions
- Source-only names and JLP field IDs must never be converted to actual voucher numbers, current site occupancy or current IUCN/DENR status unless explicitly checked.
- High-priority other original tables still pending: 2021 Mt Busa (herps and birds), 2008 Mt Kitanglad bird study, 2026 Samar herps, 2026 Agusan Marsh historical/survey subsets. The Agusan Marsh original authors link an open Darwin Core dataset https://doi.org/10.15468/t9a5uj.
- No four-vertebrate group master update, mammal 101-list change, Android app/SQLite/APK update or public publication of owner private source-derived tabular data.
