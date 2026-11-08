# PHLibrary Book 01 v1.78 | Discovery Rarity v0.3.2 editorial screening

**Date:** 8 November 2026 (Asia/Manila)  
**Scope:** Editorial manuscripts and review workbook only. No APO/LIKAS app change, field-data migration, APK, or publication release.

## Baseline and purpose

- Source: user-provided `PHLibrary_Book_01_Greater_Mindanao_v1.77_Habitats_Rarity_v0.3.1.docx`; 714 species, with habitat-enriched narratives and approved v0.3.1 labels from the 8 packaged Book 01 v1.75 JSON shards on the RC98 app branch.
- Model preserved: 40% regional geographic breadth, 20% habitat context, 30% regional occurrence estimate, 10% IUCN context; five tiers Common / Uncommon / Rare / Exceptionally rare / Legendary.
- **557** previously Uncommon and Rare entries received a complete automated text/metadata triage screen. This does *not* mean 557 scientific record-by-record reviews or 557 independently validated rank assignments.
- Eight targeted editorial changes were cross-checked against named species accounts and external literature. The other **549** middle-tier species retain prior labels. No existing numeric model workbook was available in the session, so no raw component scores or composite numbers were recalculated. Edition v0.3.2 is a working **editorial override pass**.

## Eight tier changes

- `Fejervarya vittigera` — Uncommon → Common: encountered in fields, marshes and ditches across multiple regional islands, including documented choruses. Sanguila et al. 2016 (https://doi.org/10.3897/zookeys.624.9814).
- `Limnonectes leytensis` — Uncommon → Common: records across several regional islands and riparian habitats, locally the most encountered frog in Hijo Forest and common in lower Mt Kitanglad sampling. The regional extrapolation remains a moderate-confidence editorial judgment. Plaza et al., Tinuy-an Falls (https://www.sciengine.com/doi/10.3724/ahr.2095-0357.2024.0025); Gamalo et al. 2023 and Beukema 2011 in Book 01.
- `Polypedates leucomystax` — Uncommon → Common: wide Philippine habitat breadth and human-disturbed settings. Caramoan herpetofauna study (https://doi.org/10.1016/j.japb.2020.11.005).
- `Hemidactylus frenatus` — Uncommon → Common: documented in nearly every northeastern Mindanao/adjacent island survey site in the 2016 inventory (https://doi.org/10.3897/zookeys.624.9814).
- `Brachymeles orientalis` — Uncommon → Common: 2016 primary survey characterizes this semi-fossorial skink as common across several Greater Mindanao islands (https://doi.org/10.3897/zookeys.624.9814).
- `Eutropis multifasciata` — Uncommon → Common: broad habitat generalist; Agusan Marsh 2026 treatment explicitly records it as common in disturbed areas (https://doi.org/10.3897/zookeys.1272.167304).
- `Cynopterus brachyotis` — Uncommon → Common: high captures in disturbance-tolerant lowland fruit bat studies, with broad island footprint. Duco et al. 2023 (https://ajcb.in/journals/full_papers_dec_2023/AJCB-Vol12-No2-75868_Duco%20et%20al.pdf). Taxonomic lineage questions remain unaltered.
- `Batomys russatus` — Uncommon → Exceptionally rare: described from three Dinagat lowland forest specimens, with a narrow documented island footprint and IUCN EN (https://field.biodiversitylibrary.org/bibliography/196120). This is an educational discovery classification; no standardized encounter rate has been estimated.

## Tier totals

Before (v0.3.1): Common 117; Uncommon 343; Rare 214; Exceptionally rare 37; Legendary 3 (714 total).  
After editorial v0.3.2: **Common 124; Uncommon 335; Rare 214; Exceptionally rare 38; Legendary 3 (714 total)**.

## Deliverables and verification

- Chat-local DOCX: `PHLibrary_Book_01_Greater_Mindanao_v1.78_Habitat_Informed_Rarity_Review.docx`, 338,518 bytes, SHA-256 `ad68a2c4e7996afe53b3332d5dcbf0ef6a035ff2cb12999bd94a406b3fd45a37`.
- Chat-local XLSX: `PHLibrary_Discovery_Rarity_v0.3.2_Middle_Tier_Audit_714_Taxa.xlsx`, 106,140 bytes, SHA-256 `c1a249bb05c83445b7a1dede42d5d7d359a53bceda48432eeccc6b799a4c27bc`.
- Book 01 contains exactly 714 Heading4 species, 714 Article Text narratives, 714 rarity badge strips, 26 existing index tables. The **714 narrative paragraphs are exactly unchanged** from v1.77 and only eight rarity badges differ. DOCX ZIP CRC passed.
- Rendered **248 pages** as PDF; automated scan 0 sparse pages, 0 replacement-glyph pages, 0 out-of-bound text blocks. All pages had low-resolution contact-sheet layout review, with targeted changed pages inspected at higher scale. Full 248-page manual source/citation proofreading was not performed.
- XLSX overview recalculated to 714/714; sheet formula error scan 0, ZIP CRC passed. Includes Overview, Review 557, All 714, Changes 8, Follow-up cases. Native filterable tables on reviewer sheets.
- Both artifacts are **session-local**, not committed binaries in GitHub. Never invent future sandbox links to these filenames. No workbook IUCN inputs, raw survey records, app assets, SQLite, APK, signers or code altered.

## Held or follow-up cases

- `Nisaetus pinskeri` (Uncommon): BirdLife 2024 EN and decreasing, but records indicate it may be encountered relatively frequently in suitable forest. Keep pending region-focused occurrence synthesis. https://datazone.birdlife.org/species/factsheet/south-philippine-hawk-eagle-nisaetus-pinskeri
- `Tringa guttifer` (Uncommon): BirdLife global EN; weak original Greater Mindanao primary locality basis. Verify dated regional observation and migration season before revision. https://datazone.birdlife.org/species/factsheet/spotted-greenshank-tringa-guttifer
- `Aerodramus salangana` (Uncommon): Basilan single-record/taxonomy hold.
- `Padda oryzivora` (Uncommon): introduction context and uncertain establishment.
- `Gorsachius goisagi` (Uncommon): seasonally observed Mindanao migrant.
- Outside the middle-tier target, `Ceyx melanurus` and `Aphantophryne nana` retain their separate taxonomy/geography review flags.

**Next task:** review unresolved v0.3.2 middle-tier candidate flags against primary regional studies and original v0.3.1 numerical workbook if it becomes accessible. Do not mechanically propagate the eight overrides to the Android app without an explicit data-import/versioning and QA step.
