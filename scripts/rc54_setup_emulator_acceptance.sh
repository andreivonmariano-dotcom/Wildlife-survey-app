#!/usr/bin/env bash
set -euo pipefail
mkdir -p emulator-qa
PKG=org.phwildlife.survey.beta08
adb install -r app/build/outputs/apk/debug/app-debug.apk > emulator-qa/install.txt 2>&1
launch_home(){
  adb shell am force-stop "$PKG" >/dev/null || true
  adb shell am start -n "$PKG/org.phwildlife.survey.LikasLaunchActivity" > emulator-qa/launch.txt
  sleep 6
}
capture(){
  local name="$1"
  adb shell screencap -p "/sdcard/$name.png"
  adb pull "/sdcard/$name.png" "emulator-qa/$name.png" >/dev/null
  adb shell uiautomator dump "/sdcard/$name.xml" >/dev/null
  adb pull "/sdcard/$name.xml" "emulator-qa/$name.xml" >/dev/null
}
tap_description(){
  local prefix="$1"
  adb shell uiautomator dump /sdcard/rc54-tap.xml >/dev/null
  adb pull /sdcard/rc54-tap.xml emulator-qa/rc54-tap.xml >/dev/null
  local pos
  pos=$(python3 - "$prefix" <<'PY'
import re,sys
from xml.etree import ElementTree as ET
nodes=[n.attrib for n in ET.parse("emulator-qa/rc54-tap.xml").getroot().iter("node")]
found=[n for n in nodes if n.get("content-desc","").startswith(sys.argv[1])]
assert found,("Cannot find action",sys.argv[1])
a=list(map(int,re.findall(r"\d+",found[0]["bounds"])))
assert len(a)==4 and a[0]<a[2] and a[1]<a[3]
print((a[0]+a[2])//2,(a[1]+a[3])//2)
PY
  )
  adb shell input tap $pos
  sleep 2
}
enter_setup(){
  local group="$1"
  launch_home
  tap_description "Open Fieldwork."
  tap_description "Open $group."
}
# Three actual taxa at default scale, plus Birds at 200% text and a mid-scale view.
adb shell wm density 160
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
for taxon in Birds Herpetofauna Mammals; do
  enter_setup "$taxon"
  capture "rc54-setup-$taxon-360-top"
  adb shell input swipe 170 655 170 190 460
  sleep 1
  capture "rc54-setup-$taxon-360-middle"
  adb shell input swipe 170 655 170 190 460
  sleep 1
  capture "rc54-setup-$taxon-360-bottom"
done
adb shell wm size 412x892
adb shell settings put system font_scale 1.3
enter_setup Birds
capture rc54-setup-Birds-412-font130
adb shell wm size 320x720
adb shell settings put system font_scale 2.0
enter_setup Birds
capture rc54-setup-Birds-320-font200-top
# At 200% text the sticky GPS footer begins near y=635. The visible
# ScrollView ends at y=580, so start swipes well INSIDE its content bounds.
for index in 1 2 3 4 5 6 7 8 9; do
  adb shell input swipe 160 520 160 132 450
  sleep 1
  capture "rc54-setup-Birds-320-font200-scroll$index"
done
python3 - <<'PY'
from xml.etree import ElementTree as ET
from pathlib import Path
for name in ("rc54-setup-Birds-360-top","rc54-setup-Herpetofauna-360-top",
             "rc54-setup-Mammals-360-top","rc54-setup-Birds-412-font130",
             "rc54-setup-Birds-320-font200-top"):
    nodes=[n.attrib for n in ET.parse(Path("emulator-qa")/(name+".xml")).getroot().iter("node")]
    assert any("Continue Birds survey setup to GPS preflight" in n.get("content-desc","")
               or "Continue Herpetofauna survey setup to GPS preflight" in n.get("content-desc","")
               or "Continue Mammals survey setup to GPS preflight" in n.get("content-desc","")
               for n in nodes), ("Pinned GPS action missing",name)
# Confirm that tall-font scrolling really reaches content, rather than
# producing identical top-screen captures after a swipe outside the viewport.
from hashlib import sha256
initial=Path("emulator-qa/rc54-setup-Birds-320-font200-top.png").read_bytes()
later=Path("emulator-qa/rc54-setup-Birds-320-font200-scroll1.png").read_bytes()
assert sha256(initial).digest()!=sha256(later).digest(), "200% setup did not actually scroll"
large_nodes=[n.attrib for name in ["rc54-setup-Birds-320-font200-top"]+
  [f"rc54-setup-Birds-320-font200-scroll{i}" for i in range(1,10)]
  for n in ET.parse(Path("emulator-qa")/(name+".xml")).getroot().iter("node")]
large_words=" ".join(n.get("text","") for n in large_nodes)
for label in ("Survey method","Project and location","Sampling effort","Begin survey",
              "Checklist region","Point count duration (minutes)"):
    assert label in large_words,("200% screen never exposed",label)
for taxon in ("Birds","Herpetofauna","Mammals"):
    names=[f"rc54-setup-{taxon}-360-"+step for step in ("top","middle","bottom")]
    nodes=[n.attrib for name in names for n in ET.parse(
        Path("emulator-qa")/(name+".xml")).getroot().iter("node")]
    words=" ".join(n.get("text","") for n in nodes)
    for label in (taxon+" survey","Survey method","Project and location"):
        assert label in words,(taxon,label)
print("RC54 screenshots: pinned GPS across sizes; all three taxa setup PASS")
PY
adb shell wm size 360x800
adb shell settings put system font_scale 1.0
launch_home
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc40HomeLayoutInstrumentedTest,org.phwildlife.survey.Rc53FieldworkScreenInstrumentedTest,org.phwildlife.survey.Rc54SurveySetupInstrumentedTest' \
  > emulator-qa/rc54-ui-instrumentation.txt 2>&1 || {
    tail -n 145 emulator-qa/rc54-ui-instrumentation.txt;exit 1;
  }
gradle --no-daemon :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=org.phwildlife.survey.Rc51ResearchZipInstrumentedTest' \
  > emulator-qa/rc54-research-instrumentation.txt 2>&1 || {
    tail -n 145 emulator-qa/rc54-research-instrumentation.txt;exit 1;
  }
adb logcat -d -t 1000 > emulator-qa/logcat-tail.txt || true
echo "RC54 setup 3 taxa/3 sizes, pinned GPS, active Resume and research ZIP PASS" | tee emulator-qa/rc54-summary.txt
