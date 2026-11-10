#!/usr/bin/env bash
set -euo pipefail
# Preserve the RC93 persistence and RC94 PHLibrary gates, then test BioTools routing.
bash scripts/rc94_phlibrary_emulator_qa.sh
PACKAGE=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc95-biotools-evidence
for scale in 160 200; do
  adb shell pm clear "$PACKAGE"
  if [[ "$scale" == "160" ]]; then
    adb shell wm size 1080x2280
    adb shell wm density 480
    adb shell settings put system font_scale 1.6
  else
    adb shell wm size 960x2160
    adb shell wm density 480
    adb shell settings put system font_scale 2.0
  fi
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc95BioToolsNavigationInstrumentedTest "$RUNNER" \
    | tee "rc95-biotools-evidence/navigation_${scale}.txt"
  grep -Fq 'OK (5 tests)' "rc95-biotools-evidence/navigation_${scale}.txt"
done
adb logcat -d -s AndroidRuntime:E > rc95-biotools-evidence/android_runtime_errors.txt
test ! -s rc95-biotools-evidence/android_runtime_errors.txt
