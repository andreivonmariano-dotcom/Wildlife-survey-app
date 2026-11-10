#!/usr/bin/env bash
set -euo pipefail
# The emulator-runner executes script lines separately unless handed a shell script.
# Keep both screen/font presets in one bash process; fail closed on test errors.
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc110-gallery-evidence
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am force-stop "$PKG"
  adb shell am instrument -w -r -e preset "$preset" -e class org.phwildlife.survey.Rc110OfflineGalleryInstrumentedTest "$RUNNER" | tee "rc110-gallery-evidence/${preset}-gallery.txt"
  grep -Fq "OK (3 tests)" "rc110-gallery-evidence/${preset}-gallery.txt"
done
echo "RC110 offline gallery 3 Android instrumentation tests passed at 360px standard text and 320px 200% text." | tee rc110-gallery-evidence/summary.txt
