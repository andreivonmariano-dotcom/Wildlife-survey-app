#!/usr/bin/env bash
set -euo pipefail
# Run the current RC93 schema/backup/device gates before PHLibrary visual acceptance.
bash scripts/rc92_unverified_device_qa.sh
PACKAGE=org.phwildlife.survey.beta08
RUNNER=org.phwildlife.survey.beta08.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p rc94-phlibrary-evidence
adb shell pm clear "$PACKAGE"
for scale in 160 200; do
  if [[ "$scale" == "160" ]]; then
    adb shell wm size 1080x2280
    adb shell wm density 480
    adb shell settings put system font_scale 1.6
  else
    adb shell wm size 960x2160
    adb shell wm density 480
    adb shell settings put system font_scale 2.0
  fi
  adb shell am instrument -w -r -e class \
    org.phwildlife.survey.Rc94PhLibraryInstrumentedTest "$RUNNER" \
    | tee "rc94-phlibrary-evidence/android_${scale}.txt"
  grep -Fq 'OK (2 tests)' "rc94-phlibrary-evidence/android_${scale}.txt"
  for screen in hub guides; do
    file="rc94_phlibrary_${screen}.png"
    adb pull "/sdcard/Android/data/${PACKAGE}/files/${file}" \
      "rc94-phlibrary-evidence/${screen}_${scale}.png"
  done
done
python3 - <<'PY'
from pathlib import Path
import struct,hashlib
folder=Path("rc94-phlibrary-evidence")
images=list(folder.glob("*.png"))
assert len(images)==4,images
dims={"160":(1080,2280),"200":(960,2160)}
for p in images:
    data=p.read_bytes()
    assert len(data)>12000 and data[:8]==bytes.fromhex("89504e470d0a1a0a"),p
    assert struct.unpack(">II",data[16:24])==dims[p.stem.split("_")[-1]],p
assert len({hashlib.sha256(p.read_bytes()).hexdigest() for p in images})==4
print("Verified 4 unique PHLibrary Android screen captures")
PY
adb logcat -d -s AndroidRuntime:E > rc94-phlibrary-evidence/android_runtime_errors.txt
test ! -s rc94-phlibrary-evidence/android_runtime_errors.txt
