# LIKAS RC40 | Screenshot audit and interface corrections

**Date:** 29 October 2026 · **Branch:** `feature/likas-core-fieldwork-post-rc4`  
**Target build:** `0.27.3-rc40-home-layout-copy-preview` · versionCode **100** · SQLite **27**  
**Evidence:** Three user-supplied RC39 screenshots (Home, Birds survey setup, Fieldwork), review of RC39 Java source, plus earlier RC39 app audit.

## Screenshot findings

**Home: blocked navigation (high).** The screenshot shows a Fieldwork panel taking most of the vertical screen. Library and Settings lie below the fold, creating the appearance of a single-option main menu. RC39 uses a scrolling `FrameLayout` tile with a match-parent decorative child. RC40 replaces it with a horizontal content-sized `LinearLayout`, no match-parent internal drawing layer, 108dp minimum height and independent access to the three original destinations. The hero is 152dp high. This is a source-level cause/remediation, not yet a real-device screenshot measurement.

**Bird survey: labels absent (medium).** The existing numeric defaults `10` and `2` hide their hint text. RC40 adds persistent labels for point count duration (minutes), planned route length (km), station spacing and radius, and ties conditional labels to the selected survey method. Existing defaults, units, method codes, station spacing calculations and validators are unchanged.

**Fieldwork: repeated navigation (medium).** `SURVEYS`, `Go to the field`, `Start a survey`, `Choose a wildlife group` and `NEW Choose wildlife group` repeat the same instruction. RC40 uses one `New survey` header and the same four group choices. A separate `Latest record` card duplicated the History action. The History shortcut now carries the most recent outcome in its caption. Opening Fieldwork and setup resets the scroll position to the top.

## Additional confirmed source corrections

- Analysis and History now use `SurveyOutcomeSql.RESOLVED` to classify older NULL-outcome surveys, without data migration. Archived counts exclude active sessions. This addresses the RC39 Dashboard inconsistency.
- The live survey countdown honors the existing Less motion preference. End Survey, Complete, Incomplete and Not Counted controls remain unchanged.
- Gallery previous/next targets are 48dp wide. Search, offline card imagery, individual IUCN/DENR statuses and scope remain unchanged.
- Removed marketing and defensive repeat copy from Home, Library, Settings, Habitat links, habitat profiles, species details and citation notes. Scientific caveats remain where a user could otherwise mistake a source association for a field observation.
- Five source-backed claims from the 96-account research catalog were rewritten as direct results rather than `the study found` or `according to`. Original species, samples, measurements, source names, URL records, and study scopes remain. The 96-account inventory and 456 structured source claims are unchanged.
- The full national taxon checklist and research assets are retained. No redistribution/license changes or new scientific findings were introduced.

## Regression tests

- `scripts/test_rc40_screenshot_audit.py` checks content-sized Home source, all three destinations, concise Fieldwork, visible method-specific labels, reduced motion, shared legacy outcome SQL and SQLite fixture outputs, and unchanged package/DB identity.
- `Rc40HomeLayoutInstrumentedTest.java` covers rendered Home-card height and Birds setup labels on Android.
- Existing scientific-core Python tests, JVM tests, Android app compilation and instrumented test compilation must still pass.

## Release gate

**Pending until CI completes:** compile RC40, confirm no regressions, retrieve current artifact, inspect manifest versionCode **100**, confirm the same pinned signer certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, attach exact debug APK to chat.

**Still pending after successful compilation:** execute device instrumentation, verify Home and setup screens on a 320dp/large-text phone, field test the active-survey lifecycle, and install over RC39 with real backed-up records. Source tests and AndroidTest compilation alone do not prove those behaviors.

## Remaining audit items

- Offline gallery still constructs all card views synchronously, with image decoding on the UI thread. Measure first, then move to lazy/recycled rendering.
- A dedicated physical-device pass is needed for GPS permissions, backup/restore, camera/audio evidence, and text scaling.
- The full Research and Conservation Watch remains roadmap work. Do not label it shipped.
