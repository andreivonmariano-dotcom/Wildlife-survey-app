#!/usr/bin/env bash
set -euo pipefail

mkdir -p biotools-emulator-qa
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell pm grant org.phwildlife.survey.beta08 android.permission.ACCESS_FINE_LOCATION
adb shell pm grant org.phwildlife.survey.beta08 android.permission.ACCESS_COARSE_LOCATION

adb shell wm size > biotools-emulator-qa/screen_size.txt
adb shell wm density > biotools-emulator-qa/screen_density.txt
adb shell settings get system font_scale > biotools-emulator-qa/font_scale.txt

# Run closure tests first while the disposable workspace contains no active survey.
adb shell am instrument -w -r -e class org.phwildlife.survey.BioToolsLifecycleInstrumentedTest org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner | tee biotools-emulator-qa/lifecycle_instrumentation.txt
grep -Eq 'OK \\(5 tests\\)' biotools-emulator-qa/lifecycle_instrumentation.txt

adb shell am instrument -w -r -e class org.phwildlife.survey.BioToolsNavigationInstrumentedTest org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner | tee biotools-emulator-qa/instrumentation.txt

adb pull /sdcard/Android/data/org.phwildlife.survey.beta08/files/biotools_qa biotools-emulator-qa/screenshots || true
adb pull /sdcard/Android/data/org.phwildlife.survey.beta08/files/biotools_qa_lifecycle biotools-emulator-qa/lifecycle_screenshots || true
adb logcat -d -s AndroidRuntime:E > biotools-emulator-qa/android_runtime_errors.txt

grep -Eq 'OK \(8 tests\)' biotools-emulator-qa/instrumentation.txt
