# LIKAS v0.18.3: species detail, bird crosswalk and regional evidence

## Status
Development branch: feature/v0.18.3-species-evidence, based on v0.18.2 Sy importer.
Private derived CSVs are delivered to the researcher separately, not committed
to GitHub and not bundled into the APK. The existing Android application ID,
stored survey rows and display branding are unchanged.

## A. Taxon detail pages
- Open Data & Tools > Offline taxonomy > Find species information.
- Search common or scientific names. Where multiple accepted names match,
  select one from the bounded results. From a survey recorder, long-press
  the species name to inspect the source, then return without editing fields.
- Show the accepted scientific name, common name, group/family, authority,
  author checklist's distribution text, origin/endemism, PH Red List, IUCN,
  CITES, synonyms, source version, and original publication link when known.
- Conservation categories and threat listings are snapshots from the specific
  source versions, not the current assessment from online databases.
- Never overwrite historical verbatim field observations on name changes.
- An imported regional record is separate from a GPS observation and must not
  be described as evidence of presence at this field station.

## B. Regional evidence
- Install upgraded schema 16. Only one new table is added:
  taxon_occurrence_evidence.
- Import the previously generated combined 496-entry Sy national CSV first.
- Import LIKAS_Sy_Mindanao_and_Davao_Evidence.csv second through:
  Data & Tools > Offline taxonomy > Import regional evidence CSV.
- Expected source-listed Mindanao taxa: 173 (45 amphibians, 128 reptiles).
- Expected Davao province-level records: 11, representing 8 distinct taxa.
  Davao del Sur 6, Davao Oriental 2, Davao del Norte 2, Davao de Oro 1.
- Open Mindanao & Davao species references and check each area scope.
  The Davao Region union is the union of the explicitly named provinces,
  not a separate asserted author record.
- The 380 reptile national names contain an unreviewed genus-level
  Ophiophagus entry. Do not assign an unsupported species name or local status.
- Missing species from a published distribution string remain unassessed, not absent.
- Attempt an import with an unknown taxon key. All rows must roll back.
- Reimport identical evidence. Primary keys must prevent duplicate rows.
- Run 100+ historic field observations and check their taxon source columns,
  timings, photos and survey station associations remain intact.

## C. Birds
- AviList v2026 was released 2026-10-20 and is CC BY 4.0.
  Original: https://www.avilist.org/checklist/v2026/
- WBCP published its 2025 Excel checklist for personal/scientific reference;
  the WBCP 2026 committee status report lists 765 Philippine bird species
  but reports the 2026 full checklist still unpublished as of 2026-10-01.
- References:
  https://birdwatch.ph/philippine-checklist/
  https://birdwatch.ph/2026/10/01/records-committee-annual-report-2026/
- scripts/reconcile_ph_birds_avilist.py is a conservative CSV crosswalk:
  official AviList v2026 accepted names + an attributed national occurrence
  list -> LIKAS 9-column import CSV and an unresolved-name review report.
- Source files require pinned SHA-256 and exact column mappings. Name splits,
  duplicate IDs, unmatched names and synonym-only matches do not enter the
  vetted dataset automatically.
- WBCP copyright/licensing does not grant unrestricted redistribution of its
  full lists. No purported comprehensive 2026 bird list is bundled in APK.
- Complete the crosswalk after source files are available and the researcher's
  list use/distribution permissions are confirmed.

## App/CI validation
1. Test schema 15-to-16 upgrade on a backed-up device without uninstalling.
2. Confirm the signing certificate matches the installed v0.18.2 beta.
3. Run unit tests for conservative Mindanao/Davao extraction and AviList
   exact-match/multiple-match handling, plus configured Android unit tests.
4. Verify import performance and search UI on low-memory Android phones.
5. Confirm GPS-based area selection still uses enclosing administrative
   polygons and does not guess species presence.
6. Export ZIP and compare survey and observation row counts pre/post update.
