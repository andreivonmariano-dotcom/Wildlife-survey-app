#!/usr/bin/env bash
set -euo pipefail
# Safe read-only metadata capture. Never performs install/pm clear/uninstall/export.
PKG="org.phwildlife.survey.beta08"
DEST="${1:-LIKAS-RC27-READONLY-DEVICE-QA}"
mkdir -p "$DEST"
if ! command -v adb >/dev/null 2>&1; then
  echo "Android platform-tools adb is required" >&2
  exit 2
fi
if [[ "$(adb devices | awk 'NR>1 && $2=="device" {n++} END {print n+0}')" != "1" ]]; then
  echo "Connect exactly one authorized Android debugging device" >&2
  exit 3
fi
adb shell getprop ro.product.manufacturer | tr -d '\r' > "$DEST/manufacturer.txt"
adb shell getprop ro.product.model | tr -d '\r' > "$DEST/model.txt"
adb shell getprop ro.build.version.release | tr -d '\r' > "$DEST/android-release.txt"
adb shell getprop ro.build.version.sdk | tr -d '\r' > "$DEST/android-sdk.txt"
adb shell dumpsys package "$PKG" | grep -E 'versionCode=|versionName=|firstInstallTime=|lastUpdateTime=' > "$DEST/likas-package-version.txt" || true
adb shell wm size > "$DEST/display-size.txt"
adb shell wm density > "$DEST/display-density.txt"
adb shell settings get system font_scale | tr -d '\r' > "$DEST/font-scale.txt"
printf '%s\n' "Read-only metadata collected; no LIKAS records, GPS, photos, WAVs, or contacts were accessed." > "$DEST/README.txt"
echo "Saved local non-destructive device metadata under $DEST"
