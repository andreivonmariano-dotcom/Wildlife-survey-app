#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb shell wm density 160
adb shell settings put secure show_ime_with_hard_keyboard 1 || true
adb shell ime list -s > emulator-qa/available-imes.txt 2>&1 || true
adb shell settings get secure default_input_method > emulator-qa/default-ime.txt 2>&1 || true
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install-current.txt 2>&1
capture(){
  local filename="$1"
  adb pull "/sdcard/Pictures/LIKAS_RC63/$filename" "emulator-qa/$filename" >/dev/null
  python3 - "$filename" <<'PY'
from pathlib import Path
import sys
data=(Path("emulator-qa")/sys.argv[1]).read_bytes()
assert len(data)>5000 and data[:8]==b"\x89PNG\r\n\x1a\n",sys.argv[1]
PY
}
for cfg in "p360f100 360x800 1.0" "p412f130 412x892 1.3" "p320f200 320x720 2.0"; do
  read -r preset size font <<< "$cfg"
  adb shell wm size "$size"
  adb shell settings put system font_scale "$font"
  gradle --no-daemon :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc63SpeciesChecklistInstrumentedTest" \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$preset" \
    > "emulator-qa/rc63-$preset-checklist.txt" 2>&1 || {
      adb pull "/sdcard/Pictures/LIKAS_RC63/rc63-$preset-ime-attempt.png" \
        "emulator-qa/rc63-$preset-ime-attempt.png" >/dev/null 2>&1 || true
      adb shell dumpsys input_method > "emulator-qa/rc63-$preset-ime-on-failure.txt" || true
      tail -n 130 "emulator-qa/rc63-$preset-checklist.txt"; exit 1;
    }
  for kind in checklist-top checklist-scrolled ime-attempt keyboard-open; do
    capture "rc63-$preset-$kind.png"
  done
done
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
adb shell pm list packages | grep -i 'talkback\|accessibility' > emulator-qa/accessibility-service-packages.txt || true
adb shell dumpsys accessibility > emulator-qa/accessibility-service-state.txt || true
adb shell uiautomator dump /sdcard/window.xml > emulator-qa/uiautomator-capture.txt 2>&1 || true
adb pull /sdcard/window.xml emulator-qa/window-accessibility-hierarchy.xml > /dev/null 2>&1 || true
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc62SpeciesDiscoverInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc63-reference-and-export.txt 2>&1 || {
    tail -n 130 emulator-qa/rc63-reference-and-export.txt; exit 1;
  }
# In-place update rehearsal on the same disposable Android device:
# build the prior code122 APK using identical, pinned debug-signing identity.
git fetch --depth=1 origin feature/likas-rc62-species-discover-visual
git worktree add --detach /tmp/likas-rc62-before cdc31b75933b21f25ee501f0cad3a34a45047c6b
mkdir -p /tmp/likas-rc62-before/.signing /tmp/likas-rc62-before/.taxonomy-cache
cp .signing/wildlife-beta08.keystore /tmp/likas-rc62-before/.signing/
cp .taxonomy-cache/mdd-v2.5.csv /tmp/likas-rc62-before/.taxonomy-cache/
(
 cd /tmp/likas-rc62-before
 python3 scripts/prepare_mdd_mammals.py
 python3 scripts/prepare_offline_photos.py
 python3 scripts/audit_inaturalist_photos.py --install-approved
 gradle --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest
) > emulator-qa/rc62-legacy-build.txt 2>&1 || {
  tail -n 100 emulator-qa/rc62-legacy-build.txt; exit 1;
}
adb uninstall "$PKG" >/dev/null 2>&1 || true
adb uninstall "$PKG.test" >/dev/null || true
adb install /tmp/likas-rc62-before/app/build/outputs/apk/debug/app-debug.apk
adb install /tmp/likas-rc62-before/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e class org.phwildlife.survey.Rc51ResearchZipInstrumentedTest \
  "$PKG.test/androidx.test.runner.AndroidJUnitRunner" \
  > emulator-qa/rc62-preupgrade-seed.txt 2>&1 || {
  cat emulator-qa/rc62-preupgrade-seed.txt;exit 1;
}
grep -q 'OK (1 test)' emulator-qa/rc62-preupgrade-seed.txt || {
  cat emulator-qa/rc62-preupgrade-seed.txt;exit 1;
}
adb install -r app/build/outputs/apk/debug/app-debug.apk \
  > emulator-qa/rc62-to-rc63-in-place-install.txt 2>&1
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e class org.phwildlife.survey.Rc63UpgradePreservationInstrumentedTest \
  "$PKG.test/androidx.test.runner.AndroidJUnitRunner" \
  > emulator-qa/rc63-postupgrade-preservation.txt 2>&1 || {
  cat emulator-qa/rc63-postupgrade-preservation.txt;exit 1;
}
grep -q 'OK (1 test)' emulator-qa/rc63-postupgrade-preservation.txt || {
  cat emulator-qa/rc63-postupgrade-preservation.txt;exit 1;
}
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
root=Path("emulator-qa")
for cfg in ("p360f100","p412f130","p320f200"):
  a=(root/f"rc63-{cfg}-checklist-top.png").read_bytes()
  b=(root/f"rc63-{cfg}-checklist-scrolled.png").read_bytes()
  assert sha256(a).digest()!=sha256(b).digest(),(cfg,"page failed to scroll")
print("RC63 actual full-checklist, keyboard and historical upgrade: PASS")
PY
adb logcat -d -t 1100 > emulator-qa/logcat-tail.txt || true
echo "RC63 checklist/IME/accessibility evidence and code122-to123 historical survival PASS" \
  | tee emulator-qa/rc63-summary.txt
