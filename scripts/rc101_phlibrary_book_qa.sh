#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc100_phlibrary_integration_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc101-phlibrary-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc101PhLibraryV182InstrumentedTest "$RUNNER" \
    | tee "rc101-phlibrary-evidence/$preset.txt"
  grep -Fq 'OK (2 tests)' "rc101-phlibrary-evidence/$preset.txt"
done
adb logcat -d -s AndroidRuntime:E > rc101-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc101-phlibrary-evidence/android_runtime_errors.txt
echo 'RC101: canonical 715-entry book v1.82 and rarity v0.3.4, full original bibliography and taxonomic splits passed.' | tee rc101-phlibrary-evidence/summary.txt
