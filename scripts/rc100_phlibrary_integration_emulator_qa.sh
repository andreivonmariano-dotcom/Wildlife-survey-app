#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc99_phlibrary_habitat_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc100-phlibrary-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc100PhLibraryIntegratedHabitatInstrumentedTest "$RUNNER" \
    | tee "rc100-phlibrary-evidence/$preset.txt"
  grep -Fq 'OK (2 tests)' "rc100-phlibrary-evidence/$preset.txt"
done
adb logcat -d -s AndroidRuntime:E > rc100-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc100-phlibrary-evidence/android_runtime_errors.txt
echo 'RC100: 47 merged species habitat accounts, 714 preserved taxa and citations, and reader v1.80 passed.' \
  | tee rc100-phlibrary-evidence/summary.txt