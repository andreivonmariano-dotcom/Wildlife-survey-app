# APO | Development instructions

**Current name:** APO. All new public-facing copy, APK labels, screenshots, exports and documentation use APO. Preserve historically named code, assets and evidence until dependency-verified migration.

## Owner-approved governing sources

- **Canonical Google Doc:** https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit (inside APO — Governing Documents).
- **Approved Markdown mirror:** `docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md`, **content version 1.2**. Its filename remains stable for continuity.
- **Compatible scientific and method standards:** `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`.
- **Required delivery/continuity controls:** `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md`, `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`, `docs/LIKAS_PROJECT_STATE.md` and `docs/LIKAS_DECISION_LOG.md`.
- **No unapproved charter changes:** Present the proposed exact amendment to the product owner and obtain explicit approval before updating the Google Doc, Markdown mirror or charter version. Source research and code work do not authorize document amendments. After approval, synchronize documents and record the decision. The Drive folder still inherits anyone-with-link writer access. Do not claim owner-only technical control or alter parent sharing without authorization.

## Selected development source (audited 11 November 2026)

- Consolidation branch: `refactor/apo-rc112-consolidated`, created from verified RC112 commit `acddbb2b292ac9939383183d9b0100d2dc31945d2cc` on `feature/apo-rc110-highres-offline-photo-galleries-v170`. **This is a working consolidation branch, not a released app.**
- RC112 `versionCode 172` / `0.27.3-rc112-minimal-ui-compact-icon` passed build, signer/reference verification, scientific source tests and RC109→RC112 no-clear-data emulator upgrade in Actions runs `42828793989` and `42828806254` (10 November 2026). Physical phone acceptance and direct in-chat artifact delivery remain separate.
- Both RC111 branches (`feature/apo-rc111-ui-overhaul-v171` and `feature/apo-rc111-unified-field-first-ui-v171`) diverged and had failing scientific/menu source regression steps. Keep them for selective review; do not merge them over the more advanced, tested RC112 code.
- Default `main` contains historical v0.15 Android source and current governance documents. Never replace the RC112 app with `main` application files. Git history and historical PRs are evidence, not an active product specification.
- Check actual GitHub source HEAD, CI logs and current branch name afresh before each app change. See `docs/APO_REPOSITORY_CLEANUP_AUDIT_2026_11_10.md`.

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
