# APO RC80 | Compact survey status and safe station transitions

**Date:** 2 November 2026 (Asia/Manila)  
**Source branch:** `feature/apo-survey-hud-stations-v140` from verified signed RC79 code139 (draft PR #81).  
**Target:** versionName `0.27.3-rc80-apo-survey-hud-stations`, versionCode **140**, appId `org.phwildlife.survey.beta08`, schema **28**.

## Source changes

- At Android text scaling >=160%, the tall status panel is collapsed into two always-visible accessible touch rows: active effort plus current point/visit clock, and current GPS quality (including an explicit unverified start-fix warning). The clock row expands full session, progress, pause and GNSS history in the survey scroll. The GPS row opens the existing observer-GPS detail directly. Reduced motion and existing time-source logic are retained.
- At smaller font sizes, the original status panel remains unchanged. The compact clock hides when the keyboard is visible, preserving the GPS quality status and the fixed Save / Review / End Survey dock.
- Switching generic sampling visits or bird point-count stations now checks the **full** unsaved draft, including count, distance, habitat, notes, confidence, life stage, position, movement, marked time and pending photos. Explicit discard resets only the unfinished entry and persists the cleared draft. Saved field records remain unchanged.
- End Survey keeps all three deliberate outcomes and authoritative completion policy. Its method-completion details now expand on request, avoiding a very long initial dialog. Complete still downgrades to Incomplete when protocol conditions fail.

## Protected

No database migration, result reinterpretation, observer GPS fix substitution, fabricated station coordinates, survey effort change, or modification to the locked original APO mountain icon, PAIC714 or WorldCover binary assets. Original package/signing identity retained.

## Acceptance

- Source/JVM policy tests plus Android instrumented recorder UI.
- GitHub Actions workflow `.github/workflows/apo-survey-hud-v140-emulator-qa.yml` checks original signer, immutable assets, package and version, and captures Android 14 emulator screenshots at **160%** and **200% / 320dp**.
- New RC80 APK is not a verified or available download until an actual signed run, artifact extraction, hash checks and current-chat direct attachment.
- Physical in-place upgrade with real historical data, precise GPS, acoustic/photo fieldwork, complete/incomplete/not-counted actions, research ZIP content, and full TalkBack remain unverified.

**Status:** Code and QA configuration committed. Run evidence and signer/file verification must be appended only after execution.
