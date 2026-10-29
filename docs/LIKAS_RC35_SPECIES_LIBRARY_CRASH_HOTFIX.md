# RC35 | Species Library crash hotfix

**Date:** 29 October 2026. **Reported by owner:** Species Library closes with an Android Send feedback stack trace after entering the side-scrolling species deck.

## Exact failure

`java.lang.NullPointerException: Attempt to invoke virtual method org.json.JSONObject.optJSONObject(java.lang.String) on a null object reference`

Observed stack, Android UI main thread:
- `LikasDiscoveryActivity.level(LikasDiscoveryActivity.java:67)`
- `speciesCard(...:88)`
- `shelf(...:189)`
- `render(...:237)`
- `onCreate(...:287)`.

Cause: `shelf()` passes `statusIndex.get(scientificName)` into `speciesCard()`. There are **six** dated individually reviewed IUCN/DENR status entries and **96** curated species cards. The other **90** species receive `null`. `speciesCard()` calls `level(status)`, which originally executed `status.optJSONObject()` without a null check. Android threw before the first view finished rendering. This is a **deterministic home-library crash for many records**, not an IUCN/DENR data integrity issue.

## Correction

`app/src/main/java/org/phwildlife/survey/LikasDiscoveryActivity.java` now makes `level(JSONObject status)` return **0** for `status==null`. The existing `edge(0)` returns the neutral `UiKit.BORDER`, and the existing `if(status!=null)` skips category badges. This preserves the scientific rule: a missing IUCN/DENR review is **not** LC, DD, unlisted, or a local rarity estimate. Dated reviewed statuses retain their original color and code. Habitat rendering and source citations remain unchanged.

## Regression gates

- Extended `scripts/test_rc34_conservation_discovery_ui.py` with an explicit guard for `if(status==null)return 0;`, checks **90 taxa** without reviewed category entries and verifies neutral fallback.
- Added Android instrumented `Rc35DiscoveryNullStatusInstrumentedTest` with launch assertions for **Species** and **Habitats** and no inferred rarity.
- CI now compiles the instrumented Android test APK via `:app:assembleDebugAndroidTest` in addition to the source tests, JVM tests and signed `:app:assembleDebug`. **Compiled does not mean executed.** No current emulator or physical device acceptance is claimed.

## Exact build handoff

- App/test source: `b0cd9f36caa44dd328813bc50b46d3cf37636d99` (followed by documentation-only commits).
- GitHub Scientific Core QA workflow: **40853862614**, completed **success**.
- GitHub Actions artifact **12888536434**, `LIKAS-RC35-species-crash-hotfix-debug`.
- Verified downloadable file in current chat: `/mnt/data/LIKAS-v0.27.3-RC35-species-crash-hotfix-debug.apk`.
- Size **9,656,890 bytes**; APK SHA-256 **`c64b236fa509cb038085e7e75b4288c69102785c0f72e967d93cbc236e1e7fc8`**.
- Compiled package `org.phwildlife.survey.beta08`; versionCode **95**; versionName **`0.27.3-rc35-species-crash-hotfix-preview`**.
- APK Signing Block v2 signer X509 certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**, matches RC34 pinned signer. APK ZIP/artifact CRC checks passed.
- Asset count verified against exact APK: original **456** species research facts plus **8 + 26 + 15** source-checked addenda = **505**, six separately dated conservation card entries and fifteen habitat profiles.
- No database schema, APK application ID, signing key, original art, user survey records, conservation evidence claims or photo licenses were changed.

## Open acceptance

Real-device upgrade, verified tapping through Species and Habitats, Android 14 320dp/200% typography, orientation/back-stack checks, long library scrolling, full instrumented suite execution, GNSS/camera/audio and owner-data restore remain separate tasks. RC35 is a signed **development preview**, not a field-accepted production release.

**Requirements protected:** G-01, G-03, G-05, U-10, U-11, DL-01, CC-01.
