# LIKAS | PAIC v3.8.1 taxonomic and spatial quality audit (31 October 2026)

**Scope:** public-safe methods/provenance only. **Private deliverable:** reviewed owner-private XLSX and ZIP. No original source tables, observation coordinates, usernames, identities or photos committed.

## Accepted reference remains frozen at 725 species
- **414 birds, 156 reptiles, 53 amphibians, 102 mammals**. This update is an **evidence/issue resolution supplement**, not a new source-backed acceptance of extra species.
- Audited 33 previously unlinked iNaturalist taxon names representing 155 of the 11,746 Research Grade observation rows. Every row retains its original scientific name, iNaturalist taxon ID, example observation and source status. No IDs have been silently reassigned.
- Among 406 original source names, 362 were exact accepted matches, eight published genus-name form links, one Otosaurus orthographic link, two independently approved v3.8 additions and 33 retained for evidence review. Published synonymy is not proof of an individual photograph's identification.

## New direct literature resolutions
1. *Lycodon dumerilii* is synonymized with *Stegonotus muelleri* under Bernstein et al. (2026), *Journal of Biogeography*; the revised species includes Mindanao, Basilan, Dinagat, Leyte, Samar and Siargao. **Six iNaturalist observations remain photographic-identification and geographic-review candidates**, not accepted post-synonymy occurrence confirmations. Source: https://sararuane.com/wp-content/uploads/2026/04/jbi_70202.pdf and https://reptile-database.reptarium.cz/Stegonotus/muelleri
2. *Fejervarya limnocharis*, broadly used historically, does not establish true *F. limnocharis* in Mindanao. Philippine *F. vittigera* is distinguished by AMNH's Amphibian Species of the World, and 2026 Agusan Marsh source lists original KU, BIRC and MBS specimens of *F. vittigera*. Original iNaturalist photograph ID still requires review. https://amphibiansoftheworld.amnh.org/Amphibia/Anura/Dicroglossidae/Dicroglossinae/Fejervarya/Fejervarya-limnocharis ; https://pmc.ncbi.nlm.nih.gov/articles/PMC12993478/
3. *Philautus surdus* **sensu Sanguila et al. 2016** has Mindanao published original CAS, KU and NMNH specimens (Agusan del Norte). Sy 2025-1 supplied checklist gives narrower distribution and cryptic-species concerns remain. Record as **voucher-supported historical candidate awaiting original-specimen taxon-concept review**; no automatic 726th accepted entry and no transference of old IUCN assessment. https://pmc.ncbi.nlm.nih.gov/articles/PMC5096358/
4. *Gekko monarchus* records outside Palawan questioned in Sanguila et al. 2016 due to molecular misidentifications; 12 iNaturalist Research Grade source names not automatically admitted.

## Repeat-location and GIS controls
- 11,746 Research Grade source observations; **9,207 unobscured public coordinate records** at **6,703 exact latitude/longitude pairs**. 3,102 rows are at coordinate pairs repeated in the dataset (not invalid records); maximum repeat 71. These can bias occurrence and habitat summaries if treated as independent sampling locations.
- No approved true historical PAIC island polygons, DEM/DSM rasters or dated landcover tiles were locally available. **No elevations or landcover sampled and no habitat preference determined**.
- Wrote a private offline script `LIKAS_v381_PRIVATE_spatial_enrichment.py` to validate only open public coordinates against caller-supplied valid WGS84 island polygons tagged `CORE_PAIC` or `ASSOCIATED_CAMIGUIN_SUR`. Script conservatively screens uncertainty versus shore-boundary distance; preserves missing precision; separates offshore, shoreline-overlap and uncertain records; optionally extracts *nominal point* raster values with file/year metadata, never inferred ecological preference. **Four synthetic geometry tests passed; no real-polygon/real-raster execution performed.**

## Formal unresolved dependencies
- Original complete **MDD v2.5 CSV** (102-species row-by-row names/family/synonym crosswalk) was not accessible in container due network DNS; reference https://zenodo.org/records/21654811 ; expected MD5 `533e662fd5b8f66a5f56c191e7efac44`.
- IUCN API v4 requires registered API key, appropriate use permissions and versioned direct assessment records, https://api.iucnredlist.org/ . Existing source-reported IUCN dates and separate historic DENR DAO 2019-09 are unchanged.
- External high-quality historical-PAIC island geometry; DEM vertical datum; landcover date; taxonomic confirmation of 33 names; habitat availability and sampling effort; safe Android user-selected private import.
- Neither existing app, database, surveys, APK, signing continuity nor established accepted counts changed.

## Private artifact checks
- XLSX (8 worksheet XML parts, exported and independently re-imported) SHA-256 `266086b35be6b35bea85e9af8f429bc08b406407a846c745612d007bf6c092cd`.
- Private ZIP (9 entries, CRC verified) SHA-256 `42df969a1897c11e409d864898a4dad586d73958e34b7996a488d1b74c748c3e`.
- The ZIP includes the private 725-row scientific source, all 11,746 privacy-safe observation provenance rows, 406-name crosswalk, 33 review decisions, GIS validator, source method audit and versioned QA.
