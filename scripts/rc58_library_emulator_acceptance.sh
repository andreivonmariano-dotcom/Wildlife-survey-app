#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
mkdir -p emulator-qa
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell wm density 160
capture(){
  local filename="$1"
  adb pull "/sdcard/Pictures/LIKAS_RC58/$filename" "emulator-qa/$filename" >/dev/null
  python3 - "$filename" <<'PY'
from pathlib import Path
import sys
content=(Path("emulator-qa")/sys.argv[1]).read_bytes()
assert len(content)>5000 and content[:8]==b"\x89PNG\r\n\x1a\n",sys.argv[1]
PY
}
for cfg in "p360f100 360x800 1.0" "p412f130 412x892 1.3" "p320f200 320x720 2.0"; do
  read -r name size font <<< "$cfg"
  adb shell wm size "$size"
  adb shell settings put system font_scale "$font"
  # Library browsing is read-only; the emulator is disposable and contains no user records.
  gradle --no-daemon :app:connectedDebugAndroidTest \
    '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc58LibraryVisualInstrumentedTest' \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$name" \
    > "emulator-qa/rc58-$name-instrumentation.txt" 2>&1 || {
      tail -n 150 "emulator-qa/rc58-$name-instrumentation.txt"; exit 1;
    }
  for file in library species-top species-scrolled habitats-top habitats-scrolled; do
    capture "rc58-$name-$file.png"
  done
done
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
root=Path("emulator-qa")
for size in ("p360f100","p412f130","p320f200"):
    for group in ("species","habitats"):
        before=(root/f"rc58-{size}-{group}-top.png").read_bytes()
        after=(root/f"rc58-{size}-{group}-scrolled.png").read_bytes()
        assert sha256(before).digest()!=sha256(after).digest(),(size,group,"scroll did not change")
print("RC58 Library hub, Species and Habitats screenshots captured and scrolled: PASS")
PY
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc36LibrarySwipeInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc58-reference-science-instrumentation.txt 2>&1 || {
    tail -n 160 emulator-qa/rc58-reference-science-instrumentation.txt; exit 1;
  }
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC58 Library hub, Discover and research ZIP emulator regression PASS" | tee emulator-qa/rc58-summary.txt
