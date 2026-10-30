# LIKAS RC61 | Main Library icon import and Photos/Sources optical crop

**Date:** 31 October 2026, Asia/Manila. **Status:** Android 14 emulator QA passed; development preview, not field-ready.
**Source:** RC60 draft PR #57, head `3892059a6843e56d2458a038be0ca9548729594c`, successful Android run `41096525113`, preview code120.
**User directive:** apply the two newly generated Library illustrations for Conservation and Habitat links, and fix visual clipping of Photos and Sources icons.

## Implementation
- Two new **generated** PNG art resources, `res/drawable-nodpi/likas_generated_conservation.png` and `likas_generated_habitat_links.png`, derived from the owner's in-conversation generated imagery. They retain the shield/leaf and linked forest/wetland imagery, and receive an optical transparent margin plus indexed 64×64 pixel-art optimization. Distinguish these newly selected images from the **original approved 18 PNGs**, which remain byte-identical.
- Library Conservation and Habitat links map directly to these icon resources. Retire the unused prior source-drawn `PixelShield`.
- Photos and Sources keep the **original approved camera and reports source PNGs**; they receive Library-only alias names and a 40dp displayed art footprint inside the unchanged 50dp plate. This addresses apparent crowding at the icon edges without cropping or editing the raster source.
- Use `CENTER_INSIDE` for icon images, with exact 50dp plate, at least 5dp internal optical spacing on the two previously full-bleed art items. Other art display and the Seven Library destination routes are retained.
- New `versionCode 121`, `versionName 0.27.3-rc61-library-icon-preview`, same `org.phwildlife.survey.beta08`, original signer and SQLite 27. No scientific data, GPS, survey, taxa, history, offline photographs or cited conservation statuses modified.

## Validation gate
- Python PNG decoder independently checks PNG CRCs, indexed 64px dimensions, transparency and decodable compressed image bytes.
- Real Android 14 emulator runs main Library top/bottom at 320dp/100%, 360dp/100%, 412dp/100%, 412dp/130%, 320dp/200%. It confirms all 7 destinations remain accessible, every new icon loads, Photos/Sources image sizes are 40dp with non-cropping scale type, Conservation/Habitat links new images occupy 50dp plates, and no label clipping or last-action reachability issue. RC58 Discover and RC51 research ZIP regression also run.
- CI separately checks exact original 18 icon hash locks, pinned signer, app/JVM build and scientific tests. Keep draft until actual screenshots and signed APK are verified; physical device/TalkBack/historic-data upgrade remain open.

## Acceptance after screenshot-collection correction | 31 October 2026
- Original RC61 workflow 41101788907 compiled and passed **354 Python tests** but failed screenshot collection because the instrumented test saved MediaStore screenshots under RC60 naming. **Corrected only the test screenshot prefix** at code commit `0ebf16fcfeb174e3d090b90674b34ae86cee9476`.
- Workflow **41145603571 SUCCESS**: signed APK compilation, original signing-identity check, actual Android 14 main Library screenshots at 320/360/412dp with 100/130/200% font, **nine Android instrumentation tests** including main Library, Discover and scientific ZIP; source/PNG tests passed. Actual RC60/RC61 bottom-of-Library screenshot comparison inspected; generated Conservation and Habitat links art loaded, Photos and Sources had adequate optical spacing and all seven routes remained accessible.
- Signed APK artifact **13021775109**, size **9791533 bytes**, SHA256 `3b62ed0798eb078a4ac40d866c4d9b94e28545b24b18983b3401096eecdfce88`. Independently verified ZIP integrity, Android APK Signature Scheme v2 RSA PKCS1 SHA256 signed data, complete signed-content digest in 12 chunks, and pinned certificate `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`. Screenshot/log evidence artifact **13021760299**. Direct APK and evidence attached in owner conversation. Do not describe as physical-device or historical-data upgrade acceptance.
