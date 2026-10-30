#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
mkdir -p emulator-qa
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION
# Ensure an explicit GPS-disabled state, never synthesize a location.
adb shell cmd location set-location-enabled false \
  || adb shell settings put secure location_mode 0
launch(){
  local station="$1"
  adb shell am force-stop "$PKG" >/dev/null || true
  if [[ "$station" == "station" ]]; then
    adb shell am start -n "$PKG/org.phwildlife.survey.SurveyGpsActivity" \
      --ez station_gps_mode true > emulator-qa/launch.txt
  else
    adb shell am start -n "$PKG/org.phwildlife.survey.SurveyGpsActivity" \
      --ez station_gps_mode false > emulator-qa/launch.txt
  fi
  sleep 3
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
launch survey
capture rc55-survey-gps-disabled-360
launch station
capture rc55-station-gps-disabled-360
adb shell wm size 412x892
adb shell settings put system font_scale 1.3
launch survey
capture rc55-survey-gps-disabled-412-font130
adb shell wm size 320x720
adb shell settings put system font_scale 2.0
launch survey
capture rc55-survey-gps-disabled-320-font200
adb shell input swipe 155 475 155 150 400
sleep 1
capture rc55-survey-gps-320-font200-scrolled
launch station
capture rc55-station-gps-disabled-320-font200
python3 - <<'PY'
from xml.etree import ElementTree as ET
from pathlib import Path
for name in ("rc55-survey-gps-disabled-360",
             "rc55-survey-gps-disabled-412-font130",
             "rc55-survey-gps-disabled-320-font200",
             "rc55-station-gps-disabled-360",
             "rc55-station-gps-disabled-320-font200"):
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
    assert any(d.startswith("Open Android device location settings") for d in desc),name
    print(name,"navigation and disabled-GPS status PASS")
PY
# Turn the device provider back on before the saved survey + recorder tests.
adb shell cmd location set-location-enabled true \
  || adb shell settings put secure location_mode 3
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
