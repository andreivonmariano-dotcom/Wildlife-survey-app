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
  adb shell am instrument -w -r -e preset "$preset" -e class \
    org.phwildlife.survey.Rc96PhLibraryReaderInstrumentedTest "$RUNNER" \
    | tee "rc96-phlibrary-evidence/${preset}.txt"
  grep -Fq 'OK (2 tests)' "rc96-phlibrary-evidence/${preset}.txt"
  for name in directory profile-top profile-bottom; do
    file="rc96-${preset}-${name}.png"
    adb pull "/sdcard/Android/data/${PKG}/files/Pictures/LIKAS_RC96/${file}" "rc96-phlibrary-evidence/${file}" > /dev/null
  done
done
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
root=Path("rc96-phlibrary-evidence")
for preset in ("p360f100","p320f200"):
    files=[root/f"rc96-{preset}-{name}.png" for name in ("directory","profile-top","profile-bottom")]
    data=[p.read_bytes() for p in files]
    assert all(len(b)>5000 and b[:8]==bytes([137,80,78,71,13,10,26,10]) for b in data),files
    assert sha256(data[1]).digest()!=sha256(data[2]).digest(),preset
print("Actual PHLibrary Android reader screenshots pass PNG and scrolling checks")
PY
adb logcat -d -s AndroidRuntime:E > rc96-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc96-phlibrary-evidence/android_runtime_errors.txt
