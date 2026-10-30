#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
capture() {
  local name="$1"
  adb shell screencap -p /sdcard/likas-screen.png
  adb pull /sdcard/likas-screen.png "emulator-qa/${name}.png" >/dev/null
  adb shell uiautomator dump /sdcard/likas-hierarchy.xml > "emulator-qa/${name}-dump.txt" 2>&1 || true
  adb pull /sdcard/likas-hierarchy.xml "emulator-qa/${name}.xml" >/dev/null 2>&1 || true
  adb shell dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' > "emulator-qa/${name}-foreground.txt" || true
}
visit() {
  local screen="$1"
  local label="$2"
  echo "Starting $screen" | tee -a emulator-qa/summary.txt
  adb shell am start -n "$PKG/org.phwildlife.survey.$screen" > "emulator-qa/${screen}-start.txt" 2>&1 || true
  sleep 3
  capture "$label"
}
adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt 2>&1 || true
sleep 7
capture "01-after-launch-360dp"
visit WelcomeActivity 02-home-360dp
visit Rc7SpeciesLibraryActivity 03-species-library-360dp
visit HabitatRecordsActivity 04-habitats-360dp
visit HistoryActivity 05-history-360dp
visit DashboardActivity 06-dashboard-360dp
visit LikasLibraryActivity 07-library-360dp
visit MainActivity 08-recorder-entry-360dp
adb shell settings put system font_scale 2.0
adb shell wm size 320x720
visit WelcomeActivity 09-home-320dp-font200
visit Rc7SpeciesLibraryActivity 10-species-library-320dp-font200
visit HistoryActivity 11-history-320dp-font200
adb logcat -d -t 800 > emulator-qa/logcat-tail.txt || true
echo 'Emulator screenshots, hierarchy files and launch logs collected.' | tee -a emulator-qa/summary.txt
