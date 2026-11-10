# LIKAS v0.19.8: minimal frog pond icon

Date: 2026-10-27

## Approved design
The observer selected a simpler frog head emerging halfway from still pond water, without lily pads. The selected pixel-art draft was supplied in the project conversation as `a_clean_minimalist_pixel_art_app_icon_style_illus.png`.

## Android implementation
- Source: 1254×1254 original rendered icon.
- Production crop: the approved frog artwork, cropped to remove white rounded-square borders and center the face and waterlines.
- Production image: `app/src/main/res/drawable-nodpi/likas_frog_icon.webp`, 432×432 pixels, 3,606 bytes. SHA-256: `aba89d67d5b2b815d24fb9e8c9edc3767bcc41e58d9d430f218b5ab05b0bc5e3`.
- Used by Android adaptive normal and round launcher icon resources, plus `LikasLaunchActivity` and `WelcomeActivity`.
- Startup title is separate readable text, `LIKAS`, below the frog image. The icon itself contains no words.
- The older approved illustrated LIKAS icon remains in repository history, so it can be restored without re-generation.
- Uses the existing deep forest green launcher background color and preserves app package ID and beta signing key.

## Scope
No changes to species checklists, survey state, SQLite data, survey closure, GPS tracking, methods, history, exports, or startup forest animation.

## QA
`scripts/test_likas_branding.py` verifies the binary image hash, normal and round adaptive icon declarations, startup branding, and permanent End Survey action. The CI workflow runs the existing Python and Android JUnit suite, then builds `app-debug.apk`.

Phone inspection should confirm that frog eyes stay visible in circular and rounded-square launcher masks, the frog is recognizable at 48 dp, and startup labels have sufficient contrast on both day and night forests.
