#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
# This AVD is fresh and disposable. Do NOT erase user devices.
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt
sleep 7
adb shell screencap -p /sdcard/rc51-home.png
adb pull /sdcard/rc51-home.png emulator-qa/rc51-home-360dp.png >/dev/null
adb shell uiautomator dump /sdcard/rc51-home.xml > emulator-qa/ui-dump.txt
adb pull /sdcard/rc51-home.xml emulator-qa/rc51-home-360dp.xml >/dev/null
python3 - <<'PY'
import re
from xml.etree import ElementTree
nodes=[x.attrib for x in ElementTree.parse('emulator-qa/rc51-home-360dp.xml').getroot().iter('node')]
for label in ('Open Fieldwork','Open Library','Open Settings'):
    matches=[n for n in nodes if label in n.get('content-desc','')]
    assert matches, 'Missing Home action: '+label
    box=list(map(int,re.findall(r'\d+',matches[0]['bounds'])))
    assert len(box)==4 and box[1]<720 and box[3]>box[1], (label,box)
    print(label,'position',box)
PY
# Run the full research archive fixture on a fresh installation BEFORE any
# recovery test inserts saved fieldwork data. RC50 recovery passed separately.
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc51-zip-instrumentation.txt 2>&1 || {
    tail -n 140 emulator-qa/rc51-zip-instrumentation.txt
    exit 1
  }
adb logcat -d -t 900 > emulator-qa/logcat-tail.txt || true
echo 'RC51 exact research ZIP emulator acceptance PASS' | tee emulator-qa/rc51-zip-summary.txt
