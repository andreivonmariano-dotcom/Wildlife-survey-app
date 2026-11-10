# APO | Repository development instructions

**Current product name:** APO. All new UI, documentation, descriptions, screenshots, build artifact names, accessibility labels and release titles use APO. Obsolete product names may appear only in explicitly marked historical records and stable technical identifiers that require a reviewed migration.

## Authority and amendment control

1. The **canonical editable governing charter** is the owner-controlled Google Doc: https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit, in [APO — Governing Documents](https://drive.google.com/drive/folders/1j3QFI8QTw2zgWIgJLxgQVZsnCJYEl3AI).
2. Its **approved Markdown mirror** is `docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md`. The path is historical and stable; the content is currently **version 1.2**. The Google Doc controls future approved revisions.
3. Follow compatible scientific, survey-method, QA, privacy, data-migration, signing and delivery instructions in `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`. Older requirements for publication-style screens, live research feeds and user-imported reference materials are superseded by the APO charter.
4. **Before any charter amendment**, present the exact proposed addition or change to the product owner and obtain explicit approval. Do not modify the Google Doc or its Markdown mirror based on an unapproved idea, source, other chat, automation or new app feature. After approval, version the canonical Doc, synchronize the GitHub mirror and record the decision. This applies even if edits seem minor.
5. The Google Drive repository/folder/Doc currently inherit **anyone-with-link writer access**. This is an unresolved permissions risk. The approval procedure is not enforced by Google Drive until the owner restricts sharing. Do not change parent-folder permissions without explicit authorization.

## Source branches and historical boundaries (verified 10 November 2026)

- `main` is an old **v0.15 Android source baseline** plus newer APO governing documents. It is **not an acceptable starting point for current APO app changes**.
- The two RC111 Android code branches are `feature/apo-rc111-ui-overhaul-v171` (HEAD `9815f0554f345706baa52e02842e322f1306e563`) and `feature/apo-rc111-unified-field-first-ui-v171` (HEAD `f7df249d2ea124591d6fbf343b9a5a9f7c594eb8`). They have diverged. Neither should be silently assumed canonical or automatically merged. Determine the current approved lineage, compare changes and verify tests and signing before further code work.
- Earlier RC110 work is recorded in draft PR #115 on `feature/apo-rc110-highres-offline-photo-galleries-v170`. Its signed build and physical-device upgrade remain unverified in the durable project record. Verify current CI and PR status again before relying on this statement.
- The RC4-specific pull-request lineage script and historical release workflows contain obsolete assumptions. They are **historical safeguards**, not a valid substitute for current RC111 data-preservation, package/signing and source-lineage acceptance. Any replacement gate must preserve those protections.
- Read `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`, `docs/LIKAS_PROJECT_STATE.md` and `docs/LIKAS_DECISION_LOG.md` at the beginning of each development task. Check live branch HEAD and relevant CI. These pathnames are retained for continuity.

## Product and scientific rules

APO is an offline-first Philippine wildlife fieldwork and learning tool: field surveys, a PAIC-by-PAIC species/habitat atlas (Greater Mindanao first), a local Philippine biogeography and recorded-survey-locality map, and an offline research library. Ship curated references, usable maps, licensed images and permitted papers with the installed app. Preserve survey photo/audio capture, user data export and backup. Never make a first-run download, login, external reference spreadsheet/photo/video import or network call necessary to use core references. Online capabilities follow the charter's offline acceptance gates.

**Mandatory scientific/UI copy rule APO-09 through APO-12:** All app-authored user-visible copy, including every species, habitat, ecosystem, distribution, taxonomy and conservation passage, must be concise, directly factual and source-faithful. Remove defensive arguments, reader-directed methodological debates, empty explanatory paragraphs and oppositional formulas (including “X, not Y” and “X rather than Y”). Preserve meaningful uncertainty, sourced limitations, safety notices and original scientific evidence. Every text change or packaged content edition requires automated screening and editorial acceptance before release.

An active/paused survey always provides End Survey. A completed event satisfies its chosen method; incomplete saves partial effort; canceled/not-counted records remain archived and excluded from routine analyses; zero-detection effort remains valid. Preserve actual GPS quality, time, sampling effort, source and precision. Distinguish taxon identification, reported regional records and local survey observations. Keep IUCN categories and Philippine DENR legal status distinct. Do not infer island occurrence from country occurrence.

## Engineering and delivery safeguards

- Protect Android application ID `org.phwildlife.survey.beta08`, established signing certificate, versionCode progression, installed SQLite schema/migrations, stored preferences and user records. Internal `Likas*` classes, `likas_*` resources, asset filenames, JSON schema IDs and historical file paths must be renamed only after dependency inventory and migration/compatibility tests. Branding cleanup alone does not authorize reset or destructive identifier changes.
- Keep native reference provenance, checksum locks, asset rights and editorial source records. Never discard historical research or audit evidence merely because it has obsolete nomenclature.
- Review app/build configuration, affected screens, tests and relevant workflows. Prove signed builds, upgrade/data-preservation, survey end states, offline reference access and accessibility on appropriate devices before reporting a release.
- For any downloadable handoff, follow `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md`: deliver the **verified exact new file** directly in chat. A GitHub source/Actions page is supporting context. Never substitute an older APK or claim a successful build when no new APK exists.
- Record material decisions, releases, bugs, audit outcomes and next-branch handoffs in the project-state and decision log. Keep original identifiers in the historical record.
