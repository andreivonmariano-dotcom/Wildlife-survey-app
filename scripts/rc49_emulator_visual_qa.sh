#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell wm size 320x720
adb shell wm density 160
adb shell settings put system font_scale 2.0
capture() {
  local name="$1"
  adb shell screencap -p /sdcard/likas-screen.png
  adb pull /sdcard/likas-screen.png "emulator-qa/${name}.png" >/dev/null
  adb shell uiautomator dump /sdcard/likas-hierarchy.xml > "emulator-qa/${name}-dump.txt" 2>&1 || true
  adb pull /sdcard/likas-hierarchy.xml "emulator-qa/${name}.xml" >/dev/null 2>&1 || true
  adb shell dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' > "emulator-qa/${name}-foreground.txt" || true
}
tap_match() {
  local name="$1"
  local attempt=0
  while [ "$attempt" -lt 5 ]; do
    adb shell uiautomator dump /sdcard/likas-current.xml >/dev/null 2>&1 || true
    adb pull /sdcard/likas-current.xml emulator-qa/live.xml >/dev/null 2>&1 || true
    local xy
    xy=$(python3 - "$name" <<'PY'
import re,sys,xml.etree.ElementTree as ET
try:
 root=ET.parse('emulator-qa/live.xml').getroot()
 term=sys.argv[1].lower()
 hits=[]
 for n in root.iter('node'):
  a=n.attrib
  value=(a.get('text','')+' '+a.get('content-desc','')).lower()
  if term in value:
   b=re.findall(r'\d+',a.get('bounds',''))
   if len(b)==4:
    x1,y1,x2,y2=map(int,b)
    hits.append((0 if a.get('clickable')=='true' else 1,(x1+x2)//2,(y1+y2)//2,value))
 if hits:
  _,x,y,_=sorted(hits)[0]
  print(x,y)
except Exception:
 pass
PY
)
    if [ -n "$xy" ]; then
      echo "tap $name at $xy" >> emulator-qa/navigation.txt
      adb shell input tap $xy
      sleep 3
      return 0
    fi
    adb shell input swipe 140 625 140 205 400
    sleep 2
    attempt=$((attempt+1))
  done
  echo "NOT FOUND: $name" >> emulator-qa/navigation.txt
  return 1
}
home() {
  adb shell am force-stop "$PKG" || true
  adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" >> emulator-qa/launch.txt 2>&1
  sleep 8
}
home
capture 01-home-320-font200
tap_match "Open Fieldwork" || true
capture 02-fieldwork-320-font200
tap_match "History" || true
capture 03-history-from-fieldwork
home
tap_match "Open Fieldwork" || true
tap_match "Birds" || true
capture 04-bird-setup
home
tap_match "Open Library" || true
capture 05-library-hub
tap_match "Species" || true
capture 06-species-through-library
home
tap_match "Open Library" || true
tap_match "Habitat" || true
capture 07-habitat-through-library
adb logcat -d -t 1000 > emulator-qa/logcat-tail.txt || true
echo 'Navigation through visible app controls completed.' >> emulator-qa/navigation.txt
