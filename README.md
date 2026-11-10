# APO

APO is an offline-first Philippine wildlife survey and biodiversity learning application. Its planned modules cover fieldwork, PAIC-by-PAIC species and habitat information, an offline Philippine biogeographic map with survey localities, and a locally usable scientific research library.

## Project direction

**Approved governing charter v1.2:** [canonical Google Doc](https://docs.google.com/document/d/1khryohmOO5QsOmuKchv8VWWu7Qxcq7zxVDZX9L3qCJA/edit) · [GitHub Markdown mirror](docs/APO_OFFLINE_FIRST_PRODUCT_CHARTER_v1.0.md). The Google Doc requires explicit owner approval of each proposed amendment before editing. The exact approved contents have been copied onto this branch. Check [development instructions](AGENTS.md) and [repository audit](docs/APO_REPOSITORY_CLEANUP_AUDIT_2026_11_10.md).

**APO** is the product's public name. The app's established Android package remains `org.phwildlife.survey.beta08` for update and data continuity. Historical internal names and file paths require a migration plan before they are renamed.

## Current working source

**Consolidation branch:** `refactor/apo-rc112-consolidated`, based directly on RC112 code from `feature/apo-rc110-highres-offline-photo-galleries-v170`, source commit `acddbb2b292ac9939383183d9b0100d2dc31945d`.

- VersionCode: **172**; Android versionName: `0.27.3-rc112-minimal-ui-compact-icon`.
- Signature and installed-app ID: retained from earlier signed source.
- Native catalog: Book 01 v1.82 with 715 Greater Mindanao species accounts; reference/media rights still require source-by-source validation.
- Reference image handling: verified 333-photo owner gallery recovery in the signed RC112 workflow. Actual redistributability of owner-supplied photos remains a permissions check.
- CI: successful build/signature/scientific-regression run `42828793989` and emulator signed RC109→RC112 upgrade run `42828806254` on the exact starting source. The consolidation commits need their own CI validation.
- The two earlier RC111 branches are divergent and failed their scientific/menu source tests. Their history remains available for selective comparison.
- Installed-phone release acceptance is separate. A successful source build does not mean a directly deliverable APK has been attached in this conversation.

The historical default `main` branch still contains v0.15 Android source. Use this tested RC112-descended branch for current code work. Do not merge `main` application sources over it.

## Repository organization

- `app/src/main/java`, `app/src/main/assets`, `app/src/main/res`: native Android code, embedded offline datasets and assets.
- `scripts/`: scientific content checks, offline reference preparation and Android source regressions.
- `.github/workflows/`: current build/QA workflows. Historic release workflows remain accessible from previous branches and Git history.
- `docs/`: scientific audit evidence, protocols, versioned work logs and owner-approved requirements.
- `docs/LIKAS_PROJECT_STATE.md`, `docs/LIKAS_DECISION_LOG.md`: append-only historical development handoff, with stable paths retained.

## Release and scientific safeguards

Use the original signer, Android package identity and data-preserving SQLite migrations. Verify species/source crosswalks, curated image rights, offline availability, survey closure states, GPS provenance, accessible screens and source-faithful copy. Follow the charter's strict anti-defensive writing rule across species, habitats and all scientific pages.

Current signed APK delivery is only complete when the exact freshly built APK can be attached with a verified direct file link. Do not substitute a GitHub Actions artifact or an older APK.
