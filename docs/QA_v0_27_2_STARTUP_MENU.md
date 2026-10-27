# LIKAS v0.27.2 RC3 | Startup menu review, part 1

Date: 2026-10-27, Asia/Manila.
Baseline: `ui/v0.27.1-field-polish-quality-labels`, stacked on v0.27 integration work.
Scope: **LikasLaunchActivity only** (startup landing). The separate Welcome/Home menu is part 2, not redesigned in this patch.
Governing requirements: G-05/G-07/G-10/G-11, U-01 through U-06, E-04/E-05, CC-01/DL-01.

## Source audit
| Original startup behavior | Problem | RC3 adjustment |
| --- | --- | --- |
| When no survey is active, "New Survey" opens Welcome/Home | Label implies a survey starts directly | Stable "Open LIKAS" describes the action accurately; Home contains wildlife group selection |
| With a survey, "New Survey" changes to "Home & Tools" | Same control changes identity based on saved state | Stable "Open LIKAS" for both states; a separate "Resume survey" appears only for valid unfinished work |
| A large frog illustration, oversized uppercase descriptor and developer version line compete with the forest | Brand/technical clutter, low space for field decisions | Compact frog and LIKAS identity, one short subtitle, no version footer |
| Two full-width buttons of fixed 60-62dp height | Possible text clipping at larger system font scales | Native wrap-content buttons with minimum height from existing FieldLayoutPolicy |
| Custom button styling duplicated in action() and onResume() | Duplicated drawables and mismatched interaction styling | Single styleAction() with clipped ripple and context-sensitive primary state |
| A database exception returns false from hasActiveSurvey() | An unreadable survey store appears identical to an empty archive | Nullable survey status, brief warning only if read fails; no database mutation |
| Forest imagery is darkened by a heavy full-height scrim | Approved art less visible than intended | Lighter top/middle contrast, dark bottom action panel, native text outside art |

## Layout decisions
- Retain the *exact* preapproved minimalist frog resource and original-resolution tropical day/night images.
- Keep two possible choices: direct **Resume survey** for an active event and always-available **Open LIKAS** for Home. Only one action appears without an unfinished survey.
- Preserve the existing Home wildlife-group selection. The startup screen does not open a survey event, request location permission, migrate SQLite or request the camera.
- Center the artwork and wordmark, keep controls toward the lower thumb zone, remove build version from the art, and protect text contrast.
- The decorative frog is omitted from screen-reader focus. Actual buttons remain labeled and accessible.
- User-requested cadence: **finish startup menu first**. Next screen is the Welcome/Home menu, separately reviewed.

## CI checks
- `scripts/test_v0272_startup_menu.py` checks launcher registration, approved asset references, button destinations, conditional resume, readable control sizing, and unavailable archive state.
- CI runs `python3 -m unittest discover -s scripts -p 'test_*.py'` and `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug`.
- APK signing identity, package and version should be checked against the prior RC2 artifact before in-place installation.

## Handset acceptance, not yet completed
1. First install without a saved event: show only **Open LIKAS**, which opens the real Home wildlife-group menu.
2. Active and paused survey: show **Resume survey** and **Open LIKAS**. Resume restores the same event and its timers.
3. Return from a completed/incomplete/not-counted survey: Resume disappears. No event is auto-created or silently closed.
4. Use a database that cannot be read: a clear warning is shown; do not claim the archive is empty.
5. Test 320dp/360dp widths, minimum-height devices, 100%-200% system font, TalkBack focus, and navigation bar space.
6. Observe day, dusk, and nighttime backgrounds and reduced-motion setting for readability, animation stability and battery use.

This is an incremental UI release candidate, not a real-device certified field release.
