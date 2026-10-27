# LIKAS v0.27 RC1: integration review, screen matrix and field acceptance

Date: 27 October 2026
Source branch: `release/v0.27-integrated-ux-performance-qa`
Integration PR: #31, target `main`
Version: `0.27.0-rc1`, versionCode 46, Android package `org.phwildlife.survey.beta08`

## Release state

This is a **candidate**, not a stable field release. The app's automated source checks, JVM policy tests, SQLite migration tests and Android compilation run in GitHub Actions. Real phone screenshots, touch testing, microphone recordings, overnight GNSS tracking and battery measurements have **not** been performed.

The repository's `main` branch is still the v0.15 code series. The review branch contains the ordered work from the early GPS, taxonomy, approved forest/frog art, survey outcome, acoustic, trap and method-protocol branches through v0.26, plus the v0.27 changes below. Independent PR #18 includes another GPS elevation implementation based on old main and must be compared before retirement. Do not merge both elevation implementations automatically.

## Q1: screen-by-screen acceptance matrix

Each line was checked against its Java source and manifest registration. UI appearance is a **code review**, not a verified screenshot. The field tests need real Android hardware, portrait mode, 320dp and 360dp widths, normal and 200% text, TalkBack and the keyboard visible.

| Screen | Code-level check | Required handset check |
|---|---|---|
| Forest startup | Approved forest assets and launcher entry remain | Day/night timing, smooth animation, back-to-home startup, reduced-motion behavior |
| Home / Welcome | Four group tiles stack on small widths, History and checklist shortcuts remain | Tile wrapping, large text, Resume Survey, scroll and screen reader |
| Survey method setup | Method selection, route preview, optional study rule selection | On-screen keyboard, GPS cancel/resume, location permission |
| GPS preflight | Verified fix or explicit no-fix choice retained | Under 10 m validation, slow fix, GNSS disabled, stale fix |
| Live recorder | Species Browse/Unidentified, mark time/bearing, Save/Review/End pinned | 320dp footer, large font, switching taxon groups, 100 detections |
| Camera bearing | Detection bearing and pitch separate from evidence photo | Camera permission, compass accuracy, rotation/orientation |
| Offline species picker | Search, filters, evidence and score disclosure retained | 2,000-result list, typing speed, filter persistence |
| Taxon detail | Source-review route remains present | Scientific source labels, long names, back navigation |
| Offline checklist | Original species indexes preserved | Bird/reptile/amphibian/mammal filters, Unicode search |
| Regional checklist | Area selection flow unchanged | Manual region, GPS denied, scientific provenance |
| Survey history | Context-specific actions and delayed search | Large history, closure audit, canceled/complete/incomplete filters |
| Station progress | Station visits and effort review retained | Point/route order, pause, incomplete and early ending |
| Sound library | Saved WAV and spectrogram analysis retained | Live record, long playback, label/frequency annotation, background interruption |
| Trap protocol | Camera/pitfall/live-trap inspection UI preserved | Repeat check and capture release evidence after survey closure |
| Other field protocols | Coverboards, area searches, track signs, spotlight retained | Timed zero detections, repeated area/route, sign photo/GNSS |
| Study protocols | New templates are wildlife-group-specific, old templates shared | Create/edit/delete, frozen snapshot across project changes |
| Projects and stations | Saved geographic/site data retained | Create and revisit station, empty project, lost GPS |
| Fieldwork dashboard | Counts and database integrity action retained | Mixed status totals, large dataset, portrait accessibility |
| Scientific audit | Research validity route retained | Report of missing source photos and survey outcome mismatches |
| Backup import | Import screen remains accessible | Restore archived legacy data; verify counts, IDs and source links |

`LikasLaunchActivity` and `WelcomeActivity` form one startup/Home flow in the matrix. Manifest registration remains checked by automated tests.

## Q2: long-session performance and power

- **Previously:** the one-second recorder HUD triggered station-progress and GPS-distance SQL reads every four seconds on the Activity/UI thread.
- **RC1:** a separate `SurveyHudReader` queries every six seconds on one background executor. A pending request blocks duplicates. Results return to the main thread only for the same active survey. The worker releases its database after the Activity closes.
- **RC1:** CSV export reads use the dedicated export `FieldDb`, not the Activity database being updated by fieldwork controls.
- **RC1:** the official MDD v2.5 taxonomy CSV can be cached on CI after checksum verification, cutting redundant download attempts without weakening source validation.
- **Remaining risk:** `TrackService.record()` still writes SQLite on each accepted/rejected GNSS callback. Reworking that safely requires a bounded ordered queue, reliable stop flush, and data-loss tests. It has **not** been altered in this candidate.
- **Remaining risk:** `MainActivity` still spans more than 2,300 lines. The new HUD reader is one extraction, not a full modular rewrite.

Measure performance on hardware. Recommended profile: one-hour point-count survey, one-hour line transect with GPS running, 100 observations, ten images, ten call annotations, and a final ZIP export. Capture app heap and total PSS, main-thread frame drops, storage growth, and battery level at start and finish. Battery measurements need the same handset, radio environment, brightness and sampling workload before comparing versions.

## Q3: migrations, backup and merge gate

`FieldDb` version 27 adds a nullable `survey_profile` column and a scoped index to `study_protocol_templates`. Previous templates remain `NULL`, shown as **Shared legacy**. New records use Birds, Herpetofauna or Mammals. During survey creation, LIKAS checks the template's project, method and wildlife group, then copies its chosen requirements into a permanent per-survey snapshot. No original observer record is overwritten.

`scripts/test_v027_migrations_backup.py` extracts the **actual SQL** from FieldDb v24, v25, v26 and v27 migration functions. Its tests create pre-upgrade samples and verify acoustic annotations, trap devices, board checks, old study snapshots, missing-GPS provenance, foreign-key consistency and SQLite backup/restore survival. These tests do not cover actual Android device SQLiteOpenHelper or upgrades starting at v0.15. An archived v0.15 database must be exercised on a handset or emulator before final stable publication.

### Release acceptance gates
1. Python source/QA tests and Android JUnit tests green.
2. Debug APK built and artifact inspected; retain unchanged app ID and signing identity.
3. PR #31 merge conflicts resolved in favor of the approved current frog/forest art. Legacy main branding notes retained in an archive, without restoring unused older startup code.
4. Database migration v15→v27 tested on a copy of a real prior installation, followed by a full export, uninstall/reinstall and controlled import where supported.
5. Device screen, audio, GNSS, camera, file picker and long-session acceptance tests signed off.
6. Explicit decision on independent GPS PR #18. Close stacked PRs only after their lineage and merge state are reconciled.

**No step that requires a physical phone or full v15 backup was recorded as passed in CI.** Keep the integration PR open until that fieldwork gate is met.

## Included automation

- `FieldLayoutPolicyTest`: 320dp and large-font breakpoints and button heights.
- `SurveyMethodsDisplayTest`: truthful legacy method names.
- `StudyProtocolsTest`: additive survey completion requirements.
- `test_v027_release_quality.py`: manifest screens, response sizes, bounded background worker, export DB isolation and study scoping.
- `test_v027_migrations_backup.py`: SQLite migrations 23→27 and backup/restore.
- All previous GPS, amphibian/bird, taxon, camera/trap, field and survey-outcome source checks remain.

Approved image files and the frog icon were not regenerated or changed.
