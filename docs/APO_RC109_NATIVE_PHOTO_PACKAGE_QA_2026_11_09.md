# LIKAS RC109 | Native species-photo package | Source implementation and verification

**Date:** 9 November 2026 (Asia/Manila). **Branch:** feature/apo-rc109-native-species-photos-v169. **App:** org.phwildlife.survey.beta08, versionCode 169, 0.27.3-rc109-native-species-photos; schema 29 unchanged.

## Owner direction and build contract

Species photographs are included as optimized, offline Android assets in the APK itself. No phone-side download, Drive login, or photo-import button is needed. PHLibrary Species Atlas cards, reader pages and the APO Home Species of the Day use existing OfflinePhotoEvidence loading. Missing photographs retain the neutral question-mark placeholder.

Nine **owner-supplied and source-filename-labelled** photographs are hash-locked and staged in app/photo_sources/owner_native: *Acerodon jubatus*, *Brachymeles hilong*, *Bronchocela philippina*, *Gehyra mutilata*, *Penelopides affinis*, *Platymantis corrugatus*, *Sphenomorphus variegatus*, *Staurois natator*, and *Tropidophorus misaminius*. Source JPEG metadata segments were stripped before upload. scripts/prepare_owner_native_species_photos.py checks each original SHA-256 and uses pinned Pillow 11.3.0 to resize and save a new JPEG with **no EXIF**, maximum dimension 900 pixels and strict output size limit. Generated files are packaged in assets/offline_photos/, not downloaded by the phone. Previous approved and attributed offline reference images remain untouched.

**Scientific and rights limits:** These nine labels match current PHLibrary taxa, but no independent individual photo determination, photographer authorship, or broad redistribution license has been verified. Visible photo credits and source records state that rights and identification are pending review. This is intended only for a **private owner-review development preview**, not a general public release or verified specimen record. Original Drive sources were not moved or modified. The remaining sorted Drive photos were not bundled without screening.

## Tests and gating

scripts/test_rc109_native_species_photos.py checks source hashes, exact taxon names in locked Book 01, license/identity qualifiers and generated JPEG output. RC108 native species-photo/Species of the Day instrumentation is inherited. The CI workflow uses a signed RC107 code167 artifact for no-clear-data upgrade to code169, replacing an expired RC92 artifact dependency. The original package ID, signing certificate, SQLite29 and 715 Book 01 entries must remain unchanged.

**CI status:** Pending until an RC109 workflow run reports success and signed binary inspection is complete. A build or existing file SHA must never be inferred from this source note.
