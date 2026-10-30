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
capture rc57-home-360-normal
adb shell wm size 412x892
adb shell wm density 160
adb shell settings put system font_scale 1.3
launch
capture rc57-home-412-font130
adb shell wm size 320x720
adb shell wm density 160
adb shell settings put system font_scale 2.0
launch
capture rc57-home-320-font200
adb shell input swipe 160 605 160 180 600
sleep 1
capture rc57-home-320-font200-scrolled

python3 - <<'PY'
from xml.etree import ElementTree as ET
import re
from pathlib import Path
for id in ("rc57-home-360-normal","rc57-home-412-font130",
           "rc57-home-320-font200","rc57-home-320-font200-scrolled"):
    path=Path("emulator-qa")/(id+".xml")
    nodes=[n.attrib for n in ET.parse(path).getroot().iter("node")]
    if id!="rc57-home-320-font200-scrolled":
        for label in ("Open Fieldwork","Open Library"):
            assert any(label in n.get("content-desc","") for n in nodes), (id,label)
    if id in ("rc57-home-360-normal","rc57-home-412-font130"):
        for label in ("Open Fieldwork","Open Library","Open Settings"):
            hits=[n for n in nodes if label in n.get("content-desc","")]
            assert hits,(id,label)
            box=list(map(int,re.findall(r"\d+",hits[0].get("bounds",""))))
            assert len(box)==4 and box[3]>box[1] and box[1]<720,(id,label,box)
            print(id,label,box)
    if id=="rc57-home-320-font200-scrolled":
        assert any("Open Settings" in n.get("content-desc","") for n in nodes),"Settings unreachable at 200% font"
# Confirm the 200% Home hero no longer hides the lower Settings card.
# Compare actual UI bounds against the visible ScrollView, not just an accessible node.
from xml.etree import ElementTree as ET
from pathlib import Path
import re
nodes=[n.attrib for n in ET.parse(Path("emulator-qa")/"rc57-home-320-font200.xml").getroot().iter("node")]
rect=lambda n:list(map(int,re.findall(r"\d+",n.get("bounds",""))))
scrollers=[n for n in nodes if n.get("class")=="android.widget.ScrollView"]
assert len(scrollers)==1,("expected one real Home scroll region",len(scrollers))
viewport=rect(scrollers[0])
hero=[n for n in nodes if n.get("content-desc","").startswith("LIKAS. Philippine wildlife")]
settings=[n for n in nodes if n.get("content-desc","").startswith("Open Settings.")]
assert len(hero)==len(settings)==1,("Home hero and Settings missing",len(hero),len(settings))
hero_box=rect(hero[0]);settings_box=rect(settings[0])
assert hero_box[3]-hero_box[1]<240,("200% Home hero still overlarge",hero_box)
assert settings_box[3]<=viewport[3]-8,("Settings clipped by visible scroll region",settings_box,viewport)
print("RC57 Home hero and full Settings card visible at 320dp / 200% text PASS",hero_box,settings_box)
print("RC57 screen 1 accessibility/navigation hierarchy: PASS")
PY
# All 3 screen-layout and exact research ZIP Android tests in the same disposable AVD.
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc57-instrumentation.txt 2>&1 || {
    tail -n 150 emulator-qa/rc57-instrumentation.txt
    exit 1
  }
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC57 Home 360/412/320dp + Android instrumentation PASS" | tee emulator-qa/rc57-summary.txt
