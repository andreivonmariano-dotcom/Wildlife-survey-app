# APO RC102 signed preview | Centered startup and one-time motion

**Verified:** 8 November 2026, Asia/Manila.  
**Source:** draft PR #107 `feature/apo-rc102-centered-startup-motion-v162`, HEAD `e6d56bb46958ccf50a95af911c727f9a970ea4fb`, branched from signed RC101 code161.  
**Workflow:** GitHub Actions [run 42498533640](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/actions/runs/42498533640) **SUCCESS**. No merge or real handset test was performed.

## Delivered signed APK

- Chat-local file: `APO_RC102_Centered_Startup_Motion_code162_signed.apk`.
- File size **12,452,250 bytes**; SHA-256 **`7bd551d4de52341ea158a51a1f210cc0ab91493480cd24f98897c56b5011f474`**.
- Package `org.phwildlife.survey.beta08`, versionName `0.27.3-rc102-startup-centered-motion`, versionCode **162**. CI used aapt and apksigner to check identity and exact retained certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`; local ZIP archive integrity passed.
- GitHub run artifact: `APO-RC102-Centered-Startup-signed-code162-preview` ID **13624394582**. Downloaded as an artifact ZIP to the originating conversation, extracted `app-debug.apk` and independently verified the resulting file exists, ZIP CRC and SHA-256.
- **All six protected bundled content assets** independently SHA-256 compared against the previously signed RC101 direct APK and were byte-identical: approved APO emblem master, PAIC714 CSV, WorldCover file, canonical PHLibrary Book 01 v1.82, bibliography v1.82, and taxon-concept crosswalk v1.82. No existing field survey rows, user-provided personal assets, or SQLite schema were rewritten.

## Verified UI changes

- `LikasLaunchActivity` had `identityParams.topMargin=dp(80)` despite `FrameLayout.LayoutParams(Gravity.CENTER)`. Removed the bias. The composed emblem, wordmark and new line now have a geometrically centered identity frame.
- Emblem scale from 0.93× to 1.0× (~540ms) plus existing soft fade, wordmark letter tracking from 0.24 to 0.12, and a centered 72dp × 2dp sage line expanding once (~610ms total).
- Reduced-motion/system-disabled animator condition presents the final state statically. Tap skip finishes all four animation aspects before entering Home. Offline reference bootstrap/error behavior and Welcome Home route unchanged.
- Debug-only `qa_hold_splash` allows native screenshots without triggering normal automatic navigation.
- Android emulator runs at **360×800, font 100%** and **320×720, font 200%** using real native views. `Rc102StartupMotionInstrumentedTest` verified actual identity and viewport vertical-midpoint difference <=2dp and all source/view motion states; **2 tests per preset** passed. Captured four real screenshots including static and animation-final images. The 320px and 360px PNGs were extracted and visually inspected; both show the emblem/wordmark/line centered with no clipping. Screenshot artifact **13625180012**.
- Inherited full RC101 PHLibrary/emulator tests passed. Python source regression suite also passed after updating legacy code/version expectations to RC102 without relaxing database, signing, science or survey-state checks.
- Signed RC92 code152 → RC102 code162 **adb install -r**, preserving seeded survey rows, zero-detection effort, original identification, audio checksum, end outcomes and research export, passed in Android14 emulator. Upgrade artifact ID **13625310171**. Real-owner-handset upgrade not independently tested.

## Continuity and limitations

- Keep the source branch PR #107 a **draft** while awaiting user visual acceptance/handset test. It is a signed development preview, not a stable public release. The earlier RC101 remains a usable rollback binary when needed, provided downgrade mechanics and user data are considered.
- No migration or database-schema change. In-place installation over the existing signed APO/LIKAS package is intended. **Never uninstall or clear application storage before updating.**
- Original approved mountain emblem PNG/certificate protected. No new generated artwork or replacement icon.
- Source tests and emulation are not proof of all manufacturer-specific device behaviors, GNSS fidelity or fieldwork outcomes.
