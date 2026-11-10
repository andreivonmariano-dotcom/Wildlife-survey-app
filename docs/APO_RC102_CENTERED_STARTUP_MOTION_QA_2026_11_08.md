# APO RC102 | Centered startup and one-time motion (code162)

**Owner instruction:** 8 November 2026 (Asia/Manila). Correct the vertically low startup identity and add a smooth animation distinct from the existing fade. Keep the official mountain emblem, dark-green gradient and short automatic navigation.

## Source lineage
- Branch: `feature/apo-rc102-centered-startup-motion-v162`, draft PR #107 onto signed RC101 `feature/apo-rc101-phlibrary-v182-canonical-v161`.
- Version: `0.27.3-rc102-startup-centered-motion`, **versionCode 162**, Android package `org.phwildlife.survey.beta08`, original retained signing key and SQLite29.
- Startup: `LikasLaunchActivity` remains the sole Android launcher; `WelcomeActivity` retains the original Home and survey-resume routes.
- Out of scope: All existing survey methods, records, export, taxonomy, canonical 715-account PHLibrary Book v1.82, Discovery Rarity v0.3.4, offline bibliography and locked APO artwork.

## Changes
1. Remove `identityParams.topMargin=dp(80)`. The entire mark/wordmark/accent group now uses `Gravity.CENTER` vertically and horizontally in the available content viewport. Bottom reference-loading status text remains independently bottom-anchored and does not displace the identity.
2. Preserve existing emblem and wordmark fade-in while adding an emblem **0.93×→1× scale settle**, a restrained **0.24→0.12 letter-spacing reveal**, and a centered **72dp × 2dp sage-colored line** extending outward once beneath the title. The mark settles for 540 ms and the line finishes at ~610 ms. No infinite glow, shimmer or background animation.
3. Tap-to-skip cancels all running animators and restores final alpha, scale, letter spacing and line state before attempting normal Home navigation. The splash remains dependent on reference bootstrap as before. `reduced_motion` or disabled Android animators makes the entire identity static.
4. The accent line is decorative and hidden from TalkBack; the emblem and wordmark retain their own descriptions.
5. Debug-only `qa_hold_splash` intent extra keeps the screen rendered for emulator screenshot and geometry checks. This flag is active only for debuggable APKs and is not used by normal startup.

## Tests
- Python `scripts/test_rc102_startup_motion.py`: no vertical margin, new motion structure, skip/reduced-motion, unchanged package identity and no survey DB interaction.
- Android `Rc102StartupMotionInstrumentedTest`: launches held real splash on **360dp/100%** and **320dp/200%** emulated displays, measures the actual root and identity vertical midpoints, checks static reduced-motion state, and captures screenshots. With animation scales enabled it also tests mid-animation scaling/line expansion and final resting values.
- The RC102 emulator script first runs the inherited full RC101 library/source UI suite, then the new startup checks. The workflow gates JVM/unit/source tests, original signer, identical source-art and reference hashes, actual Android14 emulator tests, and real signed RC92→RC102 no-data-clear upgrade. The new code162 APK artifact should only be delivered after workflow SUCCESS and local archive/type/hash checks.

## Release state
**In progress:** CI `APO RC102 centered startup motion and PHLibrary v1.82` started on the RC102 branch. No claim of passing tests, verified new APK, phone installation, or source-to-main merge until results are checked. 
