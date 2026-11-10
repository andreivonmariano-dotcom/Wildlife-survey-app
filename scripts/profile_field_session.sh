#!/usr/bin/env bash
# Read-only Android field-session profiler. Does not collect coordinates or audio.
set -euo pipefail
PHASE="${1:-}"
DIR="${2:-likas-device-profile}"
APP="org.phwildlife.survey.beta08"
if [[ "$PHASE" != "start" && "$PHASE" != "finish" ]]; then
  echo "Usage: bash scripts/profile_field_session.sh start|finish [output_directory]" >&2
  exit 2
fi
command -v adb >/dev/null || { echo "ADB is required" >&2; exit 2; }
adb get-state >/dev/null
mkdir -p "$DIR"
NAME="$DIR/$PHASE"
date -u +"%Y-%m-%dT%H:%M:%SZ" > "$NAME.utc.txt"
adb shell dumpsys battery | grep -E "level:|status:|temperature:|voltage:" > "$NAME.battery.txt"
adb shell dumpsys meminfo "$APP" > "$NAME.memory.txt"
adb shell dumpsys gfxinfo "$APP" framestats > "$NAME.frames.txt"
adb shell dumpsys package "$APP" | grep -m 1 "versionName=" > "$NAME.version.txt" || true
echo "Stored $PHASE battery, memory, frame and version samples in $DIR."
echo "No coordinates, survey contents or media were collected."
