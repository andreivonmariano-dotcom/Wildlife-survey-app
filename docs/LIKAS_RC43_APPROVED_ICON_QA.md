# LIKAS RC43 | Approved icon integration and signed APK QA

**Date:** 30 October 2026 (Asia/Manila)
**App:** `0.27.3-rc43-approved-icons-preview`, versionCode **103**
**Source code commit:** `449af5fb2cb22a16c5b7333965e4b1cbab7b1789`
**Branch:** `feature/likas-approved-green-icons-no-custom-survey`
**GitHub Scientific Core QA:** run **41003514753**, conclusion **success**, artifact **12954352549**.

## Included and locked

- All **18** owner-approved 512 × 512 PNGs are committed under `app/src/main/res/drawable-nodpi/likas_approved_*.png`. Each GitHub blob is an exact copy from `LIKAS_APPROVED_18_GREEN_ICONS_LOCKED_2026-10-30.zip`; `scripts/test_rc43_approved_icon_lock.py` checks SHA-256 for every file and denies unexpected resource names.
- The APK itself was unpacked and all **18** final `res/drawable-nodpi-v4/likas_approved_*.png` bytes passed the identical SHA-256 checks. There are no approved insect or plant icon tiles.
- `LikasPortalUi.java` uses mapped BitmapDrawables in Home, survey entry cards, Library, Settings and other portal cards. Filtering is disabled for crisp pixel boundaries. Existing vector glyphs remain only for symbols with no approved counterpart, such as audio or conservation.
- Birds = writhed hornbill (`Rhabdotorrhinus leucocephalus`); Herpetofauna = `Staurois natator`; Mammals = `Rusa marianna`. These stylized images are navigation illustrations, not identification reference evidence.
- The original frog launcher resource references and sources were **not modified**. Binary comparison with RC42 found both compiled launcher XML files differed only by a single Android resource-ID relink byte, with all common bitmap assets unchanged.
- Previous RC42 retirement of **new Custom Survey creation** remains, while existing survey data, history, archive/exports, SQLite 27 and survey outcomes are preserved in source.

## Verified automated checks and binary metadata

- **287 Python tests passed**, including exact new icon and retention contracts.
- Gradle `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest` **BUILD SUCCESSFUL**. Instrumentation APK compiled, instrumentation was **not executed** on emulator/phone.
- Original signing keystore and certificate cache verified by GitHub CI. Independent APK Signature Scheme v2 cryptographic verification and full APK signed-content digest check passed.
- **Certificate SHA-256:** `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`
- **APK SHA-256:** `50aa01fffca23a9516f01a36d2d7716971e2fb6831930d1bb9d83ca947872a4a`
- **APK bytes:** 9,777,880
- **Manifest:** `org.phwildlife.survey.beta08`, versionCode **103**, `0.27.3-rc43-approved-icons-preview`
- ZIP CRC passed; APK contains **58** licensed/offline JPG reference images and `assets/ph_mammals_mdd_v2_5.json`.

## Not verified

No physical Android install over RC39/RC41/RC42 with backed-up user survey records, instrumented screen test, font scale 200%, 320dp layout, TalkBack, field GPS/elevation, background duration, photo performance or backup restore fault injection has been executed for RC43. Partial restore risk remains open. This APK is a **signed development preview**, not a production/field-acceptance claim.

## Handoff

The actual signed APK was delivered as a direct ChatGPT conversation download at `/mnt/data/LIKAS-v0.27.3-RC43-approved-green-icons-debug.apk`. Paths from other chats/sessions must be independently verified and cannot be assumed present. Review PR #40 against `feature/likas-core-fieldwork-post-rc4`; preserve `main` as a divergent historical branch, not the app-source baseline.
