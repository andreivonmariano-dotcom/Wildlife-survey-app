#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
adb shell wm density 160
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/rc60-install.txt 2>&1
capture(){
  local file="$1"
  adb pull "/sdcard/Pictures/LIKAS_RC60/$file" "emulator-qa/$file" >/dev/null
  python3 - "$file" <<'PY'
from pathlib import Path
import sys
p=Path("emulator-qa")/sys.argv[1]
b=p.read_bytes()
assert len(b)>5000 and b[:8]==b"\\x89PNG\\r\\n\\x1a\\n",p
PY
}
for setting in "p360f100 360x800 1.0" "p320f200 320x720 2.0"; do
  read -r preset screen font <<< "$setting"
  adb shell wm size "$screen"
  adb shell settings put system font_scale "$font"
  gradle --no-daemon :app:connectedDebugAndroidTest \
    '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc60PhLibraryReaderInstrumentedTest' \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$preset" \
    > "emulator-qa/rc60-$preset-tests.txt" 2>&1 || {
      tail -n 170 "emulator-qa/rc60-$preset-tests.txt"; exit 1;
    }
  capture "rc60-$preset-directory.png"
  capture "rc60-$preset-profile-top.png"
  capture "rc60-$preset-profile-bottom.png"
done
python3 - <<'PY'
from pathlib import Path
import hashlib
for p in ["p360f100","p320f200"]:
    top=(Path("emulator-qa")/f"rc60-{p}-profile-top.png").read_bytes()
    bottom=(Path("emulator-qa")/f"rc60-{p}-profile-bottom.png").read_bytes()
    assert hashlib.sha256(top).digest()!=hashlib.sha256(bottom).digest(),p
print("RC60 directory and full-paragraph reader real Android screenshots PASS")
PY
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc59LibraryHubInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc60-science-regression.txt 2>&1 || {
    tail -n 150 emulator-qa/rc60-science-regression.txt; exit 1;
  }
adb logcat -d -t 900 > emulator-qa/rc60-logcat-tail.txt || true
echo "RC60 PHLibrary 714-taxon directory, profile and research ZIP regression PASS" \
  | tee emulator-qa/rc60-summary.txt
