#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/rc54-install.txt 2>&1

launch_home() {
  adb shell am force-stop "$PKG" >/dev/null || true
  adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/rc54-launch.txt
  sleep 6
}
capture() {
  local name="$1"
  adb shell screencap -p "/sdcard/$name.png"
  adb pull "/sdcard/$name.png" "emulator-qa/$name.png" >/dev/null
  adb shell uiautomator dump "/sdcard/$name.xml" >/dev/null
  adb pull "/sdcard/$name.xml" "emulator-qa/$name.xml" >/dev/null
}
tap_description() {
  local prefix="$1"
  adb shell uiautomator dump /sdcard/rc54-tap.xml >/dev/null
  adb pull /sdcard/rc54-tap.xml emulator-qa/rc54-tap.xml >/dev/null
  local coords
  coords=$(python3 - "$prefix" <<'PY'
import re,sys
from xml.etree import ElementTree as ET
nodes=ET.parse("emulator-qa/rc54-tap.xml").getroot().iter("node")
hits=[n.attrib for n in nodes if n.attrib.get("content-desc","").startswith(sys.argv[1])]
if not hits:raise SystemExit("Missing visible action: "+sys.argv[1])
box=list(map(int,re.findall(r"\d+",hits[0]["bounds"])))
assert len(box)==4 and box[2]>box[0] and box[3]>box[1],box
print((box[0]+box[2])//2,(box[1]+box[3])//2)
PY
  )
  adb shell input tap $coords
  sleep 2
}
open_bird_setup() {
  launch_home
  tap_description "Open Fieldwork."
  tap_description "Open Birds."
}
# Normal-size, enlarged-text and extreme-size screen layouts.
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
open_bird_setup
capture rc54-survey-method-360-normal
adb shell input swipe 175 665 175 265 430
sleep 1
capture rc54-survey-method-360-scrolled

adb shell wm size 412x892
adb shell wm density 160
adb shell settings put system font_scale 1.3
open_bird_setup
capture rc54-survey-method-412-font130
adb shell input swipe 195 730 195 285 430
sleep 1
capture rc54-survey-method-412-scrolled

adb shell wm size 320x720
adb shell wm density 160
adb shell settings put system font_scale 2.0
open_bird_setup
capture rc54-survey-method-320-font200
for i in 1 2 3; do
  adb shell input swipe 155 600 155 245 430
  sleep 1
  capture "rc54-survey-method-320-scrolled-$i"
done

python3 - <<'PY'
from pathlib import Path
from xml.etree import ElementTree as ET
for size,files in {
    "360":["rc54-survey-method-360-normal","rc54-survey-method-360-scrolled"],
    "412":["rc54-survey-method-412-font130","rc54-survey-method-412-scrolled"],
    "320":["rc54-survey-method-320-font200"]+
           ["rc54-survey-method-320-scrolled-"+str(i) for i in range(1,4)]
}.items():
    nodes=[]
    for f in files:
        nodes.extend(n.attrib for n in ET.parse(Path("emulator-qa")/(f+".xml")).getroot().iter("node"))
    labels=" ".join(n.get("text","")+" "+n.get("content-desc","") for n in nodes)
    for method in ("Point count","Line transect","Area search","Opportunistic"):
        assert method in labels,(size,method)
    assert "Continue to GPS" in labels,(size,"GPS footer")
    print("RC54 native survey methods reachable at",size)
PY

# Screen 1/2 and screen 3 tests use an empty disposable emulator workspace.
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch_home
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc53FieldworkScreenInstrumentedTest,org.phwildlife.survey.Rc54SurveyMethodChooserInstrumentedTest' \
  > emulator-qa/rc54-ui-instrumentation.txt 2>&1 || {
    tail -n 120 emulator-qa/rc54-ui-instrumentation.txt; exit 1;
  }
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc54-research-zip-instrumentation.txt 2>&1 || {
    tail -n 120 emulator-qa/rc54-research-zip-instrumentation.txt; exit 1;
  }
adb logcat -d -t 900 > emulator-qa/rc54-logcat-tail.txt || true
echo "RC54 survey method chooser, RC53 Fieldwork and RC51 research ZIP QA PASS" | tee emulator-qa/rc54-summary.txt
