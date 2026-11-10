# APO documentation index

**Current source branch:** `refactor/apo-rc112-consolidated`, based on tested RC112 code172. Read [root README](../README.md) and [development instructions](../AGENTS.md) before changes.

## Active authority and handoff

- [Approved APO Charter v1.2](APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md): copied exactly from the canonical [Google Doc](https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit). The filename retains v1.0 for stable source links; the document content is v1.2. Explicit owner approval is required **before any future charter amendment**.
- [Master scientific and survey methods](LIKAS_MASTER_PROJECT_SPEC_v1.0.md): applies only where consistent with the APO charter.
- [Current project state](LIKAS_PROJECT_STATE.md): durable source/build/device handoff. Historical entries are append-only and should be read by date.
- [Decision log](LIKAS_DECISION_LOG.md): historical owner decisions, with newer explicit owner direction taking precedence.
- [Cross-chat handoff policy](LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md) and [direct artifact delivery policy](LIKAS_DIRECT_DOWNLOAD_POLICY.md): stable legacy filenames, current APO duties.
- [Repository conflict audit](APO_REPOSITORY_CLEANUP_AUDIT_2026_11_10.md): source split, historical naming, CI duplication, rights and data risks.

## Scientific reference and development evidence

Taxonomic source registries, PAIC species data/crosswalks, protocol definitions and dated QA reports remain in their existing directories. Original citation ledgers, source documents and supported biological uncertainties are preserved for auditability. Do not treat an older version-specific QA note or a publication-first editorial proposal as present-day product authority.

The prior publication-first guidance in [the historical editorial evidence policy](LIKAS_PUBLICATION_FIRST_CONTENT_STANDARD.md) is archived in place and explicitly superseded for app-facing writing. Scientific provenance and original research are retained.

## Archive policy

The current branch keeps only the consolidated Android QA and manual signed-APK workflows under `.github/workflows/`. Six older workflow files remain at [archive/ci/](archive/ci/) as evidence fixtures for their historical regression tests. The full record of other obsolete workflows remains accessible through the RC112 parent commit and Git history.

Historical `LIKAS` filename prefixes and internal Android class/resource identifiers may persist until reference-safe renaming is verified. All current reader-visible branding and new labels use **APO**.

**No single source snapshot, source-file label, or CI success substitutes for a signed build plus data-preserving upgrade and physical handset acceptance where required.**
