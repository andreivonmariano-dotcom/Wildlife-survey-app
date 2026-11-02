# APO RC77 | Full BioTools destination screen audit and signed preview

**Date:** 2 November 2026 (Asia/Manila)  
**Development branch:** `feature/apo-biotools-all-screens-v137`, draft PR #79 into `feature/apo-biotools-workflows-v136`  
**Exact app/test source built:** `0075ead49c4e60cd1a69a6dc29be3520b0a26ddd`  
**Successful CI run:** `41380263162`; workflow `APO RC77 all BioTools screens signed preview`.  
**Signed build artifact:** `APO-v137-all-biotools-signed-preview` (ID `13112537646`)

## Exact delivered APK

- In-chat file: `APO-v0.27.3-RC77-All-BioTools-code137.apk`.
- **11,754,140 bytes**, SHA-256 `99b84a1dcebe8ffdce3c2e35aa26780585de4d03b7980b7b493318fe62a4815d`.
- Package `org.phwildlife.survey.beta08` (unchanged). Android `versionCode=137`, `versionName=0.27.3-rc77-apo-biotools-all-screens`.
- Original signer SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` unchanged.
- Exact CI ZIP was downloaded in this conversation, its `app-debug.apk` extracted directly to `/mnt/data/APO-v0.27.3-RC77-All-BioTools-code137.apk`, independently verified with `unzip -t`, `sha256sum`, `wc -c` and embedded-asset checks.

## Implemented scope

Reviewed the main five BioTools entry routes plus their nested pages. Changes in RC77:

1. **Field Records / Habitat:** Launching recorded habitat evidence from BioTools now passes `initial_tab=records`, rather than showing the reference guide first. The guide remains reachable in a separately labeled tab. Guide-only research controls are hidden while viewing field records. Read-only counts distinguish registered stations from environmental visit records. Missing elevations, habitat labels, and uncertainty are still displayed as missing.
2. **Field Utilities / Acoustic:** Adds a clear offline acoustic evidence introduction, a proper empty state, naturally growing recording-list rows, scalable Play/Pause and Label Call actions that stack for narrow/high-font displays, clearer annotation hierarchy and audio-review uncertainty note. Unifies hardware Back behavior into the existing recorder lifecycle, protecting in-progress captures and a pending save; retained original spectrogram analysis and acoustic label linkage.
3. **Field Utilities / Trap/Cameras:** More readable archive navigation shows all recorded device surveys and an empty-state note. Device cards and record/check actions no longer force a 48/55dp height that can clip enlarged text. Registration, check and capture dialogs scroll. Device record drilldown returns to list on Back. Active-survey method gates, UTC validation, zero-detection checks and retrieval audit remain unchanged.
4. **Field Utilities / Field Protocols:** Field protocol list uses compact numbered text actions with self-contained survey metadata and no fixed 50dp rows. Descriptive method/effort caveat is visible, and archived surveys remain read only. Number entry uses shared input styling, long form dialogs scroll, and hardware Back returns to field protocol survey list. Historical field observation, station/GPS provenance, and photo evidence logic remain.
5. **Projects & Stations / Study Templates:** Project creation, editable study template and acoustic annotation dialogs gain scrolling. Original project/station immutable identifiers, numeric validation, study rules, confirmation dialogs and database writes are otherwise retained.
6. **Surveys / History / Analysis / Exports / Quality:** RC76 work remains present: four-stage method setup, scrollable survey history outcome/search, careful effort/detection summary semantics, independent research ZIP and privacy notes, read-only data quality checks. No new unverified ecological metrics or extrapolations were added.

## Verified by CI and current-session extraction

- **410 Python regression and scientific-source tests passed.**
- Android JVM unit tasks and `:app:assembleDebug`/`:app:assembleDebugAndroidTest` completed successfully.
- CI verified exact package, code 137, versionName and original signing certificate via `aapt` and `apksigner`.
- Downloaded APK passes ZIP integrity; independent SHA-256 matches CI.
- Immutable embedded owner-approved assets still match:
  - APO mountain artwork `6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7`
  - PAIC714 master catalog `1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959`
  - ESA WorldCover derivative `e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089`.
- Database schema **28**, original package ID and signer unchanged. Source retains Save Observation, fixed End Survey, Complete/Incomplete/Not Counted and zero-detection effort safeguards.

## Remaining limitations

**This is a signed development preview, not a field-validated stable version.** Did not perform real handset RC76→RC77 installation or upgrade retention with archived surveys, instrumented emulator interaction, full 320dp or 200%-font screenshots, TalkBack review, long GNSS survey testing, real sound capture/playback, camera/pitfall workflow check, successful study-template edit/delete round trip, actual field database backup/restore, or research ZIP content inspection on a device. Source-based layout tests and Android compilation are not proof of full on-device accessibility or workflow acceptance. PR #79 remains draft and unmerged.
