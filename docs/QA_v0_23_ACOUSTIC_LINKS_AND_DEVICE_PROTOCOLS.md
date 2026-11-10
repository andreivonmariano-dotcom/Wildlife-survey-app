# LIKAS v0.23: call measurement, checklist links and device-based wildlife surveys

Date: 2026-10-27
Base: `feature/v0.22-taxon-forms-species-history`
Implementation branch: `feature/v0.23-acoustic-analysis-and-trap-protocols`

## Acoustic analysis (Q1)

- Saved WAV recordings remain app-private and attached to the original survey.
- Archived spectrograms now have **1×, 2×, 4×, 8× and 16× zoom**, earlier/later time-window controls, and adjustable contrast.
- The display regenerates up to 120 FFT time columns from the **selected region of the source 44.1 kHz WAV**, rather than enlarging a coarse full-length screenshot.
- A spectrogram tap selects playback time and calculates its corresponding **approximate frequency in Hz** on the 85–17,000 Hz logarithmic axis.
- The observer can store or edit that approximate frequency in a time-coded call annotation. `selected_frequency_hz` joins the exported acoustic label CSV.
- Peak/energy coloring is a field-visualization aid, not calibrated sound pressure or an independent identification. The upper frequency limit and telephone microphone response are not suitable for ultrasonic bat recordings.

## Taxonomy and observation links (Q2)

- An existing time-coded label can be long-pressed and linked to a **taxon_key** in the installed Philippine checklist; search by scientific or common name. The original text label is not overwritten.
- The observer can link the call to a saved `field_records.id` **from the same survey and matching taxon group**. Deleted records are excluded. A cross-survey or Bird-versus-Amphibian mismatch is refused.
- These links are **linked_unverified**, not expert identifications, and can be cleared independently.
- Database migration 24 adds `taxon_key`, `observation_id`, `selected_frequency_hz` and `review_status` to the existing `acoustic_labels` table. Older annotations remain intact.
- `acoustic_labels.csv` exports all four new columns. Source/version details remain discoverable through the retained `taxon_key` in the `taxon_catalog`.

## Device surveys (Q3)

**Camera-trap check, drift-fence/pitfall check, and live-trap check** now have a dedicated linked field module. Each records:

- Unique physical device ID, linked survey and station, deployment time (observer-confirmed UTC), notes and optional fence length.
- Repeated inspections with their UTC time, operational/damaged status, observed detections or captures, known device downtime, and condition/observer notes.
- Retrieval time, which is optional for valid repeat monitoring. The app **does not force retrieval** for a completed scheduled check.
- For pitfall and live-trap checks, a separate record for **each reported animal**, taxon or unidentified label, individual mark, observation logging time, disposition, notes and immediate release time where applicable.
- History can reopen device records after survey closure. The Home tools screen lists device surveys from prior sessions.

A requested **Complete** outcome is downgraded to **Incomplete** if the device module lacks registered deployments, any registered device has no check, or the number of pitfall/live-trap capture records is below its reported animal count. Existing station visits, measured effort, and other survey requirements still apply. **Not Counted** retains its existing meaning and audit trail.

Open deployments are allowed at survey closure. A camera or live trap can remain operating for the next planned visit. Its device status and any known gaps remain stored.

The research ZIP adds:
- `trap_deployments.csv`
- `trap_checks.csv`
- `trap_captures.csv`

The version-24 migration creates these three tables without dropping any observation or species data.

## Other methods

`area_search`, `coverboard_search`, `track_sign`, `spotlight`, and `bat_acoustic` retain their earlier **basic log** indicators. Native microphone recording does not imply calibrated ultrasound. Larger multi-device operational histories and import of external camera photos remain future extensions. Device counts and checks do not establish species identification, detectability, or statistical independence by themselves.

## Automated checks

- `AcousticViewportTest`: log-frequency axis calibration, zoom bounds and time positions.
- `CallLinkPolicyTest`: matching survey and group, valid call bounds.
- `TrapProtocolPolicyTest`: method list and minimum complete-device conditions, including valid still-deployed devices.
- `scripts/test_v023_analysis_traps.py`: schema, audio controls, linking, three protocol screens, ZIP exports and survey close integration.
- Inherited source tests and Android JUnit tests remain enabled. `app:assembleDebug` packages the compiled beta APK.

## Phone verification still needed

- Zoom/contrast controls on 320 dp and large-font devices; spectrogram readout at 1 kHz, 2 kHz and 8 kHz compared with known recorded test tones.
- Reopening a finished survey clip and linking species and a matching historical observation. Confirm scientific name provenance and no auto-identification.
- Start trap/camera survey, register existing deployed camera with correct UTC deployment date, record 0-detection operational check, and Finish Complete with camera still deployed.
- Pitfall and live-trap check with two reported animals, one capture detail, attempt Complete (must become Incomplete). Add the second capture detail and retest after completing other visit conditions.
- Export ZIP, confirm check/capture metadata, full WAV and labels, and all three device CSVs.
- Test microphone permission denied, long recordings, interrupted recording, 44.1-kHz hardware compatibility, display rotation and lock screen lifecycle.
- Existing Complete, Incomplete and Not Counted outcomes, GPS missing-start provenance, photo links and station effort must stay unchanged.

The build checks are source and JVM-level tests. No actual Android handset, microphone calibration or live animal-trap visit has been exercised.
