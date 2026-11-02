# APO RC91 | Step 4: method completion checks and a single action per tool

## Verified signed development-preview acceptance | 2 November 2026

- **Verified signed code and tests:** source SHA `41536df8af31b7f06b2d2aab83feb90e73dd1c77`, GitHub Actions workflow **41433764045 SUCCESS**, app version **0.27.3-rc91-apo-method-review**, versionCode **151**, SQLite **29**, package **org.phwildlife.survey.beta08**, original signing certificate.
- **QA:** **462 Python source/scientific regression tests passed**, Android JVM + APK and AndroidTest builds passed, **10 Android14 API34 tests at 160% font and 10 at 200%/320dp**, zero failures. Device-method and amphibian audio screens have a single visible action. All original RC90 survey-method panels, RC89 timeline, RC88 mark/draft, RC87 habitat shortlist tests retained. The finished run has no failed steps.
- **Released owner-downloadable signed preview:** `APO-v0.27.3-RC91-Method-Completion-Review-code151.apk`, **11,774,604 bytes**, SHA-256 `3529b0fd38c89a333723540febeeeb4c5619e58a319931ee3e02bcc330d26580`, direct artifact from run **41433764045**, artifact ID **13136291087**. Exact archive integrity and installed APK byte digest verified independently, with unchanged embedded PAIC714, ESA WorldCover, approved mountain artwork and pinned Commons eagle photo SHA-256. CI independently checked Android signer, app ID and version.
- **Screenshots:** artifact **13134979640**, six valid distinct PNGs, 1080×2280 at 160% and 960×2160 at 200%/320dp. The recorder and picker contact sheet was visually inspected: individual-count field and fixed Save/Review/End remain accessible. **Screenshots show default recorder/picker screens, not the expanded method-completion dialog.** The summary logic is covered by policy/JVM tests and source contracts, and the duplicate-action UI by Android instrumentation.
- **Retained photography:** Initial builds were blocked by Wikimedia Commons HTTP429 on a previously approved pinned eagle JPEG. The final workflow restores exactly nine Commons JPEG bytes from verified **signed RC90 artifact** `APO-v150-method-panels-emulator-signed-preview` (run **41430183895**, SHA-256 `8d24c96581618570aace24a50d3e6a7be641a2d082f0cf53b05ced69da9e27c3`) and then independently validates each approved SHA-256 and existing source manifest using `scripts/seed_pinned_commons_from_verified_apk.py`, with `prepare_offline_photos.py` still mandatory. No new image or license substitution. **Important durability limitation:** original GitHub Actions artifact is retained for only seven days, so the APK seed fallback is temporary; store reviewed bytes in a durable hash-pinned repository bundle or durable cache before expiration, never bypass checks.
- **Earlier emulator failure:** the initial RC91 test queried the now-hidden duplicate shortcut instead of its visible counterpart for amphibian calls and camera-trap devices. The final test identifies a visible operable button and asserts exactly one matching visible action; it passes. App-level evidence was unchanged.

**Physical-device limitations:** No verified existing-populated-database upgrade, actual station-start/effort modification/finalization on phone, expanded End Survey dialog screenshot, real GNSS/photo/audio/device and ZIP export roundtrip, screen-reader usability or field study scientific method certification. RC91 is **signed emulator-tested development preview**, draft PR #93 unmerged.



2 November 2026 · **Development preview, not field-certified.**

## Source and scope
Derived from signed RC90 code150, on `feature/apo-rc91-method-completion-review-v151`. Build target **0.27.3-rc91-apo-method-review**, code **151**, package `org.phwildlife.survey.beta08`, original signer, locked artwork, PAIC714 and WorldCover, **SQLite29 unchanged**.

### Method-specific review
- The pre-existing End Survey dialog still exposes **Finish Complete**, **End Incomplete** and **Cancel Survey · Not Counted**, and never treats a saved detection as proof that a sampling protocol was completed.
- A **read-only method-records status** (from actual saved field protocol/deployment records) appears in the dialog for coverboards, area searches, track/sign, spotlight, camera traps, drift-fence checks and live-trap checks.
- For coverboards, the preview reports board count and uninspected board count; for area search, track/sign and spotlight, count and invalid/incomplete pass count; for camera/trap protocols, deployment count, never-checked count, missing capture detail count and currently deployed devices. The latter **do not require retrieval for a scheduled completed check** by existing protocol policy.
- When a protocol record minimum is unmet, **Review method effort** or **Review device checks** goes to the existing records screen. When a required sampling station is not active, the former opens the *existing station-start workflow*, never creates a fictitious visit.
- A positive method-record status is only **ready for the other final checks**, not certified complete. Existing `completionChecklist()`, `SurveyClosePolicy.resolve`, `FieldProtocolPolicy.canComplete`, `TrapProtocolPolicy.canComplete`, `StudyProtocols`, route/area/effective effort and station rules decide the outcome later. The original unsaved-observation prompt still blocks accidental loss.

### Removing duplicate actions
- RC90 displayed context-action buttons for method/audio/devices, then again repeated the same action in the advanced configurable quick shortcut and sometimes More tools.
- RC91 computes `RecorderMethodUiPolicy.inlineQuickKind` and hides *only exact duplicates*. Alternative valid user-configured actions and Change settings remain available in Field tools. The inline method action remains prominent.
- No new media workflow, generated taxon evidence, changed source checklist or altered backend write path.

## Required verification
- `MethodCompletionReviewTest` checks actual allowed zero, invalid effort, uninspected boards, missing device/capture detail, and open devices that do not bar a scheduled check. `RecorderMethodUiPolicyTest` covers precise shortcut deduplication.
- `scripts/test_apo_rc91_completion_review.py` verifies no new database writes or altered survey classification and source bindings. Inherited scientific/source tests, Android JVM, package signer/asset checks, and Android14 (160% and 200%/320dp) instrumented recorder, timeline, method and species picker checks must pass.
- CI `.github/workflows/apo-rc91-method-review-v151-qa.yml` produces signed APK and six emulator screenshots if successful.
- Physical phone / populated SQLite29 survey, all method-specific closures, screenshots of expanded review, accessibility, interrupted drafts, actual GNSS/photos/audio, export audit roundtrip and TalkBack remain unverified until separately tested.

**Acceptance result pending.** Do not claim CI success or publish an APK before artifact/hash checks.
