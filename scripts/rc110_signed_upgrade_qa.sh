#!/usr/bin/env bash
set -euo pipefail
# Real no-data-clear in-place upgrade: pinned signed RC109 -> signed RC110.
# Test APK uses the original signer; no adb uninstall or pm clear is permitted.
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
BASELINE=/tmp/apo-rc109-signed.apk
RC110=app/build/outputs/apk/debug/app-debug.apk
TEST=app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
OUT=rc110-upgrade-evidence
mkdir -p "$OUT"
test -s "$BASELINE" && test -s "$RC110" && test -s "$TEST"
adb shell wm size 1080x2280
adb shell wm density 420
adb install "$BASELINE" | tee "$OUT/install_rc109.txt"
adb install -r "$TEST" | tee "$OUT/install_android_test.txt"
adb shell am instrument -w -r -e class "org.phwildlife.survey.Rc110SignedUpgradeInstrumentedTest#aSeedRc109Workspace" "$RUNNER" | tee "$OUT/pre_upgrade_seed.txt"
grep -Fq 'OK (1 test)' "$OUT/pre_upgrade_seed.txt"
adb shell dumpsys package "$PKG" > "$OUT/pre_upgrade_package.txt"
grep -Eq 'versionCode=169([[:space:]]|\b)' "$OUT/pre_upgrade_package.txt"
# An in-place install retains database, shared preferences and audio in app storage.
adb install -r "$RC110" | tee "$OUT/install_rc110_in_place.txt"
adb shell dumpsys package "$PKG" > "$OUT/post_upgrade_package.txt"
grep -Eq 'versionCode=175([[:space:]]|\b)' "$OUT/post_upgrade_package.txt"
adb shell am instrument -w -r -e class "org.phwildlife.survey.Rc110SignedUpgradeInstrumentedTest#bRc110UpgradePreservesRowsMediaAndExport" "$RUNNER" | tee "$OUT/post_upgrade_validation.txt"
grep -Fq 'OK (1 test)' "$OUT/post_upgrade_validation.txt"
adb logcat -d -s AndroidRuntime:E > "$OUT/android_runtime_errors.txt"
test ! -s "$OUT/android_runtime_errors.txt"
echo "Signed in-place RC109 code169 -> RC115 code175 passed; 3 survey outcomes, field record, point-count, WAV bytes and research exports retained." | tee "$OUT/summary.txt"
