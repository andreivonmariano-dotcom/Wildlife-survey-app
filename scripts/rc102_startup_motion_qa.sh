#!/usr/bin/env bash
set -euo pipefail
# Preserve all prior PHLibrary, UI and survey regression coverage.
bash scripts/rc101_phlibrary_book_qa.sh

PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc102-startup-evidence
# Previous emulator suites run with animations disabled. Restore native
# animation scales here so the scale/letter/line progression is exercised.
adb shell settings put global window_animation_scale 1.0
adb shell settings put global transition_animation_scale 1.0
adb shell settings put global animator_duration_scale 1.0

for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e preset "$preset" -e class \
    org.phwildlife.survey.Rc102StartupMotionInstrumentedTest "$RUNNER" \
    | tee "rc102-startup-evidence/${preset}.txt"
  grep -Fq 'OK (2 tests)' "rc102-startup-evidence/${preset}.txt"
  for variant in reduced animated; do
    name="${preset}-${variant}.png"
    adb pull "/sdcard/Android/data/${PKG}/files/Pictures/RC102/${name}" \
      "rc102-startup-evidence/${name}"
    test -s "rc102-startup-evidence/${name}"
  done
done
adb logcat -d -s AndroidRuntime:E > rc102-startup-evidence/android_runtime_errors.txt
test ! -s rc102-startup-evidence/android_runtime_errors.txt
printf 'RC102: centered startup at both font scales, real short motion, reduced-motion fallback, unchanged canonical Book v1.82 and signed data gate.\n' \
  | tee rc102-startup-evidence/summary.txt
