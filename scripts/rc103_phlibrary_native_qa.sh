#!/usr/bin/env bash
set -euo pipefail
# RC103 replaces the obsolete RC94–RC100 text-only visual contracts. Verify
# the current source-faithful reader, startup and all three native screens instead.
# Prior signed database preservation remains independently gated in the workflow.
# RC92 installs the app and test APKs, checks device/audio/GPS persistence and
# backup restoration without requiring superseded PHLibrary visual assertions.
bash scripts/rc92_unverified_device_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
mkdir -p rc103-phlibrary-evidence
adb shell settings put global window_animation_scale 1.0
adb shell settings put global transition_animation_scale 1.0
adb shell settings put global animator_duration_scale 1.0
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  for spec in \
    "Rc101PhLibraryV182InstrumentedTest 2" \
    "Rc96PhLibraryReaderInstrumentedTest 2" \
    "Rc102StartupMotionInstrumentedTest 2" \
    "Rc103PhLibrary2InstrumentedTest 4"; do
    read -r cls expected <<< "$spec"
    adb shell am instrument -w -r -e preset "$preset" -e class \
      "org.phwildlife.survey.${cls}" "$RUNNER" \
      | tee "rc103-phlibrary-evidence/${preset}-${cls}.txt"
    grep -Fq "OK (${expected} tests)" \
      "rc103-phlibrary-evidence/${preset}-${cls}.txt"
  done
  for view in home explorer profile habitat-index habitat-detail; do
    item="${preset}-${view}.png"
    adb pull "/sdcard/Android/data/${PKG}/files/Pictures/RC103/${item}" \
      "rc103-phlibrary-evidence/${item}" >/dev/null
    test -s "rc103-phlibrary-evidence/${item}"
  done
  for variant in reduced animated; do
    item="${preset}-${variant}.png"
    adb pull "/sdcard/Android/data/${PKG}/files/Pictures/RC102/${item}" \
      "rc103-phlibrary-evidence/${item}" >/dev/null
    test -s "rc103-phlibrary-evidence/${item}"
  done
done
python3 - <<'PY'
from pathlib import Path
folder=Path("rc103-phlibrary-evidence")
for preset in ("p360f100","p320f200"):
    for view in ("home","explorer","profile","habitat-index","habitat-detail","reduced","animated"):
        b=(folder/f"{preset}-{view}.png").read_bytes()
        assert len(b)>3000 and b[:8]==bytes([137,80,78,71,13,10,26,10]),(preset,view)
print("Fourteen native screenshot PNGs passed integrity checks.")
PY
adb logcat -d -s AndroidRuntime:E > rc103-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc103-phlibrary-evidence/android_runtime_errors.txt
echo 'RC103: canonical 715 accounts, source-faithful reader, RC102 startup, and native atlas Home/Explorer/Profile passed at normal/200% text. Prior visual suites archived as superseded; signed upgrade checked separately.' \
  | tee rc103-phlibrary-evidence/summary.txt
