# APO | Development instructions

**Current name:** APO. All new public-facing copy, APK labels, screenshots, exports and documentation use APO. Preserve historically named code, assets and evidence until dependency-verified migration.

## Owner-approved governing sources

- **Canonical Google Doc:** https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit (inside APO — Governing Documents).
- **Approved Markdown mirror:** `docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md`, **content version 1.2**. Its filename remains stable for continuity.
- **Compatible scientific and method standards:** `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`.
- **Required delivery/continuity controls:** `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md`, `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`, `docs/LIKAS_PROJECT_STATE.md` and `docs/LIKAS_DECISION_LOG.md`.
- **No unapproved charter changes:** Present the proposed exact amendment to the product owner and obtain explicit approval before updating the Google Doc, Markdown mirror or charter version. Source research and code work do not authorize document amendments. After approval, synchronize documents and record the decision. The Drive folder still inherits anyone-with-link writer access. Do not claim owner-only technical control or alter parent sharing without authorization.

## Current source and protected history (11 November 2026)

- **Canonical development branch:** `main`, after the controlled integration of validated RC112 code172 with the previously separate main governance history. The prior Android v0.15 code remains recoverable through the historical main-parent commit `eb726a840b6c9014a271a2944d3d62e84679ca91`. The RC112/consolidated history remains reachable through the other merge parent `2bf3371fa30bfe7b267d02bbfb87af341d6409a4`.
- **Current installed preview source:** RC113 `versionCode 173`, versionName `0.27.4-rc113-living-atlas-preview`, merged by PR #121 into `main` after signed Android and no-clear-data RC109→RC113 emulator QA [run 42941822102](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42941822102). The protected RC112 baseline originates from `acddbb2b292ac9939383183d9b0100d2dc31945d` and the consolidated branch. Original package ID, signer, SQLite29 migration history and packaged scientific sources remain intact.
- **Verified current QA:** [run 42941822102](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42941822102) passed science/source regression, signed build, Living Atlas native emulator screens at 360×800 normal and 320×720 200% text, inherited 333-photo gallery/reader screens, and RC109→RC113 upgrade preserving survey data. See `docs/APO_RC113_LIVING_ATLAS_EMULATOR_QA_2026_11_11.md`. Earlier promotion evidence remains in `docs/APO_MAIN_INTEGRATION_2026_11_11.md`.
- **New Living Atlas interface:** `LivingAtlasExploreActivity` is a read-only offline scientific browsing slice. It shows taxonomic family neighbors, source-text habitat mentions and bibliography navigation. Reviewed ecological edges: zero in the current provisional pack. It does not implement the complete Ecological Threads graph, Expedition Atlas national map or automatic Google Drive synchronization. The verified source IDs remain scoped to Book 01 edition 1.82.
- **Historical sources:** the divergent RC111 branches failed prior source regressions and are archived as history. The older `main` v0.15 branch tip is recoverable through its merge parent; it is no longer the active Android baseline.
- **Active CI:** `.github/workflows/apo-quality-gate.yml` runs scientific regression, signed Android compilation, offline media checks and Android emulator upgrade QA on relevant `main` changes. `.github/workflows/apo-signed-apk.yml` is manually triggered. Prior main and RC-specific workflows are archival evidence under `docs/archive/ci/` or Git history.
- Check actual current `main` commit, CI results and asset rights before any new app change. Preserve historical receipts and owner approval requirements.

## Product requirements and scientific copy

APO is an offline-first wildlife field toolkit with surveyed observations, a Greater Mindanao-first PAIC habitat/species atlas, offline national PAIC/locality mapping and a permitted scientific-literature library. Curated reference data, images, maps and authorized papers must be usable without data imports, first-run downloads, online accounts or network services. Preserve user observations, captured evidence, backups and exports. New online services follow accepted offline milestones.

**APO-09–APO-12 is a release-blocking editorial rule.** All user-visible app-authored copy, including species, habitat, ecology, distribution, conservation and other science, uses concise affirmative fact statements, clear labels and faithful sources. Remove oppositional and defensive writing (including “X, not Y” and “X rather than Y”) and redundant explanatory text. Scientific uncertainty, actual evidence, legal requirements and field-safety warnings remain accurate. Automatically scan packaged text and perform human copy review before release. Preserve original source text as provenance.

Survey End is always accessible during recording or pause. Complete, Incomplete and Not Counted have distinct documented meanings, and zero detections can be valid effort. Keep GPS provenance and accuracy, sampling times, individual identification uncertainty, island distribution evidence and dated conservation statuses. Do not convert missing data into asserted absence or change records during presentation cleanup.

## Engineering and release rules

- Protect application ID `org.phwildlife.survey.beta08`, original signing, migration chain, SQLite schema 29, historic surveys, user preferences such as `likas_ui`, Book 01 scientific accounts, stable species IDs, approved photo checksums and provenance. Internal `Likas*` class names and `likas_*` assets are migration-sensitive code identifiers.
- Prefer one bounded, repeatable CI gate for current branch: scientific/UI source regressions, JVM and Android builds, byte-locked references, signature and package/version, emulator upgrade preserving data, offline checks and accessible screen review. Archive older workflow **definitions** after checking whether their protections are represented in the active gate. Retain historical source tests and audit evidence.
- Preserve scientific source data and bibliographic citations. The historical `docs/LIKAS_PUBLICATION_FIRST_CONTENT_STANDARD.md` is superseded for UI/editorial layout by the APO charter; original-paper evidence and reference rights still apply.
- An app update needs a tested exact new APK before it can be called delivered. Provide a verified **direct chat download link** to the real newly built file whenever available, following DL-01. An Actions page is never a substitute. If the signed file cannot be retrieved, report that limitation.
- Update project state and append decisions after material work. Never erase original historical records or suggest that source changes certify physical-phone readiness.
