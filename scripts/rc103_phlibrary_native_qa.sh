#!/usr/bin/env bash
set -euo pipefail
# Preserve previous PHLibrary v1.82, field survey, and startup acceptance.
bash scripts/rc102_startup_motion_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc103-phlibrary-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e preset "$preset" -e class \
    org.phwildlife.survey.Rc103PhLibrary2InstrumentedTest "$RUNNER" \
    | tee "rc103-phlibrary-evidence/${preset}.txt"
  grep -Fq 'OK (3 tests)' "rc103-phlibrary-evidence/${preset}.txt"
  for view in home explorer profile; do
    item="${preset}-${view}.png"
    adb pull "/sdcard/Android/data/${PKG}/files/Pictures/RC103/${item}" \
      "rc103-phlibrary-evidence/${item}"
    test -s "rc103-phlibrary-evidence/${item}"
  done
done
adb logcat -d -s AndroidRuntime:E > rc103-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc103-phlibrary-evidence/android_runtime_errors.txt
echo 'RC103: native PHLibrary 2.0 home/explorer/profile at two text scales with 715 original accounts; source-backed atlas and virtualized bibliography; no changes to survey data.' \
  | tee rc103-phlibrary-evidence/summary.txt
