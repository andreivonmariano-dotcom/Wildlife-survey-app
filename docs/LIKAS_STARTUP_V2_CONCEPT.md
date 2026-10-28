# LIKAS | Rainforest Welcome v2: Design Review Brief

**Date:** 28 October 2026 (Asia/Manila)  
**Status:** New visual and interaction prototype produced for owner review. Not approved final art, not integrated in native LIKAS, and not a shipped APK.  
**Baseline:** `9e4059192afdf0d9478481f816fe5e4bcd8a2087` before this design documentation.  
**Applies to:** G-07, U-01, U-03, U-04, U-05, U-06, E-01.

## Intent

Replace the *retired* legacy forest scene with a fresh Philippine tropical rainforest design and a clearer field-first welcome screen. Keep the current Android app's database, survey history, package identity, signing identity, and core fieldwork navigation unchanged until separately tested.

## Visual concept

- One coherent composition with textured forest canopy, distant Philippine mountain forms, river, and waterfall, using a restrained pixel-art finish rather than large low-resolution squares.
- Four time states using the **same composition** to avoid geometry jumping: morning, day, dusk, and night.
- Prototype review assets have output dimensions 1224 x 1668 pixels, derived from a 612 x 834 crop of a newly generated concept board. That is review quality and **not** proof of native high-resolution detail. Commission or render full-resolution source art before Android production.
- Retain approved LIKAS greens, cream text, and the existing frog launcher icon. The launcher icon is independent from the background and was not changed by this task.
- Actual review files and the interactive browser prototype were attached directly in the originating ChatGPT conversation. **They have not been uploaded to this GitHub repository** and must not be treated as current Android app assets.

## Proposed welcome composition

1. Scenic top hero with accessible, separate UI text: LIKAS, WILDLIFE FIELD STUDIO, a short supporting statement, and one large **Start a field survey** action.
2. Category-selection panel for Birds, Herpetofauna, Mammals, and Custom, preserving the existing native survey setup paths.
3. Scrollable field workspace showing actual current features: survey history, field dashboard, offline species checklist, projects/sites, scientific data review, and legacy import.
4. No dead buttons for Habitat Atlas, Research Watch, or analysis features that are still roadmap items.
5. An active survey must offer a prominent **Resume active survey** action in place of an ambiguous new-survey primary CTA. Do not let decorative content conceal survey controls.

## Time and ambient motion

Use the phone's **local time** for gradual crossfades. The *prototype* uses illustrative periods rather than astronomical sunrise/sunset:
- 05:00-06:36: night to morning.
- 06:36-09:00: morning to day.
- 09:00-16:00: day.
- 16:00-18:00: day to dusk.
- 18:00-20:00: dusk to night.
- 20:00-05:00: night.

Subtle motion can include limited camera drift, a separate foreground foliage layer, moving cloud/mist effects, water shimmer, and low-intensity nighttime fireflies. Never tile a visibly non-seamless image across the screen or shift all layers at identical speed and call it parallax.

For native Android, animate on vsync with `postInvalidateOnAnimation()` or `Choreographer`, not a busy timer. **60 FPS is a target subject to physical-device evidence**, not a guaranteed property of the preview. Restrict animation to the visible ambient surface. Suspend it on `onPause`, off-screen, when Reduce Motion is selected, and in battery saver. Provide static fallback.

## Accessibility and performance

- All text and buttons are real Android views and remain usable with TalkBack, large font scaling, and direct sunlight.
- Keep primary touch targets at least 48 dp, prefer 52-56 dp, and do not place field actions under an artwork overlay.
- Maintain adequate dark scrim and text contrast for each lighting state.
- Decode appropriately sized artwork once. Check memory and jank on lower-end API 26+ Android phones.
- Do not create a WebView replacement for existing field capture. The HTML prototype is a visual test, not the app implementation.

## Validation completed on visual prototype

- Tested offline HTML in local Chromium through direct content rendering at 390 px and 320 px widths.
- Confirmed four survey categories appear in the preview chooser.
- Confirmed forced night opacity reaches 1.
- No browser JavaScript exceptions were observed in those tests.
- Reduced-motion mode was exercised.
- Separate 30 FPS MP4 scene study was exported.

## Not verified

No native Android code or app assets were changed by this design task. No new APK was built. No 60 FPS handset measurement, phone screenshot, accessibility audit, power-use assessment, or old-record migration test has been performed.

## Next gate

Owner reviews the actual day/night screenshots and movement, then approves or revises the composition. Once approved, prepare native-detail forest source plates and layered assets, integrate as a `WelcomeActivity` hero on an isolated Android branch, preserve existing navigation and data, test CI and an Android phone, and deliver a *newly built* APK as a direct in-chat file.
