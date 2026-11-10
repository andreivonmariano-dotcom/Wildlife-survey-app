#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk >emulator-qa/install.txt 2>&1
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt
sleep 7
adb shell screencap -p /sdcard/likas.png
adb pull /sdcard/likas.png emulator-qa/01-home-360-normal.png >/dev/null
adb shell uiautomator dump /sdcard/likas.xml >emulator-qa/ui-dump.txt
adb pull /sdcard/likas.xml emulator-qa/01-home-360-normal.xml >/dev/null
python3 - <<'PY'
import re
import xml.etree.ElementTree as ET
root=ET.parse('emulator-qa/01-home-360-normal.xml').getroot()
nodes=[n.attrib for n in root.iter('node')]
for label in ('Open Fieldwork','Open Library','Open Settings'):
    candidates=[n for n in nodes if label in n.get('content-desc','')]
    assert candidates,f'Missing Home navigation: {label}'
    bounds=re.findall(r'\d+',candidates[0]['bounds'])
    assert len(bounds)==4,(label,bounds)
    x1,y1,x2,y2=map(int,bounds)
    print(label,'position',bounds)
    assert y1<720 and x2>0 and y2>y1,f'{label} not visible in first 360dp viewport'
PY
adb shell settings put system font_scale 2.0
adb shell wm size 320x720
adb shell am force-stop "$PKG"
adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" >>emulator-qa/launch.txt
sleep 6
adb shell screencap -p /sdcard/likas.png
adb pull /sdcard/likas.png emulator-qa/02-home-320-font200.png >/dev/null
adb shell uiautomator dump /sdcard/likas.xml >emulator-qa/large-font-dump.txt
adb pull /sdcard/likas.xml emulator-qa/02-home-320-font200.xml >/dev/null
# Instrumentation uses a clean disposable AVD and tests the user-data rollback invariant.
adb shell settings put system font_scale 1.0
adb shell wm size 360x800
gradle --no-daemon :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.BackupRoundTripInstrumentedTest' > emulator-qa/instrumentation.txt 2>&1 || {
  tail -n 100 emulator-qa/instrumentation.txt
  exit 1
}
adb logcat -d -t 1000 > emulator-qa/logcat-tail.txt || true
echo "RC50 Android screen and rollback tests completed." | tee emulator-qa/summary.txt
