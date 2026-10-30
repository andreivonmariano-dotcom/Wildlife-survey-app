# LIKAS approved green pixel-art icons | Owner lock, 30 October 2026

**Status: approved and hard-locked by product owner.** This is an asset decision, not proof that artwork is bundled in an APK.

## Immutable scope

- **Launcher:** Keep the existing approved pixel frog with its upper face emerging from pond water. Do not regenerate, crop, change the launcher resources, recolor or replace it.
- **15 approved in-app utilities/navigation:** Analysis, Backup, Camera, Fieldwork, GPS, Habitats, Help, History, News, Offline, Projects, Reports, Research, Settings and Species Library. Their 64px and 512px files were carried over byte-identically from the accepted 20-icon draft.
- **3 approved wildlife survey icons:** Birds = writhed hornbill (*Rhabdotorrhinus leucocephalus*), Herpetofauna = *Staurois natator*, Mammals = *Rusa marianna*. The previously generated individual image masters, reviewed against the owner's photographs, are the locked artistic references. Do not regenerate them as generic eagle/frog/deer images.
- **Removed:** Invertebrates/insect and Plants icons. Do not include them in future LIKAS icon packs, menus, or surveys as graphical destinations. This does **not** authorize deletion of existing invertebrate or plant taxonomy/observation data.

## Technical asset lock

The owner-approved pack was produced and directly delivered in ChatGPT as `LIKAS_APPROVED_18_GREEN_ICONS_LOCKED_2026-10-30.zip`.

- ZIP SHA-256: `e0d91a0ca47f59c0e9a414d53f4d9b7e26f18bd068461ec6a3419976ca64ddc3`
- `manifest.json` inside the pack supplies full per-file SHA-256 digests, logical 64 × 64 source exports, 512 × 512 nearest-neighbor output, 48px review tiles, eight approved greens and three unaltered wildlife image masters.
- Writhed hornbill master SHA-256: `27f4641eb1b96094383ff7cd2a1b9920d89ee6489804352d8bcdedc414cd07dc`
- *Staurois natator* master SHA-256: `3775099cbe0dcd519a7f74204c0510c3f96d866d402edc99e8e0ef2f4af3b4fd`
- *Rusa marianna* master SHA-256: `d7c9c8214baa80bd49183667ba004295856e9d7c10676a2e8097af78f58c882e`

Approved display assets must remain green-dominant genuine crisp retro pixel art. Do not introduce alternate colors or stylized replacement animals. Artwork is illustrative and not a field-identification plate. At phone size verify readability, crop, padding, and attribution for reference photographs separately.

**Integration status (RC43):** The exact 18 approved 512px PNG files are committed to `app/src/main/res/drawable-nodpi/`, mapped to portal UI by `LikasPortalUi.java`, and independently confirmed byte-for-byte in the signed `0.27.3-rc43-approved-icons-preview` APK (versionCode 103). Every icon is guarded by `scripts/test_rc43_approved_icon_lock.py`. Device readability and 320dp/large-text accessibility checks remain unverified; see `docs/LIKAS_RC43_APPROVED_ICON_QA.md`.

## Custom Survey feature retirement

On the development branch `feature/likas-approved-green-icons-no-custom-survey`, the UI creation entry point and picker for Custom Survey were removed. The supported *new* survey categories remain Birds, Herpetofauna and Mammals. Preserve all old surveys, recordings, method tags, SQLite 27 schema, history, backups, and CSV export paths. Existing method-specific custom field values, station definitions and study plans must not be deleted indiscriminately. Historic custom survey records must remain inspectable.

**Build status (RC43):** The signed CI APK with all approved icons and no new Custom Survey creation passed automated tests and signature verification. Physical device upgrade and restore checks remain unverified.
