# APO

**APO** is an offline-first Android field and learning companion for Philippine wildlife biologists, ecologists, conservation practitioners and students. The planned core combines wildlife surveys, a PAIC-based species and habitat atlas, an offline biogeographic map with locally recorded survey sites, and an embedded scientific literature library.

**Repository status (10 November 2026):** The default `main` branch contains an **older v0.15 Android implementation** and current governance documentation. Current APO Android development exists on separate, unmerged RC branches. The files on `main` are insufficient evidence of the newest application, signed APK or installed feature set.

## Governing source

The approved, editable **APO Governing Product Charter v1.2** is available in Google Docs:

- [Canonical governing Google Doc](https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit)
- [APO — Governing Documents folder](https://drive.google.com/drive/folders/1j3QFI8QTw2zgWIgJLxgQVZsnCJYEl3AI)
- [Approved GitHub Markdown mirror](docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md), a stable filename whose **document content is version 1.2**
- [Repository contributor/assistant instructions](AGENTS.md)

**Every governing-document change requires the product owner's explicit approval of the proposed edit in advance.** The Google Doc and Markdown mirror must be synchronized following approval. Current Google Drive link-level edit access is too broad; see the governance notes in `AGENTS.md`.

The charter governs offline-first coverage, native reference content, scientific evidence, field data, and the strict user-facing writing standard. Historical plans remain useful as provenance and technical records.

## Android development branches

Two recent RC111 lines require comparison before choosing the next Android baseline:

- `feature/apo-rc111-ui-overhaul-v171`, HEAD `9815f0554f345706baa52e02842e322f1306e563`
- `feature/apo-rc111-unified-field-first-ui-v171`, HEAD `f7df249d2ea124591d6fbf343b9a5a9f7c594eb8`

Both use versionCode **171** and are **divergent**. Neither is merged into `main`. The preceding photo-gallery RC110 work remains in [draft PR #115](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/pull/115). Recent branch presence is evidence of source work, not an APK release or validated phone installation.

The established Android application ID is `org.phwildlife.survey.beta08`. Preserve this ID, signing identity, schema migrations and stored survey data while modernizing branding. Some internal classes and resource names still contain historical `Likas` / `likas_` identifiers. These need dependency-checked migrations; **user-visible branding is APO**.

## Where to work

- **Scientific and product decisions:** canonical charter, compatible `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`, and dated project decision log.
- **Current code, data and QA:** verify the appropriate RC111 branch, source HEAD, CI evidence and dependencies before editing.
- **Historical records:** older `docs/LIKAS_*` audits, RC4 checks and retired visual concepts are preserved to maintain traceability. They do not override the approved APO charter.
- **Repository cleanup findings:** see [10 November 2026 audit](docs/APO_REPOSITORY_CLEANUP_AUDIT_2026_11_10.md) for branch divergence, conflicting policies, old CI and source-name migration risks.

## Build and release caution

The `main` workflow is retained as a **manual historical build**. It no longer republishes a version 0.15 artifact after a documentation edit. For current APO APKs, use a verified current source lineage and the applicable signing, scientific, offline, accessibility and data-preserving upgrade tests. Provide the exact newly built APK directly to the product owner, with its tested provenance, when available.

No current release or fully functioning offline Atlas/Map/Research implementation is certified by this README.
