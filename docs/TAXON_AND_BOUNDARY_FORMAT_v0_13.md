# Taxonomy and boundary input contract

## Checklist CSV
Required header (UTF-8; quote commas; no duplicated taxon_key):
```
taxon_key,taxon_group,common_name,scientific_name,family,source,source_version,region_codes,review_status
DEMO:bird:001,Birds,Philippine Eagle,Pithecophaga jefferyi,Accipitridae,demonstration_only,2026-10,MINDANAO,unreviewed
DEMO:reptile:001,Reptiles,Philippine Sailfin Lizard,Hydrosaurus pustulatus,Agamidae,demonstration_only,2026-10,MINDANAO,unreviewed
```

The two entries are formatting examples, not a regional occurrence quality audit. Reviewed imported rows should cite independent supporting evidence in project documentation. A region code means positive evidence, not proof of absence in any other region. Upsert only uses taxon_key as the matching identity, so taxonomy version changes should receive deliberate version management.

### Regional keys
`PH`, `MINDANAO`, `VISAYAS`, `LUZON`, `PALAWAN`, `MINDORO`, `SULU`, `OTHER`; or `P:` followed by an administrative PSGC province code. Pipe-separated values are accepted. `P:1102400000` is illustrative, verify the PSGC code before using it.

### Proposed data governance
Taxonomy backbone: AviList v2026 for birds under CC BY 4.0. Preserve original source taxonomy identifiers, changes and synonymies. Mammal/reptile/amphibian accepted names require source-specific references and review. For distribution, maintain source occurrence ID, precision, date, identification reliability and separate biome/biogeographic claims. Do not infer range solely from GBIF presences.

## Offline province boundaries
Project source: `faeldon/philippines-json-maps`, `latest/geojson/regions/lowres/`, PSGC 3Q 2026, MIT. Original source map layer is polygonal, not simply a rectangular bounding box.

Processing: concatenate the 18 regional collections, preserve PSGC and province names, round coordinate pairs to 4 decimals, bundle as `app/src/main/assets/ph_provinces_2026q3.json`.

Legal/source note: see https://github.com/faeldon/philippines-json-maps and its LICENSE. The layer follows administrative PSGC geography, which does not define faunal boundaries.

Confidence rules: missing/outdated coordinates, error radius >200m, overlapping provinces or proximity under 700m to an administrative boundary results in a caution message, not an automatic label. A successful suggestion is only adopted through explicit confirmation.

## Photo evidence
Image picker uses ACTION_OPEN_DOCUMENT, then copies to application-private storage. Saving links file names through `observation_media`. Export includes images and an index. Record coordinates represent the observer, not necessarily the pictured animal. Use field note and identification-confidence controls to document uncertainty.
