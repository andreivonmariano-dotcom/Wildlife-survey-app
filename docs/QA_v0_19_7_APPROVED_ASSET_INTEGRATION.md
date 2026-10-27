# LIKAS v0.19.7: approved asset integration

Date: October 27, 2026
User-provided source: `LIKAS_approved_assets.zip`

## What is actually in the APK

Three WebP images have been restored directly from their existing Git blobs in the GitHub repository. The file hashes match the approved user upload **byte for byte**.

| Installed file | Origin in the approved ZIP | SHA-256 |
| --- | --- | --- |
| `app/src/main/assets/likas_forest/forest_day.webp` | `startup_background/forest_day.webp` | `d89e3707b5fdbb3c2317a678f77cb46caeb2754ad1893d46397f25ba9d6fad41` |
| `app/src/main/assets/likas_forest/forest_night.webp` | `startup_background/forest_night.webp` | `238e18a31c1ac5be631103fe4dd44d09aaddc39c54f469c7589baa392e53012b` |
| `app/src/main/res/drawable-nodpi/likas_launcher_art.webp` | `launcher_icon/likas_launcher_art.webp` | `ad9079af4b9dcac0a024410a1989e28012d2c98052d88e295c9dd693df2d33d5` |

The source files already existed in repository history at commit `c7c5742f322621e91d3d96a8659ea53077bd3a16` (forests) and the `main` branch commit `44825d70956151839bd8d90b14ef799c5ce8ea76` (icon). They were not present in the v0.19.6 development tree. The original Git blobs were restored without image conversion, regeneration, compression, or metadata changes.

Also restored from the user-provided version: `ic_launcher.xml`, `ic_launcher_round.xml`, `likas_colors.xml`. Replaced prior launcher icon and removed an older conflicting color definition.

## Startup presentation

`LikasForestView` now reads the original day/night images from Android assets using BitmapFactory. It shows the exact user artwork with nearest-neighbor scaling (no smoothing). Slow native horizontal panning, swaying leaves, ambient cloud strips, falling water highlights, surface ripples, and firefly lights animate over the original static artwork. The illustrative 48-frame GIF previews are **not** embedded in the app, matching their status as visual references rather than approved playback frames.

The local-time night/day crossfade starts during dawn and dusk. Animation pauses in inactive windows and respects reduced/disabled Android system animation settings. Target redraw interval: 100 milliseconds. No online requests.

`LikasLaunchActivity` displays the approved illustrated pixel icon instead of the substitute circular leaf mark. `WelcomeActivity` uses the same approved image, and Android launcher adaptive icons now reference the supplied WebP artwork.

## Checks

- `scripts/test_likas_branding.py` verifies the uploaded SHA-256 asset hashes, Android icon resources, native animation wiring, and the permanent End Survey action.
- Existing Python tests, Android JUnit suite, APK compilation, GPS rules, and survey completion flow tests run in GitHub Actions.
- Device visual acceptance remains needed: inspect pixel-art crop across 320 dp and 360 dp portrait phones, all three launcher icon masks, sunrise and dusk playback, and screen-reader behavior.
- No change to SQLite schema, sampling protocol rules, station IDs, survey outcomes, GPS accuracy requirements, taxon checklists, photo records, or exports.

## Design limitations

The original mobile background images are 240×80 panoramas, not portrait screenshots. The native background scales and crops them to fill portrait phone screens, then moves slowly across the artwork. This preserves pixel details and avoids distorting their original proportions. Users see different parts of the approved panorama over time rather than the entire 3:1 scene at once.

The source icon's 432×432 WebP includes the LIKAS wordmark and rainforest image. The screen shows it at 164 dp to avoid adding a second, duplicate wordmark.
