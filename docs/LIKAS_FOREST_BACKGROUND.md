# LIKAS pixel forest startup background

This package is for the **LIKAS** Android startup screen. It does not modify `WelcomeActivity` or its buttons.

## Included assets

- `app/src/main/assets/likas_forest/forest_day.webp`
- `app/src/main/assets/likas_forest/forest_night.webp`
- `app/src/main/java/org/phwildlife/survey/ForestBackdropView.java`

The WebP plates are **240 x 80 native-pixel retro previews** derived from the generated day/night panoramas. Scale with nearest-neighbor sampling to retain sharp pixels. They are compact and offline-friendly. The original generated panoramas were 2172 x 724 pixels. Replace the WebP plates with higher-resolution copies under the same filenames for more detail.

## Integration handoff

Instantiate `new ForestBackdropView(context)` as the first, full-screen child of the startup root container. Position the existing LIKAS title, **Start Survey**, **Continue Survey**, and version label above it. Keep all buttons clickable. No activity or database workflow change is required.

The `ForestBackdropView` provides a **code-driven seamless-time loop**, not a video or a sequence of independently generated scene frames. It reads the phone's local time, crossfades from night to day around **05:15-06:30**, and from day to night around **17:45-19:15**. Times are visual defaults, not astronomical sunrise data. The device locale/timezone controls them.

Motion includes slow horizontal back-and-forth camera panning, subtle foreground leaf sway, pixel cloud wisps, water ripples, waterfall sparkle lines, and glowing light motes. The camera reverses smoothly instead of wrapping from the right edge to the left. The supplied original images do not have mathematically validated tile seams. The oscillating pan prevents a visible join.

The view draws at roughly 15 frames per second on screen. Motion stops when detached or hidden and can be switched off with `setMotionEnabled(false)`. This is designed for low overhead on field devices.

## Important layout note

The art is a **3:1 panorama**. Portrait phones crop heavily if a full-height, aspect-correct `cover` fit is used. Review the waterfall composition on actual handsets. A separately painted portrait variant will yield a more controlled crop. Keep a dark, readable overlay behind the title/buttons if needed.

## Notes for the integrating chat

The 4 AI-generated still frames per lighting condition from the artwork exploration are **not exact frame-matched animation**. Cycling them directly creates scene jumps. The stable single images and the Java animation layer here are the intended startup implementation.

Assets are independent from user observations and never require an internet connection.
