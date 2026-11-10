# LIKAS RC7C | 96-species priorities, owner-authorized photographs and field-library audit

**Date:** 28 October 2026 (Asia/Manila)
**Development branch:** feature/likas-core-fieldwork-post-rc4; draft PR #38
**Android release:** 0.27.3-rc7c-owner-photos-preview, versionCode 54
**Compatibility:** package org.phwildlife.survey.beta08 and SQLite 27 unchanged.

## Owner-selected Q1: 96 priority species

The private research library now contains all 96 exact source-keyed priorities in two 48-entry cards, with 24 taxa in each bird, amphibian, reptile and mammal group. National sources are WBCP 2025, Emerson Y. Sy amphibian 2025-1, Sy reptile 2026-1, and ASM MDD 2.5. This does not alter historical survey identifications or original checklist rows.

The first 48 profiles retain 14 existing introductory morphology/ecology accounts and include 34 new research citations with species-specific natural-history summaries. The second 48 priorities have individual taxonomic source links and 7 species-specific literature notes. The remainder retain documented identity-only review states and blank ecology/status values, not fabricated species biology. Global IUCN and DENR threatened categories are kept separate. No checklist, photo or generic habitat description is inferred as proof of station occurrence.

The Java read-only adapters now load both priority files and all literature files. The root reference page retains source versions, original names and linked published evidence. Field identification remains tentative until relevant specialist morphological review.

## Owner-selected Q2: dreidrei photograph grant and source rights

The owner stated that account **dreidrei** is theirs and that they own the account's photographs, authorizing use in **private noncommercial LIKAS research builds**. Record this in docs/LIKAS_DREIDREI_PRIVATE_PHOTO_PERMISSION_2026_10_28.md. This is a source-specific copyright-holder grant. The original individual iNaturalist photo license remains **all rights reserved**, not CC. Public app-store distribution, release as a reusable media library, marketing and third-party licensing are outside that grant.

The public API review scanned the owner's observation feed and found 52 source-authored images matching the 96 priorities. The initial contact sheet contained 16 taxon candidates. Two photos with subjects too unclear for field-guide thumbnails were excluded. The remaining 14 images were manually visually inspected and checked for exact matching photo author, observation ID, original copyright status, actual medium JPEG and SHA-256. Each image has a distinct photo URL and verified author credit. No observations' sensitive coordinates were imported.

The owner media ledger contains 14 exact checksums. Earlier third-party CC BY / CC BY-NC ledger contains 23 exact checksums, for a target **37 iNaturalist thumbnails**. Existing licensed Commons photos remain separate and not re-licensed. The CI installation fails if the file bytes change or a photo is absent. Owner images are displayed first in LIKAS, retaining visible copyright and the private-research grant label. All rights reserved photographs from any other account remain excluded absent permission.

## Owner-selected Q3: search, layout and research-photo UI audit

- Main Species Library lists every installed source reference, and the **Priority species (96)** filter displays only source-keyed priorities.
- Search queries accepted name, former name/synonyms, common name, family and source; paging is 24 records.
- Small phones under 385dp receive vertically stacked taxonomy filters and regional/compare tools.
- Offline thumbnail labels and detailed photo credits state original photograph author and specific rights.
- Runtime lookup caches priority, published-note and installed-media membership, instead of reopening asset files for thousands of matches on every filter action.
- Each photo is a reference, not a recorded LIKAS observation. Cited species text clearly distinguishes taxonomic-only cards from literature summaries.
- Static Java contracts and Android CI do not substitute for actual device screenshot/accessibility, keyboard, TalkBack, 320dp and offline performance measurements.

## CI acceptance and limitations

Run scripts/test_rc7c_owner_library.py and all existing Python scientific tests, trusted RC4 lineage, JUnit and Android compilation. Verify final APK ZIP, signature compatibility, source pack sizes and image SHA-256 **from the actual built APK** before claiming packaged photo counts. This is not an official stable fieldwork release.

A true RC6/RC7B to RC7C upgrade test, backup restoration, observations/media preservation checks and physical phone screenshot audit remain mandatory. The private checklist rights and owner-photo rights must not be mistaken for public media redistribution clearance.
