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

## Startup-screen handoff

The previously committed `ForestBackdropView.java` and day/night WebP forest plates remain at `app/src/main/assets/likas_forest/`. In the startup view, place the ambient forest behind all controls. Give the LIKAS title large cream lettering; below it place the small caption **WILDLIFE SURVEY**. Use forest-green buttons with ivory text, and retain ample spacing. Refer to `docs/LIKAS_FOREST_BACKGROUND.md` for timing and animation guidance.

The main menu is still its existing Android layout. Its hero typography and action colors now use the LIKAS identity. The animated forest itself is **not yet attached** to the activity. The other LIKAS development chat can connect it without changing the recording workflow.

## Small screen and accessibility

The adaptive artwork is masked by the Android launcher, so all identifying text stays near its center. Keep button text as standard Android TextViews, not as part of pixel graphics. Test all icon masks and check legibility at 48 dp. Low-light forest artwork should not reduce contrast of buttons or data labels.
