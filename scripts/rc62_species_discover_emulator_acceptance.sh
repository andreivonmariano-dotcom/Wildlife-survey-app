#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb shell wm density 160
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
capture(){
  local f="$1"
  adb pull "/sdcard/Pictures/LIKAS_RC62/$f" "emulator-qa/$f" >/dev/null
  python3 - "$f" <<'PY'
from pathlib import Path
import sys
d=(Path("emulator-qa")/sys.argv[1]).read_bytes()
assert len(d)>5000 and d[:8]==b"\x89PNG\r\n\x1a\n",sys.argv[1]
PY
}
for setting in "p360f100 360x800 1.0" "p412f130 412x892 1.3" "p320f200 320x720 2.0"; do
  read -r code size font <<< "$setting"
  adb shell wm size "$size"
  adb shell settings put system font_scale "$font"
  gradle --no-daemon :app:connectedDebugAndroidTest \
    '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc62SpeciesDiscoverInstrumentedTest' \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$code" \
    > "emulator-qa/rc62-$code-instrumentation.txt" 2>&1 || {
      tail -n 160 "emulator-qa/rc62-$code-instrumentation.txt"; exit 1;
    }
  for view in species-top species-scrolled search; do
    capture "rc62-$code-$view.png"
  done
done
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
p=Path("emulator-qa")
for config in ("p360f100","p412f130","p320f200"):
    a=(p/f"rc62-{config}-species-top.png").read_bytes()
    b=(p/f"rc62-{config}-species-scrolled.png").read_bytes()
    assert sha256(a).digest()!=sha256(b).digest(),(config,"not scrolled")
print("Species top and scrolled real Android screenshots across three configurations PASS")
PY
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc58LibraryVisualInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc62-library-science-instrumentation.txt 2>&1 || {
    tail -n 150 emulator-qa/rc62-library-science-instrumentation.txt; exit 1;
  }
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC62 Species Discover 3 sizes + RC60 Library + research ZIP Android regression PASS" | tee emulator-qa/rc62-summary.txt
