# LIKAS RC64 | Main Library: real-phone icon scale correction

**Date:** 31 October 2026 (Asia/Manila). **Scope:** Main Library icon renderer only. **Status:** Implementation complete; actual Android 14 density acceptance pending. **Parent:** RC63 signed code123, PR #60. Original master spec file was not present at documented repository path; project instructions and current state/QA were consulted.

## Physical screenshot evidence and confirmed cause
The owner supplied an actual phone screenshot `6280406454061699480_121.jpg` (576x1280). Library card layout and text hierarchy were coherent, but Conservation/Habitat links were unusually tiny in pale-green plates while Photos/Sources had a conspicuous pale border, unlike Species/Similar.
The signed RC63 APK contains generated Conservation/Habitat links **64x64 PNGs**, each with ~4px transparent margin; approved Photos/Sources are full-bleed **512x512 PNGs**. `LikasPortalUi.addApprovedIcon` used `CENTER_INSIDE`, which does not upscale an image smaller than the view's physical pixels, causing 64px to display tiny at high density. Photos/Sources were explicitly shrunk to **40dp** within a **50dp** plate. These are renderer faults, not justification for further asset regeneration.

## Correction
Use `FIT_CENTER` solely for `library-conservation`, `library-habitat-links`, `library-photo`, and `library-sources`, with a uniform **50dp image inside the 50dp rounded plate**. No crop. Existing bitmap nearest-neighbor flag retained. Other symbols continue `CENTER_INSIDE`; unrelated icons/screens untouched. All 18 original approved PNGs, two recently generated PNGs and reference records remain unchanged. Survey data and SQLite 27 untouched.

## Acceptance gate
- Android14 actual screen captures at 360dp/160dpi, 360dp/**440dpi** and 320dp/440dpi at **200%** font, top and bottom. This closes the prior emulator-only-160dpi blind spot. Measured drawn bitmap alpha must cover >=75% of 50dp image for each of four affected icons; their intrinsic image sizes must remain 64 or 512px, respectively. Check all 7 clickable routes and scroll-reachability, and rerun RC60 main Library and RC51 research ZIP exact export tests.
- Full Python scientific regressions, signed original certificate and APK package/version checks. Do not claim outdoor sunlight, physical device installed build, TalkBack spoken order or older-schema migration verification from an AVD.
- Advance to versionCode **124**, `0.27.3-rc64-library-phone-icons-preview`, original app ID, signing identity and DB 27.

**Next:** Owner review of high-density screenshot versus provided real phone image; then individual Species account screens if accepted.
