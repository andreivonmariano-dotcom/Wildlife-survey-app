#!/usr/bin/env bash
set -euo pipefail
mkdir -p rc92-upgrade-evidence
PKG=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
adb shell wm size 1080x2280
adb shell wm density 420
adb install /tmp/apo-rc91-signed.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r -e class org.phwildlife.survey.Rc92SignedUpgradeInstrumentedTest\#aSeedSignedRc91Workspace "$RUNNER" | tee rc92-upgrade-evidence/pre_upgrade_seed.txt
grep -Fq 'OK (1 test)' rc92-upgrade-evidence/pre_upgrade_seed.txt
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am instrument -w -r -e class org.phwildlife.survey.Rc92SignedUpgradeInstrumentedTest\#bSignedRc92UpgradePreservesRowsMediaAndExport "$RUNNER" | tee rc92-upgrade-evidence/post_upgrade_validation.txt
grep -Fq 'OK (1 test)' rc92-upgrade-evidence/post_upgrade_validation.txt
adb logcat -d -s AndroidRuntime:E > rc92-upgrade-evidence/android_runtime_errors.txt
test ! -s rc92-upgrade-evidence/android_runtime_errors.txt
