# LIKAS RC5 Field UX and Offline Species Reference QA

**Date:** 28 October 2026 (Asia/Manila)
**Source:** Draft PR #38, RC4 scientific-core branch
**Preview version:** 0.27.3-rc5-field-ux-preview, versionCode 50
**Package:** org.phwildlife.survey.beta08
**SQLite:** 27, unchanged

## Navigation, setup and research records

Dashboard and Survey History use UiKit's common navigation header. History's detail-to-list action remains separate from Home navigation. The live recorder and End Survey paths are unchanged. Survey setup has one persistent Continue to GPS action beneath the scroll area, with observer name in the existing optional-details section. Sampling method, project, geographic checklist, GPS area suggestion, effort, routes and study rules retain their previous input values and validation.

## Source-checked additions

Thirteen introductory detailed field accounts now exist, up from ten. Three new accounts cite an IUCN SSC Pangolin Specialist Group profile and global IUCN assessment for Manis culionensis, an IUCN SSC Wild Pig Specialist Group profile for Sus cebifrons, and BirdLife historical source plus the documented August 2026 Search for Lost Birds rediscovery for Gallicolumba platenae. These are introductions, not specialist-validated field keys. Geographic evidence is not inferred from habitat or an unverified sighting. Global IUCN assessment and DENR national status are separate.

New media candidates have documented license terms, author names and Commons source links: Philippine pangolin (Shukran888, CC BY-SA 4.0), Visayan warty pig (Mhlayson, CC BY-SA 4.0) and a **mounted museum specimen**, not a living bird, of Mindoro bleeding-heart (Anand Krishnan, CC0 1.0). The actual number downloaded into the APK is verified only after media-fetch CI, and failed photos must remain unavailable rather than appear as completed.

## Evidence needed before stable field release

- Python source and taxonomy tests, Android JUnit and debug assembly, package ID and signer continuity.
- Real Android handset review at 320dp and large-font settings, with keyboard open and route/study selection changes.
- RC4 to RC5 installation without uninstall, historical surveys and photographs retained, scientific export review and actual data recovery.
- Typed-confirmation destructive controls need current-version backup and restore tests before normal field deployment.

Changes on this branch are a **draft preview**. They do not imply completed field verification, restored backups or full Philippine biodiversity photo and diagnostic coverage.
