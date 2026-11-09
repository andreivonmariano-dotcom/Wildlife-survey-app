# LIKAS RC109 | Native species-photo package | Source implementation and verification

**Date:** 9 November 2026 (Asia/Manila). **Branch:** feature/apo-rc109-native-species-photos-v169. **App:** org.phwildlife.survey.beta08, versionCode 169, 0.27.3-rc109-native-species-photos; schema 29 unchanged.

## Owner direction and build contract

Species photographs are included as optimized, offline Android assets in the APK itself. No phone-side download, Drive login, or photo-import button is needed. PHLibrary Species Atlas cards, reader pages and the APO Home Species of the Day use existing OfflinePhotoEvidence loading. Missing photographs retain the neutral question-mark placeholder.

Nine **owner-supplied and source-filename-labelled** photographs are hash-locked and staged in app/photo_sources/owner_native: *Acerodon jubatus*, *Brachymeles hilong*, *Bronchocela philippina*, *Gehyra mutilata*, *Penelopides affinis*, *Platymantis corrugatus*, *Sphenomorphus variegatus*, *Staurois natator*, and *Tropidophorus misaminius*. Source JPEG metadata segments were stripped before upload. scripts/prepare_owner_native_species_photos.py checks each original SHA-256 and uses pinned Pillow 11.3.0 to resize and save a new JPEG with **no EXIF**, maximum dimension 900 pixels and strict output size limit. Generated files are packaged in assets/offline_photos/, not downloaded by the phone. Previous approved and attributed offline reference images remain untouched.

**Scientific and rights limits:** These nine labels match current PHLibrary taxa, but no independent individual photo determination, photographer authorship, or broad redistribution license has been verified. Visible photo credits and source records state that rights and identification are pending review. This is intended only for a **private owner-review development preview**, not a general public release or verified specimen record. Original Drive sources were not moved or modified. The remaining sorted Drive photos were not bundled without screening.

## Tests and gating

scripts/test_rc109_native_species_photos.py checks source hashes, exact taxon names in locked Book 01, license/identity qualifiers and generated JPEG output. RC108 native species-photo/Species of the Day instrumentation is inherited. The CI workflow uses a signed RC107 code167 artifact for no-clear-data upgrade to code169, replacing an expired RC92 artifact dependency. The original package ID, signing certificate, SQLite29 and 715 Book 01 entries must remain unchanged.

**CI status:** Pending until an RC109 workflow run reports success and signed binary inspection is complete. A build or existing file SHA must never be inferred from this source note.


## Verified signed APK candidate (9 November 2026)

**Source head:** `310ce9e550161983d41a10710ef8f801cd3d7d9d`. Actions run `42649430318` passed 533-source-test suite, JVM/Android build and signed APK identity/reference tests before upload. **Artifact:** `13688892745`. Directly materialized `LIKAS_RC109_Native_Species_Photos_signed_code169.apk`; 13,409,687 bytes, SHA256 `fbc31e33a6302107b21bc0a081392d458e475aec0b5e20d9b8e9bbd153361366`. Independently inspected the Android V2 signature block certificate SHA256: `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, identical to RC107. ZIP integrity and all nine embedded output JPEGs passed. Compared APK to reference signed RC107: all 132 existing `assets/` entries byte-identical, including Book 01 v1.82 and licensed photo manifest; only owner preview manifest plus nine optimized JPEGs were added.

**Pending:** Android14 native screenshots and RC107→RC109 no-clear-data signed upgrade results, physical handset/TalkBack testing and photographer/identification review. Do not call this stable or production-ready from build evidence alone.


## Final CI and signed upgrade verification

GitHub Actions run **42649430318 SUCCESS**, exact tested application source SHA **310ce9e550161983d41a10710ef8f801cd3d7d9d**. Source/JVM/Android build and all package, checksum and original-signer gates passed. Native Android 14 tests passed, including RC108 species photo and Species of the Day **OK (4 tests)**. Emulator evidence artifact **13689058269** contains **24 PNG screenshots** and 16 text evidence logs. The signed RC107 (code167) → RC109 (code169) no-clear-data upgrade passed **pre: OK (1 test)** and **post: OK (1 test)**, zero AndroidRuntime error bytes, preserving seeded survey events, field records, WAV evidence, and research export semantics. Upgrade evidence artifact **13688284914** was downloaded and independently inspected.

The installed app is **not** represented as field-certified or ready for public distribution. Independent determination and photographer/redistribution rights for all nine owner-supplied photographs remain open. The owner-device upgrade, TalkBack and low-memory performance were not tested.
