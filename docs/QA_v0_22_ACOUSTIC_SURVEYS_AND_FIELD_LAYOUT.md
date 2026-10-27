# LIKAS v0.22: field forms, acoustic surveys, persistent calls, species and history

Date: 2026-10-27
Base: `feature/v0.21-field-layout-refresh`

## Implemented now

**Birds:** Distance method is visible at a glance; first-detection position and movement remain available in one expandable block. The full offline picker retains common/scientific names, origin data, and research sources under Details. The recorder now opens a dedicated native acoustic recording screen. Bird sound WAV files and manual identification labels can be reviewed after a survey ends.

**Herpetofauna:** Microhabitat, amphibian call-index, life stage and listening noise appear in the front of the field form (call index/noise only for applicable amphibian conditions). Temperature, humidity, substrate, height and weather remain available under the environmental details control. A native `amphibian_calls` station survey continues to record standardized timed listening effort and observations. `amphibian_calls` is no longer marked basic log because it now has timed visits, call index, environmental evidence and native audio review.

**Mammals:** Evidence category and equipment/trap ID appear near the top of the form. Less common height fields are under expandable measurements. Other source fields are unchanged.

**Species picker:** Smaller result cards with scientific name, family, regional evidence and a clearly labeled screening index. Removed the probability-looking horizontal score bar. Conservation/source records remain in Details. The score caution stays accessible from the geographic checklist context line.

**History:** Search by profile, method, date or station. Cleaner survey and observation cards, with outcome badges. Records keep original timestamps, audit edits and cancellation/incomplete reasons. Each survey detail can open its saved acoustic evidence.

## Dedicated acoustic recording / review

- Android microphone permission is requested at the moment the observer starts recording, not at app launch.
- Native 44,100 Hz mono, signed 16-bit lossless WAV recording with a 10-minute limit per clip. Approximately 5.29 MB per minute of PCM audio, excluding file header and filesystem overhead.
- A live, 64-band logarithmic spectrogram shows frequency content during recording. Previews of archived recordings are derived again from the WAV file, and touch-to-seek is available during playback.
- A call annotation stores the exact clip ID, start and end offsets in milliseconds, entered species/call label, notes and review timestamp. Reviewers must label ambiguous or unidentified sounds manually. Nothing attempts automatic species classification.
- The dedicated **Call recordings & spectrograms** entry in Home tools lists recordings from past surveys, including Complete, Incomplete and Not Counted. The history detail for each survey links to its clips. Recording is disabled for completed surveys, playback and annotations remain accessible.
- Audio files are in the private `files/acoustic/` folder, not temporary form state, and linked by `surveys.id` in the additive SQLite v23 schema. The database records timestamp, station, method, taxon group, sample rate, duration and format.
- The survey ZIP export contains original files under `audio/*.wav`, `acoustic_clips.csv` and `acoustic_labels.csv`. Missing referenced WAV files cause an export error rather than a silently incomplete archive.
- Recording stops and finishes the WAV file on app backgrounding. An actively recording view must remain foreground. There is no long-running microphone foreground service in this beta.

## Methods still labeled basic log

The request for comprehensive protocols extends beyond adding sound analysis. These methods still require their own evidence and effort acceptance conditions. They **remain flagged** rather than being portrayed as complete standardized protocols.

| Method | Already captured | Remaining research protocol work |
| --- | --- | --- |
| Bird area search | area, time, detections, effort | search routes, repeat detection controls, effort comparability protocol |
| Herp cover-board search | station, equipment counts, duration | permanent board IDs, installation age, per-board inspection outcome |
| Herp drift fence/pitfall | effort hours, trap nights, device count | fence length, bucket locations, open/close times, capture and release audit |
| Mammal track/sign | evidence class, search time, distance or area | independent sign encounter IDs, sign age, substrate/weather bias |
| Mammal spotlight | timed searches, distance and station/location | beam setup, repeat passes, visibility and speed logging |
| Mammal camera trap | camera days, device ID, downtime notes | deployment log, uptime events, explicit independent detection events |
| Mammal bat acoustic | acoustic recorder-hours and weather | validated ultrasonic hardware, frequency range, detector calibration and call sequence metadata |
| Mammal live trapping | trap nights, captures/signs | individual capture IDs, trap checks, marked-animal/release history |

A phone microphone sampling at 44.1 kHz has a 22.05 kHz Nyquist limit and must **not** be treated as a calibrated ultrasonic bat detector.

## Testing

- `SpectrogramMathTest` checks silence, a 2 kHz tone and invalid frame bounds.
- `test_acoustic_field.py` guards additive schema, WAV archival/export, time-coded labels, Home/History links and visible taxon quick fields.
- All existing survey/route/GPX, GPS skip, status, taxonomy and export tests remain intact and run in GitHub Actions.
- Physical phone testing still needed: microphone permission and recovery, calling in wind and rain, 5- to 10-minute clips, actual FFT rendering, 44.1 kHz device support, timer wake/sleep, clip playback seeking and archived ZIP restoration.

## Research cautions

Spectrogram colors are a visual aid. They do not establish taxonomic identity. WAV recordings can include phone hardware equalization, automatic gain or noise filtering. Archived files should be exported before uninstalling the application, as Android private files are removed with the app.

No legacy research tables are dropped, no species checklist records are altered, and the app's existing Complete / Incomplete / Not Counted logic and GPS start provenance are preserved.
