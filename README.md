# APO

APO is an offline-first Philippine wildlife survey and biodiversity learning app for biologists, ecologists, conservationists and students. Development priorities are reliable field surveys, the PAIC-by-PAIC species/habitat atlas (Greater Mindanao first), offline maps of survey localities, and an embedded scientific literature reference.

## Governing direction

The [APO Governing Product Charter v1.2](docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md) is the approved source for product scope and all scientific/UI copy. Its canonical editable edition is the [Google Doc](https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit), located in the [governing documents folder](https://drive.google.com/drive/folders/1j3QFI8QTw2zgWIgJLxgQVZsnCJYEl3AI).

**Every proposed change to the governing document requires the owner's specific prior approval.** Neither source updates nor external research authorize automatic amendments. The Markdown file retains its stable v1.0 filename, but its approved content is **v1.2**.

Read [APO developer instructions](AGENTS.md) and the [documentation index](docs/README.md) before development.

## Current Android development

The default **`main`** branch contains the consolidated APO **RC112 code172** Android application. This branch preserves both the historical main repository record and the signed RC112 source lineage through a two-parent integration commit. The original v0.15 Android implementation is retained in Git history.

- **Android ID:** `org.phwildlife.survey.beta08` (preserved for installed-app continuity).
- **Version:** `0.27.3-rc112-minimal-ui-compact-icon`, versionCode **172**.
- **App label:** APO.
- **Database:** SQLite29, with historical migrations retained.
- **Scientific reference:** Greater Mindanao Book 01 v1.82 (715 taxa), source provenance and separate dated status labels.
- **Images:** 333 offline owner-photo references in the RC112 build, with provenance/redistribution checks pending.
- **Current CI:** `.github/workflows/apo-quality-gate.yml` for source checks, signing, emulator and upgrade QA; `.github/workflows/apo-signed-apk.yml` for manually requested owner-preview APK builds.
- **Evidence:** the prior RC112 and consolidation sources passed signed build and upgrade tests in [run 42852787251](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42852787251). See [main integration QA record](docs/APO_MAIN_INTEGRATION_2026_11_11.md) for promotion verification.

Active development uses `main` or short-lived branches based on it. Previously divergent RC111 branches, older RC stages and obsolete LIKAS material remain available as version history and historical QA records.

## Release gates and unresolved work

The promoted repository source is **an owner-preview development baseline**. Public distribution requires completed photo attribution/rights review, a durable permitted source for the 333-photo gallery, review of a private-labelled reference file already present in the public source history, complete APO-09–APO-12 scientific and interface copy QA, and physical-device acceptance where appropriate.

Track [photo source and rights #116](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/116), [public source-permissions review #117](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/117) and [copy compliance #119](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/119).

The Android package ID, signing certificate, user preferences, survey records, evidence exports and preserved citation history must remain compatible. New releases require an actually verified signed APK with a direct chat download, and may not be inferred from a successful branch merge.
