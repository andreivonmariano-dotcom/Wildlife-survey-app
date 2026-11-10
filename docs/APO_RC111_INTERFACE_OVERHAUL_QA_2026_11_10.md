# APO RC111 | Interface redesign and signed-preview QA

**Date:** 10 November 2026 (Asia/Manila)
**Branch:** `feature/apo-rc110-highres-offline-photo-galleries-v170`
**Source:** approved RC110 native emulator visual audit `docs/APO_RC110_NATIVE_VISUAL_UX_AUDIT_2026_11_10.md`.
**Version:** `0.27.3-rc111-apo-interface-overhaul`, versionCode **171**; original Android app identity `org.phwildlife.survey.beta08`, signing certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`; SQLite29 unchanged.

## Owner direction
APO is the public-facing app identity. LIKAS is historical, not the current display name. The owner approved the visual/aesthetic and UX cleanup recommended in the RC110 audit, and explicitly approved the new forest-green launcher with the **smaller ivory/jade mountain ribbon**. The newly approved, separately SHA-locked master lives at `app/src/main/assets/apo_compact_launcher_master.png` (SHA256 `828b156748e5614c7a9b74efe90ee0272b1336b7f92334ebe5702a7959332c7b`). Preserve the preceding canonical master at `app/src/main/assets/apo_official_master.png` (SHA256 `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`) as an immutable historical reference. The compact source is used only for a reproducibly generated Android adaptive launcher foreground, not to silently redraw the approved ribbon or splash.

## Implemented
- Home places **Start wildlife survey** or persistent **Resume** before BioTools and PHLibrary. Species of the Day becomes a small image/text teaser below primary navigation, with science-related accessibility metadata intact. PHMaps and BioDiscover remain honestly labeled previews rather than prominent developed features.
- Survey setup method/region selection uses native multiple-line values on narrow phones and at 200% system text, without changing method, region, or protocol metadata.
- The recorder keeps pinned **Save Observation**, **Review**, and **End Survey** plus all Complete / Incomplete / Not Counted behavior. Enlarged-text controls are simplified; detailed research ZIP export is available in the secondary **More** menu, not prominent beside field detection controls.
- PHLibrary filter counts now state that an account **mentions** a habitat instead of asserting confirmed occurrence. Status display clearly distinguishes educational **Book rarity** from the **IUCN global** status, with category headings outside badge pills to avoid isolated symbols at enlarged text.
- Settings and additional user-facing backup and annotated-photo labels use **APO**. Historic backup format marker `likas-field-backup-v1` is preserved so recovery ZIPs remain readable. Other internal names and filenames are not wholesale changed solely for branding.
- Compact photo covers are downsampled, retaining the original full-size source for detailed views. **Asynchronous full-frame gallery decoding and real-device performance benchmarking remain open**.
- Native saved-bookmark destructive confirmation was instrumented using the real rendered button and dialog; no destructive action occurs without the deliberate positive action.

## Verified build, source and integrity
The signed build **GitHub Actions run 42812040234** on `db2aa67533b3e7b1af6cf80156a38a6fda1f2a26` succeeded. 537 source regression tests plus 3 additional checks passed, Java/Gradle Android assembly successful. APK package/signature/version, owner approved compact launcher, protected original APO master, PAIC714, ESA WorldCover 2020 derivative, 333 individually bundled offline owner photos and photo source hashes checked. APK SHA256 from this intermediate verified build `a54f9f12bf38a5543da97821921037ddddf2de103faf9ef146929751fea77315`; because a later accessible-label improvement was applied, do **not** present that intermediate APK as the final signed build unless its source is expressly the intended preview.

## Emulator QA and limitations
The RC110 baseline already passed 68 native screenshots on API34 at 360×800 dp 100% text and 320×720 dp 200% text and signed RC109→RC110 seeded-data preservation. The RC111 emulator suite includes PHLibrary screens, offline galleries, screenshots, and a signed RC109 code169 → RC111 code171 no-data-clear upgrade with synthetic survey outcome, point-count, photo/audio/export verification. Early RC111 failures revealed stale version tests, a changed habitat qualifier, cramped status badges, a flaky Espresso window focus in Saved species, and a compact daily card missing metadata. These source/test issues were corrected on the branch; **final emulator status must be read from the successful workflow run, not inferred from source compilation**.

**Still open:** physical Android phone installation/upgrade using a backup of real owner data; exact GNSS/altitude and location freshness in field conditions, camera/audio permissions, offline research ZIP/restore and TalkBack/nighttime/sunlight ergonomics; true dark mode, async JPEG decoding/low-memory stress, complete scientific photo identification/rights, and unimplemented live maps/news feeds. Keep the PR draft and do not merge with the historical main solely to present a release.
