# LIKAS Greater Mindanao PAIC | Inclusive resident/migrant/vagrant/introduced policy and private v2.2.1 metadata
**Decision: 31 October 2026 (Asia/Manila). Private research dataset. Does not modify app.**

## Owner decision and listing behavior
All plausible source-backed PAIC candidates remain eligible whether their source describes them as **resident, migrant, vagrant, accidental, introduced**, or as overlapping categories. A non-native or transitory species must never be silently removed from a *possible species* list merely because it is not a native year-round resident. All PAIC checklist products remain restricted to **birds, reptiles, amphibians, mammals**, and province-by-province checklist products remain cancelled.

Occurrence status is independent of the existing **LIKELY/POSSIBLE** regional evidence tier. For instance, a winter visitor can be likely in the PAIC but is not a likely year-round resident. One species can be resident and migratory (or introduced and resident). Store multiple tags, the unaltered source-reported Philippine status code, the original source distribution wording and the exact reference citation separately. WBCP labels from the national Philippine checklist must not automatically be represented as verified status on all Greater Mindanao member islands. Original source phrases describing other islands must retain their geographic caveat.

## Private v2.2.1 update
This **status-only** edition was created from private v2.2 (its own local research edition, not committed in GitHub previously). Exactly **644 existing named candidate IDs** are preserved, with **350 birds, 155 reptiles, 54 amphibians, 85 mammals** and **six separate genus-level operational records**. No taxon was added or removed. The dataset adds separate source-reported national status, non-exclusive occurrence tags, primary summary, nativity/establishment classification, supporting source text and a scope warning.

Derived non-exclusive *source tag counts*, not PAIC-specific abundance:
- Resident **266**
- Migrant/seasonal **110**
- Vagrant **53**
- Accidental **22**
- Introduced/non-native **18**
- Historical/possibly extirpated **14**
- Marine/coastal **2**
- **278** taxon entries have none of those explicit tags; they remain included without a fabricated label.

These counts overlap and must never be summed as independent species populations, nor as verified regional residents. Original WBCP 2025 status and narrative text, owner-provided Sy checklists and preexisting cited studies were read to preserve source meaning. The WBCP's bird classification details: https://birdwatch.ph/philippine-checklist/ ; https://birdwatch.ph/2025/06/30/2025-checklist-of-philippine-birds/ ; https://birdwatch.ph/2026/10/01/records-committee-annual-report-2026/ .

## Private deliverables, checksums and rights
- Workbook `LIKAS_GMND_PAIC_All_Statuses_v2.2.1_2026-10-31.xlsx`: SHA256 `304de7c3be2ecd290f39bb0657ab12c60d014ee9d99852406e712c309172a9b8`
- Offline JSON `LIKAS_GMND_PAIC_All_Statuses_v2.2.1_2026-10-31_Offline_Data.json`: SHA256 `b2009bc19f72da2bc56744040ccd031ff8109fca607564cbea16b026d1a0d953`
- CSV `LIKAS_GMND_PAIC_All_Statuses_v2.2.1_2026-10-31_Named_Species.csv`: SHA256 `d32c1ad28ecdd7419eb3b5b4c3d17b3a9329c296d19aca821dd662e54329bcaf`
- Private ZIP `LIKAS_GMND_PAIC_All_Statuses_v2.2.1_2026-10-31_PRIVATE_Source_Package.zip`: SHA256 `363e89c5b7d9d126c69f2b036e744d61025686e8c79e218bd945f412f720e55a`

Exports checked: unique 644 species IDs, six genus IDs unchanged, workbook six sheets, no formula errors, XLSX and ZIP CRC valid. Full owner-supplied WBCP/Sy source-derived distribution text and private candidate rows are **not published to this public repository**; reuse permissions remain to be cleared for public Android embedding.

## App and prior version boundary
Frozen v2.0, v2.1.1 and local v2.2 source files remain unchanged. **No Android app, SQLite, UI, survey records, taxon-occurrence database, assets, signing or APK changed** by the status update. The governing v2.2.1 version is currently a private research artifact delivered directly in the owner conversation, not an installed Species Library.
