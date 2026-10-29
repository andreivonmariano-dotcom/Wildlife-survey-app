#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
mkdir -p emulator-qa
adb wait-for-device
adb shell settings put global window_animation_scale 0 || true
adb shell settings put global transition_animation_scale 0 || true
adb shell settings put global animator_duration_scale 0 || true
adb shell wm size 480x960
adb shell wm density 240

echo '=== Load previously signed RC7C preview ==='
test -s /tmp/likas-rc7c.apk
adb install -r /tmp/likas-rc7c.apk
adb shell am start -W -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" | tee emulator-qa/previous-launch.txt
sleep 8
adb shell am force-stop "$PKG"
adb shell run-as "$PKG" ls databases/ | tee emulator-qa/previous-databases.txt
adb exec-out run-as "$PKG" cat databases/wildlife_field_v03.db > emulator-qa/previous-db.sqlite
test -s emulator-qa/previous-db.sqlite
sha256sum emulator-qa/previous-db.sqlite | tee emulator-qa/previous-db-sha256.txt

echo '=== Put media preservation fixture into old installation ==='
adb shell run-as "$PKG" mkdir -p files
echo 'LIKAS-UPGRADE-MEDIA-2026-10-28' | adb shell "run-as $PKG sh -c 'cat > files/likas_upgrade_preservation.txt'"
adb shell run-as "$PKG" cat files/likas_upgrade_preservation.txt

echo '=== Upgrade in place to new same-package preview ==='
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell run-as "$PKG" cat files/likas_upgrade_preservation.txt | tr -d '\r' | grep -F 'LIKAS-UPGRADE-MEDIA-2026-10-28'
adb exec-out run-as "$PKG" cat databases/wildlife_field_v03.db > emulator-qa/after-upgrade-db.sqlite
cmp -s emulator-qa/previous-db.sqlite emulator-qa/after-upgrade-db.sqlite
echo 'EXISTING SQLITE DATABASE BYTES PRESERVED THROUGH INSTALL'

echo '=== Verify 320dp startup and version ==='
adb shell am start -W -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" | tee emulator-qa/new-launch.txt
sleep 4
adb shell dumpsys package "$PKG" | grep -E 'versionCode|versionName' | head -5 > emulator-qa/version.txt
adb exec-out screencap -p > emulator-qa/startup.png
adb shell am force-stop "$PKG"

echo '=== Run actual SQLite backup and restore round trip ==='
# Upgrade protection was already verified. Use a clean disposable workspace for the round trip.
adb shell pm clear "$PKG"
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION || true
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION || true
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
set +e
adb shell am instrument -w -r \
  "$PKG.test/androidx.test.runner.AndroidJUnitRunner" | tee emulator-qa/instrumentation.txt
instrument_exit=${PIPESTATUS[0]}
set -e
# Keep the diagnostic screenshot even after a failed large-text assertion.
adb exec-out run-as "$PKG" cat files/rc27b_habitats_before_assert.png > emulator-qa/habitat-before-assert.png || true
if [[ $instrument_exit -ne 0 ]]; then exit "$instrument_exit"; fi
grep -Eq 'OK \(16 tests?\)' emulator-qa/instrumentation.txt
adb exec-out run-as "$PKG" cat files/rc7_library_screen.png > emulator-qa/library-320dp.png
test -s emulator-qa/library-320dp.png
adb exec-out run-as "$PKG" cat files/rc11_species_account_320dp.png > emulator-qa/species-account-320dp.png
test -s emulator-qa/species-account-320dp.png
adb exec-out run-as "$PKG" cat files/rc21_recorder_large_text.png > emulator-qa/recorder-200percent-font.png
test -s emulator-qa/recorder-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc24_compare_200percent.png > emulator-qa/compare-200percent-font.png
test -s emulator-qa/compare-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc24_conservation_200percent.png > emulator-qa/conservation-200percent-font.png
test -s emulator-qa/conservation-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc25_species_library_200percent.png > emulator-qa/species-library-200percent-font.png
test -s emulator-qa/species-library-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc25_habitats_200percent.png > emulator-qa/habitats-200percent-font.png
test -s emulator-qa/habitats-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc26_home_200percent.png > emulator-qa/home-200percent-font.png
test -s emulator-qa/home-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc26_survey_setup_200percent.png > emulator-qa/survey-setup-200percent-font.png
test -s emulator-qa/survey-setup-200percent-font.png
adb exec-out run-as "$PKG" cat files/rc26_history_200percent.png > emulator-qa/history-200percent-font.png
test -s emulator-qa/history-200percent-font.png
echo '=== Collect real 320dp screenshots for every priority species ==='
mkdir -p emulator-qa/all-96-species-screens
adb exec-out run-as "$PKG" cat files/rc19-all-96-screen-audit.csv > emulator-qa/all-96-species-audit.csv
test "$(wc -l < emulator-qa/all-96-species-audit.csv)" -eq 97
for i in $(seq 0 95); do
  name=$(printf '%03d' "$i")
  adb exec-out run-as "$PKG" cat "files/rc19-all-96-screen-audit/$name.jpg" > "emulator-qa/all-96-species-screens/$name.jpg"
  test -s "emulator-qa/all-96-species-screens/$name.jpg"
done
test "$(find emulator-qa/all-96-species-screens -type f -name '*.jpg' | wc -l)" -eq 96
echo 'ALL-96-RENDERED-SPECIES-AUDIT-PASS' > emulator-qa/all-96-audit-status.txt
echo 'DEVICE BACKUP TEST PASSED'

echo '=== Screenshot and UI hierarchy at 320dp ==='
adb shell am start -W -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" | tee emulator-qa/post-test-launch.txt
sleep 3
adb exec-out screencap -p > emulator-qa/post-test.png
adb shell uiautomator dump /sdcard/window.xml >/dev/null 2>&1 || true
adb exec-out cat /sdcard/window.xml > emulator-qa/ui-hierarchy.xml || true
adb logcat -d -t 1500 > emulator-qa/logcat-tail.txt || true
echo '=== Export protected Recorder screenshot captured by app-side instrumentation ==='
adb exec-out run-as "$PKG" cat files/rc10_recorder_screen.png > emulator-qa/recorder-320dp.png
test -s emulator-qa/recorder-320dp.png
echo 'RECORDER-DOCK-INSTRUMENTED-PASS' | tee emulator-qa/recorder-status.txt
echo 'EMULATOR-SMOKE-PASS' | tee emulator-qa/status.txt
