# LIKAS RC61 | Main Library icon import and Photos/Sources optical crop

**Date:** 31 October 2026, Asia/Manila. **Status:** source and asset implementation staged; Android emulator QA pending.
**Source:** RC60 draft PR #57, head `3892059a6843e56d2458a038be0ca9548729594c`, successful Android run `41096525113`, preview code120.
**User directive:** apply the two newly generated Library illustrations for Conservation and Habitat links, and fix visual clipping of Photos and Sources icons.

## Implementation
- Two new **generated** PNG art resources, `res/drawable-nodpi/likas_generated_conservation.png` and `likas_generated_habitat_links.png`, derived from the owner's in-conversation generated imagery. They retain the shield/leaf and linked forest/wetland imagery, and receive an optical transparent margin plus indexed 96×96 pixel-art optimization. Distinguish these newly selected images from the **original approved 18 PNGs**, which remain byte-identical.
- Library Conservation and Habitat links map directly to these icon resources. Retire the unused prior source-drawn `PixelShield`.
- Photos and Sources keep the **original approved camera and reports source PNGs**; they receive Library-only alias names and a 40dp displayed art footprint inside the unchanged 50dp plate. This addresses apparent crowding at the icon edges without cropping or editing the raster source.
- Use `CENTER_INSIDE` for icon images, with exact 50dp plate, at least 5dp internal optical spacing on the two previously full-bleed art items. Other art display and the Seven Library destination routes are retained.
- New `versionCode 121`, `versionName 0.27.3-rc61-library-icon-preview`, same `org.phwildlife.survey.beta08`, original signer and SQLite 27. No scientific data, GPS, survey, taxa, history, offline photographs or cited conservation statuses modified.

## Validation gate
- Python PNG decoder independently checks PNG CRCs, indexed 96px dimensions, transparency and decodable compressed image bytes.
- Real Android 14 emulator runs main Library top/bottom at 320dp/100%, 360dp/100%, 412dp/100%, 412dp/130%, 320dp/200%. It confirms all 7 destinations remain accessible, every new icon loads, Photos/Sources image sizes are 40dp with non-cropping scale type, Conservation/Habitat links new images occupy 50dp plates, and no label clipping or last-action reachability issue. RC58 Discover and RC51 research ZIP regression also run.
- CI separately checks exact original 18 icon hash locks, pinned signer, app/JVM build and scientific tests. Keep draft until actual screenshots and signed APK are verified; physical device/TalkBack/historic-data upgrade remain open.
