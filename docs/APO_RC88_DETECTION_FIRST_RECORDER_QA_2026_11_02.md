# APO RC88 | Step 1: detection-first field recorder
## Signed acceptance evidence | 2 November 2026

**Verified app/test code:** `76bd37cfaca773184a6f92bc71ca847b05beb1da`. GitHub Actions **41424653033 succeeded**. The project ran **452 Python scientific/source regressions**, JVM/Android app and test compilation, original Android signer/package/version checks, and locked PAIC714/WorldCover/approved mountain artwork verification. Android14 API34 emulator completed 5 tests at 160% font and 5 at 200%/320dp, with zero failures. These included explicit Mark/Clear unsaved draft interaction and pre-species positioning, the normal species picker and montane/Aleutian Tern fallback checks.

**Verified direct release artifact:** `APO-v0.27.3-RC88-Detection-First-code148.apk`, **11,765,208 bytes**, SHA-256 `7786cfcba35942a301ad6af9ee384c3a2b00cb45b7a9d9ff865ce3ee7f08e0a8`, extracted from successful signed workflow artifact ID **13132056697** and directly attached to owner conversation. ZIP integrity and exact embedded PAIC714, WorldCover and mountain-master SHA-256 independently passed in the receiving container. CI signed certificate and Android ID `org.phwildlife.survey.beta08` unchanged, versionCode **148**, SQLite **29**.

**Six screenshot evidence files** from artifact **13131662685** were retrieved and validated at 1080×2280 (160%) and 960×2160 (200%/320dp), including visual review of both recorder layouts. The initial emulator attempt failed because the individual-count field was below the fixed Save footer after adding the new Mark/Pause controls. The corrected final build uses a compact horizontal Mark/Pause row, compressed explicit status and inline text + List species entry, preserving the count and fixed Save/Review/End controls above the fold. Another source attempt failed only on historical fixed button-label assertions; corrected tests check the responsive List label plus its accessible full description. **Only run 41424653033 is final acceptance evidence.**

**Cautions:** Signed development preview, not certified for physical field use. Real on-device GNSS timing and horizontal accuracy, populated-record update from older SQLite versions, app restart/background draft recovery, microphone/camera interactions, TalkBack and ZIP data roundtrip remain unverified. Repeat validation on actual handset with a backed-up historic database. Do not claim that Mark creates a saved observation; it persists a recoverable unsaved draft only.


2 November 2026 | **development milestone**, not field-certified.

## Source
Branch `feature/apo-rc88-detection-first-v148` derives directly from signed RC87 code147/SQLite29. Target Android build `0.27.3-rc88-apo-detection-first`, versionCode **148**, unchanged package `org.phwildlife.survey.beta08`, retained signing, approved artwork and all catalog/reference bytes.

## Problem and scoped fix
The RC87 live recorder already had a Mark detection action and stored a marked time plus a qualified observer GPS fix in an event-scoped draft, but the Mark button appeared **after** species/count/photo entry and was sometimes less discoverable than advanced survey tools. This produced avoidable time-to-mark delay. RC88 moves the existing action above species identification, pairs it with Pause effort, and displays immediately whether the current unsaved encounter has been marked. The Mark label and status row are accessible at enlarged font settings. Required station-start / finish action appears before marking on station-based methods; method tools remain available below the essential observation form.

- The initial row says **Not marked · Save will use the save time**, consistent with existing save logic. A successful mark shows the device-local clock time and, only if captured from a qualified fix, the horizontal GPS accuracy (otherwise **GPS not captured**). A mark is explicitly labeled a **draft**, never a saved sighting.
- Replacing an existing marked timestamp/GPS requires a confirmation dialog. Clearing the mark also clears bearing and marked observer GPS and resets to normal save-time behavior. Paused effort and required-but-not-started units cannot mark a new detection.
- Marking persists an event-scoped draft **synchronously** so the confirmation is not shown until local preference commit succeeds. Routine text changes retain debounced asynchronous draft saves. No observation is inserted by tapping Mark. Save continues to use the captured timestamp and observer fix already stored in SQLite29 field_records, and the existing Complete / Incomplete / Not Counted survey close rules are unchanged.
- Optional short haptic feedback follows the existing field warning preference only. No change to media captures or ecological reference information.

## Verification gates
- `scripts/test_apo_rc88_detection_first.py` checks field control order, mark persistence, station/effort guards, unchanged data and survey-close contracts.
- Updated `RecorderUiEmulatorTest` checks a visible mark before identification, an unsaved draft status, and reversible clearing, for the existing 160% and 200%-font test paths.
- CI `.github/workflows/apo-rc88-detection-first-v148-qa.yml` must pass scientific Python tests, JVM/unit compile, AndroidTest build, original signer/package/art/catalog integrity, and actual Android14 instrumented picker/recorder scenarios at 160% and 200%/320dp.
- Signed APK and screenshots are downloadable to the user **only after** CI success and independent archive/asset checks. Final gate: populated-database in-place update, real phone GPS, background/restart draft recovery, actual photo/audio, TalkBack, export roundtrip, and survey close outcomes. No phone acceptance is presumed.
