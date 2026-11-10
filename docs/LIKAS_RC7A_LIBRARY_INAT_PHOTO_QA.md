# LIKAS RC7A | Private Species Library, iNaturalist media and QA

Date: 28 October 2026. Branch: feature/likas-core-fieldwork-post-rc4, draft PR #38. Target Android 0.27.3-rc7a-species-research-preview, versionCode 52, package org.phwildlife.survey.beta08, SQLite 27 unchanged. This workstream is private, noncommercial research.

## Source-linked field accounts

The RC7 plan ships 96 source-matched priorities from WBCP 2025, Sy amphibian 2025-1, Sy reptile 2026-1 and MDD 2.5. RC7A packages the first 48 across four groups: 12 birds, 12 amphibians, 12 reptiles and 12 mammals. The priority cards include exact source taxon keys, names, families, original checklist citations, method-specific recording advice, same-genus comparison candidates, source/review states and explicit missing ecology and conservation fields. Fourteen RC7A taxa already have introductory cited accounts in the RC6 diagnostic library. The other 34 are identity-checked research draft cards, not new specialist-approved identification keys. No unknown habitat, elevation, DENR or IUCN classification was fabricated. Neither source inclusion nor iNaturalist media confirms presence at a site.

## User interface

The new Rc7SpeciesLibraryActivity is the main Home and Catalog library entry. It keeps offline source catalog, group dropdown, quick name/family/source/synonym search, RC7A priority filter, existing cited guide filter, verified offline photo filter, 30-record paging, regional evidence and comparison shortcuts. Original TaxonDetailActivity displays source names, former names, WBCP/Sy/MDD citations, habitat evidence and separately labeled national/global status. The app opens the chosen taxon's public dreidrei iNaturalist observation/photo search only on direct user action. Survey, observer records, GPS and SQLite are unmodified.

## iNaturalist licensing and photo protocol

Preferred observer account: https://www.inaturalist.org/people/dreidrei.
The Python review script screens genuine iNaturalist observations, exact user identity, species names and each individual PHOTO license. Only CC0, CC BY, CC BY-SA, CC BY-NC and CC BY-NC-SA are allowed for the private research candidate stage. Media marked all rights reserved or NoDerivatives are excluded without a separate rights grant. The photo author attribution may differ from the observer name. The approved ledger requires an individual reviewer, review date, photo/observation IDs, exact HTTPS file URL, source URL, license URL, author attribution and SHA-256. Installation rejects changed bytes and malformed JPEGs before writing manifest entries. The app renders only actual bundled files, with visible photo author, license and original source. No photo was taken from an account merely on the basis of ownership assumptions.

Photo audit run 40679327655 scanned a bounded, recent subset of 360 observations under the requested username. It found 0 candidate media entries with matching checklist identities and qualifying metadata. This does not establish that no licensable photos exist on the account. No iNaturalist photo assets have been approved or bundled yet. Existing Wikimedia Commons photo sources remain unchanged; APK photo count must be checked from the final binary. The public API responds with large pages, so the audit now uses 30 items/page, 1-second rate spacing and strictly bounded requests.

## Automated tests and pending field acceptance

New scripts/test_rc7a_library.py confirms all 48 source IDs and 12 per group, no invented biology, media license screening, duplicate avoidance, valid app routes and unchanged read-only science. Existing Python scientific QA, mammal taxonomy fingerprint, Android JVM compilation and trusted RC4 lineage must pass. Actual device checks still needed for 320dp, accessibility, RC6-to-RC7 upgrade preservation, backup/restore, offline guides and image license display. Retain PR #38 as draft and label any APK a development preview. Research checklist and media access licenses do not automatically grant a public app-store publishing right.

## Next content

Expand the 34 draft identities with species-specific morphological/ecological accounts and per-claim dated literature. Build a permissioned/photo-verified subset starting with dreidrei, then other compatible licensed iNaturalist photographers. Add a source audit to the final schema migration, preserving raw observations and original names.
