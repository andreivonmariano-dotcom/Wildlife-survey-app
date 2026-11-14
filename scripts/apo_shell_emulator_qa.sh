#!/usr/bin/env bash
set -euo pipefail
PKG=org.phwildlife.survey.beta08
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"
SOURCE="/sdcard/Android/data/${PKG}/files/Pictures/APO_SHELL_REVIEW"
OUT=apo-shell-review
mkdir -p "$OUT"
for configuration in "p360f100 360x800 1.0" "p320f200 320x720 2.0"; do
    read -r preset screen font <<< "$configuration"
    adb shell wm size "$screen"
    adb shell wm density 160
    adb shell settings put system font_scale "$font"
    adb shell pm clear "$PKG"
    adb shell am instrument -w -r -e preset "$preset" \
        -e class org.phwildlife.survey.ApoShellNavigationInstrumentedTest \
        "$RUNNER" | tee "$OUT/$preset.txt"
    grep -Fq 'OK (4 tests)' "$OUT/$preset.txt"
    adb shell am instrument -w -r -e preset "$preset" \
        -e class org.phwildlife.survey.ApoOfflineMapInstrumentedTest \
        "$RUNNER" | tee "$OUT/$preset-map.txt"
    grep -Fq 'OK (1 test)' "$OUT/$preset-map.txt"
    adb shell am instrument -w -r -e preset "$preset" \
        -e class org.phwildlife.survey.ApoAtlasConnectionsInstrumentedTest \
        "$RUNNER" | tee "$OUT/$preset-taxonomy.txt"
    grep -Fq 'OK (2 tests)' "$OUT/$preset-taxonomy.txt"
    adb shell am instrument -w -r -e preset "$preset" \
        -e class org.phwildlife.survey.ApoResearchBookmarksInstrumentedTest \
        "$RUNNER" | tee "$OUT/$preset-references.txt"
    grep -Fq 'OK (1 test)' "$OUT/$preset-references.txt"
    mkdir -p "$OUT/$preset"
    adb pull "$SOURCE/." "$OUT/$preset/" >/dev/null
    python3 - "$OUT/$preset" <<'PY'
from pathlib import Path
import sys
folder=Path(sys.argv[1])
files=list(folder.glob("*.png"))
assert len(files)>=12,(folder,len(files))
for file in files:
    data=file.read_bytes()
    assert len(data)>2500 and data[:8]==bytes((137,80,78,71,13,10,26,10)),file
print(f"{folder.name}: {len(files)} native navigation screenshots checked")
PY
done
echo "APO shell navigation and large-font emulator checks passed" > "$OUT/summary.txt"
