#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
capture(){
  local file="$1"
  adb pull "/sdcard/Pictures/LIKAS_RC64/$file" "emulator-qa/$file" >/dev/null
  python3 - "$file" <<'PY'
from pathlib import Path
import sys
d=(Path("emulator-qa")/sys.argv[1]).read_bytes()
assert len(d)>5000 and d[:8]==b"\x89PNG\r\n\x1a\n",sys.argv[1]
PY
}
for setting in \
  "p360d160 360x800 160 1.0" \
  "p360d440 990x2200 440 1.0" \
  "p320d440f200 880x1980 440 2.0"; do
  read -r preset size density font <<< "$setting"
  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  gradle --no-daemon :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc64LibraryPhoneIconsInstrumentedTest" \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$preset" \
    > "emulator-qa/rc64-$preset-tests.txt" 2>&1 || {
      tail -n 140 "emulator-qa/rc64-$preset-tests.txt"; exit 1;
    }
  capture "rc64-$preset-top.png"
  capture "rc64-$preset-bottom.png"
done
adb shell wm density 160
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc60LibraryVisualInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc64-library-and-science-regressions.txt 2>&1 || {
    tail -n 150 emulator-qa/rc64-library-and-science-regressions.txt; exit 1;
  }
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
p=Path("emulator-qa")
for cfg in ("p360d160","p360d440","p320d440f200"):
    a=(p/f"rc64-{cfg}-top.png").read_bytes()
    b=(p/f"rc64-{cfg}-bottom.png").read_bytes()
    assert sha256(a).digest()!=sha256(b).digest(),cfg
print("RC64 native 160/440dpi Library icon occupancy and full scientific regression PASS")
PY
adb logcat -d -t 1200 > emulator-qa/logcat-tail.txt || true
