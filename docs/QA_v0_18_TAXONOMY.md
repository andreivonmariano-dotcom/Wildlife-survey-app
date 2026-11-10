# LIKAS v0.18: offline taxonomy import and field-search verification

This is a taxonomy-readiness build, not a claim that the national bird,
reptile and amphibian checklists are complete.

## Sources and reproducibility
- MDD v2.5 Philippine mammal candidates are compiled as before. Confirm
  checksum, source version, source counts and review status.
- AviList v2026, released 2026-10-20 under CC BY 4.0, is a global taxonomy.
  No complete Philippines filter is inferred from global membership.
- Emerson Sy amphibian version 2025-1 and reptile version 2026-1 must come
  from genuine source tables with reprint/import authorization, citation
  version and reviewer approval.
- CLI validator requires matching source, version, SHA-256 and stable taxon
  IDs, rejects duplicate scientific names and unverified region codes.
- The complete author's checklist must not be labeled 'reviewed' for
  provincial occurrence without independent locality evidence.

## SQLite schema 12 to 13
1. Back up ZIP from v0.17.1. Install signed update without uninstalling.
2. Check row counts in `surveys`, `field_records`, `taxon_catalog`,
   `observation_media`, `route_templates` before and after schema upgrade.
3. Index creation must preserve all source keys and imported names.
4. New installs must still seed verified MDD v2.5 candidate names and old
   unreviewed starter taxa. No row rewrites or data loss.
5. Reopen existing survey, use marked detection and photo. Verbatim
   identification, GPS, source and timestamps must remain unchanged.

## Taxon search
- Autocomplete on Android uses a worker filter, capped at 60 results.
- Test exact, prefix and substring queries using common/scientific name.
- Characters `_`, `%`, and `!` must be treated as literal input.
- A name from an imported list must save its own catalog source/version,
  not an older starter value for the same binomial.
- Favorites and recent verbatim field names still appear.
- Empty and unrecognized scientific names can still be recorded, never
  silently replaced with the closest match.
- 'Documented regional' mode must not treat national candidates as
  verified island/province presence; unassessed taxa remain searchable when
  this filter is disabled.
- Test national catalogs with 1,000, 5,000 and 15,000 names. Benchmark
  typing response P50/P95 on the actual field phone.

## Imports and exports
- Import an independently vetted 5,000-name UTF-8 CSV through
  Data & Tools > Offline checklist. Verify the UI remains interactive
  and cancelled/invalid inputs never create partial rows.
- Duplicate taxon keys must fail validation or be explicitly matched
  to an existing key. Original observations are never rewritten.
- The catalog source/version viewer must display current counts.
- Export ZIP and compare scientific, common, raw identification,
  original name_source and review metadata for each group.

## Release condition
A CI pass verifies compilation and unit tests only. No claim of complete
Philippine herpetofauna/bird coverage until complete source CSV files,
redistribution rights and field validation are recorded.
