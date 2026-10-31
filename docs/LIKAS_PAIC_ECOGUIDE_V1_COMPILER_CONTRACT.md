# LIKAS PAIC species ecological guide v1 | private compiler contract

**Accepted guide rule:** `PAIC-ECOGUIDE-1.0`.
**Purpose:** Generate coordinate-free species cards for all 725 retained taxa, using geospatial summaries where present and **published habitat accounts as the fallback when public coordinates are unavailable**.

## Inputs (kept private)
1. The existing `Species_environmental_evidence_provisional_v0.4_NO_COORDS.csv`, produced from the owner's private iNaturalist and raster inputs. Its inherited filename records *historical science-stage nomenclature*, and does **not** determine in-app display language. Do not overwrite it.
2. Optional `PAIC_literature_habitat_accounts_PRIVATE.csv` with a **single row per taxon ID** and the following header:

```csv
taxon_id,habitat_description,habitat_citation,habitat_reference_url,published_site_elevations_m,published_elevation_citation,published_elevation_reference_url,reported_locality_scope,source_taxon_name
```

Use a concise, faithful habitat paraphrase in `habitat_description`. A citation and original https DOI/URL are mandatory for nonempty habitat description or published numeric elevation. Record source geographic scope and original taxon name. An entire published species account may support a general habitat description when that is its actual scope, but a site collection does not establish whole-species occupancy or preference. No specialist sign-off required.

## Local operation
```bash
python scripts/likas_build_paic_ecoguide_v1.py \
  --species-csv /path/to/Species_environmental_evidence_provisional_v0.4_NO_COORDS.csv \
  --literature-csv /path/to/PAIC_literature_habitat_accounts_PRIVATE.csv \
  --output-dir /path/to/private/output
```

The literature argument may be omitted. The guide will still contain 725 species but will not invent unavailable descriptions for taxa lacking relevant public coordinates or supplied literature.

## Output
- `LIKAS_PAIC_Ecological_Guide_v1_NO_COORDS_PRIVATE.csv` (coordinate-free).
- `LIKAS_PAIC_Ecological_Guide_v1_manifest_PRIVATE.json` containing source and output SHA-256, policy version and QA counts.

The output distinguishes observed site DSM surface values, source-reported site elevations, dated WorldCover 2020 class associations and referenced field habitat descriptions. No generic “provisional evidence” or “specialist approval” label.

## Mechanical gates retained
- All 725 stable taxon IDs preserved.
- Every literature claim supported with citation URL, source taxon identity and reported geographic scope.
- No exact coordinates written to guide output.
- Unresolved taxon-name concepts preserved, not assigned.
- 26 source-count disputed taxa can have independent literature accounts; raster regional summaries from those taxa remain withheld until mechanical row reconciliation.
- Negative DSM elevations are suppressed from automatic elevation guidance rather than replaced by zero.
- No preferred/optimal habitat inference, invented numerical forest elevation band or species absence from no observations.

## Verification limits
The source compiler passed Python compilation and synthetic policy tests in CI on 31 October 2026. The private v1 data pack has not yet been generated in this session because shell/Python remained unavailable; no Android data, schema, or APK was changed. The **rule is locked**, not a claim that every species already has a published account.
