#!/usr/bin/env bash
set -euo pipefail
mkdir -p rc92-unverified-evidence
PACKAGE=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
adb shell wm size 1080x2280
adb shell wm density 420
adb shell settings put system font_scale 1.0
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell pm grant "$PACKAGE" android.permission.ACCESS_FINE_LOCATION
adb shell pm grant "$PACKAGE" android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant "$PACKAGE" android.permission.RECORD_AUDIO
# Offline GNSS preflight, not a fictional location measurement.
adb shell cmd location set-location-enabled false
adb shell cmd location is-location-enabled | tee rc92-unverified-evidence/location_disabled.txt
adb shell am instrument -w -r -e class org.phwildlife.survey.Rc92UnverifiedDeviceInstrumentedTest "$RUNNER" | tee rc92-unverified-evidence/gps_audio.txt
grep -Fq 'OK (3 tests)' rc92-unverified-evidence/gps_audio.txt
# A fresh target process must rehydrate the still-active survey and cached WAV.
adb shell am force-stop "$PACKAGE"
adb shell cmd location set-location-enabled true
adb shell am instrument -w -r -e class org.phwildlife.survey.Rc92ProcessRestartInstrumentedTest "$RUNNER" | tee rc92-unverified-evidence/process_restart.txt
grep -Fq 'OK (1 test)' rc92-unverified-evidence/process_restart.txt
adb logcat -d -s AndroidRuntime:E > rc92-unverified-evidence/android_runtime_errors.txt
test ! -s rc92-unverified-evidence/android_runtime_errors.txt
