# PHLibrary Book 01 v1.79 / Discovery Rarity v0.3.3 | Targeted regional-scope and taxonomic audit

**Date:** 8 November 2026, Asia/Manila. **Scope:** Document and evidence review only; no APK, Android source, assets, DB, or field survey data changed.

## Input baselines
- Chat-local `PHLibrary_Book_01_Greater_Mindanao_v1.78_Habitat_Informed_Rarity_Review.docx` (714 taxon entries, v0.3.2 editorial tiers).
- Chat-local `PHLibrary_Discovery_Rarity_v0.3.2_Middle_Tier_Audit_714_Taxa.xlsx`.
- Published species and taxonomic accounts below. External facts were used to check exact taxa, regional scope, and seasonal/introduced records.
- The user requested continued research into the held species; no new numeric rarity scoring model was generated.

## Main decisions
1. **`Aphantophryne nana`, Camiguin Cross Frog: Exceptionally rare → Unranked (regional-scope hold).** Original `Oreophryne nana` was described from Camiguin Island, outside the seven-island Greater Mindanao PAIC. Sanguila et al. (2016) called northeast Mindanao frogs `Oreophryne cf. nana` and explicitly expressed identification uncertainty. Camiguin was separate from Pleistocene Greater Mindanao. Preserve the 714th historical working-checklist entry with its original prose and source information, but do not use a PAIC rarity tier without a confirmed within-scope specimen. Sources: https://pmc.ncbi.nlm.nih.gov/articles/PMC5096358/ ; https://www.departments.bucknell.edu/biology/resources/msw3/browse.asp?id=13001261&s=y ; https://doi.org/10.1093/jmammal/83.2.421
2. **`Ceyx melanurus`, North Philippine Dwarf-kingfisher: Rare retained; taxonomic common name and distribution corrected.** BirdLife 2024 recognizes `C. melanurus` on Samar and Leyte within Greater Mindanao, and `C. mindanensis` on Mindanao and Basilan under the split taxon concept. The inherited regional account combined old broad `melanurus` distribution strings. Older Mindanao, Bohol, Siargao source records are not silently reassigned; account now flags crosswalk. Updated common name in the heading and linked common-name index. The southern species `C. mindanensis` is **absent** from the current 714-row Book 01 taxon list: investigate separately before deciding whether to add or substitute a record. Sources: https://doi.org/10.2305/IUCN.UK.2024-2.RLTS.T61655451A252381104.en ; https://datazone.birdlife.org/species/factsheet/south-philippine-dwarf-kingfisher-ceyx-mindanensis .
3. **`Gorsachius goisagi`, Japanese Night Heron: Uncommon retained; habitat prose corrected.** The Philippine nonbreeding context and forest-associated terrestrial foraging support field-guide descriptions of forest floor, shaded gullies, damp leaf litter and wooded waterways. Earlier mangrove-focused sentence did not reflect the strongest documented context. Source: https://heronconservation.org/herons-of-the-world/list/japanese-night-heron/ .

## Other source-checked decisions, no tier change
- `Nisaetus pinskeri` remains Uncommon. BirdLife 2024 globally EN, estimated 1,300–3,600 mature birds; notes relatively frequent observations in suitable forest. This does not supply a standardized regional detection estimate. https://datazone.birdlife.org/species/factsheet/south-philippine-hawk-eagle-nisaetus-pinskeri
- `Tringa guttifer` remains provisional Uncommon. The 2024 BirdLife account identifies a Philippine wintering population in Manila Bay, and Avibase lists the species for Mindanao, but a dated primary Greater Mindanao site voucher or independently vetted observation was not established in this pass. https://datazone.birdlife.org/species/factsheet/spotted-greenshank-tringa-guttifer ; https://avibase.bsc-eoc.org/checklist.jsp?region=PHmn
- `Aerodramus salangana` remains provisional Uncommon. Clements/Avibase describes one Basilan record of nominate `salangana`; the original voucher and locally documented colony remain unverified. https://avibase.bsc-eoc.org/species.jsp?avibaseid=E16E110580F12781&lang=EN
- `Padda oryzivora` remains Uncommon, with introduced status retained. The Avibase Zamboanga Peninsula checklist marks it introduced. Establishment and persistence of free-living populations require more specific evidence. https://avibase.bsc-eoc.org/checklist.jsp?region=PH09

## New distribution and files
- **Edition v1.79:** `PHLibrary_Book_01_Greater_Mindanao_v1.79_Taxonomy_and_Scope_Review.docx`, **338,862 bytes**, SHA-256 `86903d248bb387c484525b143d50552dfd9ed4cff9685378279af9e9fc186828`.
- **Audit workbook v0.3.3:** `PHLibrary_Discovery_Rarity_v0.3.3_Taxonomy_Scope_Audit_714.xlsx`, **119,605 bytes**, SHA-256 `c115971226305e3a20e15e79d61bbecfb1c4e9241f2f439d3e695cf0b9f5082f`.
- **Counts:** Common 124; Uncommon 335; Rare 214; Exceptionally rare 37; Legendary 3; Unranked 1. **714 total entries, 713 assigned tiers.**
- The v1.79 book retains exactly 714 headings, 714 badge strips, 714 account narratives and 26 index tables. Only two article narratives differ from v1.78: `Ceyx melanurus` and `Gorsachius goisagi`; only `Aphantophryne nana` rarity badge changed. The `Ceyx melanurus` heading and common-name index were synchronized. DOCX archive integrity passed; rendered 248-page PDF and scanned all pages: 0 sparse, replacement glyph or out-of-bounds text pages; three target pages visually inspected at readable resolution. Full page-by-page manual biological proofreading was not performed.
- Workbook adds a formula-driven v0.3.3 status column to the existing 714-row audit, a source-linked v0.3.3 evidence sheet, and new overview totals. Counts match the book; formula error search returned 0 and XLSX ZIP CRC passed. This is an **editorial version update**, not a recalculation of the 40/20/30/10 numeric model.
- Files are chat-local outputs, **not uploaded as repository binaries**; their direct links are only valid within the originating conversation. No user-facing app rarity labels were changed.

## Next work
- Taxonomic-source reconciliation for `Ceyx mindanensis` and original pre-split `Ceyx melanurus` observation IDs; source version and provenance are needed to decide whether the 714-entry checklist requires revision.
- Voucher and regional occurrences for `Aphantophryne nana` and `Aerodramus salangana`; dated regional evidence for `Tringa guttifer`.
- Future signed Android integration, if separately requested, should import a versioned 713-ranked/1-unranked model without overwriting original observational records.
