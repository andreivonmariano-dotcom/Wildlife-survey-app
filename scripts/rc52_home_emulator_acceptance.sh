#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1

launch() {
  adb shell am force-stop "$PKG" || true
  adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt
  sleep 6
}
capture() {
  local id="$1"
  adb shell screencap -p "/sdcard/$id.png"
  adb pull "/sdcard/$id.png" "emulator-qa/$id.png" >/dev/null
  adb shell uiautomator dump "/sdcard/$id.xml" > "emulator-qa/dump-$id.txt"
  adb pull "/sdcard/$id.xml" "emulator-qa/$id.xml" >/dev/null
}
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
launch
capture rc52-home-360-normal
adb shell wm size 412x892
adb shell wm density 160
adb shell settings put system font_scale 1.3
launch
capture rc52-home-412-font130
adb shell wm size 320x720
adb shell wm density 160
adb shell settings put system font_scale 2.0
launch
capture rc52-home-320-font200
adb shell input swipe 160 605 160 180 600
sleep 1
capture rc52-home-320-font200-scrolled

python3 - <<'PY'
from xml.etree import ElementTree as ET
import re
from pathlib import Path
for id in ("rc52-home-360-normal","rc52-home-412-font130",
           "rc52-home-320-font200","rc52-home-320-font200-scrolled"):
    path=Path("emulator-qa")/(id+".xml")
    nodes=[n.attrib for n in ET.parse(path).getroot().iter("node")]
    if id!="rc52-home-320-font200-scrolled":
        for label in ("Open Fieldwork","Open Library"):
            assert any(label in n.get("content-desc","") for n in nodes), (id,label)
    if id in ("rc52-home-360-normal","rc52-home-412-font130"):
        for label in ("Open Fieldwork","Open Library","Open Settings"):
            hits=[n for n in nodes if label in n.get("content-desc","")]
            assert hits,(id,label)
            box=list(map(int,re.findall(r"\d+",hits[0].get("bounds",""))))
            assert len(box)==4 and box[3]>box[1] and box[1]<720,(id,label,box)
            print(id,label,box)
    if id=="rc52-home-320-font200-scrolled":
        assert any("Open Settings" in n.get("content-desc","") for n in nodes),"Settings unreachable at 200% font"
print("RC52 screen 1 accessibility/navigation hierarchy: PASS")
PY
# All 3 screen-layout and exact research ZIP Android tests in the same disposable AVD.
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc52-instrumentation.txt 2>&1 || {
    tail -n 150 emulator-qa/rc52-instrumentation.txt
    exit 1
  }
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC52 Home 360/412/320dp + Android instrumentation PASS" | tee emulator-qa/rc52-summary.txt
