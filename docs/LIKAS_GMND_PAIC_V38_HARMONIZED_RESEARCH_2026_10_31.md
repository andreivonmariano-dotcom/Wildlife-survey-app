# LIKAS Greater Mindanao PAIC v3.8.0 | Private iNaturalist taxonomic harmonization

**31 October 2026. Research/data edition, not an app/APK release.**

## Owner-approved scope and privacy
Four vertebrate groups only: birds, amphibians, reptiles, mammals; regional *possible/source-supported* PAIC candidates, not province checklists, current occupancy or site habitat preference. Associated oceanic Camiguin Sur remains separate from land-bridge PAIC islands. Owner confirms sole personal use/noncommercial APK; no compiled owner-private WBCP/Sy or coordinate-bearing iNaturalist rows committed to this public repository.

## Verified owner-private artifacts
- `LIKAS_Greater_Mindanao_PAIC_Harmonized_Checklist_v3.8.0_PRIVATE.xlsx` SHA256 `29e6bbbb75cd57153939e430e6d8a742123c1e0749f85af15899aaade7f959b4`, 9 sheets; exported and re-imported with artifact_tool; source v3.7 intact.
- `LIKAS_Greater_Mindanao_PAIC_Harmonized_v3.8.0_PRIVATE_DATA_AND_EVIDENCE.zip` SHA256 `511118e98a15ab25f611c5043f34677456a576a067104771088d0c4c254e542f`, 10 entries, archive CRC passed. Contains xlsx, 725-species CSV, 725-species read-only research JSON, 11,746 RG observation trace CSV, 406-name concept crosswalk CSV, 33 unresolved-name CSV, harmonization rules JSON, source-specific QA, previous original-paper 142-row name comparison CSV, SHA256 manifest.
- All original data sets/checklists are immutable. **No app code, SQLite, installer, signing, startup assets or APK was modified.**

## New candidate species supported independently of iNaturalist project inclusion
1. **Petinomys mindanensis** (Sciuridae), MDD v2.5 taxon https://www.mammaldiversity.org/taxon/1001662/; original Rabor 1939 Mindanao type locality Badiangon, Gingoog, Misamis Oriental, syntypes PBS 223/PBS 224. Taxonomically distinct from existing *Petinomys crinitus*. MDD currently displays global LC secondarily, **not** direct IUCN assessment-year verification. Original DENR category unresolved.
2. **Sterrhoptilus nigrocapitatus** (Zosteropidae), WBCP 2025 source names Samar/Leyte/Bohol and eBird 2023 split distinguishes Visayan Babbler from Luzon's *S. affinis*: https://science.ebird.org/en/use-ebird-data/the-ebird-taxonomy/2023-ebird-taxonomy-update . Supplied WBCP IUCN/DENR cells blank, so **NOT REPORTED**, never LC by default.

New source-supported roster **725 accepted binomial candidate species**: birds 414, amphibians 53, reptiles 156, mammals 102. The one earlier excluded *Cinnyris aurora* remains excluded and separately retained in frozen source documents. These additions are not automatically added to the Android app.

## 406 iNaturalist name/concept harmonization
Original three user-exported projects contain **11,746 unique Research Grade observation IDs** across **406 source-name strings**; **11,591** are linked to one accepted LIKAS taxon, **155** retain original source names without taxonomic assignment. **373 accepted LIKAS taxa** have one or more linked RG record.

- 362 exact scientific-name matches (11,406 observations).
- 8 old/new genus combination display links (171 observations), retaining original taxon concepts.
- One *Otosaurus cumingii* / accepted Sy source spelling *O. cumingi* (8 observations), an orthographic crosswalk, not a new biological species. Contrast ITIS https://itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=1182757 and current Sy 2026-1.
- Two new included source-backed names (6 observations total) whose inclusion rests on **independent published range/specimen evidence**.
- Remaining **33 source names** and **155 RG observations** unassigned for geographic and/or taxonomic review.
- **Six `Lycodon dumerilii` RG rows** are explicitly withheld from automatic `Stegonotus muelleri` assignment. The Reptile Database summarizes 2026 Bernstein et al. synonymy, while Sanguila et al. 2016 documented the historical `L. dumerilii` voucher KU 319989 in Mindanao. A nomenclatural synonym summary is not a species-level re-identification of each iNaturalist photo. Sources: https://reptile-database.reptarium.cz/Stegonotus/muelleri and https://pmc.ncbi.nlm.nih.gov/articles/PMC5096358/ .
- Genus-only `Bos` retained unresolved; marine mammals, offshore species and records with known alternate national-island distributions are not promoted from project membership alone.

## Coordinates, elevation, land cover and conservation
The harmonized RG CSV contains public WGS84 point coordinates **only for unobscured observations**. All **2,539 RG deliberately obscured** points have blank coordinates, and no original private coordinates, observers or free-text place descriptions were exported. All 11,746 unique IDs and original names preserved; no duplicate or data-loss claims.

The source spatial QC had **3,478 pre-polygon <=100 m uncertainty terrain candidates**, **2,493 <=30 m land-cover candidates** and 146 named-outside-area flags. None of these points has been verified against an authoritative historic PAIC-island polygon. No DEM/DSM or WorldCover raster values were derived. Published source elevation/site notes for three mammals are preserved; **none are validated physiological elevation preference or demonstrated habitat selection**. Current 2026 IUCN statuses and updated legal DENR name concepts remain incompletely reconciled; v3.8 has 266 source-reported global categorical codes and 272 Philippine codes, independently qualified.

## Unresolved release gates
1. Authoritative detailed PAIC island/coastline polygon and separate Camiguin Sur geometry, plus buffer-based positional checks; iNat project membership was observed to include non-PAIC areas (e.g. Siquijor).
2. Local licensed, date-described DEM and land-cover raster tiles; sampling, uncertainty and temporal cover comparison.
3. 33 source-name geographic/taxonomic reviews; full original MDD v2.5 102-mammal CSV crosswalk; current IUCN API (key/terms) and original DENR synonym reconciliation.
4. Study-specific habitat availability, bias and sampling design before claiming *habitat preference* or *habitat selection*.
5. A new, safe **user-selected owner-private Android research-reference import** is required; current app's pinned national ZIP importer does **not** accept this new JSON or evidence package. Do not claim APK integration, in-app visibility or historic-survey data migration.

### Required next implementation
Keep original observations and source names immutable. Only introduce a read-only owner-imported candidate/reference overlay in Android once source format, schema safety and import failure rollback are tested. Do not distribute this private dataset in public GitHub, CI artifacts or a public release.
