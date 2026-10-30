#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1

launch_home() {
  adb shell am force-stop "$PKG" >/dev/null || true
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
tap_description() {
  local prefix="$1"
  adb shell uiautomator dump /sdcard/rc53-tap.xml >/dev/null
  adb pull /sdcard/rc53-tap.xml emulator-qa/rc53-tap.xml >/dev/null
  local coords
  coords=$(python3 - "$prefix" <<'PY'
import re,sys
from xml.etree import ElementTree as ET
a=ET.parse("emulator-qa/rc53-tap.xml").getroot()
hits=[n.attrib for n in a.iter("node")
      if n.attrib.get("content-desc","").startswith(sys.argv[1])]
if not hits:raise SystemExit("Missing visible action: "+sys.argv[1])
box=list(map(int,re.findall(r"\d+",hits[0]["bounds"])))
assert len(box)==4 and box[2]>box[0] and box[3]>box[1],box
print((box[0]+box[2])//2,(box[1]+box[3])//2)
PY
  )
  adb shell input tap $coords
  sleep 2
}
open_fieldwork() {
  launch_home
  tap_description "Open Fieldwork."
}
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
open_fieldwork
capture rc53-fieldwork-360-normal
adb shell input swipe 155 640 155 245 400
sleep 1
capture rc53-fieldwork-360-scrolled

adb shell wm size 412x892
adb shell wm density 160
adb shell settings put system font_scale 1.3
open_fieldwork
capture rc53-fieldwork-412-font130
adb shell input swipe 190 730 190 225 460
sleep 1
capture rc53-fieldwork-412-scrolled

adb shell wm size 320x720
adb shell wm density 160
adb shell settings put system font_scale 2.0
open_fieldwork
capture rc53-fieldwork-320-font200
adb shell input swipe 150 600 150 205 470
sleep 1
capture rc53-fieldwork-320-mid
adb shell input swipe 150 600 150 205 470
sleep 1
capture rc53-fieldwork-320-bottom
adb shell input swipe 150 600 150 205 470
sleep 1
capture rc53-fieldwork-320-bottom2
tap_description "Show additional field tools"
capture rc53-fieldwork-320-tools-expanded
# The five expanded options follow the button in the scroll view, so check
# them AFTER an ordinary swipe rather than assuming they are immediately onscreen.
adb shell input swipe 150 595 150 190 480
sleep 1
capture rc53-fieldwork-320-tools-expanded-scrolled

python3 - <<'PY'
from xml.etree import ElementTree as ET
from pathlib import Path
captures={
 "360":["rc53-fieldwork-360-normal","rc53-fieldwork-360-scrolled"],
 "412":["rc53-fieldwork-412-font130","rc53-fieldwork-412-scrolled"],
 "320":["rc53-fieldwork-320-font200","rc53-fieldwork-320-mid",
        "rc53-fieldwork-320-bottom","rc53-fieldwork-320-bottom2",
        "rc53-fieldwork-320-tools-expanded",
        "rc53-fieldwork-320-tools-expanded-scrolled"]
}
for width,paths in captures.items():
    descriptions=[]
    for name in paths:
        nodes=ET.parse(Path("emulator-qa")/(name+".xml")).getroot().iter("node")
        descriptions.extend(n.attrib.get("content-desc","") for n in nodes)
    for label in ("Open Birds.","Open Herpetofauna.","Open Mammals.",
                  "Open Projects & stations","Open History.","Open Analysis.",
                  "Show additional field tools"):
        assert any(d.startswith(label) for d in descriptions),(width,label)
    print("All Fieldwork destinations reachable",width)
assert any(d.startswith("Open Camera traps") for d in descriptions),"Expanded tools not visible"
print("RC53 visible hierarchy PASS")
PY

# Verify empty-workspace Home and Fieldwork, including synthetic active Resume.
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch_home
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc53FieldworkScreenInstrumentedTest' \
  > emulator-qa/rc53-fieldwork-instrumentation.txt 2>&1 || {
    tail -n 120 emulator-qa/rc53-fieldwork-instrumentation.txt; exit 1;
  }
# Run science ZIP fixture second: it seeds synthetic archive rows on this AVD.
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc53-research-zip-instrumentation.txt 2>&1 || {
    tail -n 120 emulator-qa/rc53-research-zip-instrumentation.txt; exit 1;
  }
adb logcat -d -t 1400 > emulator-qa/logcat-tail.txt || true
echo "RC53 Fieldwork 360/412/320dp, active Resume, and science ZIP: PASS" | tee emulator-qa/rc53-summary.txt
