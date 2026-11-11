#!/usr/bin/env bash
set -euo pipefail
# Actual Android emulator evidence for owner review of Living Atlas RC113.
PKG=org.phwildlife.survey.beta08
RUNNER="$PKG.test/androidx.test.runner.AndroidJUnitRunner"
SOURCE="/sdcard/Android/data/$PKG/files/Pictures/LIVING_ATLAS_REVIEW"
OUT="living-atlas-emulator-evidence"
mkdir -p "$OUT"
for row in "p360f100 360x800 160 1.0" "p320f200 320x720 160 2.0"; do
  read -r preset size density font <<< "$row"
  adb shell wm size "$size"
  adb shell wm density "$density"
  adb shell settings put system font_scale "$font"
  adb shell am force-stop "$PKG"
  adb shell am instrument -w -r -e preset "$preset" -e class \
    "org.phwildlife.survey.LivingAtlasExplorerInstrumentedTest#aExploreSpeciesNavigateAndCapture" \
    "$RUNNER" | tee "$OUT/$preset-test.txt"
  grep -Fq "OK (1 test)" "$OUT/$preset-test.txt"
  mkdir -p "$OUT/$preset"
  adb pull "$SOURCE/." "$OUT/$preset/" >/dev/null
done
python3 - <<'PY'
from pathlib import Path
p=Path("living-atlas-emulator-evidence")
for preset in ("p360f100","p320f200"):
    images=list((p/preset).glob(preset+"-*.png"))
    assert len(images)==4,(preset,[x.name for x in images])
    for f in images:
        b=f.read_bytes()
        assert len(b)>2500 and b[:8]==bytes((137,80,78,71,13,10,26,10)),f
    print(f"{preset}: {len(images)} native screen captures, all valid PNG")
PY
echo "Living Atlas RC113 view and back-navigation checks passed at 360px standard and 320px 200% text." | tee "$OUT/summary.txt"
