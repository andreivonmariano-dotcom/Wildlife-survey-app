#!/usr/bin/env bash
set -euo pipefail
# Preserve all RC95 source and signed-upgrade checks and add the Book 01 reader.
bash scripts/rc95_biotools_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc96-phlibrary-evidence
for setting in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset size density scale <<< "$setting"
  adb shell pm clear "$PKG"
  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$scale"
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc96PhLibraryReaderInstrumentedTest "$RUNNER" \
    | tee "rc96-phlibrary-evidence/${preset}.txt"
  grep -Fq 'OK (2 tests)' "rc96-phlibrary-evidence/${preset}.txt"
done
adb logcat -d -s AndroidRuntime:E > rc96-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc96-phlibrary-evidence/android_runtime_errors.txt
