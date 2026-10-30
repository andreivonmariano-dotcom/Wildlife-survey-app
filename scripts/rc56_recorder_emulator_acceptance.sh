#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
mkdir -p emulator-qa
# CI uses only a disposable Android 14 virtual device. Never touch owner's records.
prepare_clean(){
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell pm clear "$PKG" >/dev/null
  adb shell pm grant "$PKG" android.permission.ACCESS_COARSE_LOCATION
  adb shell pm grant "$PKG" android.permission.ACCESS_FINE_LOCATION
  adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
  adb shell cmd location set-location-enabled false >/dev/null \
    || adb shell settings put secure location_mode 0
}
capture_from_app(){
  local name="$1"
  adb exec-out run-as "$PKG" cat "files/$name" > "emulator-qa/$name"
  test -s "emulator-qa/$name"
}
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
adb shell wm density 160

for config in "Birds 360x800 1.0" "Mammals 412x892 1.3" "Herpetofauna 320x720 2.0"; do
  read -r group size scale <<< "$config"
  adb shell wm size "$size"
  adb shell settings put system font_scale "$scale"
  prepare_clean
  # Instrumentation launches the live recorder with an explicit *missing start fix*.
  # No fabricated GNSS fix, detection or observation is inserted.
  gradle --no-daemon :app:connectedDebugAndroidTest \
    '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc56RecorderUiInstrumentedTest' \
    "-Pandroid.testInstrumentationRunnerArguments.profile=$group" \
    > "emulator-qa/rc56-recorder-$group-instrumentation.txt" 2>&1 || {
      tail -n 130 "emulator-qa/rc56-recorder-$group-instrumentation.txt"; exit 1;
    }
  capture_from_app "rc56-recorder-$group-top.png"
  capture_from_app "rc56-recorder-$group-scrolled.png"
done

python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
p=Path("emulator-qa")
for group in ("Birds","Mammals","Herpetofauna"):
    a=(p/f"rc56-recorder-{group}-top.png").read_bytes()
    b=(p/f"rc56-recorder-{group}-scrolled.png").read_bytes()
    assert sha256(a).digest()!=sha256(b).digest(),(group,"recorder form did not scroll")
    print(group,"native recorder screen top and scroll differ: PASS")
PY

# Independent app state for existing regression tests. They require clean surveys.
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
prepare_clean
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc53FieldworkScreenInstrumentedTest,org.phwildlife.survey.Rc54SurveySetupInstrumentedTest,org.phwildlife.survey.Rc55GpsPreflightInstrumentedTest' \
  > emulator-qa/rc56-prior-ui-instrumentation.txt 2>&1 || {
    tail -n 140 emulator-qa/rc56-prior-ui-instrumentation.txt;exit 1;
  }
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc56-science-zip-instrumentation.txt 2>&1 || {
    tail -n 140 emulator-qa/rc56-science-zip-instrumentation.txt;exit 1;
  }
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC56 real Birds/Mammals/Herpetofauna recorder screens, pinned End, prior UI and ZIP PASS" \
  | tee emulator-qa/rc56-summary.txt
