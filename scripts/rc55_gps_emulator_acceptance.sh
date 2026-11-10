#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
mkdir -p emulator-qa
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
# Ensure an explicit GPS-disabled state, never synthesize a location.
adb shell cmd location set-location-enabled false \
  || adb shell settings put secure location_mode 0
tap_description(){
  local prefix="$1"
  adb shell uiautomator dump /sdcard/rc55-tap.xml >/dev/null
  adb pull /sdcard/rc55-tap.xml emulator-qa/rc55-tap.xml >/dev/null
  local pos
  pos=$(python3 - "$prefix" <<'PY'
import re,sys
from xml.etree import ElementTree as ET
nodes=[n.attrib for n in ET.parse("emulator-qa/rc55-tap.xml").getroot().iter("node")]
hits=[x for x in nodes if x.get("content-desc","").startswith(sys.argv[1])]
assert hits,(sys.argv[1],"missing action")
bounds=list(map(int,re.findall(r"\d+",hits[0]["bounds"])))
assert len(bounds)==4 and bounds[2]>bounds[0] and bounds[3]>bounds[1]
print((bounds[0]+bounds[2])//2,(bounds[1]+bounds[3])//2)
PY
  )
  adb shell input tap $pos
  sleep 2
}
launch_survey(){
  # The GPS activity is deliberately non-exported; enter via actual user UI.
  adb shell am force-stop "$PKG" >/dev/null || true
  adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt
  sleep 5
  tap_description "Open Fieldwork."
  tap_description "Open Birds."
  tap_description "Continue Birds survey setup to GPS preflight"
  sleep 2
}
capture(){
  local name="$1"
  adb shell screencap -p "/sdcard/$name.png"
  adb pull "/sdcard/$name.png" "emulator-qa/$name.png" >/dev/null
  adb shell uiautomator dump "/sdcard/$name.xml" >/dev/null
  adb pull "/sdcard/$name.xml" "emulator-qa/$name.xml" >/dev/null
}
adb shell wm density 160
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch_survey
capture rc55-survey-gps-disabled-360
launch_survey
adb shell wm size 412x892
adb shell settings put system font_scale 1.3
launch_survey
capture rc55-survey-gps-disabled-412-font130
adb shell wm size 320x720
adb shell settings put system font_scale 2.0
launch_survey
capture rc55-survey-gps-disabled-320-font200
# At 200% text the sticky footer starts near y480. Swipe INSIDE the ScrollView.
adb shell input swipe 155 405 155 122 450
sleep 1
capture rc55-survey-gps-320-font200-scrolled1
adb shell input swipe 155 405 155 122 450
sleep 1
capture rc55-survey-gps-320-font200-scrolled2
python3 - <<'PY'
from xml.etree import ElementTree as ET
from pathlib import Path
for name in ("rc55-survey-gps-disabled-360",
             "rc55-survey-gps-disabled-412-font130",
             "rc55-survey-gps-disabled-320-font200"):
    nodes=[n.attrib for n in ET.parse(Path("emulator-qa")/(name+".xml")).getroot().iter("node")]
    desc=[n.get("content-desc","") for n in nodes]
    text=" ".join(n.get("text","") for n in nodes)
    assert "Device GPS is switched off" in text,(name,"wrong status",text[:220])
    if "station" in name:
        assert any("Return to the active survey without starting a station" in d for d in desc),name
        assert "Start without GPS" in text,name
    else:
        assert any("Start survey with its starting GPS position missing" in d for d in desc),name
        assert any("Cancel GPS acquisition and return to survey setup" in d for d in desc),name
    if "320-font200" not in name:
        assert any(d.startswith("Open Android device location settings") for d in desc),name
    print(name,"navigation and disabled-GPS status PASS")
# On narrow/large-font phones, advanced controls are below the fold by design.
# Prove they are reachable with actual content-area scrolling.
from hashlib import sha256
p=Path("emulator-qa")
first=(p/"rc55-survey-gps-disabled-320-font200.png").read_bytes()
scrolled=(p/"rc55-survey-gps-320-font200-scrolled1.png").read_bytes()
assert sha256(first).digest()!=sha256(scrolled).digest(),"200% GPS page did not scroll"
pages=["rc55-survey-gps-disabled-320-font200",
       "rc55-survey-gps-320-font200-scrolled1",
       "rc55-survey-gps-320-font200-scrolled2"]
d=[n.attrib.get("content-desc","") for name in pages
   for n in ET.parse(p/(name+".xml")).getroot().iter("node")]
assert any(t.startswith("Open Android device location settings") for t in d),    "Location settings unreachable at 200% text"
print("RC55 320dp/200% scrolling and Location settings PASS")
PY
# This is a fresh disposable CI AVD, NEVER an owner's phone.
# Isolate screenshot navigation from instrumentation fixtures. In particular,
# reenabling GPS while preflight is visible could legitimately auto-start a
# synthetic survey and invalidate zero-record assertions.
adb shell am force-stop "$PKG" >/dev/null || true
adb uninstall "$PKG" >/dev/null
adb install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
# Keep location disabled to prevent emulator-generated starting fixes in tests.
adb shell cmd location set-location-enabled false \
  || adb shell settings put secure location_mode 0
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc53FieldworkScreenInstrumentedTest,org.phwildlife.survey.Rc54SurveySetupInstrumentedTest,org.phwildlife.survey.Rc55GpsPreflightInstrumentedTest' \
  > emulator-qa/rc55-gps-ui-instrumentation.txt 2>&1 || {
    tail -n 125 emulator-qa/rc55-gps-ui-instrumentation.txt; exit 1;
  }
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc55-research-instrumentation.txt 2>&1 || {
    tail -n 125 emulator-qa/rc55-research-instrumentation.txt; exit 1;
  }
adb logcat -d -t 900 > emulator-qa/logcat-tail.txt || true
echo "RC55 survey/station GPS UI + disabled provider and research ZIP: PASS" | tee emulator-qa/rc55-summary.txt
