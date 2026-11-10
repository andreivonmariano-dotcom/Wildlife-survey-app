#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc97_phlibrary_visual_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc98-phlibrary-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell pm clear "$PKG"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc98PhLibraryEditorialInstrumentedTest "$RUNNER" \
    | tee "rc98-phlibrary-evidence/$preset.txt"
  grep -Fq 'OK (2 tests)' "rc98-phlibrary-evidence/$preset.txt"
done
adb logcat -d -s AndroidRuntime:E > rc98-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc98-phlibrary-evidence/android_runtime_errors.txt
echo "RC98 edited habitat prose, one-entry book hub and group/rarity links pass." \
  | tee rc98-phlibrary-evidence/summary.txt
