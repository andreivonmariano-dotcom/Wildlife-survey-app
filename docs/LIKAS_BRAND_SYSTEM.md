# LIKAS identity: retro wildlife field app

## Brand direction

A deliberately limited pixel-art identity inspired by a tropical Philippine forest. It depicts a frog, mountains, a waterfall, and the **LIKAS** wordmark. The refined launcher art was reduced to an 80 × 80 pixel color grid with **20 colors**, enlarged with nearest-neighbor sampling to 432 × 432. The resulting offline WebP is lossless. It is concept-art-derived and pixel-refined, not a claim of manually painted individual pixels.

## Installed Android assets

- `res/drawable-nodpi/likas_launcher_art.webp` contains the native 432 × 432 foreground illustration.
- `res/mipmap-anydpi-v26/ic_launcher.xml` declares the adaptive launcher icon.
- `res/mipmap-anydpi-v26/ic_launcher_round.xml` declares the round variant.
- `res/values/likas_colors.xml` provides named forest palette colors.
- AndroidManifest labels the app **LIKAS** and points to both icons. Minimum SDK 26 supports adaptive icons.
- The home hero in `WelcomeActivity` now shows **LIKAS / WILDLIFE SURVEY**. Primary menu green follows the forest palette via `UiKit`.

## Colors

- Deep forest / icon backing: `#08271E`
- Forest green / primary action: `#1C5B3E`
- Canopy / dark surface: `#133F31`
- Moss / accents: `#718E42`
- River / cool accents: `#639899`
- Cream / logo: `#FFF0C1`
- Mist / supporting labels: `#B6D8D1`

## Startup scenery reset, 28 October 2026

The earlier forest day/night artwork, dedicated `ForestBackdropView` class, and startup-scene instructions have been removed at the owner's direction. The existing static welcome UI remains in place. No new background or animation is selected yet. The adaptive launcher icon and named LIKAS colors are separate brand resources and remain installed.

The installed main menu remains the existing static Android layout. A separate four-state rainforest visual prototype was produced on 28 October 2026 for review, but its art and motion were **not** installed into the Android activity. See `docs/LIKAS_STARTUP_V2_CONCEPT.md`. Do not reconnect the retired forest implementation.

## Small screen and accessibility

The adaptive artwork is masked by the Android launcher, so all identifying text stays near its center. Keep button text as standard Android TextViews, not as part of pixel graphics. Test all icon masks and check legibility at 48 dp. Low-light forest artwork should not reduce contrast of buttons or data labels.
