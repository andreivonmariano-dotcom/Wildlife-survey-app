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
