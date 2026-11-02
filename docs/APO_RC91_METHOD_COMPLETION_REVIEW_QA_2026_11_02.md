# APO RC91 | Step 4: method completion checks and a single action per tool

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
