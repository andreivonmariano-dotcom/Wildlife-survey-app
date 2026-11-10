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

## Current Android source

The selected consolidation baseline is **RC112 code172**, sourced from `feature/apo-rc110-highres-offline-photo-galleries-v170` at `acddbb2b292ac9939383183d9b0100d2dc31945d`. [The consolidation branch](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/tree/refactor/apo-rc112-consolidated) is `refactor/apo-rc112-consolidated`. This is the **working Android source** for safe repository cleanup, pending its own QA acceptance.

The RC112 baseline passed signed APK compilation and Android emulator upgrade verification from RC109 in CI runs `42828793989` and `42828806254`. Two older RC111 branches remain divergent; their regression checks failed before APK compilation. They are retained for selective review.

The established application ID is `org.phwildlife.survey.beta08`; retain its original signer and migrations. Legacy internal class, resource and preference names have compatibility implications. User-facing branding is **APO**.

The default `main` Android code remains historical. Promotion of the active Android branch to `main` is separate from this documentation cleanup and requires successful CI, evidence preservation, and controlled branch integration.

## Where to work

- **Scientific and product decisions:** canonical charter, compatible `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`, and dated project decision log.
- **Current code, data and QA:** verify the appropriate RC111 branch, source HEAD, CI evidence and dependencies before editing.
- **Historical records:** older `docs/LIKAS_*` audits, RC4 checks and retired visual concepts are preserved to maintain traceability. They do not override the approved APO charter.
- **Repository cleanup findings:** see [10 November 2026 audit](docs/APO_REPOSITORY_CLEANUP_AUDIT_2026_11_10.md) for branch divergence, conflicting policies, old CI and source-name migration risks.

## Build and release caution

The `main` workflow is retained as a **manual historical build**. It no longer republishes a version 0.15 artifact after a documentation edit. For current APO APKs, use a verified current source lineage and the applicable signing, scientific, offline, accessibility and data-preserving upgrade tests. Provide the exact newly built APK directly to the product owner, with its tested provenance, when available.

No current release or fully functioning offline Atlas/Map/Research implementation is certified by this README.
