# LIKAS RC57 | Screen 1 Home visual recheck

**30 October 2026 (Asia/Manila). Status:** Code staged; Android emulator QA pending.

## Scope and baseline evidence
- Based on actual RC56 live recorder code SHA `587c8b52c1545e82251b3e70c6b961fb91c7ea17`, with upstream PR #53 draft and prior RC52 Home acceptance.
- Reopened the actual Android 14 RC52 Home PNGs and accessibility XML (run `41050079116`) at 360dp/100%, 412dp/130%, and 320dp/200%. At 320dp/200%, the non-active hero occupied bounds **[16,36][304,286]**, height **250dp**, and Settings occupied **[16,555][304,684]**, exactly reaching the Home ScrollView lower bound y=684. The screenshot visibly clips the Settings card's lower border. Scrolling reveals it.
- This is a small, verified layout deficiency, not a field-recording failure.

## Scoped Android change
- On Home with no active survey and system font scale >=160%, reduce the hero minimum height, vertical padding, title top margin, title base font to 21sp, and subtitle base font to 12sp. The Android system still applies 160-200% scaling and text remains native TextView content.
- Preserve the exact LIKAS text, approved frog/fieldwork/library/settings artwork, current palette, normal-size hero, compact active-survey hero, Home navigation, Resume ordering and all screen-reader descriptions.
- Version preview code 117, name `0.27.3-rc57-home-visual-preview`; original package `org.phwildlife.survey.beta08`, signer, schema 27 and scientific modules unchanged. No data migration, taxonomy or survey logic changed.

## Android verification gate
- New `scripts/rc57_home_emulator_acceptance.sh` captures the actual Android screen at 360dp/100%, 412dp/130%, and 320dp/200%, including post-scroll, with accessibility hierarchies.
- Assert the measured 200% hero is **under 240dp** and that the Settings card ends **at least 8dp above the ScrollView boundary**, plus all Home navigation targets present. Rerun Home and research ZIP instrumentation on a disposable AVD, all Python/JVM tests and signed app compile.
- Independently inspect screenshots and verify the new APK signing, version and archive before delivering direct APK.
- Release gates remain: physical TalkBack/outdoor visibility, real historic in-place upgrade and record restore, GPS/elevation, extended survey/export integrity.

## Next screen
After actual Home screen acceptance, continue with Fieldwork, Survey Setup, GPS preflight, Live Recorder, History/observation review, and the remaining Library, Habitat, Research and Settings screens, without combining unrelated changes.

## Emulator and release acceptance | 30 October 2026
- **Verified application and test source:** `13261f6f49a1a68c86b2fbf76f02a531c2256f4e` on draft PR #54; final Android 14 workflow `41084197310` **success**. The RC4 ancestry PR gate passed on the draft branch. Earlier CI attempts failed on stale test assertions for v116 and the previous fixed-height hero expression; those checks were updated to v117 with no field-data code edits.
- **Automated testing:** 343 Python tests, signed app/JVM compile, Android 14 emulator with **3 passing instrumentation tests** (Home navigation/geometry and research ZIP), zero failures in final run.
- **Actual screenshot measurements:** prior RC52 at 320dp/200% had Home hero `[16,36][304,286]` (250dp tall), Settings `[16,555][304,684]`, and ScrollView bottom 684. New RC57 hero `[16,36][304,251]` (**215dp**, 35dp shorter), Settings `[16,520][304,653]` (31dp above scroll boundary). All three Home destinations remain readable without horizontal clipping, in the unscrolled 320dp/200% screenshot. The normal 360dp/100% and 412dp/130% hero bounds remained `[*,36][*,187]` (151dp). The approved art and palette visibly remain intact. Full PNG/XML source in emulator artifact `12990384839`.
- **APK artifact:** `12990743567`, extracted direct APK `LIKAS-v0.27.3-RC57-home-visual-preview-debug.apk`, **9,786,904 bytes**, SHA-256 `41a3b6a2913506ac52e87db14268b1274a4e5425ee4e109bbd6384452bfdc923`. APK ZIP CRC passes; 18 approved icon PNGs retained. APK v2 RSA-PKCS1-SHA256 signedData signature, original certificate SHA256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, and APK v2 whole-file signed-content digest independently verified. Build source app ID remains `org.phwildlife.survey.beta08`, preview versionCode **117**, SQLite **27**.
- **In-chat direct deliverables:** actual APK, Android screenshots/evidence ZIP, and actual 320dp/200% before-after PNG were attached with direct verified file links. These links are session-specific.
- **Unverified:** actual physical Android daylight/TalkBack, active-survey Home state screenshot at 200%, in-place upgrade with historical observations/photos/audio, process-death and continuous export consistency. Existing stacked PRs are draft and not production-released.
- **Next:** continue **Screen 2 Fieldwork hub** in a new scoped branch from verified RC57 code, showing actual Android screenshots at normal and enlarged text. Preserve all field methodologies and approved art.
