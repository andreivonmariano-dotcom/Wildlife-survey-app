# LIKAS | Core fieldwork development workstream

**Created:** 28 October 2026, Asia/Manila  
**Base:** LIKAS v0.27.3 RC4, versionCode 49, SQLite schema 27, package `org.phwildlife.survey.beta08`  
**Branch:** `feature/likas-core-fieldwork-post-rc4`  
**Scope:** Wildlife surveys, species/taxonomic evidence, ecological/habitat records, research information, analysis/export, usability, performance and data quality.  
**Explicitly excluded:** Startup animation, sprite art, forest scenery, startup branding and Android launcher icon. These belong to a separate owner-directed chat and must not be edited in this workstream.

## Working rules

- Treat `feature/v0.27.3-startup-living-forest` as the last verified installed RC4 base. The newer `feature/v0.27.4-layered-sprite-player-stage` is a parallel **startup** workstream, not a dependency for scientific-feature implementation. Never restart from historical `main` v0.15 or the obsolete v0.16 preview.
- Avoid edits to `LikasLaunchActivity.java`, `LikasForestView.java`, `LikasSpriteForestView.java`, `app/src/main/assets/likas_forest/`, launch icon resources or startup animation CI/tests. Coordinate any necessary app entrypoint changes as a separate, explicitly reviewed merge.
- Preserve package identity, installed signing certificate, SQLite schema 27, existing survey records, media files, GPS uncertainty/provenance, taxonomy references and research-data exports.
- Keep always-available End Survey and its Complete, Incomplete and Not Counted semantics. An observation-free but completed sampling effort is a valid record.
- Use source-attributed Philippine checklists and distinguish taxonomy, geographic occurrence evidence, inferred habitat suitability and verified field observations.
- Avoid mislabeled release APKs. New installable releases require higher versionCode than 49, verified signing continuity and in-place upgrade testing. No release is authorized by creating this branch.
- Follow the project's direct in-chat artifact delivery and cross-chat GitHub state/decision logging requirements.

## Candidate priorities for owner selection

1. **Survey lifecycle and field usability:** GPS preflight versus voluntary missing fix, sampling effort/protocol completion, resume/end/cancel routes, small-screen observation entry and offline status.
2. **Taxonomic and species information quality:** Birds, herpetofauna and mammal checklist reconciliation, common/scientific names, synonyms, distribution evidence and unresolved occurrence data.
3. **Habitat and ecological data:** Source-dated habitat profiles, altitude/uncertainty, geo-linked site snapshots and clear distinction between verified measurements and screening indices.
4. **Scientific review and analysis/export:** Survey effort totals, zero-detection treatment, data validation, photographs and WAV/acoustic evidence, reproducible CSV/ZIP and backup/restore testing.
5. **Research and conservation updates:** Credible, dated, source-linked wildlife research/news module, with offline caching, freshness and provenance labels. Do not present unimplemented roadmap features as existing.

**Next gate:** Owner selects the first non-startup priority. Audit only that module's current source and scientific behavior, then implement and test on this isolated branch. Do not change startup code without new explicit approval.
