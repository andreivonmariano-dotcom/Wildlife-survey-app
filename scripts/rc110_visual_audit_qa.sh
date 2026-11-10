#!/usr/bin/env bash
set -euo pipefail
# RC110 native UI screenshot audit; disposable Android emulator only.
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
SOURCE="/sdcard/Android/data/${PKG}/files/Pictures/RC110_UI_AUDIT"
OUT=rc110-visual-audit-evidence
mkdir -p "$OUT"
execute(){
  local preset="$1" method="$2" profile="${3:-Birds}"
  local name="${preset}-${method}-${profile}"
  adb shell am instrument -w -r -e preset "$preset" -e profile "$profile" \
      -e class "org.phwildlife.survey.Rc110VisualAuditInstrumentedTest#${method}" \
      "$RUNNER" | tee "$OUT/${name}.txt"
  grep -Fq "OK (1 test)" "$OUT/${name}.txt"
}
for config in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset screen density font <<< "$config"
  adb shell wm size "$screen"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell pm clear "$PKG"
  execute "$preset" aPortalAndSurveySetupVisualAudit
  execute "$preset" bSettingsVisualAudit
  mkdir -p "$OUT/$preset"
  adb pull "$SOURCE/." "$OUT/$preset/" >/dev/null
  for profile in Birds Herpetofauna Mammals; do
    adb shell pm clear "$PKG"
    execute "$preset" cRecorderVisualAudit "$profile"
    adb pull "$SOURCE/." "$OUT/$preset/" >/dev/null
  done
done
python3 - <<'PY'
from pathlib import Path
root=Path("rc110-visual-audit-evidence")
for preset in ("p360f100","p320f200"):
    files=list((root/preset).glob("*.png"))
    assert len(files)>=27,(preset,len(files))
    for f in files:
        data=f.read_bytes()
        assert len(data)>2000 and data[:8]==bytes((137,80,78,71,13,10,26,10)),f
    print(f"{preset}: {len(files)} native screenshots with valid PNG headers")
PY
echo "RC110 actual native screen captures completed; visual inspection follows." | tee "$OUT/summary.txt"
