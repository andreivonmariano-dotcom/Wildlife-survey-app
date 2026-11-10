#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
for config in "p360d160f100 360x800 160 1.0" "p360d440f100 990x2200 440 1.0" "p320d440f200 880x1980 440 2.0"; do
  read -r preset pixels dpi font <<< "$config"
  adb shell wm size "$pixels"
  adb shell wm density "$dpi"
  adb shell settings put system font_scale "$font"
  gradle --no-daemon :app:connectedDebugAndroidTest \
    "-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc66SpeciesAccountInstrumentedTest" \
    "-Pandroid.testInstrumentationRunnerArguments.preset=$preset" \
    > "emulator-qa/$preset.txt" 2>&1 || {
      tail -n 120 "emulator-qa/$preset.txt"; exit 1;
    }
  for kind in account-top account-bottom search; do
    name="rc66-$preset-$kind.png"
    adb pull "/sdcard/Pictures/LIKAS_RC66/$name" "emulator-qa/$name" >/dev/null
  done
done
adb shell wm size 360x800
adb shell wm density 160
adb shell settings put system font_scale 1.0
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc63SpeciesChecklistInstrumentedTest,org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  '-Pandroid.testInstrumentationRunnerArguments.preset=p360f100' \
  > emulator-qa/scientific-checklist-and-research-export.txt 2>&1 || {
    tail -n 120 emulator-qa/scientific-checklist-and-research-export.txt;exit 1;
  }
python3 - <<'PY'
from pathlib import Path
from hashlib import sha256
root=Path("emulator-qa")
for tag in ("p360d160f100","p360d440f100","p320d440f200"):
    shots={}
    for kind in ("account-top","account-bottom","search"):
        path=root/f"rc66-{tag}-{kind}.png"
        raw=path.read_bytes()
        assert raw[:8].hex()=="89504e470d0a1a0a" and len(raw)>5000,path
        shots[kind]=sha256(raw).digest()
    assert shots["account-top"]!=shots["account-bottom"],tag
print("RC66 account screenshots, search and scroll: PASS")
PY
adb logcat -d -t 900 > emulator-qa/logcat-tail.txt || true
echo "RC66 source account emulator review and prior scientific ZIP tests PASS" \
  | tee emulator-qa/rc66-summary.txt
