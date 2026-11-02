# APO RC90 | Step 3: method-adaptive survey recording panels
2 November 2026 · Development stage, unsigned until CI verified.

## Scope and source
Derived from the signed and emulator-tested RC89 code149 app. New branch `feature/apo-rc90-adaptive-method-panels-v150`, target name `0.27.3-rc90-apo-method-panels`, code **150**, original `org.phwildlife.survey.beta08` package, signer, locked media and references. SQLite version remains **29**. The current `SurveyMethods.forProfile` registers 17 profile-method options. New `RecorderMethodUiPolicy` is strictly presentation-only; it does not modify protocols, save rules, elapsed effort, sampling units, counts, database, source taxonomy or scientific eligibility.

## Method-specific behaviors
- **Bird point count and line transect:** emphasize distance source and show first-detection position/movement immediately. Other bird methods keep first-detection detail collapsed but accessible. Point count remains station-gated and continues to use the actual timed count; line transect still requires real magnetic geometry before perpendicular distance can be derived.
- **Herpetofauna amphibian calls:** display call-index and assessed background noise in the main herp panel; move microhabitat and life stage to its existing optional environment panel. Amphibian audio/evidence action opens the existing Call Library, never generates a sound record on its own.
- **Herpetofauna VES / transect / coverboard / drift fence:** microhabitat and life stage remain prominent; call score, substrate, environmental values and other metadata stay accessible by disclosure rather than silently disappearing. Coverboard and drift-fence actions open existing method and device records.
- **Mammals camera-trap and live-trap checks:** keep evidence type and device/trap identifier in the main mammal form, with optional height below the existing Measurements disclosure. Other mammal methods retain equipment ID in that disclosure; track-sign/spotlight effort opens their existing checked protocol tools.
- **Other and incidental methods:** explicit short labels avoid claims of standardized absence or fully implemented acoustic effort. Bat acoustic is labeled a basic detection log, consistent with the method catalog. Existing `Save Observation`, `Review`, `End Survey`, draft/timeline, GNSS, correction and export flows are preserved.

## Validation required
1. `RecorderMethodUiPolicyTest` tests all registered method focus labels and exact first-detection, listening, microhabitat and device action gates.
2. `scripts/test_apo_rc90_method_panels.py` ensures actual UI bindings, no database behavior in policy, and unchanged Complete / Incomplete / Not Counted semantics.
3. `RecorderMethodPanelsEmulatorTest` opens actual amphibian calling, herp VES and camera-trap screens and inspects the visible/default-collapsed fields and method protocol actions. Existing `RecorderUiEmulatorTest`, `ObservationTimelineEmulatorTest` and `RecorderPickerCompactEmulatorTest` remain in the Android14/160%/200%-font suite.
4. Dedicated signed workflow `.github/workflows/apo-rc90-adaptive-method-panels-v150-qa.yml`: science Python tests, JVM, Android build, signer and pinned assets, Android14 UI tests and screenshots before delivering the verified APK.
5. **Not field-certified:** physical handset save→reopen→correction→research ZIP audit; prior populated database upgrade; real GPS/photos/audio; station lifecycle; complete/incomplete/not-counted and accessibility/TalkBack remain external acceptance gates.

Do not report release acceptance, CI success or direct APK before checking actual results.
