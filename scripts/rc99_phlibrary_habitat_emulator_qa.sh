#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc98_phlibrary_editorial_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc99-phlibrary-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc99PhLibraryHabitatInstrumentedTest "$RUNNER" \
    | tee "rc99-phlibrary-evidence/$preset.txt"
  grep -Fq 'OK (1 test)' "rc99-phlibrary-evidence/$preset.txt"
done
adb logcat -d -s AndroidRuntime:E > rc99-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc99-phlibrary-evidence/android_runtime_errors.txt
echo "RC99 100 habitat field cues and 714 unchanged rarity tiers pass instrumentation." \
  | tee rc99-phlibrary-evidence/summary.txt
