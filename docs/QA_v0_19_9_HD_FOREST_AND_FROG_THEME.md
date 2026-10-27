# LIKAS v0.19.9: frog startup palette and original-resolution forest

Date: 2026-10-27
Base: `feature/v0.19.8-minimal-frog-icon`
Branch: `feature/v0.19.9-frog-theme-smooth-forest`

## Requested adjustments

### Q1: launcher shape previews

The approved frog icon was checked with circular and rounded-square masks and a 48 dp size mock-up. The previews were exported from the actual selected `frog-icon-q78.webp` design, not unrelated generated artwork. The same icon remains connected to the app's normal/round adaptive launcher resources and Home/Launch screens.

### Q2: frog-inspired launch theme

- Status bar, navigation bar, and background use deep pond green `#08271E`.
- Main/secondary actions use cream `#EFF3DF` and dark green borders.
- Heading and subtitle use cream and pale sage. A darker green scrim supports text contrast on both daylight scenes.
- Retains separate LIKAS name and frog emblem, Resume Survey and New Survey/Home & Tools actions.
- Survey forms, export records, and taxonomic data remain unchanged.

### Q3: higher detail and smooth movement

**Source artwork restored in this exact branch:**

| Android asset | Resolution | Size | SHA-256 |
| --- | --- | --- | --- |
| `app/src/main/assets/likas_forest/forest_day_hd.webp` | 2172×724 | 480,134 bytes | `8b62eda698354db6d410964a342e5ccbe8cd284e3577a51fc72e4839dface76f` |
| `app/src/main/assets/likas_forest/forest_night_hd.webp` | 2172×724 | 408,336 bytes | `a21568f109b863cb86a27b8dfae87aebfa656914252232b6c70fc0c95f1467af` |

These are resized **only for WebP compression** at the originals' full pixel dimensions. The input images come from `LIKAS_approved_assets.zip`, not an enlarged 240×80 image. GitHub binary blob hashes have been validated against the local produced WebP bytes.

The renderer loads HD day/night first, reverting to the 240×80 WebP source only if an HD asset is missing. The new HD files are bundled in this branch's APK.

The previous implementation drew one integer-rounded source rectangle every 100 ms, which made the image visibly jump in 10 FPS steps. The current implementation uses a reusable Android `Matrix` to translate each image by fractional pixels. Native bilinear bitmap filtering, a 33 ms frame interval target (up to ~30 FPS), a 96-second seamless pan cycle and cosine-based travel deliver gradual motion. The looping curve is unit tested, including start/end continuity and maximum per-frame displacement.

A high-resolution RGB_565 decoded bitmap uses roughly `2172 × 724 × 2 = 3,145,728` bytes, so day and night artwork together occupy about 6 MiB of decoded memory before Android's rendering surfaces and caches. No frame bitmaps are allocated during draws. The animation ceases repeated redraws on hidden screens or with Android animations turned off.

Day-to-night crossfade and native low-cost cloud, water, foliage and firefly motion remain active.

## Validation

GitHub Actions runs Android APK assembly, policy unit tests, and Python checks. `scripts/test_likas_branding.py` verifies the actual high-resolution WebP file hashes, image loader priority, and 33 ms image filtering configuration.

**Remaining phone checks:** validate launcher masks on current Android launchers, 320 dp and 360 dp phone layouts, motion stability, GPU frame pacing, day/night transitions, background battery usage and the newest APK's install/upgrade path. Code targets are not measured FPS or battery results.

Survey schemas, method parameters, GPS accuracy requirements, survey end outcome distinctions, elevation datum, checklists, history and exports are left unchanged.
