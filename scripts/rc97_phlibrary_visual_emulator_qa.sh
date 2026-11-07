#!/usr/bin/env bash
set -euo pipefail
bash scripts/rc96_phlibrary_reader_emulator_qa.sh
PKG=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc97-phlibrary-evidence
for setting in "p360f100 360x800 160 1.0" "p412f130 412x892 160 1.3" "p320f200 320x720 160 2.0"; do
  read -r preset size density font <<< "$setting"
  adb shell pm clear "$PKG"
  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am instrument -w -r -e preset "$preset" -e class \
    org.phwildlife.survey.Rc97PhLibraryVisualInstrumentedTest "$RUNNER" \
    | tee "rc97-phlibrary-evidence/$preset.txt"
  grep -Fq 'OK (3 tests)' "rc97-phlibrary-evidence/$preset.txt"
  for name in hub-top hub-bottom directory profile-top profile-bottom; do
    file="rc97-$preset-$name.png"
    adb pull "/sdcard/Pictures/APO_RC97/$file" "rc97-phlibrary-evidence/$file" >/dev/null
  done
done
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
root=Path("rc97-phlibrary-evidence")
for preset in ("p360f100","p412f130","p320f200"):
    files=[root/f"rc97-{preset}-{part}.png" for part in
        ("hub-top","hub-bottom","directory","profile-top","profile-bottom")]
    data=[p.read_bytes() for p in files]
    assert all(len(b)>5000 and b[:8]==bytes([137,80,78,71,13,10,26,10])
               for b in data),files
    assert sha256(data[3]).digest()!=sha256(data[4]).digest(),preset
print("All 15 real Android PHLibrary captures are valid and profiles scroll.")
PY
adb logcat -d -s AndroidRuntime:E > rc97-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc97-phlibrary-evidence/android_runtime_errors.txt
